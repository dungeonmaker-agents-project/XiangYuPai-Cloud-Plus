package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.content.domain.entity.ActivityParticipant;

import java.util.List;

/**
 * 活动参与者Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface ActivityParticipantMapper extends BaseMapper<ActivityParticipant> {

    /**
     * 查询活动的参与者列表
     *
     * @param activityId 活动ID
     * @param status     状态(可选)
     * @return 参与者列表
     */
    @Select("""
        <script>
        SELECT * FROM activity_participant
        WHERE activity_id = #{activityId}
          AND deleted = 0
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        ORDER BY register_time ASC
        </script>
        """)
    List<ActivityParticipant> selectByActivityId(@Param("activityId") Long activityId,
                                                  @Param("status") String status);

    /**
     * 查询用户参与的活动记录
     *
     * @param userId 用户ID
     * @param status 状态(可选)
     * @return 参与记录列表
     */
    @Select("""
        <script>
        SELECT * FROM activity_participant
        WHERE user_id = #{userId}
          AND deleted = 0
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        ORDER BY register_time DESC
        </script>
        """)
    List<ActivityParticipant> selectByUserId(@Param("userId") Long userId,
                                              @Param("status") String status);

    /**
     * 查询用户在指定活动的参与记录
     *
     * @param activityId 活动ID
     * @param userId     用户ID
     * @return 参与记录
     */
    @Select("""
        SELECT * FROM activity_participant
        WHERE activity_id = #{activityId}
          AND user_id = #{userId}
          AND deleted = 0
        LIMIT 1
        """)
    ActivityParticipant selectByActivityAndUser(@Param("activityId") Long activityId,
                                                 @Param("userId") Long userId);

    /**
     * 统计活动的参与人数
     *
     * @param activityId 活动ID
     * @param status     状态
     * @return 人数
     */
    @Select("""
        SELECT COUNT(*) FROM activity_participant
        WHERE activity_id = #{activityId}
          AND status = #{status}
          AND deleted = 0
        """)
    int countByActivityIdAndStatus(@Param("activityId") Long activityId,
                                    @Param("status") String status);
}
