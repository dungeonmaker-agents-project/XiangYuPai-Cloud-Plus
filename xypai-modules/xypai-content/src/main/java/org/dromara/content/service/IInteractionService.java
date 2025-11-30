package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.dto.InteractionDTO;
import org.dromara.content.domain.dto.MyCollectionQueryDTO;
import org.dromara.content.domain.dto.MyLikeQueryDTO;
import org.dromara.content.domain.vo.InteractionResultVO;
import org.dromara.content.domain.vo.MyCollectionVO;
import org.dromara.content.domain.vo.MyLikeVO;

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

    /**
     * 获取我的点赞列表
     *
     * @param queryDTO 查询参数
     * @param userId 用户ID
     * @return 点赞列表
     */
    Page<MyLikeVO> getMyLikeList(MyLikeQueryDTO queryDTO, Long userId);

    /**
     * 获取我的收藏列表
     *
     * @param queryDTO 查询参数
     * @param userId 用户ID
     * @return 收藏列表
     */
    Page<MyCollectionVO> getMyCollectionList(MyCollectionQueryDTO queryDTO, Long userId);

}
