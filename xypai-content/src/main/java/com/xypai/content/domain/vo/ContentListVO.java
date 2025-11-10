package com.xypai.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容列表VO
 *
 * @author xypai
 * @date 2025-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容列表VO")
public class ContentListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 内容ID
     */
    @Schema(description = "内容ID")
    private Long id;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long userId;

    /**
     * 创建者信息
     */
    @Schema(description = "创建者信息")
    private AuthorVO author;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private Integer type;

    /**
     * 内容类型描述
     */
    @Schema(description = "内容类型描述")
    private String typeDesc;

    /**
     * 内容标题
     */
    @Schema(description = "内容标题")
    private String title;

    /**
     * 内容摘要
     */
    @Schema(description = "内容摘要")
    private String summary;

    /**
     * 封面图片
     */
    @Schema(description = "封面图片")
    private String coverImage;

    /**
     * 图片列表
     */
    @Schema(description = "图片列表")
    private List<String> images;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息")
    private String location;

    /**
     * 地点名称（v7.1新增）
     */
    @Schema(description = "地点名称")
    private String locationName;

    /**
     * 详细地址（v7.1新增）
     */
    @Schema(description = "详细地址")
    private String locationAddress;

    /**
     * 经度（v7.1新增）
     */
    @Schema(description = "经度")
    private Double longitude;

    /**
     * 纬度（v7.1新增）
     */
    @Schema(description = "纬度")
    private Double latitude;

    /**
     * 距离（km，仅附近内容查询返回 - v7.1新增）
     */
    @Schema(description = "距离（km）")
    private Double distance;

    /**
     * 城市ID（v7.1新增）
     */
    @Schema(description = "城市ID")
    private Long cityId;

    /**
     * 内容状态
     */
    @Schema(description = "内容状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Long likeCount;

    /**
     * 评论数
     */
    @Schema(description = "评论数")
    private Long commentCount;

    /**
     * 分享数
     */
    @Schema(description = "分享数")
    private Long shareCount;

    /**
     * 收藏数
     */
    @Schema(description = "收藏数")
    private Long collectCount;

    /**
     * 查看数
     */
    @Schema(description = "查看数")
    private Long viewCount;

    /**
     * 当前用户是否已点赞
     */
    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    /**
     * 当前用户是否已收藏
     */
    @Schema(description = "当前用户是否已收藏")
    private Boolean collected;

    /**
     * 当前用户是否已关注作者
     */
    @Schema(description = "当前用户是否已关注作者")
    private Boolean followedAuthor;

    /**
     * 活动特定信息
     */
    @Schema(description = "活动特定信息")
    private ActivityInfoVO activityInfo;

    /**
     * 技能特定信息
     */
    @Schema(description = "技能特定信息")
    private SkillInfoVO skillInfo;

    /**
     * 作者信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "作者信息VO")
    public static class AuthorVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像")
        private String avatar;
    }

    /**
     * 活动信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "活动信息VO")
    public static class ActivityInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "活动开始时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Schema(description = "活动结束时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        @Schema(description = "活动地点")
        private String location;

        @Schema(description = "最大参与人数")
        private Integer maxParticipants;

        @Schema(description = "当前报名人数")
        private Integer currentParticipants;

        @Schema(description = "当前用户是否已报名")
        private Boolean signed;
    }

    /**
     * 技能信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "技能信息VO")
    public static class SkillInfoVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "技能分类")
        private String category;

        @Schema(description = "技能等级")
        private String level;

        @Schema(description = "价格(元)")
        private String price;

        @Schema(description = "价格(分)")
        private Long priceFen;
    }
}
