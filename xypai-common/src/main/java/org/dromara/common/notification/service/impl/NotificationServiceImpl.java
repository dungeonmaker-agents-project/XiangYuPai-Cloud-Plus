package org.dromara.common.notification.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.notification.domain.bo.NotificationCreateBo;
import org.dromara.common.notification.domain.bo.NotificationQueryBo;
import org.dromara.common.notification.domain.entity.Notification;
import org.dromara.common.notification.domain.vo.NotificationVo;
import org.dromara.common.notification.domain.vo.UnreadCountVo;
import org.dromara.common.notification.mapper.NotificationMapper;
import org.dromara.common.notification.service.INotificationService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 通知服务实现
 * Notification Service Implementation
 *
 * @author XiangYuPai Team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public TableDataInfo<NotificationVo> queryNotifications(NotificationQueryBo query) {
        Long userId = LoginHelper.getUserId();
        log.info("查询通知列表 - 用户ID: {}, 类型: {}", userId, query.getType());

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getDeleted, 0);

        // 类型筛选
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq(Notification::getType, query.getType());
        }

        // 已读状态筛选
        if (query.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, query.getIsRead());
        }

        wrapper.orderByDesc(Notification::getCreateTime);

        Page<NotificationVo> page = notificationMapper.selectVoPage(
            new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return TableDataInfo.build(page);
    }

    @Override
    public UnreadCountVo getUnreadCount(Long userId) {
        log.debug("获取未读数统计 - 用户ID: {}", userId);

        UnreadCountVo countVo = new UnreadCountVo();

        // 点赞未读数
        countVo.setLikeCount(countUnreadByType(userId, "like"));

        // 评论未读数
        countVo.setCommentCount(countUnreadByType(userId, "comment"));

        // 关注未读数
        countVo.setFollowCount(countUnreadByType(userId, "follow"));

        // 系统通知未读数
        countVo.setSystemCount(countUnreadByType(userId, "system"));

        // 活动通知未读数
        countVo.setActivityCount(countUnreadByType(userId, "activity"));

        // 总未读数
        long total = countVo.getLikeCount() + countVo.getCommentCount() +
                     countVo.getFollowCount() + countVo.getSystemCount() +
                     countVo.getActivityCount();
        countVo.setTotalCount(total);

        return countVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> createNotification(NotificationCreateBo createBo) {
        log.info("创建通知 - 接收用户ID: {}, 类型: {}", createBo.getUserId(), createBo.getType());

        Notification notification = BeanUtil.toBean(createBo, Notification.class);
        notification.setIsRead(0);  // 未读
        notification.setStatus(0);  // 正常

        notificationMapper.insert(notification);

        log.info("通知创建成功 - ID: {}", notification.getId());
        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> batchCreateNotifications(List<NotificationCreateBo> createBos) {
        log.info("批量创建通知 - 数量: {}", createBos.size());

        List<Notification> notifications = createBos.stream()
            .map(bo -> {
                Notification notification = BeanUtil.toBean(bo, Notification.class);
                notification.setIsRead(0);
                notification.setStatus(0);
                return notification;
            })
            .toList();

        notificationMapper.insertBatch(notifications);

        log.info("批量通知创建成功");
        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> markAsRead(Long id) {
        log.info("标记通知为已读 - ID: {}", id);

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return R.fail("通知不存在");
        }

        // 验证权限
        if (!notification.getUserId().equals(LoginHelper.getUserId())) {
            return R.fail("无权操作该通知");
        }

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, id)
                     .set(Notification::getIsRead, 1)
                     .set(Notification::getReadAt, new Date());

        notificationMapper.update(null, updateWrapper);

        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> batchMarkAsRead(List<Long> ids) {
        log.info("批量标记通知为已读 - 数量: {}", ids.size());

        Long userId = LoginHelper.getUserId();

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Notification::getId, ids)
                     .eq(Notification::getUserId, userId)
                     .set(Notification::getIsRead, 1)
                     .set(Notification::getReadAt, new Date());

        notificationMapper.update(null, updateWrapper);

        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> markAllAsRead(Long userId, String type) {
        log.info("全部标记为已读 - 用户ID: {}, 类型: {}", userId, type);

        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId)
                     .eq(Notification::getIsRead, 0);

        if (type != null && !type.isEmpty()) {
            updateWrapper.eq(Notification::getType, type);
        }

        updateWrapper.set(Notification::getIsRead, 1)
                     .set(Notification::getReadAt, new Date());

        notificationMapper.update(null, updateWrapper);

        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteNotification(Long id) {
        log.info("删除通知 - ID: {}", id);

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            return R.fail("通知不存在");
        }

        // 验证权限
        if (!notification.getUserId().equals(LoginHelper.getUserId())) {
            return R.fail("无权删除该通知");
        }

        notificationMapper.deleteById(id);

        return R.ok(true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> clearReadNotifications(Long userId) {
        log.info("清空已读通知 - 用户ID: {}", userId);

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 1);

        notificationMapper.delete(wrapper);

        return R.ok(true);
    }

    /**
     * 统计指定类型的未读数
     */
    private Long countUnreadByType(Long userId, String type) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getType, type)
               .eq(Notification::getIsRead, 0)
               .eq(Notification::getDeleted, 0);

        return notificationMapper.selectCount(wrapper);
    }
}
