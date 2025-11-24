package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.entity.Topic;
import org.dromara.content.domain.vo.TopicListVO;
import org.dromara.content.mapper.TopicMapper;
import org.dromara.content.service.ITopicService;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 话题服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements ITopicService {

    private final TopicMapper topicMapper;

    private static final String CACHE_KEY_HOT_TOPICS = "topic:hot:page:";

    @Override
    public Page<TopicListVO> getHotTopics(Integer page, Integer pageSize) {
        // 1. 尝试从缓存读取
        String cacheKey = CACHE_KEY_HOT_TOPICS + page;
        Page<TopicListVO> cachedPage = RedisUtils.getCacheObject(cacheKey);
        if (cachedPage != null) {
            log.debug("从缓存获取热门话题,页码:{}", page);
            return cachedPage;
        }

        // 2. 查询数据库
        Page<Topic> topicPage = topicMapper.selectPage(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<Topic>()
                .eq(Topic::getIsHot, 1)
                .orderByDesc(Topic::getPostCount)
                .orderByDesc(Topic::getParticipantCount)
        );

        // 3. 转换为VO
        Page<TopicListVO> voPage = convertToVOPage(topicPage);

        // 4. 缓存结果(1小时)
        RedisUtils.setCacheObject(cacheKey, voPage, Duration.ofHours(1));

        return voPage;
    }

    @Override
    public Page<TopicListVO> searchTopics(String keyword, Integer page, Integer pageSize) {
        // 查询数据库(使用LIKE模糊搜索)
        Page<Topic> topicPage = topicMapper.selectPage(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<Topic>()
                .like(Topic::getName, keyword)
                .or()
                .like(Topic::getDescription, keyword)
                .orderByDesc(Topic::getPostCount)
                .orderByDesc(Topic::getParticipantCount)
        );

        // 转换为VO
        return convertToVOPage(topicPage);
    }

    /**
     * 转换Topic实体分页到TopicListVO分页
     */
    private Page<TopicListVO> convertToVOPage(Page<Topic> topicPage) {
        Page<TopicListVO> voPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());

        voPage.setRecords(
            topicPage.getRecords().stream()
                .map(this::convertToVO)
                .toList()
        );

        return voPage;
    }

    /**
     * 转换Topic实体到TopicListVO
     */
    private TopicListVO convertToVO(Topic topic) {
        return TopicListVO.builder()
            .name(topic.getName())
            .description(topic.getDescription())
            .coverImage(topic.getCoverImage())
            .participantCount(topic.getParticipantCount())
            .postCount(topic.getPostCount())
            .isOfficial(topic.getIsOfficial() == 1)
            .isHot(topic.getIsHot() == 1)
            .build();
    }

}
