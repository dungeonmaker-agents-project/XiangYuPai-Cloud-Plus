package org.dromara.common.api.report;

import org.dromara.common.core.domain.R;

/**
 * 举报服务远程调用接口
 * Remote Report Service Interface
 *
 * <p>用途：其他微服务通过Dubbo调用此接口进行举报和封禁管理</p>
 * <p>实现：xypai-common模块实现此接口</p>
 *
 * @author XiangYuPai Team
 */
public interface RemoteReportService {

    // ==================== 用户状态检查 ====================

    /**
     * 检查用户是否被封禁
     *
     * @param userId 用户ID
     * @return true=已封禁，false=未封禁
     */
    R<Boolean> isUserBanned(Long userId);

    /**
     * 检查用户是否被禁言
     *
     * @param userId 用户ID
     * @return true=已禁言，false=未禁言
     */
    R<Boolean> isUserMuted(Long userId);

    /**
     * 检查用户是否可以发布内容（未封禁且未禁言）
     *
     * @param userId 用户ID
     * @return true=可以发布，false=不可以发布
     */
    R<Boolean> canUserPost(Long userId);

    // ==================== 内容审核 ====================

    /**
     * 检查内容是否被举报过
     *
     * @param contentType 内容类型（post/moment/comment）
     * @param contentId   内容ID
     * @return true=被举报过，false=未被举报
     */
    R<Boolean> isContentReported(String contentType, Long contentId);

    /**
     * 获取内容被举报次数
     *
     * @param contentType 内容类型
     * @param contentId   内容ID
     * @return 举报次数
     */
    R<Integer> getReportCount(String contentType, Long contentId);

    // ==================== 处罚管理 ====================

    /**
     * 封禁用户
     *
     * @param userId   用户ID
     * @param duration 封禁时长（分钟，null表示永久封禁）
     * @param reason   封禁原因
     * @return 是否成功
     */
    R<Boolean> banUser(Long userId, Integer duration, String reason);

    /**
     * 禁言用户
     *
     * @param userId   用户ID
     * @param duration 禁言时长（分钟，null表示永久禁言）
     * @param reason   禁言原因
     * @return 是否成功
     */
    R<Boolean> muteUser(Long userId, Integer duration, String reason);

    /**
     * 解除用户封禁
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    R<Boolean> unbanUser(Long userId);

    /**
     * 解除用户禁言
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    R<Boolean> unmuteUser(Long userId);

    // ==================== 举报查询 ====================

    /**
     * 获取用户被举报次数
     *
     * @param userId 用户ID
     * @return 被举报次数
     */
    R<Integer> getUserReportCount(Long userId);

    /**
     * 检查用户是否重复举报
     *
     * @param reporterId  举报人ID
     * @param contentType 内容类型
     * @param contentId   内容ID
     * @return true=已举报过，false=未举报过
     */
    R<Boolean> isDuplicateReport(Long reporterId, String contentType, Long contentId);
}
