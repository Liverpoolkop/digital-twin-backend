package com.example.digitaltwin.entity;

import java.time.LocalDateTime;

/**
 * 评论点赞记录实体
 * 对应表：sys_comment_like
 */
public class CommentLike {

    /** 评论ID */
    private Long commentId;

    /** 点赞用户ID */
    private Long userId;

    /** 点赞时间 */
    private LocalDateTime createTime;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
