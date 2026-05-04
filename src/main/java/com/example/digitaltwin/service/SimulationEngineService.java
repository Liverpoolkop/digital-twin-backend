package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.AiComparisonResult;
import com.example.digitaltwin.dto.MultiIndicatorAiComparisonRequest;
import com.example.digitaltwin.dto.MultiOrganAiComparisonResult;
import com.example.digitaltwin.dto.MultiOrganSimulationRequest;
import com.example.digitaltwin.dto.MultiOrganSimulationResult;
import com.example.digitaltwin.dto.SimulationRequest;
import com.example.digitaltwin.entity.SimulationRecord;

public interface SimulationEngineService {

    /**
     * 执行数字孪生仿真预测
     *
     * @param req 前端传入的仿真参数
     * @return 持久化后的仿真记录（含预测值与数据库 ID）
     */
    SimulationRecord runSimulation(SimulationRequest req);

    /**
     * 执行多器官协同仿真预测
     *
     * @param req 多器官仿真请求参数
     * @return 多器官仿真结果
     */
    MultiOrganSimulationResult runMultiOrganSimulation(MultiOrganSimulationRequest req);

    /**
     * 执行AI对比模式仿真
     * 同时返回AI预测曲线和三种数学拟合曲线
     *
     * @param req 仿真请求参数
     * @return AI对比结果（包含4条曲线）
     */
    AiComparisonResult runAiComparison(SimulationRequest req);

    /**
     * 执行多指标AI对比模式仿真
     * 为每个指标返回AI预测曲线和三种数学拟合曲线
     *
     * @param req 多指标AI对比请求参数
     * @return 多指标AI对比结果
     */
    MultiOrganAiComparisonResult runMultiOrganAiComparison(MultiIndicatorAiComparisonRequest req);
}
