package org.dromara.common.support.assertions;

import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 媒体服务断言工具类
 * <p>
 * 提供媒体上传相关的自定义断言方法
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
public class MediaAssertions {

    /**
     * 断言图片上传成功
     *
     * @param response API响应
     */
    public static void assertImageUploadSuccess(Response response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getLong("data.fileId")).isNotNull();
        assertThat(response.jsonPath().getString("data.fileUrl")).isNotNull();
        assertThat(response.jsonPath().getString("data.fileUrl")).startsWith("http");
        assertThat(response.jsonPath().getString("data.fileName")).isNotNull();
        assertThat(response.jsonPath().getLong("data.fileSize")).isGreaterThan(0);
        assertThat(response.jsonPath().getString("data.md5")).isNotNull();
    }

    /**
     * 断言视频上传成功
     *
     * @param response API响应
     */
    public static void assertVideoUploadSuccess(Response response) {
        assertImageUploadSuccess(response);

        // 视频应该有封面图
        assertThat(response.jsonPath().getString("data.thumbnail")).isNotNull();
    }

    /**
     * 断言文件URL有效
     *
     * @param fileUrl 文件URL
     */
    public static void assertValidFileUrl(String fileUrl) {
        assertThat(fileUrl).isNotNull();
        assertThat(fileUrl).startsWith("http");
        assertThat(fileUrl).contains(".");  // 应该有文件扩展名
    }

    /**
     * 断言MD5格式正确
     *
     * @param md5 MD5值
     */
    public static void assertValidMd5(String md5) {
        assertThat(md5).isNotNull();
        assertThat(md5).hasSize(32);  // MD5是32位十六进制字符串
        assertThat(md5).matches("[a-f0-9]{32}");
    }

    /**
     * 断言文件大小在限制范围内
     *
     * @param fileSize 文件大小(字节)
     * @param maxSize  最大限制
     */
    public static void assertFileSizeWithinLimit(long fileSize, long maxSize) {
        assertThat(fileSize).isGreaterThan(0);
        assertThat(fileSize).isLessThanOrEqualTo(maxSize);
    }

    /**
     * 断言图片上传失败 (文件过大)
     *
     * @param response API响应
     */
    public static void assertFileTooLargeError(Response response) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        assertThat(response.jsonPath().getString("msg")).contains("文件大小超过限制");
    }

    /**
     * 断言文件类型不支持
     *
     * @param response API响应
     */
    public static void assertUnsupportedFileTypeError(Response response) {
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        assertThat(response.jsonPath().getString("msg")).contains("不支持的文件类型");
    }

    /**
     * 断言图片压缩成功
     *
     * @param originalSize  原始大小
     * @param compressedSize 压缩后大小
     */
    public static void assertImageCompressed(long originalSize, long compressedSize) {
        // 压缩后应该小于或等于原始大小
        assertThat(compressedSize).isLessThanOrEqualTo(originalSize);
    }

    /**
     * 断言缩略图生成成功
     *
     * @param response API响应
     */
    public static void assertThumbnailGenerated(Response response) {
        String thumbnail = response.jsonPath().getString("data.thumbnail");
        assertThat(thumbnail).isNotNull();
        assertThat(thumbnail).startsWith("http");
        assertThat(thumbnail).contains("thumb");
    }

    /**
     * 断言秒传成功 (两次上传返回相同URL)
     *
     * @param firstUrl  第一次上传的URL
     * @param secondUrl 第二次上传的URL
     */
    public static void assertInstantUploadSuccess(String firstUrl, String secondUrl) {
        assertThat(firstUrl).isEqualTo(secondUrl);
    }
}
