package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.SimulationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SimulationRecordMapper {

    int insert(SimulationRecord record);

    long countByUserId(@Param("userId") Long userId);

    List<SimulationRecord> selectByUserId(@Param("userId") Long userId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    long countAll();

    long countCreatedSince(@Param("days") int days);

    long countPendingExperiments();

    long countApprovedExperiments();
}
