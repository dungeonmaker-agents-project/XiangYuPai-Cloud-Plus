package org.dromara.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.chat.domain.entity.Message;

/**
 * Message Mapper Interface
 * 消息数据访问层
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
