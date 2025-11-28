package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动详情VO
 *
 * @author XiangYuPai
 */
@Data
@Schema(description = "活动详情")
public class ActivityDetailVO implements Serializable {

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

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "图片列表")
    private List<String> imageUrls;

    // 时间相关
    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时间显示文本")
    private String timeDisplay;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationDeadline;

    @Schema(description = "报名截止显示文本")
    private String registrationDeadlineDisplay;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // 地点相关
    @Schema(description = "地点名称")
    private String locationName;

    @Schema(description = "详细地址")
    private String locationAddress;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "距离(米)")
    private Integer distance;

    @Schema(description = "距离显示文本")
    private String distanceDisplay;

    // 人数相关
    @Schema(description = "最少人数")
    private Integer minMembers;

    @Schema(description = "最大人数")
    private Integer maxMembers;

    @Schema(description = "当前人数")
    private Integer currentMembers;

    @Schema(description = "人数显示文本")
    private String membersDisplay;

    @Schema(description = "性别限制")
    private String genderLimit;

    @Schema(description = "性别限制显示文本")
    private String genderLimitDisplay;

    // 费用相关
    @Schema(description = "是否收费")
    private Boolean isPaid;

    @Schema(description = "费用金额")
    private BigDecimal fee;

    @Schema(description = "费用说明")
    private String feeDescription;

    @Schema(description = "费用显示文本")
    private String feeDisplay;

    // 状态相关
    @Schema(description = "活动状态")
    private String status;

    @Schema(description = "状态显示文本")
    private String statusDisplay;

    @Schema(description = "是否需要审核")
    private Boolean needApproval;

    @Schema(description = "联系方式")
    private String contactInfo;

    // 统计
    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "分享次数")
    private Integer shareCount;

    // 标签
    @Schema(description = "标签列表")
    private List<String> tags;

    // 发起人
    @Schema(description = "发起人信息")
    private OrganizerVO organizer;

    // 参与者
    @Schema(description = "参与者列表")
    private List<ParticipantVO> participants;

    @Schema(description = "待审核人数")
    private Integer pendingCount;

    // 当前用户状态
    @Schema(description = "当前用户是否是发起人")
    private Boolean isOrganizer;

    @Schema(description = "当前用户报名状态: none=未报名, pending=待审核, approved=已通过, rejected=已拒绝, cancelled=已取消")
    private String currentUserStatus;

    @Schema(description = "当前用户是否可以报名")
    private Boolean canRegister;

    @Schema(description = "当前用户是否可以取消报名")
    private Boolean canCancel;

    @Schema(description = "不能报名的原因")
    private String cannotRegisterReason;

    /**
     * 发起人信息
     */
    @Data
    @Schema(description = "发起人信息")
    public static class OrganizerVO implements Serializable {
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

        @Schema(description = "认证类型")
        private String verifyType;

        @Schema(description = "用户等级")
        private Integer level;

        @Schema(description = "组织活动数量")
        private Integer organizedCount;

        @Schema(description = "好评率显示")
        private String goodRateDisplay;

        @Schema(description = "个人简介")
        private String bio;
    }

    /**
     * 参与者信息
     */
    @Data
    @Schema(description = "参与者信息")
    public static class ParticipantVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "头像URL")
        private String avatarUrl;

        @Schema(description = "性别")
        private String gender;

        @Schema(description = "报名状态")
        private String status;

        @Schema(description = "报名时间")
        private LocalDateTime registerTime;

        @Schema(description = "报名留言")
        private String message;
    }
}
