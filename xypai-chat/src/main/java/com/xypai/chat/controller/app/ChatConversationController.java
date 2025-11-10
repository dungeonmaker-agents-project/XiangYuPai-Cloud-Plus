package com.xypai.chat.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.chat.domain.dto.ConversationCreateDTO;
import com.xypai.chat.domain.dto.ConversationQueryDTO;
import com.xypai.chat.domain.dto.ParticipantOperationDTO;
import com.xypai.chat.domain.vo.ConversationDetailVO;
import com.xypai.chat.domain.vo.ConversationListVO;
import com.xypai.chat.service.IChatConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天会话控制�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "聊天会话", description = "聊天会话管理API")
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Validated
public class ChatConversationController extends BaseController {

    private final IChatConversationService conversationService;

    /**
     * 查询会话列表
     */
    @Operation(summary = "查询会话列表", description = "分页查询会话列表信息")
    @GetMapping("/list")
    @SaCheckPermission("chat:conversation:list")
    public TableDataInfo<ConversationListVO> list(ConversationQueryDTO query) {
        List<ConversationListVO> list = conversationService.selectConversationList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取会话详细信息
     */
    @Operation(summary = "获取会话详细信息", description = "根据会话ID获取详细信息")
    @GetMapping("/{conversationId}")
    @SaCheckPermission("chat:conversation:query")
    @Log(title = "会话管理", businessType = BusinessType.OTHER)
    public R<ConversationDetailVO> getInfo(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return R.ok(conversationService.selectConversationById(conversationId));
    }

    /**
     * 创建会话
     */
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    @PostMapping
    @SaCheckPermission("chat:conversation:add")
    @Log(title = "会话管理", businessType = BusinessType.INSERT)
    public R<Long> add(@Validated @RequestBody ConversationCreateDTO conversationCreateDTO) {
        return R.ok("会话创建成功", conversationService.createConversation(conversationCreateDTO));
    }

    /**
     * 修改会话
     */
    @Operation(summary = "修改会话", description = "更新会话信息")
    @PutMapping
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "会话管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@PathVariable Long conversationId, @Validated @RequestBody Map<String, Object> updateData) {
        String title = (String) updateData.get("title");
        String description = (String) updateData.get("description");
        String avatar = (String) updateData.get("avatar");
        boolean success = conversationService.updateConversationInfo(conversationId, title, description, avatar);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话", description = "删除指定会话")
    @DeleteMapping("/{conversationId}")
    @SaCheckPermission("chat:conversation:remove")
    @Log(title = "会话管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        boolean success = conversationService.deleteConversation(conversationId, "用户删除");
        return success ? R.ok() : R.fail("删除失败");
    }

    /**
     * 创建私聊会话
     */
    @Operation(summary = "创建私聊会话", description = "与指定用户创建私聊会话")
    @PostMapping("/private/{targetUserId}")
    @SaCheckPermission("chat:conversation:add")
    @Log(title = "创建私聊", businessType = BusinessType.INSERT)
    public R<Long> createPrivateConversation(
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long targetUserId) {
        return R.ok("私聊会话创建成功", conversationService.createPrivateConversation(targetUserId));
    }

    /**
     * 创建群聊会话
     */
    @Operation(summary = "创建群聊会话", description = "创建群聊会话")
    @PostMapping("/group")
    @SaCheckPermission("chat:conversation:add")
    @Log(title = "创建群聊", businessType = BusinessType.INSERT)
    public R<Long> createGroupConversation(
            @Parameter(description = "群聊名称", required = true)
            @RequestParam String title,
            @Parameter(description = "群聊描述")
            @RequestParam(required = false) String description,
            @Parameter(description = "初始成员ID列表")
            @RequestBody(required = false) List<Long> memberIds) {
        ConversationCreateDTO createDTO = ConversationCreateDTO.builder()
                .type(2) // 群聊类型
                .title(title)
                .description(description)
                .participantIds(memberIds)
                .build();
        return R.ok("群聊会话创建成功", conversationService.createConversation(createDTO));
    }

    /**
     * 创建订单会话
     */
    @Operation(summary = "创建订单会话", description = "为订单创建专用会话")
    @PostMapping("/order/{orderId}")
    @SaCheckPermission("chat:conversation:add")
    @Log(title = "创建订单会话", businessType = BusinessType.INSERT)
    public R<Long> createOrderConversation(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        // TODO: 需要买家ID和卖家ID参数
        return R.ok("订单会话功能需要完善", 0L);
    }

    /**
     * 获取我的会话列表
     */
    @Operation(summary = "获取我的会话列表", description = "获取当前用户的会话列表")
    @GetMapping("/my")
    @SaCheckPermission("chat:conversation:query")
    public TableDataInfo<ConversationListVO> getMyConversations(
            @Parameter(description = "会话类型")
            @RequestParam(required = false) Integer type) {
        List<ConversationListVO> list = conversationService.selectMyConversations(type, false);
        return TableDataInfo.build(list);
    }

    /**
     * 邀请用户加入会话
     */
    @Operation(summary = "邀请用户加入会话", description = "邀请用户加入群聊会话")
    @PostMapping("/{conversationId}/invite")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "邀请用户", businessType = BusinessType.UPDATE)
    public R<Void> inviteUsers(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        ParticipantOperationDTO operationDTO = ParticipantOperationDTO.builder()
                .conversationId(conversationId)
                .userIds(userIds)
                .operationType("add")
                .build();
        boolean success = conversationService.addParticipants(operationDTO);
        return success ? R.ok() : R.fail("邀请失败");
    }

