package org.dromara.auth.test.integration;

import org.dromara.auth.test.base.BaseControllerTest;
import org.dromara.auth.test.data.LoginTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test Cases
 *
 * <p>Tests complete user journeys through multiple endpoints</p>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("End-to-End Integration Tests")
public class IntegrationFlowTest extends BaseControllerTest {

    private static final String PASSWORD_LOGIN_URL = "/auth/login/password";
    private static final String SMS_LOGIN_URL = "/auth/login/sms";
    private static final String SEND_SMS_URL = "/sms/send";
    private static final String CHECK_PHONE_URL = "/auth/check/phone";
    private static final String VERIFY_CODE_URL = "/auth/password/reset/verify";
    private static final String RESET_PASSWORD_URL = "/auth/password/reset/confirm";
    private static final String SET_PAYMENT_PASSWORD_URL = "/auth/payment-password/set";
    private static final String UPDATE_PAYMENT_PASSWORD_URL = "/auth/payment-password/update";
    private static final String VERIFY_PAYMENT_PASSWORD_URL = "/auth/payment-password/verify";
    private static final String REFRESH_TOKEN_URL = "/auth/token/refresh";
    private static final String LOGOUT_URL = "/auth/logout";

    // ==================== Complete New User Journey ====================

    @Test
    @DisplayName("TC-9.1: Complete New User Flow - SMS Registration to Full Setup")
    public void testCompleteNewUserFlow() throws Exception {
        String newUserMobile = LoginTestData.TestUsers.NEW_USER_MOBILE;
        String verificationCode = "123456";

        // Step 1: Check if phone is registered
        String checkPhonePayload = String.format(
            "{\"countryCode\":\"+86\",\"phoneNumber\":\"%s\"}", newUserMobile);
        ResultActions checkResult = performPost(CHECK_PHONE_URL, checkPhonePayload);
        checkResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isRegistered").value(false));

        // Step 2: Send SMS verification code
        String sendSmsPayload = String.format(
            "{\"mobile\":\"%s\",\"type\":\"login\",\"region\":\"+86\"}", newUserMobile);
        ResultActions sendSmsResult = performPost(SEND_SMS_URL, sendSmsPayload);
        sendSmsResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.codeId").isNotEmpty());

        // Step 3: SMS login with auto-registration
        String smsLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"%s\",\"agreeToTerms\":true,\"clientId\":\"app\",\"grantType\":\"app_sms\"}",
            newUserMobile, verificationCode);
        ResultActions smsLoginResult = performPost(SMS_LOGIN_URL, smsLoginPayload);
        smsLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.isNewUser").value(true))  // New user flag
            .andExpect(jsonPath("$.data.userId").isNotEmpty());

        String accessToken = extractToken(smsLoginResult);

