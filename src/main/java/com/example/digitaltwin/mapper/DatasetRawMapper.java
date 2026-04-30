package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.DatasetRaw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DatasetRawMapper {

    List<DatasetRaw> selectTrainingData(@Param("animalType") String animalType,
                                        @Param("chemicalName") String chemicalName,
                                        @Param("indicatorName") String indicatorName,
                                        @Param("minTemp") BigDecimal minTemp,
                                        @Param("maxTemp") BigDecimal maxTemp);

    long countAll(@Param("animalType") String animalType,
                  @Param("chemicalName") String chemicalName,
                  @Param("indicatorName") String indicatorName);

    List<DatasetRaw> selectPage(@Param("animalType") String animalType,
                                @Param("chemicalName") String chemicalName,
                                @Param("indicatorName") String indicatorName,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    DatasetRaw selectById(Long id);

    int insert(DatasetRaw datasetRaw);

    int updateById(DatasetRaw datasetRaw);

    int deleteById(Long id);
}
