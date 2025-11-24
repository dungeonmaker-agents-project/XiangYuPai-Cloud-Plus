package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.ContentCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface CollectionMapper extends BaseMapper<ContentCollection> {

}
