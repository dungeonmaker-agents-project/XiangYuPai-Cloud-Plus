package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.FeedTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态话题关联Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface FeedTopicMapper extends BaseMapper<FeedTopic> {

}
