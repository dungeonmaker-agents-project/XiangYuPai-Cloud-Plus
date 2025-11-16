package org.dromara.common.media.dubbo;

import org.dromara.common.core.domain.R;
import org.dromara.common.media.domain.MediaFile;
import org.dromara.common.media.domain.vo.MediaUploadResultVo;
import org.dromara.common.media.mapper.MediaFileMapper;
import org.dromara.common.media.service.IMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RemoteMediaService Dubbo实现类单元测试
 * Remote Media Service Implementation Unit Tests
 *
 * @author XiangYuPai Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("媒体服务RPC实现测试")
public class RemoteMediaServiceImplTest {

    @Mock
    private IMediaService mediaService;

    @Mock
    private MediaFileMapper mediaFileMapper;

    @InjectMocks
    private RemoteMediaServiceImpl remoteMediaService;

    private MediaFile mediaFile;
    private Long fileId;
    private Long userId;

    @BeforeEach
    void setUp() {
        fileId = 1001L;
        userId = 2001L;

        mediaFile = new MediaFile();
        mediaFile.setId(fileId);
        mediaFile.setUserId(userId);
        mediaFile.setFileUrl("https://oss.example.com/test.jpg");
        mediaFile.setFileName("test.jpg");
        mediaFile.setFileType("image/jpeg");
        mediaFile.setFileSize(1024000L);
        mediaFile.setMd5("abc123def456");
    }

