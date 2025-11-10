package com.xypai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.chat.domain.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询会话的消息列表(分页)
     *
     * @param conversationId 会话ID
     * @param baseMessageId 基准消息ID(用于分页)
     * @param direction 查询方向(before=向前, after=向后)
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ChatMessage> selectConversationMessages(@Param("conversationId") Long conversationId,
                                                @Param("baseMessageId") Long baseMessageId,
                                                @Param("direction") String direction,
                                                @Param("limit") Integer limit);

    /**
     * 查询会话的最新消息
     *
     * @param conversationId 会话ID
     * @return 最新消息
     */
    ChatMessage selectLatestMessage(@Param("conversationId") Long conversationId);

    /**
     * 查询用户在会话中的未读消息数量
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadTime 最后已读时间
     * @return 未读消息数量
     */
    Integer countUnreadMessages(@Param("conversationId") Long conversationId,
                               @Param("userId") Long userId,
                               @Param("lastReadTime") LocalDateTime lastReadTime);

    /**
     * 搜索会话中的消息
     *
     * @param conversationId 会话ID
     * @param keyword 关键词
     * @param messageType 消息类型(可选)
     * @param senderId 发送者ID(可选)
     * @param limit 限制数量
     * @return 消息列表
     */
    List<ChatMessage> searchMessages(@Param("conversationId") Long conversationId,
                                    @Param("keyword") String keyword,
                                    @Param("messageType") Integer messageType,
                                    @Param("senderId") Long senderId,
                                    @Param("limit") Integer limit);

    /**
     * 查询指定时间范围内的消息
     *
     * @param conversationId 会话ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<ChatMessage> selectMessagesByTimeRange(@Param("conversationId") Long conversationId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新消息状态
     *
     * @param messageIds 消息ID列表
     * @param newStatus 新状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("messageIds") List<Long> messageIds,
                         @Param("newStatus") Integer newStatus);

    /**
     * 删除会话的所有消息
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    int deleteConversationMessages(@Param("conversationId") Long conversationId);

    /**
     * 查询可以撤回的消息(5分钟内)
     *
     * @param senderId 发送者ID
     * @param minutes 分钟数
     * @return 消息列表
     */
    List<ChatMessage> selectRecallableMessages(@Param("senderId") Long senderId,
                                              @Param("minutes") Integer minutes);

    /**
     * 查询会话消息统计
     *
     * @param conversationId 会话ID
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @return 统计信息
     */
    Map<String, Object> selectMessageStats(@Param("conversationId") Long conversationId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户发送的消息统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> selectUserMessageStats(@Param("userId") Long userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热门表情使用统计
     *
     * @param limit 限制数量
     * @param days 统计天数
     * @return 表情统计列表
     */
    List<Map<String, Object>> selectPopularEmojis(@Param("limit") Integer limit,
                                                 @Param("days") Integer days);

    /**
     * 查询需要清理的过期消息
     *
     * @param days 保存天数
     * @param batchSize 批次大小
     * @return 过期消息ID列表
     */
    List<Long> selectExpiredMessages(@Param("days") Integer days,
                                    @Param("batchSize") Integer batchSize);

    /**
     * 查询引用了指定消息的回复消息
     *
     * @param replyToId 被回复消息ID
     * @return 回复消息列表
     */
    List<ChatMessage> selectReplyMessages(@Param("replyToId") Long replyToId);

    /**
     * 查询会话中的媒体消息
     *
     * @param conversationId 会话ID
     * @param messageType 消息类型(图片、视频、文件等)
     * @param limit 限制数量
     * @return 媒体消息列表
     */
    List<ChatMessage> selectMediaMessages(@Param("conversationId") Long conversationId,
                                         @Param("messageType") Integer messageType,
                                         @Param("limit") Integer limit);

    /**
     * 更新消息的已读状态(群聊使用)
     *
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param readTime 已读时间
     * @return 影响行数
     */
    int updateMessageReadStatus(@Param("messageId") Long messageId,
                               @Param("userId") Long userId,
                               @Param("readTime") LocalDateTime readTime);

    /**
     * 查询消息的已读用户列表
     *
     * @param messageId 消息ID
     * @return 已读用户信息列表
     */
    List<Map<String, Object>> selectMessageReadUsers(@Param("messageId") Long messageId);

    // ========== v7.1新增方法 ==========
    
    /**
     * 更新会话最后消息冗余字段（v7.1性能优化）
     *
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @param messageTime 消息时间
     * @return 影响行数
     */
    int updateConversationLastMessage(@Param("conversationId") Long conversationId,
                                     @Param("messageId") Long messageId,
                                     @Param("messageTime") LocalDateTime messageTime);
}


