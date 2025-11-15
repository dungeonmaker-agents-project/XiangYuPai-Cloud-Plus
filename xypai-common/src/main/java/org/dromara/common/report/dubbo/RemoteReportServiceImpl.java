package org.dromara.common.report.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.api.report.RemoteReportService;
import org.dromara.common.core.domain.R;
import org.dromara.common.report.domain.entity.Punishment;
import org.dromara.common.report.domain.entity.Report;
import org.dromara.common.report.mapper.PunishmentMapper;
import org.dromara.common.report.mapper.ReportMapper;
import org.dromara.common.report.service.IReportService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

/**
 * 举报服务远程调用实现
 * Remote Report Service Implementation (Dubbo Provider)
 *
 * <p>用途: 为其他微服务提供举报和封禁管理的RPC接口</p>
 * <p>调用方: xypai-user, xypai-content, xypai-chat等</p>
 *
 * @author XiangYuPai Team
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteReportServiceImpl implements RemoteReportService {

    private final IReportService reportService;
    private final ReportMapper reportMapper;
    private final PunishmentMapper punishmentMapper;

    @Override
    public R<Boolean> isUserBanned(Long userId) {
        log.debug("RPC调用 - 检查用户是否被封禁: userId={}", userId);

        try {
            boolean banned = reportService.isUserBanned(userId);
            return R.ok(banned);
        } catch (Exception e) {
            log.error("检查用户封禁状态失败: userId={}", userId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> isUserMuted(Long userId) {
        log.debug("RPC调用 - 检查用户是否被禁言: userId={}", userId);

        try {
            boolean muted = reportService.isUserMuted(userId);
            return R.ok(muted);
        } catch (Exception e) {
            log.error("检查用户禁言状态失败: userId={}", userId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> canUserPost(Long userId) {
        log.debug("RPC调用 - 检查用户是否可以发布内容: userId={}", userId);

        try {
            boolean banned = reportService.isUserBanned(userId);
            boolean muted = reportService.isUserMuted(userId);

            boolean canPost = !banned && !muted;
            return R.ok(canPost);
        } catch (Exception e) {
            log.error("检查用户发布权限失败: userId={}", userId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> isContentReported(String contentType, Long contentId) {
        log.info("RPC调用 - 检查内容是否被举报: contentType={}, contentId={}", contentType, contentId);

        try {
            Long count = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                    .eq(Report::getContentType, contentType)
                    .eq(Report::getContentId, contentId)
            );

            return R.ok(count > 0);
        } catch (Exception e) {
            log.error("检查内容举报状态失败: contentType={}, contentId={}", contentType, contentId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }

    @Override
    public R<Integer> getReportCount(String contentType, Long contentId) {
        log.info("RPC调用 - 获取内容被举报次数: contentType={}, contentId={}", contentType, contentId);

        try {
            Long count = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                    .eq(Report::getContentType, contentType)
                    .eq(Report::getContentId, contentId)
            );

            return R.ok(count.intValue());
        } catch (Exception e) {
            log.error("获取内容举报次数失败: contentType={}, contentId={}", contentType, contentId, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> banUser(Long userId, Integer duration, String reason) {
        log.info("RPC调用 - 封禁用户: userId={}, duration={}分钟, reason={}", userId, duration, reason);

        try {
            // 检查是否已存在生效的封禁
            Punishment existingBan = punishmentMapper.selectOne(
                new LambdaQueryWrapper<Punishment>()
                    .eq(Punishment::getUserId, userId)
                    .eq(Punishment::getType, "ban")
                    .eq(Punishment::getStatus, 0)  // 0=生效中
                    .last("LIMIT 1")
            );

            if (existingBan != null) {
                return R.fail("用户已被封禁");
            }

            // 创建封禁记录
            Punishment punishment = new Punishment();
            punishment.setUserId(userId);
            punishment.setType("ban");
            punishment.setReason(reason);
            punishment.setDuration(duration);
            punishment.setStartTime(new Date());

            // 计算结束时间
            if (duration != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.MINUTE, duration);
                punishment.setEndTime(calendar.getTime());
            }

            punishment.setStatus(0);  // 0=生效中

            int inserted = punishmentMapper.insert(punishment);
            return R.ok(inserted > 0);
        } catch (Exception e) {
            log.error("封禁用户失败: userId={}", userId, e);
            return R.fail("封禁失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> muteUser(Long userId, Integer duration, String reason) {
        log.info("RPC调用 - 禁言用户: userId={}, duration={}分钟, reason={}", userId, duration, reason);

        try {
            // 检查是否已存在生效的禁言
            Punishment existingMute = punishmentMapper.selectOne(
                new LambdaQueryWrapper<Punishment>()
                    .eq(Punishment::getUserId, userId)
                    .eq(Punishment::getType, "mute")
                    .eq(Punishment::getStatus, 0)  // 0=生效中
                    .last("LIMIT 1")
            );

            if (existingMute != null) {
                return R.fail("用户已被禁言");
            }

            // 创建禁言记录
            Punishment punishment = new Punishment();
            punishment.setUserId(userId);
            punishment.setType("mute");
            punishment.setReason(reason);
            punishment.setDuration(duration);
            punishment.setStartTime(new Date());

            // 计算结束时间
            if (duration != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.MINUTE, duration);
                punishment.setEndTime(calendar.getTime());
            }

            punishment.setStatus(0);  // 0=生效中

            int inserted = punishmentMapper.insert(punishment);
            return R.ok(inserted > 0);
        } catch (Exception e) {
            log.error("禁言用户失败: userId={}", userId, e);
            return R.fail("禁言失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> unbanUser(Long userId) {
        log.info("RPC调用 - 解除用户封禁: userId={}", userId);

        try {
            // 查找生效的封禁记录
            Punishment punishment = punishmentMapper.selectOne(
                new LambdaQueryWrapper<Punishment>()
                    .eq(Punishment::getUserId, userId)
                    .eq(Punishment::getType, "ban")
                    .eq(Punishment::getStatus, 0)  // 0=生效中
                    .last("LIMIT 1")
            );

            if (punishment == null) {
                return R.fail("用户未被封禁");
            }

            // 更新状态为已解除
            punishment.setStatus(1);  // 1=已解除
            int updated = punishmentMapper.updateById(punishment);

            return R.ok(updated > 0);
        } catch (Exception e) {
            log.error("解除封禁失败: userId={}", userId, e);
            return R.fail("解除失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> unmuteUser(Long userId) {
        log.info("RPC调用 - 解除用户禁言: userId={}", userId);

        try {
            // 查找生效的禁言记录
            Punishment punishment = punishmentMapper.selectOne(
                new LambdaQueryWrapper<Punishment>()
                    .eq(Punishment::getUserId, userId)
                    .eq(Punishment::getType, "mute")
                    .eq(Punishment::getStatus, 0)  // 0=生效中
                    .last("LIMIT 1")
            );

            if (punishment == null) {
                return R.fail("用户未被禁言");
            }

            // 更新状态为已解除
            punishment.setStatus(1);  // 1=已解除
            int updated = punishmentMapper.updateById(punishment);

            return R.ok(updated > 0);
        } catch (Exception e) {
            log.error("解除禁言失败: userId={}", userId, e);
            return R.fail("解除失败: " + e.getMessage());
        }
    }

    @Override
    public R<Integer> getUserReportCount(Long userId) {
        log.info("RPC调用 - 获取用户被举报次数: userId={}", userId);

        try {
            Long count = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                    .eq(Report::getReportedUserId, userId)
            );

            return R.ok(count.intValue());
        } catch (Exception e) {
            log.error("获取用户被举报次数失败: userId={}", userId, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> isDuplicateReport(Long reporterId, String contentType, Long contentId) {
        log.debug("RPC调用 - 检查重复举报: reporterId={}, contentType={}, contentId={}",
                reporterId, contentType, contentId);

        try {
            Long count = reportMapper.selectCount(
                new LambdaQueryWrapper<Report>()
                    .eq(Report::getReporterId, reporterId)
                    .eq(Report::getContentType, contentType)
                    .eq(Report::getContentId, contentId)
            );

            return R.ok(count > 0);
        } catch (Exception e) {
            log.error("检查重复举报失败: reporterId={}, contentType={}, contentId={}",
                    reporterId, contentType, contentId, e);
            return R.fail("检查失败: " + e.getMessage());
        }
    }
}
