package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职业字典表实体（统一管理职业类型）
 * 
 * 设计优势：
 * 1. 统一管理枚举值，避免硬编码
 * 2. 支持动态新增职业类型
 * 3. 支持多语言扩展（可添加OccupationDictI18n表）
 * 4. 支持图标和分类管理
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("occupation_dict")
public class OccupationDict implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职业编码（主键，如：model/student/freelancer）
     */
    @TableId(type = IdType.INPUT)
    private String code;

    /**
     * 职业名称（如：模特/学生/自由职业）
     */
    @TableField("name")
    private String name;

    /**
     * 职业分类（如：艺术/教育/服务）
     */
    @TableField("category")
    private String category;

    /**
     * 图标URL（前端展示用）
     */
    @TableField("icon_url")
    private String iconUrl;

    /**
     * 排序顺序（热门职业优先显示）
     */
    @TableField("sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 状态（0=禁用，1=启用）
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ==================== 业务方法 ====================

    /**
     * 状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return Status.ENABLED.getCode().equals(this.status);
    }

    /**
     * 检查是否禁用
     */
    public boolean isDisabled() {
        return Status.DISABLED.getCode().equals(this.status);
    }

    /**
     * 启用职业
     */
    public void enable() {
        this.status = Status.ENABLED.getCode();
    }

    /**
     * 禁用职业
     */
    public void disable() {
        this.status = Status.DISABLED.getCode();
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        for (Status s : Status.values()) {
            if (s.getCode().equals(this.status)) {
                return s.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 检查是否有图标
     */
    public boolean hasIcon() {
        return iconUrl != null && !iconUrl.isEmpty();
    }

    /**
     * 检查是否属于某个分类
     */
    public boolean belongsToCategory(String categoryName) {
        return category != null && category.equals(categoryName);
    }
}

