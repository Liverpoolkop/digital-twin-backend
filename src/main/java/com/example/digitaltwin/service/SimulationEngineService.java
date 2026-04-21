package com.example.digitaltwin.service;

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
}
