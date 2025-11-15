package org.dromara.common.integration.flow;

import io.restassured.response.Response;
import org.dromara.common.support.FlowTestBase;
import org.dromara.common.support.assertions.MediaAssertions;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 媒体上传流程测试
 * <p>
 * 业务流程:
 * 1. 用户选择文件
 * 2. 前端验证文件类型和大小
 * 3. 上传文件 (POST /api/media/upload)
 * 4. 后端处理:
 *    - 验证文件
 *    - 计算MD5 (检查秒传)
 *    - 压缩/处理
 *    - 上传OSS
 * 5. 返回文件URL
 * 6. 前端显示上传成功
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("媒体上传流程测试")
class MediaUploadFlowTest extends FlowTestBase {

    private static final long ONE_MB = 1024 * 1024;
    private static final long TEN_MB = 10 * ONE_MB;
    private static final long HUNDRED_MB = 100 * ONE_MB;

    /**
     * 场景1: 图片上传完整流程
     * <p>
     * 步骤:
     * 1. 创建测试图片文件 (1MB)
     * 2. 上传图片
     * 3. 验证返回: fileId, fileUrl, fileName, fileSize, thumbnail
     * 4. 验证文件: OSS中文件存在, 数据库记录存在, 缩略图生成成功
     */
    @Test
    @Order(1)
    @DisplayName("图片上传完整流程")
    void testImageUploadFlow() {
        // Given: 创建测试图片文件 (1MB)
        File imageFile = dataBuilder.createTestImageFile("test-image.jpg", ONE_MB);

        // When: 上传图片
        Response response = uploadImage(imageFile, "post");

        // Then: 验证响应
        MediaAssertions.assertImageUploadSuccess(response);

        // 提取上传结果
        Long fileId = response.jsonPath().getLong("data.fileId");
        String fileUrl = response.jsonPath().getString("data.fileUrl");
        String fileName = response.jsonPath().getString("data.fileName");
        Long fileSize = response.jsonPath().getLong("data.fileSize");
        String md5 = response.jsonPath().getString("data.md5");
        String thumbnail = response.jsonPath().getString("data.thumbnail");

        // 验证文件ID
        assertThat(fileId).isNotNull();
        assertThat(fileId).isGreaterThan(0);

        // 验证文件URL
        MediaAssertions.assertValidFileUrl(fileUrl);

        // 验证文件名
        assertThat(fileName).isNotBlank();

        // 验证文件大小
        assertThat(fileSize).isGreaterThan(0);

        // 验证MD5
        MediaAssertions.assertValidMd5(md5);

        // 验证缩略图
        MediaAssertions.assertThumbnailGenerated(response);
        assertThat(thumbnail).isNotEqualTo(fileUrl);  // 缩略图URL应该不同于原图

        // TODO: 验证OSS中文件存在
        // TODO: 验证数据库记录存在
    }

    /**
     * 场景2: 视频上传流程
     * <p>
     * 上传视频文件，验证封面图生成
     */
    @Test
    @Order(2)
    @DisplayName("视频上传流程")
    void testVideoUploadFlow() {
        // Given: 创建测试视频文件 (5MB)
        File videoFile = dataBuilder.createTestVideoFile("test-video.mp4", 5 * ONE_MB);

        // When: 上传视频
        Response response = uploadVideo(videoFile, "post");

        // Then: 验证响应
        MediaAssertions.assertVideoUploadSuccess(response);

        // 验证视频特定字段
        String fileUrl = response.jsonPath().getString("data.fileUrl");
        String thumbnail = response.jsonPath().getString("data.thumbnail");

        assertThat(fileUrl).contains(".mp4");
        assertThat(thumbnail).isNotNull();

        // 视频应该有封面图 (缩略图)
        MediaAssertions.assertThumbnailGenerated(response);

        // TODO: 验证封面图可访问
    }

