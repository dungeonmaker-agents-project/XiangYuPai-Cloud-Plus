package org.dromara.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动参与者表
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("activity_participant")
public class ActivityParticipant implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参与记录ID
     */
    @TableId(value = "participant_id", type = IdType.AUTO)
    private Long participantId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态: pending=待审核, approved=已通过, rejected=已拒绝, cancelled=已取消
     */
    @Builder.Default
    private String status = "pending";

    /**
     * 报名留言
     */
    private String message;

    /**
     * 报名时间
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime registerTime;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 0=未删除 1=已删除
     */
    @TableLogic
    private Integer deleted;
}
