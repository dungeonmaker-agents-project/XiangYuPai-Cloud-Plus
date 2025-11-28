package org.dromara.content.dubbo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.content.api.RemoteActivityService;
import org.dromara.content.api.domain.dto.RemoteActivityPublishDto;
import org.dromara.content.api.domain.dto.RemoteActivityQueryDto;
import org.dromara.content.api.domain.dto.RemoteActivityRegisterDto;
import org.dromara.content.api.domain.vo.*;
import org.dromara.content.domain.dto.ActivityListQueryDTO;
import org.dromara.content.domain.dto.ActivityPublishDTO;
import org.dromara.content.domain.dto.ActivityRegisterDTO;
import org.dromara.content.domain.entity.ActivityType;
import org.dromara.content.domain.vo.ActivityDetailVO;
import org.dromara.content.domain.vo.ActivityListVO;
import org.dromara.content.service.IActivityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动远程服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteActivityServiceImpl implements RemoteActivityService {

    private final IActivityService activityService;

    @Override
    public RemoteActivityPageResult queryActivityList(RemoteActivityQueryDto queryDTO, Long userId) {
        log.info("RPC查询活动列表: queryDTO={}, userId={}", queryDTO, userId);

        // 转换查询条件
        ActivityListQueryDTO localQuery = new ActivityListQueryDTO();
        localQuery.setPageNum(queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1);
        localQuery.setPageSize(queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10);
        localQuery.setTypeCode(queryDTO.getTypeCode());
        localQuery.setGender(queryDTO.getGender());
        localQuery.setCity(queryDTO.getCity());
        localQuery.setDistrict(queryDTO.getDistrict());
        localQuery.setSortBy(queryDTO.getSortBy());

        // 调用本地服务
        Page<ActivityListVO> page = activityService.getActivityList(localQuery, userId);

        // 转换结果
        RemoteActivityPageResult result = new RemoteActivityPageResult();
        result.setList(page.getRecords().stream()
            .map(this::convertToRemoteVo)
            .collect(Collectors.toList()));
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setPages((int) page.getPages());
        result.setHasNext(page.getCurrent() < page.getPages());

        // 统计信息
        RemoteActivityPageResult.FilterStats stats = new RemoteActivityPageResult.FilterStats();
        stats.setTotalActivities((int) page.getTotal());
        stats.setTodayNew(0); // TODO: 实现今日新增统计
        stats.setWeeklyHot(0); // TODO: 实现本周热门统计
        result.setFilterStats(stats);

        return result;
    }

    @Override
    public RemoteActivityDetailVo getActivityDetail(Long activityId, Long userId) {
        log.info("RPC获取活动详情: activityId={}, userId={}", activityId, userId);

        ActivityDetailVO detail = activityService.getActivityDetail(activityId, userId);
        if (detail == null) {
            return null;
        }

        return convertToRemoteDetailVo(detail);
    }

    @Override
    public Long publishActivity(RemoteActivityPublishDto publishDTO, Long userId) {
        log.info("RPC发布活动: publishDTO={}, userId={}", publishDTO, userId);

        // 转换发布DTO
        ActivityPublishDTO localDTO = new ActivityPublishDTO();
        localDTO.setTypeCode(publishDTO.getTypeCode());
        localDTO.setTitle(publishDTO.getTitle());
        localDTO.setDescription(publishDTO.getDescription());
        localDTO.setCoverImageUrl(publishDTO.getCoverImageUrl());
        localDTO.setStartTime(publishDTO.getStartTime());
        localDTO.setEndTime(publishDTO.getEndTime());
        localDTO.setRegistrationDeadline(publishDTO.getRegistrationDeadline());
        localDTO.setLocationName(publishDTO.getLocationName());
        localDTO.setLocationAddress(publishDTO.getLocationAddress());
        localDTO.setCity(publishDTO.getCity());
        localDTO.setDistrict(publishDTO.getDistrict());
        localDTO.setLongitude(publishDTO.getLongitude());
        localDTO.setLatitude(publishDTO.getLatitude());
        localDTO.setMinMembers(publishDTO.getMinMembers());
        localDTO.setMaxMembers(publishDTO.getMaxMembers());
        localDTO.setGenderLimit(publishDTO.getGenderLimit());
        localDTO.setIsPaid(publishDTO.getIsPaid());
        localDTO.setFee(publishDTO.getFee());
        localDTO.setFeeDescription(publishDTO.getFeeDescription());
        localDTO.setNeedApproval(publishDTO.getNeedApproval());
        localDTO.setContactInfo(publishDTO.getContactInfo());

        return activityService.publishActivity(localDTO, userId);
    }

    @Override
    public RemoteActivityRegisterResult registerActivity(RemoteActivityRegisterDto registerDTO, Long userId) {
        log.info("RPC报名活动: registerDTO={}, userId={}", registerDTO, userId);

        RemoteActivityRegisterResult result = new RemoteActivityRegisterResult();

        try {
            // 转换注册DTO
            ActivityRegisterDTO localDTO = new ActivityRegisterDTO();
            localDTO.setActivityId(registerDTO.getActivityId());
            localDTO.setMessage(registerDTO.getMessage());

            Long participantId = activityService.registerActivity(localDTO, userId);

            result.setSuccess(true);
            result.setParticipantId(participantId);
            result.setStatus("approved"); // 根据活动设置决定是否需要审核
            result.setStatusMessage("报名成功");

            // 获取活动详情来填充其他信息
            ActivityDetailVO detail = activityService.getActivityDetail(registerDTO.getActivityId(), userId);
            if (detail != null) {
                result.setNeedPay(detail.getIsPaid());
                result.setPayAmount(detail.getFee());
                result.setCurrentMembers(detail.getCurrentMembers());
                result.setMaxMembers(detail.getMaxMembers());
            }
        } catch (RuntimeException e) {
            result.setSuccess(false);
            result.setStatusMessage(e.getMessage());
        }

        return result;
    }

    @Override
    public Boolean cancelRegistration(Long activityId, Long userId, String reason) {
        log.info("RPC取消报名: activityId={}, userId={}, reason={}", activityId, userId, reason);
        return activityService.cancelRegistration(activityId, userId, reason);
    }

    @Override
    public Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId) {
        log.info("RPC审核报名: activityId={}, participantId={}, approved={}, userId={}",
            activityId, participantId, approved, userId);
        return activityService.approveRegistration(activityId, participantId, approved, userId);
    }

    @Override
    public Boolean cancelActivity(Long activityId, Long userId, String reason) {
        log.info("RPC取消活动: activityId={}, userId={}, reason={}", activityId, userId, reason);
        return activityService.cancelActivity(activityId, userId, reason);
    }

    @Override
    public List<RemoteActivityTypeVo> getAllActivityTypes() {
        log.info("RPC获取所有活动类型");
        List<ActivityType> types = activityService.getAllActivityTypes();
        return types.stream()
            .map(this::convertToRemoteTypeVo)
            .collect(Collectors.toList());
    }

    @Override
    public List<RemoteActivityTypeVo> getHotActivityTypes() {
        log.info("RPC获取热门活动类型");
        List<ActivityType> types = activityService.getHotActivityTypes();
        return types.stream()
            .map(this::convertToRemoteTypeVo)
            .collect(Collectors.toList());
    }

    @Override
    public void incrementViewCount(Long activityId) {
        activityService.incrementViewCount(activityId);
    }

    @Override
    public void incrementShareCount(Long activityId) {
        activityService.incrementShareCount(activityId);
    }

    // ========== 转换方法 ==========

    private RemoteActivityVo convertToRemoteVo(ActivityListVO local) {
        RemoteActivityVo remote = new RemoteActivityVo();
        remote.setActivityId(local.getActivityId());
        remote.setTypeCode(local.getTypeCode());
        remote.setTypeName(local.getTypeName());
        remote.setTitle(local.getTitle());
        remote.setCoverImageUrl(local.getCoverImageUrl());
        remote.setStartTime(local.getStartTime());
        remote.setEndTime(local.getEndTime());
        remote.setTimeDisplay(local.getTimeDisplay());
        remote.setLocationName(local.getLocationName());
        remote.setCity(local.getCity());
        remote.setDistrict(local.getDistrict());
        remote.setDistance(local.getDistance());
        remote.setDistanceDisplay(local.getDistanceDisplay());
        remote.setCurrentMembers(local.getCurrentMembers());
        remote.setMaxMembers(local.getMaxMembers());
        remote.setMembersDisplay(local.getMembersDisplay());
        remote.setGenderLimit(local.getGenderLimit());
        remote.setGenderLimitDisplay(local.getGenderLimitDisplay());
        remote.setIsPaid(local.getIsPaid());
        remote.setFee(local.getFee());
        remote.setFeeDisplay(local.getFeeDisplay());
        remote.setStatus(local.getStatus());
        remote.setStatusDisplay(local.getStatusDisplay());
        remote.setTags(local.getTags());

        // 发起人信息
        if (local.getOrganizer() != null) {
            remote.setOrganizerId(local.getOrganizer().getUserId());
            remote.setOrganizerNickname(local.getOrganizer().getNickname());
            remote.setOrganizerAvatarUrl(local.getOrganizer().getAvatarUrl());
            remote.setOrganizerIsVerified(local.getOrganizer().getIsVerified());
        }

        remote.setParticipantAvatars(local.getParticipantAvatars());

        return remote;
    }

    private RemoteActivityDetailVo convertToRemoteDetailVo(ActivityDetailVO local) {
        RemoteActivityDetailVo remote = new RemoteActivityDetailVo();
        remote.setActivityId(local.getActivityId());
        remote.setTypeCode(local.getTypeCode());
        remote.setTypeName(local.getTypeName());
        remote.setTitle(local.getTitle());
        remote.setDescription(local.getDescription());
        remote.setCoverImageUrl(local.getCoverImageUrl());
        remote.setImageUrls(local.getImageUrls());

        // 时间
        remote.setStartTime(local.getStartTime());
        remote.setEndTime(local.getEndTime());
        remote.setTimeDisplay(local.getTimeDisplay());
        remote.setRegistrationDeadline(local.getRegistrationDeadline());
        remote.setRegistrationDeadlineDisplay(local.getRegistrationDeadlineDisplay());
        remote.setCreateTime(local.getCreateTime());

        // 地点
        remote.setLocationName(local.getLocationName());
        remote.setLocationAddress(local.getLocationAddress());
        remote.setCity(local.getCity());
        remote.setDistrict(local.getDistrict());
        remote.setLongitude(local.getLongitude());
        remote.setLatitude(local.getLatitude());
        remote.setDistance(local.getDistance());
        remote.setDistanceDisplay(local.getDistanceDisplay());

        // 人数
        remote.setMinMembers(local.getMinMembers());
        remote.setMaxMembers(local.getMaxMembers());
        remote.setCurrentMembers(local.getCurrentMembers());
        remote.setMembersDisplay(local.getMembersDisplay());
        remote.setGenderLimit(local.getGenderLimit());
        remote.setGenderLimitDisplay(local.getGenderLimitDisplay());

        // 费用
        remote.setIsPaid(local.getIsPaid());
        remote.setFee(local.getFee());
        remote.setFeeDescription(local.getFeeDescription());
        remote.setFeeDisplay(local.getFeeDisplay());

        // 状态
        remote.setStatus(local.getStatus());
        remote.setStatusDisplay(local.getStatusDisplay());
        remote.setNeedApproval(local.getNeedApproval());
        remote.setContactInfo(local.getContactInfo());

        // 统计
        remote.setViewCount(local.getViewCount());
        remote.setShareCount(local.getShareCount());

        // 标签
        remote.setTags(local.getTags());

        // 发起人
        if (local.getOrganizer() != null) {
            RemoteActivityDetailVo.OrganizerInfo organizer = new RemoteActivityDetailVo.OrganizerInfo();
            organizer.setUserId(local.getOrganizer().getUserId());
            organizer.setNickname(local.getOrganizer().getNickname());
            organizer.setAvatarUrl(local.getOrganizer().getAvatarUrl());
            organizer.setIsVerified(local.getOrganizer().getIsVerified());
            organizer.setVerifyType(local.getOrganizer().getVerifyType());
            organizer.setLevel(local.getOrganizer().getLevel());
            organizer.setOrganizedCount(local.getOrganizer().getOrganizedCount());
            organizer.setGoodRateDisplay(local.getOrganizer().getGoodRateDisplay());
            organizer.setBio(local.getOrganizer().getBio());
            remote.setOrganizer(organizer);
        }

        // 参与者
        if (local.getParticipants() != null) {
            List<RemoteActivityDetailVo.ParticipantInfo> participants = new ArrayList<>();
            for (ActivityDetailVO.ParticipantVO p : local.getParticipants()) {
                RemoteActivityDetailVo.ParticipantInfo info = new RemoteActivityDetailVo.ParticipantInfo();
                info.setUserId(p.getUserId());
                info.setNickname(p.getNickname());
                info.setAvatarUrl(p.getAvatarUrl());
                info.setGender(p.getGender());
                info.setStatus(p.getStatus());
                info.setRegisterTime(p.getRegisterTime());
                info.setMessage(p.getMessage());
                participants.add(info);
            }
            remote.setParticipants(participants);
        }

        remote.setPendingCount(local.getPendingCount());

        // 当前用户状态
        remote.setIsOrganizer(local.getIsOrganizer());
        remote.setCurrentUserStatus(local.getCurrentUserStatus());
        remote.setCanRegister(local.getCanRegister());
        remote.setCanCancel(local.getCanCancel());
        remote.setCannotRegisterReason(local.getCannotRegisterReason());

        return remote;
    }

    private RemoteActivityTypeVo convertToRemoteTypeVo(ActivityType type) {
        RemoteActivityTypeVo remote = new RemoteActivityTypeVo();
        remote.setTypeCode(type.getTypeCode());
        remote.setTypeName(type.getTypeName());
        remote.setIconUrl(type.getIconUrl());
        remote.setIsHot(type.getIsHot() != null && type.getIsHot());
        remote.setSortOrder(type.getSortOrder());
        return remote;
    }
}
