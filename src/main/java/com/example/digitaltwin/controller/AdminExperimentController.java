package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.service.ExperimentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/experiments")
@CrossOrigin(origins = "*")
public class AdminExperimentController {

    private final ExperimentService experimentService;

    public AdminExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping("/{id}/approve")
    public Result<Experiment> approve(@PathVariable Long id, @RequestBody(required = false) ReviewRequest request) {
        try {
            String reviewComment = request != null ? request.getReviewComment() : null;
            return Result.success(experimentService.approve(id, reviewComment));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Experiment> reject(@PathVariable Long id, @RequestBody(required = false) ReviewRequest request) {
        try {
            String reviewComment = request != null ? request.getReviewComment() : null;
            return Result.success(experimentService.reject(id, reviewComment));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    public static class ReviewRequest {
        private String reviewComment;

        public String getReviewComment() {
            return reviewComment;
        }

        public void setReviewComment(String reviewComment) {
            this.reviewComment = reviewComment;
        }
    }
}
