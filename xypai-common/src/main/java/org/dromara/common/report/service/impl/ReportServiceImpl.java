package org.dromara.common.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.bo.ReportReviewBo;
import org.dromara.common.report.domain.bo.ReportSubmitBo;
import org.dromara.common.report.domain.entity.Punishment;
import org.dromara.common.report.domain.entity.Report;
import org.dromara.common.report.mapper.PunishmentMapper;
import org.dromara.common.report.mapper.ReportMapper;
import org.dromara.common.report.service.IReportService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 举报服务实现
 * Report Service Implementation
 *
 * @author XiangYuPai Team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements IReportService {

    private final ReportMapper reportMapper;
    private final PunishmentMapper punishmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> submitReport(ReportSubmitBo submitBo) {
        Long reporterId = LoginHelper.getUserId();
        log.info("提交举报 - 举报人: {}, 被举报用户: {}, 内容类型: {}",
                 reporterId, submitBo.getReportedUserId(), submitBo.getContentType());

        // 检查是否重复举报
        LambdaQueryWrapper<Report> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Report::getReporterId, reporterId)
                    .eq(Report::getContentType, submitBo.getContentType())
                    .eq(Report::getContentId, submitBo.getContentId())
                    .eq(Report::getDeleted, 0);

        Long existingCount = reportMapper.selectCount(checkWrapper);
        if (existingCount > 0) {
            return R.fail("您已举报过该内容");
        }

        // 创建举报记录
        Report report = BeanUtil.toBean(submitBo, Report.class);
        report.setReporterId(reporterId);
        report.setStatus(0);  // 0=待审核

        reportMapper.insert(report);

        log.info("举报提交成功 - ID: {}", report.getId());
        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> reviewReport(ReportReviewBo reviewBo) {
        Long reviewerId = LoginHelper.getUserId();
        log.info("审核举报 - 举报ID: {}, 审核结果: {}", reviewBo.getReportId(), reviewBo.getReviewResult());

        // 查询举报记录
        Report report = reportMapper.selectById(reviewBo.getReportId());
        if (report == null) {
            return R.fail("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            return R.fail("该举报已审核");
        }

        // 更新举报记录
        report.setStatus(2);  // 2=已处理
        report.setReviewerId(reviewerId);
        report.setReviewResult(reviewBo.getReviewResult());
        report.setReviewRemark(reviewBo.getReviewRemark());
        report.setReviewedAt(new Date());

        reportMapper.updateById(report);

        // 根据审核结果执行处罚
        if (reviewBo.getReviewResult() == 1) {
            // 1=警告 (暂不实施具体处罚)
            log.info("审核结果: 警告 - 用户ID: {}", report.getReportedUserId());

        } else if (reviewBo.getReviewResult() == 2) {
            // 2=删除内容 (需要调用对应模块的删除接口)
            log.info("审核结果: 删除内容 - 内容类型: {}, 内容ID: {}",
                     report.getContentType(), report.getContentId());
            // TODO: 调用对应模块删除内容

        } else if (reviewBo.getReviewResult() == 3) {
            // 3=封禁用户
            log.info("审核结果: 封禁用户 - 用户ID: {}, 时长: {}分钟",
                     report.getReportedUserId(), reviewBo.getPunishmentDuration());

            Punishment punishment = new Punishment();
            punishment.setUserId(report.getReportedUserId());
            punishment.setReportId(report.getId());
            punishment.setType("ban");
            punishment.setReason(reviewBo.getReviewRemark());
            punishment.setDuration(reviewBo.getPunishmentDuration());
            punishment.setStartTime(new Date());

            if (reviewBo.getPunishmentDuration() != null) {
                // 计算结束时间
                long endTimeMillis = System.currentTimeMillis() +
                                   (reviewBo.getPunishmentDuration() * 60L * 1000L);
                punishment.setEndTime(new Date(endTimeMillis));
            }

            punishment.setStatus(0);  // 0=生效中
            punishment.setOperatorId(reviewerId);
            punishment.setOperatorRemark(reviewBo.getReviewRemark());

            punishmentMapper.insert(punishment);
        }

        log.info("举报审核完成 - ID: {}", reviewBo.getReportId());
        return R.ok(true);
    }

    @Override
    public boolean isUserBanned(Long userId) {
        LambdaQueryWrapper<Punishment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Punishment::getUserId, userId)
               .eq(Punishment::getType, "ban")
               .eq(Punishment::getStatus, 0)  // 0=生效中
               .eq(Punishment::getDeleted, 0);

        // 检查是否有生效中的封禁
        Punishment punishment = punishmentMapper.selectOne(wrapper);
        if (punishment == null) {
            return false;
        }

        // 检查是否已过期
        if (punishment.getEndTime() != null && punishment.getEndTime().before(new Date())) {
            // 自动解除过期封禁
            punishment.setStatus(2);  // 2=已过期
            punishmentMapper.updateById(punishment);
            return false;
        }

        return true;
    }

    @Override
    public boolean isUserMuted(Long userId) {
        LambdaQueryWrapper<Punishment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Punishment::getUserId, userId)
               .eq(Punishment::getType, "mute")
               .eq(Punishment::getStatus, 0)
               .eq(Punishment::getDeleted, 0);

        Punishment punishment = punishmentMapper.selectOne(wrapper);
        if (punishment == null) {
            return false;
        }

        if (punishment.getEndTime() != null && punishment.getEndTime().before(new Date())) {
            punishment.setStatus(2);
            punishmentMapper.updateById(punishment);
            return false;
        }

        return true;
    }
}