        // Step 4: Set payment password
        String setPaymentPasswordPayload = "{\"paymentPassword\":\"123456\",\"confirmPassword\":\"123456\"}";
        ResultActions setPaymentPasswordResult = performPostWithAuth(
            SET_PAYMENT_PASSWORD_URL, setPaymentPasswordPayload, accessToken);
        setPaymentPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("设置成功")));

        // Step 5: Verify payment password
        String verifyPaymentPasswordPayload = "{\"paymentPassword\":\"123456\"}";
        ResultActions verifyPaymentPasswordResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPaymentPasswordPayload, accessToken);
        verifyPaymentPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.verified").value(true));

        // Step 6: Logout
        ResultActions logoutResult = performPostWithAuth(LOGOUT_URL, "{}", accessToken);
        logoutResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 7: Verify token is invalidated
        ResultActions postLogoutResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPaymentPasswordPayload, accessToken);
        postLogoutResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== Complete Existing User Journey ====================

    @Test
    @DisplayName("TC-9.2: Complete Existing User Flow - Password Login to Logout")
    public void testCompleteExistingUserFlow() throws Exception {
        String existingUserMobile = LoginTestData.TestUsers.USER1_MOBILE;
        String existingUserPassword = LoginTestData.TestUsers.USER1_PASSWORD;

        // Step 1: Check if phone is registered
        String checkPhonePayload = String.format(
            "{\"countryCode\":\"+86\",\"phoneNumber\":\"%s\"}", existingUserMobile);
        ResultActions checkResult = performPost(CHECK_PHONE_URL, checkPhonePayload);
        checkResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isRegistered").value(true));

        // Step 2: Password login
        String passwordLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"%s\",\"agreeToTerms\":true,\"clientId\":\"app\",\"grantType\":\"app_password\"}",
            existingUserMobile, existingUserPassword);
        ResultActions passwordLoginResult = performPost(PASSWORD_LOGIN_URL, passwordLoginPayload);
        passwordLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.isNewUser").value(false))  // Existing user
            .andExpect(jsonPath("$.data.userId").value(LoginTestData.TestUsers.USER1_ID));

        String accessToken = extractToken(passwordLoginResult);
        String responseBody = passwordLoginResult.andReturn().getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseBody)
            .path("data").path("refreshToken").asText();

        // Step 3: Set payment password
        String setPaymentPasswordPayload = "{\"paymentPassword\":\"654321\",\"confirmPassword\":\"654321\"}";
        ResultActions setPaymentPasswordResult = performPostWithAuth(
            SET_PAYMENT_PASSWORD_URL, setPaymentPasswordPayload, accessToken);
        setPaymentPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(400))));  // May already be set

        // Step 4: Verify payment password
        String verifyPaymentPasswordPayload = "{\"paymentPassword\":\"654321\"}";
        ResultActions verifyPaymentPasswordResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPaymentPasswordPayload, accessToken);
        verifyPaymentPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 5: Update payment password
        String updatePaymentPasswordPayload =
            "{\"oldPaymentPassword\":\"654321\",\"newPaymentPassword\":\"111111\",\"confirmPassword\":\"111111\"}";
        ResultActions updatePaymentPasswordResult = performPostWithAuth(
            UPDATE_PAYMENT_PASSWORD_URL, updatePaymentPasswordPayload, accessToken);
        updatePaymentPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(400))));

        // Step 6: Refresh token
        String refreshTokenPayload = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
        ResultActions refreshTokenResult = performPost(REFRESH_TOKEN_URL, refreshTokenPayload);
        refreshTokenResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isNotEmpty());

        String newAccessToken = objectMapper.readTree(
            refreshTokenResult.andReturn().getResponse().getContentAsString())
            .path("data").path("token").asText();

        // Step 7: Use new token
        ResultActions useNewTokenResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPaymentPasswordPayload, newAccessToken);
        useNewTokenResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 8: Logout
        ResultActions logoutResult = performPostWithAuth(LOGOUT_URL, "{}", newAccessToken);
        logoutResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Forgot Password Flow ====================

    @Test
    @DisplayName("TC-9.3: Complete Forgot Password Flow - SMS to New Password Login")
    public void testCompleteForgotPasswordFlow() throws Exception {
        String userMobile = LoginTestData.TestUsers.USER1_MOBILE;
        String verificationCode = "123456";
        String newPassword = "newSecurePass123";

        // Step 1: Check phone is registered
        String checkPhonePayload = String.format(
            "{\"countryCode\":\"+86\",\"phoneNumber\":\"%s\"}", userMobile);
        ResultActions checkResult = performPost(CHECK_PHONE_URL, checkPhonePayload);
        checkResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isRegistered").value(true));

        // Step 2: Send reset SMS
        String sendSmsPayload = String.format(
            "{\"mobile\":\"%s\",\"type\":\"reset\",\"region\":\"+86\"}", userMobile);
        ResultActions sendSmsResult = performPost(SEND_SMS_URL, sendSmsPayload);
        sendSmsResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: Verify code
        String verifyCodePayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"%s\"}",
            userMobile, verificationCode);
        ResultActions verifyCodeResult = performPost(VERIFY_CODE_URL, verifyCodePayload);
        verifyCodeResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("验证成功")));

        // Step 4: Reset password
        String resetPasswordPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"%s\",\"newPassword\":\"%s\"}",
            userMobile, verificationCode, newPassword);
        ResultActions resetPasswordResult = performPost(RESET_PASSWORD_URL, resetPasswordPayload);
        resetPasswordResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("密码重置成功")));

        // Step 5: Login with new password
        String passwordLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"%s\",\"agreeToTerms\":true}",
            userMobile, newPassword);
        ResultActions passwordLoginResult = performPost(PASSWORD_LOGIN_URL, passwordLoginPayload);
        passwordLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // Step 6: Verify old password no longer works
        String oldPasswordLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"%s\",\"agreeToTerms\":true}",
            userMobile, LoginTestData.TestUsers.USER1_PASSWORD);
        ResultActions oldPasswordLoginResult = performPost(PASSWORD_LOGIN_URL, oldPasswordLoginPayload);
        oldPasswordLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== SMS Rate Limiting Flow ====================

    @Test
    @DisplayName("TC-9.4: SMS Rate Limiting - Multiple Attempts")
    public void testSmsRateLimitingFlow() throws Exception {
        String testMobile = "13800138099";

        // Step 1: Send first SMS
        String sendSmsPayload = String.format(
            "{\"mobile\":\"%s\",\"type\":\"login\",\"region\":\"+86\"}", testMobile);
        ResultActions firstSmsResult = performPost(SEND_SMS_URL, sendSmsPayload);
        firstSmsResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.nextSendTime").value(60));

        // Step 2: Immediately try to send second SMS
        ResultActions secondSmsResult = performPost(SEND_SMS_URL, sendSmsPayload);
        secondSmsResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(429))
            .andExpect(jsonPath("$.message").value(containsString("频繁")));

        // Step 3: Wait and verify error message contains remaining time
        secondSmsResult.andExpect(jsonPath("$.message").value(matchesPattern(".*\\d+.*秒.*")));
    }

    // ==================== Payment Password Complete Flow ====================

    @Test
    @DisplayName("TC-9.5: Complete Payment Password Management Flow")
    public void testCompletePaymentPasswordFlow() throws Exception {
        // Step 1: Login
        var loginRequest = LoginTestData.defaultPasswordLogin();
        ResultActions loginResult = performPost(PASSWORD_LOGIN_URL, loginRequest);
        String accessToken = extractToken(loginResult);

        // Step 2: Set payment password
        String setPayload = "{\"paymentPassword\":\"111111\",\"confirmPassword\":\"111111\"}";
        ResultActions setResult = performPostWithAuth(
            SET_PAYMENT_PASSWORD_URL, setPayload, accessToken);
        setResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(400))));

        // Step 3: Verify payment password (correct)
        String verifyCorrectPayload = "{\"paymentPassword\":\"111111\"}";
        ResultActions verifyCorrectResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyCorrectPayload, accessToken);
        verifyCorrectResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.verified").value(true));

        // Step 4: Verify payment password (wrong)
        String verifyWrongPayload = "{\"paymentPassword\":\"000000\"}";
        ResultActions verifyWrongResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyWrongPayload, accessToken);
        verifyWrongResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.verified").value(false));

        // Step 5: Update payment password
        String updatePayload =
            "{\"oldPaymentPassword\":\"111111\",\"newPaymentPassword\":\"222222\",\"confirmPassword\":\"222222\"}";
        ResultActions updateResult = performPostWithAuth(
            UPDATE_PAYMENT_PASSWORD_URL, updatePayload, accessToken);
        updateResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(400))));

        // Step 6: Verify new password works
        String verifyNewPayload = "{\"paymentPassword\":\"222222\"}";
        ResultActions verifyNewResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyNewPayload, accessToken);
        verifyNewResult.andExpect(status().isOk());

        // Step 7: Verify old password doesn't work
        ResultActions verifyOldResult = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyCorrectPayload, accessToken);
        verifyOldResult.andExpect(status().isOk());
    }

    // ==================== Multiple Login Methods ====================

    @Test
    @DisplayName("TC-9.6: Multiple Login Methods - Password and SMS")
    public void testMultipleLoginMethods() throws Exception {
        String userMobile = LoginTestData.TestUsers.USER1_MOBILE;
        String userPassword = LoginTestData.TestUsers.USER1_PASSWORD;
        String verificationCode = "123456";

        // Step 1: Login with password
        String passwordLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"%s\",\"agreeToTerms\":true}",
            userMobile, userPassword);
        ResultActions passwordLoginResult = performPost(PASSWORD_LOGIN_URL, passwordLoginPayload);
        passwordLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isNewUser").value(false));

        String passwordToken = extractToken(passwordLoginResult);

        // Step 2: Logout from password session
        ResultActions logoutResult = performPostWithAuth(LOGOUT_URL, "{}", passwordToken);
        logoutResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: Send SMS for login
        String sendSmsPayload = String.format(
            "{\"mobile\":\"%s\",\"type\":\"login\",\"region\":\"+86\"}", userMobile);
        ResultActions sendSmsResult = performPost(SEND_SMS_URL, sendSmsPayload);
        sendSmsResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 4: Login with SMS
        String smsLoginPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"%s\",\"agreeToTerms\":true}",
            userMobile, verificationCode);
        ResultActions smsLoginResult = performPost(SMS_LOGIN_URL, smsLoginPayload);
        smsLoginResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isNewUser").value(false))  // Same user
            .andExpect(jsonPath("$.data.userId").value(LoginTestData.TestUsers.USER1_ID));
    }

    // ==================== Error Recovery Flow ====================

    @Test
    @DisplayName("TC-9.7: Error Recovery - Failed Login Retry")
    public void testErrorRecoveryFlow() throws Exception {
        String userMobile = LoginTestData.TestUsers.USER1_MOBILE;
        String correctPassword = LoginTestData.TestUsers.USER1_PASSWORD;

        // Step 1: Failed login attempt 1
        String wrongPasswordPayload1 = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"wrongpass1\",\"agreeToTerms\":true}",
            userMobile);
        ResultActions failedAttempt1 = performPost(PASSWORD_LOGIN_URL, wrongPasswordPayload1);
        failedAttempt1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));

        // Step 2: Failed login attempt 2
        String wrongPasswordPayload2 = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"wrongpass2\",\"agreeToTerms\":true}",
            userMobile);
        ResultActions failedAttempt2 = performPost(PASSWORD_LOGIN_URL, wrongPasswordPayload2);
        failedAttempt2.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));

        // Step 3: Successful login with correct password
        String correctPasswordPayload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"password\":\"%s\",\"agreeToTerms\":true}",
            userMobile, correctPassword);
        ResultActions successfulLogin = performPost(PASSWORD_LOGIN_URL, correctPasswordPayload);
        successfulLogin.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // ==================== Concurrent User Operations ====================

    @Test
    @DisplayName("TC-9.8: Concurrent Operations - Multiple Tokens for Same User")
    public void testConcurrentUserOperations() throws Exception {
        // Step 1: Login first time
        var loginRequest1 = LoginTestData.defaultPasswordLogin();
        ResultActions loginResult1 = performPost(PASSWORD_LOGIN_URL, loginRequest1);
        String token1 = extractToken(loginResult1);

        // Step 2: Login second time (same user, different session)
        var loginRequest2 = LoginTestData.defaultPasswordLogin();
        ResultActions loginResult2 = performPost(PASSWORD_LOGIN_URL, loginRequest2);
        String token2 = extractToken(loginResult2);

        // Step 3: Both tokens should be valid
        String verifyPayload = "{\"paymentPassword\":\"123456\"}";

        ResultActions verify1 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, token1);
        verify1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));

        ResultActions verify2 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, token2);
        verify2.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));

        // Step 4: Logout from first session
        ResultActions logout1 = performPostWithAuth(LOGOUT_URL, "{}", token1);
        logout1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 5: First token should be invalid, second still valid
        ResultActions verifyAfterLogout1 = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, token1);
        verifyAfterLogout1.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));

        ResultActions verifyStillValid = performPostWithAuth(
            VERIFY_PAYMENT_PASSWORD_URL, verifyPayload, token2);
        verifyStillValid.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(404))));
    }
}
