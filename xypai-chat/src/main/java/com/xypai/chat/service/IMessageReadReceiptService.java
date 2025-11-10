package com.xypai.chat.service;

import com.xypai.chat.domain.vo.MessageVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息已读回执服务接口 (v7.1新增)
 * 
 * 功能：
 * - 记录消息已读状态
 * - 查询已读用户列表
 * - 更新投递状态
 * - 推送已读回执
 * 
 * @author xypai
 * @date 2025-01-14
 */
public interface IMessageReadReceiptService {

    /**
     * 记录消息已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param readTime 已读时间
     * @return 是否成功
     */
    boolean recordMessageRead(Long messageId, Long userId, LocalDateTime readTime);

    /**
     * 批量记录消息已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @param readTime 已读时间
     * @return 成功记录的数量
     */
    int batchRecordMessageRead(List<Long> messageIds, Long userId, LocalDateTime readTime);

    /**
     * 查询消息已读用户列表
     * 
     * @param messageId 消息ID
     * @return 已读状态列表
     */
    List<MessageVO.ReadStatusVO> getMessageReadStatus(Long messageId);

    /**
     * 查询消息已读人数
     * 
     * @param messageId 消息ID
     * @return 已读人数
     */
    int countMessageReadUsers(Long messageId);

    /**
     * 更新消息投递状态
     * 
     * @param messageId 消息ID
     * @param deliveryStatus 投递状态
     * @return 是否成功
     */
    boolean updateDeliveryStatus(Long messageId, Integer deliveryStatus);

    /**
     * 推送已读回执给发送者
     * 
     * @param messageId 消息ID
     * @param readUserId 已读用户ID
     * @return 是否成功
     */
    boolean pushReadReceiptToSender(Long messageId, Long readUserId);

    /**
     * 检查是否所有成员已读（群聊）
     * 
     * @param messageId 消息ID
     * @param conversationId 会话ID
     * @return 是否所有人已读
     */
    boolean isAllMembersRead(Long messageId, Long conversationId);
}

