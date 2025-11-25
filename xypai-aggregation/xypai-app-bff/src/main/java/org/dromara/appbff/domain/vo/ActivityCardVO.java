package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动卡片VO（列表展示用）
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "活动卡片信息")
public class ActivityCardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动类型")
    private String activityType;

    @Schema(description = "活动类型名称")
    private String activityTypeName;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "活动封面图片URL")
    private String coverImageUrl;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "格式化的时间显示", example = "11月30日 周六 14:00")
    private String timeDisplay;

    @Schema(description = "活动地点名称")
    private String locationName;

    @Schema(description = "距离(米)")
    private Integer distance;

    @Schema(description = "格式化的距离显示", example = "1.2km")
    private String distanceDisplay;

    @Schema(description = "当前报名人数")
    private Integer currentMembers;

    @Schema(description = "最大人数")
    private Integer maxMembers;

    @Schema(description = "人数显示", example = "5/8人")
    private String membersDisplay;

    @Schema(description = "性别限制: all, male, female")
    private String genderLimit;

    @Schema(description = "性别限制显示", example = "不限/仅男/仅女")
    private String genderLimitDisplay;

    @Schema(description = "是否收费")
    private Boolean isPaid;

    @Schema(description = "费用金额")
    private BigDecimal fee;

    @Schema(description = "费用显示", example = "免费/¥50/人")
    private String feeDisplay;

    @Schema(description = "活动状态: recruiting(报名中), full(已满员), ongoing(进行中), ended(已结束), cancelled(已取消)")
    private String status;

    @Schema(description = "状态显示")
    private String statusDisplay;

    @Schema(description = "发起人信息")
    private OrganizerBriefVO organizer;

    @Schema(description = "部分参与者头像列表(最多显示5个)")
    private List<String> participantAvatars;

    @Schema(description = "活动标签")
    private List<String> tags;

    /**
     * 发起人简要信息
     */
    @Data
    @Schema(description = "发起人简要信息")
    public static class OrganizerBriefVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像URL")
        private String avatarUrl;

        @Schema(description = "认证标识")
        private Boolean isVerified;
    }
}
