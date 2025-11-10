package com.xypai.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户关系实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_relation")
public class UserRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发起用户ID
     */
    @TableField("user_id")
    @NotNull(message = "发起用户ID不能为空")
    private Long userId;

    /**
     * 目标用户ID
     */
    @TableField("target_id")
    @NotNull(message = "目标用户ID不能为空")
    private Long targetId;

    /**
     * 关系类型(1=关注,2=拉黑,3=好友,4=特别关注)
     */
    @TableField("type")
    @NotNull(message = "关系类型不能为空")
    private Integer type;

    /**
     * 关系状态(0=已取消,1=正常)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 建立关系时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 关系类型枚举
     */
    public enum Type {
        FOLLOW(1, "关注"),
        BLOCK(2, "拉黑"),
        FRIEND(3, "好友"),
        SPECIAL_FOLLOW(4, "特别关注");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Type fromCode(Integer code) {
            for (Type type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 关系状态枚举
     */
    public enum Status {
        CANCELLED(0, "已取消"),
        NORMAL(1, "正常");

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
     * 是否为关注关系
     */
    public boolean isFollow() {
        return Type.FOLLOW.getCode().equals(this.type);
    }

    /**
     * 是否为拉黑关系
     */
    public boolean isBlock() {
        return Type.BLOCK.getCode().equals(this.type);
    }

    /**
     * 是否为好友关系
     */
    public boolean isFriend() {
        return Type.FRIEND.getCode().equals(this.type);
    }

    /**
     * 是否为特别关注
     */
    public boolean isSpecialFollow() {
        return Type.SPECIAL_FOLLOW.getCode().equals(this.type);
    }

    /**
     * 获取关系类型描述
     */
    public String getTypeDesc() {
        Type relationType = Type.fromCode(this.type);
        return relationType != null ? relationType.getDesc() : "未知";
    }

    /**
     * 检查是否为相同用户的关系
     */
    public boolean isSelfRelation() {
        return userId.equals(targetId);
    }

    /**
     * 检查关系是否正常
     */
    public boolean isNormal() {
        return Status.NORMAL.getCode().equals(this.status);
    }

    /**
     * 检查关系是否已取消
     */
    public boolean isCancelled() {
        return Status.CANCELLED.getCode().equals(this.status);
    }

    /**
     * 激活关系
     */
    public void activate() {
        this.status = Status.NORMAL.getCode();
    }

    /**
     * 取消关系
     */
    public void cancel() {
        this.status = Status.CANCELLED.getCode();
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
}

