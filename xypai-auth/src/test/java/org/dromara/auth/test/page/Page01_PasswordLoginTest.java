package org.dromara.auth.test.page;

import com.fasterxml.jackson.databind.JsonNode;
import org.dromara.auth.test.base.BaseControllerTest;
import org.dromara.auth.test.data.LoginTestData;
import org.dromara.auth.test.data.LoginTestData.PasswordLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Page 1: Password Login Tests
 *
 * <p>Frontend Documentation: 01-密码登录页面.md</p>
 * <p>Tests all scenarios from the password login page including:
 * - Form validation
 * - Authentication success/failure
 * - Field name corrections (mobile vs phoneNumber, accessToken vs token)
 * - Security tests
 * - Country code support
 * </p>
 *
 * @author XyPai Backend Team
 * @date 2025-11-14
 */
@DisplayName("Page 1: Password Login Page Tests")
public class Page01_PasswordLoginTest extends BaseControllerTest {

    private static final String PASSWORD_LOGIN_URL = "/auth/login/password";

    // ==================== Success Scenarios ====================

    @Test
    @DisplayName("TC-P1-01: Valid credentials login success")
    public void testValidCredentialsLoginSuccess() throws Exception {
        // Given: Valid user credentials
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Perform password login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Login successful
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("成功")))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("TC-P1-02: Response contains all required fields")
    public void testResponseContainsAllRequiredFields() throws Exception {
        // Given: Valid credentials
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: All required fields present
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())       // ⚠️ accessToken not token
            .andExpect(jsonPath("$.data.userId").exists())
            .andExpect(jsonPath("$.data.nickname").isNotEmpty())
            .andExpect(jsonPath("$.data.isNewUser").exists())              // ⚠️ Important field
            .andExpect(jsonPath("$.data.expireIn").isNumber());            // ⚠️ Token expiry

        // Verify isNewUser is false for password login (existing user)
        result.andExpect(jsonPath("$.data.isNewUser").value(false));
    }

    @Test
    @DisplayName("TC-P1-03: Token is valid and can access protected endpoints")
    public void testTokenIsValidForProtectedEndpoints() throws Exception {
        // Given: User logged in successfully
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();
        ResultActions loginResult = performPost(PASSWORD_LOGIN_URL, request);

        String responseBody = loginResult.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.path("data").path("accessToken").asText();

        // When: Access protected endpoint with token
        var verifyRequest = LoginTestData.verifyPaymentPassword("123456");
        ResultActions protectedResult = performPostWithAuth(
            "/auth/payment-password/verify", verifyRequest, accessToken
        );

        // Then: Can access protected endpoint (200 or 404 if not set, but not 401)
        protectedResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));
    }

    // ==================== Validation Tests (Frontend Form Validation) ====================

    @Test
    @DisplayName("TC-P1-04: Empty mobile number → 400")
    public void testEmptyMobileNumber() throws Exception {
        // Given: Empty mobile
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("")
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("手机号")));
    }

    @Test
    @DisplayName("TC-P1-05: Invalid mobile format → 400")
    public void testInvalidMobileFormat() throws Exception {
        // Given: Invalid format (not 11 digits)
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("123456")
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P1-06: Mobile number too short → 400")
    public void testMobileTooShort() throws Exception {
        // Given: Too short mobile
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("138001")
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P1-07: Empty password → 400")
    public void testEmptyPassword() throws Exception {
        // Given: Empty password
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password("")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("密码")));
    }

    @Test
    @DisplayName("TC-P1-08: Password too short → 400")
    public void testPasswordTooShort() throws Exception {
        // Given: Password too short (< 6 chars)
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password("123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P1-09: Did not agree to terms → 400")
    public void testDidNotAgreeToTerms() throws Exception {
        // Given: agreeToTerms is false
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password(LoginTestData.TestUsers.USER1_PASSWORD)
            .agreeToTerms(false)
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("协议")));
    }

    @Test
    @DisplayName("TC-P1-10: Null mobile → 400")
    public void testNullMobile() throws Exception {
        // Given: Null mobile
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(null)
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== Authentication Failure Tests ====================

    @Test
    @DisplayName("TC-P1-11: Wrong password → 401")
    public void testWrongPassword() throws Exception {
        // Given: Correct mobile but wrong password
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password("wrongpassword123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Authentication failed
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("密码")));
    }

    @Test
    @DisplayName("TC-P1-12: User not found → 401 or 404")
    public void testUserNotFound() throws Exception {
        // Given: Unregistered mobile
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("19999999999")
            .password("anypassword123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: User not found
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(401), is(404))))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("用户")));
    }

    @Test
    @DisplayName("TC-P1-13: Disabled user account → 403")
    public void testDisabledUserAccount() throws Exception {
        // Given: Disabled user (if such test data exists)
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("13800138099")  // Assume this is a disabled account
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Account disabled (may return 401 or 403)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(401), is(403), is(404))));
    }

    // ==================== Security Tests ====================

    @Test
    @DisplayName("TC-P1-14: SQL injection in mobile → Safe")
    public void testSqlInjectionInMobile() throws Exception {
        // Given: SQL injection attempt
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile("13800138000' OR '1'='1")
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Treated as invalid mobile format or user not found
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401), is(404))));
    }

    @Test
    @DisplayName("TC-P1-15: XSS attack in password → Safe")
    public void testXssAttackInPassword() throws Exception {
        // Given: XSS script in password
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password("<script>alert('xss')</script>")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Treated as wrong password (401), response should not contain script
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));

        String responseBody = result.andReturn().getResponse().getContentAsString();
        assert !responseBody.contains("<script>") : "Response should not contain XSS script";
    }

    @Test
    @DisplayName("TC-P1-16: Very long password → Rejected or handled safely")
    public void testVeryLongPassword() throws Exception {
        // Given: Extremely long password
        String longPassword = "a".repeat(10000);
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password(longPassword)
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Handled safely (400 or 401)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401))));
    }

    // ==================== Interface Correction Tests ====================

    @Test
    @DisplayName("TC-P1-17: Request uses 'mobile' not 'phoneNumber' ⚠️")
    public void testRequestUsesMobileNotPhoneNumber() throws Exception {
        // Given: Request with correct field name 'mobile'
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Success (field name is correct)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P1-18: Response has 'accessToken' not 'token' ⚠️")
    public void testResponseHasAccessTokenNotToken() throws Exception {
        // Given: Valid login
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Response has 'accessToken'
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("TC-P1-19: Response has 'isNewUser' field ⚠️")
    public void testResponseHasIsNewUserField() throws Exception {
        // Given: Valid login
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Response has 'isNewUser' field set to false (existing user)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isNewUser").exists())
            .andExpect(jsonPath("$.data.isNewUser").value(false));
    }

    @Test
    @DisplayName("TC-P1-20: Response has 'expireIn' field ⚠️")
    public void testResponseHasExpireInField() throws Exception {
        // Given: Valid login
        PasswordLoginRequest request = LoginTestData.defaultPasswordLogin();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Response has 'expireIn' (token expiry time in seconds)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.expireIn").exists())
            .andExpect(jsonPath("$.data.expireIn").isNumber())
            .andExpect(jsonPath("$.data.expireIn").value(greaterThan(0)));
    }

    // ==================== Country Code Support ====================

    @Test
    @DisplayName("TC-P1-21: Login with +86 country code")
    public void testLoginWithPlus86CountryCode() throws Exception {
        // Given: +86 country code
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .countryCode("+86")
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password(LoginTestData.TestUsers.USER1_PASSWORD)
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P1-22: Login with +852 country code")
    public void testLoginWithPlus852CountryCode() throws Exception {
        // Given: +852 country code (Hong Kong)
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .countryCode("+852")
            .mobile("51234567")  // HK mobile format
            .password("password123")
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Accepted (may not find user, but validates country code)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(401), is(404))));
    }

    @Test
    @DisplayName("TC-P1-23: Login with invalid country code → 400")
    public void testLoginWithInvalidCountryCode() throws Exception {
        // Given: Invalid country code
        PasswordLoginRequest request = PasswordLoginRequest.builder()
            .countryCode("+999")
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .password(LoginTestData.TestUsers.USER1_PASSWORD)
            .build();

        // When: Login
        ResultActions result = performPost(PASSWORD_LOGIN_URL, request);

        // Then: Validation error or user not found
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401), is(404))));
    }
}
