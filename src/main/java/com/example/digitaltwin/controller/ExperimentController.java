package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.service.ExperimentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experiments")
@CrossOrigin(origins = "*")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping
    public Result<PageResult<Experiment>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            return Result.success(experimentService.listPage(name, status, sortBy, order, pageNum, pageSize));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Experiment> getById(@PathVariable Long id) {
        try {
            return Result.success(experimentService.getById(id));
        } catch (IllegalArgumentException e) {
            return Result.notFound();
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<Experiment> create(@RequestBody Experiment experiment) {
        try {
            return Result.success(experimentService.create(experiment));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Experiment> update(@PathVariable Long id, @RequestBody Experiment experiment) {
        try {
            return Result.success(experimentService.update(id, experiment));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/submit")
    public Result<Experiment> submit(@PathVariable Long id) {
        try {
            return Result.success(experimentService.submit(id));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            experimentService.delete(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.notFound();
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }
}
