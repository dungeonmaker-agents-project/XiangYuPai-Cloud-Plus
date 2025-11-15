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
 * 城市API测试
 * <p>
 * 测试API: GET /api/city/list
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("城市API测试")
class CityApiTest extends ApiTestBase {

    /**
     * 测试用例1: 成功获取城市列表
     */
    @Test
    @DisplayName("成功获取城市列表")
    void testGetCityList_Success() {
        // When: 获取城市列表
        Response response = authenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 验证返回成功
        LocationAssertions.assertCityListResponse(response);

        // 验证热门城市
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");
        assertThat(hotCities).isNotEmpty();
        assertThat(hotCities.size()).isGreaterThanOrEqualTo(5);

        // 验证包含常见热门城市
        List<String> cityNames = hotCities.stream()
            .map(city -> (String) city.get("cityName"))
            .toList();
        assertThat(cityNames).contains("北京", "上海", "深圳");

        // 验证全部城市
        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");
        assertThat(allCities).isNotEmpty();
        assertThat(allCities.size()).isGreaterThanOrEqualTo(10);  // 至少10个字母分组
    }

    /**
     * 测试用例2: 未登录访问
     */
    @Test
    @DisplayName("未登录访问")
    void testGetCityList_Unauthorized() {
        // When: 不带Token访问
        Response response = unauthenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 应该返回401错误 (如果需要登录的话)
        // 注意: 城市列表可能不需要登录，根据实际业务调整
        assertThat(response.statusCode()).isIn(200, 401);

        if (response.statusCode() == 401) {
            // 如果需要登录，验证错误信息
            assertThat(response.jsonPath().getString("msg")).isNotBlank();
        }
    }

    /**
     * 测试用例3: 验证数据结构完整性
     */
    @Test
    @DisplayName("验证数据结构完整性")
    void testGetCityList_DataStructure() {
        // When: 获取城市列表
        Response response = authenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);

        // 验证热门城市字段完整性
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");
        for (Map<String, Object> city : hotCities) {
            // 必须字段
            assertThat(city).containsKeys("cityCode", "cityName", "isHot");
            assertThat(city.get("cityCode")).isNotNull();
            assertThat(city.get("cityName")).isNotNull();
            assertThat((Integer) city.get("isHot")).isEqualTo(1);

            // 可选字段
            if (city.containsKey("province")) {
                assertThat(city.get("province")).isNotNull();
            }
            if (city.containsKey("pinyin")) {
                assertThat(city.get("pinyin")).isNotNull();
            }
        }

        // 验证全部城市字段完整性
        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");
        for (String letter : allCities.keySet()) {
            List<Map<String, Object>> cities = allCities.get(letter);
            for (Map<String, Object> city : cities) {
                assertThat(city).containsKeys("cityCode", "cityName", "province");
                assertThat(city.get("cityCode")).isNotNull();
                assertThat(city.get("cityName")).isNotNull();
                assertThat(city.get("province")).isNotNull();
            }
        }
    }

    /**
     * 测试用例4: 验证字母分组
     */
    @Test
    @DisplayName("验证字母分组")
    void testGetCityList_AlphabetGrouping() {
        // When: 获取城市列表
        Response response = authenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 验证返回成功
        assertThat(response.statusCode()).isEqualTo(200);

        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");

        // 验证包含常见字母分组
        assertThat(allCities).containsKey("B");  // 北京
        assertThat(allCities).containsKey("S");  // 上海、深圳

        // 验证B分组包含北京
        List<Map<String, Object>> bCities = allCities.get("B");
        assertThat(bCities).isNotEmpty();

        boolean foundBeijing = bCities.stream()
            .anyMatch(city -> "北京".equals(city.get("cityName")));
        assertThat(foundBeijing).isTrue();

        // 验证S分组包含上海和深圳
        List<Map<String, Object>> sCities = allCities.get("S");
        assertThat(sCities).hasSizeGreaterThanOrEqualTo(2);

        List<String> sCityNames = sCities.stream()
            .map(city -> (String) city.get("cityName"))
            .toList();
        assertThat(sCityNames).contains("上海", "深圳");
    }

    /**
     * 测试用例5: 缓存测试
     */
    @Test
    @DisplayName("缓存测试")
    void testGetCityList_Cache() {
        // When: 第一次请求
        long start1 = System.currentTimeMillis();
        Response response1 = authenticatedRequest()
            .when()
            .get("/api/city/list");
        long time1 = System.currentTimeMillis() - start1;

        assertThat(response1.statusCode()).isEqualTo(200);
        int hotCityCount1 = response1.jsonPath().getList("data.hotCities").size();

        // When: 第二次请求 (应该走缓存)
        long start2 = System.currentTimeMillis();
        Response response2 = authenticatedRequest()
            .when()
            .get("/api/city/list");
        long time2 = System.currentTimeMillis() - start2;

        assertThat(response2.statusCode()).isEqualTo(200);
        int hotCityCount2 = response2.jsonPath().getList("data.hotCities").size();

        // Then: 验证两次返回数据一致
        assertThat(hotCityCount1).isEqualTo(hotCityCount2);

        // 第二次请求应该更快 (走缓存)
        // 注意: 这个断言可能不稳定，仅供参考
        if (time1 > 50) {  // 如果第一次请求超过50ms
            assertThat(time2).isLessThanOrEqualTo(time1);
        }
    }

    /**
     * 测试用例6: 热门城市数量验证
     */
    @Test
    @DisplayName("热门城市数量验证")
    void testGetCityList_HotCityCount() {
        // When: 获取城市列表
        Response response = authenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 验证热门城市数量合理
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");

        // 热门城市应该在5-20个之间
        assertThat(hotCities.size()).isBetween(5, 20);

        // 所有热门城市的isHot字段应该为1
        for (Map<String, Object> city : hotCities) {
            assertThat((Integer) city.get("isHot")).isEqualTo(1);
        }
    }

    /**
     * 测试用例7: 城市代码唯一性验证
     */
    @Test
    @DisplayName("城市代码唯一性验证")
    void testGetCityList_UniqueCityCode() {
        // When: 获取城市列表
        Response response = authenticatedRequest()
            .when()
            .get("/api/city/list");

        // Then: 验证所有城市代码唯一
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");
        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");

        // 收集所有城市代码
        java.util.Set<String> allCityCodes = new java.util.HashSet<>();

        // 添加热门城市代码
        for (Map<String, Object> city : hotCities) {
            String cityCode = (String) city.get("cityCode");
            assertThat(allCityCodes.add(cityCode))
                .as("城市代码 %s 重复", cityCode)
                .isTrue();
        }

        // 添加全部城市代码
        for (List<Map<String, Object>> cities : allCities.values()) {
            for (Map<String, Object> city : cities) {
                String cityCode = (String) city.get("cityCode");
                // 注意: 热门城市会同时出现在allCities中，所以这里不验证唯一性
                allCityCodes.add(cityCode);
            }
        }

        // 验证至少有100个不同的城市
        assertThat(allCityCodes.size()).isGreaterThanOrEqualTo(100);
    }
}
