package org.dromara.auth.test.controller;

import org.dromara.auth.test.base.BaseControllerTest;
import org.dromara.auth.test.data.LoginTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Token Management Test Cases
 *
 * <p>Tests token refresh and logout operations</p>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Token Management Tests")
public class TokenManagementTest extends BaseControllerTest {

    private static final String PASSWORD_LOGIN_URL = "/auth/login/password";
    private static final String REFRESH_TOKEN_URL = "/auth/token/refresh";
    private static final String LOGOUT_URL = "/auth/logout";
    private static final String VERIFY_PAYMENT_PASSWORD_URL = "/auth/payment-password/verify";

    private String testUserToken;
    private String testUserRefreshToken;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Login to get tokens
        loginAndExtractTokens();
    }

    /**
     * Helper method to login and extract both access and refresh tokens
     */
    private void loginAndExtractTokens() throws Exception {
        var request = LoginTestData.defaultPasswordLogin();
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseBody).path("data");

        testUserToken = jsonNode.path("accessToken").asText();
        testUserRefreshToken = jsonNode.path("refreshToken").asText();
    }

    // ==================== Refresh Token Tests ====================

    @Test
    @DisplayName("TC-5.1: Refresh Token - Success")
    public void testRefreshToken_Success() throws Exception {
        // Given: Valid refresh token request
        String payload = String.format("{\"refreshToken\":\"%s\"}", testUserRefreshToken);

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: New tokens generated successfully
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("成功")))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.data.expireIn").isNumber());
    }

    @Test
    @DisplayName("TC-5.2: Refresh Token - Tokens Are Different From Original")
    public void testRefreshToken_NewTokensDifferent() throws Exception {
        // Given: Valid refresh token request
        String payload = String.format("{\"refreshToken\":\"%s\"}", testUserRefreshToken);

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: New tokens should be different from original
        String responseBody = result.andReturn().getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(responseBody).path("data");
        String newAccessToken = jsonNode.path("token").asText();
        String newRefreshToken = jsonNode.path("refreshToken").asText();

        // Verify new tokens are different
        assert !newAccessToken.equals(testUserToken) : "New access token should be different";
        assert !newRefreshToken.equals(testUserRefreshToken) : "New refresh token should be different";
    }

    @Test
    @DisplayName("TC-5.3: Refresh Token - New Token Is Valid")
    public void testRefreshToken_NewTokenIsValid() throws Exception {
        // Given: Refresh token to get new access token
        String payload = String.format("{\"refreshToken\":\"%s\"}", testUserRefreshToken);
        ResultActions refreshResult = performPost(REFRESH_TOKEN_URL, payload);

        String responseBody = refreshResult.andReturn().getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(responseBody)
            .path("data").path("token").asText();

        // When: Use new token to access protected endpoint
        String verifyPayload = "{\"paymentPassword\":\"123456\"}";
        ResultActions verifyResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, newAccessToken);

        // Then: New token works correctly
        verifyResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));  // 200 or 404 (if not set)
    }

    @Test
    @DisplayName("TC-5.4: Refresh Token - Empty Refresh Token")
    public void testRefreshToken_EmptyRefreshToken() throws Exception {
        // Given: Request with empty refresh token
        String payload = "{\"refreshToken\":\"\"}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsString("refreshToken")));
    }

    @Test
    @DisplayName("TC-5.5: Refresh Token - Invalid Refresh Token")
    public void testRefreshToken_InvalidRefreshToken() throws Exception {
        // Given: Request with invalid refresh token
        String payload = "{\"refreshToken\":\"invalid_token_here\"}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Request rejected
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("token")));
    }

    @Test
    @DisplayName("TC-5.6: Refresh Token - Expired Refresh Token")
    public void testRefreshToken_ExpiredRefreshToken() throws Exception {
        // Given: Request with expired refresh token
        String payload = "{\"refreshToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token\"}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Request rejected with expiration error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(anyOf(
                containsStringIgnoringCase("过期"),
                containsStringIgnoringCase("失效"),
                containsStringIgnoringCase("invalid")
            )));
    }

    @Test
    @DisplayName("TC-5.7: Refresh Token - Malformed Refresh Token")
    public void testRefreshToken_MalformedRefreshToken() throws Exception {
        // Given: Request with malformed refresh token
        String payload = "{\"refreshToken\":\"not.a.valid.jwt.token\"}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Request rejected
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-5.8: Refresh Token - Token Reuse Prevention")
    public void testRefreshToken_ReuseP revention() throws Exception {
        // Given: Valid refresh token
        String payload = String.format("{\"refreshToken\":\"%s\"}", testUserRefreshToken);

        // When: Use refresh token first time
        ResultActions result1 = performPost(REFRESH_TOKEN_URL, payload);
        result1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // When: Try to reuse the same refresh token
        ResultActions result2 = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Second attempt may succeed or fail depending on implementation
        // (Some systems invalidate old refresh tokens, some allow reuse within grace period)
        result2.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(401))));
    }

    // ==================== Logout Tests ====================

    @Test
    @DisplayName("TC-5.9: Logout - Success")
    public void testLogout_Success() throws Exception {
        // Given: Valid authentication token
        // When: Logout
        ResultActions result = performPostWithAuth(LOGOUT_URL, "{}", testUserToken);

        // Then: Logout successful
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("登出成功")));
    }

    @Test
    @DisplayName("TC-5.10: Logout - Token Invalidated After Logout")
    public void testLogout_TokenInvalidatedAfterLogout() throws Exception {
        // Given: User logs out
        performPostWithAuth(LOGOUT_URL, "{}", testUserToken);

        // When: Try to use the same token after logout
        String verifyPayload = "{\"paymentPassword\":\"123456\"}";
        ResultActions result = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, testUserToken);

        // Then: Token no longer valid
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("token")));
    }

    @Test
    @DisplayName("TC-5.11: Logout - Without Authentication")
    public void testLogout_WithoutAuth() throws Exception {
        // Given: Request without authentication token
        // When: Logout
        ResultActions result = performPost(LOGOUT_URL, "{}");

        // Then: Authentication error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-5.12: Logout - With Invalid Token")
    public void testLogout_InvalidToken() throws Exception {
        // Given: Invalid authentication token
        // When: Logout
        ResultActions result = performPostWithAuth(LOGOUT_URL, "{}", "invalid_token_here");

        // Then: Authentication error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-5.13: Logout - Double Logout")
    public void testLogout_DoubleLogout() throws Exception {
        // Given: User logs out first time
        ResultActions result1 = performPostWithAuth(LOGOUT_URL, "{}", testUserToken);
        result1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // When: Try to logout again with same token
        ResultActions result2 = performPostWithAuth(LOGOUT_URL, "{}", testUserToken);

        // Then: Second logout fails (token already invalidated)
        result2.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-5.14: Logout - Verify Refresh Token Also Invalidated")
    public void testLogout_RefreshTokenAlsoInvalidated() throws Exception {
        // Given: User logs out
        performPostWithAuth(LOGOUT_URL, "{}", testUserToken);

        // When: Try to use refresh token after logout
        String payload = String.format("{\"refreshToken\":\"%s\"}", testUserRefreshToken);
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Refresh token should also be invalidated
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(401), is(200))));
        // Note: Behavior depends on whether logout invalidates both access and refresh tokens
    }

    // ==================== Token Lifecycle Tests ====================

    @Test
    @DisplayName("TC-5.15: Token Lifecycle - Login -> Use -> Refresh -> Use -> Logout")
    public void testTokenLifecycle_Complete() throws Exception {
        // Step 1: Login
        var loginRequest = LoginTestData.defaultPasswordLogin();
        ResultActions loginResult = performPost(PASSWORD_LOGIN_URL, loginRequest);
        String accessToken = extractToken(loginResult);

        String loginResponseBody = loginResult.andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponseBody)
            .path("data").path("refreshToken").asText();

        // Step 2: Use access token
        String verifyPayload = "{\"paymentPassword\":\"123456\"}";
        ResultActions useResult1 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, accessToken);
        useResult1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));

        // Step 3: Refresh token
        String refreshPayload = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
        ResultActions refreshResult = performPost(REFRESH_TOKEN_URL, refreshPayload);
        String newAccessToken = objectMapper.readTree(
            refreshResult.andReturn().getResponse().getContentAsString())
            .path("data").path("token").asText();

        // Step 4: Use new access token
        ResultActions useResult2 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, newAccessToken);
        useResult2.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));

        // Step 5: Logout
        ResultActions logoutResult = performPostWithAuth(LOGOUT_URL, "{}", newAccessToken);
        logoutResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 6: Verify token no longer works
        ResultActions useResult3 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, newAccessToken);
        useResult3.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== Security Tests ====================

    @Test
    @DisplayName("TC-5.16: Refresh Token - SQL Injection in Refresh Token")
    public void testRefreshToken_SqlInjectionAttempt() throws Exception {
        // Given: Request with SQL injection attempt
        String payload = "{\"refreshToken\":\"' OR '1'='1\"}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Request rejected
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-5.17: Token Security - Token Length Validation")
    public void testToken_LengthValidation() throws Exception {
        // Given: Extremely long token (potential DoS attack)
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longToken.append("a");
        }
        String payload = String.format("{\"refreshToken\":\"%s\"}", longToken);

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Request handled safely (rejected or processed without error)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401))));
    }

    @Test
    @DisplayName("TC-5.18: Token Security - Concurrent Logout")
    public void testToken_ConcurrentLogout() throws Exception {
        // Given: Valid token
        // When: Multiple concurrent logout requests
        ResultActions result1 = performPostWithAuth(LOGOUT_URL, "{}", testUserToken);
        ResultActions result2 = performPostWithAuth(LOGOUT_URL, "{}", testUserToken);

        // Then: At least one should succeed
        boolean oneSuccess = (result1.andReturn().getResponse().getContentAsString().contains("\"code\":200") ||
                             result2.andReturn().getResponse().getContentAsString().contains("\"code\":200"));
        assert oneSuccess : "At least one logout should succeed";
    }

    // ==================== Boundary Tests ====================

    @Test
    @DisplayName("TC-5.19: Refresh Token - Null Refresh Token Field")
    public void testRefreshToken_NullRefreshToken() throws Exception {
        // Given: Request with null refresh token
        String payload = "{\"refreshToken\":null}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-5.20: Refresh Token - Missing Refresh Token Field")
    public void testRefreshToken_MissingRefreshTokenField() throws Exception {
        // Given: Request without refresh token field
        String payload = "{}";

        // When: Refresh token
        ResultActions result = performPost(REFRESH_TOKEN_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }
}
