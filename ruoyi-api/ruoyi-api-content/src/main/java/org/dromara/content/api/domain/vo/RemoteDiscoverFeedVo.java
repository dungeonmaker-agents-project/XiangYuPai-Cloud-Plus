package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发现页内容项VO
 * 对应UI文档的 ContentItem 数据模型
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Data
public class RemoteDiscoverFeedVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 基础信息 ==========

    /**
     * 内容ID
     */
    private Long id;

    /**
     * 内容类型: image/video
     */
    private String type;

    // ========== 媒体数据 (mediaData) ==========

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 宽高比 (width/height)
     */
    private BigDecimal aspectRatio;

    /**
     * 视频时长(秒)，仅video类型有效
     */
    private Integer duration;

    /**
     * 媒体宽度(px)
     */
    private Integer width;

    /**
     * 媒体高度(px)
     */
    private Integer height;

    // ========== 文本数据 (textData) ==========

    /**
     * 标题
     */
    private String title;

    /**
     * 内容摘要
     */
    private String content;

    // ========== 作者数据 (authorData) ==========

    /**
     * 作者用户ID
     */
    private Long userId;

    /**
     * 作者头像URL
     */
    private String userAvatar;

    /**
     * 作者昵称
     */
    private String userNickname;

    // ========== 统计数据 (statsData) ==========

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否已点赞
     */
    private Boolean isLiked;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 是否已收藏
     */
    private Boolean isCollected;

    // ========== 元信息 (metaData) ==========

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 位置名称
     */
    private String location;

    /**
     * 距离（米），同城Tab时有值
     */
    private Double distance;

}
