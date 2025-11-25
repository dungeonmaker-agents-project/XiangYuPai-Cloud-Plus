package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.*;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.ActivityService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 组局活动服务实现类（Mock数据版本）
 * TODO: 后续替换为真实RPC调用
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    // Mock数据存储
    private static final Map<Long, ActivityDetailVO> ACTIVITY_STORE = new ConcurrentHashMap<>();
    private static final Map<String, Set<Long>> USER_REGISTRATIONS = new ConcurrentHashMap<>(); // userId -> activityIds
    private static final AtomicLong ACTIVITY_ID_GENERATOR = new AtomicLong(1000);

    // 初始化Mock数据
    static {
        initMockData();
    }

    private static void initMockData() {
        // 创建一些Mock活动数据
        for (int i = 1; i <= 20; i++) {
            ActivityDetailVO activity = createMockActivity(ACTIVITY_ID_GENERATOR.incrementAndGet(), i);
            ACTIVITY_STORE.put(activity.getActivityId(), activity);
        }
    }

    private static ActivityDetailVO createMockActivity(Long activityId, int index) {
        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivityId(activityId);

        // 随机活动类型
        String[] types = {"billiards", "board_game", "explore", "sports", "music", "food"};
        String[] typeNames = {"台球", "桌游", "密室探索", "运动健身", "音乐活动", "美食聚餐"};
        int typeIndex = index % types.length;
        vo.setActivityType(types[typeIndex]);
        vo.setActivityTypeName(typeNames[typeIndex]);

        vo.setTitle("周末" + typeNames[typeIndex] + "局第" + index + "期");
        vo.setDescription("欢迎大家来参加" + typeNames[typeIndex] + "活动，新手老手都可以，重在参与和交流！");
        vo.setCoverImageUrl("https://picsum.photos/400/300?random=" + activityId);
        vo.setImageUrls(Arrays.asList(
            "https://picsum.photos/400/300?random=" + (activityId * 10 + 1),
            "https://picsum.photos/400/300?random=" + (activityId * 10 + 2)
        ));

        // 时间设置
        LocalDateTime now = LocalDateTime.now();
        vo.setStartTime(now.plusDays(index).withHour(14).withMinute(0));
        vo.setEndTime(now.plusDays(index).withHour(18).withMinute(0));
        vo.setTimeDisplay(formatTimeDisplay(vo.getStartTime(), vo.getEndTime()));
        vo.setRegistrationDeadline(vo.getStartTime().minusHours(2));
        vo.setRegistrationDeadlineDisplay("报名截止: " + vo.getStartTime().minusHours(2).toLocalDate());
        vo.setCreateTime(now.minusDays(index % 5));

        // 地点
        String[] locations = {"星球台球俱乐部", "欢乐桌游馆", "密室逃脱体验店", "阳光健身房", "音乐工坊", "网红餐厅"};
        vo.setLocationName(locations[typeIndex]);
        vo.setLocationAddress("深圳市南山区科技园南路" + (88 + index) + "号");
        vo.setLongitude(new BigDecimal("113.9" + (30 + index)));
        vo.setLatitude(new BigDecimal("22.5" + (40 + index)));
        vo.setCity("深圳市");
        vo.setDistrict("南山区");
        vo.setDistance(500 + index * 100);
        vo.setDistanceDisplay(formatDistance(vo.getDistance()));

        // 人数与费用
        vo.setMinMembers(2);
        vo.setMaxMembers(4 + index % 6);
        vo.setCurrentMembers(1 + index % vo.getMaxMembers());
        vo.setMembersDisplay(vo.getCurrentMembers() + "/" + vo.getMaxMembers() + "人");

        String[] genders = {"all", "male", "female"};
        String[] genderDisplays = {"不限", "仅男生", "仅女生"};
        int genderIndex = index % 3;
        vo.setGenderLimit(genders[genderIndex]);
        vo.setGenderLimitDisplay(genderDisplays[genderIndex]);

        vo.setIsPaid(index % 3 == 0);
        if (vo.getIsPaid()) {
            vo.setFee(new BigDecimal(30 + index * 5));
            vo.setFeeDescription("包含场地费和饮料");
            vo.setFeeDisplay("¥" + vo.getFee() + "/人");
        } else {
            vo.setFee(BigDecimal.ZERO);
            vo.setFeeDisplay("免费");
        }

        // 状态
        if (vo.getCurrentMembers() >= vo.getMaxMembers()) {
            vo.setStatus("full");
            vo.setStatusDisplay("已满员");
        } else if (vo.getStartTime().isBefore(now)) {
            vo.setStatus("ongoing");
            vo.setStatusDisplay("进行中");
        } else {
            vo.setStatus("recruiting");
            vo.setStatusDisplay("报名中");
        }

        vo.setNeedApproval(index % 4 == 0);

        // 发起人
        ActivityDetailVO.OrganizerVO organizer = new ActivityDetailVO.OrganizerVO();
        organizer.setUserId(100L + index);
        organizer.setNickname("组局达人" + index);
        organizer.setAvatarUrl("https://picsum.photos/100/100?random=" + (100 + index));
        organizer.setIsVerified(index % 2 == 0);
        organizer.setVerifyType(index % 2 == 0 ? "real_name" : null);
        organizer.setLevel(1 + index % 5);
        organizer.setOrganizedCount(5 + index * 2);
        organizer.setGoodRateDisplay((90 + index % 10) + "%");
        organizer.setBio("热爱" + typeNames[typeIndex] + "，希望认识更多朋友");
        vo.setOrganizer(organizer);

        // 参与者
        List<ActivityDetailVO.ParticipantVO> participants = new ArrayList<>();
        for (int j = 0; j < vo.getCurrentMembers(); j++) {
            ActivityDetailVO.ParticipantVO participant = new ActivityDetailVO.ParticipantVO();
            participant.setUserId(200L + index * 10 + j);
            participant.setNickname("参与者" + (index * 10 + j));
            participant.setAvatarUrl("https://picsum.photos/100/100?random=" + (200 + index * 10 + j));
            participant.setGender(j % 2 == 0 ? "male" : "female");
            participant.setStatus("approved");
            participant.setRegisterTime(now.minusHours(j * 2));
            participant.setMessage(j == 0 ? "期待参加！" : null);
            participants.add(participant);
        }
        vo.setParticipants(participants);
        vo.setPendingCount(index % 3);

        // 当前用户状态（默认值，实际会根据userId动态计算）
        vo.setIsOrganizer(false);
        vo.setCurrentUserStatus("none");
        vo.setCanRegister(true);
        vo.setCanCancel(false);

        // 其他
        vo.setTags(Arrays.asList("新手友好", "周末局", typeNames[typeIndex]));
        vo.setContactInfo("微信: activity" + index);
        vo.setViewCount(100 + index * 20);
        vo.setShareCount(10 + index * 2);

        return vo;
    }

    private static String formatTimeDisplay(LocalDateTime start, LocalDateTime end) {
        return String.format("%d月%d日 %s %02d:%02d-%02d:%02d",
            start.getMonthValue(), start.getDayOfMonth(),
            getDayOfWeekChinese(start.getDayOfWeek().getValue()),
            start.getHour(), start.getMinute(),
            end.getHour(), end.getMinute());
    }

    private static String getDayOfWeekChinese(int dayOfWeek) {
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return days[dayOfWeek];
    }

    private static String formatDistance(Integer distance) {
        if (distance == null) return "";
        if (distance < 1000) {
            return distance + "m";
        }
        return String.format("%.1fkm", distance / 1000.0);
    }

    // ========== 接口实现 ==========

    @Override
    public ActivityListResultVO queryActivityList(ActivityListQueryDTO queryDTO, Long userId) {
        log.info("查询活动列表: queryDTO={}, userId={}", queryDTO, userId);

        List<ActivityCardVO> cards = new ArrayList<>();
        List<ActivityDetailVO> activities = new ArrayList<>(ACTIVITY_STORE.values());

        // 简单筛选逻辑
        if (queryDTO.getActivityType() != null && !queryDTO.getActivityType().isEmpty()) {
            activities = activities.stream()
                .filter(a -> queryDTO.getActivityType().contains(a.getActivityType()))
                .toList();
        }
        if (queryDTO.getGender() != null && !"all".equals(queryDTO.getGender())) {
            activities = activities.stream()
                .filter(a -> "all".equals(a.getGenderLimit()) || queryDTO.getGender().equals(a.getGenderLimit()))
                .toList();
        }

        // 排序
        if ("newest".equals(queryDTO.getSortBy())) {
            activities = activities.stream()
                .sorted(Comparator.comparing(ActivityDetailVO::getCreateTime).reversed())
                .toList();
        } else if ("distance_asc".equals(queryDTO.getSortBy())) {
            activities = activities.stream()
                .sorted(Comparator.comparing(ActivityDetailVO::getDistance))
                .toList();
        } else if ("start_time_asc".equals(queryDTO.getSortBy())) {
            activities = activities.stream()
                .sorted(Comparator.comparing(ActivityDetailVO::getStartTime))
                .toList();
        }

        // 分页
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, activities.size());

        if (start < activities.size()) {
            for (int i = start; i < end; i++) {
                cards.add(convertToCard(activities.get(i)));
            }
        }

        // 构建结果
        ActivityListResultVO result = new ActivityListResultVO();
        result.setList(cards);
        result.setTotal((long) activities.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) activities.size() / pageSize));
        result.setHasNext(end < activities.size());

        // 统计信息
        ActivityListResultVO.FilterStatsVO stats = new ActivityListResultVO.FilterStatsVO();
        stats.setTotalActivities(ACTIVITY_STORE.size());
        stats.setTodayNew(5);
        stats.setWeeklyHot(12);
        result.setFilterStats(stats);

        return result;
    }

    private ActivityCardVO convertToCard(ActivityDetailVO detail) {
        ActivityCardVO card = new ActivityCardVO();
        card.setActivityId(detail.getActivityId());
        card.setActivityType(detail.getActivityType());
        card.setActivityTypeName(detail.getActivityTypeName());
        card.setTitle(detail.getTitle());
        card.setCoverImageUrl(detail.getCoverImageUrl());
        card.setStartTime(detail.getStartTime());
        card.setEndTime(detail.getEndTime());
        card.setTimeDisplay(detail.getTimeDisplay());
        card.setLocationName(detail.getLocationName());
        card.setDistance(detail.getDistance());
        card.setDistanceDisplay(detail.getDistanceDisplay());
        card.setCurrentMembers(detail.getCurrentMembers());
        card.setMaxMembers(detail.getMaxMembers());
        card.setMembersDisplay(detail.getMembersDisplay());
        card.setGenderLimit(detail.getGenderLimit());
        card.setGenderLimitDisplay(detail.getGenderLimitDisplay());
        card.setIsPaid(detail.getIsPaid());
        card.setFee(detail.getFee());
        card.setFeeDisplay(detail.getFeeDisplay());
        card.setStatus(detail.getStatus());
        card.setStatusDisplay(detail.getStatusDisplay());
        card.setTags(detail.getTags());

        // 发起人简要信息
        if (detail.getOrganizer() != null) {
            ActivityCardVO.OrganizerBriefVO organizer = new ActivityCardVO.OrganizerBriefVO();
            organizer.setUserId(detail.getOrganizer().getUserId());
            organizer.setNickname(detail.getOrganizer().getNickname());
            organizer.setAvatarUrl(detail.getOrganizer().getAvatarUrl());
            organizer.setIsVerified(detail.getOrganizer().getIsVerified());
            card.setOrganizer(organizer);
        }

        // 参与者头像
        if (detail.getParticipants() != null) {
            card.setParticipantAvatars(detail.getParticipants().stream()
                .limit(5)
                .map(ActivityDetailVO.ParticipantVO::getAvatarUrl)
                .toList());
        }

        return card;
    }

    @Override
    public ActivityDetailVO getActivityDetail(Long activityId, Long userId) {
        log.info("获取活动详情: activityId={}, userId={}", activityId, userId);

        ActivityDetailVO detail = ACTIVITY_STORE.get(activityId);
        if (detail == null) {
            return null;
        }

        // 复制一份，避免修改原数据
        ActivityDetailVO result = copyActivityDetail(detail);

        // 设置当前用户状态
        if (userId != null) {
            result.setIsOrganizer(userId.equals(result.getOrganizer().getUserId()));
            Set<Long> registrations = USER_REGISTRATIONS.getOrDefault(userId.toString(), Collections.emptySet());
            if (registrations.contains(activityId)) {
                result.setCurrentUserStatus("approved");
                result.setCanRegister(false);
                result.setCanCancel(true);
            } else {
                result.setCurrentUserStatus("none");
                result.setCanRegister("recruiting".equals(result.getStatus()));
                result.setCanCancel(false);
            }

            // 未报名且已满员
            if ("none".equals(result.getCurrentUserStatus()) && "full".equals(result.getStatus())) {
                result.setCanRegister(false);
                result.setCannotRegisterReason("活动已满员");
            }
        }

        // 增加浏览量
        detail.setViewCount(detail.getViewCount() + 1);

        return result;
    }

    private ActivityDetailVO copyActivityDetail(ActivityDetailVO source) {
        ActivityDetailVO copy = new ActivityDetailVO();
        copy.setActivityId(source.getActivityId());
        copy.setActivityType(source.getActivityType());
        copy.setActivityTypeName(source.getActivityTypeName());
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setCoverImageUrl(source.getCoverImageUrl());
        copy.setImageUrls(source.getImageUrls());
        copy.setStartTime(source.getStartTime());
        copy.setEndTime(source.getEndTime());
        copy.setTimeDisplay(source.getTimeDisplay());
        copy.setRegistrationDeadline(source.getRegistrationDeadline());
        copy.setRegistrationDeadlineDisplay(source.getRegistrationDeadlineDisplay());
        copy.setCreateTime(source.getCreateTime());
        copy.setLocationName(source.getLocationName());
        copy.setLocationAddress(source.getLocationAddress());
        copy.setLongitude(source.getLongitude());
        copy.setLatitude(source.getLatitude());
        copy.setCity(source.getCity());
        copy.setDistrict(source.getDistrict());
        copy.setDistance(source.getDistance());
        copy.setDistanceDisplay(source.getDistanceDisplay());
        copy.setMinMembers(source.getMinMembers());
        copy.setMaxMembers(source.getMaxMembers());
        copy.setCurrentMembers(source.getCurrentMembers());
        copy.setMembersDisplay(source.getMembersDisplay());
        copy.setGenderLimit(source.getGenderLimit());
        copy.setGenderLimitDisplay(source.getGenderLimitDisplay());
        copy.setIsPaid(source.getIsPaid());
        copy.setFee(source.getFee());
        copy.setFeeDescription(source.getFeeDescription());
        copy.setFeeDisplay(source.getFeeDisplay());
        copy.setStatus(source.getStatus());
        copy.setStatusDisplay(source.getStatusDisplay());
        copy.setNeedApproval(source.getNeedApproval());
        copy.setOrganizer(source.getOrganizer());
        copy.setParticipants(source.getParticipants());
        copy.setPendingCount(source.getPendingCount());
        copy.setTags(source.getTags());
        copy.setContactInfo(source.getContactInfo());
        copy.setViewCount(source.getViewCount());
        copy.setShareCount(source.getShareCount());
        return copy;
    }

    @Override
    public ActivityRegisterResultVO registerActivity(ActivityRegisterDTO registerDTO, Long userId) {
        log.info("报名活动: registerDTO={}, userId={}", registerDTO, userId);

        ActivityDetailVO activity = ACTIVITY_STORE.get(registerDTO.getActivityId());
        ActivityRegisterResultVO result = new ActivityRegisterResultVO();

        if (activity == null) {
            result.setSuccess(false);
            result.setStatusMessage("活动不存在");
            return result;
        }

        // 检查是否已报名
        String userKey = userId.toString();
        Set<Long> registrations = USER_REGISTRATIONS.computeIfAbsent(userKey, k -> ConcurrentHashMap.newKeySet());
        if (registrations.contains(registerDTO.getActivityId())) {
            result.setSuccess(false);
            result.setStatusMessage("您已报名该活动");
            return result;
        }

        // 检查是否满员
        if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
            result.setSuccess(false);
            result.setStatusMessage("活动已满员");
            return result;
        }

        // 报名成功
        registrations.add(registerDTO.getActivityId());
        activity.setCurrentMembers(activity.getCurrentMembers() + 1);
        activity.setMembersDisplay(activity.getCurrentMembers() + "/" + activity.getMaxMembers() + "人");

        if (activity.getCurrentMembers() >= activity.getMaxMembers()) {
            activity.setStatus("full");
            activity.setStatusDisplay("已满员");
        }

        result.setSuccess(true);
        result.setStatus(activity.getNeedApproval() ? "pending" : "approved");
        result.setStatusMessage(activity.getNeedApproval() ? "报名成功，等待发起人审核" : "报名成功");
        result.setNeedPay(activity.getIsPaid());
        result.setPayAmount(activity.getFee());
        result.setCurrentMembers(activity.getCurrentMembers());
        result.setMaxMembers(activity.getMaxMembers());
        result.setActivityTitle(activity.getTitle());
        result.setActivityTimeDisplay(activity.getTimeDisplay());
        result.setActivityLocation(activity.getLocationName());

        if (result.getNeedPay()) {
            result.setPayOrderId("PAY" + System.currentTimeMillis());
        }

        return result;
    }

    @Override
    public Boolean cancelActivityRegistration(ActivityRegisterCancelDTO cancelDTO, Long userId) {
        log.info("取消报名: cancelDTO={}, userId={}", cancelDTO, userId);

        String userKey = userId.toString();
        Set<Long> registrations = USER_REGISTRATIONS.get(userKey);
        if (registrations == null || !registrations.contains(cancelDTO.getActivityId())) {
            return false;
        }

        ActivityDetailVO activity = ACTIVITY_STORE.get(cancelDTO.getActivityId());
        if (activity == null) {
            return false;
        }

        registrations.remove(cancelDTO.getActivityId());
        activity.setCurrentMembers(Math.max(0, activity.getCurrentMembers() - 1));
        activity.setMembersDisplay(activity.getCurrentMembers() + "/" + activity.getMaxMembers() + "人");

        if (activity.getCurrentMembers() < activity.getMaxMembers() && "full".equals(activity.getStatus())) {
            activity.setStatus("recruiting");
            activity.setStatusDisplay("报名中");
        }

        return true;
    }

    @Override
    public ActivityPublishConfigVO getPublishConfig(Long userId) {
        log.info("获取发布配置: userId={}", userId);

        ActivityPublishConfigVO config = new ActivityPublishConfigVO();

        // 活动类型
        List<ActivityPublishConfigVO.ActivityTypeVO> types = new ArrayList<>();
        String[][] typeData = {
            {"billiards", "台球", "true"},
            {"board_game", "桌游", "true"},
            {"explore", "密室探索", "false"},
            {"sports", "运动健身", "true"},
            {"music", "音乐活动", "false"},
            {"food", "美食聚餐", "true"},
            {"movie", "电影观影", "false"},
            {"travel", "周边旅行", "false"}
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
    }

    private ActivityPublishConfigVO.OptionVO createOption(String value, String label) {
        ActivityPublishConfigVO.OptionVO option = new ActivityPublishConfigVO.OptionVO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    @Override
    public ActivityPublishResultVO publishActivity(ActivityPublishDTO publishDTO, Long userId) {
        log.info("发布活动: publishDTO={}, userId={}", publishDTO, userId);

        ActivityPublishResultVO result = new ActivityPublishResultVO();

        // 创建新活动
        Long activityId = ACTIVITY_ID_GENERATOR.incrementAndGet();
        ActivityDetailVO activity = new ActivityDetailVO();
        activity.setActivityId(activityId);
        activity.setActivityType(publishDTO.getActivityType());
        activity.setActivityTypeName(getActivityTypeName(publishDTO.getActivityType()));
        activity.setTitle(publishDTO.getTitle());
        activity.setDescription(publishDTO.getDescription());
        activity.setCoverImageUrl(publishDTO.getCoverImageId() != null ?
            "https://picsum.photos/400/300?random=" + publishDTO.getCoverImageId() :
            "https://picsum.photos/400/300?random=" + activityId);
        activity.setStartTime(publishDTO.getStartTime());
        activity.setEndTime(publishDTO.getEndTime());
        activity.setTimeDisplay(formatTimeDisplay(publishDTO.getStartTime(), publishDTO.getEndTime()));
        activity.setRegistrationDeadline(publishDTO.getRegistrationDeadline());
        activity.setCreateTime(LocalDateTime.now());
        activity.setLocationName(publishDTO.getLocationName());
        activity.setLocationAddress(publishDTO.getLocationAddress());
        activity.setLongitude(publishDTO.getLongitude());
        activity.setLatitude(publishDTO.getLatitude());
        activity.setMinMembers(publishDTO.getMinMembers());
        activity.setMaxMembers(publishDTO.getMaxMembers());
        activity.setCurrentMembers(1); // 发起人算一个
        activity.setMembersDisplay("1/" + publishDTO.getMaxMembers() + "人");
        activity.setGenderLimit(publishDTO.getGenderLimit() != null ? publishDTO.getGenderLimit() : "all");
        activity.setGenderLimitDisplay(getGenderLimitDisplay(activity.getGenderLimit()));
        activity.setIsPaid(publishDTO.getIsPaid() != null && publishDTO.getIsPaid());
        activity.setFee(publishDTO.getFee() != null ? publishDTO.getFee() : BigDecimal.ZERO);
        activity.setFeeDescription(publishDTO.getFeeDescription());
        activity.setFeeDisplay(activity.getIsPaid() ? "¥" + activity.getFee() + "/人" : "免费");
        activity.setStatus("recruiting");
        activity.setStatusDisplay("报名中");
        activity.setNeedApproval(publishDTO.getNeedApproval() != null && publishDTO.getNeedApproval());
        activity.setTags(publishDTO.getTags());
        activity.setContactInfo(publishDTO.getContactInfo());
        activity.setViewCount(0);
        activity.setShareCount(0);

        // 发起人信息
        ActivityDetailVO.OrganizerVO organizer = new ActivityDetailVO.OrganizerVO();
        organizer.setUserId(userId);
        organizer.setNickname("用户" + userId);
        organizer.setAvatarUrl("https://picsum.photos/100/100?random=" + userId);
        organizer.setIsVerified(false);
        organizer.setLevel(1);
        organizer.setOrganizedCount(1);
        organizer.setGoodRateDisplay("100%");
        activity.setOrganizer(organizer);

        activity.setParticipants(new ArrayList<>());
        activity.setPendingCount(0);

        ACTIVITY_STORE.put(activityId, activity);

        // 构建返回结果
        result.setSuccess(true);
        result.setActivityId(activityId);
        result.setTitle(activity.getTitle());
        result.setStatus("recruiting");
        result.setStatusMessage("活动已发布成功，等待其他用户报名");
        result.setNeedPay(false);
        result.setFreePublishRemaining(2);
        result.setShareUrl("https://xypai.com/activity/" + activityId);
        result.setShareQrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=https://xypai.com/activity/" + activityId);

        return result;
    }

    private String getActivityTypeName(String type) {
        Map<String, String> typeNames = Map.of(
            "billiards", "台球",
            "board_game", "桌游",
            "explore", "密室探索",
            "sports", "运动健身",
            "music", "音乐活动",
            "food", "美食聚餐",
            "movie", "电影观影",
            "travel", "周边旅行"
        );
        return typeNames.getOrDefault(type, type);
    }

    private String getGenderLimitDisplay(String genderLimit) {
        Map<String, String> displays = Map.of(
            "all", "不限",
            "male", "仅男生",
            "female", "仅女生"
        );
        return displays.getOrDefault(genderLimit, "不限");
    }

    @Override
    public ActivityPayResultVO payActivity(ActivityPayDTO payDTO, Long userId) {
        log.info("支付活动: payDTO={}, userId={}", payDTO, userId);

        ActivityPayResultVO result = new ActivityPayResultVO();
        ActivityDetailVO activity = ACTIVITY_STORE.get(payDTO.getActivityId());

        if (activity == null) {
            result.setSuccess(false);
            result.setStatusMessage("活动不存在");
            return result;
        }

        result.setSuccess(true);
        result.setPayOrderId("PAY" + System.currentTimeMillis());
        result.setAmount(activity.getFee());
        result.setPaymentMethod(payDTO.getPaymentMethod());
        result.setPaymentStatus("success");
        result.setStatusMessage("支付成功");
        result.setActivityId(activity.getActivityId());
        result.setActivityTitle(activity.getTitle());

        if ("wechat".equals(payDTO.getPaymentMethod())) {
            result.setPaymentParams("{\"appId\":\"wx...\",\"timeStamp\":\"...\"}");
        } else if ("alipay".equals(payDTO.getPaymentMethod())) {
            result.setPaymentParams("alipay_sdk=...");
        }

        result.setTimeoutSeconds(900);

        return result;
    }

    @Override
    public Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId) {
        log.info("审核报名: activityId={}, participantId={}, approved={}, userId={}", activityId, participantId, approved, userId);

        ActivityDetailVO activity = ACTIVITY_STORE.get(activityId);
        if (activity == null || !userId.equals(activity.getOrganizer().getUserId())) {
            return false;
        }

        // Mock实现：简单返回成功
        return true;
    }

    @Override
    public Boolean cancelActivity(Long activityId, String reason, Long userId) {
        log.info("取消活动: activityId={}, reason={}, userId={}", activityId, reason, userId);

        ActivityDetailVO activity = ACTIVITY_STORE.get(activityId);
        if (activity == null || !userId.equals(activity.getOrganizer().getUserId())) {
            return false;
        }

        activity.setStatus("cancelled");
        activity.setStatusDisplay("已取消");

        return true;
    }
}
