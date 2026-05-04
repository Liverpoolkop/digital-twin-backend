package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.AiComparisonResult;
import com.example.digitaltwin.dto.IndicatorAiComparison;
import com.example.digitaltwin.dto.MultiIndicatorAiComparisonRequest;
import com.example.digitaltwin.dto.MultiOrganAiComparisonResult;
import com.example.digitaltwin.dto.MultiOrganSimulationRequest;
import com.example.digitaltwin.dto.MultiOrganSimulationResult;
import com.example.digitaltwin.dto.OrganIndicatorResult;
import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.dto.TimeValuePoint;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.mapper.DatasetRawMapper;
import com.example.digitaltwin.mapper.SimulationRecordMapper;
import com.example.digitaltwin.security.AuthenticatedUser;
import com.example.digitaltwin.security.SecurityUtils;
import com.example.digitaltwin.service.AiPredictService;
import com.example.digitaltwin.service.LlmContextBuilder;
import com.example.digitaltwin.service.SimulationEngineService;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

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
    private final AiPredictService aiPredictService;
    private final LlmContextBuilder contextBuilder;

    public SimulationEngineServiceImpl(DatasetRawMapper datasetRawMapper,
                                       SimulationRecordMapper simulationRecordMapper,
                                       AiPredictService aiPredictService,
                                       LlmContextBuilder contextBuilder) {
        this.datasetRawMapper = datasetRawMapper;
        this.simulationRecordMapper = simulationRecordMapper;
        this.aiPredictService = aiPredictService;
        this.contextBuilder = contextBuilder;
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
                log.info("[POLYNOMIAL] 系数 c0={}, c1={}, c2=, 预测值={}",
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
            case "AI" -> throw new IllegalArgumentException(
                    "AI模式请使用 /api/simulation/run-ai-comparison 接口");
            default -> throw new IllegalArgumentException(
                    "不支持的算法模型：" + req.getAlgorithmModel() + "，可选值：LINEAR / POLYNOMIAL / LOGARITHMIC / AI");
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

    // 器官-指标映射配置
    private static final Map<String, List<IndicatorMeta>> ORGAN_INDICATOR_MAP = Map.of(
        "Heart", List.of(new IndicatorMeta("Heart_Rate", "次/分钟")),
        "Liver", List.of(
            new IndicatorMeta("ALT", "U/L"),
            new IndicatorMeta("AST", "U/L")
        ),
        "Lung", List.of(new IndicatorMeta("Respiratory_Rate", "次/分钟"))
    );

    private static class IndicatorMeta {
        final String name;
        final String unit;
        IndicatorMeta(String name, String unit) {
            this.name = name;
            this.unit = unit;
        }
    }

    @Override
    public MultiOrganSimulationResult runMultiOrganSimulation(MultiOrganSimulationRequest req) {
        Map<String, List<OrganIndicatorResult>> organResults = new HashMap<>();

        for (String organ : req.getOrgans()) {
            List<IndicatorMeta> indicators = ORGAN_INDICATOR_MAP.get(organ);
            if (indicators == null) {
                throw new IllegalArgumentException("未知器官: " + organ);
            }

            List<OrganIndicatorResult> indicatorResults = new ArrayList<>();
            for (IndicatorMeta indicator : indicators) {
                List<DatasetRaw> trainingData = datasetRawMapper.selectByMultiOrganConditions(
                    req.getTargetAnimal(),
                    req.getTargetChemical(),
                    organ,
                    indicator.name,
                    req.getTargetDosage()
                );

                if (trainingData.isEmpty()) {
                    throw new IllegalStateException(
                        String.format("器官 %s 指标 %s 无可用训练数据", organ, indicator.name));
                }

                double predictedValue = trainAndPredict(trainingData, req.getTargetDosage(), req.getSelectedModel());

                OrganIndicatorResult result = new OrganIndicatorResult();
                result.setOrgan(organ);
                result.setIndicatorName(indicator.name);
                result.setPredictedValue(predictedValue);
                result.setUnit(indicator.unit);
                indicatorResults.add(result);
            }

            organResults.put(organ, indicatorResults);
        }

        MultiOrganSimulationResult result = new MultiOrganSimulationResult();
        result.setTargetAnimal(req.getTargetAnimal());
        result.setTargetChemical(req.getTargetChemical());
        result.setTargetDosage(req.getTargetDosage());
        result.setSelectedModel(req.getSelectedModel());
        result.setOrganResults(organResults);
        result.setSimulationTime(LocalDateTime.now());

        return result;
    }

    @Override
    public AiComparisonResult runAiComparison(SimulationRequest req) {
        log.info("[AI对比模式] 开始执行，物种={}, 化学物质={}, 指标={}, 剂量={}",
            req.getAnimalType(), req.getChemicalName(), req.getIndicatorName(), req.getTargetDosage());

        AiComparisonResult result = new AiComparisonResult();

        // 1. 获取训练数据
        List<DatasetRaw> trainingData = datasetRawMapper.selectTrainingData(
            req.getAnimalType(),
            req.getChemicalName(),
            req.getIndicatorName(),
            BigDecimal.valueOf(req.getMinTemp()),
            BigDecimal.valueOf(req.getMaxTemp())
        );

        if (trainingData == null || trainingData.isEmpty()) {
            result.setPredictionSource("AI_FAILED");
            result.setErrorMessage("未找到训练数据");
            return result;
        }

        // 2. 尝试AI预测
        try {
            String context = contextBuilder.buildHistoricalContext(
                req.getAnimalType(), req.getChemicalName(), req.getIndicatorName()
            );
            String systemPrompt = contextBuilder.buildSystemPrompt();
            String userPrompt = contextBuilder.buildUserPrompt(
                req.getAnimalType(), req.getChemicalName(),
                req.getTargetDosage(), req.getIndicatorName(), context
            );

            List<TimeValuePoint> aiCurve = aiPredictService.predict(systemPrompt, userPrompt);

            if (aiCurve != null && !aiCurve.isEmpty()) {
                result.setAiCurve(aiCurve);
                result.setPredictionSource("AI_SUCCESS");
                log.info("[AI对比模式] AI预测成功，获得{}个数据点", aiCurve.size());
            } else {
                result.setPredictionSource("AI_FAILED");
                result.setErrorMessage("AI预测返回空结果");
                log.warn("[AI对比模式] AI预测失败，将只返回数学拟合曲线");
            }
        } catch (Exception e) {
            result.setPredictionSource("AI_FAILED");
            result.setErrorMessage("AI预测异常: " + e.getMessage());
            log.error("[AI对比模式] AI预测异常", e);
        }

        // 3. 生成三种数学拟合曲线
        result.setLinearCurve(generateMathCurveByAlgorithm(trainingData, "LINEAR", req.getTargetDosage()));
        result.setPolynomialCurve(generateMathCurveByAlgorithm(trainingData, "POLYNOMIAL", req.getTargetDosage()));
        result.setLogarithmicCurve(generateMathCurveByAlgorithm(trainingData, "LOGARITHMIC", req.getTargetDosage()));

        log.info("[AI对比模式] 完成，预测来源={}", result.getPredictionSource());
        return result;
    }

    private double trainAndPredict(List<DatasetRaw> trainingData, Double targetDosage, String model) {
        String modelUpper = model.toUpperCase();

        if ("LINEAR".equals(modelUpper)) {
            SimpleRegression regression = new SimpleRegression();
            for (DatasetRaw data : trainingData) {
                regression.addData(data.getDosage().doubleValue(), data.getIndicatorValue().doubleValue());
            }
            return regression.predict(targetDosage);
        } else if ("POLYNOMIAL".equals(modelUpper)) {
            WeightedObservedPoints points = new WeightedObservedPoints();
            for (DatasetRaw data : trainingData) {
                points.add(data.getDosage().doubleValue(), data.getIndicatorValue().doubleValue());
            }
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
            double[] coeffs = fitter.fit(points.toList());
            return coeffs[0] + coeffs[1] * targetDosage + coeffs[2] * targetDosage * targetDosage;
        } else if ("LOGARITHMIC".equals(modelUpper)) {
            SimpleRegression regression = new SimpleRegression();
            for (DatasetRaw data : trainingData) {
                double dosage = data.getDosage().doubleValue();
                if (dosage > 0) {
                    regression.addData(Math.log(dosage), data.getIndicatorValue().doubleValue());
                }
            }
            if (regression.getN() < 2) {
                throw new IllegalStateException("对数回归需要至少2个有效样本（dosage > 0）");
            }
            return regression.predict(Math.log(targetDosage));
        } else if ("AI".equals(modelUpper)) {
            throw new IllegalArgumentException("AI模式请使用 /api/simulation/run-multi-indicator-ai-comparison 接口");
        } else {
            throw new IllegalArgumentException("不支持的算法模型：" + model);
        }
    }

    /**
     * 根据算法生成数学拟合曲线
     *
     * @param trainingData 训练数据
     * @param algorithm 算法类型
     * @param targetDosage 目标剂量
     * @return 时间序列曲线
     */
    private List<TimeValuePoint> generateMathCurveByAlgorithm(List<DatasetRaw> trainingData, String algorithm, Double targetDosage) {
        // 1. 使用指定算法预测目标剂量下的基准值
        double baseValue = trainAndPredict(trainingData, targetDosage, algorithm);

        // 2. 生成时间序列曲线（使用指数衰减模型）
        List<TimeValuePoint> curve = new ArrayList<>();
        double lambda = 0.12;  // 衰减系数

        for (double t = 0; t <= 12.0; t += 0.5) {
            double value = baseValue * Math.exp(-lambda * t);
            TimeValuePoint point = new TimeValuePoint();
            point.setTime(t);
            point.setValue(Math.round(value * 10000.0) / 10000.0);
            curve.add(point);
        }

        return curve;
    }

    @Override
    public MultiOrganAiComparisonResult runMultiOrganAiComparison(MultiIndicatorAiComparisonRequest req) {
        log.info("[多指标AI对比] 开始执行，物种=, 化学物质={}, 指标数量={}, 剂量={}",
            req.getAnimalType(), req.getChemicalName(), req.getIndicatorNames().size(), req.getTargetDosage());

        MultiOrganAiComparisonResult result = new MultiOrganAiComparisonResult();
        List<IndicatorAiComparison> indicators = new ArrayList<>();

        int successCount = 0;
        int totalCount = req.getIndicatorNames().size();

        // 遍历每个指标，分别进行AI对比
        for (String indicatorName : req.getIndicatorNames()) {
            log.info("[多指标AI对比] 处理指标: {}", indicatorName);

            // 构建单指标请求
            SimulationRequest singleReq = new SimulationRequest();
            singleReq.setAnimalType(req.getAnimalType());
            singleReq.setChemicalName(req.getChemicalName());
            singleReq.setIndicatorName(indicatorName);
            singleReq.setTargetDosage(req.getTargetDosage());
            singleReq.setMinTemp(req.getMinTemp());
            singleReq.setMaxTemp(req.getMaxTemp());

            // 调用单指标AI对比
            AiComparisonResult aiResult = runAiComparison(singleReq);

            // 转换为IndicatorAiComparison
            IndicatorAiComparison indicator = new IndicatorAiComparison();
            indicator.setIndicatorName(indicatorName);
            indicator.setAiCurve(aiResult.getAiCurve());
            indicator.setLinearCurve(aiResult.getLinearCurve());
            indicator.setPolynomialCurve(aiResult.getPolynomialCurve());
            indicator.setLogarithmicCurve(aiResult.getLogarithmicCurve());
            indicator.setPredictionSource(aiResult.getPredictionSource());

            indicators.add(indicator);

            if ("AI_SUCCESS".equals(aiResult.getPredictionSource())) {
                successCount++;
            }
        }

        result.setIndicators(indicators);

        // 判断整体预测来源
        if (successCount == totalCount) {
            result.setPredictionSource("AI_SUCCESS");
        } else if (successCount > 0) {
            result.setPredictionSource("AI_PARTIAL");
        } else {
            result.setPredictionSource("AI_FAILED");
        }

        log.info("[多指标AI对比] 完成，成功{}/{}个指标，整体状态={}",
            successCount, totalCount, result.getPredictionSource());

        return result;
    }
}
