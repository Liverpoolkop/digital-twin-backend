package com.example.digitaltwin.controller;

import com.example.digitaltwin.common.Result;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experiments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExperimentController {

    private final ExperimentService experimentService;

    /**
     * 分页列表：可选名称模糊搜索；sortBy=id|createdTime；order=asc|desc
     */
    @GetMapping
    public Result<PageResult<Experiment>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        return Result.success(experimentService.listPage(name, sortBy, order, pageNum, pageSize));
    }

    /** 查询单条 */
    @GetMapping("/{id}")
    public Result<Experiment> getById(@PathVariable Long id) {
        Experiment experiment = experimentService.getById(id);
        return experiment != null ? Result.success(experiment) : Result.notFound();
    }

    /** 新增 */
    @PostMapping
    public Result<Experiment> create(@RequestBody Experiment experiment) {
        return Result.success(experimentService.create(experiment));
    }

    /** 修改 */
    @PutMapping("/{id}")
    public Result<Experiment> update(@PathVariable Long id,
                                     @RequestBody Experiment experiment) {
        Experiment updated = experimentService.update(id, experiment);
        return updated != null ? Result.success(updated) : Result.notFound();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        experimentService.delete(id);
        return Result.success();
    }
}
