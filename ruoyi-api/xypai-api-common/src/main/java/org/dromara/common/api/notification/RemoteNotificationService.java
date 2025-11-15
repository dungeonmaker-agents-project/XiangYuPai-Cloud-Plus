package org.dromara.common.api.notification;

import org.dromara.common.core.domain.R;

/**
 * 通知服务远程调用接口
 * Remote Notification Service Interface
 *
 * <p>用途：其他微服务通过Dubbo调用此接口发送通知</p>
 * <p>实现：xypai-common模块实现此接口</p>
 *
 * @author XiangYuPai Team
 */
public interface RemoteNotificationService {

    // ==================== 发送通知 ====================

    /**
     * 发送点赞通知
     *
     * @param userId     接收通知的用户ID
     * @param fromUserId 点赞者用户ID
     * @param contentType 被点赞内容类型（post/moment/comment）
     * @param contentId  被点赞内容ID
     * @return 是否成功
     */
    R<Boolean> sendLikeNotification(Long userId, Long fromUserId, String contentType, Long contentId);

    /**
     * 发送评论通知
     *
     * @param userId      接收通知的用户ID
     * @param fromUserId  评论者用户ID
     * @param contentType 被评论内容类型（post/moment）
     * @param contentId   被评论内容ID
     * @param commentText 评论内容摘要
     * @return 是否成功
     */
    R<Boolean> sendCommentNotification(Long userId, Long fromUserId, String contentType,
                                       Long contentId, String commentText);

    /**
     * 发送关注通知
     *
     * @param userId     被关注的用户ID
     * @param fromUserId 关注者用户ID
     * @return 是否成功
     */
    R<Boolean> sendFollowNotification(Long userId, Long fromUserId);

    /**
     * 发送系统通知
     *
     * @param userId  接收通知的用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否成功
     */
    R<Boolean> sendSystemNotification(Long userId, String title, String content);

    /**
     * 批量发送系统通知
     *
     * @param userIds 用户ID列表
     * @param title   通知标题
     * @param content 通知内容
     * @return 是否成功
     */
    R<Boolean> batchSendSystemNotification(Long[] userIds, String title, String content);

    /**
     * 发送活动通知
     *
     * @param userId     接收通知的用户ID
     * @param activityId 活动ID
     * @param title      通知标题
     * @param content    通知内容
     * @return 是否成功
     */
    R<Boolean> sendActivityNotification(Long userId, Long activityId, String title, String content);

    // ==================== 查询未读数 ====================

    /**
     * 获取用户未读通知总数
     *
     * @param userId 用户ID
     * @return 未读数
     */
    R<Long> getUnreadCount(Long userId);

    /**
     * 获取指定类型的未读数
     *
     * @param userId 用户ID
     * @param type   通知类型（like/comment/follow/system/activity）
     * @return 未读数
     */
    R<Long> getUnreadCountByType(Long userId, String type);

    // ==================== 通知管理 ====================

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     * @param userId         用户ID（权限校验）
     * @return 是否成功
     */
    R<Boolean> deleteNotification(Long notificationId, Long userId);
}
