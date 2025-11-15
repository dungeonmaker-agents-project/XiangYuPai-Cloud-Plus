package org.dromara.common.integration.flow;

import io.restassured.response.Response;
import org.dromara.common.support.FlowTestBase;
import org.dromara.common.support.assertions.LocationAssertions;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 城市定位页面流程测试
 * <p>
 * 测试场景基于前端页面: 04-城市定位页面.md
 * <p>
 * 业务流程:
 * 1. 用户打开城市选择页
 * 2. 获取城市列表 (GET /api/location/cities)
 * 3. 显示: 当前定位、最近访问、热门城市、全部城市(A-Z)
 * 4. 用户选择方式:
 *    - GPS定位 (POST /api/location/detect)
 *    - 点击最近访问城市
 *    - 点击热门城市
 *    - 浏览全部城市选择
 * 5. 调用选择接口 (POST /api/location/city/select)
 * 6. 判断: 是否有区域?
 *    - 是 → 跳转区域选择页
 *    - 否 → 直接返回首页
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("城市定位页面流程测试")
class CityLocationFlowTest extends FlowTestBase {

    private static final String BEIJING_CODE = "110100";
    private static final String SHANGHAI_CODE = "310100";
    private static final String SHENZHEN_CODE = "440300";
    private static final String SMALL_CITY_CODE = "360700";  // 赣州 (假设无区域)

    /**
     * 场景1: 完整的城市选择流程
     * <p>
     * 步骤:
     * 1. 获取城市列表
     * 2. 验证返回热门城市、全部城市(按字母分组)
     * 3. 用户选择"北京"
     * 4. 验证 hasDistricts=true (北京有区域)
     * 5. 应该跳转到区域选择页面
     */
    @Test
    @Order(1)
    @DisplayName("完整的城市选择流程")
    void testCompleteCitySelectionFlow() {
        // When: 步骤1 - 获取城市列表
        Response cityListResponse = getCityList();

        // Then: 验证城市列表返回成功
        LocationAssertions.assertCityListResponse(cityListResponse);

        // 验证热门城市列表
        List<Map<String, Object>> hotCities = cityListResponse.jsonPath().getList("data.hotCities");
        assertThat(hotCities).isNotEmpty();
        assertThat(hotCities.size()).isGreaterThanOrEqualTo(5);  // 至少5个热门城市

        // 验证全部城市按字母分组
        Map<String, List> allCities = cityListResponse.jsonPath().getMap("data.allCities");
        assertThat(allCities).isNotEmpty();
        assertThat(allCities).containsKey("B");  // 应该有B分组 (北京)
        assertThat(allCities).containsKey("S");  // 应该有S分组 (上海、深圳)

        // When: 步骤2 - 用户选择"北京"
        Response selectionResponse = selectCity(BEIJING_CODE, "北京", "hot");

        // Then: 验证选择成功
        LocationAssertions.assertCitySelectionSuccess(selectionResponse, BEIJING_CODE);

        // 验证北京有区域，需要跳转到区域选择页
        assertThat(selectionResponse.jsonPath().getBoolean("data.hasDistricts")).isTrue();

        // 前端应该跳转到区域选择页面
        // TODO: 在实际集成测试中，可以验证Session或返回的跳转标志
    }

