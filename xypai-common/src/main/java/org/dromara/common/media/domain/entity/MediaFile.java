package org.dromara.common.media.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 媒体文件实体
 * Media File Entity
 *
 * @author XiangYuPai Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("media_file")
public class MediaFile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 媒体文件ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文件类型: image/video/audio
     */
    private String fileType;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String storedName;

    /**
     * 文件路径 (OSS相对路径)
     */
    private String filePath;

    /**
     * 文件URL (完整访问地址)
     */
    private String fileUrl;

    /**
     * 缩略图URL (图片/视频封面)
     */
    private String thumbnailUrl;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * MIME类型
     */
    private String mimeType;

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

    /**
     * 存储平台: aliyun/tencent/local
     */
    private String storage;

    /**
     * OSS Bucket名称
     */
    private String bucketName;

    /**
     * 文件MD5值 (去重使用)
     */
    private String md5;

    /**
     * 业务类型: avatar/post/moment/chat等
     */
    private String bizType;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 状态: 0=正常, 1=审核中, 2=违规
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField(value = "deleted")
    private Long deleted;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(value = "version")
    private Long version;
}
