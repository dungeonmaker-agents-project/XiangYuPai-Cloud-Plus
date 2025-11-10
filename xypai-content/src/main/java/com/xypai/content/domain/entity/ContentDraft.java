package com.xypai.content.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xypai.content.handler.PointTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 内容草稿实体
 * 
 * 草稿自动保存策略：
 * 1. 前端每10秒自动保存一次
 * 2. 草稿30天后自动过期清理
 * 3. 发布成功后删除对应草稿
 * 4. 用户最多保留10个未发布草稿
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "content_draft", autoResultMap = true)
public class ContentDraft implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 草稿ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容类型(1=动态,2=活动,3=技能)
     */
    @TableField("type")
    private Integer type;

    /**
     * 标题草稿
     */
    @TableField("title")
    private String title;

    /**
     * 正文草稿
     */
    @TableField("content")
    private String content;

    /**
     * 封面图
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 地点名称
     */
    @TableField("location_name")
    private String locationName;

    /**
     * 详细地址
     */
    @TableField("location_address")
    private String locationAddress;

    /**
     * 地理位置
     */
    @TableField(value = "location", typeHandler = PointTypeHandler.class)
    private Point location;

    /**
     * 城市ID
     */
    @TableField("city_id")
    private Long cityId;

    /**
     * 扩展数据（活动、技能特有字段）
     */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> data;

    /**
     * 自动保存时间
     */
    @TableField("auto_save_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime autoSaveTime;

    /**
     * 过期时间（30天后清理）
     */
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 草稿状态(0=已发布,1=编辑中,2=过期,3=删除)
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

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 草稿状态枚举
     */
    public enum Status {
        PUBLISHED(0, "已发布"),
        EDITING(1, "编辑中"),
        EXPIRED(2, "已过期"),
        DELETED(3, "已删除");

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
     * 是否编辑中
     */
    public boolean isEditing() {
        return Status.EDITING.getCode().equals(this.status);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return Status.EXPIRED.getCode().equals(this.status) 
            || (expireTime != null && LocalDateTime.now().isAfter(expireTime));
    }
}

