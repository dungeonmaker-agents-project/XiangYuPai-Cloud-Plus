package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.vo.FeedListVO;
import org.dromara.content.domain.vo.TopicListVO;

/**
 * 话题服务接口
 *
 * @author XiangYuPai
 */
public interface ITopicService {

    /**
     * 获取热门话题列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @return 话题列表
     */
    Page<TopicListVO> getHotTopics(Integer page, Integer pageSize);

    /**
     * 搜索话题
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页数量
     * @return 话题列表
     */
    Page<TopicListVO> searchTopics(String keyword, Integer page, Integer pageSize);

    /**
     * 获取话题下的动态列表
     *
     * @param topicId  话题ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param userId   当前用户ID（可选）
     * @return 动态列表
     */
    Page<FeedListVO> getTopicFeeds(Long topicId, Integer page, Integer pageSize, Long userId);

}
