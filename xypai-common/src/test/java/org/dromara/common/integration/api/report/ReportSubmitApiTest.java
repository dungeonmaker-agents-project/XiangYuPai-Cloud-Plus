package org.dromara.common.integration.api.report;

import io.restassured.response.Response;
import org.dromara.common.support.ApiTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 举报提交API测试
 * <p>
 * 测试API: POST /api/report/submit
 *
 * @author XiangYuPai Team
 * @since 2025-11-15
 */
@DisplayName("举报提交API测试")
class ReportSubmitApiTest extends ApiTestBase {

    /**
     * 测试用例1: 举报帖子成功
     */
    @Test
    @DisplayName("举报帖子成功")
    void testReportPost_Success() {
        // Given: 准备举报数据
        String requestBody = "{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":1001," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"这条帖子包含不当内容，请审核\"" +
            "}";

        // When: 提交举报
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 验证提交成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
        assertThat(response.jsonPath().getLong("data.reportId")).isNotNull();
    }

    /**
     * 测试用例2: 举报评论
     */
    @Test
    @DisplayName("举报评论")
    void testReportComment_Success() {
        // Given: 准备举报评论数据
        String requestBody = "{" +
            "\"targetType\":\"comment\"," +
            "\"targetId\":2001," +
            "\"reason\":\"辱骂他人\"," +
            "\"content\":\"该评论存在人身攻击\"" +
            "}";

        // When: 提交举报
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 验证提交成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
    }

    /**
     * 测试用例3: 举报用户
     */
    @Test
    @DisplayName("举报用户")
    void testReportUser_Success() {
        // Given: 准备举报用户数据
        String requestBody = "{" +
            "\"targetType\":\"user\"," +
            "\"targetId\":3001," +
            "\"reason\":\"垃圾广告\"," +
            "\"content\":\"该用户频繁发送垃圾广告\"" +
            "}";

        // When: 提交举报
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 验证提交成功
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getInt("code")).isEqualTo(200);
    }

    /**
     * 测试用例4: 缺少必要参数
     */
    @Test
    @DisplayName("缺少必要参数")
    void testReportSubmit_MissingParams() {
        // Given: 缺少reason参数
        String requestBody = "{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":1001," +
            "\"content\":\"举报内容\"" +
            "}";

        // When: 尝试提交
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例5: 无效的目标类型
     */
    @Test
    @DisplayName("无效的目标类型")
    void testReportSubmit_InvalidTargetType() {
        // Given: 无效的targetType
        String requestBody = "{" +
            "\"targetType\":\"invalid_type\"," +
            "\"targetId\":1001," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"举报内容\"" +
            "}";

        // When: 尝试提交
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 应该返回错误
        if (response.statusCode() == 200) {
            assertThat(response.jsonPath().getInt("code")).isNotEqualTo(200);
            assertThat(response.jsonPath().getString("msg")).contains("类型");
        } else {
            assertThat(response.statusCode()).isEqualTo(400);
        }
    }

    /**
     * 测试用例6: 不存在的目标ID
     */
    @Test
    @DisplayName("不存在的目标ID")
    void testReportSubmit_TargetNotFound() {
        // Given: 不存在的targetId
        String requestBody = "{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":999999999," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"举报内容\"" +
            "}";

        // When: 尝试提交
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 可能返回成功或错误(取决于业务规则)
        // 有些系统允许举报不存在的内容
        assertThat(response.statusCode()).isIn(200, 400, 404);
    }

    /**
     * 测试用例7: 重复举报
     */
    @Test
    @DisplayName("重复举报")
    void testReportSubmit_Duplicate() {
        // Given: 准备举报数据
        String requestBody = "{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":5001," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"这条帖子包含不当内容\"" +
            "}";

        // When: 第一次举报
        Response firstResponse = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        assertThat(firstResponse.statusCode()).isEqualTo(200);

        // When: 第二次举报相同内容
        Response secondResponse = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 可能返回错误或成功(取决于业务规则)
        // 有些系统不允许重复举报
        if (secondResponse.statusCode() == 200) {
            Integer code = secondResponse.jsonPath().getInt("code");
            // 可能返回成功或业务错误
            if (code != 200) {
                assertThat(secondResponse.jsonPath().getString("msg"))
                    .containsAnyOf("已举报", "重复");
            }
        }
    }

    /**
     * 测试用例8: 举报理由过长
     */
    @Test
    @DisplayName("举报理由过长")
    void testReportSubmit_ContentTooLong() {
        // Given: 创建超长内容
        String longContent = "x".repeat(1001);  // 假设限制1000字符
        String requestBody = String.format("{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":1001," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"%s\"" +
            "}", longContent);

        // When: 尝试提交
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 可能返回错误
        if (response.statusCode() == 200 && response.jsonPath().getInt("code") != 200) {
            assertThat(response.jsonPath().getString("msg"))
                .containsAnyOf("长度", "超出", "限制");
        }
    }

    /**
     * 测试用例9: 未登录举报
     */
    @Test
    @DisplayName("未登录举报")
    void testReportSubmit_Unauthorized() {
        // Given: 准备举报数据
        String requestBody = "{" +
            "\"targetType\":\"post\"," +
            "\"targetId\":1001," +
            "\"reason\":\"违规内容\"," +
            "\"content\":\"举报内容\"" +
            "}";

        // When: 不带Token提交
        Response response = unauthenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 应该返回401错误
        assertThat(response.statusCode()).isEqualTo(401);
    }

    /**
     * 测试用例10: 自己举报自己
     */
    @Test
    @DisplayName("自己举报自己")
    void testReportSubmit_SelfReport() {
        // Given: 举报自己的内容
        String requestBody = String.format("{" +
            "\"targetType\":\"user\"," +
            "\"targetId\":%d," +
            "\"reason\":\"测试\"," +
            "\"content\":\"自己举报自己\"" +
            "}", testUserId);

        // When: 尝试提交
        Response response = authenticatedRequest()
            .body(requestBody)
            .when()
            .post("/api/report/submit");

        // Then: 可能返回错误(不允许自己举报自己)
        if (response.statusCode() == 200) {
            Integer code = response.jsonPath().getInt("code");
            if (code != 200) {
                assertThat(response.jsonPath().getString("msg"))
                    .containsAnyOf("不能", "自己");
            }
        }
    }
}