    /**
     * 场景3: MD5秒传
     * <p>
     * 上传相同文件两次，验证秒传功能
     */
    @Test
    @Order(3)
    @DisplayName("MD5秒传")
    void testInstantUploadWithMD5() {
        // Given: 创建测试图片
        File imageFile = dataBuilder.createTestImageFile("test-instant.jpg", ONE_MB);

        // When: 第一次上传
        Response firstResponse = uploadImage(imageFile, "post");
        MediaAssertions.assertImageUploadSuccess(firstResponse);

        String firstFileUrl = firstResponse.jsonPath().getString("data.fileUrl");
        String firstMd5 = firstResponse.jsonPath().getString("data.md5");

        // When: 第二次上传相同文件
        Response secondResponse = uploadImage(imageFile, "post");
        MediaAssertions.assertImageUploadSuccess(secondResponse);

        String secondFileUrl = secondResponse.jsonPath().getString("data.fileUrl");
        String secondMd5 = secondResponse.jsonPath().getString("data.md5");

        // Then: 验证秒传成功
        // 两次上传返回相同的URL (秒传)
        MediaAssertions.assertInstantUploadSuccess(firstFileUrl, secondFileUrl);

        // MD5应该相同
        assertThat(firstMd5).isEqualTo(secondMd5);

        // 第二次上传应该更快 (因为是秒传)
        // TODO: 可以比较响应时间验证
    }

    /**
     * 场景4: 文件大小限制
     * <p>
     * 测试超过大小限制的文件
     */
    @Test
    @Order(4)
    @DisplayName("文件大小限制")
    void testFileSizeLimit() {
        // Given: 创建超大图片 (11MB，超过10MB限制)
        File largeImage = dataBuilder.createTestImageFile("large-image.jpg", 11 * ONE_MB);

        // When: 尝试上传
        Response response = uploadImage(largeImage, "post");

        // Then: 应该返回错误
        MediaAssertions.assertFileTooLargeError(response);

        // Given: 创建超大视频 (101MB，超过100MB限制)
        File largeVideo = dataBuilder.createTestVideoFile("large-video.mp4", 101 * ONE_MB);

        // When: 尝试上传
        Response videoResponse = uploadVideo(largeVideo, "post");

        // Then: 应该返回错误
        MediaAssertions.assertFileTooLargeError(videoResponse);
    }

    /**
     * 场景5: 文件类型验证
     * <p>
     * 测试不支持的文件类型
     */
    @Test
    @Order(5)
    @DisplayName("文件类型验证")
    void testFileTypeValidation() {
        // Given: 创建不支持的文件类型 (.exe)
        File exeFile = dataBuilder.createTestFile("test.exe", ONE_MB);

        // When: 尝试上传
        Response response = uploadImage(exeFile, "post");

        // Then: 应该返回错误
        MediaAssertions.assertUnsupportedFileTypeError(response);
    }

    /**
     * 场景6: 图片压缩验证
     * <p>
     * 上传大图片，验证压缩功能
     */
    @Test
    @Order(6)
    @DisplayName("图片压缩验证")
    void testImageCompression() {
        // Given: 创建5MB高分辨率图片
        File largeImage = dataBuilder.createTestImageFile(
            "large-resolution.jpg",
            5 * ONE_MB,
            4000,  // 宽度
            3000   // 高度
        );

        long originalSize = largeImage.length();

        // When: 上传图片
        Response response = uploadImage(largeImage, "post");

        // Then: 验证上传成功
        MediaAssertions.assertImageUploadSuccess(response);

        // 获取上传后的文件大小
        Long uploadedSize = response.jsonPath().getLong("data.fileSize");

        // 验证压缩效果
        // 注意: 压缩后可能不一定更小，取决于原图质量
        // 这里只验证大小合理即可
        assertThat(uploadedSize).isGreaterThan(0);

        // 如果配置了压缩，大图应该被压缩
        if (originalSize > 2 * ONE_MB) {
            // 对于超过2MB的图片，压缩后应该小于等于原大小
            MediaAssertions.assertImageCompressed(originalSize, uploadedSize);
        }
    }

