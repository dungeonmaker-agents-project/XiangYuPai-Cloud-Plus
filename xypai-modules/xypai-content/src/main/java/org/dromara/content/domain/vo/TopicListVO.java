package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 话题列表项VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "话题列表项")
public class TopicListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "话题ID")
    private Long id;

    @Schema(description = "话题名称")
    private String name;

    @Schema(description = "话题描述")
    private String description;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "参与人数")
    private Integer participantCount;

    @Schema(description = "帖子数")
    private Integer postCount;

    @Schema(description = "是否官方话题")
    private Boolean isOfficial;

    @Schema(description = "是否热门话题")
    private Boolean isHot;

}
