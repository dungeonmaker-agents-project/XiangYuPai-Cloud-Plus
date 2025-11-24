package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

}
