package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.mapper.ExperimentMapper;
import com.example.digitaltwin.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperimentServiceImpl implements ExperimentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE     = 100;

    private final ExperimentMapper experimentMapper;

    @Override
    public PageResult<Experiment> listPage(String name, String sortBy, String order, int pageNum, int pageSize) {
        String orderColumn = resolveOrderColumn(sortBy);
        String orderDirection = resolveOrderDirection(order);

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        long total = experimentMapper.countByName(blankToNull(name));
        int offset = (pageNum - 1) * pageSize;
        List<Experiment> records = experimentMapper.selectPage(
                blankToNull(name), orderColumn, orderDirection, offset, pageSize);

        return new PageResult<>(records, total, pageNum, pageSize);
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    /** 仅允许映射到真实列名，防注入 */
    private static String resolveOrderColumn(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "id";
        }
        return switch (sortBy.trim()) {
            case "createdTime" -> "created_time";
            default -> "id";
        };
    }

    private static String resolveOrderDirection(String order) {
        if (!StringUtils.hasText(order)) {
            return "ASC";
        }
        String o = order.trim();
        if ("desc".equalsIgnoreCase(o)) {
            return "DESC";
        }
        return "ASC";
    }

    @Override
    public Experiment getById(Long id) {
        return experimentMapper.selectById(id);
    }

    @Override
    public Experiment create(Experiment experiment) {
        experimentMapper.insert(experiment);
        return experimentMapper.selectById(experiment.getId());
    }

    @Override
    public Experiment update(Long id, Experiment experiment) {
        experiment.setId(id);
        experimentMapper.updateById(experiment);
        return experimentMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        experimentMapper.deleteById(id);
    }
}
