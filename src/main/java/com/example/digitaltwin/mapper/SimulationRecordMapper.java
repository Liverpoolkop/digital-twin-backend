package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.SimulationRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SimulationRecordMapper {

    /** 插入一条仿真预测记录，自动回填 id */
    int insert(SimulationRecord record);
}
