package org.dromara.content.api;

import org.dromara.content.api.domain.dto.*;
import org.dromara.content.api.domain.vo.*;

import java.util.List;

/**
 * 活动远程服务接口
 *
 * @author XiangYuPai
 */
public interface RemoteActivityService {

    /**
     * 查询活动列表
     *
     * @param queryDTO 查询条件
     * @param userId   当前用户ID
     * @return 活动列表结果
     */
    RemoteActivityPageResult queryActivityList(RemoteActivityQueryDto queryDTO, Long userId);

    /**
     * 获取活动详情
     *
     * @param activityId 活动ID
     * @param userId     当前用户ID
     * @return 活动详情
     */
    RemoteActivityDetailVo getActivityDetail(Long activityId, Long userId);

    /**
     * 发布活动
     *
     * @param publishDTO 发布信息
     * @param userId     用户ID
     * @return 活动ID
     */
    Long publishActivity(RemoteActivityPublishDto publishDTO, Long userId);

    /**
     * 报名活动
     *
     * @param registerDTO 报名信息
     * @param userId      用户ID
     * @return 报名结果
     */
    RemoteActivityRegisterResult registerActivity(RemoteActivityRegisterDto registerDTO, Long userId);

    /**
     * 取消报名
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param reason     取消原因
     * @return 是否成功
     */
    Boolean cancelRegistration(Long activityId, Long userId, String reason);

    /**
     * 审核报名
     *
     * @param activityId    活动ID
     * @param participantId 参与者ID
     * @param approved      是否通过
     * @param userId        发起人ID
     * @return 是否成功
     */
    Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId);

    /**
     * 取消活动
     *
     * @param activityId 活动ID
     * @param userId     发起人ID
     * @param reason     取消原因
     * @return 是否成功
     */
    Boolean cancelActivity(Long activityId, Long userId, String reason);

    /**
     * 获取所有活动类型
     *
     * @return 活动类型列表
     */
    List<RemoteActivityTypeVo> getAllActivityTypes();

    /**
     * 获取热门活动类型
     *
     * @return 热门活动类型列表
     */
    List<RemoteActivityTypeVo> getHotActivityTypes();

    /**
     * 增加浏览量
     *
     * @param activityId 活动ID
     */
    void incrementViewCount(Long activityId);

    /**
     * 增加分享量
     *
     * @param activityId 活动ID
     */
    void incrementShareCount(Long activityId);
}
