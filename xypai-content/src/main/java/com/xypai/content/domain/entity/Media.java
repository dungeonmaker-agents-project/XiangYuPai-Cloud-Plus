package com.xypai.content.domain.entity;

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
 * 媒体文件实体
 * 
 * 统一管理图片、视频、音频等媒体资源
 * 
 * 关联类型（ref_type）：
 * - content: 内容媒体
 * - draft: 草稿媒体
 * - comment: 评论媒体
 * - profile: 用户头像/背景
 * - message: 消息媒体
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("media")
public class Media implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 媒体文件ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上传者ID
     */
    @TableField("uploader_id")
    @NotNull(message = "上传者ID不能为空")
    private Long uploaderId;

    /**
     * 关联类型(content/draft/comment/profile/message)
     */
    @TableField("ref_type")
    @NotBlank(message = "关联类型不能为空")
    private String refType;

    /**
     * 关联对象ID
     */
    @TableField("ref_id")
    private Long refId;

    /**
     * 文件类型(1=图片,2=视频,3=音频)
     */
    @TableField("file_type")
    @NotNull(message = "文件类型不能为空")
    private Integer fileType;

    /**
     * 文件访问URL(CDN地址)
     */
    @TableField("file_url")
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    /**
     * 缩略图URL
     */
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 宽度(像素)
     */
    @TableField("width")
    private Integer width;

    /**
     * 高度(像素)
     */
    @TableField("height")
    private Integer height;

    /**
     * 时长(秒，视频/音频使用)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件格式(jpg/png/mp4/mp3等)
     */
    @TableField("file_format")
    private String fileFormat;

    /**
     * 状态(0=删除,1=正常,2=审核中)
     */
    @TableField("status")
    @Builder.Default
    private Integer status = 1;

    /**
     * 上传时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        IMAGE(1, "图片"),
        VIDEO(2, "视频"),
        AUDIO(3, "音频");

        private final Integer code;
        private final String desc;

        FileType(Integer code, String desc) {
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
     * 关联类型枚举
     */
    public enum RefType {
        CONTENT("content", "内容"),
        DRAFT("draft", "草稿"),
        COMMENT("comment", "评论"),
        PROFILE("profile", "用户资料"),
        MESSAGE("message", "消息");

        private final String code;
        private final String desc;

        RefType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 是否为图片
     */
    public boolean isImage() {
        return FileType.IMAGE.getCode().equals(this.fileType);
    }

    /**
     * 是否为视频
     */
    public boolean isVideo() {
        return FileType.VIDEO.getCode().equals(this.fileType);
    }

    /**
     * 是否为音频
     */
    public boolean isAudio() {
        return FileType.AUDIO.getCode().equals(this.fileType);
    }

    /**
     * 获取文件大小（MB）
     */
    public Double getFileSizeMB() {
        return fileSize != null ? fileSize / 1024.0 / 1024.0 : null;
    }
}

