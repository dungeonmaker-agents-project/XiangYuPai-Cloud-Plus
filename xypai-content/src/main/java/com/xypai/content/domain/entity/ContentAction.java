package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 内容行为统一实体
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "content_action", autoResultMap = true)
public class ContentAction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 行为记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联内容ID
     */
    @TableField("content_id")
    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 行为类型(1=点赞,2=评论,6=报名)
     */
    @TableField("action")
    @NotNull(message = "行为类型不能为空")
    private Integer action;

    /**
     * 行为扩展数据JSON
     */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 行为时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 行为类型枚举
     */
    public enum Action {
        LIKE(1, "点赞"),
        COMMENT(2, "评论"),
        SHARE(3, "分享"),
        COLLECT(4, "收藏"),
        REPORT(5, "举报"),
        SIGNUP(6, "报名"),
        VIEW(7, "查看"),
        FOLLOW(8, "关注");

        private final Integer code;
        private final String desc;

        Action(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Action fromCode(Integer code) {
            for (Action action : values()) {
                if (action.getCode().equals(code)) {
                    return action;
                }
            }
            return null;
        }
    }

    /**
     * 是否为点赞行为
     */
    public boolean isLike() {
        return Action.LIKE.getCode().equals(this.action);
    }

    /**
     * 是否为评论行为
     */
    public boolean isComment() {
        return Action.COMMENT.getCode().equals(this.action);
    }

    /**
     * 是否为分享行为
     */
    public boolean isShare() {
        return Action.SHARE.getCode().equals(this.action);
    }

    /**
     * 是否为收藏行为
     */
    public boolean isCollect() {
        return Action.COLLECT.getCode().equals(this.action);
    }

    /**
     * 是否为报名行为
     */
    public boolean isSignup() {
        return Action.SIGNUP.getCode().equals(this.action);
    }

    /**
     * 获取行为描述
     */
    public String getActionDesc() {
        Action actionType = Action.fromCode(this.action);
        return actionType != null ? actionType.getDesc() : "未知";
    }

    /**
     * 获取评论内容
     */
    public String getCommentContent() {
        return data != null && isComment() ? (String) data.get("content") : null;
    }

    /**
     * 设置评论内容
     */
    public void setCommentContent(String content) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("content", content);
    }

    /**
     * 获取评论回复的目标ID
     */
    public Long getReplyToId() {
        return data != null && isComment() ? 
            ((Number) data.get("reply_to_id")).longValue() : null;
    }

    /**
     * 设置评论回复的目标ID
     */
    public void setReplyToId(Long replyToId) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("reply_to_id", replyToId);
    }

    /**
     * 获取报名信息
     */
    public Map<String, Object> getSignupInfo() {
        return data != null && isSignup() ? 
            (Map<String, Object>) data.get("signup_info") : null;
    }

    /**
     * 设置报名信息
     */
    public void setSignupInfo(Map<String, Object> signupInfo) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put("signup_info", signupInfo);
    }
}
