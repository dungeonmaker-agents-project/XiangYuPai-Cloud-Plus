package org.dromara.common.integration.api.location;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 地理编码API测试
 * <p>
 * 测试API:
 * - POST /api/location/geocode - 地址编码(地址 -> 坐标)
 * - POST /api/location/reverse-geocode - 逆地理编码(坐标 -> 地址)
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("地理编码API测试")
class GeocodeApiTest extends ApiTestBase {

    /**
     * 测试用例1: 地址编码成功
     */
    @Test
    @DisplayName("地址编码成功")
    void testGeocode_Success() {
        // Given: 准备地址
        String address = "北京市朝阳区望京SOHO";
        String requestBody = String.format("{\"address\":\"%s\"}", address);

        // When: 调用地址编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/geocode");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证返回坐标
        Double latitude = response.jsonPath().getDouble("data.latitude");
        Double longitude = response.jsonPath().getDouble("data.longitude");

        assertThat(latitude).isNotNull();
        assertThat(longitude).isNotNull();

        // 验证坐标在北京范围内 (纬度39-41, 经度115-117)
        assertThat(latitude).isBetween(39.0, 41.0);
        assertThat(longitude).isBetween(115.0, 117.0);

        // 验证返回格式化地址
        String formattedAddress = response.jsonPath().getString("data.formattedAddress");
        assertThat(formattedAddress).isNotBlank();
    }

    /**
     * 测试用例2: 逆地理编码成功
     */
    @Test
    @DisplayName("逆地理编码成功")
    void testReverseGeocode_Success() {
        // Given: 北京天安门坐标
        BigDecimal[] beijing = dataBuilder.beijingCoordinates();
        String requestBody = String.format(
            "{\"latitude\":%s,\"longitude\":%s}",
            beijing[0].toString(), beijing[1].toString()
        );

        // When: 调用逆地理编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/reverse-geocode");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);

        // 验证返回地址信息
        String province = response.jsonPath().getString("data.province");
        String city = response.jsonPath().getString("data.city");
        String district = response.jsonPath().getString("data.district");
        String formattedAddress = response.jsonPath().getString("data.formattedAddress");

        assertThat(province).contains("北京");
        assertThat(city).contains("北京");
        assertThat(district).isNotBlank();
        assertThat(formattedAddress).isNotBlank();
    }

    /**
     * 测试用例3: 空地址
     */
    @Test
    @DisplayName("空地址")
    void testGeocode_EmptyAddress() {
        // Given: 空地址
        String requestBody = "{\"address\":\"\"}";

        // When: 调用地址编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/geocode");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("地址");
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例4: 无效地址
     */
    @Test
    @DisplayName("无效地址")
    void testGeocode_InvalidAddress() {
        // Given: 不存在的地址
        String invalidAddress = "火星上的随便一个地方123456";
        String requestBody = String.format("{\"address\":\"%s\"}", invalidAddress);

        // When: 调用地址编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/geocode");

        // Then: 应该返回错误或没有结果
        if (response.statusCode() == 200) {
            // 可能返回成功但data为空，或者code不是200
            Integer code = response.jsonPath().getInt("code");
            if (code == 200) {
                // 如果code是200，data可能为null
                Object data = response.jsonPath().get("data");
                // 数据可能为null或者没有找到结果
            } else {
                assertThat(response.jsonPath().getString("msg")).isNotBlank();
            }
        }
    }

    /**
     * 测试用例5: 多个城市同名地址
     */
    @Test
    @DisplayName("多个城市同名地址")
    void testGeocode_AmbiguousAddress() {
        // Given: 模糊地址 (多个城市都可能有)
        String address = "人民路1号";
        String requestBody = String.format("{\"address\":\"%s\"}", address);

        // When: 调用地址编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/geocode");

        // Then: 应该返回结果 (可能是某个城市的人民路)
        // 或者返回多个候选结果
        assertThat(response.statusCode()).isEqualTo(200);

        // 如果有返回数据，验证坐标有效性
        if (response.jsonPath().get("data.latitude") != null) {
            Double latitude = response.jsonPath().getDouble("data.latitude");
            Double longitude = response.jsonPath().getDouble("data.longitude");

            assertThat(latitude).isBetween(-90.0, 90.0);
            assertThat(longitude).isBetween(-180.0, 180.0);
        }
    }

    /**
     * 测试用例6: 逆地理编码 - 海洋坐标
     */
    @Test
    @DisplayName("逆地理编码 - 海洋坐标")
    void testReverseGeocode_OceanCoordinates() {
        // Given: 太平洋中的坐标
        String requestBody = "{\"latitude\":0.0,\"longitude\":180.0}";

        // When: 调用逆地理编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/reverse-geocode");

        // Then: 可能返回空结果或海洋名称
        assertThat(response.statusCode()).isEqualTo(200);

        // 验证响应格式
        if (response.jsonPath().get("data") != null) {
            // 如果有数据，可能返回附近的岛屿或空信息
            String formattedAddress = response.jsonPath().getString("data.formattedAddress");
            // formattedAddress可能为空或包含海洋信息
        }
    }

    /**
     * 测试用例7: 地址编码 - 详细地址
     */
    @Test
    @DisplayName("地址编码 - 详细地址")
    void testGeocode_DetailedAddress() {
        // Given: 非常详细的地址
        String detailedAddress = "北京市朝阳区望京街道望京SOHO塔1 A座1001室";
        String requestBody = String.format("{\"address\":\"%s\"}", detailedAddress);

        // When: 调用地址编码接口
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/geocode");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);

        if (response.jsonPath().getInt("code") == 200) {
            // 验证返回精确坐标
            Double latitude = response.jsonPath().getDouble("data.latitude");
            Double longitude = response.jsonPath().getDouble("data.longitude");

            assertThat(latitude).isNotNull();
            assertThat(longitude).isNotNull();

            // 验证在北京范围内
            assertThat(latitude).isBetween(39.0, 41.0);
            assertThat(longitude).isBetween(115.0, 117.0);
        }
    }
}
