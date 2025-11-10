package com.xypai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.chat.domain.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天会话Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    /**
     * 查询用户的会话列表
     *
     * @param userId 用户ID
     * @param type 会话类型(可选)
     * @param status 会话状态(可选)
     * @param limit 限制数量
     * @return 会话列表
     */
    List<ChatConversation> selectUserConversations(@Param("userId") Long userId, 
                                                   @Param("type") Integer type, 
                                                   @Param("status") Integer status, 
                                                   @Param("limit") Integer limit);

    /**
     * 查询用户创建的会话
     *
     * @param creatorId 创建者ID
     * @param type 会话类型(可选)
     * @param status 会话状态(可选)
     * @return 会话列表
     */
    List<ChatConversation> selectCreatedConversations(@Param("creatorId") Long creatorId, 
                                                      @Param("type") Integer type, 
                                                      @Param("status") Integer status);

    /**
     * 查询两个用户之间的私聊会话
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 私聊会话
     */
    ChatConversation selectPrivateConversation(@Param("userId1") Long userId1, 
                                              @Param("userId2") Long userId2);

    /**
     * 根据订单ID查询订单会话
     *
     * @param orderId 订单ID
     * @return 订单会话
     */
    ChatConversation selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询会话的参与者数量
     *
     * @param conversationId 会话ID
     * @return 参与者数量
     */
    Integer countParticipants(@Param("conversationId") Long conversationId);

    /**
     * 检查用户是否为会话参与者
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否为参与者
     */
    boolean isParticipant(@Param("conversationId") Long conversationId, 
                         @Param("userId") Long userId);

    /**
     * 更新会话最后活跃时间
     *
     * @param conversationId 会话ID
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    int updateLastActiveTime(@Param("conversationId") Long conversationId, 
                            @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 批量更新会话状态
     *
     * @param conversationIds 会话ID列表
     * @param newStatus 新状态
     * @param updatedAt 更新时间
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("conversationIds") List<Long> conversationIds, 
                         @Param("newStatus") Integer newStatus, 
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 查询长时间未活跃的会话
     *
     * @param days 天数
     * @param status 会话状态
     * @return 会话列表
     */
    List<ChatConversation> selectInactiveConversations(@Param("days") Integer days, 
                                                       @Param("status") Integer status);

    /**
     * 查询用户会话统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> selectUserConversationStats(@Param("userId") Long userId);

    /**
     * 查询热门群聊
     *
     * @param limit 限制数量
     * @param days 统计天数
     * @return 热门群聊列表
     */
    List<Map<String, Object>> selectPopularGroupChats(@Param("limit") Integer limit, 
                                                      @Param("days") Integer days);

    /**
     * 搜索会话
     *
     * @param keyword 关键词
     * @param userId 用户ID(限制用户可见的会话)
     * @param type 会话类型(可选)
     * @param limit 限制数量
     * @return 会话列表
     */
    List<ChatConversation> searchConversations(@Param("keyword") String keyword, 
                                              @Param("userId") Long userId, 
                                              @Param("type") Integer type, 
                                              @Param("limit") Integer limit);

    /**
     * 查询需要自动归档的会话
     *
     * @param days 无活跃天数
     * @param status 当前状态
     * @return 会话列表
     */
    List<ChatConversation> selectConversationsForAutoArchive(@Param("days") Integer days, 
                                                             @Param("status") Integer status);

    /**
     * 查询系统通知会话
     *
     * @param userId 用户ID
     * @return 系统通知会话
     */
    ChatConversation selectSystemNotificationConversation(@Param("userId") Long userId);
}

