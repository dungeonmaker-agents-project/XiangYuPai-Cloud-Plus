package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发现页点赞结果VO
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
public class RemoteDiscoverLikeResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 新的点赞状态
     */
    private Boolean isLiked;

    /**
     * 新的点赞数
     */
    private Integer likeCount;

    /**
     * 消息（错误时有值）
     */
    private String message;

    /**
     * 创建成功结果
     */
    public static RemoteDiscoverLikeResultVo success(boolean isLiked, int likeCount) {
        RemoteDiscoverLikeResultVo result = new RemoteDiscoverLikeResultVo();
        result.setSuccess(true);
        result.setIsLiked(isLiked);
        result.setLikeCount(likeCount);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static RemoteDiscoverLikeResultVo fail(String message) {
        RemoteDiscoverLikeResultVo result = new RemoteDiscoverLikeResultVo();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
