package com.xypai.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xypai.chat.domain.dto.ConversationCreateDTO;
import com.xypai.chat.domain.dto.ConversationQueryDTO;
import com.xypai.chat.domain.dto.ParticipantOperationDTO;
import com.xypai.chat.domain.entity.ChatConversation;
import com.xypai.chat.domain.entity.ChatMessage;
import com.xypai.chat.domain.entity.ChatParticipant;
import com.xypai.chat.domain.vo.ConversationDetailVO;
import com.xypai.chat.domain.vo.ConversationListVO;
import com.xypai.chat.mapper.ChatConversationMapper;
import com.xypai.chat.mapper.ChatMessageMapper;
import com.xypai.chat.mapper.ChatParticipantMapper;
import com.xypai.chat.service.IChatConversationService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天会话服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConversationServiceImpl implements IChatConversationService {

    private final ChatConversationMapper chatConversationMapper;
    private final ChatParticipantMapper chatParticipantMapper;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(ConversationCreateDTO createDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 验证会话类型
        if (createDTO.getType() == null) {
            throw new ServiceException("会话类型不能为空");
        }

        // 构建扩展信息
        Map<String, Object> metadata = buildConversationMetadata(createDTO);

        // 创建会话
        ChatConversation conversation = ChatConversation.builder()
                .type(createDTO.getType())
                .title(createDTO.getTitle())
                .creatorId(currentUserId)
                .metadata(metadata)
                .status(ChatConversation.Status.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = chatConversationMapper.insert(conversation);
        if (result <= 0) {
            throw new ServiceException("创建会话失败");
        }

        // 添加创建者为参与�?
        ChatParticipant creatorParticipant = ChatParticipant.builder()
                .conversationId(conversation.getId())
                .userId(currentUserId)
                .role(ChatParticipant.Role.OWNER.getCode()) // 创建者为群主
                .joinTime(LocalDateTime.now())
                .lastReadTime(LocalDateTime.now())
                .status(ChatParticipant.Status.NORMAL.getCode())
                .build();

        chatParticipantMapper.insert(creatorParticipant);

        // 添加其他参与�?
        if (createDTO.getParticipantIds() != null && !createDTO.getParticipantIds().isEmpty()) {
            List<ChatParticipant> participants = createDTO.getParticipantIds().stream()
                    .filter(userId -> !userId.equals(currentUserId)) // 排除创建�?
                    .map(userId -> ChatParticipant.builder()
                            .conversationId(conversation.getId())
                            .userId(userId)
                            .role(ChatParticipant.Role.MEMBER.getCode())
                            .joinTime(LocalDateTime.now())
                            .status(ChatParticipant.Status.NORMAL.getCode())
                            .build())
                    .collect(Collectors.toList());

            if (!participants.isEmpty()) {
                chatParticipantMapper.batchInsertParticipants(participants);
            }
        }

        log.info("创建会话成功，会话ID：{}，类型：{}，创建者：{}", 
                conversation.getId(), createDTO.getType(), currentUserId);
        
        return conversation.getId();
    }

    @Override
    public List<ConversationListVO> selectConversationList(ConversationQueryDTO queryDTO) {
        LambdaQueryWrapper<ChatConversation> queryWrapper = buildQueryWrapper(queryDTO);
        List<ChatConversation> conversations = chatConversationMapper.selectList(queryWrapper);
        return convertToListVOs(conversations, queryDTO.getIncludeLatestMessage(), queryDTO.getIncludeUnreadCount());
    }

    @Override
    public ConversationDetailVO selectConversationById(Long conversationId) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        ChatConversation conversation = chatConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !validateAccessPermission(conversationId, currentUserId)) {
            throw new ServiceException("无权限访问该会话");
        }

        return convertToDetailVO(conversation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConversationInfo(Long conversationId, String title, String description, String avatar) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        ChatConversation conversation = chatConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        // 权限验证 - 只有群主和管理员可以修改
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !checkUserPermission(conversationId, currentUserId, "modify")) {
            throw new ServiceException("无权限修改会话信息");
        }

        // 更新会话信息
        Map<String, Object> metadata = conversation.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        if (StringUtils.isNotBlank(description)) {
            metadata.put("description", description);
        }
        if (StringUtils.isNotBlank(avatar)) {
            metadata.put("avatar", avatar);
        }

        ChatConversation updateConversation = ChatConversation.builder()
                .id(conversationId)
                .title(title)
                .metadata(metadata)
                .updatedAt(LocalDateTime.now())
                .build();

        int result = chatConversationMapper.updateById(updateConversation);
        
        log.info("更新会话信息成功，会话ID：{}", conversationId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(Long conversationId, String reason) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        ChatConversation conversation = chatConversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        // 权限验证 - 只有创建者可以删�?解散
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(conversation.getCreatorId())) {
            throw new ServiceException("只有创建者可以解散会话");
        }

        // 更新会话状态为已解�?
        ChatConversation updateConversation = ChatConversation.builder()
                .id(conversationId)
                .status(ChatConversation.Status.DISSOLVED.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = chatConversationMapper.updateById(updateConversation);
        if (result > 0) {
            // 更新所有参与者状态为已退�?
            List<ChatParticipant> participants = chatParticipantMapper.selectConversationParticipants(
                    conversationId, ChatParticipant.Status.NORMAL.getCode());
            if (!participants.isEmpty()) {
                List<Long> userIds = participants.stream()
                        .map(ChatParticipant::getUserId)
                        .collect(Collectors.toList());
                chatParticipantMapper.batchUpdateParticipantStatus(
                        conversationId, userIds, ChatParticipant.Status.LEFT.getCode());
            }

            log.info("解散会话成功，会话ID：{}，原因：{}", conversationId, reason);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveConversation(Long conversationId) {
        return updateConversationStatus(conversationId, ChatConversation.Status.ARCHIVED.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unarchiveConversation(Long conversationId) {
        return updateConversationStatus(conversationId, ChatConversation.Status.NORMAL.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addParticipants(ParticipantOperationDTO operationDTO) {
        if (operationDTO.getConversationId() == null) {
            throw new ServiceException("会话ID不能为空");
        }
        if (operationDTO.getUserIds() == null || operationDTO.getUserIds().isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        ChatConversation conversation = chatConversationMapper.selectById(operationDTO.getConversationId());
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !checkUserPermission(operationDTO.getConversationId(), currentUserId, "invite")) {
            throw new ServiceException("无权限邀请成员");
        }

        // 检查最大成员数限制
        Integer maxMembers = conversation.getMaxMembers();
        if (maxMembers != null) {
            Integer currentCount = chatParticipantMapper.countParticipants(
                    operationDTO.getConversationId(), ChatParticipant.Status.NORMAL.getCode());
            if (currentCount + operationDTO.getUserIds().size() > maxMembers) {
                throw new ServiceException("超出最大成员数限制");
            }
        }

        // 添加参与�?
        List<ChatParticipant> participants = operationDTO.getUserIds().stream()
                .filter(userId -> !chatParticipantMapper.isParticipant(operationDTO.getConversationId(), userId))
                .map(userId -> ChatParticipant.builder()
                        .conversationId(operationDTO.getConversationId())
                        .userId(userId)
                        .role(ChatParticipant.Role.MEMBER.getCode())
                        .joinTime(LocalDateTime.now())
                        .status(ChatParticipant.Status.NORMAL.getCode())
                        .build())
                .collect(Collectors.toList());

        if (!participants.isEmpty()) {
            int result = chatParticipantMapper.batchInsertParticipants(participants);
            log.info("添加参与者成功，会话ID：{}，添加数量：{}", operationDTO.getConversationId(), result);
            return result > 0;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeParticipants(ParticipantOperationDTO operationDTO) {
        if (operationDTO.getConversationId() == null) {
            throw new ServiceException("会话ID不能为空");
        }
        if (operationDTO.getUserIds() == null || operationDTO.getUserIds().isEmpty()) {
            throw new ServiceException("用户ID列表不能为空");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !checkUserPermission(operationDTO.getConversationId(), currentUserId, "remove")) {
            throw new ServiceException("无权限移除成员");
        }

        // 不能移除群主
        ChatParticipant owner = chatParticipantMapper.selectOwner(operationDTO.getConversationId());
        if (owner != null && operationDTO.getUserIds().contains(owner.getUserId())) {
            throw new ServiceException("不能移除群主");
        }

        // 移除参与�?
        int result = chatParticipantMapper.batchUpdateParticipantStatus(
                operationDTO.getConversationId(), operationDTO.getUserIds(), ChatParticipant.Status.LEFT.getCode());

        log.info("移除参与者成功，会话ID：{}，移除数量：{}", operationDTO.getConversationId(), result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitConversation(Long conversationId, String reason) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        ChatParticipant participant = chatParticipantMapper.selectParticipant(conversationId, currentUserId);
        if (participant == null) {
            throw new ServiceException("您不是该会话的参与者");
        }

        // 群主不能直接退出，需要先转让群主
        if (participant.isOwner()) {
            throw new ServiceException("群主不能直接退出，请先转让群主身份");
        }

        // 更新参与者状�?
        List<Long> userIds = Collections.singletonList(currentUserId);
        int result = chatParticipantMapper.batchUpdateParticipantStatus(
                conversationId, userIds, ChatParticipant.Status.LEFT.getCode());

        log.info("退出会话成功，会话ID：{}，用户ID：{}，原因：{}", conversationId, currentUserId, reason);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferOwnership(Long conversationId, Long newOwnerId) {
        if (conversationId == null || newOwnerId == null) {
            throw new ServiceException("会话ID和新群主ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 验证当前用户是否为群�?
        ChatParticipant currentOwner = chatParticipantMapper.selectOwner(conversationId);
        if (currentOwner == null || !currentOwner.getUserId().equals(currentUserId)) {
            throw new ServiceException("只有群主可以转让群主身份");
        }

        // 验证新群主是否为会话成员
        ChatParticipant newOwner = chatParticipantMapper.selectParticipant(conversationId, newOwnerId);
        if (newOwner == null || !newOwner.isNormal()) {
            throw new ServiceException("新群主必须是正常状态的会话成员");
        }

        // 执行转让
        boolean result = chatParticipantMapper.transferOwnership(conversationId, currentUserId, newOwnerId);

        log.info("转让群主成功，会话ID：{}，原群主：{}，新群主：{}", conversationId, currentUserId, newOwnerId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setAdmin(Long conversationId, Long userId, Boolean isAdmin) {
        if (conversationId == null || userId == null) {
            throw new ServiceException("会话ID和用户ID不能为空");
        }

        // 权限验证 - 只有群主可以设置管理员
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !checkUserPermission(conversationId, currentUserId, "set_admin")) {
            throw new ServiceException("只有群主可以设置管理员");
        }

        ChatParticipant participant = chatParticipantMapper.selectParticipant(conversationId, userId);
        if (participant == null) {
            throw new ServiceException("用户不是该会话的参与者");
        }

        // 不能设置群主为管理员
        if (participant.isOwner()) {
            throw new ServiceException("不能设置群主为管理员");
        }

        Integer newRole = Boolean.TRUE.equals(isAdmin) ? 
                         ChatParticipant.Role.ADMIN.getCode() : ChatParticipant.Role.MEMBER.getCode();

        ChatParticipant updateParticipant = ChatParticipant.builder()
                .id(participant.getId())
                .role(newRole)
                .build();

        int result = chatParticipantMapper.updateById(updateParticipant);

        log.info("设置管理员成功，会话ID：{}，用户ID：{}，是否管理员：{}", conversationId, userId, isAdmin);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean muteUser(Long conversationId, Long userId, Boolean isMuted) {
        if (conversationId == null || userId == null) {
            throw new ServiceException("会话ID和用户ID不能为空");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !checkUserPermission(conversationId, currentUserId, "mute")) {
            throw new ServiceException("无权限禁言用户");
        }

        ChatParticipant participant = chatParticipantMapper.selectParticipant(conversationId, userId);
        if (participant == null) {
            throw new ServiceException("用户不是该会话的参与者");
        }

        Integer newStatus = Boolean.TRUE.equals(isMuted) ? 
                           ChatParticipant.Status.MUTED.getCode() : ChatParticipant.Status.NORMAL.getCode();

        List<Long> userIds = Collections.singletonList(userId);
        int result = chatParticipantMapper.batchUpdateParticipantStatus(conversationId, userIds, newStatus);

        log.info("设置用户禁言成功，会话ID：{}，用户ID：{}，是否禁言：{}", conversationId, userId, isMuted);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrivateConversation(Long targetUserId) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        if (currentUserId.equals(targetUserId)) {
            throw new ServiceException("不能与自己创建私聊");
        }

        // 检查是否已存在私聊
        ChatConversation existConversation = chatConversationMapper.selectPrivateConversation(currentUserId, targetUserId);
        if (existConversation != null) {
            return existConversation.getId();
        }

        // 创建私聊会话
        ConversationCreateDTO createDTO = ConversationCreateDTO.builder()
                .type(ChatConversation.Type.PRIVATE.getCode())
                .participantIds(Collections.singletonList(targetUserId))
                .build();

        return createConversation(createDTO);
    }

    @Override
    public Long getOrCreatePrivateConversation(Long targetUserId) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 查找已存在的私聊
        ChatConversation conversation = chatConversationMapper.selectPrivateConversation(currentUserId, targetUserId);
        if (conversation != null) {
            return conversation.getId();
        }

        // 创建新的私聊
        return createPrivateConversation(targetUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderConversation(Long orderId, Long buyerId, Long sellerId) {
        if (orderId == null || buyerId == null || sellerId == null) {
            throw new ServiceException("订单ID、买家ID、卖家ID不能为空");
        }

        // 检查是否已存在订单会话
        ChatConversation existConversation = chatConversationMapper.selectByOrderId(orderId);
        if (existConversation != null) {
            return existConversation.getId();
        }

        // 创建订单会话
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("order_id", orderId);

        ChatConversation conversation = ChatConversation.builder()
                .type(ChatConversation.Type.ORDER.getCode())
                .title("订单会话 #" + orderId)
                .creatorId(buyerId) // 买家为创建�?
                .metadata(metadata)
                .status(ChatConversation.Status.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        chatConversationMapper.insert(conversation);

        // 添加买家和卖家为参与�?
        List<ChatParticipant> participants = Arrays.asList(
                ChatParticipant.builder()
                        .conversationId(conversation.getId())
                        .userId(buyerId)
                        .role(ChatParticipant.Role.MEMBER.getCode())
                        .joinTime(LocalDateTime.now())
                        .status(ChatParticipant.Status.NORMAL.getCode())
                        .build(),
                ChatParticipant.builder()
                        .conversationId(conversation.getId())
                        .userId(sellerId)
                        .role(ChatParticipant.Role.MEMBER.getCode())
                        .joinTime(LocalDateTime.now())
                        .status(ChatParticipant.Status.NORMAL.getCode())
                        .build()
        );

        chatParticipantMapper.batchInsertParticipants(participants);

        log.info("创建订单会话成功，订单ID：{}，会话ID：{}", orderId, conversation.getId());
        return conversation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSystemNotificationConversation(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // 检查是否已存在系统通知会话
        ChatConversation existConversation = chatConversationMapper.selectSystemNotificationConversation(userId);
        if (existConversation != null) {
            return existConversation.getId();
        }

        // 创建系统通知会话
        ChatConversation conversation = ChatConversation.builder()
                .type(ChatConversation.Type.SYSTEM.getCode())
                .title("系统通知")
                .creatorId(null) // 系统会话无创建�?
                .status(ChatConversation.Status.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        chatConversationMapper.insert(conversation);

        // 添加用户为参与�?
        ChatParticipant participant = ChatParticipant.builder()
                .conversationId(conversation.getId())
                .userId(userId)
                .role(ChatParticipant.Role.MEMBER.getCode())
                .joinTime(LocalDateTime.now())
                .status(ChatParticipant.Status.NORMAL.getCode())
                .build();

        chatParticipantMapper.insert(participant);

        log.info("创建系统通知会话成功，用户ID：{}，会话ID：{}", userId, conversation.getId());
        return conversation.getId();
    }

    @Override
    public List<ConversationListVO> selectMyConversations(Integer type, Boolean includeArchived) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        List<ChatConversation> conversations = chatConversationMapper.selectUserConversations(
                currentUserId, type, includeArchived ? null : ChatConversation.Status.NORMAL.getCode(), null);
        return convertToListVOs(conversations, true, true);
    }

    @Override
    public List<ConversationListVO> searchConversations(String keyword, Integer type, Integer limit) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        List<ChatConversation> conversations = chatConversationMapper.searchConversations(
                keyword, currentUserId, type, limit != null ? limit : 20);
        return convertToListVOs(conversations, false, false);
    }

    @Override
    public Map<String, Object> getUserConversationStats(Long userId) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        return chatConversationMapper.selectUserConversationStats(targetUserId);
    }

    @Override
    public List<Map<String, Object>> getPopularGroupChats(Integer limit, Integer days) {
        return chatConversationMapper.selectPopularGroupChats(
                limit != null ? limit : 10, days != null ? days : 7);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoArchiveInactiveConversations(Integer inactiveDays) {
        List<ChatConversation> conversations = chatConversationMapper.selectConversationsForAutoArchive(
                inactiveDays != null ? inactiveDays : 30, ChatConversation.Status.NORMAL.getCode());

        if (conversations.isEmpty()) {
            return 0;
        }

        List<Long> conversationIds = conversations.stream()
                .map(ChatConversation::getId)
                .collect(Collectors.toList());

        return chatConversationMapper.batchUpdateStatus(
                conversationIds, ChatConversation.Status.ARCHIVED.getCode(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteConversations(List<Long> conversationIds, String reason) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return 0;
        }

        return chatConversationMapper.batchUpdateStatus(
                conversationIds, ChatConversation.Status.DISSOLVED.getCode(), LocalDateTime.now());
    }

    @Override
    public boolean checkUserPermission(Long conversationId, Long userId, String permission) {
        if (conversationId == null || userId == null || StringUtils.isBlank(permission)) {
            return false;
        }

        ChatParticipant participant = chatParticipantMapper.selectParticipant(conversationId, userId);
        if (participant == null || !participant.isNormal()) {
            return false;
        }

        switch (permission.toLowerCase()) {
            case "send_message":
                return participant.canSpeak();
            case "invite":
                return participant.canInvite();
            case "remove":
            case "mute":
                return participant.canKickMember();
            case "modify":
                return participant.hasAdminPermission();
            case "set_admin":
                return participant.isOwner();
            default:
                return false;
        }
    }

    @Override
    public boolean validateAccessPermission(Long conversationId, Long userId) {
        return isParticipant(conversationId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastActiveTime(Long conversationId) {
        if (conversationId == null) {
            return false;
        }

        return chatConversationMapper.updateLastActiveTime(conversationId, LocalDateTime.now()) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pinConversation(Long conversationId, Boolean isPinned) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // v7.1: 更新参与者的置顶状�?
        int result = chatParticipantMapper.updatePinnedStatus(conversationId, currentUserId, isPinned);
        
        if (result > 0) {
            log.info("设置会话置顶成功，会话ID：{}，用户ID：{}，是否置顶：{}", 
                    conversationId, currentUserId, isPinned);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean muteConversation(Long conversationId, Boolean isMuted) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // v7.1: 更新参与者的免打扰状态（永久免打扰）
        int result = chatParticipantMapper.updateMutedStatus(
                conversationId, currentUserId, isMuted, null);
        
        if (result > 0) {
            log.info("设置会话免打扰成功，会话ID：{}，用户ID：{}，是否免打扰：{}", 
                    conversationId, currentUserId, isMuted);
        }

        return result > 0;
    }

    /**
     * v7.1新增：设置定时免打扰
     */
    public boolean muteConversationUntil(Long conversationId, LocalDateTime muteUntil) {
        if (conversationId == null || muteUntil == null) {
            throw new ServiceException("会话ID和免打扰截止时间不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        int result = chatParticipantMapper.updateMutedStatus(
                conversationId, currentUserId, true, muteUntil);
        
        if (result > 0) {
            log.info("设置定时免打扰成功，会话ID：{}，用户ID：{}，截止时间：{}", 
                    conversationId, currentUserId, muteUntil);
        }

        return result > 0;
    }

    @Override
    public List<ConversationDetailVO.ParticipantVO> getConversationParticipants(Long conversationId, Boolean includeLeft) {
        if (conversationId == null) {
            throw new ServiceException("会话ID不能为空");
        }

        Integer status = includeLeft ? null : ChatParticipant.Status.NORMAL.getCode();
        List<ChatParticipant> participants = chatParticipantMapper.selectConversationParticipants(conversationId, status);
        
        return participants.stream()
                .map(this::convertToParticipantVO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsConversation(Long conversationId) {
        if (conversationId == null) {
            return false;
        }

        ChatConversation conversation = chatConversationMapper.selectById(conversationId);
        return conversation != null && conversation.isNormal();
    }

    @Override
    public boolean isParticipant(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return false;
        }

        return chatParticipantMapper.isParticipant(conversationId, userId);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ChatConversation> buildQueryWrapper(ConversationQueryDTO query) {
        return Wrappers.lambdaQuery(ChatConversation.class)
                .eq(query.getType() != null, ChatConversation::getType, query.getType())
                .eq(query.getCreatorId() != null, ChatConversation::getCreatorId, query.getCreatorId())
                .eq(query.getStatus() != null, ChatConversation::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getTitle()), ChatConversation::getTitle, query.getTitle())
                .ge(query.getParams() != null && query.getParams().get("beginTime") != null,
                        ChatConversation::getCreatedAt, query.getParams().get("beginTime"))
                .le(query.getParams() != null && query.getParams().get("endTime") != null,
                        ChatConversation::getCreatedAt, query.getParams().get("endTime"))
                .orderByDesc(ChatConversation::getUpdatedAt);
    }

    /**
     * 构建会话扩展信息
     */
    private Map<String, Object> buildConversationMetadata(ConversationCreateDTO dto) {
        Map<String, Object> metadata = new HashMap<>();
        
        if (StringUtils.isNotBlank(dto.getDescription())) {
            metadata.put("description", dto.getDescription());
        }
        if (StringUtils.isNotBlank(dto.getAvatar())) {
            metadata.put("avatar", dto.getAvatar());
        }
        if (dto.getMaxMembers() != null) {
            metadata.put("max_members", dto.getMaxMembers());
        }
        if (dto.getInviteEnabled() != null) {
            metadata.put("invite_enabled", dto.getInviteEnabled());
        }
        if (dto.getOrderId() != null) {
            metadata.put("order_id", dto.getOrderId());
        }
        if (dto.getExtraData() != null) {
            metadata.putAll(dto.getExtraData());
        }
        
        return metadata;
    }

    /**
     * 更新会话状�?
     */
    private boolean updateConversationStatus(Long conversationId, Integer status) {
        if (conversationId == null || status == null) {
            return false;
        }

        ChatConversation updateConversation = ChatConversation.builder()
                .id(conversationId)
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();

        return chatConversationMapper.updateById(updateConversation) > 0;
    }

    /**
     * 转换为列表VO
     */
    private List<ConversationListVO> convertToListVOs(List<ChatConversation> conversations, 
                                                     Boolean includeLatestMessage, Boolean includeUnreadCount) {
        if (conversations == null || conversations.isEmpty()) {
            return new ArrayList<>();
        }

        return conversations.stream()
                .map(conversation -> convertToListVO(conversation, includeLatestMessage, includeUnreadCount))
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表VO（v7.1完整实现�?
     */
    private ConversationListVO convertToListVO(ChatConversation conversation, 
                                              Boolean includeLatestMessage, Boolean includeUnreadCount) {
        Long currentUserId = LoginHelper.getUserId();
        
        ConversationListVO.ConversationListVOBuilder builder = ConversationListVO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .typeDesc(conversation.getTypeDesc())
                .title(conversation.getTitle())
                .description(conversation.getDescription())
                .avatar(conversation.getAvatarUrl() != null ? conversation.getAvatarUrl() : conversation.getAvatar())
                .creatorId(conversation.getCreatorId())
                .status(conversation.getStatus())
                .statusDesc(conversation.getStatusDesc())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                // v7.1新增：冗余字段（性能优化�?
                .orderId(conversation.getOrderId())
                .lastMessageId(conversation.getLastMessageId())
                .lastMessageTime(conversation.getLastMessageTime())
                .totalMessageCount(conversation.getTotalMessageCount())
                .participantCount(conversation.getMemberCount());

        // v7.1: 查询当前用户的个性化设置（置�?免打�?未读数）
        if (currentUserId != null) {
            ChatParticipant currentParticipant = chatParticipantMapper.selectParticipant(
                    conversation.getId(), currentUserId);
            
            if (currentParticipant != null) {
                builder.isPinned(currentParticipant.getIsPinned())
                       .isMuted(currentParticipant.getIsMuted())
                       .muteUntil(currentParticipant.getMuteUntil())
                       .userRole(currentParticipant.getRoleDesc())
                       .groupNickname(currentParticipant.getNickname())
                       .unreadCount(currentParticipant.getUnreadCount());
            }
        }

        // v7.1优化：如果需要最新消息，优先从冗余字段获�?
        if (Boolean.TRUE.equals(includeLatestMessage) && conversation.getLastMessageId() != null) {
            try {
                ChatMessage lastMessage = chatMessageMapper.selectById(conversation.getLastMessageId());
                if (lastMessage != null) {
                    ConversationListVO.LatestMessageVO latestMessageVO = ConversationListVO.LatestMessageVO.builder()
                            .messageId(lastMessage.getId())
                            .senderId(lastMessage.getSenderId())
                            .messageType(lastMessage.getMessageType())
                            .messageTypeDesc(lastMessage.getMessageTypeDesc())
                            .content(lastMessage.getContent())
                            .preview(generateMessagePreview(lastMessage.getMessageType(), 
                                    lastMessage.getContent(), lastMessage.getMediaData()))
                            .createdAt(lastMessage.getCreatedAt())
                            .isSystem(lastMessage.isSystem())
                            .build();
                    builder.latestMessage(latestMessageVO);
                }
            } catch (Exception e) {
                log.error("查询最新消息失败，会话ID：{}", conversation.getId(), e);
            }
        }

        return builder.build();
    }

    /**
     * 生成消息预览（v7.1�?
     */
    private String generateMessagePreview(Integer messageType, String content, Map<String, Object> mediaData) {
        if (messageType == null) {
            return content != null && content.length() > 30 ? 
                   content.substring(0, 30) + "..." : content;
        }

        switch (messageType) {
            case 1: // 文本
                return content != null && content.length() > 30 ? 
                       content.substring(0, 30) + "..." : content;
            case 2: // 图片
                return "[图片]";
            case 3: // 语音
                Integer duration = mediaData != null ? (Integer) mediaData.get("duration") : null;
                return duration != null ? String.format("[语音] %d\"", duration) : "[语音]";
            case 4: // 视频
                return "[视频]";
            case 5: // 文件
                return "[文件]";
            case 6: // 系统通知
                return content;
            case 7: // 表情
                return "[表情]";
            case 8: // 位置
                return "[位置]";
            case 9: // 订单卡片
                return "[订单卡片]";
            default:
                return "[未知消息]";
        }
    }

    /**
     * 转换为详情VO
     */
    private ConversationDetailVO convertToDetailVO(ChatConversation conversation) {
        return ConversationDetailVO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .typeDesc(conversation.getTypeDesc())
                .title(conversation.getTitle())
                .description(conversation.getDescription())
                .avatar(conversation.getAvatar())
                .creatorId(conversation.getCreatorId())
                .status(conversation.getStatus())
                .statusDesc(conversation.getStatusDesc())
                .maxMembers(conversation.getMaxMembers())
                .inviteEnabled(conversation.isInviteEnabled())
                .participants(getConversationParticipants(conversation.getId(), false))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .version(conversation.getVersion())
                .metadata(conversation.getMetadata())
                .build();
    }

    /**
     * 转换为参与者VO
     */
    private ConversationDetailVO.ParticipantVO convertToParticipantVO(ChatParticipant participant) {
        return ConversationDetailVO.ParticipantVO.builder()
                .participantId(participant.getId())
                .userId(participant.getUserId())
                .role(participant.getRoleDesc())
                .roleDesc(participant.getRoleDesc())
                .joinedAt(participant.getJoinTime())
                .lastReadAt(participant.getLastReadTime())
                .isMuted(participant.isMuted())
                .build();
    }
}

