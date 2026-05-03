package com.example.digitaltwin.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台评论实体
 * 对应表：sys_comment
 */
public class Comment {

    /** 主键ID */
    private Long id;

    /** 评论用户ID */
    private Long userId;

    /** 关联实验ID，可为空 */
    private Long experimentId;

    /** 根评论ID；主评论为空 */
    private Long rootId;

    /** 父评论ID；主评论为空 */
    private Long parentId;

    /** 被回复用户ID，可为空 */
    private Long replyToUserId;

    /** 评论内容（纯文本） */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 审核状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 驳回原因/审核备注 */
    private String rejectReason;

    /** 审核人ID */
    private Long reviewedBy;

    /** 审核时间 */
    private LocalDateTime reviewedTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 评论作者昵称 */
    private String nickname;

    /** 被回复用户昵称 */
    private String replyToNickname;

    /** 当前登录用户是否已点赞 */
    private Boolean likedByCurrentUser;

    /** 当前登录用户是否为作者 */
    private Boolean ownedByCurrentUser;

    /** 子回复列表 */
    private List<Comment> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedTime() {
        return reviewedTime;
    }

    public void setReviewedTime(LocalDateTime reviewedTime) {
        this.reviewedTime = reviewedTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getReplyToNickname() {
        return replyToNickname;
    }

    public void setReplyToNickname(String replyToNickname) {
        this.replyToNickname = replyToNickname;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Boolean getOwnedByCurrentUser() {
        return ownedByCurrentUser;
    }

    public void setOwnedByCurrentUser(Boolean ownedByCurrentUser) {
        this.ownedByCurrentUser = ownedByCurrentUser;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public void setChildren(List<Comment> children) {
        this.children = children;
    }
}
