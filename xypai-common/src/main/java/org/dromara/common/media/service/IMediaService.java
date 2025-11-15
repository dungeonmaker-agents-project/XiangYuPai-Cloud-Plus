package org.dromara.common.media.service;

import org.dromara.common.core.domain.R;
import org.dromara.common.media.domain.bo.MediaUploadBo;
import org.dromara.common.media.domain.vo.MediaUploadResultVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体服务接口
 * Media Service Interface
 *
 * @author XiangYuPai Team
 */
public interface IMediaService {

    /**
     * 上传媒体文件
     *
     * @param uploadBo 上传参数
     * @return 上传结果
     */
    R<MediaUploadResultVo> uploadMedia(MediaUploadBo uploadBo);

    /**
     * 删除媒体文件
     *
     * @param id 媒体文件ID
     * @return 是否成功
     */
    R<Boolean> deleteMedia(Long id);

    /**
     * 根据MD5查找文件 (秒传功能)
     *
     * @param md5 文件MD5值
     * @return 媒体文件信息
     */
    MediaUploadResultVo findByMd5(String md5);

    /**
     * 验证文件类型
     *
     * @param file 上传文件
     * @param allowedTypes 允许的类型 (image/video/audio)
     * @return 是否通过验证
     */
    boolean validateFileType(MultipartFile file, String... allowedTypes);

    /**
     * 验证文件大小
     *
     * @param fileSize 文件大小 (字节)
     * @param maxSizeMB 最大大小 (MB)
     * @return 是否通过验证
     */
    boolean validateFileSize(long fileSize, int maxSizeMB);
}
