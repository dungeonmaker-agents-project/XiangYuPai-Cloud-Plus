package org.dromara.common.integration.api.location;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.dromara.common.support.assertions.LocationAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 区域API测试
 * <p>
 * 测试API:
 * - GET /api/location/districts?cityCode=xxx
 * - POST /api/location/district/select
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("区域API测试")
class DistrictApiTest extends ApiTestBase {

    private static final String BEIJING_CODE = "110100";
    private static final String SHANGHAI_CODE = "310100";
    private static final String SMALL_CITY_CODE = "360700";

    /**
     * 测试用例1: 成功获取区域列表
     */
    @Test
    @DisplayName("成功获取区域列表")
    void testGetDistricts_Success() {
        // When: 获取北京的区域列表
        Response response = authenticatedRequest()
            .param("cityCode", BEIJING_CODE)
            .when()
            .get("/api/location/districts");

        // Then: 验证返回成功
        LocationAssertions.assertDistrictListResponse(response);

        // 验证城市名称
        assertThat(response.jsonPath().getString("data.cityName")).isEqualTo("北京");

        // 验证区域列表
        List<Map<String, Object>> districts = response.jsonPath().getList("data.districts");
        assertThat(districts).isNotEmpty();

        // 验证第一个是"全城"选项
        Map<String, Object> firstDistrict = districts.get(0);
        assertThat(firstDistrict.get("name")).asString().contains("全");
        assertThat((Boolean) firstDistrict.get("isAll")).isTrue();

        // 验证包含具体区域
        assertThat(districts.size()).isGreaterThan(1);
    }

    /**
     * 测试用例2: 缺少城市代码参数
     */
    @Test
    @DisplayName("缺少城市代码参数")
    void testGetDistricts_MissingCityCode() {
        // When: 不传cityCode参数
        Response response = authenticatedRequest()
            .when()
            .get("/api/location/districts");

        // Then: 应该返回错误
        assertThat(response.statusCode()).isIn(400, 500);

        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("cityCode");
        }
    }

    /**
     * 测试用例3: 无效的城市代码
     */
    @Test
    @DisplayName("无效的城市代码")
    void testGetDistricts_InvalidCityCode() {
        // When: 传入不存在的城市代码
        Response response = authenticatedRequest()
            .param("cityCode", "999999")
            .when()
            .get("/api/location/districts");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("城市");
        } else {
            assertThat(response.statusCode()).isIn(400, 404);
        }
    }

    /**
     * 测试用例4: 小城市无区域的情况
     */
    @Test
    @DisplayName("小城市无区域的情况")
    void testGetDistricts_CityWithoutDistricts() {
        // When: 获取小城市的区域列表
        Response response = authenticatedRequest()
            .param("cityCode", SMALL_CITY_CODE)
            .when()
            .get("/api/location/districts");

        // Then: 应该返回成功
        assertThat(response.statusCode()).isEqualTo(200);

        // 如果没有区域，应该只返回"全城"选项
        List<Map<String, Object>> districts = response.jsonPath().getList("data.districts");
        if (districts.size() == 1) {
            Map<String, Object> onlyDistrict = districts.get(0);
            assertThat(onlyDistrict.get("name")).asString().contains("全");
            assertThat((Boolean) onlyDistrict.get("isAll")).isTrue();
        }
    }

    /**
     * 测试用例5: 选择区域成功
     */
    @Test
    @DisplayName("选择区域成功")
    void testSelectDistrict_Success() {
        // Given: 准备选择数据
        String requestBody = String.format(
            "{\"cityCode\":\"%s\",\"districtCode\":\"110105\"}",
            BEIJING_CODE
        );

        // When: 选择朝阳区
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/district/select");

        // Then: 验证选择成功
        LocationAssertions.assertDistrictSelectionSuccess(response, "110105");
        assertThat(response.jsonPath().getString("data.selectedDistrict.name"))
            .isEqualTo("朝阳区");
    }

    /**
     * 测试用例6: 选择"全城"
     */
    @Test
    @DisplayName("选择全城")
    void testSelectDistrict_SelectAll() {
        // Given: 准备选择"全城"数据
        String requestBody = String.format(
            "{\"cityCode\":\"%s\",\"districtCode\":\"all\"}",
            BEIJING_CODE
        );

        // When: 选择全北京
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/district/select");

        // Then: 验证选择成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("data.success")).isTrue();

        // 选择全城时，名称应该包含"全"
        assertThat(response.jsonPath().getString("data.selectedDistrict.name"))
            .contains("全");
    }

    /**
     * 测试用例7: 缺少必要参数
     */
    @Test
    @DisplayName("缺少必要参数")
    void testSelectDistrict_MissingParams() {
        // Given: 缺少districtCode
        String requestBody = String.format(
            "{\"cityCode\":\"%s\"}",
            BEIJING_CODE
        );

        // When: 尝试选择
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/location/district/select");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例8: 区域数据结构验证
     */
    @Test
    @DisplayName("区域数据结构验证")
    void testGetDistricts_DataStructure() {
        // When: 获取区域列表
        Response response = authenticatedRequest()
            .param("cityCode", BEIJING_CODE)
            .when()
            .get("/api/location/districts");

        // Then: 验证数据结构
        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, Object>> districts = response.jsonPath().getList("data.districts");
        for (Map<String, Object> district : districts) {
            // 必须字段
            assertThat(district).containsKeys("code", "name", "isAll");
            assertThat(district.get("code")).isNotNull();
            assertThat(district.get("name")).isNotNull();
            assertThat(district.get("isAll")).isNotNull();

            // 验证isAll字段类型
            assertThat(district.get("isAll")).isInstanceOf(Boolean.class);
        }
    }
}
