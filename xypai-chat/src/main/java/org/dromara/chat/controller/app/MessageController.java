package org.dromara.chat.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.chat.domain.dto.ChatHistoryQueryDTO;
import org.dromara.chat.domain.dto.ConversationQueryDTO;
import org.dromara.chat.domain.dto.MessageSendDTO;
import org.dromara.chat.domain.vo.ConversationVO;
import org.dromara.chat.domain.vo.MessageVO;
import org.dromara.chat.domain.vo.UnreadCountVO;
import org.dromara.chat.service.IFileUploadService;
import org.dromara.chat.service.IMessageService;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Message Management Controller
 * 消息管理控制器
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "Message Management", description = "消息管理API")
public class MessageController extends BaseController {

    private final IMessageService messageService;
    private final IFileUploadService fileUploadService;

    /**
     * Get unread message count
     * 获取未读消息数
     *
     * @return Unread count VO
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get Unread Count", description = "获取未读消息数（聊天+点赞+评论+粉丝+系统通知）")
    public R<UnreadCountVO> getUnreadCount() {
        Long userId = LoginHelper.getUserId();
        UnreadCountVO result = messageService.getUnreadCount(userId);
        return R.ok(result);
    }

    /**
     * Get conversation list
     * 获取会话列表
     *
     * @param queryDTO Query parameters
     * @return Paginated conversation list
     */
    @GetMapping("/conversations")
    @Operation(summary = "Get Conversation List", description = "获取私信会话列表（分页）")
    public R<Page<ConversationVO>> getConversations(@Valid ConversationQueryDTO queryDTO) {
        Long userId = LoginHelper.getUserId();
        Page<ConversationVO> result = messageService.getConversations(userId, queryDTO);
        return R.ok(result);
    }

    /**
     * Delete conversation
     * 删除会话
     *
     * @param conversationId Conversation ID
     * @return Success response
     */
    @DeleteMapping("/conversation/{conversationId}")
    @Operation(summary = "Delete Conversation", description = "删除指定会话（软删除）")
    @Parameter(name = "conversationId", description = "会话ID", required = true)
    public R<Void> deleteConversation(@PathVariable("conversationId") Long conversationId) {
        Long userId = LoginHelper.getUserId();
        messageService.deleteConversation(userId, conversationId);
        return R.ok();
    }

    /**
     * Clear all messages
     * 清除所有消息
     *
     * @return Success response
     */
    @PostMapping("/clear-all")
    @Operation(summary = "Clear All Messages", description = "清除所有消息（删除所有会话）")
    public R<Void> clearAllMessages() {
        Long userId = LoginHelper.getUserId();
        messageService.clearAllMessages(userId);
        return R.ok();
    }

    /**
     * Get chat history
     * 获取聊天记录
     *
     * @param conversationId Conversation ID
     * @param queryDTO Query parameters
     * @return Paginated message list
     */
    @GetMapping("/chat/{conversationId}")
    @Operation(summary = "Get Chat History", description = "获取指定会话的聊天记录（分页）")
    @Parameter(name = "conversationId", description = "会话ID", required = true)
    public R<Page<MessageVO>> getChatHistory(
            @PathVariable("conversationId") Long conversationId,
            @Valid ChatHistoryQueryDTO queryDTO) {

        Long userId = LoginHelper.getUserId();

        // Set conversation ID from path variable
        queryDTO.setConversationId(conversationId);

        Page<MessageVO> result = messageService.getChatHistory(userId, queryDTO);
        return R.ok(result);
    }

    /**
     * Send message
     * 发送消息
     *
     * @param sendDTO Message send request
     * @return Sent message
     */
    @PostMapping("/send")
    @Operation(summary = "Send Message", description = "发送消息（文字/图片/语音/视频）")
    public R<MessageVO> sendMessage(@Valid @RequestBody MessageSendDTO sendDTO) {
        Long userId = LoginHelper.getUserId();
        MessageVO result = messageService.sendMessage(userId, sendDTO);
        return R.ok(result);
    }

    /**
     * Mark messages as read
     * 标记消息已读
     *
     * @param conversationId Conversation ID
     * @return Number of messages marked as read
     */
    @PutMapping("/read/{conversationId}")
    @Operation(summary = "Mark Messages As Read", description = "标记指定会话的所有消息为已读")
    @Parameter(name = "conversationId", description = "会话ID", required = true)
    public R<Integer> markMessagesAsRead(@PathVariable("conversationId") Long conversationId) {
        Long userId = LoginHelper.getUserId();
        Integer readCount = messageService.markMessagesAsRead(userId, conversationId);
        return R.ok(readCount);
    }

    /**
     * Recall message
     * 撤回消息
     *
     * @param messageId Message ID
     * @return Success response
     */
    @PostMapping("/recall/{messageId}")
    @Operation(summary = "Recall Message", description = "撤回消息（2分钟内有效）")
    @Parameter(name = "messageId", description = "消息ID", required = true)
    public R<Void> recallMessage(@PathVariable("messageId") Long messageId) {
        Long userId = LoginHelper.getUserId();
        messageService.recallMessage(userId, messageId);
        return R.ok();
    }

    /**
     * Delete message
     * 删除消息
     *
     * @param messageId Message ID
     * @return Success response
     */
    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete Message", description = "删除消息（软删除）")
    @Parameter(name = "messageId", description = "消息ID", required = true)
    public R<Void> deleteMessage(@PathVariable("messageId") Long messageId) {
        Long userId = LoginHelper.getUserId();
        messageService.deleteMessage(userId, messageId);
        return R.ok();
    }

    /**
     * Upload media file
     * 上传媒体文件
     *
     * @param file     File to upload
     * @param fileType File type: image/voice/video
     * @return Upload result with file URL(s)
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload Media File", description = "上传媒体文件（图片/语音/视频）")
    public R<Map<String, Object>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {

        Map<String, Object> result = new HashMap<>();

        try {
            switch (fileType.toLowerCase()) {
                case "image":
                    String imageUrl = fileUploadService.uploadImage(file);
                    result.put("mediaUrl", imageUrl);
                    result.put("fileType", "image");
                    break;

                case "voice":
                    String voiceUrl = fileUploadService.uploadVoice(file);
                    Integer voiceDuration = fileUploadService.getFileDuration(voiceUrl);
                    result.put("mediaUrl", voiceUrl);
                    result.put("duration", voiceDuration);
                    result.put("fileType", "voice");
                    break;

                case "video":
                    String[] videoResult = fileUploadService.uploadVideo(file);
                    Integer videoDuration = fileUploadService.getFileDuration(videoResult[0]);
                    result.put("mediaUrl", videoResult[0]);
                    result.put("thumbnailUrl", videoResult[1]);
                    result.put("duration", videoDuration);
                    result.put("fileType", "video");
                    break;

                default:
                    return R.fail("不支持的文件类型: " + fileType);
            }

            log.info("Media file uploaded successfully: fileType={}, userId={}",
                fileType, LoginHelper.getUserId());

            return R.ok(result);

        } catch (Exception e) {
            log.error("Failed to upload media file: fileType={}, error={}",
                fileType, e.getMessage(), e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
