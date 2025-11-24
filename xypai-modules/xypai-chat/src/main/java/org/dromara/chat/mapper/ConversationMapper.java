package org.dromara.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.chat.domain.entity.Conversation;

/**
 * Conversation Mapper Interface
 * 会话数据访问层
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 获取用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    @Select("SELECT COALESCE(SUM(unread_count), 0) " +
            "FROM conversation " +
            "WHERE user_id = #{userId} AND deleted = 0")
    Integer getTotalUnreadCount(@Param("userId") Long userId);
}
