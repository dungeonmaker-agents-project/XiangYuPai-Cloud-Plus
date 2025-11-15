package org.dromara.content.service;

import org.dromara.content.domain.dto.InteractionDTO;
import org.dromara.content.domain.vo.InteractionResultVO;

/**
 * 互动服务接口
 *
 * @author XiangYuPai
 */
public interface IInteractionService {

    /**
     * 点赞/取消点赞
     *
     * @param interactionDTO 互动参数
     * @param userId 用户ID
     * @return 操作结果
     */
    InteractionResultVO handleLike(InteractionDTO interactionDTO, Long userId);

    /**
     * 收藏/取消收藏
     *
     * @param interactionDTO 互动参数
     * @param userId 用户ID
     * @return 操作结果
     */
    InteractionResultVO handleCollect(InteractionDTO interactionDTO, Long userId);

    /**
     * 分享
     *
     * @param targetId 目标ID
     * @param shareChannel 分享渠道
     * @param userId 用户ID
     * @return 分享结果
     */
    InteractionResultVO handleShare(Long targetId, String shareChannel, Long userId);

}
