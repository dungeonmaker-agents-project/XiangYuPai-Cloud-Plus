package org.dromara.chat.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Remote Chat Unread Count VO
 * 远程调用-未读消息数VO
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteChatUnreadCountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Unread chat message count
     * 未读聊天消息数
     */
    private Integer chatMessages;

    /**
     * Unread likes count
     * 未读点赞数
     */
    private Integer likes;

    /**
     * Unread comments count
     * 未读评论数
     */
    private Integer comments;

    /**
     * Unread followers count
     * 未读粉丝数
     */
    private Integer followers;

    /**
     * Unread system notifications count
     * 未读系统通知数
     */
    private Integer system;

    /**
     * Total unread count
     * 总未读数
     */
    private Integer total;
}
