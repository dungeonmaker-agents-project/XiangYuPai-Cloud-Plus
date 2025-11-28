package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.*;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.ActivityService;
import org.dromara.content.api.RemoteActivityService;
import org.dromara.content.api.domain.dto.RemoteActivityPublishDto;
import org.dromara.content.api.domain.dto.RemoteActivityQueryDto;
import org.dromara.content.api.domain.dto.RemoteActivityRegisterDto;
import org.dromara.content.api.domain.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组局活动服务实现类（RPC版本）
 * 通过 Dubbo RPC 调用 xypai-content 服务
 *
 * @author XyPai Team
 * @date 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    @DubboReference
    private RemoteActivityService remoteActivityService;

    @Override
    public ActivityListResultVO queryActivityList(ActivityListQueryDTO queryDTO, Long userId) {
        log.info("查询活动列表: queryDTO={}, userId={}", queryDTO, userId);

        try {
            // 转换查询条件
            RemoteActivityQueryDto rpcQuery = new RemoteActivityQueryDto();
            rpcQuery.setPageNum(queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1);
            rpcQuery.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10);
            rpcQuery.setSortBy(queryDTO.getSortBy());
            rpcQuery.setGender(queryDTO.getGender());
            rpcQuery.setCity(queryDTO.getCityCode());
            rpcQuery.setDistrict(queryDTO.getDistrictCode());

            // 处理活动类型筛选
            if (queryDTO.getActivityType() != null && !queryDTO.getActivityType().isEmpty()) {
                rpcQuery.setTypeCodes(queryDTO.getActivityType());
                if (queryDTO.getActivityType().size() == 1) {
                    rpcQuery.setTypeCode(queryDTO.getActivityType().get(0));
                }
            }

            // 调用 RPC 接口
            RemoteActivityPageResult rpcResult = remoteActivityService.queryActivityList(rpcQuery, userId);

            // 转换结果
            return convertToListResult(rpcResult);
        } catch (Exception e) {
            log.error("查询活动列表失败: queryDTO={}, error={}", queryDTO, e.getMessage(), e);
            // 返回空结果作为降级处理
            return createEmptyListResult();
        }
    }

    @Override
    public ActivityDetailVO getActivityDetail(Long activityId, Long userId) {
        log.info("获取活动详情: activityId={}, userId={}", activityId, userId);

        try {
            RemoteActivityDetailVo rpcResult = remoteActivityService.getActivityDetail(activityId, userId);

            if (rpcResult == null) {
                log.warn("活动不存在: activityId={}", activityId);
                return null;
            }

            // 增加浏览量
            remoteActivityService.incrementViewCount(activityId);

            return convertToDetailVO(rpcResult);
        } catch (Exception e) {
            log.error("获取活动详情失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ActivityRegisterResultVO registerActivity(ActivityRegisterDTO registerDTO, Long userId) {
        log.info("报名活动: registerDTO={}, userId={}", registerDTO, userId);

        ActivityRegisterResultVO result = new ActivityRegisterResultVO();

        try {
            RemoteActivityRegisterDto rpcDTO = new RemoteActivityRegisterDto();
            rpcDTO.setActivityId(registerDTO.getActivityId());
            rpcDTO.setMessage(registerDTO.getMessage());

            RemoteActivityRegisterResult rpcResult = remoteActivityService.registerActivity(rpcDTO, userId);

            result.setSuccess(rpcResult.getSuccess());
            result.setStatus(rpcResult.getStatus());
            result.setStatusMessage(rpcResult.getStatusMessage());
            result.setNeedPay(rpcResult.getNeedPay());
            result.setPayAmount(rpcResult.getPayAmount());
            result.setCurrentMembers(rpcResult.getCurrentMembers());
            result.setMaxMembers(rpcResult.getMaxMembers());

            // 获取活动基本信息用于展示
            if (rpcResult.getSuccess()) {
                RemoteActivityDetailVo detail = remoteActivityService.getActivityDetail(registerDTO.getActivityId(), userId);
                if (detail != null) {
                    result.setActivityTitle(detail.getTitle());
                    result.setActivityTimeDisplay(detail.getTimeDisplay());
                    result.setActivityLocation(detail.getLocationName());
                }
            }

            if (result.getNeedPay() != null && result.getNeedPay()) {
                result.setPayOrderId("PAY" + System.currentTimeMillis());
            }

        } catch (Exception e) {
            log.error("报名活动失败: registerDTO={}, error={}", registerDTO, e.getMessage(), e);
            result.setSuccess(false);
            result.setStatusMessage("报名失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Boolean cancelActivityRegistration(ActivityRegisterCancelDTO cancelDTO, Long userId) {
        log.info("取消报名: cancelDTO={}, userId={}", cancelDTO, userId);

        try {
            return remoteActivityService.cancelRegistration(cancelDTO.getActivityId(), userId, cancelDTO.getReason());
        } catch (Exception e) {
            log.error("取消报名失败: cancelDTO={}, error={}", cancelDTO, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ActivityPublishConfigVO getPublishConfig(Long userId) {
        log.info("获取发布配置: userId={}", userId);

        try {
            ActivityPublishConfigVO config = new ActivityPublishConfigVO();

            // 从远程服务获取活动类型
            List<RemoteActivityTypeVo> remoteTypes = remoteActivityService.getAllActivityTypes();
            List<ActivityPublishConfigVO.ActivityTypeVO> types = remoteTypes.stream()
                .map(this::convertToActivityTypeVO)
                .collect(Collectors.toList());
            config.setActivityTypes(types);

            // 性别选项
            List<ActivityPublishConfigVO.OptionVO> genderOptions = Arrays.asList(
                createOption("all", "不限"),
                createOption("male", "仅男生"),
                createOption("female", "仅女生")
            );
            config.setGenderOptions(genderOptions);

            // 人数配置
            ActivityPublishConfigVO.MemberLimitConfig memberConfig = new ActivityPublishConfigVO.MemberLimitConfig();
            memberConfig.setMinLimit(2);
            memberConfig.setMaxLimit(50);
            memberConfig.setDefaultMin(2);
            memberConfig.setDefaultMax(8);
            config.setMemberLimitConfig(memberConfig);

            // 费用配置
            ActivityPublishConfigVO.FeeConfig feeConfig = new ActivityPublishConfigVO.FeeConfig();
            feeConfig.setAllowPaid(true);
            feeConfig.setMinFee(new BigDecimal("1"));
            feeConfig.setMaxFee(new BigDecimal("9999"));
            feeConfig.setPlatformFeeRate(new BigDecimal("0.05"));
            feeConfig.setPlatformFeeDescription("平台收取5%服务费");
            config.setFeeConfig(feeConfig);

            // 热门标签
            config.setHotTags(Arrays.asList("新手友好", "周末局", "AA制", "交友", "技术交流", "休闲放松"));

            // 发布规则
            config.setPublishRules(Arrays.asList(
                "活动内容必须真实有效",
                "禁止发布违法违规内容",
                "活动时间不能早于当前时间",
                "发起人需遵守平台规则"
            ));

            // 用户发布权限
            config.setCanPublish(true);
            config.setFreePublishRemaining(3);
            config.setPublishPrice(new BigDecimal("5"));

            return config;
        } catch (Exception e) {
            log.error("获取发布配置失败: userId={}, error={}", userId, e.getMessage(), e);
            return createDefaultPublishConfig();
        }
    }

    @Override
    public ActivityPublishResultVO publishActivity(ActivityPublishDTO publishDTO, Long userId) {
        log.info("发布活动: publishDTO={}, userId={}", publishDTO, userId);

        ActivityPublishResultVO result = new ActivityPublishResultVO();

        try {
            RemoteActivityPublishDto rpcDTO = new RemoteActivityPublishDto();
            rpcDTO.setTypeCode(publishDTO.getActivityType());
            rpcDTO.setTitle(publishDTO.getTitle());
            rpcDTO.setDescription(publishDTO.getDescription());
            rpcDTO.setCoverImageUrl(publishDTO.getCoverImageId() != null ?
                "https://example.com/image/" + publishDTO.getCoverImageId() : null);
            rpcDTO.setStartTime(publishDTO.getStartTime());
            rpcDTO.setEndTime(publishDTO.getEndTime());
            rpcDTO.setRegistrationDeadline(publishDTO.getRegistrationDeadline());
            rpcDTO.setLocationName(publishDTO.getLocationName());
            rpcDTO.setLocationAddress(publishDTO.getLocationAddress());
            rpcDTO.setLongitude(publishDTO.getLongitude());
            rpcDTO.setLatitude(publishDTO.getLatitude());
            rpcDTO.setMinMembers(publishDTO.getMinMembers());
            rpcDTO.setMaxMembers(publishDTO.getMaxMembers());
            rpcDTO.setGenderLimit(publishDTO.getGenderLimit() != null ? publishDTO.getGenderLimit() : "all");
            rpcDTO.setIsPaid(publishDTO.getIsPaid() != null && publishDTO.getIsPaid());
            rpcDTO.setFee(publishDTO.getFee());
            rpcDTO.setFeeDescription(publishDTO.getFeeDescription());
            rpcDTO.setNeedApproval(publishDTO.getNeedApproval() != null && publishDTO.getNeedApproval());
            rpcDTO.setContactInfo(publishDTO.getContactInfo());
            rpcDTO.setTags(publishDTO.getTags());

            Long activityId = remoteActivityService.publishActivity(rpcDTO, userId);

            result.setSuccess(true);
            result.setActivityId(activityId);
            result.setTitle(publishDTO.getTitle());
            result.setStatus("recruiting");
            result.setStatusMessage("活动已发布成功，等待其他用户报名");
            result.setNeedPay(false);
            result.setFreePublishRemaining(2);
            result.setShareUrl("https://xypai.com/activity/" + activityId);
            result.setShareQrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://xypai.com/activity/" + activityId);

        } catch (Exception e) {
            log.error("发布活动失败: publishDTO={}, error={}", publishDTO, e.getMessage(), e);
            result.setSuccess(false);
            result.setStatusMessage("发布失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public ActivityPayResultVO payActivity(ActivityPayDTO payDTO, Long userId) {
        log.info("支付活动: payDTO={}, userId={}", payDTO, userId);

        ActivityPayResultVO result = new ActivityPayResultVO();

        try {
            RemoteActivityDetailVo detail = remoteActivityService.getActivityDetail(payDTO.getActivityId(), userId);
            if (detail == null) {
                result.setSuccess(false);
                result.setStatusMessage("活动不存在");
                return result;
            }

            result.setSuccess(true);
            result.setPayOrderId("PAY" + System.currentTimeMillis());
            result.setAmount(detail.getFee());
            result.setPaymentMethod(payDTO.getPaymentMethod());
            result.setPaymentStatus("success");
            result.setStatusMessage("支付成功");
            result.setActivityId(detail.getActivityId());
            result.setActivityTitle(detail.getTitle());

            if ("wechat".equals(payDTO.getPaymentMethod())) {
                result.setPaymentParams("{\"appId\":\"wx...\",\"timeStamp\":\"...\"}");
            } else if ("alipay".equals(payDTO.getPaymentMethod())) {
                result.setPaymentParams("alipay_sdk=...");
            }

            result.setTimeoutSeconds(900);

        } catch (Exception e) {
            log.error("支付活动失败: payDTO={}, error={}", payDTO, e.getMessage(), e);
            result.setSuccess(false);
            result.setStatusMessage("支付失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId) {
        log.info("审核报名: activityId={}, participantId={}, approved={}, userId={}", activityId, participantId, approved, userId);

        try {
            return remoteActivityService.approveRegistration(activityId, participantId, approved, userId);
        } catch (Exception e) {
            log.error("审核报名失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Boolean cancelActivity(Long activityId, String reason, Long userId) {
        log.info("取消活动: activityId={}, reason={}, userId={}", activityId, reason, userId);

        try {
            return remoteActivityService.cancelActivity(activityId, userId, reason);
        } catch (Exception e) {
            log.error("取消活动失败: activityId={}, error={}", activityId, e.getMessage(), e);
            return false;
        }
    }

    // ========== 转换方法 ==========

    private ActivityListResultVO convertToListResult(RemoteActivityPageResult rpc) {
        ActivityListResultVO result = new ActivityListResultVO();

        if (rpc.getList() != null) {
            List<ActivityCardVO> cards = rpc.getList().stream()
                .map(this::convertToCardVO)
                .collect(Collectors.toList());
            result.setList(cards);
        } else {
            result.setList(Collections.emptyList());
        }

        result.setTotal(rpc.getTotal());
        result.setPageNum(rpc.getPageNum());
        result.setPageSize(rpc.getPageSize());
        result.setPages(rpc.getPages());
        result.setHasNext(rpc.getHasNext());

        if (rpc.getFilterStats() != null) {
            ActivityListResultVO.FilterStatsVO stats = new ActivityListResultVO.FilterStatsVO();
            stats.setTotalActivities(rpc.getFilterStats().getTotalActivities());
            stats.setTodayNew(rpc.getFilterStats().getTodayNew());
            stats.setWeeklyHot(rpc.getFilterStats().getWeeklyHot());
            result.setFilterStats(stats);
        }

        return result;
    }

    private ActivityCardVO convertToCardVO(RemoteActivityVo rpc) {
        ActivityCardVO card = new ActivityCardVO();
        card.setActivityId(rpc.getActivityId());
        card.setActivityType(rpc.getTypeCode());
        card.setActivityTypeName(rpc.getTypeName());
        card.setTitle(rpc.getTitle());
        card.setCoverImageUrl(rpc.getCoverImageUrl());
        card.setStartTime(rpc.getStartTime());
        card.setEndTime(rpc.getEndTime());
        card.setTimeDisplay(rpc.getTimeDisplay());
        card.setLocationName(rpc.getLocationName());
        card.setDistance(rpc.getDistance());
        card.setDistanceDisplay(rpc.getDistanceDisplay());
        card.setCurrentMembers(rpc.getCurrentMembers());
        card.setMaxMembers(rpc.getMaxMembers());
        card.setMembersDisplay(rpc.getMembersDisplay());
        card.setGenderLimit(rpc.getGenderLimit());
        card.setGenderLimitDisplay(rpc.getGenderLimitDisplay());
        card.setIsPaid(rpc.getIsPaid());
        card.setFee(rpc.getFee());
        card.setFeeDisplay(rpc.getFeeDisplay());
        card.setStatus(rpc.getStatus());
        card.setStatusDisplay(rpc.getStatusDisplay());
        card.setTags(rpc.getTags());

        // 发起人信息
        if (rpc.getOrganizerId() != null) {
            ActivityCardVO.OrganizerBriefVO organizer = new ActivityCardVO.OrganizerBriefVO();
            organizer.setUserId(rpc.getOrganizerId());
            organizer.setNickname(rpc.getOrganizerNickname());
            organizer.setAvatarUrl(rpc.getOrganizerAvatarUrl());
            organizer.setIsVerified(rpc.getOrganizerIsVerified());
            card.setOrganizer(organizer);
        }

        card.setParticipantAvatars(rpc.getParticipantAvatars());

        return card;
    }

    private ActivityDetailVO convertToDetailVO(RemoteActivityDetailVo rpc) {
        ActivityDetailVO detail = new ActivityDetailVO();
        detail.setActivityId(rpc.getActivityId());
        detail.setActivityType(rpc.getTypeCode());
        detail.setActivityTypeName(rpc.getTypeName());
        detail.setTitle(rpc.getTitle());
        detail.setDescription(rpc.getDescription());
        detail.setCoverImageUrl(rpc.getCoverImageUrl());
        detail.setImageUrls(rpc.getImageUrls());

        // 时间
        detail.setStartTime(rpc.getStartTime());
        detail.setEndTime(rpc.getEndTime());
        detail.setTimeDisplay(rpc.getTimeDisplay());
        detail.setRegistrationDeadline(rpc.getRegistrationDeadline());
        detail.setRegistrationDeadlineDisplay(rpc.getRegistrationDeadlineDisplay());
        detail.setCreateTime(rpc.getCreateTime());

        // 地点
        detail.setLocationName(rpc.getLocationName());
        detail.setLocationAddress(rpc.getLocationAddress());
        detail.setLongitude(rpc.getLongitude());
        detail.setLatitude(rpc.getLatitude());
        detail.setCity(rpc.getCity());
        detail.setDistrict(rpc.getDistrict());
        detail.setDistance(rpc.getDistance());
        detail.setDistanceDisplay(rpc.getDistanceDisplay());

        // 人数
        detail.setMinMembers(rpc.getMinMembers());
        detail.setMaxMembers(rpc.getMaxMembers());
        detail.setCurrentMembers(rpc.getCurrentMembers());
        detail.setMembersDisplay(rpc.getMembersDisplay());
        detail.setGenderLimit(rpc.getGenderLimit());
        detail.setGenderLimitDisplay(rpc.getGenderLimitDisplay());

        // 费用
        detail.setIsPaid(rpc.getIsPaid());
        detail.setFee(rpc.getFee());
        detail.setFeeDescription(rpc.getFeeDescription());
        detail.setFeeDisplay(rpc.getFeeDisplay());

        // 状态
        detail.setStatus(rpc.getStatus());
        detail.setStatusDisplay(rpc.getStatusDisplay());
        detail.setNeedApproval(rpc.getNeedApproval());

        // 发起人
        if (rpc.getOrganizer() != null) {
            ActivityDetailVO.OrganizerVO organizer = new ActivityDetailVO.OrganizerVO();
            organizer.setUserId(rpc.getOrganizer().getUserId());
            organizer.setNickname(rpc.getOrganizer().getNickname());
            organizer.setAvatarUrl(rpc.getOrganizer().getAvatarUrl());
            organizer.setIsVerified(rpc.getOrganizer().getIsVerified());
            organizer.setVerifyType(rpc.getOrganizer().getVerifyType());
            organizer.setLevel(rpc.getOrganizer().getLevel());
            organizer.setOrganizedCount(rpc.getOrganizer().getOrganizedCount());
            organizer.setGoodRateDisplay(rpc.getOrganizer().getGoodRateDisplay());
            organizer.setBio(rpc.getOrganizer().getBio());
            detail.setOrganizer(organizer);
        }

        // 参与者
        if (rpc.getParticipants() != null) {
            List<ActivityDetailVO.ParticipantVO> participants = new ArrayList<>();
            for (RemoteActivityDetailVo.ParticipantInfo p : rpc.getParticipants()) {
                ActivityDetailVO.ParticipantVO pvo = new ActivityDetailVO.ParticipantVO();
                pvo.setUserId(p.getUserId());
                pvo.setNickname(p.getNickname());
                pvo.setAvatarUrl(p.getAvatarUrl());
                pvo.setGender(p.getGender());
                pvo.setStatus(p.getStatus());
                pvo.setRegisterTime(p.getRegisterTime());
                pvo.setMessage(p.getMessage());
                participants.add(pvo);
            }
            detail.setParticipants(participants);
        }

        detail.setPendingCount(rpc.getPendingCount());

        // 当前用户状态
        detail.setIsOrganizer(rpc.getIsOrganizer());
        detail.setCurrentUserStatus(rpc.getCurrentUserStatus());
        detail.setCanRegister(rpc.getCanRegister());
        detail.setCanCancel(rpc.getCanCancel());
        detail.setCannotRegisterReason(rpc.getCannotRegisterReason());

        // 其他
        detail.setTags(rpc.getTags());
        detail.setContactInfo(rpc.getContactInfo());
        detail.setViewCount(rpc.getViewCount());
        detail.setShareCount(rpc.getShareCount());

        return detail;
    }

    private ActivityPublishConfigVO.ActivityTypeVO convertToActivityTypeVO(RemoteActivityTypeVo rpc) {
        ActivityPublishConfigVO.ActivityTypeVO type = new ActivityPublishConfigVO.ActivityTypeVO();
        type.setCode(rpc.getTypeCode());
        type.setName(rpc.getTypeName());
        type.setIconUrl(rpc.getIconUrl());
        type.setIsHot(rpc.getIsHot());
        type.setDefaultTags(Arrays.asList("新手友好", "周末局"));
        return type;
    }

    // ========== 降级处理方法 ==========

    private ActivityListResultVO createEmptyListResult() {
        ActivityListResultVO result = new ActivityListResultVO();
        result.setList(Collections.emptyList());
        result.setTotal(0L);
        result.setPageNum(1);
        result.setPageSize(10);
        result.setPages(0);
        result.setHasNext(false);

        ActivityListResultVO.FilterStatsVO stats = new ActivityListResultVO.FilterStatsVO();
        stats.setTotalActivities(0);
        stats.setTodayNew(0);
        stats.setWeeklyHot(0);
        result.setFilterStats(stats);

        return result;
    }

    private ActivityPublishConfigVO createDefaultPublishConfig() {
        ActivityPublishConfigVO config = new ActivityPublishConfigVO();

        // 默认活动类型
        List<ActivityPublishConfigVO.ActivityTypeVO> types = new ArrayList<>();
        String[][] typeData = {
            {"billiards", "台球", "true"},
            {"board_game", "桌游", "true"},
            {"explore", "密室探索", "false"},
            {"sports", "运动健身", "true"},
            {"music", "音乐活动", "false"},
            {"food", "美食聚餐", "true"}
        };
        for (String[] data : typeData) {
            ActivityPublishConfigVO.ActivityTypeVO type = new ActivityPublishConfigVO.ActivityTypeVO();
            type.setCode(data[0]);
            type.setName(data[1]);
            type.setIconUrl("https://picsum.photos/50/50?random=" + data[0].hashCode());
            type.setIsHot("true".equals(data[2]));
            type.setDefaultTags(Arrays.asList("新手友好", "周末局"));
            types.add(type);
        }
        config.setActivityTypes(types);

        config.setGenderOptions(Arrays.asList(
            createOption("all", "不限"),
            createOption("male", "仅男生"),
            createOption("female", "仅女生")
        ));

        ActivityPublishConfigVO.MemberLimitConfig memberConfig = new ActivityPublishConfigVO.MemberLimitConfig();
        memberConfig.setMinLimit(2);
        memberConfig.setMaxLimit(50);
        memberConfig.setDefaultMin(2);
        memberConfig.setDefaultMax(8);
        config.setMemberLimitConfig(memberConfig);

        ActivityPublishConfigVO.FeeConfig feeConfig = new ActivityPublishConfigVO.FeeConfig();
        feeConfig.setAllowPaid(true);
        feeConfig.setMinFee(new BigDecimal("1"));
        feeConfig.setMaxFee(new BigDecimal("9999"));
        feeConfig.setPlatformFeeRate(new BigDecimal("0.05"));
        feeConfig.setPlatformFeeDescription("平台收取5%服务费");
        config.setFeeConfig(feeConfig);

        config.setHotTags(Arrays.asList("新手友好", "周末局", "AA制", "交友", "技术交流", "休闲放松"));
        config.setPublishRules(Arrays.asList(
            "活动内容必须真实有效",
            "禁止发布违法违规内容",
            "活动时间不能早于当前时间",
            "发起人需遵守平台规则"
        ));
        config.setCanPublish(true);
        config.setFreePublishRemaining(3);
        config.setPublishPrice(new BigDecimal("5"));

        return config;
    }

    private ActivityPublishConfigVO.OptionVO createOption(String value, String label) {
        ActivityPublishConfigVO.OptionVO option = new ActivityPublishConfigVO.OptionVO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }
}
