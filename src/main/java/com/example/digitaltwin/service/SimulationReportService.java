package com.example.digitaltwin.service;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.SimulationRecord;

public interface SimulationReportService {

    PageResult<SimulationRecord> listCurrentUserRecords(int pageNum, int pageSize);
}
