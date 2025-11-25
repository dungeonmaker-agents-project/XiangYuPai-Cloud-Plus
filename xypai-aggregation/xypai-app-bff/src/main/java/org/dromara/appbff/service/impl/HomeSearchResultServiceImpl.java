package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.SearchQueryDTO;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.HomeSearchResultService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页搜索结果服务实现（Mock 数据）
 * <p>
 * 🎯 核心功能:
 * 1. 支持4种Tab搜索：全部、用户、下单、话题
 * 2. 支持分页查询
 * 3. 支持关键词匹配
 * 4. 返回Tab统计信息
 * <p>
 * ⚠️ 当前实现:
 * - 使用 Mock 数据模拟搜索结果
 * - 关键词匹配基于简单的contains判断
 * - 分页逻辑基于内存数据切片
 * <p>
 * 🔮 后续改进:
 * - 接入 Elasticsearch 实现全文搜索
 * - 接入 xypai-content 获取真实动态数据
 * - 接入 xypai-user 获取真实用户数据
 * - 实现复杂的相关性排序算法
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeSearchResultServiceImpl implements HomeSearchResultService {

    // Mock 动态数据
    private static final List<MockPost> MOCK_POSTS = Arrays.asList(
        new MockPost(1L, "王者荣耀上分", "专业陪练，快速上分", "https://picsum.photos/300/400?random=1", "image", 1001L, "游戏达人", 88, 12),
        new MockPost(2L, "英雄联盟", "LOL教学视频", "https://picsum.photos/300/400?random=2", "video", 1002L, "LOL教练", 156, 24),
        new MockPost(3L, "台球技巧", "教你打好台球", "https://picsum.photos/300/400?random=3", "image", 1003L, "台球高手", 99, 8),
        new MockPost(4L, "K歌技巧", "唱歌教学", "https://picsum.photos/300/400?random=4", "video", 1004L, "K歌女王", 234, 45),
        new MockPost(5L, "王者攻略", "最新版本攻略", "https://picsum.photos/300/400?random=5", "image", 1005L, "攻略大神", 567, 89),
        new MockPost(6L, "剧本杀推荐", "好玩的剧本杀", "https://picsum.photos/300/400?random=6", "image", 1006L, "剧本达人", 123, 15),
        new MockPost(7L, "桌游教学", "狼人杀技巧", "https://picsum.photos/300/400?random=7", "video", 1007L, "桌游玩家", 178, 22),
        new MockPost(8L, "游戏陪玩", "全能陪玩", "https://picsum.photos/300/400?random=8", "image", 1008L, "陪玩小姐姐", 445, 67)
    );

    // Mock 用户数据
    private static final List<MockUser> MOCK_USERS = Arrays.asList(
        new MockUser(1001L, "https://picsum.photos/100?random=101", "游戏达人", 19, "male", "专业游戏陪练，上分快", true),
        new MockUser(1002L, "https://picsum.photos/100?random=102", "LOL教练", 25, "male", "5年LOL经验，带你飞", true),
        new MockUser(1003L, "https://picsum.photos/100?random=103", "台球高手", 28, "male", "台球陪练，包教包会", false),
        new MockUser(1004L, "https://picsum.photos/100?random=104", "K歌女王", 22, "female", "唱歌陪唱，欢乐多多", true),
        new MockUser(1005L, "https://picsum.photos/100?random=105", "攻略大神", 24, "male", "各种游戏攻略", true),
        new MockUser(1006L, "https://picsum.photos/100?random=106", "剧本达人", 26, "female", "剧本杀专家", false),
        new MockUser(1007L, "https://picsum.photos/100?random=107", "桌游玩家", 23, "male", "桌游陪玩", true),
        new MockUser(1008L, "https://picsum.photos/100?random=108", "陪玩小姐姐", 21, "female", "甜美声音，游戏技术好", true)
    );

    // Mock 服务提供者数据
    private static final List<MockOrderProvider> MOCK_ORDER_PROVIDERS = Arrays.asList(
        new MockOrderProvider(1001L, "https://picsum.photos/100?random=101", "王者陪练", "female", 1200, List.of("可线上", "2元"), "王者荣耀上分，5年经验", 10, "单", true),
        new MockOrderProvider(1002L, "https://picsum.photos/100?random=102", "LOL大神", "male", 3200, List.of("可线上", "3元"), "英雄联盟教学，钻石段位", 15, "单", true),
        new MockOrderProvider(1003L, "https://picsum.photos/100?random=103", "台球教练", "male", 500, List.of("可线下", "台球厅"), "台球陪练，专业教学", 20, "小时", false),
        new MockOrderProvider(1004L, "https://picsum.photos/100?random=104", "K歌陪唱", "female", 2100, List.of("可线上", "可线下"), "K歌陪唱，嗓音甜美", 12, "单", true),
        new MockOrderProvider(1005L, "https://picsum.photos/100?random=105", "游戏陪玩", "female", 1800, List.of("可线上", "全能"), "多种游戏陪玩", 8, "单", true),
        new MockOrderProvider(1006L, "https://picsum.photos/100?random=106", "剧本杀DM", "male", 4500, List.of("可线下", "剧本杀店"), "剧本杀主持", 30, "场", false)
    );

    // Mock 话题数据
    private static final List<MockTopic> MOCK_TOPICS = Arrays.asList(
        new MockTopic(1L, "王者荣耀", "🎮", "王者荣耀游戏陪玩与交流", true, "热门", 1234, 56789),
        new MockTopic(2L, "英雄联盟", "🎮", "LOL玩家社区", true, "热门", 987, 45678),
        new MockTopic(3L, "台球陪练", "🎱", "台球技巧交流", false, null, 234, 12345),
        new MockTopic(4L, "K歌陪唱", "🎤", "唱歌爱好者聚集地", false, null, 456, 23456),
        new MockTopic(5L, "游戏陪玩", "🎮", "各类游戏陪玩服务", true, "热门", 789, 34567),
        new MockTopic(6L, "剧本杀", "🎭", "剧本杀爱好者", false, null, 345, 15678)
    );

    @Override
    public SearchResultVO search(SearchQueryDTO queryDTO) {
        log.info("执行搜索, keyword: {}, type: {}", queryDTO.getKeyword(), queryDTO.getType());

        // 获取所有Tab的统计信息
        List<SearchResultVO.TabInfo> tabs = buildTabsInfo(queryDTO.getKeyword());

        // 根据type调用对应的方法
        Object results = switch (queryDTO.getType()) {
            case "all" -> searchAll(queryDTO);
            case "user" -> searchUsers(queryDTO, null);
            case "order" -> searchOrders(queryDTO);
            case "topic" -> searchTopics(queryDTO);
            default -> searchAll(queryDTO);
        };

        // 计算总数
        int total = tabs.stream()
            .filter(t -> t.getType().equals(queryDTO.getType()))
            .findFirst()
            .map(SearchResultVO.TabInfo::getCount)
            .orElse(0);

        return SearchResultVO.builder()
            .keyword(queryDTO.getKeyword())
            .total(total)
            .hasMore(false)
            .tabs(tabs)
            .results(results)
            .build();
    }

    @Override
    public SearchAllResultVO searchAll(SearchQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword().toLowerCase();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        // 过滤匹配的动态
        List<SearchAllResultVO.SearchAllItem> allItems = new ArrayList<>();

        for (MockPost post : MOCK_POSTS) {
            if (post.title.toLowerCase().contains(keyword) || post.description.toLowerCase().contains(keyword)) {
                allItems.add(SearchAllResultVO.SearchAllItem.builder()
                    .itemType("post")
                    .itemId(post.postId)
                    .post(SearchAllResultVO.PostInfo.builder()
                        .postId(post.postId)
                        .title(post.title)
                        .description(post.description)
                        .thumbnail(post.thumbnail)
                        .mediaType(post.mediaType)
                        .author(SearchAllResultVO.AuthorInfo.builder()
                            .userId(post.authorId)
                            .avatar("https://picsum.photos/100?random=" + post.authorId)
                            .nickname(post.authorName)
                            .build())
                        .stats(SearchAllResultVO.StatsInfo.builder()
                            .likes(post.likes)
                            .comments(post.comments)
                            .build())
                        .build())
                    .build());
            }
        }

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allItems.size());
        List<SearchAllResultVO.SearchAllItem> pagedItems = allItems.subList(start, Math.max(start, Math.min(end, allItems.size())));

        return SearchAllResultVO.builder()
            .total(allItems.size())
            .hasMore(end < allItems.size())
            .list(pagedItems)
            .build();
    }

    @Override
    public SearchUserResultVO searchUsers(SearchQueryDTO queryDTO, Long currentUserId) {
        String keyword = queryDTO.getKeyword().toLowerCase();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        // 过滤匹配的用户
        List<SearchUserResultVO.SearchUserItem> matchedUsers = MOCK_USERS.stream()
            .filter(user -> user.nickname.toLowerCase().contains(keyword) ||
                           (user.signature != null && user.signature.toLowerCase().contains(keyword)))
            .map(user -> SearchUserResultVO.SearchUserItem.builder()
                .userId(user.userId)
                .avatar(user.avatar)
                .nickname(user.nickname)
                .age(user.age)
                .gender(user.gender)
                .signature(user.signature)
                .isVerified(user.isVerified)
                .relationStatus("none") // Mock: 默认无关系
                .build())
            .collect(Collectors.toList());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, matchedUsers.size());
        List<SearchUserResultVO.SearchUserItem> pagedUsers = matchedUsers.subList(start, Math.max(start, Math.min(end, matchedUsers.size())));

        return SearchUserResultVO.builder()
            .total(matchedUsers.size())
            .hasMore(end < matchedUsers.size())
            .list(pagedUsers)
            .build();
    }

    @Override
    public SearchOrderResultVO searchOrders(SearchQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword().toLowerCase();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        // 过滤匹配的服务提供者
        List<SearchOrderResultVO.SearchOrderItem> matchedProviders = MOCK_ORDER_PROVIDERS.stream()
            .filter(provider -> provider.nickname.toLowerCase().contains(keyword) ||
                               provider.description.toLowerCase().contains(keyword))
            .map(provider -> SearchOrderResultVO.SearchOrderItem.builder()
                .userId(provider.userId)
                .avatar(provider.avatar)
                .nickname(provider.nickname)
                .gender(provider.gender)
                .distance(provider.distance)
                .distanceText(formatDistance(provider.distance))
                .tags(provider.tags.stream()
                    .map(tag -> SearchOrderResultVO.UserTag.builder()
                        .text(tag)
                        .type("service")
                        .color("#7C3AED")
                        .build())
                    .collect(Collectors.toList()))
                .description(provider.description)
                .price(SearchOrderResultVO.PriceInfo.builder()
                    .amount(provider.priceAmount)
                    .unit(provider.priceUnit)
                    .displayText(provider.priceAmount + " 金币/" + provider.priceUnit)
                    .build())
                .isOnline(provider.isOnline)
                .build())
            .collect(Collectors.toList());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, matchedProviders.size());
        List<SearchOrderResultVO.SearchOrderItem> pagedProviders = matchedProviders.subList(start, Math.max(start, Math.min(end, matchedProviders.size())));

        return SearchOrderResultVO.builder()
            .total(matchedProviders.size())
            .hasMore(end < matchedProviders.size())
            .list(pagedProviders)
            .build();
    }

    @Override
    public SearchTopicResultVO searchTopics(SearchQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword().toLowerCase();
        int pageNum = queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize();

        // 过滤匹配的话题
        List<SearchTopicResultVO.SearchTopicItem> matchedTopics = MOCK_TOPICS.stream()
            .filter(topic -> topic.topicName.toLowerCase().contains(keyword) ||
                            (topic.description != null && topic.description.toLowerCase().contains(keyword)))
            .map(topic -> SearchTopicResultVO.SearchTopicItem.builder()
                .topicId(topic.topicId)
                .topicName(topic.topicName)
                .icon(topic.icon)
                .description(topic.description)
                .isHot(topic.isHot)
                .hotLabel(topic.hotLabel)
                .stats(SearchTopicResultVO.TopicStats.builder()
                    .posts(topic.posts)
                    .views(topic.views)
                    .build())
                .build())
            .collect(Collectors.toList());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, matchedTopics.size());
        List<SearchTopicResultVO.SearchTopicItem> pagedTopics = matchedTopics.subList(start, Math.max(start, Math.min(end, matchedTopics.size())));

        return SearchTopicResultVO.builder()
            .total(matchedTopics.size())
            .hasMore(end < matchedTopics.size())
            .list(pagedTopics)
            .build();
    }

    /**
     * 构建所有Tab的统计信息
     */
    private List<SearchResultVO.TabInfo> buildTabsInfo(String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        // 统计各Tab的结果数量
        int allCount = (int) MOCK_POSTS.stream()
            .filter(p -> p.title.toLowerCase().contains(lowerKeyword) || p.description.toLowerCase().contains(lowerKeyword))
            .count();

        int userCount = (int) MOCK_USERS.stream()
            .filter(u -> u.nickname.toLowerCase().contains(lowerKeyword) ||
                        (u.signature != null && u.signature.toLowerCase().contains(lowerKeyword)))
            .count();

        int orderCount = (int) MOCK_ORDER_PROVIDERS.stream()
            .filter(o -> o.nickname.toLowerCase().contains(lowerKeyword) || o.description.toLowerCase().contains(lowerKeyword))
            .count();

        int topicCount = (int) MOCK_TOPICS.stream()
            .filter(t -> t.topicName.toLowerCase().contains(lowerKeyword) ||
                        (t.description != null && t.description.toLowerCase().contains(lowerKeyword)))
            .count();

        return Arrays.asList(
            SearchResultVO.TabInfo.builder().type("all").label("全部").count(allCount).build(),
            SearchResultVO.TabInfo.builder().type("user").label("用户").count(userCount).build(),
            SearchResultVO.TabInfo.builder().type("order").label("下单").count(orderCount).build(),
            SearchResultVO.TabInfo.builder().type("topic").label("话题").count(topicCount).build()
        );
    }

    /**
     * 格式化距离
     */
    private String formatDistance(int distanceInMeters) {
        if (distanceInMeters < 1000) {
            return distanceInMeters + "m";
        } else {
            return String.format("%.1fkm", distanceInMeters / 1000.0);
        }
    }

    // Mock数据类
    private record MockPost(Long postId, String title, String description, String thumbnail, String mediaType,
                           Long authorId, String authorName, int likes, int comments) {}

    private record MockUser(Long userId, String avatar, String nickname, int age, String gender,
                           String signature, boolean isVerified) {}

    private record MockOrderProvider(Long userId, String avatar, String nickname, String gender, int distance,
                                    List<String> tags, String description, int priceAmount, String priceUnit, boolean isOnline) {}

    private record MockTopic(Long topicId, String topicName, String icon, String description,
                            boolean isHot, String hotLabel, int posts, int views) {}
}
