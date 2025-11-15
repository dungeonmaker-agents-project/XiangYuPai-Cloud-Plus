package org.dromara.chat.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.chat.service.IFileUploadService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * File Upload Service Implementation
 * 文件上传服务实现类
 *
 * NOTE: This implementation uses RuoYi-Cloud-Plus OSS module for file storage.
 * The actual storage backend (MinIO, Aliyun OSS, Qiniu, etc.) is configured in application.yml
 *
 * @author XiangYuPai Team
 * @since 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements IFileUploadService {

    private final OssClient ossClient;

    // File size limits (bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VOICE_SIZE = 2 * 1024 * 1024;  // 2MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

    // Allowed file types
    private static final List<String> IMAGE_TYPES = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> VOICE_TYPES = Arrays.asList("mp3", "wav", "m4a", "aac");
    private static final List<String> VIDEO_TYPES = Arrays.asList("mp4", "mov", "avi", "mkv");

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // Validate file
            validateFile(file, MAX_IMAGE_SIZE, IMAGE_TYPES, "图片");

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.extName(originalFilename);
            String filename = "chat/images/" + IdUtil.fastSimpleUUID() + "." + extension;

            // Upload to OSS
            UploadResult result = ossClient.upload(file.getBytes(), filename, file.getContentType());

            log.info("Image uploaded successfully: filename={}, url={}", filename, result.getUrl());
            return result.getUrl();

        } catch (IOException e) {
            log.error("Failed to upload image", e);
            throw new ServiceException("图片上传失败");
        }
    }

    @Override
    public String uploadVoice(MultipartFile file) {
        try {
            // Validate file
            validateFile(file, MAX_VOICE_SIZE, VOICE_TYPES, "语音");

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.extName(originalFilename);
            String filename = "chat/voices/" + IdUtil.fastSimpleUUID() + "." + extension;

            // Upload to OSS
            UploadResult result = ossClient.upload(file.getBytes(), filename, file.getContentType());

            log.info("Voice uploaded successfully: filename={}, url={}", filename, result.getUrl());
            return result.getUrl();

        } catch (IOException e) {
            log.error("Failed to upload voice", e);
            throw new ServiceException("语音上传失败");
        }
    }

    @Override
    public String[] uploadVideo(MultipartFile file) {
        try {
            // Validate file
            validateFile(file, MAX_VIDEO_SIZE, VIDEO_TYPES, "视频");

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.extName(originalFilename);
            String videoFilename = "chat/videos/" + IdUtil.fastSimpleUUID() + "." + extension;

            // Upload video to OSS
            UploadResult videoResult = ossClient.upload(file.getBytes(), videoFilename, file.getContentType());

            // TODO: Generate video thumbnail
            // For now, return placeholder thumbnail URL
            String thumbnailUrl = generateThumbnailPlaceholder(videoResult.getUrl());

            log.info("Video uploaded successfully: videoUrl={}, thumbnailUrl={}",
                videoResult.getUrl(), thumbnailUrl);

            return new String[]{videoResult.getUrl(), thumbnailUrl};

        } catch (IOException e) {
            log.error("Failed to upload video", e);
            throw new ServiceException("视频上传失败");
        }
    }

    @Override
    public Integer getFileDuration(String fileUrl) {
        // TODO: Implement actual duration extraction using FFmpeg or similar
        // For now, return default duration
        log.warn("File duration extraction not yet implemented, returning default value");
        return 0;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file, long maxSize, List<String> allowedTypes, String fileTypeName) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new ServiceException(fileTypeName + "文件不能为空");
        }

        // Check file size
        if (file.getSize() > maxSize) {
            throw new ServiceException(fileTypeName + "文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        // Check file type
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new ServiceException("文件名不能为空");
        }

        String extension = FileUtil.extName(originalFilename).toLowerCase();
        if (!allowedTypes.contains(extension)) {
            throw new ServiceException(fileTypeName + "文件格式不支持，仅支持: " + String.join(", ", allowedTypes));
        }

        log.debug("File validation passed: filename={}, size={}, type={}",
            originalFilename, file.getSize(), extension);
    }

    /**
     * Generate thumbnail placeholder URL
     * TODO: Replace with actual thumbnail generation logic
     */
    private String generateThumbnailPlaceholder(String videoUrl) {
        // For now, return a placeholder thumbnail URL
        // In production, this would use FFmpeg to extract a frame from the video
        return videoUrl.replace(".mp4", "_thumbnail.jpg")
            .replace(".mov", "_thumbnail.jpg")
            .replace(".avi", "_thumbnail.jpg");
    }
}
