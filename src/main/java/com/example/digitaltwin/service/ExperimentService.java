package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;

public interface ExperimentService {

    /**
     * 分页列表（可选名称模糊、排序、分页）
     *
     * @param sortBy      id 或 createdTime
     * @param order       asc 或 desc（大小写不敏感）
     * @param pageNum     从 1 开始
     * @param pageSize    每页条数，上限由实现约束
     */
    PageResult<Experiment> listPage(String name, String sortBy, String order, int pageNum, int pageSize);

    Experiment getById(Long id);

    Experiment create(Experiment experiment);

    Experiment update(Long id, Experiment experiment);

    void delete(Long id);
}
