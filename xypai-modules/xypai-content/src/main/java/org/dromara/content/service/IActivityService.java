package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.dto.ActivityListQueryDTO;
import org.dromara.content.domain.dto.ActivityPublishDTO;
import org.dromara.content.domain.dto.ActivityRegisterDTO;
import org.dromara.content.domain.entity.Activity;
import org.dromara.content.domain.entity.ActivityParticipant;
import org.dromara.content.domain.entity.ActivityType;
import org.dromara.content.domain.vo.ActivityDetailVO;
import org.dromara.content.domain.vo.ActivityListVO;

import java.util.List;

/**
 * 活动服务接口
 *
 * @author XiangYuPai
 */
public interface IActivityService {

    /**
     * 获取活动列表
     *
     * @param queryDTO      查询参数
     * @param currentUserId 当前用户ID(可为null)
     * @return 活动列表分页
     */
    Page<ActivityListVO> getActivityList(ActivityListQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取活动详情
     *
     * @param activityId 活动ID
     * @param userId     当前用户ID(可为null)
     * @return 活动详情
     */
    ActivityDetailVO getActivityDetail(Long activityId, Long userId);

    /**
     * 发布活动
     *
     * @param publishDTO 发布参数
     * @param userId     用户ID
     * @return 活动ID
     */
    Long publishActivity(ActivityPublishDTO publishDTO, Long userId);

    /**
     * 报名活动
     *
     * @param registerDTO 报名参数
     * @param userId      用户ID
     * @return 参与者记录ID
     */
    Long registerActivity(ActivityRegisterDTO registerDTO, Long userId);

    /**
     * 取消报名
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @param reason     取消原因
     * @return 是否成功
     */
    boolean cancelRegistration(Long activityId, Long userId, String reason);

    /**
     * 审核报名
     *
     * @param activityId    活动ID
     * @param participantId 参与者ID
     * @param approved      是否通过
     * @param userId        操作人ID(发起人)
     * @return 是否成功
     */
    boolean approveRegistration(Long activityId, Long participantId, boolean approved, Long userId);

    /**
     * 取消活动
     *
     * @param activityId 活动ID
     * @param userId     操作人ID(发起人)
     * @param reason     取消原因
     * @return 是否成功
     */
    boolean cancelActivity(Long activityId, Long userId, String reason);

    /**
     * 获取活动参与者列表
     *
     * @param activityId 活动ID
     * @param status     状态(可选)
     * @return 参与者列表
     */
    List<ActivityParticipant> getParticipants(Long activityId, String status);

    /**
     * 获取用户参与的活动列表
     *
     * @param userId 用户ID
     * @param status 状态(可选)
     * @return 参与记录列表
     */
    List<ActivityParticipant> getUserParticipations(Long userId, String status);

    /**
     * 获取所有活动类型
     *
     * @return 活动类型列表
     */
    List<ActivityType> getAllActivityTypes();

    /**
     * 获取热门活动类型
     *
     * @return 热门活动类型列表
     */
    List<ActivityType> getHotActivityTypes();

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
