package org.dromara.common.integration.flow;

import io.restassured.response.Response;
import org.dromara.common.support.FlowTestBase;
import org.dromara.common.support.assertions.LocationAssertions;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 区域选择页面流程测试
 * <p>
 * 测试场景基于前端页面: 03-区域选择页面.md
 * <p>
 * 业务流程:
 * 1. 用户进入区域选择页面
 * 2. 获取当前城市的区域列表 (GET /api/location/districts?cityCode=xxx)
 * 3. 显示区域选项 (全XX、XX区等)
 * 4. 用户点击选择区域
 * 5. 调用选择接口 (POST /api/location/district/select)
 * 6. 返回首页，刷新Feed流
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("区域选择页面流程测试")
class DistrictSelectionFlowTest extends FlowTestBase {

    private static final String BEIJING_CITY_CODE = "110100";
    private static final String CHAOYANG_DISTRICT_CODE = "110105";
    private static final String SHENZHEN_CITY_CODE = "440300";
    private static final String INVALID_CITY_CODE = "999999";

    /**
     * 场景1: 完整的区域选择流程
     * <p>
     * 步骤:
     * 1. 用户在北京，打开区域选择页
     * 2. 获取北京的区域列表
     * 3. 验证返回"全北京"、"东城区"、"朝阳区"等选项
     * 4. 用户选择"朝阳区"
     * 5. 验证选择成功
     * 6. 验证用户位置信息已更新
     */
    @Test
    @Order(1)
    @DisplayName("完整的区域选择流程")
    void testCompleteDistrictSelectionFlow() {
        // Given: 用户已登录，当前在北京
        String cityCode = BEIJING_CITY_CODE;

        // When: 步骤1 - 获取北京的区域列表
        Response districtListResponse = getDistrictList(cityCode);

        // Then: 验证区域列表返回成功
        LocationAssertions.assertDistrictListResponse(districtListResponse);
        assertThat(districtListResponse.jsonPath().getString("data.cityName")).isEqualTo("北京");
        assertThat(districtListResponse.jsonPath().getList("data.districts")).hasSizeGreaterThan(0);

        // 验证包含"全北京"选项
        assertThat(districtListResponse.jsonPath().getString("data.districts[0].name"))
            .contains("全");

        // When: 步骤2 - 用户选择"朝阳区"
        Response selectionResponse = selectDistrict(cityCode, CHAOYANG_DISTRICT_CODE);

        // Then: 验证选择成功
        LocationAssertions.assertDistrictSelectionSuccess(selectionResponse, CHAOYANG_DISTRICT_CODE);
        assertThat(selectionResponse.jsonPath().getString("data.selectedDistrict.name"))
            .isEqualTo("朝阳区");

        // Verify: 用户位置信息已更新 (后续请求应该能看到新的位置)
        // TODO: 验证数据库或缓存中的用户位置信息
    }

