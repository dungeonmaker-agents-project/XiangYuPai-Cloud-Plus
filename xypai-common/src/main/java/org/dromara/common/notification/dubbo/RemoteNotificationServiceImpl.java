package org.dromara.common.notification.dubbo;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.api.notification.RemoteNotificationService;
import org.dromara.common.core.domain.R;
import org.dromara.common.notification.domain.Notification;
import org.dromara.common.notification.domain.bo.NotificationCreateBo;
import org.dromara.common.notification.domain.vo.UnreadCountVo;
import org.dromara.common.notification.mapper.NotificationMapper;
import org.dromara.common.notification.service.INotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通知服务远程调用实现
 * Remote Notification Service Implementation (Dubbo Provider)
 *
 * <p>用途: 为其他微服务提供通知相关的RPC接口</p>
 * <p>调用方: xypai-user, xypai-content, xypai-chat等</p>
 *
 * @author XiangYuPai Team
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteNotificationServiceImpl implements RemoteNotificationService {

    private final INotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @Override
    public R<Boolean> sendLikeNotification(Long userId, Long fromUserId,
                                          String contentType, Long contentId) {
        log.info("RPC调用 - 发送点赞通知: userId={}, fromUserId={}, contentType={}, contentId={}",
                userId, fromUserId, contentType, contentId);

        try {
            // 不给自己发通知
            if (userId.equals(fromUserId)) {
                return R.ok(false);
            }

            NotificationCreateBo createBo = new NotificationCreateBo();
            createBo.setUserId(userId);
            createBo.setFromUserId(fromUserId);
            createBo.setType("like");
            createBo.setContentType(contentType);
            createBo.setContentId(contentId);
            createBo.setTitle("点赞通知");
            createBo.setContent("有人赞了你的" + getContentTypeName(contentType));

            return notificationService.createNotification(createBo);
        } catch (Exception e) {
            log.error("发送点赞通知失败", e);
            return R.fail("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> sendCommentNotification(Long userId, Long fromUserId,
                                             String contentType, Long contentId, String commentText) {
        log.info("RPC调用 - 发送评论通知: userId={}, fromUserId={}, contentType={}, contentId={}",
                userId, fromUserId, contentType, contentId);

        try {
            // 不给自己发通知
            if (userId.equals(fromUserId)) {
                return R.ok(false);
            }

            // 评论摘要
            String summary = StrUtil.isBlank(commentText) ? ""
                    : (commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText);

            NotificationCreateBo createBo = new NotificationCreateBo();
            createBo.setUserId(userId);
            createBo.setFromUserId(fromUserId);
            createBo.setType("comment");
            createBo.setContentType(contentType);
            createBo.setContentId(contentId);
            createBo.setTitle("评论通知");
            createBo.setContent("有人评论了你的" + getContentTypeName(contentType) + ": " + summary);

            return notificationService.createNotification(createBo);
        } catch (Exception e) {
            log.error("发送评论通知失败", e);
            return R.fail("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> sendFollowNotification(Long userId, Long fromUserId) {
        log.info("RPC调用 - 发送关注通知: userId={}, fromUserId={}", userId, fromUserId);

        try {
            // 不给自己发通知
            if (userId.equals(fromUserId)) {
                return R.ok(false);
            }

            NotificationCreateBo createBo = new NotificationCreateBo();
            createBo.setUserId(userId);
            createBo.setFromUserId(fromUserId);
            createBo.setType("follow");
            createBo.setTitle("关注通知");
            createBo.setContent("有人关注了你");

            return notificationService.createNotification(createBo);
        } catch (Exception e) {
            log.error("发送关注通知失败", e);
            return R.fail("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> sendSystemNotification(Long userId, String title, String content) {
        log.info("RPC调用 - 发送系统通知: userId={}, title={}", userId, title);

        try {
            NotificationCreateBo createBo = new NotificationCreateBo();
            createBo.setUserId(userId);
            createBo.setType("system");
            createBo.setTitle(title);
            createBo.setContent(content);

            return notificationService.createNotification(createBo);
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
            return R.fail("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> batchSendSystemNotification(Long[] userIds, String title, String content) {
        log.info("RPC调用 - 批量发送系统通知: userIds={}, title={}", Arrays.toString(userIds), title);

        try {
            if (userIds == null || userIds.length == 0) {
                return R.ok(true);
            }

            List<NotificationCreateBo> createBos = new ArrayList<>();
            for (Long userId : userIds) {
                NotificationCreateBo createBo = new NotificationCreateBo();
                createBo.setUserId(userId);
                createBo.setType("system");
                createBo.setTitle(title);
                createBo.setContent(content);
                createBos.add(createBo);
            }

            return notificationService.batchCreateNotifications(createBos);
        } catch (Exception e) {
            log.error("批量发送系统通知失败", e);
            return R.fail("批量发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> sendActivityNotification(Long userId, Long activityId,
                                              String title, String content) {
        log.info("RPC调用 - 发送活动通知: userId={}, activityId={}, title={}",
                userId, activityId, title);

        try {
            NotificationCreateBo createBo = new NotificationCreateBo();
            createBo.setUserId(userId);
            createBo.setType("activity");
            createBo.setContentType("activity");
            createBo.setContentId(activityId);
            createBo.setTitle(title);
            createBo.setContent(content);

            return notificationService.createNotification(createBo);
        } catch (Exception e) {
            log.error("发送活动通知失败", e);
            return R.fail("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public R<Long> getUnreadCount(Long userId) {
        log.debug("RPC调用 - 获取未读通知总数: userId={}", userId);

        try {
            UnreadCountVo countVo = notificationService.getUnreadCount(userId);
            return R.ok(countVo.getTotalCount());
        } catch (Exception e) {
            log.error("获取未读通知总数失败: userId={}", userId, e);
            return R.fail("获取未读数失败: " + e.getMessage());
        }
    }

    @Override
    public R<Long> getUnreadCountByType(Long userId, String type) {
        log.debug("RPC调用 - 获取指定类型未读数: userId={}, type={}", userId, type);

        try {
            UnreadCountVo countVo = notificationService.getUnreadCount(userId);

            long count = switch (type) {
                case "like" -> countVo.getLikeCount();
                case "comment" -> countVo.getCommentCount();
                case "follow" -> countVo.getFollowCount();
                case "system" -> countVo.getSystemCount();
                case "activity" -> countVo.getActivityCount();
                default -> 0L;
            };

            return R.ok(count);
        } catch (Exception e) {
            log.error("获取指定类型未读数失败: userId={}, type={}", userId, type, e);
            return R.fail("获取未读数失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> deleteNotification(Long notificationId, Long userId) {
        log.info("RPC调用 - 删除通知: notificationId={}, userId={}", notificationId, userId);

        try {
            // 验证通知所有权
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                return R.fail("通知不存在");
            }

            if (!notification.getUserId().equals(userId)) {
                return R.fail("无权限删除此通知");
            }

            return notificationService.deleteNotification(notificationId);
        } catch (Exception e) {
            log.error("删除通知失败: notificationId={}, userId={}", notificationId, userId, e);
            return R.fail("删除通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取内容类型名称
     */
    private String getContentTypeName(String contentType) {
        return switch (contentType) {
            case "post" -> "帖子";
            case "moment" -> "动态";
            case "comment" -> "评论";
            default -> "内容";
        };
    }
}
