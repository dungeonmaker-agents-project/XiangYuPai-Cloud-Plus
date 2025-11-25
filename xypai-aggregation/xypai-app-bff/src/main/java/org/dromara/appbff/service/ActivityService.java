package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.*;
import org.dromara.appbff.domain.vo.*;

/**
 * 组局活动服务接口
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
public interface ActivityService {

    // ========== 活动列表 ==========

    /**
     * 查询活动列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID（用于计算距离等）
     * @return 活动列表结果
     */
    ActivityListResultVO queryActivityList(ActivityListQueryDTO queryDTO, Long userId);

    // ========== 活动详情 ==========

    /**
     * 获取活动详情
     *
     * @param activityId 活动ID
     * @param userId     当前用户ID
     * @return 活动详情
     */
    ActivityDetailVO getActivityDetail(Long activityId, Long userId);

    // ========== 活动报名 ==========

    /**
     * 报名参加活动
     *
     * @param registerDTO 报名信息
     * @param userId      当前用户ID
     * @return 报名结果
     */
    ActivityRegisterResultVO registerActivity(ActivityRegisterDTO registerDTO, Long userId);

    /**
     * 取消活动报名
     *
     * @param cancelDTO 取消信息
     * @param userId    当前用户ID
     * @return 是否成功
     */
    Boolean cancelActivityRegistration(ActivityRegisterCancelDTO cancelDTO, Long userId);

    // ========== 发布组局 ==========

    /**
     * 获取发布组局配置
     *
     * @param userId 当前用户ID
     * @return 配置信息
     */
    ActivityPublishConfigVO getPublishConfig(Long userId);

    /**
     * 发布组局活动
     *
     * @param publishDTO 发布信息
     * @param userId     当前用户ID
     * @return 发布结果
     */
    ActivityPublishResultVO publishActivity(ActivityPublishDTO publishDTO, Long userId);

    // ========== 支付相关 ==========

    /**
     * 支付活动费用（报名费或发布费）
     *
     * @param payDTO 支付信息
     * @param userId 当前用户ID
     * @return 支付结果
     */
    ActivityPayResultVO payActivity(ActivityPayDTO payDTO, Long userId);

    // ========== 发起人操作 ==========

    /**
     * 审核报名申请（仅发起人）
     *
     * @param activityId    活动ID
     * @param participantId 报名用户ID
     * @param approved      是否通过
     * @param userId        当前用户ID（发起人）
     * @return 是否成功
     */
    Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId);

    /**
     * 取消活动（仅发起人）
     *
     * @param activityId 活动ID
     * @param reason     取消原因
     * @param userId     当前用户ID（发起人）
     * @return 是否成功
     */
    Boolean cancelActivity(Long activityId, String reason, Long userId);
}
