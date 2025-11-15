package org.dromara.chat.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * File Upload Service Interface
 * 文件上传服务接口
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
public interface IFileUploadService {

    /**
     * Upload image file
     * 上传图片文件
     *
     * @param file Image file
     * @return File URL
     */
    String uploadImage(MultipartFile file);

    /**
     * Upload voice file
     * 上传语音文件
     *
     * @param file Voice file
     * @return File URL
     */
    String uploadVoice(MultipartFile file);

    /**
     * Upload video file
     * 上传视频文件
     *
     * @param file Video file
     * @return Array with [videoUrl, thumbnailUrl]
     */
    String[] uploadVideo(MultipartFile file);

    /**
     * Get file duration (for voice/video)
     * 获取文件时长（语音/视频）
     *
     * @param fileUrl File URL
     * @return Duration in seconds
     */
    Integer getFileDuration(String fileUrl);
}
