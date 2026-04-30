package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;

public interface ExperimentService {

    PageResult<Experiment> listPage(String name, String status, String sortBy, String order, int pageNum, int pageSize);

    Experiment getById(Long id);

    Experiment create(Experiment experiment);

    Experiment update(Long id, Experiment experiment);

    Experiment submit(Long id);

    Experiment approve(Long id, String reviewComment);

    Experiment reject(Long id, String reviewComment);

    void delete(Long id);
}