    /**
     * 场景7: 业务关联
     * <p>
     * 上传图片后，关联到具体业务
     */
    @Test
    @Order(7)
    @DisplayName("业务关联")
    void testFileBusinessBinding() {
        // Given: 上传图片
        File imageFile = dataBuilder.createTestImageFile("business-image.jpg", ONE_MB);
        Response uploadResponse = uploadImage(imageFile, "post");

        MediaAssertions.assertImageUploadSuccess(uploadResponse);

        Long fileId = uploadResponse.jsonPath().getLong("data.fileId");
        String fileUrl = uploadResponse.jsonPath().getString("data.fileUrl");

        // When: 关联到帖子 (bizType="post", bizId=1001)
        Response bindResponse = bindFileToBusiness(fileId, "post", 1001L);

        // Then: 验证关联成功
        assertThat(bindResponse.statusCode()).isEqualTo(200);
        assertThat(bindResponse.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(bindResponse.jsonPath().getBoolean("data.success")).isTrue();

        // TODO: 验证数据库中的关联记录
    }

    /**
     * 场景8: 批量上传
     * <p>
     * 模拟用户一次选择多张图片上传
     */
    @Test
    @Order(8)
    @DisplayName("批量上传")
    void testBatchUpload() {
        // Given: 创建3张测试图片
        File image1 = dataBuilder.createTestImageFile("batch-1.jpg", ONE_MB);
        File image2 = dataBuilder.createTestImageFile("batch-2.jpg", ONE_MB);
        File image3 = dataBuilder.createTestImageFile("batch-3.jpg", ONE_MB);

        // When: 依次上传3张图片
        Response response1 = uploadImage(image1, "post");
        Response response2 = uploadImage(image2, "post");
        Response response3 = uploadImage(image3, "post");

        // Then: 验证都上传成功
        MediaAssertions.assertImageUploadSuccess(response1);
        MediaAssertions.assertImageUploadSuccess(response2);
        MediaAssertions.assertImageUploadSuccess(response3);

        // 验证返回的fileId不同
        Long fileId1 = response1.jsonPath().getLong("data.fileId");
        Long fileId2 = response2.jsonPath().getLong("data.fileId");
        Long fileId3 = response3.jsonPath().getLong("data.fileId");

        assertThat(fileId1).isNotEqualTo(fileId2);
        assertThat(fileId2).isNotEqualTo(fileId3);
        assertThat(fileId1).isNotEqualTo(fileId3);
    }

    /**
     * 场景9: 并发上传测试
     * <p>
     * 测试并发上传的稳定性
     */
    @Test
    @Order(9)
    @DisplayName("并发上传测试")
    void testConcurrentUpload() throws InterruptedException {
        // Given: 准备并发数量
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // When: 并发上传5个文件
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    File image = dataBuilder.createTestImageFile(
                        "concurrent-" + index + ".jpg",
                        500 * 1024  // 500KB
                    );

                    Response response = uploadImage(image, "post");

                    if (response.statusCode() == 200 &&
                        response.jsonPath().getInt("code") == 200) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        // Then: 等待所有线程完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // 验证所有上传都成功
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(errorCount.get()).isEqualTo(0);
    }

    /**
     * 场景10: 未登录上传
     * <p>
     * 测试未认证用户尝试上传
     */
    @Test
    @Order(10)
    @DisplayName("未登录上传")
    void testUploadWithoutAuth() {
        // Given: 创建测试图片
        File imageFile = dataBuilder.createTestImageFile("no-auth.jpg", ONE_MB);

        // When: 不带Token上传
        Response response = unauthenticatedRequest()
            .multiPart("file", imageFile, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 应该返回401未认证错误
        assertThat(response.statusCode()).isEqualTo(401);
    }
}
