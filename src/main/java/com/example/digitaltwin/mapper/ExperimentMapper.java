package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.Experiment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExperimentMapper {

    /** 条件计数（与分页查询 WHERE 一致） */
    long countByName(@Param("name") String name);

    /**
     * 分页查询。orderColumn / orderDirection 仅允许服务端白名单映射，禁止前端直接拼 SQL。
     */
    List<Experiment> selectPage(
            @Param("name") String name,
            @Param("orderColumn") String orderColumn,
            @Param("orderDirection") String orderDirection,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /** 按主键查询 */
    Experiment selectById(Long id);

    /** 新增 */
    int insert(Experiment experiment);

    /** 按主键更新 */
    int updateById(Experiment experiment);

    /** 按主键删除 */
    int deleteById(Long id);
}
