package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.CommentCreateRequest;
import com.example.digitaltwin.entity.Comment;
import com.example.digitaltwin.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/public")
    public Result<List<Comment>> listPublic(@RequestParam(required = false) Long experimentId) {
        try {
            return Result.success(commentService.listPublicTree(experimentId));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @GetMapping("/recent")
    public Result<List<Comment>> listRecent(@RequestParam(required = false, defaultValue = "6") int limit) {
        try {
            return Result.success(commentService.listRecentApproved(limit));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @GetMapping
    public Result<List<Comment>> listVisible(@RequestParam(required = false) Long experimentId) {
        try {
            return Result.success(commentService.listVisibleTreeForCurrentUser(experimentId));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Comment> create(@RequestBody CommentCreateRequest request) {
        try {
            return Result.success(commentService.create(
                    request != null ? request.getExperimentId() : null,
                    request != null ? request.getParentId() : null,
                    request != null ? request.getContent() : null
            ));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public Result<Comment> like(@PathVariable Long id) {
        try {
            return Result.success(commentService.like(id));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/like")
    public Result<Comment> unlike(@PathVariable Long id) {
        try {
            return Result.success(commentService.unlike(id));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
