package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户举报实体
 * 对应表: user_reports
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_reports")
public class UserReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 举报ID（主键）
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 被举报用户ID
     */
    @TableField("reported_user_id")
    private Long reportedUserId;

    /**
     * 举报原因: spam, abuse, inappropriate, fraud, other
     */
    @TableField("reason")
    private String reason;

    /**
     * 举报详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 证据（图片URL，多个用逗号分隔）
     */
    @TableField("evidence")
    private String evidence;

    /**
     * 处理状态: pending, processing, resolved, rejected
     */
    @TableField("status")
    private String status;

    /**
     * 审核时间
     */
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核结果
     */
    @TableField("review_result")
    private String reviewResult;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
