package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.content.domain.dto.ActivityListQueryDTO;
import org.dromara.content.domain.dto.ActivityPublishDTO;
import org.dromara.content.domain.dto.ActivityRegisterDTO;
import org.dromara.content.domain.entity.Activity;
import org.dromara.content.domain.entity.ActivityParticipant;
import org.dromara.content.domain.entity.ActivityType;
import org.dromara.content.domain.vo.ActivityDetailVO;
import org.dromara.content.domain.vo.ActivityListVO;
import org.dromara.content.mapper.ActivityMapper;
import org.dromara.content.mapper.ActivityParticipantMapper;
import org.dromara.content.mapper.ActivityTypeMapper;
import org.dromara.content.service.IActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements IActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityParticipantMapper participantMapper;
    private final ActivityTypeMapper activityTypeMapper;

    // 活动类型名称缓存
    private Map<String, String> typeNameCache;

    @Override
    public Page<ActivityListVO> getActivityList(ActivityListQueryDTO queryDTO, Long currentUserId) {
        log.info("查询活动列表: queryDTO={}, userId={}", queryDTO, currentUserId);

        Page<Activity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getDeleted, 0);

        // 类型筛选
        if (queryDTO.getTypeCode() != null && !queryDTO.getTypeCode().isEmpty()) {
            wrapper.eq(Activity::getTypeCode, queryDTO.getTypeCode());
        }

        // 性别筛选
        if (queryDTO.getGender() != null && !queryDTO.getGender().isEmpty() && !"all".equals(queryDTO.getGender())) {
            wrapper.and(w -> w.eq(Activity::getGenderLimit, "all")
                .or().eq(Activity::getGenderLimit, queryDTO.getGender()));
        }

        // 城市区县筛选
        if (queryDTO.getCity() != null && !queryDTO.getCity().isEmpty()) {
            wrapper.eq(Activity::getCity, queryDTO.getCity());
        }
        if (queryDTO.getDistrict() != null && !queryDTO.getDistrict().isEmpty()) {
            wrapper.eq(Activity::getDistrict, queryDTO.getDistrict());
        }

        // 只显示招募中和已满员的活动
        wrapper.in(Activity::getStatus, "recruiting", "full");

        // 排序
        String sortBy = queryDTO.getSortBy();
        if ("newest".equals(sortBy)) {
            wrapper.orderByDesc(Activity::getCreateTime);
        } else if ("start_time_asc".equals(sortBy)) {
            wrapper.orderByAsc(Activity::getStartTime);
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Activity::getCreateTime);
        }

        Page<Activity> activityPage = activityMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ActivityListVO> resultPage = new Page<>(activityPage.getCurrent(), activityPage.getSize(), activityPage.getTotal());
        List<ActivityListVO> voList = activityPage.getRecords().stream()
            .map(this::convertToListVO)
            .collect(Collectors.toList());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public ActivityDetailVO getActivityDetail(Long activityId, Long userId) {
        log.info("获取活动详情: activityId={}, userId={}", activityId, userId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getDeleted() == 1) {
            return null;
        }

        ActivityDetailVO vo = convertToDetailVO(activity);

        // 查询参与者列表
        List<ActivityParticipant> participants = participantMapper.selectByActivityId(activityId, "approved");
        vo.setParticipants(convertToParticipantVOList(participants));

        // 待审核人数
        int pendingCount = participantMapper.countByActivityIdAndStatus(activityId, "pending");
        vo.setPendingCount(pendingCount);

        // 设置当前用户状态
        if (userId != null) {
            vo.setIsOrganizer(userId.equals(activity.getOrganizerId()));

            ActivityParticipant participation = participantMapper.selectByActivityAndUser(activityId, userId);
            if (participation != null) {
                vo.setCurrentUserStatus(participation.getStatus());
                vo.setCanRegister(false);
                vo.setCanCancel("approved".equals(participation.getStatus()) || "pending".equals(participation.getStatus()));
            } else {
                vo.setCurrentUserStatus("none");
                vo.setCanRegister("recruiting".equals(activity.getStatus()));
                vo.setCanCancel(false);

                if ("full".equals(activity.getStatus())) {
                    vo.setCanRegister(false);
                    vo.setCannotRegisterReason("活动已满员");
                }
            }
        } else {
            vo.setIsOrganizer(false);
            vo.setCurrentUserStatus("none");
            vo.setCanRegister(false);
            vo.setCanCancel(false);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishActivity(ActivityPublishDTO publishDTO, Long userId) {
        log.info("发布活动: publishDTO={}, userId={}", publishDTO, userId);

        Activity activity = new Activity();
        activity.setOrganizerId(userId);
        activity.setTypeCode(publishDTO.getTypeCode());
        activity.setTitle(publishDTO.getTitle());
        activity.setDescription(publishDTO.getDescription());
        activity.setCoverImageUrl(publishDTO.getCoverImageUrl());
        activity.setStartTime(publishDTO.getStartTime());
        activity.setEndTime(publishDTO.getEndTime());
        activity.setRegistrationDeadline(publishDTO.getRegistrationDeadline());
        activity.setLocationName(publishDTO.getLocationName());
        activity.setLocationAddress(publishDTO.getLocationAddress());
        activity.setCity(publishDTO.getCity());
        activity.setDistrict(publishDTO.getDistrict());
        activity.setLongitude(publishDTO.getLongitude());
        activity.setLatitude(publishDTO.getLatitude());
        activity.setMinMembers(publishDTO.getMinMembers());
        activity.setMaxMembers(publishDTO.getMaxMembers());
        activity.setCurrentMembers(1); // 发起人算一个
        activity.setGenderLimit(publishDTO.getGenderLimit());
        activity.setIsPaid(publishDTO.getIsPaid());
        activity.setFee(publishDTO.getFee());
        activity.setFeeDescription(publishDTO.getFeeDescription());
        activity.setNeedApproval(publishDTO.getNeedApproval());
        activity.setContactInfo(publishDTO.getContactInfo());
        activity.setStatus("recruiting");
        activity.setViewCount(0);
        activity.setShareCount(0);

        activityMapper.insert(activity);

        log.info("活动发布成功: activityId={}", activity.getActivityId());
        return activity.getActivityId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long registerActivity(ActivityRegisterDTO registerDTO, Long userId) {
        log.info("报名活动: registerDTO={}, userId={}", registerDTO, userId);

        Long activityId = registerDTO.getActivityId();
        Activity activity = activityMapper.selectById(activityId);

        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        if (!"recruiting".equals(activity.getStatus())) {
            throw new RuntimeException("活动当前不接受报名");
        }

        // 检查是否已报名
        ActivityParticipant existing = participantMapper.selectByActivityAndUser(activityId, userId);
        if (existing != null) {
            throw new RuntimeException("您已报名该活动");
        }

        // 检查是否满员
        if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
            throw new RuntimeException("活动已满员");
        }

        // 创建报名记录
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivityId(activityId);
        participant.setUserId(userId);
        participant.setMessage(registerDTO.getMessage());

        if (activity.getNeedApproval()) {
            participant.setStatus("pending");
        } else {
            participant.setStatus("approved");
            participant.setApproveTime(LocalDateTime.now());

            // 更新活动人数
            activity.setCurrentMembers(activity.getCurrentMembers() + 1);
            if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
                activity.setStatus("full");
            }
            activityMapper.updateById(activity);
        }

        participantMapper.insert(participant);

        log.info("报名成功: participantId={}", participant.getParticipantId());
        return participant.getParticipantId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegistration(Long activityId, Long userId, String reason) {
        log.info("取消报名: activityId={}, userId={}, reason={}", activityId, userId, reason);

        ActivityParticipant participant = participantMapper.selectByActivityAndUser(activityId, userId);
        if (participant == null) {
            return false;
        }

        // 更新状态为已取消
        participant.setStatus("cancelled");
        participant.setCancelTime(LocalDateTime.now());
        participant.setCancelReason(reason);
        participantMapper.updateById(participant);

        // 如果之前是已通过状态，需要更新活动人数
        if ("approved".equals(participant.getStatus())) {
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null) {
                activity.setCurrentMembers(Math.max(0, activity.getCurrentMembers() - 1));
                if ("full".equals(activity.getStatus()) && activity.getCurrentMembers() < activity.getMaxMembers()) {
                    activity.setStatus("recruiting");
                }
                activityMapper.updateById(activity);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRegistration(Long activityId, Long participantId, boolean approved, Long userId) {
        log.info("审核报名: activityId={}, participantId={}, approved={}, userId={}", activityId, participantId, approved, userId);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !userId.equals(activity.getOrganizerId())) {
            return false;
        }

        ActivityParticipant participant = participantMapper.selectById(participantId);
        if (participant == null || !activityId.equals(participant.getActivityId())) {
            return false;
        }

        if (!"pending".equals(participant.getStatus())) {
            return false;
        }

        participant.setApproveTime(LocalDateTime.now());

        if (approved) {
            // 检查是否满员
            if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
                throw new RuntimeException("活动已满员，无法通过审核");
            }

            participant.setStatus("approved");
            participantMapper.updateById(participant);

            // 更新活动人数
            activity.setCurrentMembers(activity.getCurrentMembers() + 1);
            if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
                activity.setStatus("full");
            }
            activityMapper.updateById(activity);
        } else {
            participant.setStatus("rejected");
            participantMapper.updateById(participant);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelActivity(Long activityId, Long userId, String reason) {
        log.info("取消活动: activityId={}, userId={}, reason={}", activityId, userId, reason);

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !userId.equals(activity.getOrganizerId())) {
            return false;
        }

        activity.setStatus("cancelled");
        activityMapper.updateById(activity);

        return true;
    }

    @Override
    public List<ActivityParticipant> getParticipants(Long activityId, String status) {
        return participantMapper.selectByActivityId(activityId, status);
    }

    @Override
    public List<ActivityParticipant> getUserParticipations(Long userId, String status) {
        return participantMapper.selectByUserId(userId, status);
    }

    @Override
    public List<ActivityType> getAllActivityTypes() {
        return activityTypeMapper.selectAllEnabled();
    }

    @Override
    public List<ActivityType> getHotActivityTypes() {
        return activityTypeMapper.selectHotTypes();
    }

    @Override
    public void incrementViewCount(Long activityId) {
        activityMapper.update(null,
            new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getActivityId, activityId)
                .setSql("view_count = view_count + 1"));
    }

    @Override
    public void incrementShareCount(Long activityId) {
        activityMapper.update(null,
            new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getActivityId, activityId)
                .setSql("share_count = share_count + 1"));
    }

    // ========== 私有方法 ==========

    private ActivityListVO convertToListVO(Activity activity) {
        ActivityListVO vo = new ActivityListVO();
        vo.setActivityId(activity.getActivityId());
        vo.setTypeCode(activity.getTypeCode());
        vo.setTypeName(getTypeName(activity.getTypeCode()));
        vo.setTitle(activity.getTitle());
        vo.setCoverImageUrl(activity.getCoverImageUrl());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setTimeDisplay(formatTimeDisplay(activity.getStartTime(), activity.getEndTime()));
        vo.setLocationName(activity.getLocationName());
        vo.setCity(activity.getCity());
        vo.setDistrict(activity.getDistrict());
        vo.setCurrentMembers(activity.getCurrentMembers());
        vo.setMaxMembers(activity.getMaxMembers());
        vo.setMembersDisplay(activity.getCurrentMembers() + "/" + activity.getMaxMembers() + "人");
        vo.setGenderLimit(activity.getGenderLimit());
        vo.setGenderLimitDisplay(getGenderLimitDisplay(activity.getGenderLimit()));
        vo.setIsPaid(activity.getIsPaid());
        vo.setFee(activity.getFee());
        vo.setFeeDisplay(activity.getIsPaid() ? "¥" + activity.getFee() + "/人" : "免费");
        vo.setStatus(activity.getStatus());
        vo.setStatusDisplay(getStatusDisplay(activity.getStatus()));

        // TODO: 查询发起人信息和参与者头像

        return vo;
    }

    private ActivityDetailVO convertToDetailVO(Activity activity) {
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivityId(activity.getActivityId());
        vo.setTypeCode(activity.getTypeCode());
        vo.setTypeName(getTypeName(activity.getTypeCode()));
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setCoverImageUrl(activity.getCoverImageUrl());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setTimeDisplay(formatTimeDisplay(activity.getStartTime(), activity.getEndTime()));
        vo.setRegistrationDeadline(activity.getRegistrationDeadline());
        vo.setRegistrationDeadlineDisplay(activity.getRegistrationDeadline() != null ?
            "报名截止: " + activity.getRegistrationDeadline().toLocalDate() : null);
        vo.setCreateTime(activity.getCreateTime());
        vo.setLocationName(activity.getLocationName());
        vo.setLocationAddress(activity.getLocationAddress());
        vo.setCity(activity.getCity());
        vo.setDistrict(activity.getDistrict());
        vo.setLongitude(activity.getLongitude());
        vo.setLatitude(activity.getLatitude());
        vo.setMinMembers(activity.getMinMembers());
        vo.setMaxMembers(activity.getMaxMembers());
        vo.setCurrentMembers(activity.getCurrentMembers());
        vo.setMembersDisplay(activity.getCurrentMembers() + "/" + activity.getMaxMembers() + "人");
        vo.setGenderLimit(activity.getGenderLimit());
        vo.setGenderLimitDisplay(getGenderLimitDisplay(activity.getGenderLimit()));
        vo.setIsPaid(activity.getIsPaid());
        vo.setFee(activity.getFee());
        vo.setFeeDescription(activity.getFeeDescription());
        vo.setFeeDisplay(activity.getIsPaid() ? "¥" + activity.getFee() + "/人" : "免费");
        vo.setStatus(activity.getStatus());
        vo.setStatusDisplay(getStatusDisplay(activity.getStatus()));
        vo.setNeedApproval(activity.getNeedApproval());
        vo.setContactInfo(activity.getContactInfo());
        vo.setViewCount(activity.getViewCount());
        vo.setShareCount(activity.getShareCount());

        // TODO: 查询发起人信息

        return vo;
    }

    private List<ActivityDetailVO.ParticipantVO> convertToParticipantVOList(List<ActivityParticipant> participants) {
        if (participants == null) {
            return new ArrayList<>();
        }
        return participants.stream().map(p -> {
            ActivityDetailVO.ParticipantVO vo = new ActivityDetailVO.ParticipantVO();
            vo.setUserId(p.getUserId());
            vo.setStatus(p.getStatus());
            vo.setRegisterTime(p.getRegisterTime());
            vo.setMessage(p.getMessage());
            // TODO: 查询用户昵称、头像等
            vo.setNickname("用户" + p.getUserId());
            return vo;
        }).collect(Collectors.toList());
    }

    private String getTypeName(String typeCode) {
        if (typeNameCache == null) {
            List<ActivityType> types = activityTypeMapper.selectAllEnabled();
            typeNameCache = types.stream()
                .collect(Collectors.toMap(ActivityType::getTypeCode, ActivityType::getTypeName));
        }
        return typeNameCache.getOrDefault(typeCode, typeCode);
    }

    private String formatTimeDisplay(LocalDateTime start, LocalDateTime end) {
        if (start == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日 HH:mm");
        String startStr = start.format(formatter);
        if (end != null && start.toLocalDate().equals(end.toLocalDate())) {
            return startStr + "-" + end.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return startStr;
    }

    private String getGenderLimitDisplay(String genderLimit) {
        return switch (genderLimit) {
            case "male" -> "仅男生";
            case "female" -> "仅女生";
            default -> "不限";
        };
    }

    private String getStatusDisplay(String status) {
        return switch (status) {
            case "recruiting" -> "招募中";
            case "full" -> "已满员";
            case "ongoing" -> "进行中";
            case "completed" -> "已结束";
            case "cancelled" -> "已取消";
            default -> status;
        };
    }
}
