package org.dromara.common.notification.controller.app;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.notification.domain.bo.NotificationQueryBo;
import org.dromara.common.notification.domain.vo.NotificationVo;
import org.dromara.common.notification.domain.vo.UnreadCountVo;
import org.dromara.common.notification.service.INotificationService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知服务C端控制器
 * Notification Service App Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notification")
public class NotificationController extends BaseController {

    private final INotificationService notificationService;

    /**
     * 查询通知列表
     *
     * @param query 查询参数
     * @return 通知列表
     */
    @SaCheckRole("user")
    @GetMapping("/list")
    public TableDataInfo<NotificationVo> getNotifications(@Validated NotificationQueryBo query) {
        return notificationService.queryNotifications(query);
    }

    /**
     * 获取未读数统计
     *
     * @return 未读数统计
     */
    @SaCheckRole("user")
    @GetMapping("/unread-count")
    public R<UnreadCountVo> getUnreadCount() {
        Long userId = LoginHelper.getUserId();
        return R.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * 标记为已读
     *
     * @param id 通知ID
     * @return 是否成功
     */
    @SaCheckRole("user")
    @PutMapping("/{id}/read")
    public R<Boolean> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    /**
     * 批量标记为已读
     *
     * @param ids 通知ID列表
     * @return 是否成功
     */
    @SaCheckRole("user")
    @PutMapping("/batch-read")
    public R<Boolean> batchMarkAsRead(@RequestBody List<Long> ids) {
        return notificationService.batchMarkAsRead(ids);
    }

    /**
     * 全部标记为已读
     *
     * @param type 通知类型 (可选)
     * @return 是否成功
     */
    @SaCheckRole("user")
    @PutMapping("/read-all")
    public R<Boolean> markAllAsRead(@RequestParam(required = false) String type) {
        Long userId = LoginHelper.getUserId();
        return notificationService.markAllAsRead(userId, type);
    }

    /**
     * 删除通知
     *
     * @param id 通知ID
     * @return 是否成功
     */
    @SaCheckRole("user")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteNotification(@PathVariable Long id) {
        return notificationService.deleteNotification(id);
    }

    /**
     * 清空已读通知
     *
     * @return 是否成功
     */
    @SaCheckRole("user")
    @DeleteMapping("/clear-read")
    public R<Boolean> clearReadNotifications() {
        Long userId = LoginHelper.getUserId();
        return notificationService.clearReadNotifications(userId);
    }
}
