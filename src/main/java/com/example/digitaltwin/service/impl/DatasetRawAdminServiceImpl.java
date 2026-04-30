package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.DatasetRaw;
import com.example.digitaltwin.mapper.DatasetRawMapper;
import com.example.digitaltwin.service.DatasetRawAdminService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DatasetRawAdminServiceImpl implements DatasetRawAdminService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final DatasetRawMapper datasetRawMapper;

    public DatasetRawAdminServiceImpl(DatasetRawMapper datasetRawMapper) {
        this.datasetRawMapper = datasetRawMapper;
    }

    @Override
    public PageResult<DatasetRaw> listPage(String animalType, String chemicalName, String indicatorName, int pageNum, int pageSize) {
        String normalizedAnimalType = normalizeAnimalType(animalType, false);
        String normalizedChemicalName = blankToNull(chemicalName);
        String normalizedIndicatorName = blankToNull(indicatorName);

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        long total = datasetRawMapper.countAll(normalizedAnimalType, normalizedChemicalName, normalizedIndicatorName);
        int offset = (pageNum - 1) * pageSize;
        List<DatasetRaw> records = datasetRawMapper.selectPage(normalizedAnimalType, normalizedChemicalName, normalizedIndicatorName, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public DatasetRaw getById(Long id) {
        DatasetRaw record = datasetRawMapper.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("基础数据不存在");
        }
        return record;
    }

    @Override
    public DatasetRaw create(DatasetRaw datasetRaw) {
        validatePayload(datasetRaw);
        DatasetRaw toCreate = normalizePayload(datasetRaw);
        datasetRawMapper.insert(toCreate);
        return datasetRawMapper.selectById(toCreate.getId());
    }

    @Override
    public DatasetRaw update(Long id, DatasetRaw datasetRaw) {
        getById(id);
        validatePayload(datasetRaw);
        DatasetRaw toUpdate = normalizePayload(datasetRaw);
        toUpdate.setId(id);
        datasetRawMapper.updateById(toUpdate);
        return datasetRawMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        datasetRawMapper.deleteById(id);
    }

    private void validatePayload(DatasetRaw datasetRaw) {
        if (datasetRaw == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (!StringUtils.hasText(datasetRaw.getAnimalType())) {
            throw new IllegalArgumentException("物种不能为空");
        }
        if (!StringUtils.hasText(datasetRaw.getChemicalName())) {
            throw new IllegalArgumentException("化学物质不能为空");
        }
        if (!StringUtils.hasText(datasetRaw.getIndicatorName())) {
            throw new IllegalArgumentException("指标名称不能为空");
        }
        requirePositive(datasetRaw.getDosage(), "剂量");
        requireNonNull(datasetRaw.getIndicatorValue(), "观测数值");
        requireNonNull(datasetRaw.getTemperature(), "环境温度");
    }

    private DatasetRaw normalizePayload(DatasetRaw datasetRaw) {
        DatasetRaw normalized = new DatasetRaw();
        normalized.setAnimalType(normalizeAnimalType(datasetRaw.getAnimalType(), true));
        normalized.setChemicalName(datasetRaw.getChemicalName().trim());
        normalized.setDosage(datasetRaw.getDosage());
        normalized.setIndicatorName(datasetRaw.getIndicatorName().trim());
        normalized.setIndicatorValue(datasetRaw.getIndicatorValue());
        normalized.setTemperature(datasetRaw.getTemperature());
        return normalized;
    }

    private static String normalizeAnimalType(String animalType, boolean required) {
        if (!StringUtils.hasText(animalType)) {
            if (required) {
                throw new IllegalArgumentException("物种不能为空");
            }
            return null;
        }
        String normalized = animalType.trim().toUpperCase();
        return switch (normalized) {
            case "MOUSE", "RABBIT", "FROG" -> normalized;
            default -> throw new IllegalArgumentException("不支持的物种：" + animalType);
        };
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + "必须大于 0");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
}
