package com.xypai.chat.service;

import com.xypai.chat.domain.dto.ConversationCreateDTO;
import com.xypai.chat.domain.dto.ConversationQueryDTO;
import com.xypai.chat.domain.dto.ParticipantOperationDTO;
import com.xypai.chat.domain.vo.ConversationDetailVO;
import com.xypai.chat.domain.vo.ConversationListVO;

import java.util.List;
import java.util.Map;

/**
 * 聊天会话服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IChatConversationService {

    /**
     * 创建会话
     *
     * @param createDTO 创建数据
     * @return 会话ID
     */
    Long createConversation(ConversationCreateDTO createDTO);

    /**
     * 查询会话列表
     *
     * @param queryDTO 查询条件
     * @return 会话列表
     */
    List<ConversationListVO> selectConversationList(ConversationQueryDTO queryDTO);

    /**
     * 根据ID查询会话详情
     *
     * @param conversationId 会话ID
     * @return 会话详情
     */
    ConversationDetailVO selectConversationById(Long conversationId);

    /**
     * 更新会话信息
     *
     * @param conversationId 会话ID
     * @param title 会话标题
     * @param description 会话描述
     * @param avatar 会话头像
     * @return 是否成功
     */
    boolean updateConversationInfo(Long conversationId, String title, String description, String avatar);

    /**
     * 删除/解散会话
     *
     * @param conversationId 会话ID
     * @param reason 删除原因
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, String reason);

    /**
     * 归档会话
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    boolean archiveConversation(Long conversationId);

    /**
     * 恢复归档会话
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    boolean unarchiveConversation(Long conversationId);

    /**
     * 添加参与者
     *
     * @param operationDTO 操作数据
     * @return 是否成功
     */
    boolean addParticipants(ParticipantOperationDTO operationDTO);

    /**
     * 移除参与者
     *
     * @param operationDTO 操作数据
     * @return 是否成功
     */
    boolean removeParticipants(ParticipantOperationDTO operationDTO);

    /**
     * 退出会话
     *
     * @param conversationId 会话ID
     * @param reason 退出原因
     * @return 是否成功
     */
    boolean quitConversation(Long conversationId, String reason);

    /**
     * 转让群主
     *
     * @param conversationId 会话ID
     * @param newOwnerId 新群主ID
     * @return 是否成功
     */
    boolean transferOwnership(Long conversationId, Long newOwnerId);

    /**
     * 设置管理员
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isAdmin 是否设为管理员
     * @return 是否成功
     */
    boolean setAdmin(Long conversationId, Long userId, Boolean isAdmin);

    /**
     * 禁言/解禁用户
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted 是否禁言
     * @return 是否成功
     */
    boolean muteUser(Long conversationId, Long userId, Boolean isMuted);

    /**
     * 创建私聊会话
     *
     * @param targetUserId 目标用户ID
     * @return 会话ID
     */
    Long createPrivateConversation(Long targetUserId);

    /**
     * 获取或创建私聊会话
     *
     * @param targetUserId 目标用户ID
     * @return 会话ID
     */
    Long getOrCreatePrivateConversation(Long targetUserId);

    /**
     * 创建订单会话
     *
     * @param orderId 订单ID
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @return 会话ID
     */
    Long createOrderConversation(Long orderId, Long buyerId, Long sellerId);

    /**
     * 创建系统通知会话
     *
     * @param userId 用户ID
     * @return 会话ID
     */
    Long createSystemNotificationConversation(Long userId);

    /**
     * 查询我的会话列表
     *
     * @param type 会话类型(可选)
     * @param includeArchived 是否包含归档会话
     * @return 会话列表
     */
    List<ConversationListVO> selectMyConversations(Integer type, Boolean includeArchived);

    /**
     * 搜索会话
     *
     * @param keyword 关键词
     * @param type 会话类型(可选)
     * @param limit 限制数量
     * @return 会话列表
     */
    List<ConversationListVO> searchConversations(String keyword, Integer type, Integer limit);

    /**
     * 查询用户会话统计
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @return 统计信息
     */
    Map<String, Object> getUserConversationStats(Long userId);

    /**
     * 查询热门群聊
     *
     * @param limit 限制数量
     * @param days 统计天数
     * @return 热门群聊列表
     */
    List<Map<String, Object>> getPopularGroupChats(Integer limit, Integer days);

    /**
     * 自动归档长时间未活跃的会话
     *
     * @param inactiveDays 无活跃天数
     * @return 归档数量
     */
    int autoArchiveInactiveConversations(Integer inactiveDays);

    /**
     * 批量删除会话
     *
     * @param conversationIds 会话ID列表
     * @param reason 删除原因
     * @return 删除数量
     */
    int batchDeleteConversations(List<Long> conversationIds, String reason);

    /**
     * 检查用户权限
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    boolean checkUserPermission(Long conversationId, Long userId, String permission);

    /**
     * 验证会话访问权限
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否有访问权限
     */
    boolean validateAccessPermission(Long conversationId, Long userId);

    /**
     * 更新会话最后活跃时间
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    boolean updateLastActiveTime(Long conversationId);

    /**
     * 设置会话置顶
     *
     * @param conversationId 会话ID
     * @param isPinned 是否置顶
     * @return 是否成功
     */
    boolean pinConversation(Long conversationId, Boolean isPinned);

    /**
     * 设置会话静音
     *
     * @param conversationId 会话ID
     * @param isMuted 是否静音
     * @return 是否成功
     */
    boolean muteConversation(Long conversationId, Boolean isMuted);

    /**
     * 获取会话参与者列表
     *
     * @param conversationId 会话ID
     * @param includeLeft 是否包含已退出的参与者
     * @return 参与者列表
     */
    List<ConversationDetailVO.ParticipantVO> getConversationParticipants(Long conversationId, Boolean includeLeft);

    /**
     * 检查会话是否存在
     *
     * @param conversationId 会话ID
     * @return 是否存在
     */
    boolean existsConversation(Long conversationId);

    /**
     * 检查是否为会话参与者
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否为参与者
     */
    boolean isParticipant(Long conversationId, Long userId);
}

