package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.entity.PublishConfig;
import org.dromara.content.domain.entity.Topic;
import org.dromara.content.domain.entity.TopicCategory;
import org.dromara.content.domain.vo.PublishConfigVO;
import org.dromara.content.domain.vo.TopicCategoryVO;
import org.dromara.content.domain.vo.TopicListVO;
import org.dromara.content.mapper.PublishConfigMapper;
import org.dromara.content.mapper.TopicCategoryMapper;
import org.dromara.content.mapper.TopicMapper;
import org.dromara.content.service.IPublishService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发布功能服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements IPublishService {

    private final PublishConfigMapper publishConfigMapper;
    private final TopicCategoryMapper topicCategoryMapper;
    private final TopicMapper topicMapper;

    private static final String CACHE_KEY_PUBLISH_CONFIG = "publish:config";
    private static final String CACHE_KEY_TOPIC_CATEGORIES = "publish:topic:categories";

    @Override
    public PublishConfigVO getPublishConfig() {
        // 1. 尝试从缓存读取
        PublishConfigVO cachedConfig = RedisUtils.getCacheObject(CACHE_KEY_PUBLISH_CONFIG);
        if (cachedConfig != null) {
            log.debug("从缓存获取发布配置");
            return cachedConfig;
        }

        // 2. 查询数据库
        List<PublishConfig> configs = publishConfigMapper.selectList(null);

        // 3. 转换为Map便于查找
        Map<String, String> configMap = configs.stream()
            .collect(Collectors.toMap(
                PublishConfig::getConfigKey,
                PublishConfig::getConfigValue,
                (v1, v2) -> v1
            ));

        // 4. 构建VO
        PublishConfigVO configVO = PublishConfigVO.builder()
            .maxTitleLength(getIntValue(configMap, "max_title_length", 50))
            .maxContentLength(getIntValue(configMap, "max_content_length", 2000))
            .maxImageCount(getIntValue(configMap, "max_image_count", 9))
            .maxVideoCount(getIntValue(configMap, "max_video_count", 1))
            .maxTopicCount(getIntValue(configMap, "max_topic_count", 5))
            .maxImageSize(getLongValue(configMap, "max_image_size", 10485760L))
            .maxVideoSize(getLongValue(configMap, "max_video_size", 104857600L))
            .supportedImageFormats(configMap.getOrDefault("supported_image_formats", "jpg,jpeg,png,gif,webp"))
            .supportedVideoFormats(configMap.getOrDefault("supported_video_formats", "mp4,mov,avi"))
            .build();

        // 5. 缓存结果(24小时)
        RedisUtils.setCacheObject(CACHE_KEY_PUBLISH_CONFIG, configVO, Duration.ofHours(24));

        return configVO;
    }

    @Override
    public List<TopicCategoryVO> getTopicCategories() {
        // 1. 尝试从缓存读取
        List<TopicCategoryVO> cachedCategories = RedisUtils.getCacheObject(CACHE_KEY_TOPIC_CATEGORIES);
        if (cachedCategories != null) {
            log.debug("从缓存获取话题分类");
            return cachedCategories;
        }

        // 2. 查询所有启用的分类
        List<TopicCategory> categories = topicCategoryMapper.selectList(
            new LambdaQueryWrapper<TopicCategory>()
                .eq(TopicCategory::getStatus, 1)
                .orderByAsc(TopicCategory::getSortOrder)
        );

        // 3. 查询所有热门话题
        List<Topic> hotTopics = topicMapper.selectList(
            new LambdaQueryWrapper<Topic>()
                .eq(Topic::getIsHot, 1)
                .orderByDesc(Topic::getPostCount)
                .last("LIMIT 50")
        );

        // 4. 构建分类VO列表
        List<TopicCategoryVO> categoryVOList = new ArrayList<>();

        for (TopicCategory category : categories) {
            // 为每个分类筛选相关话题
            // 这里简单按分类名匹配话题描述，实际应该使用关联表
            List<TopicListVO> relatedTopics = hotTopics.stream()
                .filter(topic -> matchCategory(topic, category))
                .map(this::convertTopicToVO)
                .limit(10)
                .toList();

            TopicCategoryVO categoryVO = TopicCategoryVO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .topics(relatedTopics)
                .build();

            categoryVOList.add(categoryVO);
        }

        // 5. 缓存结果(1小时)
        RedisUtils.setCacheObject(CACHE_KEY_TOPIC_CATEGORIES, categoryVOList, Duration.ofHours(1));

        return categoryVOList;
    }

    /**
     * 简单匹配话题和分类(实际项目应使用关联表)
     */
    private boolean matchCategory(Topic topic, TopicCategory category) {
        if (topic.getDescription() == null) return false;
        String desc = topic.getDescription().toLowerCase();
        String catName = category.getName().toLowerCase();

        // 简单的关键词匹配
        return switch (catName) {
            case "游戏" -> desc.contains("游戏") || desc.contains("王者") || desc.contains("lol") || desc.contains("电竞");
            case "生活" -> desc.contains("生活") || desc.contains("日常") || desc.contains("探店") || desc.contains("旅行");
            case "美食" -> desc.contains("美食") || desc.contains("吃") || desc.contains("餐厅");
            case "运动" -> desc.contains("运动") || desc.contains("健身") || desc.contains("跑步");
            case "娱乐" -> desc.contains("娱乐") || desc.contains("电影") || desc.contains("音乐");
            default -> true; // "其他"分类
        };
    }

    /**
     * 转换Topic到TopicListVO
     */
    private TopicListVO convertTopicToVO(Topic topic) {
        return TopicListVO.builder()
            .id(topic.getId())
            .name(topic.getName())
            .description(topic.getDescription())
            .coverImage(topic.getCoverImage())
            .participantCount(topic.getParticipantCount())
            .postCount(topic.getPostCount())
            .isOfficial(topic.getIsOfficial() == 1)
            .isHot(topic.getIsHot() == 1)
            .build();
    }

    private Integer getIntValue(Map<String, String> map, String key, Integer defaultValue) {
        String value = map.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Long getLongValue(Map<String, String> map, String key, Long defaultValue) {
        String value = map.get(key);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
