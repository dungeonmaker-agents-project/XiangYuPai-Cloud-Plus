package com.xypai.chat.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.chat.domain.dto.MessageSendDTO;
import com.xypai.chat.domain.dto.MessageQueryDTO;
import com.xypai.chat.domain.vo.MessageVO;
import com.xypai.chat.service.IChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "聊天消息", description = "聊天消息管理API")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Validated
public class ChatMessageController extends BaseController {

    private final IChatMessageService messageService;

    /**
     * 查询消息列表
     */
    @Operation(summary = "查询消息列表", description = "分页查询消息列表信息")
    @GetMapping("/list")
    @SaCheckPermission("chat:message:list")
    public TableDataInfo<MessageVO> list(MessageQueryDTO query) {
        List<MessageVO> list = messageService.selectMessageList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取消息详细信息
     */
    @Operation(summary = "获取消息详细信息", description = "根据消息ID获取详细信息")
    @GetMapping("/{messageId}")
    @SaCheckPermission("chat:message:query")
    @Log(title = "消息管理", businessType = BusinessType.OTHER)
    public R<MessageVO> getInfo(
            @Parameter(description = "消息ID", required = true)
            @PathVariable Long messageId) {
        return R.ok(messageService.selectMessageById(messageId));
    }

    /**
     * 发送文本消息
     */
    @Operation(summary = "发送文本消息", description = "发送文本类型消息")
    @PostMapping("/text")
    @SaCheckPermission("chat:message:add")
    @Log(title = "发送消息", businessType = BusinessType.INSERT)
    public R<Long> sendTextMessage(@Validated @RequestBody MessageSendDTO messageSendDTO) {
        return R.ok("消息发送成功", messageService.sendTextMessage(messageSendDTO.getConversationId(), messageSendDTO.getContent(), messageSendDTO.getReplyToId()));
    }

    /**
     * 发送图片消息
     */
    @Operation(summary = "发送图片消息", description = "发送图片类型消息")
    @PostMapping("/image")
    @SaCheckPermission("chat:message:add")
    @Log(title = "发送图片", businessType = BusinessType.INSERT)
    public R<Long> sendImageMessage(
            @Parameter(description = "会话ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "图片文件", required = true)
            @RequestParam MultipartFile image,
            @Parameter(description = "回复消息ID")
            @RequestParam(required = false) Long replyToId) {
        // TODO: 实现文件上传逻辑，获取图片URL
        String imageUrl = "temp_image_url.jpg"; // 临时URL，需要实现文件上传
        return R.ok("图片发送成功", messageService.sendImageMessage(conversationId, imageUrl, null, null, null));
    }

    /**
     * 发送语音消息
     */
    @Operation(summary = "发送语音消息", description = "发送语音类型消息")
    @PostMapping("/voice")
    @SaCheckPermission("chat:message:add")
    @Log(title = "发送语音", businessType = BusinessType.INSERT)
    public R<Long> sendVoiceMessage(
            @Parameter(description = "会话ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "语音文件", required = true)
            @RequestParam MultipartFile voice,
            @Parameter(description = "语音时长(秒)")
            @RequestParam(required = false) Integer duration,
            @Parameter(description = "回复消息ID")
            @RequestParam(required = false) Long replyToId) {
        // TODO: 实现文件上传逻辑，获取语音URL
        String voiceUrl = "temp_voice_url.mp3"; // 临时URL，需要实现文件上传
        return R.ok("语音发送成功", messageService.sendVoiceMessage(conversationId, voiceUrl, duration, 0L));
    }

    /**
     * 发送文件消息
     */
    @Operation(summary = "发送文件消息", description = "发送文件类型消息")
    @PostMapping("/file")
    @SaCheckPermission("chat:message:add")
    @Log(title = "发送文件", businessType = BusinessType.INSERT)
    public R<Long> sendFileMessage(
            @Parameter(description = "会话ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "文件", required = true)
            @RequestParam MultipartFile file,
            @Parameter(description = "回复消息ID")
            @RequestParam(required = false) Long replyToId) {
        // TODO: 实现文件上传逻辑，获取文件URL
        String fileUrl = "temp_file_url.doc"; // 临时URL，需要实现文件上传
        String fileName = file.getOriginalFilename();
        return R.ok("文件发送成功", messageService.sendFileMessage(conversationId, fileUrl, fileName, file.getSize()));
    }

    /**
     * 撤回消息
     */
    @Operation(summary = "撤回消息", description = "撤回指定消息")
    @PutMapping("/{messageId}/recall")
    @SaCheckPermission("chat:message:edit")
    @Log(title = "撤回消息", businessType = BusinessType.UPDATE)
    public R<Void> recallMessage(
            @Parameter(description = "消息ID", required = true)
            @PathVariable Long messageId) {
        boolean success = messageService.recallMessage(messageId, "用户撤回");
        return success ? R.ok() : R.fail("撤回失败");
    }

    /**
     * 删除消息
     */
    @Operation(summary = "删除消息", description = "删除指定消息")
    @DeleteMapping("/{messageId}")
    @SaCheckPermission("chat:message:remove")
    @Log(title = "删除消息", businessType = BusinessType.DELETE)
    public R<Void> deleteMessage(
            @Parameter(description = "消息ID", required = true)
            @PathVariable Long messageId) {
        boolean success = messageService.deleteMessage(messageId, false);
        return success ? R.ok() : R.fail("删除失败");
    }

    /**
     * 获取会话消息
     */
    @Operation(summary = "获取会话消息", description = "获取指定会话的消息列表")
    @GetMapping("/conversation/{conversationId}")
    @SaCheckPermission("chat:message:query")
    public TableDataInfo<MessageVO> getConversationMessages(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "最后消息ID(分页)")
            @RequestParam(required = false) Long lastMessageId) {
        List<MessageVO> list = messageService.selectConversationMessages(conversationId, lastMessageId, "before", 20);
        return TableDataInfo.build(list);
    }

    /**
     * 标记消息已读
     */
    @Operation(summary = "标记消息已读", description = "标记指定会话的消息为已读")
    @PutMapping("/conversation/{conversationId}/read")
    @SaCheckPermission("chat:message:edit")
    @Log(title = "标记已读", businessType = BusinessType.UPDATE)
    public R<Void> markMessagesAsRead(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "最后已读消息ID")
            @RequestParam(required = false) Long lastReadMessageId) {
        boolean success = messageService.markMessageAsRead(conversationId, lastReadMessageId);
        return success ? R.ok() : R.fail("标记失败");
    }

    /**
     * 搜索消息
     */
    @Operation(summary = "搜索消息", description = "在指定会话中搜索消息")
    @GetMapping("/search")
    @SaCheckPermission("chat:message:query")
    public TableDataInfo<MessageVO> searchMessages(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "会话ID")
            @RequestParam(required = false) Long conversationId,
            @Parameter(description = "消息类型")
            @RequestParam(required = false) Integer messageType) {
        List<MessageVO> list = messageService.searchMessages(conversationId, keyword, messageType, null, 20);
        return TableDataInfo.build(list);
    }

    /**
     * 获取未读消息数量
     */
    @Operation(summary = "获取未读消息数量", description = "获取当前用户的未读消息数量")
    @GetMapping("/unread-count")
    @SaCheckPermission("chat:message:query")
    public R<Map<String, Object>> getUnreadCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", 0); // TODO: 实现获取用户总未读消息数
        return R.ok(result);
    }

