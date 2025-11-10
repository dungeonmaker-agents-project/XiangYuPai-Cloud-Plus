package com.xypai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.chat.domain.entity.MessageSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户消息设置Mapper接口
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Mapper
public interface MessageSettingsMapper extends BaseMapper<MessageSettings> {

    /**
     * 根据用户ID查询消息设置
     * 
     * @param userId 用户ID
     * @return 消息设置
     */
    @Select("SELECT * FROM message_settings WHERE user_id = #{userId}")
    MessageSettings selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除消息设置
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    @Select("DELETE FROM message_settings WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}

