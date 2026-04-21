package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.DatasetRaw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DatasetRawMapper {

    /**
     * 查询训练集：按物种 + 化学物质 + 指标名称 + 温度区间精确筛选
     *
     * @param animalType    物种
     * @param chemicalName  化学物质
     * @param indicatorName 观测指标名称（只取同类指标做回归，避免混用不同量纲数据）
     * @param minTemp       最低温度（含）
     * @param maxTemp       最高温度（含）
     */
    List<DatasetRaw> selectTrainingData(
            @Param("animalType")    String     animalType,
            @Param("chemicalName")  String     chemicalName,
            @Param("indicatorName") String     indicatorName,
            @Param("minTemp")       BigDecimal minTemp,
            @Param("maxTemp")       BigDecimal maxTemp
    );
}
