package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Comment;

import java.util.List;

public interface CommentService {

    Comment create(Long experimentId, Long parentId, String content);

    List<Comment> listPublicTree(Long experimentId);

    List<Comment> listVisibleTreeForCurrentUser(Long experimentId);

    List<Comment> listRecentApproved(int limit);

    Comment like(Long id);

    Comment unlike(Long id);

    PageResult<Comment> listPendingPage(String keyword, Long experimentId, int pageNum, int pageSize);

    Comment audit(Long id, String status, String reviewComment);
}
