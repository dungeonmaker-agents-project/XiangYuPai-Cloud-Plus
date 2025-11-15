package org.dromara.common.notification.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.notification.domain.bo.NotificationCreateBo;
import org.dromara.common.notification.domain.bo.NotificationQueryBo;
import org.dromara.common.notification.domain.vo.NotificationVo;
import org.dromara.common.notification.domain.vo.UnreadCountVo;

import java.util.List;

/**
 * 通知服务接口
 * Notification Service Interface
 *
 * @author XiangYuPai Team
 */
public interface INotificationService {

    /**
     * 查询通知列表
     *
     * @param query 查询参数
     * @return 通知列表
     */
    TableDataInfo<NotificationVo> queryNotifications(NotificationQueryBo query);

    /**
     * 获取未读数统计
     *
     * @param userId 用户ID
     * @return 未读数统计
     */
    UnreadCountVo getUnreadCount(Long userId);

    /**
     * 创建通知
     *
     * @param createBo 创建参数
     * @return 是否成功
     */
    R<Boolean> createNotification(NotificationCreateBo createBo);

    /**
     * 批量创建通知
     *
     * @param createBos 创建参数列表
     * @return 是否成功
     */
    R<Boolean> batchCreateNotifications(List<NotificationCreateBo> createBos);

    /**
     * 标记为已读
     *
     * @param id 通知ID
     * @return 是否成功
     */
    R<Boolean> markAsRead(Long id);

    /**
     * 批量标记为已读
     *
     * @param ids 通知ID列表
     * @return 是否成功
     */
    R<Boolean> batchMarkAsRead(List<Long> ids);

    /**
     * 全部标记为已读
     *
     * @param userId 用户ID
     * @param type   通知类型 (可选)
     * @return 是否成功
     */
    R<Boolean> markAllAsRead(Long userId, String type);

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 是否成功
     */
    R<Boolean> deleteNotification(Long id);

    /**
     * 清空已读通知
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    R<Boolean> clearReadNotifications(Long userId);
}
