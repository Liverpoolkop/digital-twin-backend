package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.service.SimulationEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数字孪生仿真接口
 *
 * POST /api/simulation/run
 *   请求体：SimulationRequest（JSON）
 *   响应：Result<SimulationRecord>
 */
@Slf4j
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationEngineService simulationEngineService;

    /**
     * 执行仿真预测
     *
     * 前端传入：
     *   userId        操作人 ID
     *   animalType    目标物种（MOUSE / RABBIT / FROG）
     *   chemicalName  化学物质名称
     *   minTemp       环境温度筛选下限（℃）
     *   maxTemp       环境温度筛选上限（℃）
     *   algorithmModel 算法标识（LINEAR / POLYNOMIAL / LOGARITHMIC）
     *   targetDosage  预测剂量
     *
     * 后端返回预测结果及持久化后的记录 ID。
     */
    @PostMapping("/run")
    public Result<SimulationRecord> run(@RequestBody SimulationRequest req) {
        log.info("[SimulationController] 收到仿真请求: {}", req);
        try {
            SimulationRecord result = simulationEngineService.runSimulation(req);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            // 参数不合法（如不支持的算法、剂量 ≤ 0）
            log.warn("[SimulationController] 参数异常: {}", e.getMessage());
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            // 业务异常（如无训练数据、有效样本不足）
            log.warn("[SimulationController] 业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("[SimulationController] 未知异常", e);
            return Result.error("仿真引擎内部错误，请稍后重试");
        }
    }
}
