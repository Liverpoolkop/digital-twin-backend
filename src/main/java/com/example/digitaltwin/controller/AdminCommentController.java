package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.CommentAuditRequest;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Comment;
import com.example.digitaltwin.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@CrossOrigin(origins = "*")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<Comment>> listPending(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long experimentId,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            return Result.success(commentService.listPendingPage(keyword, experimentId, pageNum, pageSize));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/audit")
    public Result<Comment> audit(@PathVariable Long id, @RequestBody(required = false) CommentAuditRequest request) {
        try {
            return Result.success(commentService.audit(
                    id,
                    request != null ? request.getStatus() : null,
                    request != null ? request.getReviewComment() : null
            ));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
