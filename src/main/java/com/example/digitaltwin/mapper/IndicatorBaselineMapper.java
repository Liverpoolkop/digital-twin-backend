package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.IndicatorBaseline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 生理指标基准 Mapper
 */
@Mapper
public interface IndicatorBaselineMapper {

    /**
     * 根据物种、器官、指标查询基准值
     *
     * @param species 物种
     * @param organName 器官名称
     * @param indicatorName 指标名称
     * @return 基准值配置
     */
    @Select("SELECT * FROM sys_indicator_baseline " +
            "WHERE species = #{species} " +
            "AND organ_name = #{organName} " +
            "AND indicator_name = #{indicatorName}")
    IndicatorBaseline selectByCondition(@Param("species") String species,
                                        @Param("organName") String organName,
                                        @Param("indicatorName") String indicatorName);
}
