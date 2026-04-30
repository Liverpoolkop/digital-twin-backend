package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.SimulationRecord;
import com.example.digitaltwin.mapper.SimulationRecordMapper;
import com.example.digitaltwin.security.SecurityUtils;
import com.example.digitaltwin.service.SimulationReportService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationReportServiceImpl implements SimulationReportService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final SimulationRecordMapper simulationRecordMapper;

    public SimulationReportServiceImpl(SimulationRecordMapper simulationRecordMapper) {
        this.simulationRecordMapper = simulationRecordMapper;
    }

    @Override
    public PageResult<SimulationRecord> listCurrentUserRecords(int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        Long userId = SecurityUtils.getCurrentUser().getId();
        long total = simulationRecordMapper.countByUserId(userId);
        int offset = (pageNum - 1) * pageSize;
        List<SimulationRecord> records = simulationRecordMapper.selectByUserId(userId, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }
}