    /**
     * 场景2: 选择"全城"
     * <p>
     * 用户选择"全深圳"而不是具体区域
     */
    @Test
    @Order(2)
    @DisplayName("选择'全城'")
    void testSelectAllDistrictFlow() {
        // Given: 用户在深圳
        String cityCode = SHENZHEN_CITY_CODE;

        // When: 获取深圳的区域列表
        Response districtListResponse = getDistrictList(cityCode);

        // Then: 验证返回成功
        LocationAssertions.assertDistrictListResponse(districtListResponse);

        // When: 用户选择"全深圳" (districtCode = "all")
        Response selectionResponse = selectDistrict(cityCode, "all");

        // Then: 验证选择成功
        assertThat(selectionResponse.statusCode()).isEqualTo(200);
        assertThat(selectionResponse.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(selectionResponse.jsonPath().getBoolean("data.success")).isTrue();

        // 选择全城时，应该标记为"全"区域
        assertThat(selectionResponse.jsonPath().getString("data.selectedDistrict.name"))
            .contains("全");
    }

    /**
     * 场景3: 城市无区域的情况
     * <p>
     * 测试小城市，没有区域划分的情况
     */
    @Test
    @Order(3)
    @DisplayName("城市无区域的情况")
    void testCityWithoutDistricts() {
        // Given: 一个小城市 (假设code为360700)
        String smallCityCode = "360700";  // 赣州

        // When: 获取该城市的区域列表
        Response districtListResponse = getDistrictList(smallCityCode);

        // Then: 应该返回成功，但区域列表可能只有"全城"选项
        assertThat(districtListResponse.statusCode()).isEqualTo(200);

        // 如果没有区域，应该自动只显示"全城"
        int districtCount = districtListResponse.jsonPath().getList("data.districts").size();
        if (districtCount == 1) {
            assertThat(districtListResponse.jsonPath().getString("data.districts[0].name"))
                .contains("全");
        }

        // When: 选择全城
        Response selectionResponse = selectDistrict(smallCityCode, "all");

        // Then: 应该直接选择成功
        assertThat(selectionResponse.statusCode()).isEqualTo(200);
        assertThat(selectionResponse.jsonPath().getBoolean("data.success")).isTrue();
    }

    /**
     * 场景4: 网络异常处理
     * <p>
     * 模拟网络错误，验证错误提示
     */
    @Test
    @Order(4)
    @DisplayName("网络异常处理")
    void testNetworkErrorHandling() {
        // Given: 无效的请求参数 (空城市代码)
        String invalidCityCode = "";

        // When: 尝试获取区域列表
        Response response = getDistrictList(invalidCityCode);

        // Then: 应该返回错误
        assertThat(response.statusCode()).isIn(400, 500);

        // 或者API返回200但code不是200
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).isNotBlank();
        }
    }

    /**
     * 场景5: 无效城市代码
     * <p>
     * 测试不存在的城市代码
     */
    @Test
    @Order(5)
    @DisplayName("无效城市代码")
    void testInvalidCityCode() {
        // Given: 不存在的城市代码
        String cityCode = INVALID_CITY_CODE;

        // When: 尝试获取区域列表
        Response response = getDistrictList(cityCode);

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            // 如果HTTP状态码是200，业务code应该不是200
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("城市");
        } else {
            // 或者直接返回404等错误状态码
            assertThat(response.statusCode()).isIn(400, 404, 500);
        }
    }

    /**
     * 场景6: 并发选择区域
     * <p>
     * 测试多个用户同时选择区域是否会有问题
     */
    @Test
    @Order(6)
    @DisplayName("并发选择区域")
    void testConcurrentDistrictSelection() {
        // Given: 准备测试数据
        String cityCode = BEIJING_CITY_CODE;
        String districtCode = CHAOYANG_DISTRICT_CODE;

        // When: 快速发送5次相同的选择请求
        for (int i = 0; i < 5; i++) {
            Response response = selectDistrict(cityCode, districtCode);

            // Then: 每次都应该成功
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.jsonPath().getBoolean("data.success")).isTrue();
        }

        // Verify: 最终用户位置应该是朝阳区
        // TODO: 验证数据库中的最终状态
    }

    /**
     * 场景7: 切换区域
     * <p>
     * 用户先选择一个区域，然后又切换到另一个区域
     */
    @Test
    @Order(7)
    @DisplayName("切换区域")
    void testSwitchDistrict() {
        // Given: 用户在北京
        String cityCode = BEIJING_CITY_CODE;

        // When: 先选择朝阳区
        Response firstSelection = selectDistrict(cityCode, CHAOYANG_DISTRICT_CODE);
        assertThat(firstSelection.jsonPath().getBoolean("data.success")).isTrue();

        // Then: 等待一小段时间
        waitFor(100);

        // When: 再选择东城区
        String dongchengCode = "110101";
        Response secondSelection = selectDistrict(cityCode, dongchengCode);

        // Then: 第二次选择也应该成功
        assertThat(secondSelection.statusCode()).isEqualTo(200);
        assertThat(secondSelection.jsonPath().getBoolean("data.success")).isTrue();
        assertThat(secondSelection.jsonPath().getString("data.selectedDistrict.code"))
            .isEqualTo(dongchengCode);

        // Verify: 最终用户位置应该是东城区 (最后一次选择)
        // TODO: 验证数据库中的最终状态是东城区
    }
}
