package org.dromara.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户职业实体（支持多职业）
 * 对应表: user_occupations
 *
 * 对应UI文档: 个人主页-编辑_结构文档.md
 * - BasicInfoData.occupation: Array<String>?
 * - 最多5个职业
 *
 * @author XiangYuPai
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_occupations")
public class UserOccupation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 职业记录ID（主键）
     */
    @TableId(value = "occupation_id", type = IdType.AUTO)
    private Long occupationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 职业名称（1-30字符）
     */
    @TableField("occupation_name")
    private String occupationName;

    /**
     * 排序序号（越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
     * 软删除（0-未删除，1-已删除）
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
