package com.xypai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.chat.domain.entity.ChatParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会话参与者Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ChatParticipantMapper extends BaseMapper<ChatParticipant> {

    /**
     * 查询会话的参与者列表
     *
     * @param conversationId 会话ID
     * @param status 参与状态(可选)
     * @return 参与者列表
     */
    List<ChatParticipant> selectConversationParticipants(@Param("conversationId") Long conversationId,
                                                         @Param("status") Integer status);

    /**
     * 查询用户参与的会话列表
     *
     * @param userId 用户ID
     * @param status 参与状态(可选)
     * @return 参与记录列表
     */
    List<ChatParticipant> selectUserParticipations(@Param("userId") Long userId,
                                                   @Param("status") Integer status);

    /**
     * 查询用户在指定会话中的参与记录
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 参与记录
     */
    ChatParticipant selectParticipant(@Param("conversationId") Long conversationId,
                                     @Param("userId") Long userId);

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
     * 统计会话的参与者数量
     *
     * @param conversationId 会话ID
     * @param status 参与状态(可选)
     * @return 参与者数量
     */
    Integer countParticipants(@Param("conversationId") Long conversationId,
                             @Param("status") Integer status);

    /**
     * 批量添加参与者
     *
     * @param participants 参与者列表
     * @return 影响行数
     */
    int batchInsertParticipants(@Param("participants") List<ChatParticipant> participants);

    /**
     * 批量更新参与者状态
     *
     * @param conversationId 会话ID
     * @param userIds 用户ID列表
     * @param newStatus 新状态
     * @return 影响行数
     */
    int batchUpdateParticipantStatus(@Param("conversationId") Long conversationId,
                                    @Param("userIds") List<Long> userIds,
                                    @Param("newStatus") Integer newStatus);

    /**
     * 更新参与者最后已读时间
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param lastReadTime 最后已读时间
     * @return 影响行数
     */
    int updateLastReadTime(@Param("conversationId") Long conversationId,
                          @Param("userId") Long userId,
                          @Param("lastReadTime") LocalDateTime lastReadTime);

    /**
     * 查询会话的管理员列表
     *
     * @param conversationId 会话ID
     * @return 管理员列表
     */
    List<ChatParticipant> selectAdmins(@Param("conversationId") Long conversationId);

    /**
     * 查询会话的群主
     *
     * @param conversationId 会话ID
     * @return 群主信息
     */
    ChatParticipant selectOwner(@Param("conversationId") Long conversationId);

    /**
     * 转让群主
     *
     * @param conversationId 会话ID
     * @param oldOwnerId 原群主ID
     * @param newOwnerId 新群主ID
     * @return 是否成功
     */
    boolean transferOwnership(@Param("conversationId") Long conversationId,
                             @Param("oldOwnerId") Long oldOwnerId,
                             @Param("newOwnerId") Long newOwnerId);

    /**
     * 查询用户的未读消息会话列表
     *
     * @param userId 用户ID
     * @return 有未读消息的会话列表
     */
    List<Map<String, Object>> selectUnreadConversations(@Param("userId") Long userId);

    /**
     * 查询长时间未读的参与者
     *
     * @param conversationId 会话ID
     * @param days 天数
     * @return 参与者列表
     */
    List<ChatParticipant> selectInactiveParticipants(@Param("conversationId") Long conversationId,
                                                     @Param("days") Integer days);

    /**
     * 删除会话的所有参与者记录
     *
     * @param conversationId 会话ID
     * @return 影响行数
     */
    int deleteConversationParticipants(@Param("conversationId") Long conversationId);

    /**
     * 查询参与者权限信息
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 权限信息
     */
    Map<String, Object> selectParticipantPermissions(@Param("conversationId") Long conversationId,
                                                     @Param("userId") Long userId);

    /**
     * 统计用户参与的会话数量
     *
     * @param userId 用户ID
     * @param type 会话类型(可选)
     * @return 会话数量统计
     */
    Map<String, Object> selectUserConversationCount(@Param("userId") Long userId,
                                                    @Param("type") Integer type);

    /**
     * 查询最活跃的参与者
     *
     * @param conversationId 会话ID
     * @param days 统计天数
     * @param limit 限制数量
     * @return 活跃参与者列表
     */
    List<Map<String, Object>> selectActiveParticipants(@Param("conversationId") Long conversationId,
                                                       @Param("days") Integer days,
                                                       @Param("limit") Integer limit);

    // ========== v7.1新增方法 ==========
    
    /**
     * 增加除指定用户外的所有参与者未读数量（v7.1未读计数优化）
     *
     * @param conversationId 会话ID
     * @param excludeUserId 排除的用户ID（通常是发送者）
     * @return 影响行数
     */
    int incrementUnreadCountExcept(@Param("conversationId") Long conversationId,
                                  @Param("excludeUserId") Long excludeUserId);

    /**
     * 更新参与者已读位置（v7.1精确已读定位）
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param messageId 最后已读消息ID
     * @param readTime 已读时间
     * @return 影响行数
     */
    int updateReadPosition(@Param("conversationId") Long conversationId,
                          @Param("userId") Long userId,
                          @Param("messageId") Long messageId,
                          @Param("readTime") LocalDateTime readTime);

    /**
     * 批量更新参与者的置顶状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isPinned 是否置顶
     * @return 影响行数
     */
    int updatePinnedStatus(@Param("conversationId") Long conversationId,
                          @Param("userId") Long userId,
                          @Param("isPinned") Boolean isPinned);

    /**
     * 批量更新参与者的免打扰状态
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否免打扰
     * @param muteUntil 免打扰截止时间
     * @return 影响行数
     */
    int updateMutedStatus(@Param("conversationId") Long conversationId,
                         @Param("userId") Long userId,
                         @Param("isMuted") Boolean isMuted,
                         @Param("muteUntil") LocalDateTime muteUntil);
}


