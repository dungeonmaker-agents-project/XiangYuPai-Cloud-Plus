package org.dromara.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.chat.domain.dto.ChatHistoryQueryDTO;
import org.dromara.chat.domain.dto.ConversationQueryDTO;
import org.dromara.chat.domain.dto.MessageSendDTO;
import org.dromara.chat.domain.vo.ConversationVO;
import org.dromara.chat.domain.vo.MessageVO;
import org.dromara.chat.domain.vo.UnreadCountVO;

/**
 * Message Service Interface
 * 消息服务接口
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
public interface IMessageService {

    /**
     * 获取未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数统计
     */
    UnreadCountVO getUnreadCount(Long userId);

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 会话列表分页
     */
    Page<ConversationVO> getConversations(Long userId, ConversationQueryDTO queryDTO);

    /**
     * 删除会话
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 清除所有消息
     *
     * @param userId 用户ID
     */
    void clearAllMessages(Long userId);

    /**
     * 获取聊天记录
     *
     * @param userId 用户ID
     * @param queryDTO 查询参数
     * @return 消息列表分页
     */
    Page<MessageVO> getChatHistory(Long userId, ChatHistoryQueryDTO queryDTO);

    /**
     * 发送消息
     *
     * @param userId 发送者ID
     * @param sendDTO 发送消息DTO
     * @return 发送的消息
     */
    MessageVO sendMessage(Long userId, MessageSendDTO sendDTO);

    /**
     * 标记消息已读
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     * @return 标记为已读的消息数
     */
    Integer markMessagesAsRead(Long userId, Long conversationId);

    /**
     * 撤回消息
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void recallMessage(Long userId, Long messageId);

    /**
     * 删除消息
     *
     * @param userId 用户ID
     * @param messageId 消息ID
     */
    void deleteMessage(Long userId, Long messageId);
}
