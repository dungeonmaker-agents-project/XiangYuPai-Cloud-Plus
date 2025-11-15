package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Like;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {

}
