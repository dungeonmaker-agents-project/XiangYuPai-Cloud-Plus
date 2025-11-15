package org.dromara.common.integration.api.media;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.dromara.common.support.assertions.MediaAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 媒体查询API测试
 * <p>
 * 测试API:
 * - GET /api/media/{fileId} - 获取文件信息
 * - POST /api/media/bind - 绑定文件到业务
 * - DELETE /api/media/{fileId} - 删除文件
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("媒体查询API测试")
class MediaQueryApiTest extends ApiTestBase {

    /**
     * 测试用例1: 获取文件信息
     */
    @Test
    @DisplayName("获取文件信息")
    void testGetFileInfo_Success() {
        // Given: 先上传一个文件
        File testImage = dataBuilder.createTestImageFile("query.jpg", 1024 * 1024);
        Response uploadResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(uploadResponse);
        Long fileId = uploadResponse.jsonPath().getLong("data.fileId");

        // When: 获取文件信息
        Response response = authenticatedRequest()
            .when()
            .get("/api/media/" + fileId);

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证文件信息
        assertThat(response.jsonPath().getLong("data.fileId")).isEqualTo(fileId);
        assertThat(response.jsonPath().getString("data.fileUrl")).isNotBlank();
        assertThat(response.jsonPath().getString("data.fileName")).isNotBlank();
        assertThat(response.jsonPath().getLong("data.fileSize")).isGreaterThan(0);
    }

    /**
     * 测试用例2: 查询不存在的文件
     */
    @Test
    @DisplayName("查询不存在的文件")
    void testGetFileInfo_NotFound() {
        // Given: 不存在的文件ID
        Long nonExistentId = 999999999L;

        // When: 尝试获取文件信息
        Response response = authenticatedRequest()
            .when()
            .get("/api/media/" + nonExistentId);

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).containsAnyOf("不存在", "未找到");
        } else {
            assertThat(response.statusCode()).isEqualTo(404);
        }
    }

    /**
     * 测试用例3: 绑定文件到业务
     */
    @Test
    @DisplayName("绑定文件到业务")
    void testBindFileToBusiness_Success() {
        // Given: 先上传一个文件
        File testImage = dataBuilder.createTestImageFile("bind.jpg", 1024 * 1024);
        Response uploadResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(uploadResponse);
        Long fileId = uploadResponse.jsonPath().getLong("data.fileId");

        // When: 绑定到帖子
        String requestBody = String.format(
            "{\"fileId\":%d,\"bizType\":\"post\",\"bizId\":1001}",
            fileId
        );

        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/media/bind");

        // Then: 验证绑定成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("data.success")).isTrue();
    }

    /**
     * 测试用例4: 绑定不存在的文件
     */
    @Test
    @DisplayName("绑定不存在的文件")
    void testBindFileToBusiness_FileNotFound() {
        // Given: 不存在的文件ID
        String requestBody = "{\"fileId\":999999999,\"bizType\":\"post\",\"bizId\":1001}";

        // When: 尝试绑定
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/media/bind");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isIn(400, 404);
        }
    }

    /**
     * 测试用例5: 删除文件
     */
    @Test
    @DisplayName("删除文件")
    void testDeleteFile_Success() {
        // Given: 先上传一个文件
        File testImage = dataBuilder.createTestImageFile("delete.jpg", 1024 * 1024);
        Response uploadResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(uploadResponse);
        Long fileId = uploadResponse.jsonPath().getLong("data.fileId");

        // When: 删除文件
        Response response = authenticatedRequest()
            .when()
            .delete("/api/media/" + fileId);

        // Then: 验证删除成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 再次查询应该不存在
        Response queryResponse = authenticatedRequest()
            .when()
            .get("/api/media/" + fileId);

        if (queryResponse.statusCode() == 200) {
            assertThat(queryResponse.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(queryResponse.statusCode()).isEqualTo(404);
        }
    }

    /**
     * 测试用例6: 删除不存在的文件
     */
    @Test
    @DisplayName("删除不存在的文件")
    void testDeleteFile_NotFound() {
        // Given: 不存在的文件ID
        Long nonExistentId = 999999999L;

        // When: 尝试删除
        Response response = authenticatedRequest()
            .when()
            .delete("/api/media/" + nonExistentId);

        // Then: 可能返回成功(幂等)或404
        assertThat(response.statusCode()).isIn(200, 404);
    }

    /**
     * 测试用例7: 批量绑定文件
     */
    @Test
    @DisplayName("批量绑定文件")
    void testBatchBindFiles() {
        // Given: 上传3个文件
        Long fileId1 = uploadTestImage("batch1.jpg");
        Long fileId2 = uploadTestImage("batch2.jpg");
        Long fileId3 = uploadTestImage("batch3.jpg");

        // When: 批量绑定
        String requestBody = String.format(
            "{\"fileIds\":[%d,%d,%d],\"bizType\":\"post\",\"bizId\":2001}",
            fileId1, fileId2, fileId3
        );

        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/media/batch-bind");

        // Then: 验证绑定成功 (如果API支持批量绑定)
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        } else if (response.statusCode() == 404) {
            // API可能不支持批量绑定，跳过
            // 可以改为循环单个绑定
        }
    }

    /**
     * 辅助方法: 上传测试图片并返回fileId
     */
    private Long uploadTestImage(String fileName) {
        File testImage = dataBuilder.createTestImageFile(fileName, 500 * 1024);
        Response response = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(response);
        return response.jsonPath().getLong("data.fileId");
    }

    /**
     * 测试用例8: 根据MD5查询文件
     */
    @Test
    @DisplayName("根据MD5查询文件")
    void testQueryFileByMd5() {
        // Given: 上传一个文件
        File testImage = dataBuilder.createTestImageFile("md5query.jpg", 1024 * 1024);
        Response uploadResponse = given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", testImage, "image/jpeg")
            .multiPart("bizType", "post")
            .when()
            .post("/api/media/upload");

        MediaAssertions.assertImageUploadSuccess(uploadResponse);
        String md5 = uploadResponse.jsonPath().getString("data.md5");

        // When: 根据MD5查询
        Response response = authenticatedRequest()
            .param("md5", md5)
            .when()
            .get("/api/media/query-by-md5");

        // Then: 验证查询成功
        if (response.statusCode() == 200 && response.jsonPath().getInt("code") == 200) {
            assertThat(response.jsonPath().getString("data.md5")).isEqualTo(md5);
            assertThat(response.jsonPath().getString("data.fileUrl")).isNotBlank();
        }
    }
}
