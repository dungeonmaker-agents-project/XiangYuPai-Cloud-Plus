package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.ServiceListQueryDTO;
import org.dromara.appbff.domain.dto.ServiceReviewQueryDTO;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.SkillServiceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 技能服务服务实现类（Mock数据版本）
 * TODO: 后续替换为真实RPC调用 (xypai-user 的 skill 相关接口)
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceServiceImpl implements SkillServiceService {

    // Mock数据存储
    private static final Map<Long, ServiceDetailVO> SERVICE_STORE = new ConcurrentHashMap<>();
    private static final AtomicLong SERVICE_ID_GENERATOR = new AtomicLong(1000);

    // 技能类型配置
    private static final Map<String, String> SKILL_TYPES = Map.of(
        "王者荣耀", "honor_of_kings",
        "英雄联盟", "league_of_legends",
        "和平精英", "peace_elite",
        "台球", "billiards",
        "桌游", "board_game"
    );

    static {
        initMockData();
    }

    private static void initMockData() {
        // 为每种技能类型创建Mock数据
        String[] skillTypes = {"王者荣耀", "英雄联盟", "和平精英", "台球", "桌游"};
        for (String skillType : skillTypes) {
            for (int i = 1; i <= 15; i++) {
                ServiceDetailVO service = createMockService(SERVICE_ID_GENERATOR.incrementAndGet(), skillType, i);
                SERVICE_STORE.put(service.getServiceId(), service);
            }
        }
    }

    private static ServiceDetailVO createMockService(Long serviceId, String skillType, int index) {
        ServiceDetailVO vo = new ServiceDetailVO();
        vo.setServiceId(serviceId);

        // 服务提供者信息
        ServiceDetailVO.ProviderDetailVO provider = new ServiceDetailVO.ProviderDetailVO();
        provider.setUserId(1000L + serviceId);
        provider.setNickname(generateNickname(index));
        provider.setAvatar("https://picsum.photos/100/100?random=" + serviceId);
        provider.setGender(index % 3 == 0 ? "male" : "female");
        provider.setAge(18 + index % 10);
        provider.setIsOnline(index % 2 == 0);
        provider.setIsVerified(index % 3 == 0);
        provider.setLevel(1 + index % 5);
        provider.setCertifications(index % 3 == 0 ? Arrays.asList("实名认证", "大神") : Arrays.asList("实名认证"));
        vo.setProvider(provider);

        // 封面和图片
        vo.setBannerImage("https://picsum.photos/400/200?random=" + serviceId);
        vo.setImages(Arrays.asList(
            "https://picsum.photos/400/300?random=" + (serviceId * 10 + 1),
            "https://picsum.photos/400/300?random=" + (serviceId * 10 + 2),
            "https://picsum.photos/400/300?random=" + (serviceId * 10 + 3)
        ));

        // 技能信息
        ServiceDetailVO.SkillDetailVO skillInfo = new ServiceDetailVO.SkillDetailVO();
        skillInfo.setSkillType(SKILL_TYPES.getOrDefault(skillType, skillType));
        skillInfo.setSkillLabel(skillType);

        if (skillType.contains("王者") || skillType.contains("英雄") || skillType.contains("和平")) {
            // 游戏类技能
            String[] areas = {"微信区", "QQ区", "安卓区", "iOS区"};
            String[] ranks = {"荣耀王者", "超凡大师", "星耀", "钻石"};
            String[] positions = {"打野", "中路", "射手", "辅助", "上路"};

            skillInfo.setGameArea(areas[index % areas.length]);
            skillInfo.setRank(ranks[index % ranks.length]);
            skillInfo.setRankScore(1500 + index * 50);
            skillInfo.setRankDisplay("巅峰" + skillInfo.getRankScore() + "+");
            skillInfo.setPosition(Arrays.asList(positions[index % positions.length], positions[(index + 1) % positions.length]));
            skillInfo.setVoiceType(index % 2 == 0 ? "软萌" : "御姐");
        } else {
            // 线下服务类技能
            skillInfo.setGameArea(null);
            skillInfo.setRank(null);
        }
        vo.setSkillInfo(skillInfo);

        // 标签
        List<ServiceCardVO.TagVO> tags = new ArrayList<>();
        if (index % 2 == 0) {
            ServiceCardVO.TagVO tag1 = new ServiceCardVO.TagVO();
            tag1.setText("可线上");
            tag1.setType("service");
            tag1.setColor("#4CAF50");
            tags.add(tag1);
        }
        ServiceCardVO.TagVO tag2 = new ServiceCardVO.TagVO();
        tag2.setText(skillType.contains("王者") ? "技术好" : "服务好");
        tag2.setType("skill");
        tag2.setColor("#2196F3");
        tags.add(tag2);
        vo.setTags(tags);

        // 描述
        vo.setDescription(generateDescription(skillType, index));

        // 价格
        ServiceCardVO.PriceVO price = new ServiceCardVO.PriceVO();
        price.setAmount(new BigDecimal(5 + index * 3));
        price.setUnit(skillType.contains("王者") || skillType.contains("英雄") ? "局" : "小时");
        price.setDisplayText(price.getAmount() + " 金币/" + price.getUnit());
        vo.setPrice(price);

        // 可用时间
        ServiceDetailVO.ScheduleVO schedule = new ServiceDetailVO.ScheduleVO();
        schedule.setAvailable("每天 " + (10 + index % 8) + ":00-" + (18 + index % 6) + ":00");
        vo.setSchedule(schedule);

        // 位置信息（线下服务才有）
        if (!skillType.contains("王者") && !skillType.contains("英雄") && !skillType.contains("和平")) {
            ServiceDetailVO.LocationVO location = new ServiceDetailVO.LocationVO();
            location.setAddress("深圳市南山区科技园南路" + (88 + index) + "号");
            location.setDistrict("南山区");
            location.setDistance(500 + index * 200);
            location.setDistanceDisplay(formatDistance(location.getDistance()));
            vo.setLocation(location);
        }

        // 统计数据
        ServiceCardVO.StatsVO stats = new ServiceCardVO.StatsVO();
        stats.setOrders(50 + index * 10);
        stats.setRating(new BigDecimal("4." + (5 + index % 5)));
        stats.setReviewCount(30 + index * 5);
        vo.setStats(stats);

        // 评价信息
        vo.setReviews(createMockReviews(serviceId, index));

        // 可用状态
        vo.setIsAvailable(index % 5 != 0);
        if (!vo.getIsAvailable()) {
            vo.setUnavailableReason("当前服务者暂不接单");
        }

        return vo;
    }

    private static String generateNickname(int index) {
        String[] prefixes = {"小", "大", "萌", "酷", "帅"};
        String[] suffixes = {"糖糖", "宝宝", "大神", "王者", "陪玩"};
        return prefixes[index % prefixes.length] + suffixes[(index / 5) % suffixes.length] + index;
    }

    private static String generateDescription(String skillType, int index) {
        if (skillType.contains("王者")) {
            return "王者荣耀" + (1 + index % 5) + "赛季巅峰" + (1800 + index * 20) + "，主打" +
                (index % 2 == 0 ? "打野/中路" : "射手/辅助") + "，带你上分无压力！声音好听，脾气温和，欢迎来撩~";
        } else if (skillType.contains("台球")) {
            return "专业台球教练，" + (3 + index % 5) + "年陪练经验，可教学中式八球/美式九球。" +
                "耐心细致，新手友好，欢迎预约~";
        } else {
            return "热爱" + skillType + "，" + (2 + index % 4) + "年经验，欢迎一起玩！" +
                "可教学可陪玩，气氛轻松愉快~";
        }
    }

    private static ServiceDetailVO.ReviewsVO createMockReviews(Long serviceId, int index) {
        ServiceDetailVO.ReviewsVO reviews = new ServiceDetailVO.ReviewsVO();
        reviews.setTotal(30 + index * 5);

        // 评价统计
        ServiceDetailVO.ReviewSummaryVO summary = new ServiceDetailVO.ReviewSummaryVO();
        summary.setExcellent(25 + index * 4);
        summary.setPositive(3 + index % 3);
        summary.setNegative(index % 2);
        reviews.setSummary(summary);

        // 评价标签
        List<ServiceDetailVO.ReviewTagVO> tags = new ArrayList<>();
        String[] tagTexts = {"声音好听", "技术好", "态度好", "准时", "有耐心"};
        for (int i = 0; i < 3; i++) {
            ServiceDetailVO.ReviewTagVO tag = new ServiceDetailVO.ReviewTagVO();
            tag.setText(tagTexts[(index + i) % tagTexts.length]);
            tag.setCount(10 + i * 5);
            tags.add(tag);
        }
        reviews.setTags(tags);

        // 最近评价
        List<ServiceDetailVO.ReviewItemVO> recent = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ServiceDetailVO.ReviewItemVO item = new ServiceDetailVO.ReviewItemVO();
            item.setReviewId(serviceId * 100 + i);
            item.setRating(4 + (i % 2));
            item.setContent(generateReviewContent(i));
            item.setCreatedAt(LocalDateTime.now().minusDays(i * 2));

            ServiceDetailVO.ReviewerVO reviewer = new ServiceDetailVO.ReviewerVO();
            reviewer.setUserId(2000L + serviceId * 10 + i);
            reviewer.setNickname("评价用户" + (serviceId * 10 + i));
            reviewer.setAvatar("https://picsum.photos/50/50?random=" + (serviceId * 10 + i));
            item.setReviewer(reviewer);

            if (i == 0) {
                item.setReply("感谢支持，欢迎下次再来~");
            }
            recent.add(item);
        }
        reviews.setRecent(recent);

        return reviews;
    }

    private static String generateReviewContent(int index) {
        String[] contents = {
            "技术很好，带我上了两星，下次还来！",
            "声音超好听，玩得很开心~",
            "非常有耐心，讲解也很详细，推荐！",
            "准时开始，服务态度很好",
            "陪玩大神，MVP全场最佳！"
        };
        return contents[index % contents.length];
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
    public ServiceListResultVO queryServiceList(ServiceListQueryDTO queryDTO, Long userId) {
        log.info("查询服务列表: queryDTO={}, userId={}", queryDTO, userId);

        ServiceListResultVO result = new ServiceListResultVO();

        // 技能类型信息
        ServiceListResultVO.SkillTypeVO skillType = new ServiceListResultVO.SkillTypeVO();
        skillType.setType(SKILL_TYPES.getOrDefault(queryDTO.getSkillType(), queryDTO.getSkillType()));
        skillType.setLabel(queryDTO.getSkillType());
        skillType.setIcon("https://picsum.photos/50/50?random=" + queryDTO.getSkillType().hashCode());
        result.setSkillType(skillType);

        // Tab配置
        List<ServiceListResultVO.TabVO> tabs = new ArrayList<>();
        if (queryDTO.getSkillType().contains("王者") || queryDTO.getSkillType().contains("英雄")) {
            tabs.add(createTab("glory_king", "荣耀王者", 12));
            tabs.add(createTab("online", "线上", 35));
            tabs.add(createTab("offline", "线下", 8));
        } else {
            tabs.add(createTab("online", "线上", 20));
            tabs.add(createTab("offline", "线下", 25));
        }
        tabs.add(createTab("my", "我的", 3));
        result.setTabs(tabs);

        // 筛选配置
        result.setFilters(createFilterConfig(queryDTO.getSkillType()));

        // 查询服务列表
        List<ServiceDetailVO> allServices = SERVICE_STORE.values().stream()
            .filter(s -> s.getSkillInfo().getSkillLabel().equals(queryDTO.getSkillType()))
            .toList();

        // 应用筛选
        List<ServiceDetailVO> filtered = applyFilters(allServices, queryDTO);

        // 应用排序
        filtered = applySort(filtered, queryDTO.getSortBy());

        // 分页
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<ServiceCardVO> cards = new ArrayList<>();
        if (start < filtered.size()) {
            for (int i = start; i < end; i++) {
                cards.add(convertToCard(filtered.get(i)));
            }
        }

        result.setList(cards);
        result.setTotal((long) filtered.size());
        result.setHasMore(end < filtered.size());

        return result;
    }

    private ServiceListResultVO.TabVO createTab(String value, String label, int count) {
        ServiceListResultVO.TabVO tab = new ServiceListResultVO.TabVO();
        tab.setValue(value);
        tab.setLabel(label);
        tab.setCount(count);
        return tab;
    }

    private ServiceListResultVO.FilterConfigVO createFilterConfig(String skillType) {
        ServiceListResultVO.FilterConfigVO config = new ServiceListResultVO.FilterConfigVO();

        // 排序选项
        config.setSortOptions(Arrays.asList(
            createOption("smart", "智能排序"),
            createOption("price_asc", "价格最低"),
            createOption("rating_desc", "评分最高"),
            createOption("orders_desc", "订单最多")
        ));

        // 性别选项
        config.setGenderOptions(Arrays.asList(
            createOption("all", "不限性别"),
            createOption("male", "男"),
            createOption("female", "女")
        ));

        // 状态选项
        config.setStatusOptions(Arrays.asList(
            createOption("online", "在线"),
            createOption("active_3d", "3天内活跃"),
            createOption("active_7d", "7天内活跃")
        ));

        // 游戏类技能特有筛选
        if (skillType.contains("王者") || skillType.contains("英雄") || skillType.contains("和平")) {
            config.setGameAreas(Arrays.asList(
                createOption("微信区", "微信区"),
                createOption("QQ区", "QQ区"),
                createOption("安卓区", "安卓区"),
                createOption("iOS区", "iOS区")
            ));

            config.setRanks(Arrays.asList(
                createOption("荣耀王者", "荣耀王者"),
                createOption("超凡大师", "超凡大师"),
                createOption("星耀", "星耀"),
                createOption("钻石", "钻石")
            ));

            config.setPositions(Arrays.asList(
                createOption("打野", "打野"),
                createOption("中路", "中路"),
                createOption("射手", "射手"),
                createOption("辅助", "辅助"),
                createOption("上路", "上路")
            ));
        }

        // 价格区间
        config.setPriceRanges(Arrays.asList(
            createPriceRange("0-10", "0-10金币", BigDecimal.ZERO, BigDecimal.TEN),
            createPriceRange("10-30", "10-30金币", BigDecimal.TEN, new BigDecimal("30")),
            createPriceRange("30-50", "30-50金币", new BigDecimal("30"), new BigDecimal("50")),
            createPriceRange("50+", "50金币以上", new BigDecimal("50"), null)
        ));

        // 标签
        config.setTags(Arrays.asList(
            createOption("可线上", "可线上"),
            createOption("技术好", "技术好"),
            createOption("声音好听", "声音好听"),
            createOption("新手友好", "新手友好")
        ));

        return config;
    }

    private ServiceListResultVO.OptionVO createOption(String value, String label) {
        ServiceListResultVO.OptionVO option = new ServiceListResultVO.OptionVO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    private ServiceListResultVO.PriceRangeVO createPriceRange(String value, String label, BigDecimal min, BigDecimal max) {
        ServiceListResultVO.PriceRangeVO range = new ServiceListResultVO.PriceRangeVO();
        range.setValue(value);
        range.setLabel(label);
        range.setMin(min);
        range.setMax(max);
        return range;
    }

    private List<ServiceDetailVO> applyFilters(List<ServiceDetailVO> services, ServiceListQueryDTO queryDTO) {
        if (queryDTO.getFilters() == null) {
            return new ArrayList<>(services);
        }

        ServiceListQueryDTO.ServiceFilterDTO filters = queryDTO.getFilters();
        return services.stream()
            .filter(s -> {
                // 性别筛选
                if (filters.getGender() != null && !"all".equals(filters.getGender())) {
                    if (!filters.getGender().equals(s.getProvider().getGender())) {
                        return false;
                    }
                }
                // 在线状态筛选
                if ("online".equals(filters.getStatus())) {
                    if (!s.getProvider().getIsOnline()) {
                        return false;
                    }
                }
                // 游戏大区筛选
                if (filters.getGameArea() != null && s.getSkillInfo().getGameArea() != null) {
                    if (!filters.getGameArea().equals(s.getSkillInfo().getGameArea())) {
                        return false;
                    }
                }
                return true;
            })
            .toList();
    }

    private List<ServiceDetailVO> applySort(List<ServiceDetailVO> services, String sortBy) {
        List<ServiceDetailVO> sorted = new ArrayList<>(services);
        if (sortBy == null || "smart".equals(sortBy)) {
            // 智能排序：在线优先 + 评分 + 订单数
            sorted.sort((a, b) -> {
                int onlineCompare = Boolean.compare(b.getProvider().getIsOnline(), a.getProvider().getIsOnline());
                if (onlineCompare != 0) return onlineCompare;
                return b.getStats().getRating().compareTo(a.getStats().getRating());
            });
        } else if ("price_asc".equals(sortBy)) {
            sorted.sort(Comparator.comparing(s -> s.getPrice().getAmount()));
        } else if ("rating_desc".equals(sortBy)) {
            sorted.sort((a, b) -> b.getStats().getRating().compareTo(a.getStats().getRating()));
        } else if ("orders_desc".equals(sortBy)) {
            sorted.sort((a, b) -> b.getStats().getOrders().compareTo(a.getStats().getOrders()));
        }
        return sorted;
    }

    private ServiceCardVO convertToCard(ServiceDetailVO detail) {
        ServiceCardVO card = new ServiceCardVO();
        card.setServiceId(detail.getServiceId());
        card.setDescription(detail.getDescription());
        card.setTags(detail.getTags());
        card.setPrice(detail.getPrice());
        card.setStats(detail.getStats());

        // Provider
        ServiceCardVO.ProviderBriefVO provider = new ServiceCardVO.ProviderBriefVO();
        provider.setUserId(detail.getProvider().getUserId());
        provider.setAvatar(detail.getProvider().getAvatar());
        provider.setNickname(detail.getProvider().getNickname());
        provider.setGender(detail.getProvider().getGender());
        provider.setAge(detail.getProvider().getAge());
        provider.setIsOnline(detail.getProvider().getIsOnline());
        provider.setIsVerified(detail.getProvider().getIsVerified());
        card.setProvider(provider);

        // Skill
        ServiceCardVO.SkillBriefVO skill = new ServiceCardVO.SkillBriefVO();
        skill.setSkillType(detail.getSkillInfo().getSkillType());
        skill.setGameArea(detail.getSkillInfo().getGameArea());
        skill.setRank(detail.getSkillInfo().getRank());
        skill.setRankScore(detail.getSkillInfo().getRankScore());
        skill.setPosition(detail.getSkillInfo().getPosition());
        card.setSkillInfo(skill);

        // Distance
        if (detail.getLocation() != null) {
            card.setDistance(detail.getLocation().getDistance());
            card.setDistanceDisplay(detail.getLocation().getDistanceDisplay());
        }

        return card;
    }

    @Override
    public ServiceDetailVO getServiceDetail(Long serviceId, Long userId) {
        log.info("获取服务详情: serviceId={}, userId={}", serviceId, userId);
        return SERVICE_STORE.get(serviceId);
    }

    @Override
    public ServiceReviewListVO getServiceReviews(ServiceReviewQueryDTO queryDTO) {
        log.info("获取服务评价列表: queryDTO={}", queryDTO);

        ServiceDetailVO service = SERVICE_STORE.get(queryDTO.getServiceId());
        if (service == null || service.getReviews() == null) {
            return null;
        }

        ServiceReviewListVO result = new ServiceReviewListVO();
        result.setServiceId(queryDTO.getServiceId());
        result.setSummary(service.getReviews().getSummary());
        result.setTags(service.getReviews().getTags());

        // 生成更多评价数据用于分页展示
        List<ServiceDetailVO.ReviewItemVO> allReviews = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ServiceDetailVO.ReviewItemVO item = new ServiceDetailVO.ReviewItemVO();
            item.setReviewId(queryDTO.getServiceId() * 100 + i);
            item.setRating(3 + (i % 3));
            item.setContent(generateReviewContent(i));
            item.setCreatedAt(LocalDateTime.now().minusDays(i));

            ServiceDetailVO.ReviewerVO reviewer = new ServiceDetailVO.ReviewerVO();
            reviewer.setUserId(3000L + i);
            reviewer.setNickname("用户" + (3000 + i));
            reviewer.setAvatar("https://picsum.photos/50/50?random=" + (3000 + i));
            item.setReviewer(reviewer);

            allReviews.add(item);
        }

        // 分页
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allReviews.size());

        result.setTotal((long) allReviews.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) allReviews.size() / pageSize));
        result.setHasNext(end < allReviews.size());

        if (start < allReviews.size()) {
            result.setList(allReviews.subList(start, end));
        } else {
            result.setList(Collections.emptyList());
        }

        return result;
    }
}
