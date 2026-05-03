package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    int insert(Comment comment);

    Comment selectById(@Param("id") Long id);

    List<Comment> selectPublicTreeCandidates(@Param("experimentId") Long experimentId,
                                             @Param("limit") Integer limit);

    List<Comment> selectVisibleForCurrentUser(@Param("experimentId") Long experimentId,
                                              @Param("currentUserId") Long currentUserId);

    List<Comment> selectRecentApproved(@Param("limit") int limit);

    long countPending(@Param("keyword") String keyword,
                      @Param("experimentId") Long experimentId);

    List<Comment> selectPendingPage(@Param("keyword") String keyword,
                                    @Param("experimentId") Long experimentId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    int updateAudit(@Param("id") Long id,
                    @Param("status") String status,
                    @Param("reviewedBy") Long reviewedBy,
                    @Param("reviewComment") String reviewComment);

    int updateLikeCountDelta(@Param("id") Long id,
                             @Param("delta") int delta);
}
