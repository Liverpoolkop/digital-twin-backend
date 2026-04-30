package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.mapper.DatasetRawMapper;
import com.example.digitaltwin.mapper.SimulationRecordMapper;
import com.example.digitaltwin.security.AuthenticatedUser;
import com.example.digitaltwin.security.SecurityUtils;
import com.example.digitaltwin.service.SimulationEngineService;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Service
public class SimulationEngineServiceImpl implements SimulationEngineService {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngineServiceImpl.class);
    /** 预测结果保留小数位数 */
    private static final int SCALE = 6;

    private final DatasetRawMapper datasetRawMapper;
    private final SimulationRecordMapper simulationRecordMapper;

    public SimulationEngineServiceImpl(DatasetRawMapper datasetRawMapper,
                                       SimulationRecordMapper simulationRecordMapper) {
        this.datasetRawMapper = datasetRawMapper;
        this.simulationRecordMapper = simulationRecordMapper;
    }

    @Override
    public SimulationRecord runSimulation(SimulationRequest req) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();

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

        log.info("[仿真引擎] 用户={}, 训练集大小={}, 算法={}, 目标剂量={}",
                currentUser.getUsername(), trainingData.size(), req.getAlgorithmModel(), req.getTargetDosage());

        // ─── 3. 根据算法标识选择拟合策略并计算预测值 ──────────────────────────
        double predictedValue;
        switch (req.getAlgorithmModel().toUpperCase()) {
            case "LINEAR" -> {
                SimpleRegression linearReg = new SimpleRegression();
                for (DatasetRaw d : trainingData) {
                    linearReg.addData(d.getDosage().doubleValue(), d.getIndicatorValue().doubleValue());
                }
                predictedValue = linearReg.predict(req.getTargetDosage());
                log.info("[LINEAR] 截距={}, 斜率={}, 预测值={}",
                        linearReg.getIntercept(), linearReg.getSlope(), predictedValue);
            }
            case "POLYNOMIAL" -> {
                PolynomialCurveFitter polyFitter = PolynomialCurveFitter.create(2);
                WeightedObservedPoints polyPoints = new WeightedObservedPoints();
                for (DatasetRaw d : trainingData) {
                    polyPoints.add(1.0, d.getDosage().doubleValue(), d.getIndicatorValue().doubleValue());
                }
                double[] coeffs = polyFitter.fit(polyPoints.toList());
                double x = req.getTargetDosage();
                predictedValue = coeffs[0] + coeffs[1] * x + coeffs[2] * x * x;
                log.info("[POLYNOMIAL] 系数 c0={}, c1={}, c2={}, 预测值={}",
                        coeffs[0], coeffs[1], coeffs[2], predictedValue);
            }
            case "LOGARITHMIC" -> {
                SimpleRegression logReg = new SimpleRegression();
                for (DatasetRaw d : trainingData) {
                    double dosageVal = d.getDosage().doubleValue();
                    if (dosageVal <= 0) {
                        log.warn("[LOGARITHMIC] 忽略无效样本：dosage={}", dosageVal);
                        continue;
                    }
                    logReg.addData(Math.log(dosageVal), d.getIndicatorValue().doubleValue());
                }
                if (logReg.getN() < 2) {
                    throw new IllegalStateException("有效训练样本不足（对数回归要求剂量 > 0 的样本至少 2 条）");
                }
                predictedValue = logReg.predict(Math.log(req.getTargetDosage()));
                log.info("[LOGARITHMIC] 截距 a={}, 斜率 b={}, 预测值={}",
                        logReg.getIntercept(), logReg.getSlope(), predictedValue);
            }
            default -> throw new IllegalArgumentException(
                    "不支持的算法模型：" + req.getAlgorithmModel() + "，可选值：LINEAR / POLYNOMIAL / LOGARITHMIC");
        }

        // ─── 4. 封装并持久化仿真记录 ──────────────────────────────────────────
        SimulationRecord record = new SimulationRecord();
        record.setUserId(currentUser.getId());
        record.setExperimentId(req.getExperimentId());
        record.setTargetAnimal(req.getAnimalType());
        record.setTargetChemical(req.getChemicalName());
        record.setIndicatorName(req.getIndicatorName());
        record.setInputDosage(BigDecimal.valueOf(req.getTargetDosage()));
        record.setSelectedModel(req.getAlgorithmModel().toUpperCase());
        record.setPredictedValue(BigDecimal.valueOf(predictedValue).setScale(SCALE, RoundingMode.HALF_UP));

        simulationRecordMapper.insert(record);
        log.info("[仿真引擎] 记录已写入数据库，id={}, userId={}, 预测值={}",
                record.getId(), currentUser.getId(), record.getPredictedValue());

        return record;
    }
}
