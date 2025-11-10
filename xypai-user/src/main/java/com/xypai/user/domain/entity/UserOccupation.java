package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户职业关联表实体（符合第一范式）
 * 
 * 设计优势：
 * 1. 支持高效查询：SELECT * FROM UserOccupation WHERE occupation_code='model'
 * 2. 数据完整性：外键约束保证数据有效性
 * 3. 易于扩展：新增职业只需在字典表添加
 * 4. 支持用户自定义职业显示顺序
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_occupation")
public class UserOccupation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 职业编码（关联OccupationDict表）
     */
    @TableField("occupation_code")
    @NotBlank(message = "职业编码不能为空")
    private String occupationCode;

    /**
     * 排序顺序（支持用户自定义显示顺序）
     */
    @TableField("sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ==================== 业务方法 ====================

    /**
     * 检查是否为主要职业（排序第一）
     */
    public boolean isPrimary() {
        return sortOrder != null && sortOrder == 0;
    }

    /**
     * 设置为主要职业
     */
    public void setAsPrimary() {
        this.sortOrder = 0;
    }

    /**
     * 增加排序顺序
     */
    public void incrementSortOrder() {
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        this.sortOrder++;
    }

    /**
     * 减少排序顺序
     */
    public void decrementSortOrder() {
        if (this.sortOrder != null && this.sortOrder > 0) {
            this.sortOrder--;
        }
    }

    /**
     * 设置排序顺序
     */
    public void updateSortOrder(Integer newOrder) {
        if (newOrder != null && newOrder >= 0) {
            this.sortOrder = newOrder;
        }
    }
}

