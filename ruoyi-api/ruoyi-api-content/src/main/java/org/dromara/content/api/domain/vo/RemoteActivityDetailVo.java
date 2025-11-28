package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动详情VO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityDetailVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动类型编码
     */
    private String typeCode;

    /**
     * 活动类型名称
     */
    private String typeName;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private String coverImageUrl;

    /**
     * 图片列表
     */
    private List<String> imageUrls;

    // 时间相关
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timeDisplay;
    private LocalDateTime registrationDeadline;
    private String registrationDeadlineDisplay;
    private LocalDateTime createTime;

    // 地点相关
    private String locationName;
    private String locationAddress;
    private String city;
    private String district;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer distance;
    private String distanceDisplay;

    // 人数相关
    private Integer minMembers;
    private Integer maxMembers;
    private Integer currentMembers;
    private String membersDisplay;
    private String genderLimit;
    private String genderLimitDisplay;

    // 费用相关
    private Boolean isPaid;
    private BigDecimal fee;
    private String feeDescription;
    private String feeDisplay;

    // 状态相关
    private String status;
    private String statusDisplay;
    private Boolean needApproval;
    private String contactInfo;

    // 统计
    private Integer viewCount;
    private Integer shareCount;

    // 标签
    private List<String> tags;

    // 发起人
    private OrganizerInfo organizer;

    // 参与者
    private List<ParticipantInfo> participants;
    private Integer pendingCount;

    // 当前用户状态
    private Boolean isOrganizer;
    private String currentUserStatus;
    private Boolean canRegister;
    private Boolean canCancel;
    private String cannotRegisterReason;

    @Data
    public static class OrganizerInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Boolean isVerified;
        private String verifyType;
        private Integer level;
        private Integer organizedCount;
        private String goodRateDisplay;
        private String bio;
    }

    @Data
    public static class ParticipantInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long userId;
        private String nickname;
        private String avatarUrl;
        private String gender;
        private String status;
        private LocalDateTime registerTime;
        private String message;
    }
}
