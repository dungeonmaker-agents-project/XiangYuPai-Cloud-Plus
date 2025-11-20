package org.dromara.common.support.assertions;

import io.restassured.response.Response;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 位置服务断言工具类
 * <p>
 * 提供位置相关的自定义断言方法
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
public class LocationAssertions {

    /**
     * 断言城市列表响应格式正确
     *
     * @param response API响应
     */
    public static void assertCityListResponse(Response response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat((Object) response.jsonPath().get("data")).isNotNull();
        assertThat((Object) response.jsonPath().get("data.hotCities")).isNotNull();
        assertThat((Object) response.jsonPath().get("data.allCities")).isNotNull();
    }

    /**
     * 断言区域列表响应格式正确
     *
     * @param response API响应
     */
    public static void assertDistrictListResponse(Response response) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat((Object) response.jsonPath().get("data")).isNotNull();
        assertThat((Object) response.jsonPath().get("data.cityName")).isNotNull();
        assertThat((Object) response.jsonPath().get("data.districts")).isNotNull();
    }

    /**
     * 断言坐标有效
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    public static void assertValidCoordinates(BigDecimal latitude, BigDecimal longitude) {
        assertThat(latitude).isNotNull();
        assertThat(longitude).isNotNull();

        // 纬度范围: -90 到 90
        assertThat(latitude.doubleValue()).isBetween(-90.0, 90.0);

        // 经度范围: -180 到 180
        assertThat(longitude.doubleValue()).isBetween(-180.0, 180.0);
    }

    /**
     * 断言距离计算结果合理
     *
     * @param distance 距离(米)
     */
    public static void assertReasonableDistance(BigDecimal distance) {
        assertThat(distance).isNotNull();

        // 距离应该是非负数
        assertThat(distance.doubleValue()).isGreaterThanOrEqualTo(0);

        // 距离不应该超过地球周长 (约40000km)
        assertThat(distance.doubleValue()).isLessThan(40000000);
    }

    /**
     * 断言城市选择响应正确
     *
     * @param response     API响应
     * @param expectedCity 期望的城市代码
     */
    public static void assertCitySelectionSuccess(Response response, String expectedCity) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("data.success")).isTrue();
        assertThat(response.jsonPath().getString("data.selectedCity.cityCode")).isEqualTo(expectedCity);
    }

    /**
     * 断言区域选择响应正确
     *
     * @param response         API响应
     * @param expectedDistrict 期望的区域代码
     */
    public static void assertDistrictSelectionSuccess(Response response, String expectedDistrict) {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("data.success")).isTrue();

        if (!"all".equals(expectedDistrict)) {
            assertThat(response.jsonPath().getString("data.selectedDistrict.code")).isEqualTo(expectedDistrict);
        }
    }
}
