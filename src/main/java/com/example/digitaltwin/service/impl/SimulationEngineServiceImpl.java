package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.mapper.DatasetRawMapper;
import com.example.digitaltwin.mapper.SimulationRecordMapper;
import com.example.digitaltwin.service.SimulationEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 数字孪生仿真引擎实现
 *
 * 支持三种回归算法：
 *   1. LINEAR      - 简单线性回归         y = a + b·x
 *   2. POLYNOMIAL  - 二次多项式回归       y = c0 + c1·x + c2·x²
 *   3. LOGARITHMIC - 对数回归            y = a + b·ln(x)
 *
 * 流程：查询训练集 → 根据算法拟合模型 → 代入目标剂量预测 → 持久化记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationEngineServiceImpl implements SimulationEngineService {

    private final DatasetRawMapper     datasetRawMapper;
    private final SimulationRecordMapper simulationRecordMapper;

    /** 预测结果保留小数位数 */
    private static final int SCALE = 6;

    @Override
    public SimulationRecord runSimulation(SimulationRequest req) {

        // ─── 1. 参数校验 ──────────────────────────────────────────────────────
        if (req.getTargetDosage() == null || req.getTargetDosage() <= 0) {
            throw new IllegalArgumentException("目标剂量必须大于 0");
        }

        // ─── 2. 从数据库获取训练集 ─────────────────────────────────────────────
        //   根据物种 + 化学物质 + 指标名称 + 温度区间精确筛选历史样本
        List<DatasetRaw> trainingData = datasetRawMapper.selectTrainingData(
                req.getAnimalType(),
                req.getChemicalName(),
                req.getIndicatorName(),
                BigDecimal.valueOf(req.getMinTemp()),
                BigDecimal.valueOf(req.getMaxTemp())
        );

        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalStateException(
                    String.format("未找到训练数据：物种=%s，化学物质=%s，指标=%s，温度区间=[%.1f, %.1f]",
                            req.getAnimalType(), req.getChemicalName(), req.getIndicatorName(),
                            req.getMinTemp(), req.getMaxTemp()));
        }

        log.info("[仿真引擎] 训练集大小={}, 算法={}, 目标剂量={}",
                trainingData.size(), req.getAlgorithmModel(), req.getTargetDosage());

        // ─── 3. 根据算法标识选择拟合策略并计算预测值 ──────────────────────────
        double predictedValue;
        switch (req.getAlgorithmModel().toUpperCase()) {

            case "LINEAR" -> {
                /* ── 简单线性回归 y = a + b·x ──────────────────────────────────
                 *  原理：最小二乘法，使 Σ(yi - (a + b·xi))² 最小。
                 *  commons-math3 的 SimpleRegression 自动完成拟合，
                 *  predict(x) 直接给出 x 对应的预测值。
                 * ─────────────────────────────────────────────────────────── */
                SimpleRegression linearReg = new SimpleRegression();
                for (DatasetRaw d : trainingData) {
                    // x = 剂量，y = 指标观测值
                    linearReg.addData(d.getDosage().doubleValue(),
                                      d.getIndicatorValue().doubleValue());
                }
                predictedValue = linearReg.predict(req.getTargetDosage());
                log.info("[LINEAR] 截距={}, 斜率={}, 预测值={}",
                        linearReg.getIntercept(), linearReg.getSlope(), predictedValue);
            }

            case "POLYNOMIAL" -> {
                /* ── 二次多项式回归 y = c0 + c1·x + c2·x² ────────────────────
                 *  原理：PolynomialCurveFitter 基于 Levenberg-Marquardt 非线性最小
                 *  二乘优化，拟合 degree=2 的多项式曲线。
                 *  返回系数数组 coeffs[]：
                 *    coeffs[0] = c0（常数项）
                 *    coeffs[1] = c1（一次项系数）
                 *    coeffs[2] = c2（二次项系数）
                 *  预测：手动代入目标剂量计算 y。
                 * ─────────────────────────────────────────────────────────── */
                PolynomialCurveFitter polyFitter = PolynomialCurveFitter.create(2);
                WeightedObservedPoints polyPoints = new WeightedObservedPoints();
                for (DatasetRaw d : trainingData) {
                    // weight=1.0 表示每个样本权重相等
                    polyPoints.add(1.0,
                                   d.getDosage().doubleValue(),
                                   d.getIndicatorValue().doubleValue());
                }
                double[] coeffs = polyFitter.fit(polyPoints.toList());
                double x = req.getTargetDosage();
                // 代入多项式：c0 + c1·x + c2·x²
                predictedValue = coeffs[0] + coeffs[1] * x + coeffs[2] * x * x;
                log.info("[POLYNOMIAL] 系数 c0={}, c1={}, c2={}, 预测值={}",
                        coeffs[0], coeffs[1], coeffs[2], predictedValue);
            }

            case "LOGARITHMIC" -> {
                /* ── 对数回归 y = a + b·ln(x) ──────────────────────────────────
                 *  原理：令 x' = ln(x)，则原式化为线性形式 y = a + b·x'。
                 *  因此只需将所有样本的 dosage 取自然对数后，
                 *  送入 SimpleRegression 进行普通线性拟合即可。
                 *  预测时同样对目标剂量取对数再代入直线方程。
                 *  注意：剂量必须 > 0（对数定义域要求）。
                 * ─────────────────────────────────────────────────────────── */
                SimpleRegression logReg = new SimpleRegression();
                for (DatasetRaw d : trainingData) {
                    double dosageVal = d.getDosage().doubleValue();
                    if (dosageVal <= 0) {
                        log.warn("[LOGARITHMIC] 忽略无效样本：dosage={}", dosageVal);
                        continue;
                    }
                    // 将 x 替换为 ln(x) 实现线性化
                    logReg.addData(Math.log(dosageVal),
                                   d.getIndicatorValue().doubleValue());
                }
                if (logReg.getN() < 2) {
                    throw new IllegalStateException("有效训练样本不足（对数回归要求剂量 > 0 的样本至少 2 条）");
                }
                // predict(ln(targetDosage)) 等价于计算 a + b·ln(targetDosage)
                predictedValue = logReg.predict(Math.log(req.getTargetDosage()));
                log.info("[LOGARITHMIC] 截距 a={}, 斜率 b={}, 预测值={}",
                        logReg.getIntercept(), logReg.getSlope(), predictedValue);
            }

            default -> throw new IllegalArgumentException(
                    "不支持的算法模型：" + req.getAlgorithmModel()
                    + "，可选值：LINEAR / POLYNOMIAL / LOGARITHMIC");
        }

        // ─── 4. 封装并持久化仿真记录 ──────────────────────────────────────────
        SimulationRecord record = new SimulationRecord();
        record.setUserId(req.getUserId());
        record.setExperimentId(req.getExperimentId());
        record.setTargetAnimal(req.getAnimalType());
        record.setTargetChemical(req.getChemicalName());
        record.setIndicatorName(req.getIndicatorName());
        record.setInputDosage(BigDecimal.valueOf(req.getTargetDosage()));
        record.setSelectedModel(req.getAlgorithmModel().toUpperCase());
        // 保留 SCALE 位小数，四舍五入
        record.setPredictedValue(
                BigDecimal.valueOf(predictedValue).setScale(SCALE, RoundingMode.HALF_UP));

        simulationRecordMapper.insert(record);
        log.info("[仿真引擎] 记录已写入数据库，id={}, 预测值={}", record.getId(), record.getPredictedValue());

        return record;
    }
}
