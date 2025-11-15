package org.dromara.common.media.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体上传结果视图对象
 * Media Upload Result VO
 *
 * @author XiangYuPai Team
 */
@Data
public class MediaUploadResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 媒体文件ID
     */
    private Long id;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 视频时长 (秒)
     */
    private Integer duration;
}