    /**
     * 获取会话未读消息数量
     */
    @Operation(summary = "获取会话未读消息数量", description = "获取指定会话的未读消息数量")
    @GetMapping("/conversation/{conversationId}/unread-count")
    @SaCheckPermission("chat:message:query")
    public R<Integer> getConversationUnreadCount(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId) {
        return R.ok(messageService.countUnreadMessages(conversationId, null));
    }

    /**
     * 转发消息
     */
    @Operation(summary = "转发消息", description = "转发消息到其他会话")
    @PostMapping("/{messageId}/forward")
    @SaCheckPermission("chat:message:add")
    @Log(title = "转发消息", businessType = BusinessType.INSERT)
    public R<Integer> forwardMessage(
            @Parameter(description = "消息ID", required = true)
            @PathVariable Long messageId,
            @Parameter(description = "目标会话ID列表", required = true)
            @RequestBody List<Long> targetConversationIds) {
        return R.ok("消息转发成功", messageService.forwardMessage(messageId, targetConversationIds));
    }

    /**
     * 获取消息统计
     */
    @Operation(summary = "获取消息统计", description = "获取消息统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("chat:message:query")
    public R<Map<String, Object>> getMessageStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(messageService.getMessageStats(null, null, null, null));
    }

    /**
     * 导出聊天记录
     */
    @Operation(summary = "导出聊天记录", description = "导出指定会话的聊天记录")
    @PostMapping("/conversation/{conversationId}/export")
    @SaCheckPermission("chat:message:export")
    @Log(title = "导出聊天记录", businessType = BusinessType.EXPORT)
    public R<String> exportChatHistory(
            @Parameter(description = "会话ID", required = true)
            @PathVariable Long conversationId,
            @Parameter(description = "导出开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "导出结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok("导出功能暂未实现", "TODO: 实现导出聊天记录功能");
    }

    /**
     * 发送系统消息
     */
    @Operation(summary = "发送系统消息", description = "管理员发送系统消息")
    @PostMapping("/system")
    @SaCheckPermission("chat:message:admin")
    @Log(title = "发送系统消息", businessType = BusinessType.INSERT)
    public R<Long> sendSystemMessage(
            @Parameter(description = "会话ID", required = true)
            @RequestParam Long conversationId,
            @Parameter(description = "消息内容", required = true)
            @RequestParam String content,
            @Parameter(description = "消息类型")
            @RequestParam(defaultValue = "6") Integer messageType) {
        return R.ok("系统消息发送成功", messageService.sendSystemMessage(conversationId, content));
    }

    /**
     * 批量删除消息
     */
    @Operation(summary = "批量删除消息", description = "批量删除指定消息")
    @DeleteMapping("/batch")
    @SaCheckPermission("chat:message:remove")
    @Log(title = "批量删除消息", businessType = BusinessType.DELETE)
    public R<Void> batchDeleteMessages(
            @Parameter(description = "消息ID列表", required = true)
            @RequestBody List<Long> messageIds) {
        int count = messageService.batchDeleteMessages(messageIds, false);
        return count > 0 ? R.ok() : R.fail("删除失败");
    }
}
