package org.dromara.common.media.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 媒体文件实体
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("media_file")
public class MediaFile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件MD5
     */
    private String md5;

    /**
     * 缩略图URL
     */
    private String thumbnail;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 视频时长(秒)
     */
    private Integer duration;

    /**
     * 业务类型 (post/comment/avatar等)
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 状态 (0=正常 1=删除)
     */
    private Integer status;

    /**
     * 删除标志 (0=正常 1=删除)
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
