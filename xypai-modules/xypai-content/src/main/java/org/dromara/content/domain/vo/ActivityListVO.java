package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动列表VO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "活动列表")
public class ActivityListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动类型编码")
    private String typeCode;

    @Schema(description = "活动类型名称")
    private String typeName;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时间显示文本")
    private String timeDisplay;

    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "距离(米)")
    private Integer distance;

    @Schema(description = "距离显示文本")
    private String distanceDisplay;

    @Schema(description = "当前人数")
    private Integer currentMembers;

    @Schema(description = "最大人数")
    private Integer maxMembers;

    @Schema(description = "人数显示文本")
    private String membersDisplay;

    @Schema(description = "性别限制")
    private String genderLimit;

    @Schema(description = "性别限制显示文本")
    private String genderLimitDisplay;

    @Schema(description = "是否收费")
    private Boolean isPaid;

    @Schema(description = "费用金额")
    private BigDecimal fee;

    @Schema(description = "费用显示文本")
    private String feeDisplay;

    @Schema(description = "活动状态")
    private String status;

    @Schema(description = "状态显示文本")
    private String statusDisplay;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "发起人信息")
    private OrganizerBriefVO organizer;

    @Schema(description = "参与者头像列表(最多5个)")
    private List<String> participantAvatars;

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

        @Schema(description = "是否认证")
        private Boolean isVerified;
    }
}
