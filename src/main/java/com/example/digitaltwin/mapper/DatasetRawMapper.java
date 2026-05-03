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

    /**
     * 多器官查询训练数据（新增organ条件）
     */
    List<DatasetRaw> selectByMultiOrganConditions(@Param("animalType") String animalType,
                                                   @Param("chemicalName") String chemicalName,
                                                   @Param("organ") String organ,
                                                   @Param("indicatorName") String indicatorName,
                                                   @Param("targetDosage") Double targetDosage);

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
