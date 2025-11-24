package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.SearchHistoryDeleteDTO;
import org.dromara.appbff.domain.dto.SearchSuggestQueryDTO;
import org.dromara.appbff.domain.vo.SearchDeleteVO;
import org.dromara.appbff.domain.vo.SearchInitVO;
import org.dromara.appbff.domain.vo.SearchSuggestVO;
import org.dromara.appbff.service.HomeSearchService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 首页搜索服务实现（Mock 数据）
 * <p>
 * 🎯 核心功能:
 * 1. 返回搜索历史记录（最多10条）
 * 2. 返回热门搜索关键词
 * 3. 根据输入关键词提供搜索建议
 * 4. 支持删除搜索历史
 * <p>
 * ⚠️ 当前实现:
 * - 使用 Mock 数据模拟搜索功能
 * - 搜索历史使用内存存储（ConcurrentHashMap）
 * - 建议数据基于关键词匹配生成
 * <p>
 * 🔮 后续改进:
 * - 接入 xypai-content 服务获取真实的话题、动态数据
 * - 接入 xypai-user 服务获取真实的用户数据
 * - 使用 Redis 存储搜索历史
 * - 接入 Elasticsearch 实现全文搜索
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeSearchServiceImpl implements HomeSearchService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Mock：用户搜索历史（内存存储）
    // Key: userId, Value: List<SearchHistoryItem>
    private static final Map<Long, List<SearchInitVO.SearchHistoryItem>> USER_SEARCH_HISTORY = new ConcurrentHashMap<>();

    // Mock：热门搜索关键词（全局数据）
    private static final List<SearchInitVO.HotKeywordItem> HOT_KEYWORDS = List.of(
        SearchInitVO.HotKeywordItem.builder().keyword("王者荣耀").rank(1).isHot(true).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("英雄联盟").rank(2).isHot(true).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("探店").rank(3).isHot(false).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("K歌陪唱").rank(4).isHot(false).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("台球陪练").rank(5).isHot(true).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("剧本杀").rank(6).isHot(false).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("桌游陪玩").rank(7).isHot(false).build(),
        SearchInitVO.HotKeywordItem.builder().keyword("游戏陪玩").rank(8).isHot(true).build()
    );

    // Mock：搜索建议数据库
    private static final Map<String, List<String>> SUGGESTION_DATABASE = new HashMap<>();

    static {
        SUGGESTION_DATABASE.put("王者", List.of("王者荣耀陪玩", "王者荣耀排位", "王者荣耀上分", "王者荣耀教练"));
        SUGGESTION_DATABASE.put("英雄", List.of("英雄联盟陪玩", "英雄联盟上分", "英雄联盟教学"));
        SUGGESTION_DATABASE.put("探店", List.of("探店达人", "探店陪玩", "探店拍照"));
        SUGGESTION_DATABASE.put("K歌", List.of("K歌陪唱", "K歌高手", "K歌教学"));
        SUGGESTION_DATABASE.put("台球", List.of("台球陪练", "台球教学", "台球高手"));
        SUGGESTION_DATABASE.put("剧本", List.of("剧本杀陪玩", "剧本杀达人", "剧本杀推理"));
        SUGGESTION_DATABASE.put("桌游", List.of("桌游陪玩", "桌游达人", "桌游教学"));
        SUGGESTION_DATABASE.put("游戏", List.of("游戏陪玩", "游戏代练", "游戏教学"));
    }

    @Override
    public SearchInitVO getSearchInit(Long userId) {
        log.info("获取搜索初始数据, userId: {}", userId);

        // 获取用户的搜索历史（最多10条）
        List<SearchInitVO.SearchHistoryItem> history = USER_SEARCH_HISTORY.getOrDefault(userId, new ArrayList<>());
        if (history.size() > 10) {
            history = history.subList(0, 10);
        }

        return SearchInitVO.builder()
            .searchHistory(history)
            .hotKeywords(HOT_KEYWORDS)
            .placeholder("搜索更多")
            .build();
    }

    @Override
    public SearchSuggestVO getSearchSuggestions(SearchSuggestQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword();
        Integer limit = queryDTO.getLimit() != null ? queryDTO.getLimit() : 10;

        log.info("获取搜索建议, keyword: {}, limit: {}", keyword, limit);

        List<SearchSuggestVO.SuggestionItem> suggestions = new ArrayList<>();

        // 1. 匹配用户建议（使用 👤 图标）
        if (keyword.matches(".*\\d+$")) {
            suggestions.add(SearchSuggestVO.SuggestionItem.builder()
                .text(keyword)
                .type("user")
                .highlight(keyword)
                .icon("👤")
                .extra("用户")
                .build());
        }

        // 2. 匹配话题建议（使用 # 图标）
        if (keyword.contains("王者") || keyword.contains("英雄") || keyword.contains("游戏")) {
            suggestions.add(SearchSuggestVO.SuggestionItem.builder()
                .text("#" + keyword + "话题")
                .type("topic")
                .highlight(keyword)
                .icon("#")
                .extra("23条动态")
                .build());
        }

        // 3. 匹配关键词建议（使用 🔍 图标）
        for (Map.Entry<String, List<String>> entry : SUGGESTION_DATABASE.entrySet()) {
            if (keyword.contains(entry.getKey())) {
                for (String suggestion : entry.getValue()) {
                    if (suggestions.size() >= limit) {
                        break;
                    }
                    suggestions.add(SearchSuggestVO.SuggestionItem.builder()
                        .text(suggestion)
                        .type("keyword")
                        .highlight(entry.getKey())
                        .icon("🔍")
                        .extra(null)
                        .build());
                }
            }
        }

        // 4. 如果没有匹配结果，返回默认建议
        if (suggestions.isEmpty()) {
            suggestions.add(SearchSuggestVO.SuggestionItem.builder()
                .text(keyword + "陪玩")
                .type("keyword")
                .highlight(keyword)
                .icon("🔍")
                .extra(null)
                .build());
        }

        // 限制建议数量
        if (suggestions.size() > limit) {
            suggestions = suggestions.subList(0, limit);
        }

        return SearchSuggestVO.builder()
            .suggestions(suggestions)
            .build();
    }

    @Override
    public SearchDeleteVO deleteSearchHistory(Long userId, SearchHistoryDeleteDTO deleteDTO) {
        log.info("删除搜索历史, userId: {}, deleteDTO: {}", userId, deleteDTO);

        List<SearchInitVO.SearchHistoryItem> history = USER_SEARCH_HISTORY.get(userId);

        if (history == null || history.isEmpty()) {
            return SearchDeleteVO.builder()
                .success(true)
                .message("没有搜索历史可删除")
                .build();
        }

        // 清空所有历史
        if (Boolean.TRUE.equals(deleteDTO.getClearAll())) {
            USER_SEARCH_HISTORY.remove(userId);
            log.info("已清空用户 {} 的所有搜索历史", userId);
            return SearchDeleteVO.builder()
                .success(true)
                .message("已清空所有搜索历史")
                .build();
        }

        // 删除单条历史
        if (deleteDTO.getKeyword() != null && !deleteDTO.getKeyword().isEmpty()) {
            history.removeIf(item -> item.getKeyword().equals(deleteDTO.getKeyword()));
            log.info("已删除用户 {} 的搜索历史: {}", userId, deleteDTO.getKeyword());
            return SearchDeleteVO.builder()
                .success(true)
                .message("删除成功")
                .build();
        }

        return SearchDeleteVO.builder()
            .success(false)
            .message("请指定要删除的关键词或设置clearAll=true")
            .build();
    }

    /**
     * 辅助方法：添加搜索历史（测试用）
     */
    public void addSearchHistory(Long userId, String keyword, String type) {
        List<SearchInitVO.SearchHistoryItem> history = USER_SEARCH_HISTORY.computeIfAbsent(userId, k -> new ArrayList<>());

        SearchInitVO.SearchHistoryItem item = SearchInitVO.SearchHistoryItem.builder()
            .keyword(keyword)
            .searchTime(LocalDateTime.now().format(DATE_FORMAT))
            .type(type)
            .build();

        history.add(0, item); // 添加到开头
        log.info("已添加搜索历史: userId={}, keyword={}", userId, keyword);
    }
}
