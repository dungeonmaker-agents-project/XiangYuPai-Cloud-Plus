package org.dromara.content.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 发布配置VO
 *
 * @author XiangYuPai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发布配置")
public class PublishConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题最大长度")
    private Integer maxTitleLength;

    @Schema(description = "正文最大长度")
    private Integer maxContentLength;

    @Schema(description = "最多上传图片数量")
    private Integer maxImageCount;

    @Schema(description = "最多上传视频数量")
    private Integer maxVideoCount;

    @Schema(description = "最多选择话题数量")
    private Integer maxTopicCount;

    @Schema(description = "单张图片最大大小(字节)")
    private Long maxImageSize;

    @Schema(description = "单个视频最大大小(字节)")
    private Long maxVideoSize;

    @Schema(description = "支持的图片格式")
    private String supportedImageFormats;

    @Schema(description = "支持的视频格式")
    private String supportedVideoFormats;

}
