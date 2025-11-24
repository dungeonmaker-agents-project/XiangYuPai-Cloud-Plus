package org.dromara.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 动态列表VO
 * Post List VO
 *
 * @author XiangYuPai
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Post list response")
public class PostListVo {

    @Schema(description = "Posts")
    private List<PostVo> posts;

    @Schema(description = "Total count")
    private Long total;

    @Schema(description = "Has more data")
    private Boolean hasMore;
}
