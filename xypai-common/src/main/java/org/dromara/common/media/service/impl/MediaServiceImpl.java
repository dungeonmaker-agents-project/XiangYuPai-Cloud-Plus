package org.dromara.common.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.media.domain.bo.MediaUploadBo;
import org.dromara.common.media.domain.entity.MediaFile;
import org.dromara.common.media.domain.vo.MediaUploadResultVo;
import org.dromara.common.media.mapper.MediaFileMapper;
import org.dromara.common.media.service.IMediaService;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.factory.OssFactory;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 媒体服务实现
 * Media Service Implementation
 *
 * @author XiangYuPai Team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MediaServiceImpl implements IMediaService {

    private final MediaFileMapper mediaFileMapper;

    /**
     * 允许的图片类型
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * 允许的视频类型
     */
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/mpeg", "video/quicktime"};

    /**
     * 图片最大大小: 10MB
     */
    private static final int MAX_IMAGE_SIZE_MB = 10;

    /**
     * 视频最大大小: 100MB
     */
    private static final int MAX_VIDEO_SIZE_MB = 100;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<MediaUploadResultVo> uploadMedia(MediaUploadBo uploadBo) {
        MultipartFile file = uploadBo.getFile();

        log.info("开始上传媒体文件 - 原始文件名: {}, 业务类型: {}",
                 file.getOriginalFilename(), uploadBo.getBizType());

        try {
            // 1. 计算文件MD5 (用于秒传)
            String md5 = DigestUtil.md5Hex(file.getInputStream());

            // 2. 检查是否已存在相同文件 (秒传)
            MediaUploadResultVo existingFile = findByMd5(md5);
            if (existingFile != null) {
                log.info("文件已存在，秒传成功 - MD5: {}", md5);
                return R.ok(existingFile);
            }

            // 3. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null) {
                return R.fail("无法识别文件类型");
            }

            String fileType = getFileType(contentType);
            if (!validateFileType(file, fileType)) {
                return R.fail("不支持的文件类型: " + contentType);
            }

            // 4. 验证文件大小
            int maxSize = fileType.equals("image") ? MAX_IMAGE_SIZE_MB : MAX_VIDEO_SIZE_MB;
            if (!validateFileSize(file.getSize(), maxSize)) {
                return R.fail("文件大小超过限制 (最大 " + maxSize + "MB)");
            }

            // 5. 构建存储路径
            String fileExt = FileUtil.extName(file.getOriginalFilename());
            String storedName = IdUtil.fastSimpleUUID() + "." + fileExt;
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filePath = uploadBo.getBizType() + "/" + datePath + "/" + storedName;

            // 6. 上传到OSS
            OssClient ossClient = OssFactory.instance();
            byte[] fileBytes = file.getBytes();
            UploadResult uploadResult = ossClient.upload(
                new ByteArrayInputStream(fileBytes),
                filePath,
                (long) fileBytes.length,
                contentType
            );

            // 7. 保存媒体记录
            MediaFile mediaFile = new MediaFile();
            mediaFile.setUserId(LoginHelper.getUserId());
            mediaFile.setFileType(fileType);
            mediaFile.setOriginalName(file.getOriginalFilename());
            mediaFile.setStoredName(storedName);
            mediaFile.setFilePath(filePath);
            mediaFile.setFileUrl(uploadResult.getUrl());
            mediaFile.setFileSize(file.getSize());
            mediaFile.setFileExt(fileExt);
            mediaFile.setMimeType(contentType);
            mediaFile.setMd5(md5);
            mediaFile.setBizType(uploadBo.getBizType());
            mediaFile.setBizId(uploadBo.getBizId());
            mediaFile.setStatus(0);  // 0=正常

            mediaFileMapper.insert(mediaFile);

            // 8. 构建返回结果
            MediaUploadResultVo result = BeanUtil.toBean(mediaFile, MediaUploadResultVo.class);

            log.info("媒体文件上传成功 - ID: {}, URL: {}", mediaFile.getId(), mediaFile.getFileUrl());

            return R.ok(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteMedia(Long id) {
        log.info("删除媒体文件 - ID: {}", id);

        MediaFile mediaFile = mediaFileMapper.selectById(id);
        if (mediaFile == null) {
            return R.fail("文件不存在");
        }

        // 验证权限 (只能删除自己的文件)
        if (!mediaFile.getUserId().equals(LoginHelper.getUserId())) {
            return R.fail("无权删除该文件");
        }

        // 从OSS删除文件
        try {
            OssClient ossClient = OssFactory.instance();
            ossClient.delete(mediaFile.getFilePath());
        } catch (Exception e) {
            log.error("从OSS删除文件失败 - Path: {}", mediaFile.getFilePath(), e);
        }

        // 逻辑删除数据库记录
        mediaFileMapper.deleteById(id);

        log.info("媒体文件删除成功 - ID: {}", id);
        return R.ok(true);
    }

    @Override
    public MediaUploadResultVo findByMd5(String md5) {
        LambdaQueryWrapper<MediaFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFile::getMd5, md5)
               .eq(MediaFile::getDeleted, 0)
               .last("LIMIT 1");

        MediaFile mediaFile = mediaFileMapper.selectOne(wrapper);
        if (mediaFile == null) {
            return null;
        }

        return BeanUtil.toBean(mediaFile, MediaUploadResultVo.class);
    }

    @Override
    public boolean validateFileType(MultipartFile file, String... allowedTypes) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        for (String allowedType : allowedTypes) {
            if (allowedType.equals("image") && Arrays.asList(ALLOWED_IMAGE_TYPES).contains(contentType)) {
                return true;
            }
            if (allowedType.equals("video") && Arrays.asList(ALLOWED_VIDEO_TYPES).contains(contentType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validateFileSize(long fileSize, int maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024L * 1024L;
        return fileSize <= maxSizeBytes;
    }

    /**
     * 根据MIME类型获取文件类型
     */
    private String getFileType(String contentType) {
        if (contentType.startsWith("image/")) {
            return "image";
        }
        if (contentType.startsWith("video/")) {
            return "video";
        }
        if (contentType.startsWith("audio/")) {
            return "audio";
        }
        return "file";
    }
}
