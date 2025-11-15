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
 * 图片上传API测试
 * <p>
 * 测试API: POST /api/media/upload
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("图片上传API测试")
class ImageUploadApiTest extends ApiTestBase {

    @AfterEach
    void cleanupFiles() {
        dataBuilder.cleanupTestFiles();
    }

    /**
     * 测试用例1: 成功上传图片
     */
    @Test
    @DisplayName("成功上传图片")
    void testUploadImage_Success() {
        // Given: 创建1MB测试图片
        File testImage = dataBuilder.createTestImageFile("test.jpg", 1024 * 1024);

        // When: 上传图片
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证上传成功
        MediaAssertions.assertImageUploadSuccess(response);

        // 验证返回字段
        Long fileId = response.jsonPath().getLong("data.fileId");
        String fileUrl = response.jsonPath().getString("data.fileUrl");
        String md5 = response.jsonPath().getString("data.md5");

        assertThat(fileId).isGreaterThan(0);
        MediaAssertions.assertValidFileUrl(fileUrl);
        MediaAssertions.assertValidMd5(md5);
    }

    /**
     * 测试用例2: 上传PNG格式图片
     */
    @Test
    @DisplayName("上传PNG格式图片")
    void testUploadImage_PngFormat() {
        // Given: 创建PNG图片
        File testImage = dataBuilder.createTestImageFile("test.png", 500 * 1024);

        // When: 上传图片
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/png")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证上传成功
        MediaAssertions.assertImageUploadSuccess(response);

        String fileUrl = response.jsonPath().getString("data.fileUrl");
        assertThat(fileUrl).containsAnyOf(".png", ".jpg", ".jpeg");  // 可能被转换
    }

    /**
     * 测试用例3: 文件过大
     */
    @Test
    @DisplayName("文件过大")
    void testUploadImage_FileTooLarge() {
        // Given: 创建11MB图片 (超过10MB限制)
        File largeImage = dataBuilder.createTestImageFile("large.jpg", 11 * 1024 * 1024);

        // When: 尝试上传
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", largeImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证返回错误
        MediaAssertions.assertFileTooLargeError(response);
    }

    /**
     * 测试用例4: 不支持的文件类型
     */
    @Test
    @DisplayName("不支持的文件类型")
    void testUploadImage_UnsupportedType() {
        // Given: 创建exe文件
        File exeFile = dataBuilder.createTestFile("test.exe", 1024);

        // When: 尝试上传
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", exeFile, "application/exe")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证返回错误
        MediaAssertions.assertUnsupportedFileTypeError(response);
    }

    /**
     * 测试用例5: MD5秒传
     */
    @Test
    @DisplayName("MD5秒传")
    void testUploadImage_InstantUpload() {
        // Given: 创建测试图片
        File testImage = dataBuilder.createTestImageFile("instant.jpg", 1024 * 1024);

        // When: 第一次上传
        Response firstResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(firstResponse);
        String firstUrl = firstResponse.jsonPath().getString("data.fileUrl");

        // When: 第二次上传相同文件
        Response secondResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(secondResponse);
        String secondUrl = secondResponse.jsonPath().getString("data.fileUrl");

        // Then: 验证秒传 (返回相同URL)
        MediaAssertions.assertInstantUploadSuccess(firstUrl, secondUrl);
    }

    /**
     * 测试用例6: 缩略图生成
     */
    @Test
    @DisplayName("缩略图生成")
    void testUploadImage_ThumbnailGeneration() {
        // Given: 创建测试图片
        File testImage = dataBuilder.createTestImageFile("thumb.jpg", 2 * 1024 * 1024, 1920, 1080);

        // When: 上传图片
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证缩略图生成
        MediaAssertions.assertImageUploadSuccess(response);
        MediaAssertions.assertThumbnailGenerated(response);

        String fileUrl = response.jsonPath().getString("data.fileUrl");
        String thumbnail = response.jsonPath().getString("data.thumbnail");

        assertThat(thumbnail).isNotEqualTo(fileUrl);
        assertThat(thumbnail).contains("thumb");
    }

    /**
     * 测试用例7: 图片压缩
     */
    @Test
    @DisplayName("图片压缩")
    void testUploadImage_Compression() {
        // Given: 创建5MB高分辨率图片
        File largeImage = dataBuilder.createTestImageFile("compress.jpg", 5 * 1024 * 1024, 4000, 3000);
        long originalSize = largeImage.length();

        // When: 上传图片
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", largeImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证上传成功
        MediaAssertions.assertImageUploadSuccess(response);

        Long uploadedSize = response.jsonPath().getLong("data.fileSize");

        // 验证压缩效果
        if (originalSize > 2 * 1024 * 1024) {
            MediaAssertions.assertImageCompressed(originalSize, uploadedSize);
        }
    }

    /**
     * 测试用例8: 未登录上传
     */
    @Test
    @DisplayName("未登录上传")
    void testUploadImage_Unauthorized() {
        // Given: 创建测试图片
        File testImage = dataBuilder.createTestImageFile("unauthorized.jpg", 1024 * 1024);

        // When: 不带Token上传
        Response response = unauthenticatedRequest()
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 验证返回401错误
        assertThat(response.statusCode()).isEqualTo(401);
    }

    /**
     * 测试用例9: 缺少bizType参数
     */
    @Test
    @DisplayName("缺少bizType参数")
    void testUploadImage_MissingBizType() {
        // Given: 创建测试图片
        File testImage = dataBuilder.createTestImageFile("no-biztype.jpg", 1024 * 1024);

        // When: 不传bizType参数
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .when()
            .post("/api/media/upload");

        // Then: 可能返回错误，或使用默认bizType
        // 根据实际业务逻辑调整
        if (response.statusCode() != 200) {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例10: 空文件
     */
    @Test
    @DisplayName("空文件")
    void testUploadImage_EmptyFile() {
        // Given: 创建空文件
        File emptyFile = dataBuilder.createTestFile("empty.jpg", 0);

        // When: 尝试上传
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", emptyFile, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("文件");
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }
}