    /**
     * 移除会话成员
     */
    @Operation(summary = "移除会话成员", description = "从会话中移除指定成员")
    @DeleteMapping("/{conversationId}/members/{userId}")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "移除成员", businessType = BusinessType.DELETE)
    public R<Void> removeMember(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        ParticipantOperationDTO operationDTO = ParticipantOperationDTO.builder()
                .conversationId(conversationId)
                .userIds(List.of(userId))
                .operationType("remove")
                .build();
        boolean success = conversationService.removeParticipants(operationDTO);
        return success ? R.ok() : R.fail("移除失败");
    }

    /**
     * 退出会话
     */
    @Operation(summary = "退出会话", description = "当前用户退出会话")
    @PostMapping("/{conversationId}/leave")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "退出会话", businessType = BusinessType.UPDATE)
    public R<Void> leaveConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        boolean success = conversationService.quitConversation(conversationId, "用户退出");
        return success ? R.ok() : R.fail("退出失败");
    }

    /**
     * 设置会话管理员
     */
    @Operation(summary = "设置会话管理员", description = "设置群聊管理员")
    @PutMapping("/{conversationId}/admin/{userId}")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "设置管理员", businessType = BusinessType.UPDATE)
    public R<Void> setAdmin(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "是否设为管理员", required = true)
            @RequestParam Boolean isAdmin) {
        boolean success = conversationService.setAdmin(conversationId, userId, isAdmin);
        return success ? R.ok() : R.fail("操作失败");
    }

    /**
     * 禁言用户
     */
    @Operation(summary = "禁言用户", description = "禁言会话中的用户")
    @PutMapping("/{conversationId}/mute/{userId}")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "禁言用户", businessType = BusinessType.UPDATE)
    public R<Void> muteUser(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "是否禁言", required = true)
            @RequestParam Boolean muted) {
        boolean success = conversationService.muteUser(conversationId, userId, muted);
        return success ? R.ok() : R.fail("操作失败");
    }

    /**
     * 归档会话
     */
    @Operation(summary = "归档会话", description = "归档指定会话")
    @PutMapping("/{conversationId}/archive")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "归档会话", businessType = BusinessType.UPDATE)
    public R<Void> archiveConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        boolean success = conversationService.archiveConversation(conversationId);
        return success ? R.ok() : R.fail("归档失败");
    }

    /**
     * 恢复会话
     */
    @Operation(summary = "恢复会话", description = "恢复已归档的会话")
    @PutMapping("/{conversationId}/restore")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "恢复会话", businessType = BusinessType.UPDATE)
    public R<Void> restoreConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        boolean success = conversationService.unarchiveConversation(conversationId);
        return success ? R.ok() : R.fail("恢复失败");
    }

    /**
     * 获取会话成员列表
     */
    @Operation(summary = "获取会话成员列表", description = "获取指定会话的成员列表")
    @GetMapping("/{conversationId}/members")
    @SaCheckPermission("chat:conversation:query")
    public TableDataInfo<Map<String, Object>> getConversationMembers(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        // TODO: 实现成员列表转换逻辑
        List<Map<String, Object>> list = new ArrayList<>();
        return TableDataInfo.build(list);
    }

    /**
     * 获取会话统计信息
     */
    @Operation(summary = "获取会话统计信息", description = "获取会话的统计数据")
    @GetMapping("/{conversationId}/statistics")
    @SaCheckPermission("chat:conversation:query")
    public R<Map<String, Object>> getConversationStatistics(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        Map<String, Object> stats = conversationService.getUserConversationStats(null);
        return R.ok(stats);
    }

    // ========== v7.1新增API ==========
    
    /**
     * 置顶会话
     */
    @Operation(summary = "置顶会话", description = "置顶或取消置顶会话")
    @PutMapping("/{conversationId}/pin")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "置顶会话", businessType = BusinessType.UPDATE)
    public R<Void> pinConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "是否置顶", required = true)
            @RequestParam Boolean isPinned) {
        boolean success = conversationService.pinConversation(conversationId, isPinned);
        return success ? R.ok() : R.fail("操作失败");
    }

    /**
     * 免打扰设置
     */
    @Operation(summary = "免打扰设置", description = "设置会话免打扰")
    @PutMapping("/{conversationId}/mute")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "免打扰设置", businessType = BusinessType.UPDATE)
    public R<Void> muteConversation(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "是否免打扰", required = true)
            @RequestParam Boolean isMuted,
            @Parameter(description = "免打扰截止时间")
            @RequestParam(required = false) String muteUntil) {
        boolean success = conversationService.muteConversation(conversationId, isMuted);
        return success ? R.ok() : R.fail("操作失败");
    }

    /**
     * 标记会话已读
     */
    @Operation(summary = "标记会话已读", description = "标记会话中的所有消息为已读")
    @PutMapping("/{conversationId}/read")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "标记会话已读", businessType = BusinessType.UPDATE)
    public R<Void> markConversationAsRead(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        // 调用消息服务的标记已读方�?
        return R.ok();
    }

    /**
     * 转让群主
     */
    @Operation(summary = "转让群主", description = "转让群主身份给其他成员")
    @PutMapping("/{conversationId}/transfer-owner/{newOwnerId}")
    @SaCheckPermission("chat:conversation:edit")
    @Log(title = "转让群主", businessType = BusinessType.UPDATE)
    public R<Void> transferOwnership(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "新群主ID", required = true)
            @PathVariable Long newOwnerId) {
        boolean success = conversationService.transferOwnership(conversationId, newOwnerId);
        return success ? R.ok() : R.fail("转让失败");
    }

    /**
     * 搜索会话
     */
    @Operation(summary = "搜索会话", description = "搜索会话")
    @GetMapping("/search")
    @SaCheckPermission("chat:conversation:query")
    public TableDataInfo<ConversationListVO> searchConversations(
            @Parameter(description = "搜索关键字", required = true)
            @RequestParam String keyword,
            @Parameter(description = "会话类型")
            @RequestParam(required = false) Integer type) {
        List<ConversationListVO> list = conversationService.searchConversations(keyword, type, 20);
        return TableDataInfo.build(list);
    }
}
