package org.dromara.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message Entity
 * 消息表 - 聊天消息记录
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("message")
public class Message extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型: text/image/voice/video
     */
    private String messageType;

    /**
     * 消息内容 (文字消息)
     */
    private String content;

    /**
     * 媒体URL (图片/语音/视频)
     */
    private String mediaUrl;

    /**
     * 缩略图URL (视频消息)
     */
    private String thumbnailUrl;

    /**
     * 时长 (语音/视频，单位：秒)
     */
    private Integer duration;

    /**
     * 消息状态: 0=发送中, 1=已送达, 2=已读, 3=失败
     */
    @Builder.Default
    private Integer status = 0;

    /**
     * 是否已撤回
     */
    @Builder.Default
    private Boolean isRecalled = false;

    /**
     * 撤回时间
     */
    private LocalDateTime recalledAt;

    /**
     * 是否已删除 (逻辑删除)
     */
    @TableLogic
    @Builder.Default
    private Integer deleted = 0;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 版本号 (乐观锁)
     */
    @Version
    @Builder.Default
    private Integer version = 0;
}
