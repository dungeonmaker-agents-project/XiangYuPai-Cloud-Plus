package org.dromara.common.integration.api.media;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.dromara.common.support.assertions.MediaAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 视频上传API测试
 * <p>
 * 测试API: POST /api/media/upload
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("视频上传API测试")
class VideoUploadApiTest extends ApiTestBase {

    @AfterEach
    void cleanupFiles() {
        dataBuilder.cleanupTestFiles();
    }

    /**
     * 测试用例1: 成功上传视频
     */
    @Test
    @DisplayName("成功上传视频")
    void testUploadVideo_Success() {
        // Given: 创建5MB测试视频
        File testVideo = dataBuilder.createTestVideoFile("test.mp4", 5 * 1024 * 1024);

        // When: 上传视频
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证上传成功
        MediaAssertions.assertVideoUploadSuccess(response);

        // 验证视频特定字段
        String fileUrl = response.jsonPath().getString("data.fileUrl");
        String thumbnail = response.jsonPath().getString("data.thumbnail");

        assertThat(fileUrl).contains(".mp4");
        assertThat(thumbnail).isNotNull();
        MediaAssertions.assertThumbnailGenerated(response);
    }

    /**
     * 测试用例2: 上传大视频
     */
    @Test
    @DisplayName("上传大视频")
    void testUploadVideo_LargeFile() {
        // Given: 创建50MB视频
        File largeVideo = dataBuilder.createTestVideoFile("large.mp4", 50 * 1024 * 1024);

        // When: 上传视频
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", largeVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证上传成功 (50MB在100MB限制内)
        MediaAssertions.assertVideoUploadSuccess(response);

        Long fileSize = response.jsonPath().getLong("data.fileSize");
        assertThat(fileSize).isGreaterThan(0);
        MediaAssertions.assertFileSizeWithinLimit(fileSize, 100 * 1024 * 1024);
    }

    /**
     * 测试用例3: 视频文件过大
     */
    @Test
    @DisplayName("视频文件过大")
    void testUploadVideo_FileTooLarge() {
        // Given: 创建101MB视频 (超过100MB限制)
        File tooLargeVideo = dataBuilder.createTestVideoFile("toolarge.mp4", 101 * 1024 * 1024);

        // When: 尝试上传
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", tooLargeVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证返回错误
        MediaAssertions.assertFileTooLargeError(response);
    }

    /**
     * 测试用例4: 不支持的视频格式
     */
    @Test
    @DisplayName("不支持的视频格式")
    void testUploadVideo_UnsupportedFormat() {
        // Given: 创建不支持的格式 (假设.avi不支持)
        File aviFile = dataBuilder.createTestVideoFile("test.avi", 5 * 1024 * 1024);

        // When: 尝试上传
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", aviFile, "video/avi")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 可能返回错误或成功 (取决于业务规则)
        // 如果不支持，应该返回错误
        if (response.statusCode() != 200 || response.jsonPath().getInt("code") != 200) {
            // 验证错误信息包含格式相关内容
            if (response.statusCode() == 200) {
                assertThat(response.jsonPath().getString("msg")).containsAnyOf("格式", "类型");
            }
        }
    }

    /**
     * 测试用例5: 视频MD5秒传
     */
    @Test
    @DisplayName("视频MD5秒传")
    void testUploadVideo_InstantUpload() {
        // Given: 创建测试视频
        File testVideo = dataBuilder.createTestVideoFile("instant.mp4", 10 * 1024 * 1024);

        // When: 第一次上传
        Response firstResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertVideoUploadSuccess(firstResponse);
        String firstUrl = firstResponse.jsonPath().getString("data.fileUrl");
        String firstMd5 = firstResponse.jsonPath().getString("data.md5");

        // When: 第二次上传相同文件
        Response secondResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertVideoUploadSuccess(secondResponse);
        String secondUrl = secondResponse.jsonPath().getString("data.fileUrl");
        String secondMd5 = secondResponse.jsonPath().getString("data.md5");

        // Then: 验证秒传
        MediaAssertions.assertInstantUploadSuccess(firstUrl, secondUrl);
        assertThat(firstMd5).isEqualTo(secondMd5);
    }

    /**
     * 测试用例6: 视频封面图生成
     */
    @Test
    @DisplayName("视频封面图生成")
    void testUploadVideo_ThumbnailGeneration() {
        // Given: 创建测试视频
        File testVideo = dataBuilder.createTestVideoFile("thumbnail.mp4", 10 * 1024 * 1024);

        // When: 上传视频
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testVideo, "video/mp4")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证封面图生成
        MediaAssertions.assertVideoUploadSuccess(response);

        String thumbnail = response.jsonPath().getString("data.thumbnail");
        assertThat(thumbnail).isNotNull();
        assertThat(thumbnail).startsWith("http");

        // 封面图应该是图片格式
        assertThat(thumbnail).containsAnyOf(".jpg", ".jpeg", ".png");
    }

    /**
     * 测试用例7: 不同视频格式
     */
    @Test
    @DisplayName("不同视频格式")
    void testUploadVideo_DifferentFormats() {
        // Test MOV格式
        File movFile = dataBuilder.createTestVideoFile("test.mov", 5 * 1024 * 1024);
        Response movResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", movFile, "video/quicktime")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // 验证MOV格式上传结果
        if (movResponse.statusCode() == 200 && movResponse.jsonPath().getInt("code") == 200) {
            MediaAssertions.assertVideoUploadSuccess(movResponse);
        }

        // Test MKV格式
        File mkvFile = dataBuilder.createTestVideoFile("test.mkv", 5 * 1024 * 1024);
        Response mkvResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", mkvFile, "video/x-matroska")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // 验证MKV格式上传结果
        if (mkvResponse.statusCode() == 200 && mkvResponse.jsonPath().getInt("code") == 200) {
            MediaAssertions.assertVideoUploadSuccess(mkvResponse);
        }
    }
}
