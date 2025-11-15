package org.dromara.common.media.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * 媒体上传业务对象
 * Media Upload BO
 *
 * @author XiangYuPai Team
 */
@Data
public class MediaUploadBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 上传文件
     */
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;

    /**
     * 业务类型: avatar/post/moment/chat等
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 关联业务ID (可选)
     */
    private Long bizId;

    /**
     * 是否生成缩略图 (默认true)
     */
    private Boolean generateThumbnail = true;

    /**
     * 是否压缩图片 (默认true)
     */
    private Boolean compressImage = true;
}
