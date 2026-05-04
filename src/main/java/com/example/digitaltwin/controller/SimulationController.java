package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.AiComparisonResult;
import com.example.digitaltwin.dto.MultiIndicatorAiComparisonRequest;
import com.example.digitaltwin.dto.MultiOrganAiComparisonResult;
import com.example.digitaltwin.dto.MultiOrganSimulationRequest;
import com.example.digitaltwin.dto.MultiOrganSimulationResult;
import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.service.SimulationEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 数字孪生仿真接口
 *
 * POST /api/simulation/run
 *   请求体：SimulationRequest（JSON）
 *   响应：Result<SimulationRecord>
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    private final SimulationEngineService simulationEngineService;

    public SimulationController(SimulationEngineService simulationEngineService) {
        this.simulationEngineService = simulationEngineService;
    }

    /**
     * 执行仿真预测
     *
     * 前端传入：
     *   animalType     目标物种（MOUSE / RABBIT / FROG）
     *   chemicalName   化学物质名称
     *   minTemp        环境温度筛选下限（℃）
     *   maxTemp        环境温度筛选上限（℃）
     *   algorithmModel 算法标识（LINEAR / POLYNOMIAL / LOGARITHMIC）
     *   targetDosage   预测剂量
     *
     * 用户身份以后端登录态为准，不再信任前端传入 userId。
     */
    @PostMapping("/run")
    public Result<SimulationRecord> run(@RequestBody SimulationRequest req) {
        log.info("[SimulationController] 收到仿真请求");
        try {
            SimulationRecord result = simulationEngineService.runSimulation(req);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("[SimulationController] 参数异常: {}", e.getMessage());
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("[SimulationController] 业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("[SimulationController] 未知异常", e);
            return Result.error("仿真引擎内部错误，请稍后重试");
        }
    }

    /**
     * 执行多器官协同仿真
     */
    @PostMapping("/run-multi-organ")
    public Result<MultiOrganSimulationResult> runMultiOrgan(@RequestBody MultiOrganSimulationRequest req) {
        log.info("[SimulationController] 收到多器官仿真请求");
        try {
            if (req.getOrgans() == null || req.getOrgans().isEmpty()) {
                return Result.error("器官列表不能为空");
            }
            MultiOrganSimulationResult result = simulationEngineService.runMultiOrganSimulation(req);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("[SimulationController] 参数异常: {}", e.getMessage());
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("[SimulationController] 业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("[SimulationController] 未知异常", e);
            return Result.error("多器官仿真引擎内部错误，请稍后重试");
        }
    }

    /**
     * 执行AI对比模式仿真
     * 返回AI预测曲线和三种数学拟合曲线
     */
    @PostMapping("/run-ai-comparison")
    public Result<AiComparisonResult> runAiComparison(@RequestBody SimulationRequest req) {
        log.info("[SimulationController] 收到AI对比模式请求");
        try {
            AiComparisonResult result = simulationEngineService.runAiComparison(req);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("[SimulationController] 参数异常: {}", e.getMessage());
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("[SimulationController] 业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("[SimulationController] 未知异常", e);
            return Result.error("AI对比模式内部错误，请稍后重试");
        }
    }

    /**
     * 执行多指标AI对比模式仿真
     * 为每个指标返回AI预测曲线和三种数学拟合曲线
     */
    @PostMapping("/run-multi-indicator-ai-comparison")
    public Result<MultiOrganAiComparisonResult> runMultiIndicatorAiComparison(@RequestBody MultiIndicatorAiComparisonRequest req) {
        log.info("[SimulationController] 收到多指标AI对比模式请求，指标数量={}", req.getIndicatorNames().size());
        try {
            MultiOrganAiComparisonResult result = simulationEngineService.runMultiOrganAiComparison(req);
            return Result.success(result);
        } catch (Exception e) {
            log.error("[SimulationController] 多指标AI对比异常", e);
            return Result.error("多指标AI对比内部错误，请稍后重试");
        }
    }
}
