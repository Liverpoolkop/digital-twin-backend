package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.DatasetRaw;

public interface DatasetRawAdminService {

    PageResult<DatasetRaw> listPage(String animalType, String chemicalName, String indicatorName, int pageNum, int pageSize);

    DatasetRaw getById(Long id);

    DatasetRaw create(DatasetRaw datasetRaw);

    DatasetRaw update(Long id, DatasetRaw datasetRaw);

    void delete(Long id);
}
