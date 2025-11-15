package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
