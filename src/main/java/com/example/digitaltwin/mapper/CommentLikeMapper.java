package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeMapper {

    int insert(CommentLike commentLike);

    int delete(@Param("commentId") Long commentId,
               @Param("userId") Long userId);

    int exists(@Param("commentId") Long commentId,
               @Param("userId") Long userId);
}
