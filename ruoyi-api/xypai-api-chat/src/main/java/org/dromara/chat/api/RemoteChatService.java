package org.dromara.chat.api;

import org.dromara.chat.api.domain.vo.RemoteChatConversationVo;
import org.dromara.chat.api.domain.vo.RemoteChatMessageVo;
import org.dromara.chat.api.domain.vo.RemoteChatUnreadCountVo;
import org.dromara.common.core.exception.ServiceException;

import java.util.List;

/**
 * Chat Remote Service Interface
 * 聊天远程服务接口
 *
 * <p>用途：其他服务通过Dubbo调用此接口获取聊天数据</p>
 * <p>实现：xypai-chat模块实现此接口</p>
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
public interface RemoteChatService {

    // ==================== Unread Count ====================

    /**
     * Get user's unread message count
     * 获取用户未读消息数
     *
     * @param userId User ID
     * @return Unread count VO
     * @throws ServiceException Service exception
     */
    RemoteChatUnreadCountVo getUserUnreadCount(Long userId) throws ServiceException;

    // ==================== Conversations ====================

    /**
     * Get user's conversation list
     * 获取用户会话列表
     *
     * @param userId User ID
     * @param limit  Max number of conversations to return
     * @return List of conversation VOs
     * @throws ServiceException Service exception
     */
    List<RemoteChatConversationVo> getUserConversations(Long userId, Integer limit) throws ServiceException;

    /**
     * Get conversation by ID
     * 根据ID获取会话信息
     *
     * @param userId         User ID
     * @param conversationId Conversation ID
     * @return Conversation VO
     * @throws ServiceException Service exception (e.g., conversation not found or no permission)
     */
    RemoteChatConversationVo getConversationById(Long userId, Long conversationId) throws ServiceException;

    // ==================== Messages ====================

    /**
     * Get conversation messages
     * 获取会话消息列表
     *
     * @param userId         User ID
     * @param conversationId Conversation ID
     * @param limit          Max number of messages to return
     * @return List of message VOs
     * @throws ServiceException Service exception
     */
    List<RemoteChatMessageVo> getConversationMessages(Long userId, Long conversationId, Integer limit) throws ServiceException;

    /**
     * Get message by ID
     * 根据ID获取消息
     *
     * @param userId    User ID
     * @param messageId Message ID
     * @return Message VO
     * @throws ServiceException Service exception (e.g., message not found or no permission)
     */
    RemoteChatMessageVo getMessageById(Long userId, Long messageId) throws ServiceException;

    // ==================== Online Status ====================

    /**
     * Check if user is online
     * 检查用户是否在线
     *
     * @param userId User ID
     * @return true if online, false otherwise
     */
    boolean isUserOnline(Long userId);

    /**
     * Batch check users' online status
     * 批量检查用户在线状态
     *
     * @param userIds List of user IDs
     * @return Map of userId to online status (true/false)
     */
    java.util.Map<Long, Boolean> batchCheckOnlineStatus(List<Long> userIds);
}
