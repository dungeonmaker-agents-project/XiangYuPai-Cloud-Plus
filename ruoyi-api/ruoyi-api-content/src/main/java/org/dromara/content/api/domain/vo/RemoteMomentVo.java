package org.dromara.content.api.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 动态详情VO (用于RPC传输)
 *
 * <p>用途：对方主页的动态Tab展示</p>
 * <p>对应UI文档: 对方主页_结构文档.md - MomentItem</p>
 *
 * @author XyPai Team
 * @date 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMomentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    private Long id;

    /**
     * 媒体类型: image/video
     */
    private String type;

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
     * 标题（可能为空）
     */
    private String title;

    /**
     * 作者用户ID
     */
    private Long authorId;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 创建时间戳
     */
    private Long createdTimestamp;
}
