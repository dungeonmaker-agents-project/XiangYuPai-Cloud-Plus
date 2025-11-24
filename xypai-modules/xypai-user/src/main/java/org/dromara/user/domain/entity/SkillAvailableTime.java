package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 技能可用时间实体
 * 对应表: skill_available_times
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skill_available_times")
public class SkillAvailableTime implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时间ID（主键）
     */
    @TableId(value = "time_id", type = IdType.AUTO)
    private Long timeId;

    /**
     * 技能ID
     */
    @TableField("skill_id")
    private Long skillId;

    /**
     * 星期几（1-7，1=周一，7=周日）
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalTime endTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
