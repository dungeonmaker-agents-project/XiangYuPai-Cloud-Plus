package com.xypai.chat.controller.app;

import com.xypai.chat.domain.dto.MessageSettingsUpdateDTO;
import com.xypai.chat.domain.vo.MessageSettingsVO;
import com.xypai.chat.service.IMessageSettingsService;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消息设置控制器
 * 
 * @author xypai
 * @date 2025-01-14
 */
@Tag(name = "消息设置", description = "用户消息设置管理API")
@RestController
@RequestMapping("/api/v1/message-settings")
@RequiredArgsConstructor
@Validated
public class MessageSettingsController extends BaseController {

    private final IMessageSettingsService messageSettingsService;

    /**
     * 获取当前用户的消息设置
     */
    @Operation(summary = "获取我的消息设置", description = "获取当前用户的消息设置")
    @GetMapping("/my")
    @SaCheckPermission("chat:settings:query")
    public R<MessageSettingsVO> getMySettings() {
        return R.ok(messageSettingsService.getMySettings());
    }

    /**
     * 更新消息设置
     */
    @Operation(summary = "更新消息设置", description = "更新当前用户的消息设置")
    @PutMapping
    @SaCheckPermission("chat:settings:edit")
    @Log(title = "更新消息设置", businessType = BusinessType.UPDATE)
    public R<Void> updateSettings(@Validated @RequestBody MessageSettingsUpdateDTO updateDTO) {
        boolean success = messageSettingsService.updateSettings(updateDTO);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 重置为默认设置
     */
    @Operation(summary = "重置为默认设置", description = "重置当前用户的消息设置为默认值")
    @PostMapping("/reset")
    @SaCheckPermission("chat:settings:edit")
    @Log(title = "重置消息设置", businessType = BusinessType.UPDATE)
    public R<Void> resetSettings() {
        boolean success = messageSettingsService.resetToDefault(null);
        return success ? R.ok() : R.fail("重置失败");
    }

    /**
     * 初始化消息设置（管理员功能）
     */
    @Operation(summary = "初始化用户消息设置", description = "为指定用户初始化消息设置")
    @PostMapping("/init/{userId}")
    @SaCheckPermission("chat:settings:admin")
    @Log(title = "初始化消息设置", businessType = BusinessType.INSERT)
    public R<Void> initSettings(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean success = messageSettingsService.initSettings(userId);
        return success ? R.ok() : R.fail("初始化失败");
    }

    /**
     * 快捷设置：开启/关闭推送
     */
    @Operation(summary = "快捷设置推送", description = "一键开启或关闭所有推送")
    @PutMapping("/quick/push/{enabled}")
    @SaCheckPermission("chat:settings:edit")
    @Log(title = "快捷设置推送", businessType = BusinessType.UPDATE)
    public R<Void> quickSetPush(
            @Parameter(description = "是否开启", required = true)
            @PathVariable Boolean enabled) {
        MessageSettingsUpdateDTO updateDTO = MessageSettingsUpdateDTO.builder()
                .pushEnabled(enabled)
                .build();
        boolean success = messageSettingsService.updateSettings(updateDTO);
        return success ? R.ok() : R.fail("设置失败");
    }

    /**
     * 快捷设置：开启/关闭已读回执
     */
    @Operation(summary = "快捷设置已读回执", description = "一键开启或关闭已读回执")
    @PutMapping("/quick/read-receipt/{enabled}")
    @SaCheckPermission("chat:settings:edit")
    @Log(title = "快捷设置已读回执", businessType = BusinessType.UPDATE)
    public R<Void> quickSetReadReceipt(
            @Parameter(description = "是否开启", required = true)
            @PathVariable Boolean enabled) {
        MessageSettingsUpdateDTO updateDTO = MessageSettingsUpdateDTO.builder()
                .messageReadReceipt(enabled)
                .build();
        boolean success = messageSettingsService.updateSettings(updateDTO);
        return success ? R.ok() : R.fail("设置失败");
    }

    /**
     * 快捷设置：隐私模式（不允许陌生人发消息）
     */
    @Operation(summary = "快捷设置隐私模式", description = "开启或关闭隐私模式")
    @PutMapping("/quick/privacy-mode/{enabled}")
    @SaCheckPermission("chat:settings:edit")
    @Log(title = "快捷设置隐私模式", businessType = BusinessType.UPDATE)
    public R<Void> quickSetPrivacyMode(
            @Parameter(description = "是否开启", required = true)
            @PathVariable Boolean enabled) {
        // 隐私模式：只允许互相关注的人发消息
        Integer whoCanMessage = enabled ? 2 : 0;
        MessageSettingsUpdateDTO updateDTO = MessageSettingsUpdateDTO.builder()
                .whoCanMessage(whoCanMessage)
                .build();
        boolean success = messageSettingsService.updateSettings(updateDTO);
        return success ? R.ok() : R.fail("设置失败");
    }
}
