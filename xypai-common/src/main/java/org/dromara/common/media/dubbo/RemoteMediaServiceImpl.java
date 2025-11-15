package org.dromara.common.media.dubbo;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.api.media.RemoteMediaService;
import org.dromara.common.core.domain.R;
import org.dromara.common.media.domain.MediaFile;
import org.dromara.common.media.domain.vo.MediaUploadResultVo;
import org.dromara.common.media.mapper.MediaFileMapper;
import org.dromara.common.media.service.IMediaService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 媒体服务远程调用实现
 * Remote Media Service Implementation (Dubbo Provider)
 *
 * <p>用途: 为其他微服务提供媒体文件相关的RPC接口</p>
 * <p>调用方: xypai-user, xypai-content, xypai-chat等</p>
 *
 * @author XiangYuPai Team
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteMediaServiceImpl implements RemoteMediaService {

    private final IMediaService mediaService;
    private final MediaFileMapper mediaFileMapper;

    @Override
    public R<String> getFileUrl(Long fileId) {
        log.info("RPC调用 - 获取文件URL: fileId={}", fileId);

        try {
            MediaFile mediaFile = mediaFileMapper.selectById(fileId);
            if (mediaFile == null) {
                return R.fail("文件不存在");
            }

            return R.ok(mediaFile.getFileUrl());
        } catch (Exception e) {
            log.error("获取文件URL失败: fileId={}", fileId, e);
            return R.fail("获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> findFileByMd5(String md5) {
        log.info("RPC调用 - 根据MD5查找文件: md5={}", md5);

        try {
            if (StrUtil.isBlank(md5)) {
                return R.fail("MD5不能为空");
            }

            MediaUploadResultVo existingFile = mediaService.findByMd5(md5);
            if (existingFile != null) {
                return R.ok(existingFile.getFileUrl());
            }

            return R.fail("文件不存在");
        } catch (Exception e) {
            log.error("根据MD5查找文件失败: md5={}", md5, e);
            return R.fail("查找文件失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> deleteFile(Long fileId, Long userId) {
        log.info("RPC调用 - 删除文件: fileId={}, userId={}", fileId, userId);

        try {
            // 验证文件所有权
            MediaFile mediaFile = mediaFileMapper.selectById(fileId);
            if (mediaFile == null) {
                return R.fail("文件不存在");
            }

            if (!mediaFile.getUserId().equals(userId)) {
                return R.fail("无权限删除此文件");
            }

            // 执行删除
            R<Boolean> result = mediaService.deleteMedia(fileId);
            return result;
        } catch (Exception e) {
            log.error("删除文件失败: fileId={}, userId={}", fileId, userId, e);
            return R.fail("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> batchDeleteFiles(Long[] fileIds, Long userId) {
        log.info("RPC调用 - 批量删除文件: fileIds={}, userId={}", Arrays.toString(fileIds), userId);

        try {
            if (fileIds == null || fileIds.length == 0) {
                return R.ok(true);
            }

            // 批量删除
            int successCount = 0;
            for (Long fileId : fileIds) {
                R<Boolean> result = deleteFile(fileId, userId);
                if (result.isSuccess() && result.getData()) {
                    successCount++;
                }
            }

            log.info("批量删除完成: 成功{}/总数{}", successCount, fileIds.length);
            return R.ok(successCount == fileIds.length);
        } catch (Exception e) {
            log.error("批量删除文件失败: fileIds={}", Arrays.toString(fileIds), e);
            return R.fail("批量删除失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> verifyFileOwnership(Long fileId, Long userId) {
        log.debug("RPC调用 - 验证文件所有权: fileId={}, userId={}", fileId, userId);

        try {
            MediaFile mediaFile = mediaFileMapper.selectById(fileId);
            if (mediaFile == null) {
                return R.ok(false);
            }

            boolean isOwner = mediaFile.getUserId().equals(userId);
            return R.ok(isOwner);
        } catch (Exception e) {
            log.error("验证文件所有权失败: fileId={}, userId={}", fileId, userId, e);
            return R.fail("验证失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> fileExists(Long fileId) {
        log.debug("RPC调用 - 验证文件是否存在: fileId={}", fileId);

        try {
            MediaFile mediaFile = mediaFileMapper.selectById(fileId);
            return R.ok(mediaFile != null);
        } catch (Exception e) {
            log.error("验证文件存在失败: fileId={}", fileId, e);
            return R.fail("验证失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> bindFileToBiz(Long fileId, String bizType, Long bizId) {
        log.info("RPC调用 - 关联文件到业务: fileId={}, bizType={}, bizId={}", fileId, bizType, bizId);

        try {
            MediaFile mediaFile = mediaFileMapper.selectById(fileId);
            if (mediaFile == null) {
                return R.fail("文件不存在");
            }

            // 更新业务关联信息
            mediaFile.setBizType(bizType);
            mediaFile.setBizId(bizId);
            int updated = mediaFileMapper.updateById(mediaFile);

            return R.ok(updated > 0);
        } catch (Exception e) {
            log.error("关联文件失败: fileId={}, bizType={}, bizId={}", fileId, bizType, bizId, e);
            return R.fail("关联文件失败: " + e.getMessage());
        }
    }

    @Override
    public R<String[]> getFilesByBiz(String bizType, Long bizId) {
        log.info("RPC调用 - 查询业务关联文件: bizType={}, bizId={}", bizType, bizId);

        try {
            List<MediaFile> mediaFiles = mediaFileMapper.selectList(
                new LambdaQueryWrapper<MediaFile>()
                    .eq(MediaFile::getBizType, bizType)
                    .eq(MediaFile::getBizId, bizId)
                    .orderByAsc(MediaFile::getCreateTime)
            );

            String[] fileUrls = mediaFiles.stream()
                .map(MediaFile::getFileUrl)
                .toArray(String[]::new);

            return R.ok(fileUrls);
        } catch (Exception e) {
            log.error("查询业务关联文件失败: bizType={}, bizId={}", bizType, bizId, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
