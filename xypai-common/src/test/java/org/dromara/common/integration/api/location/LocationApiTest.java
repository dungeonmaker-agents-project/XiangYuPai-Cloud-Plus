package org.dromara.common.integration.api.location;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.dromara.common.support.assertions.LocationAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 位置API测试
 * <p>
 * 测试API:
 * - POST /api/location/detect - GPS定位
 * - GET /api/location/distance - 距离计算
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("位置API测试")
class LocationApiTest extends ApiTestBase {

    /**
     * 测试用例1: GPS定位成功
     */
    @Test
    @DisplayName("GPS定位成功")
    void testDetectLocation_Success() {
        // Given: 北京天安门坐标
        BigDecimal[] beijing = dataBuilder.beijingCoordinates();
        String requestBody = String.format(
            "{\"latitude\":%s,\"longitude\":%s}",
            beijing[0].toString(), beijing[1].toString()
        );

        // When: 调用定位接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/detect");

        // Then: 验证返回北京
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证返回数据
        assertThat(response.jsonPath().getString("data.cityName")).contains("北京");
        assertThat(response.jsonPath().getString("data.cityCode")).isEqualTo("110100");
        assertThat(response.jsonPath().getString("data.province")).isNotBlank();
        assertThat(response.jsonPath().getString("data.formattedAddress")).isNotBlank();
    }

    /**
     * 测试用例2: 无效坐标
     */
    @Test
    @DisplayName("无效坐标")
    void testDetectLocation_InvalidCoordinates() {
        // Given: 无效坐标 (超出范围)
        String requestBody = "{\"latitude\":999.0,\"longitude\":999.0}";

        // When: 调用定位接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/detect");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("坐标");
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例3: 缺少坐标参数
     */
    @Test
    @DisplayName("缺少坐标参数")
    void testDetectLocation_MissingParams() {
        // Given: 只有纬度，缺少经度
        String requestBody = "{\"latitude\":39.9}";

        // When: 调用定位接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/detect");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例4: 不同城市定位验证
     */
    @Test
    @DisplayName("不同城市定位验证")
    void testDetectLocation_DifferentCities() {
        // Test 1: 上海
        BigDecimal[] shanghai = dataBuilder.shanghaiCoordinates();
        String shanghaiBody = String.format(
            "{\"latitude\":%s,\"longitude\":%s}",
            shanghai[0].toString(), shanghai[1].toString()
        );

        Response shanghaiResponse = authenticatedRequest()
            .body(shanghaiBody)
            .when()
            .post("/api/location/detect");

        assertThat(shanghaiResponse.statusCode()).isEqualTo(200);
        assertThat(shanghaiResponse.jsonPath().getString("data.cityName")).contains("上海");

        // Test 2: 深圳
        BigDecimal[] shenzhen = dataBuilder.shenzhenCoordinates();
        String shenzhenBody = String.format(
            "{\"latitude\":%s,\"longitude\":%s}",
            shenzhen[0].toString(), shenzhen[1].toString()
        );

        Response shenzhenResponse = authenticatedRequest()
            .body(shenzhenBody)
            .when()
            .post("/api/location/detect");

        assertThat(shenzhenResponse.statusCode()).isEqualTo(200);
        assertThat(shenzhenResponse.jsonPath().getString("data.cityName")).contains("深圳");
    }

    /**
     * 测试用例5: 计算距离
     */
    @Test
    @DisplayName("计算距离")
    void testCalculateDistance() {
        // Given: 两个坐标点 (北京到上海)
        BigDecimal[] beijing = dataBuilder.beijingCoordinates();
        BigDecimal[] shanghai = dataBuilder.shanghaiCoordinates();

        String requestBody = String.format(
            "{\"fromLat\":%s,\"fromLng\":%s,\"toLat\":%s,\"toLng\":%s}",
            beijing[0].toString(), beijing[1].toString(),
            shanghai[0].toString(), shanghai[1].toString()
        );

        // When: 计算距离
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/distance");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证距离合理 (北京到上海约1000公里)
        BigDecimal distance = new BigDecimal(response.jsonPath().getString("data.distance"));
        LocationAssertions.assertReasonableDistance(distance);

        // 距离应该在800-1500公里之间 (800000-1500000米)
        assertThat(distance.doubleValue()).isBetween(800000.0, 1500000.0);

        // 验证格式化文本
        String displayText = response.jsonPath().getString("data.displayText");
        assertThat(displayText).isNotBlank();
        assertThat(displayText).containsAnyOf("km", "公里", "米");
    }

    /**
     * 测试用例6: 计算短距离
     */
    @Test
    @DisplayName("计算短距离")
    void testCalculateDistance_ShortDistance() {
        // Given: 相近的两个坐标 (差0.01度，约1公里)
        BigDecimal lat1 = BigDecimal.valueOf(39.9);
        BigDecimal lng1 = BigDecimal.valueOf(116.4);
        BigDecimal lat2 = BigDecimal.valueOf(39.91);
        BigDecimal lng2 = BigDecimal.valueOf(116.41);

        String requestBody = String.format(
            "{\"fromLat\":%s,\"fromLng\":%s,\"toLat\":%s,\"toLng\":%s}",
            lat1.toString(), lng1.toString(), lat2.toString(), lng2.toString()
        );

        // When: 计算距离
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/distance");

        // Then: 验证距离合理 (应该在几百米到几公里之间)
        assertThat(response.statusCode()).isEqualTo(200);

        BigDecimal distance = new BigDecimal(response.jsonPath().getString("data.distance"));
        LocationAssertions.assertReasonableDistance(distance);

        // 短距离应该小于5公里 (5000米)
        assertThat(distance.doubleValue()).isLessThan(5000.0);
    }

    /**
     * 测试用例7: 相同坐标距离为0
     */
    @Test
    @DisplayName("相同坐标距离为0")
    void testCalculateDistance_SameLocation() {
        // Given: 相同坐标
        BigDecimal lat = BigDecimal.valueOf(39.9);
        BigDecimal lng = BigDecimal.valueOf(116.4);

        String requestBody = String.format(
            "{\"fromLat\":%s,\"fromLng\":%s,\"toLat\":%s,\"toLng\":%s}",
            lat.toString(), lng.toString(), lat.toString(), lng.toString()
        );

        // When: 计算距离
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/distance");

        // Then: 距离应该为0或非常接近0
        assertThat(response.statusCode()).isEqualTo(200);

        BigDecimal distance = new BigDecimal(response.jsonPath().getString("data.distance"));
        assertThat(distance.doubleValue()).isLessThan(1.0);  // 小于1米
    }

    /**
     * 测试用例8: 坐标验证
     */
    @Test
    @DisplayName("坐标验证")
    void testValidateCoordinates() {
        // Given: 有效坐标
        BigDecimal validLat = BigDecimal.valueOf(39.9);
        BigDecimal validLng = BigDecimal.valueOf(116.4);

        // When & Then: 验证有效坐标
        LocationAssertions.assertValidCoordinates(validLat, validLng);

        // 验证边界坐标
        LocationAssertions.assertValidCoordinates(BigDecimal.valueOf(90.0), BigDecimal.valueOf(180.0));
        LocationAssertions.assertValidCoordinates(BigDecimal.valueOf(-90.0), BigDecimal.valueOf(-180.0));
    }
}
