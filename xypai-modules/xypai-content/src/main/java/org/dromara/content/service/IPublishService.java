package org.dromara.content.service;

import org.dromara.content.domain.vo.PublishConfigVO;
import org.dromara.content.domain.vo.TopicCategoryVO;

import java.util.List;

/**
 * 发布功能服务接口
 * 包含发布配置、话题分类等发布相关功能
 *
 * @author XiangYuPai
 */
public interface IPublishService {

    /**
     * 获取发布配置
     *
     * @return 发布配置
     */
    PublishConfigVO getPublishConfig();

    /**
     * 获取话题分类列表(含分类下的话题)
     *
     * @return 分类列表
     */
    List<TopicCategoryVO> getTopicCategories();

}
