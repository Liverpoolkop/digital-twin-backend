package com.example.digitaltwin.service.impl;

import com.example.digitaltwin.common.ExperimentStatuses;
import com.example.digitaltwin.dto.PageResult;
import com.example.digitaltwin.entity.Experiment;
import com.example.digitaltwin.mapper.ExperimentMapper;
import com.example.digitaltwin.security.AuthenticatedUser;
import com.example.digitaltwin.security.SecurityUtils;
import com.example.digitaltwin.service.ExperimentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExperimentServiceImpl implements ExperimentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ExperimentMapper experimentMapper;

    public ExperimentServiceImpl(ExperimentMapper experimentMapper) {
        this.experimentMapper = experimentMapper;
    }

    @Override
    public PageResult<Experiment> listPage(String name, String status, String sortBy, String order, int pageNum, int pageSize) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        String orderColumn = resolveOrderColumn(sortBy);
        String orderDirection = resolveOrderDirection(order);
        String normalizedStatus = normalizeStatus(status);

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }

        long total = experimentMapper.countVisible(
                blankToNull(name),
                normalizedStatus,
                currentUser.getId(),
                SecurityUtils.isAdmin()
        );
        int offset = (pageNum - 1) * pageSize;
        List<Experiment> records = experimentMapper.selectVisiblePage(
                blankToNull(name),
                normalizedStatus,
                currentUser.getId(),
                SecurityUtils.isAdmin(),
                orderColumn,
                orderDirection,
                offset,
                pageSize
        );

        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public Experiment getById(Long id) {
        Experiment experiment = requireExperiment(id);
        assertReadable(experiment);
        return experiment;
    }

    @Override
    public Experiment create(Experiment experiment) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUser();
        validateEditablePayload(experiment);

        Experiment toCreate = new Experiment();
        toCreate.setName(experiment.getName().trim());
        toCreate.setAnimalType(experiment.getAnimalType().trim().toUpperCase());
        toCreate.setChemicalName(experiment.getChemicalName().trim());
        toCreate.setIndicatorName(experiment.getIndicatorName().trim());

        // 【核心修复：新增这3行，接收前端传值并提供默认兜底】
        String mode = experiment.getSimulationMode();
        toCreate.setSimulationMode(StringUtils.hasText(mode) ? mode.trim() : "SINGLE");
        toCreate.setTargetOrgans(blankToNull(experiment.getTargetOrgans()));

        toCreate.setDescription(experiment.getDescription());
        toCreate.setStatus(ExperimentStatuses.DRAFT);
        toCreate.setSubmittedBy(currentUser.getId());
        toCreate.setReviewedBy(null);
        toCreate.setReviewedTime(null);
        toCreate.setReviewComment(null);

        experimentMapper.insert(toCreate);
        return experimentMapper.selectById(toCreate.getId());
    }

    @Override
    public Experiment update(Long id, Experiment experiment) {
        Experiment existing = requireExperiment(id);
        assertEditable(existing);
        validateEditablePayload(experiment);

        existing.setName(experiment.getName().trim());
        existing.setAnimalType(experiment.getAnimalType().trim().toUpperCase());
        existing.setChemicalName(experiment.getChemicalName().trim());
        existing.setIndicatorName(experiment.getIndicatorName().trim());

        // 【核心修复：新增这3行，确保编辑时也能更新仿真模式】
        String mode = experiment.getSimulationMode();
        existing.setSimulationMode(StringUtils.hasText(mode) ? mode.trim() : "SINGLE");
        existing.setTargetOrgans(blankToNull(experiment.getTargetOrgans()));

        existing.setDescription(experiment.getDescription());
        existing.setReviewedBy(null);
        existing.setReviewedTime(null);
        if (ExperimentStatuses.REJECTED.equals(existing.getStatus())) {
            existing.setStatus(ExperimentStatuses.DRAFT);
            existing.setReviewComment(null);
        }

        experimentMapper.updateById(existing);
        return experimentMapper.selectById(id);
    }

    @Override
    public Experiment submit(Long id) {
        Experiment existing = requireExperiment(id);
        assertOwner(existing);
        if (!ExperimentStatuses.DRAFT.equals(existing.getStatus())
                && !ExperimentStatuses.REJECTED.equals(existing.getStatus())) {
            throw new IllegalStateException("只有草稿或已驳回的实验申请才可提交审批");
        }

        existing.setStatus(ExperimentStatuses.PENDING);
        existing.setReviewedBy(null);
        existing.setReviewedTime(null);
        existing.setReviewComment(null);
        experimentMapper.updateById(existing);
        return experimentMapper.selectById(id);
    }

    @Override
    public Experiment approve(Long id, String reviewComment) {
        Experiment existing = requireExperiment(id);
        assertAdmin();
        if (!ExperimentStatuses.PENDING.equals(existing.getStatus())) {
            throw new IllegalStateException("只有待审批实验才能审批通过");
        }

        experimentMapper.updateStatus(id, ExperimentStatuses.APPROVED, SecurityUtils.getCurrentUser().getId(), blankToNull(reviewComment));
        return experimentMapper.selectById(id);
    }

    @Override
    public Experiment reject(Long id, String reviewComment) {
        Experiment existing = requireExperiment(id);
        assertAdmin();
        if (!ExperimentStatuses.PENDING.equals(existing.getStatus())) {
            throw new IllegalStateException("只有待审批实验才能驳回");
        }

        experimentMapper.updateStatus(id, ExperimentStatuses.REJECTED, SecurityUtils.getCurrentUser().getId(), blankToNull(reviewComment));
        return experimentMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        Experiment existing = requireExperiment(id);
        if (SecurityUtils.isAdmin()) {
            experimentMapper.deleteById(id);
            return;
        }
        assertOwner(existing);
        if (ExperimentStatuses.PENDING.equals(existing.getStatus())) {
            throw new IllegalStateException("待审批实验不允许删除，请等待审批或联系管理员");
        }
        if (ExperimentStatuses.APPROVED.equals(existing.getStatus())) {
            throw new IllegalStateException("已审批通过的实验不允许删除");
        }
        experimentMapper.deleteById(id);
    }

    private Experiment requireExperiment(Long id) {
        Experiment experiment = experimentMapper.selectById(id);
        if (experiment == null) {
            throw new IllegalArgumentException("实验记录不存在");
        }
        return experiment;
    }

    private void assertReadable(Experiment experiment) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        boolean owner = currentUserId.equals(experiment.getSubmittedBy());
        boolean approved = ExperimentStatuses.APPROVED.equals(experiment.getStatus());
        if (!owner && !approved) {
            throw new IllegalStateException("无权查看该实验申请");
        }
    }

    private void assertEditable(Experiment experiment) {
        assertOwner(experiment);
        if (ExperimentStatuses.PENDING.equals(experiment.getStatus())) {
            throw new IllegalStateException("待审批实验不可修改");
        }
        if (ExperimentStatuses.APPROVED.equals(experiment.getStatus())) {
            throw new IllegalStateException("已审批通过的实验不可修改");
        }
    }

    private void assertOwner(Experiment experiment) {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        if (!currentUserId.equals(experiment.getSubmittedBy())) {
            throw new IllegalStateException("只能操作自己的实验申请");
        }
    }

    private void assertAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new IllegalStateException("仅管理员可执行该操作");
        }
    }

    private void validateEditablePayload(Experiment experiment) {
        if (experiment == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (!StringUtils.hasText(experiment.getName())) {
            throw new IllegalArgumentException("实验名称不能为空");
        }
        if (!StringUtils.hasText(experiment.getAnimalType())) {
            throw new IllegalArgumentException("实验动物不能为空");
        }
        if (!StringUtils.hasText(experiment.getChemicalName())) {
            throw new IllegalArgumentException("化学物质不能为空");
        }
        if (!StringUtils.hasText(experiment.getIndicatorName())) {
            throw new IllegalArgumentException("观测指标不能为空");
        }
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private static String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case ExperimentStatuses.DRAFT,
                 ExperimentStatuses.PENDING,
                 ExperimentStatuses.APPROVED,
                 ExperimentStatuses.REJECTED -> normalized;
            default -> throw new IllegalArgumentException("不支持的实验状态：" + status);
        };
    }

    private static String resolveOrderColumn(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "id";
        }
        return switch (sortBy.trim()) {
            case "createdTime" -> "created_time";
            case "updatedTime" -> "updated_time";
            default -> "id";
        };
    }

    private static String resolveOrderDirection(String order) {
        if (!StringUtils.hasText(order)) {
            return "ASC";
        }
        return "desc".equalsIgnoreCase(order.trim()) ? "DESC" : "ASC";
    }
}
