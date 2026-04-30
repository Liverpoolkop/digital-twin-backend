package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.service.DatasetRawAdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/datasets")
@CrossOrigin(origins = "*")
public class AdminDatasetRawController {

    private final DatasetRawAdminService datasetRawAdminService;

    public AdminDatasetRawController(DatasetRawAdminService datasetRawAdminService) {
        this.datasetRawAdminService = datasetRawAdminService;
    }

    @GetMapping
    public Result<PageResult<DatasetRaw>> list(
            @RequestParam(required = false) String animalType,
            @RequestParam(required = false) String chemicalName,
            @RequestParam(required = false) String indicatorName,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            return Result.success(datasetRawAdminService.listPage(animalType, chemicalName, indicatorName, pageNum, pageSize));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<DatasetRaw> getById(@PathVariable Long id) {
        try {
            return Result.success(datasetRawAdminService.getById(id));
        } catch (IllegalArgumentException e) {
            return Result.notFound();
        }
    }

    @PostMapping
    public Result<DatasetRaw> create(@RequestBody DatasetRaw datasetRaw) {
        try {
            return Result.success(datasetRawAdminService.create(datasetRaw));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<DatasetRaw> update(@PathVariable Long id, @RequestBody DatasetRaw datasetRaw) {
        try {
            return Result.success(datasetRawAdminService.update(id, datasetRaw));
        } catch (IllegalArgumentException e) {
            return Result.error("参数错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            datasetRawAdminService.delete(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.notFound();
        }
    }
}
