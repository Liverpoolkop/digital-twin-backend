package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.common.CommentStatuses;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Comment;
import com.example.digitaltwin.entity.CommentLike;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.mapper.CommentLikeMapper;
import com.example.digitaltwin.mapper.CommentMapper;
import com.example.digitaltwin.mapper.ExperimentMapper;
import com.example.digitaltwin.security.AuthenticatedUser;
import com.example.digitaltwin.security.SecurityUtils;
import com.example.digitaltwin.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RECENT_LIMIT = 6;
    private static final int MAX_RECENT_LIMIT = 20;

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final ExperimentMapper experimentMapper;

    public CommentServiceImpl(CommentMapper commentMapper,
                              CommentLikeMapper commentLikeMapper,
                              ExperimentMapper experimentMapper) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.experimentMapper = experimentMapper;
    }

    @Override
    public Comment create(Long experimentId, Long parentId, String content) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        String normalizedContent = normalizeContent(content);
        Long normalizedExperimentId = validateExperimentId(experimentId);

        Comment parent = null;
        if (parentId != null) {
            parent = requireComment(parentId);
            assertCommentReadableForReply(parent, currentUser);
            if (parent.getExperimentId() != null) {
                normalizedExperimentId = parent.getExperimentId();
            }
        }

        Comment comment = new Comment();
        comment.setUserId(currentUser.getId());
        comment.setExperimentId(normalizedExperimentId);
        comment.setParentId(parent != null ? parent.getId() : null);
        comment.setRootId(parent == null ? null : (parent.getRootId() != null ? parent.getRootId() : parent.getId()));
        comment.setReplyToUserId(parent != null ? parent.getUserId() : null);
        comment.setContent(normalizedContent);
        comment.setLikeCount(0);
        comment.setStatus(CommentStatuses.PENDING);
        comment.setRejectReason(null);
        comment.setReviewedBy(null);
        comment.setReviewedTime(null);

        commentMapper.insert(comment);
        return commentMapper.selectById(comment.getId());
    }

    @Override
    public List<Comment> listPublicTree(Long experimentId) {
        Long normalizedExperimentId = validateExperimentId(experimentId);
        return buildTree(commentMapper.selectPublicTreeCandidates(normalizedExperimentId, null));
    }

    @Override
    public List<Comment> listVisibleTreeForCurrentUser(Long experimentId) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        Long normalizedExperimentId = validateExperimentId(experimentId);
        return buildTree(commentMapper.selectVisibleForCurrentUser(normalizedExperimentId, currentUser.getId()));
    }

    @Override
    public List<Comment> listRecentApproved(int limit) {
        int normalizedLimit = limit <= 0 ? DEFAULT_RECENT_LIMIT : Math.min(limit, MAX_RECENT_LIMIT);
        return commentMapper.selectRecentApproved(normalizedLimit);
    }

    @Override
    @Transactional
    public Comment like(Long id) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        Comment comment = requireComment(id);
        if (!CommentStatuses.APPROVED.equals(comment.getStatus()) && !currentUser.getId().equals(comment.getUserId())) {
            throw new IllegalStateException("仅可点赞公开评论");
        }
        if (commentLikeMapper.exists(id, currentUser.getId()) > 0) {
            return reloadForCurrentUser(id, currentUser.getId());
        }

        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(id);
        commentLike.setUserId(currentUser.getId());
        commentLikeMapper.insert(commentLike);
        commentMapper.updateLikeCountDelta(id, 1);
        return reloadForCurrentUser(id, currentUser.getId());
    }

    @Override
    @Transactional
    public Comment unlike(Long id) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        requireComment(id);
        if (commentLikeMapper.delete(id, currentUser.getId()) > 0) {
            commentMapper.updateLikeCountDelta(id, -1);
        }
        return reloadForCurrentUser(id, currentUser.getId());
    }

    @Override
    public PageResult<Comment> listPendingPage(String keyword, Long experimentId, int pageNum, int pageSize) {
        assertAdmin();
        Long normalizedExperimentId = validateExperimentId(experimentId);
        String normalizedKeyword = blankToNull(keyword);

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        long total = commentMapper.countPending(normalizedKeyword, normalizedExperimentId);
        int offset = (pageNum - 1) * pageSize;
        List<Comment> records = commentMapper.selectPendingPage(normalizedKeyword, normalizedExperimentId, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public Comment audit(Long id, String status, String reviewComment) {
        assertAdmin();
        Comment existing = requireComment(id);
        if (!CommentStatuses.PENDING.equals(existing.getStatus())) {
            throw new IllegalStateException("仅待审核评论可执行审核操作");
        }

        String normalizedStatus = normalizeAuditStatus(status);
        String normalizedReviewComment = blankToNull(reviewComment);
        if (CommentStatuses.REJECTED.equals(normalizedStatus) && !StringUtils.hasText(normalizedReviewComment)) {
            throw new IllegalArgumentException("驳回时请填写审核意见");
        }

        commentMapper.updateAudit(id, normalizedStatus, SecurityUtils.getCurrentUser().getId(), normalizedReviewComment);
        return commentMapper.selectById(id);
    }

    private Comment requireComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        return comment;
    }

    private void assertCommentReadableForReply(Comment parent, AuthenticatedUser currentUser) {
        if (CommentStatuses.APPROVED.equals(parent.getStatus())) {
            return;
        }
        if (currentUser.getId().equals(parent.getUserId())) {
            return;
        }
        if (SecurityUtils.isAdmin()) {
            return;
        }
        throw new IllegalStateException("不能回复当前不可见评论");
    }

    private Long validateExperimentId(Long experimentId) {
        if (experimentId == null) {
            return null;
        }
        Experiment experiment = experimentMapper.selectById(experimentId);
        if (experiment == null) {
            throw new IllegalArgumentException("关联实验不存在");
        }
        return experimentId;
    }

    private Comment reloadForCurrentUser(Long id, Long currentUserId) {
        List<Comment> visible = commentMapper.selectVisibleForCurrentUser(null, currentUserId);
        return visible.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .orElseGet(() -> commentMapper.selectById(id));
    }

    private List<Comment> buildTree(List<Comment> flatList) {
        if (flatList == null || flatList.isEmpty()) {
            return List.of();
        }

        Map<Long, Comment> nodeMap = new LinkedHashMap<>();
        for (Comment source : flatList) {
            source.setChildren(new ArrayList<>());
            source.setLikedByCurrentUser(Boolean.TRUE.equals(source.getLikedByCurrentUser()));
            source.setOwnedByCurrentUser(Boolean.TRUE.equals(source.getOwnedByCurrentUser()));
            nodeMap.put(source.getId(), source);
        }

        List<Comment> roots = new ArrayList<>();
        for (Comment comment : nodeMap.values()) {
            Long parentId = comment.getParentId();
            if (parentId != null && nodeMap.containsKey(parentId)) {
                nodeMap.get(parentId).getChildren().add(comment);
            } else {
                roots.add(comment);
            }
        }

        Comparator<Comment> timeDesc = Comparator.comparing(Comment::getCreateTime).reversed();
        sortTree(roots, timeDesc);
        return roots;
    }

    private void sortTree(List<Comment> comments, Comparator<Comment> comparator) {
        comments.sort(comparator);
        for (Comment comment : comments) {
            if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
                sortTree(comment.getChildren(), comparator);
            }
        }
    }

    private void assertAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new IllegalStateException("仅管理员可执行该操作");
        }
    }

    private static String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        String normalized = content.trim();
        if (normalized.length() > 1000) {
            throw new IllegalArgumentException("评论内容不能超过 1000 个字符");
        }
        return normalized;
    }

    private static String normalizeAuditStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("审核状态不能为空");
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case CommentStatuses.APPROVED, CommentStatuses.REJECTED -> normalized;
            default -> throw new IllegalArgumentException("不支持的审核状态：" + status);
        };
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