    @Test
    @DisplayName("获取文件URL - 正常情况")
    void testGetFileUrl_Success() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);

        // When
        R<String> result = remoteMediaService.getFileUrl(fileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(mediaFile.getFileUrl());

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("获取文件URL - 文件不存在")
    void testGetFileUrl_FileNotFound() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(null);

        // When
        R<String> result = remoteMediaService.getFileUrl(fileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("文件不存在");

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("根据MD5查找文件 - 文件存在")
    void testFindFileByMd5_Found() {
        // Given
        String md5 = "abc123def456";
        MediaUploadResultVo uploadResult = new MediaUploadResultVo();
        uploadResult.setFileUrl(mediaFile.getFileUrl());

        when(mediaService.findByMd5(md5)).thenReturn(uploadResult);

        // When
        R<String> result = remoteMediaService.findFileByMd5(md5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(mediaFile.getFileUrl());

        // Verify
        verify(mediaService).findByMd5(md5);
    }

    @Test
    @DisplayName("根据MD5查找文件 - 文件不存在")
    void testFindFileByMd5_NotFound() {
        // Given
        String md5 = "nonexistent";
        when(mediaService.findByMd5(md5)).thenReturn(null);

        // When
        R<String> result = remoteMediaService.findFileByMd5(md5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("文件不存在");

        // Verify
        verify(mediaService).findByMd5(md5);
    }

    @Test
    @DisplayName("根据MD5查找文件 - MD5为空")
    void testFindFileByMd5_EmptyMd5() {
        // When
        R<String> result = remoteMediaService.findFileByMd5("");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("MD5不能为空");

        // Verify
        verify(mediaService, never()).findByMd5(any());
    }

    @Test
    @DisplayName("删除文件 - 正常情况")
    void testDeleteFile_Success() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);
        when(mediaService.deleteMedia(fileId)).thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteMediaService.deleteFile(fileId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
        verify(mediaService).deleteMedia(fileId);
    }

    @Test
    @DisplayName("删除文件 - 无权限")
    void testDeleteFile_NoPermission() {
        // Given
        Long otherUserId = 3001L;
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);

        // When
        R<Boolean> result = remoteMediaService.deleteFile(fileId, otherUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("无权限删除此文件");

        // Verify
        verify(mediaFileMapper).selectById(fileId);
        verify(mediaService, never()).deleteMedia(any());
    }

    @Test
    @DisplayName("删除文件 - 文件不存在")
    void testDeleteFile_FileNotFound() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(null);

        // When
        R<Boolean> result = remoteMediaService.deleteFile(fileId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("文件不存在");

        // Verify
        verify(mediaFileMapper).selectById(fileId);
        verify(mediaService, never()).deleteMedia(any());
    }

    @Test
    @DisplayName("批量删除文件 - 正常情况")
    void testBatchDeleteFiles_Success() {
        // Given
        Long[] fileIds = {1001L, 1002L};
        MediaFile file1 = new MediaFile();
        file1.setId(1001L);
        file1.setUserId(userId);

        MediaFile file2 = new MediaFile();
        file2.setId(1002L);
        file2.setUserId(userId);

        when(mediaFileMapper.selectById(1001L)).thenReturn(file1);
        when(mediaFileMapper.selectById(1002L)).thenReturn(file2);
        when(mediaService.deleteMedia(anyLong())).thenReturn(R.ok(true));

        // When
        R<Boolean> result = remoteMediaService.batchDeleteFiles(fileIds, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper, times(2)).selectById(anyLong());
        verify(mediaService, times(2)).deleteMedia(anyLong());
    }

    @Test
    @DisplayName("批量删除文件 - 文件列表为空")
    void testBatchDeleteFiles_EmptyArray() {
        // When
        R<Boolean> result = remoteMediaService.batchDeleteFiles(new Long[]{}, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("验证文件所有权 - 属于用户")
    void testVerifyFileOwnership_Owned() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);

        // When
        R<Boolean> result = remoteMediaService.verifyFileOwnership(fileId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("验证文件所有权 - 不属于用户")
    void testVerifyFileOwnership_NotOwned() {
        // Given
        Long otherUserId = 3001L;
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);

        // When
        R<Boolean> result = remoteMediaService.verifyFileOwnership(fileId, otherUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("验证文件是否存在 - 存在")
    void testFileExists_True() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);

        // When
        R<Boolean> result = remoteMediaService.fileExists(fileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("验证文件是否存在 - 不存在")
    void testFileExists_False() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(null);

        // When
        R<Boolean> result = remoteMediaService.fileExists(fileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isFalse();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
    }

    @Test
    @DisplayName("关联文件到业务 - 正常情况")
    void testBindFileToBiz_Success() {
        // Given
        String bizType = "post";
        Long bizId = 5001L;

        when(mediaFileMapper.selectById(fileId)).thenReturn(mediaFile);
        when(mediaFileMapper.updateById(any(MediaFile.class))).thenReturn(1);

        // When
        R<Boolean> result = remoteMediaService.bindFileToBiz(fileId, bizType, bizId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isTrue();

        // Verify
        verify(mediaFileMapper).selectById(fileId);
        verify(mediaFileMapper).updateById(argThat(file ->
            file.getBizType().equals(bizType) && file.getBizId().equals(bizId)
        ));
    }

    @Test
    @DisplayName("关联文件到业务 - 文件不存在")
    void testBindFileToBiz_FileNotFound() {
        // Given
        when(mediaFileMapper.selectById(fileId)).thenReturn(null);

        // When
        R<Boolean> result = remoteMediaService.bindFileToBiz(fileId, "post", 5001L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMsg()).contains("文件不存在");

        // Verify
        verify(mediaFileMapper).selectById(fileId);
        verify(mediaFileMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("查询业务关联文件 - 正常情况")
    void testGetFilesByBiz_Success() {
        // Given
        String bizType = "post";
        Long bizId = 5001L;

        MediaFile file1 = new MediaFile();
        file1.setFileUrl("https://oss.example.com/image1.jpg");

        MediaFile file2 = new MediaFile();
        file2.setFileUrl("https://oss.example.com/image2.jpg");

        when(mediaFileMapper.selectList(any())).thenReturn(Arrays.asList(file1, file2));

        // When
        R<String[]> result = remoteMediaService.getFilesByBiz(bizType, bizId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData()[0]).isEqualTo(file1.getFileUrl());
        assertThat(result.getData()[1]).isEqualTo(file2.getFileUrl());

        // Verify
        verify(mediaFileMapper).selectList(any());
    }
}
