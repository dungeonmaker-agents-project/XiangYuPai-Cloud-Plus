package org.dromara.common.support;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

/**
 * 流程测试基类
 * <p>
 * 提供前端页面流程测试的通用方法:
 * - 模拟前端API调用
 * - 业务流程验证
 * - 数据准备和清理
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
public abstract class FlowTestBase extends ApiTestBase {

    @Autowired
    protected TestDataBuilder dataBuilder;

    // ==================== 位置服务相关方法 ====================

    /**
     * 获取城市列表
     *
     * @return API响应
     */
    protected Response getCityList() {
        return authenticatedRequest()
            .when()
            .get("/api/city/list");
    }

    /**
     * 选择城市
     *
     * @param cityCode 城市代码
     * @param cityName 城市名称
     * @param source   来源 (manual/gps/recent/hot)
     * @return API响应
     */
    protected Response selectCity(String cityCode, String cityName, String source) {
        String requestBody = String.format(
            "{\"cityCode\":\"%s\",\"cityName\":\"%s\",\"source\":\"%s\"}",
            cityCode, cityName, source
        );

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/city/select");
    }

    /**
     * 获取区域列表
     *
     * @param cityCode 城市代码
     * @return API响应
     */
    protected Response getDistrictList(String cityCode) {
        return authenticatedRequest()
            .param("cityCode", cityCode)
            .when()
            .get("/api/location/districts");
    }

    /**
     * 选择区域
     *
     * @param cityCode     城市代码
     * @param districtCode 区域代码 (可以是 "all" 表示全城)
     * @return API响应
     */
    protected Response selectDistrict(String cityCode, String districtCode) {
        String requestBody = String.format(
            "{\"cityCode\":\"%s\",\"districtCode\":\"%s\"}",
            cityCode, districtCode
        );

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/district/select");
    }

    /**
     * GPS定位
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return API响应
     */
    protected Response detectLocation(BigDecimal latitude, BigDecimal longitude) {
        String requestBody = String.format(
            "{\"latitude\":%s,\"longitude\":%s}",
            latitude.toString(), longitude.toString()
        );

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/detect");
    }

    // ==================== 媒体服务相关方法 ====================

    /**
     * 上传图片
     *
     * @param file    图片文件
     * @param bizType 业务类型
     * @return API响应
     */
    protected Response uploadImage(File file, String bizType) {
        return given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", file, "image/jpeg")
            .multiPart("bizType", bizType)
            .when()
            .post("/api/media/upload");
    }

    /**
     * 上传视频
     *
     * @param file    视频文件
     * @param bizType 业务类型
     * @return API响应
     */
    protected Response uploadVideo(File file, String bizType) {
        return given()
            .header("Authorization", "Bearer " + userToken)
            .multiPart("file", file, "video/mp4")
            .multiPart("bizType", bizType)
            .when()
            .post("/api/media/upload");
    }

    /**
     * 绑定文件到业务
     *
     * @param fileId  文件ID
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return API响应
     */
    protected Response bindFileToBusiness(Long fileId, String bizType, Long bizId) {
        String requestBody = String.format(
            "{\"fileId\":%d,\"bizType\":\"%s\",\"bizId\":%d}",
            fileId, bizType, bizId
        );

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/media/bind");
    }

    // ==================== 通知服务相关方法 ====================

    /**
     * 获取未读通知数
     *
     * @return API响应
     */
    protected Response getUnreadCount() {
        return authenticatedRequest()
            .when()
            .get("/api/notification/unread-count");
    }

    /**
     * 获取通知列表
     *
     * @param type    通知类型 (like/comment/follow/system)
     * @param pageNum 页码
     * @return API响应
     */
    protected Response getNotificationList(String type, int pageNum) {
        return authenticatedRequest()
            .param("type", type)
            .param("pageNum", pageNum)
            .param("pageSize", 20)
            .when()
            .get("/api/notification/list");
    }

    /**
     * 标记通知已读
     *
     * @param notificationId 通知ID
     * @return API响应
     */
    protected Response markNotificationAsRead(Long notificationId) {
        return authenticatedRequest()
            .when()
            .put("/api/notification/read/" + notificationId);
    }

    /**
     * 批量标记已读
     *
     * @param notificationIds 通知ID列表
     * @return API响应
     */
    protected Response batchMarkAsRead(Long[] notificationIds) {
        StringBuilder idsJson = new StringBuilder("[");
        for (int i = 0; i < notificationIds.length; i++) {
            idsJson.append(notificationIds[i]);
            if (i < notificationIds.length - 1) {
                idsJson.append(",");
            }
        }
        idsJson.append("]");

        String requestBody = "{\"ids\":" + idsJson + "}";

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .put("/api/notification/batch-read");
    }

    /**
     * 全部标记已读
     *
     * @param type 通知类型
     * @return API响应
     */
    protected Response markAllAsRead(String type) {
        return authenticatedRequest()
            .param("type", type)
            .when()
            .put("/api/notification/read-all");
    }

    /**
     * 清除已读通知
     *
     * @param type 通知类型
     * @return API响应
     */
    protected Response clearReadNotifications(String type) {
        return authenticatedRequest()
            .param("type", type)
            .when()
            .delete("/api/notification/clear");
    }

    // ==================== 举报服务相关方法 ====================

    /**
     * 提交举报
     *
     * @param targetType 目标类型 (post/comment/user)
     * @param targetId   目标ID
     * @param reason     举报原因
     * @param content    举报内容
     * @return API响应
     */
    protected Response submitReport(String targetType, Long targetId, String reason, String content) {
        String requestBody = String.format(
            "{\"targetType\":\"%s\",\"targetId\":%d,\"reason\":\"%s\",\"content\":\"%s\"}",
            targetType, targetId, reason, content
        );

        return authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");
    }
}
