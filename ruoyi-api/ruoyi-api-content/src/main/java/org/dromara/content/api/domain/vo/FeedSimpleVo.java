package org.dromara.content.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 动态简要信息VO (用于RPC传输)
 *
 * <p>用途：首页用户卡片下方展示的动态预览</p>
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedSimpleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    private Long feedId;

    /**
     * 封面图URL
     */
    private String coverImage;

    /**
     * 动态内容（截取前50字符）
     */
    private String content;

    /**
     * 动态类型: 1=动态, 2=活动, 3=技能
     */
    private Integer type;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 创建时间戳
     */
    private Long createdTimestamp;
}