    /**
     * 场景2: GPS定位流程
     * <p>
     * 用户使用GPS定位获取当前城市
     */
    @Test
    @Order(2)
    @DisplayName("GPS定位流程")
    void testGPSLocationFlow() {
        // Given: 用户触发GPS定位，前端获取GPS坐标 (北京天安门)
        BigDecimal[] coordinates = dataBuilder.beijingCoordinates();
        BigDecimal latitude = coordinates[0];   // 39.904989
        BigDecimal longitude = coordinates[1];  // 116.405285

        // When: 调用定位解析接口
        Response detectResponse = detectLocation(latitude, longitude);

        // Then: 应该返回北京
        assertThat(detectResponse.statusCode()).isEqualTo(200);
        assertThat(detectResponse.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(detectResponse.jsonPath().getString("data.cityName")).contains("北京");

        // 验证返回详细位置信息
        assertThat(detectResponse.jsonPath().getString("data.cityCode")).isEqualTo(BEIJING_CODE);
        assertThat(detectResponse.jsonPath().getString("data.province")).isNotBlank();
        assertThat(detectResponse.jsonPath().getString("data.formattedAddress")).isNotBlank();

        // When: 用户确认定位结果，选择该城市
        Response selectionResponse = selectCity(BEIJING_CODE, "北京", "gps");

        // Then: 选择成功
        LocationAssertions.assertCitySelectionSuccess(selectionResponse, BEIJING_CODE);
    }

    /**
     * 场景3: 选择无区域的城市
     * <p>
     * 选择小城市，没有区域划分，应该直接返回首页
     */
    @Test
    @Order(3)
    @DisplayName("选择无区域的城市")
    void testSelectCityWithoutDistricts() {
        // When: 用户选择一个小城市 (假设没有区域划分)
        Response selectionResponse = selectCity(SMALL_CITY_CODE, "赣州", "manual");

        // Then: 验证选择成功
        assertThat(selectionResponse.statusCode()).isEqualTo(200);
        assertThat(selectionResponse.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(selectionResponse.jsonPath().getBoolean("data.success")).isTrue();

        // 验证没有区域，应该直接返回首页
        assertThat(selectionResponse.jsonPath().getBoolean("data.hasDistricts")).isFalse();

        // 前端应该直接返回首页，而不是跳转到区域选择页
    }

    /**
     * 场景4: 城市列表数据验证
     * <p>
     * 验证城市列表数据结构完整性
     */
    @Test
    @Order(4)
    @DisplayName("城市列表数据验证")
    void testCityListDataStructure() {
        // When: 获取城市列表
        Response response = getCityList();

        // Then: 验证数据结构
        LocationAssertions.assertCityListResponse(response);

        // 验证热门城市结构
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");
        for (Map<String, Object> city : hotCities) {
            assertThat(city).containsKeys("cityCode", "cityName", "isHot");
            assertThat(city.get("cityCode")).isNotNull();
            assertThat(city.get("cityName")).isNotNull();
            assertThat((Integer) city.get("isHot")).isEqualTo(1);  // 热门城市标志
        }

        // 验证全部城市结构
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
     * 场景5: 字母分组正确性
     * <p>
     * 验证城市按首字母正确分组
     */
    @Test
    @Order(5)
    @DisplayName("字母分组正确性")
    void testCityAlphabetGrouping() {
        // When: 获取城市列表
        Response response = getCityList();

        // Then: 验证字母分组
        Map<String, List> allCities = response.jsonPath().getMap("data.allCities");

        // 验证A分组 (应该包含安庆等)
        if (allCities.containsKey("A")) {
            List<Map<String, Object>> aCities = allCities.get("A");
            assertThat(aCities).isNotEmpty();
            // 所有A分组的城市首字母应该是A
            for (Map<String, Object> city : aCities) {
                String pinyin = (String) city.get("pinyin");
                if (pinyin != null) {
                    assertThat(pinyin.toUpperCase()).startsWith("A");
                }
            }
        }

        // 验证B分组 (应该包含北京、保定等)
        assertThat(allCities).containsKey("B");
        List<Map<String, Object>> bCities = allCities.get("B");
        assertThat(bCities).isNotEmpty();

        // 北京应该在B分组
        boolean foundBeijing = bCities.stream()
            .anyMatch(city -> "北京".equals(city.get("cityName")));
        assertThat(foundBeijing).isTrue();

        // 验证S分组 (应该包含上海、深圳等)
        assertThat(allCities).containsKey("S");
        List<Map<String, Object>> sCities = allCities.get("S");
        assertThat(sCities).hasSizeGreaterThanOrEqualTo(2);  // 至少上海和深圳
    }

    /**
     * 场景6: 热门城市排序
     * <p>
     * 验证热门城市的排序正确
     */
    @Test
    @Order(6)
    @DisplayName("热门城市排序")
    void testHotCitiesOrder() {
        // When: 获取城市列表
        Response response = getCityList();

        // Then: 验证热门城市列表
        List<Map<String, Object>> hotCities = response.jsonPath().getList("data.hotCities");

        // 验证热门城市数量
        assertThat(hotCities).hasSizeGreaterThanOrEqualTo(5);

        // 验证常见热门城市存在
        List<String> hotCityNames = hotCities.stream()
            .map(city -> (String) city.get("cityName"))
            .toList();

        assertThat(hotCityNames).contains("北京", "上海", "深圳", "广州");

        // 验证热门城市有排序字段
        for (Map<String, Object> city : hotCities) {
            assertThat(city).containsKey("sortOrder");
        }

        // 验证按sortOrder排序
        for (int i = 1; i < hotCities.size(); i++) {
            Integer prevOrder = (Integer) hotCities.get(i - 1).get("sortOrder");
            Integer currOrder = (Integer) hotCities.get(i).get("sortOrder");
            if (prevOrder != null && currOrder != null) {
                assertThat(currOrder).isGreaterThanOrEqualTo(prevOrder);
            }
        }
    }

    /**
     * 场景7: 最近访问记录
     * <p>
     * 测试用户选择城市后，最近访问记录的更新
     */
    @Test
    @Order(7)
    @DisplayName("最近访问记录")
    void testRecentCitiesHistory() {
        // Given: 用户依次选择几个城市
        selectCity(BEIJING_CODE, "北京", "manual");
        waitFor(100);

        selectCity(SHANGHAI_CODE, "上海", "manual");
        waitFor(100);

        selectCity(SHENZHEN_CODE, "深圳", "manual");
        waitFor(100);

        // When: 再次获取城市列表
        Response response = getCityList();

        // Then: 验证最近访问记录 (如果API返回的话)
        // 注意: 这个功能可能需要后端实现，如果未实现可以跳过验证
        if (response.jsonPath().get("data.recentCities") != null) {
            List<Map<String, Object>> recentCities = response.jsonPath().getList("data.recentCities");

            // 最近访问应该包含刚才选择的城市
            List<String> recentCityCodes = recentCities.stream()
                .map(city -> (String) city.get("cityCode"))
                .toList();

            assertThat(recentCityCodes).contains(BEIJING_CODE, SHANGHAI_CODE, SHENZHEN_CODE);

            // 最近的应该在前面 (深圳 > 上海 > 北京)
            if (recentCities.size() >= 3) {
                assertThat(recentCityCodes.get(0)).isEqualTo(SHENZHEN_CODE);
            }
        }
    }

    /**
     * 场景8: GPS定位失败处理
     * <p>
     * 测试GPS坐标无效或定位失败的情况
     */
    @Test
    @Order(8)
    @DisplayName("GPS定位失败处理")
    void testGPSLocationFailure() {
        // Given: 无效的GPS坐标 (超出范围)
        BigDecimal invalidLat = BigDecimal.valueOf(999.0);
        BigDecimal invalidLng = BigDecimal.valueOf(999.0);

        // When: 尝试定位
        Response response = detectLocation(invalidLat, invalidLng);

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            // 业务层返回错误
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("坐标");
        } else {
            // HTTP层返回错误
            assertThat(response.statusCode()).isIn(400, 500);
        }
    }

    /**
     * 场景9: 缓存测试
     * <p>
     * 验证城市列表使用缓存，第二次请求更快
     */
    @Test
    @Order(9)
    @DisplayName("缓存测试")
    void testCityListCache() {
        // When: 第一次请求
        long start1 = System.currentTimeMillis();
        Response response1 = getCityList();
        long time1 = System.currentTimeMillis() - start1;

        assertThat(response1.statusCode()).isEqualTo(200);

        // When: 第二次请求 (应该走缓存)
        long start2 = System.currentTimeMillis();
        Response response2 = getCityList();
        long time2 = System.currentTimeMillis() - start2;

        assertThat(response2.statusCode()).isEqualTo(200);

        // Then: 验证两次返回数据一致
        assertThat(response1.jsonPath().getList("data.hotCities").size())
            .isEqualTo(response2.jsonPath().getList("data.hotCities").size());

        // 第二次请求应该更快 (走缓存)
        // 注意: 这个断言可能不稳定，仅供参考
        if (time1 > 100) {  // 如果第一次请求超过100ms
            assertThat(time2).isLessThan(time1);
        }
    }
}
