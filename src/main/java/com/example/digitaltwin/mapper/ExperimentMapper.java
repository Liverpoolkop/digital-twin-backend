package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.Experiment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExperimentMapper {

    long countVisible(@Param("name") String name,
                      @Param("status") String status,
                      @Param("currentUserId") Long currentUserId,
                      @Param("isAdmin") boolean isAdmin);

    List<Experiment> selectVisiblePage(@Param("name") String name,
                                       @Param("status") String status,
                                       @Param("currentUserId") Long currentUserId,
                                       @Param("isAdmin") boolean isAdmin,
                                       @Param("orderColumn") String orderColumn,
                                       @Param("orderDirection") String orderDirection,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    Experiment selectById(Long id);

    int insert(Experiment experiment);

    int updateById(Experiment experiment);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("reviewedBy") Long reviewedBy,
                     @Param("reviewComment") String reviewComment);

    int deleteById(Long id);
}
