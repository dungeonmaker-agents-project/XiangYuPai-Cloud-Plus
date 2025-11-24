package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

}
