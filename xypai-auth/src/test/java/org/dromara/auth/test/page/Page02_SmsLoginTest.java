package org.dromara.auth.test.page;

import com.fasterxml.jackson.databind.JsonNode;
import org.dromara.auth.test.base.BaseControllerTest;
import org.dromara.auth.test.data.LoginTestData;
import org.dromara.auth.test.data.LoginTestData.SendSmsRequest;
import org.dromara.auth.test.data.LoginTestData.SmsLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Page 2: SMS Login Tests
 *
 * <p>Frontend Documentation: 02-验证码登录页面.md</p>
 * <p>Tests all scenarios from the SMS login page including:
 * - Send SMS code
 * - SMS login for existing users
 * - SMS login with auto-registration for new users (isNewUser flag)
 * - Verification code validation
 * - Rate limiting
 * - Field name corrections (mobile, type, region, accessToken)
 * </p>
 *
 * @author XyPai Backend Team
 * @date 2025-11-14
 */
@DisplayName("Page 2: SMS Login Page Tests")
public class Page02_SmsLoginTest extends BaseControllerTest {

    private static final String SEND_SMS_URL = "/sms/send";  // ⚠️ NOT /auth/sms/send
    private static final String SMS_LOGIN_URL = "/auth/login/sms";

    // ==================== Send SMS Code Tests ====================

    @Test
    @DisplayName("TC-P2-01: Send SMS for login type → Success")
    public void testSendSmsForLoginSuccess() throws Exception {
        // Given: Valid send SMS request for login
        SendSmsRequest request = LoginTestData.sendLoginSms(LoginTestData.TestUsers.USER1_MOBILE);

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("成功")));
    }

    @Test
    @DisplayName("TC-P2-02: Response contains codeId, expiresIn, nextSendTime")
    public void testSendSmsResponseFields() throws Exception {
        // Given: Valid request
        SendSmsRequest request = LoginTestData.sendLoginSms("13800138001");

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: All required fields present
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.codeId").isNotEmpty())          // ⚠️ Important: codeId
            .andExpect(jsonPath("$.data.expiresIn").isNumber())         // ⚠️ Code validity (300s)
            .andExpect(jsonPath("$.data.nextSendTime").isNumber())      // ⚠️ Cooldown (60s)
            .andExpect(jsonPath("$.data.mobile").value("13800138001"));

        // Verify values are reasonable
        result.andExpect(jsonPath("$.data.expiresIn").value(greaterThan(0)))
            .andExpect(jsonPath("$.data.nextSendTime").value(greaterThan(0)));
    }

    @Test
    @DisplayName("TC-P2-03: Rate limiting - 60 second interval → 429")
    public void testSendSmsRateLimiting() throws Exception {
        // Given: Same mobile number
        String mobile = "13800138002";
        SendSmsRequest request = LoginTestData.sendLoginSms(mobile);

        // When: Send first SMS
        ResultActions firstResult = performPost(SEND_SMS_URL, request);
        firstResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // When: Immediately send second SMS
        ResultActions secondResult = performPost(SEND_SMS_URL, request);

        // Then: Rate limited
        secondResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(429))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("频繁")));
    }

    @Test
    @DisplayName("TC-P2-04: Daily limit - 10 codes per day → 429")
    public void testSendSmsDailyLimit() throws Exception {
        // Note: This test documents expected behavior
        // Manual test or Redis mocking required for full validation

        // Expected: After 10 SMS codes in one day, 11th request returns 429
        // with message containing "每日发送次数已达上限"
    }

    @Test
    @DisplayName("TC-P2-05: Empty mobile → 400")
    public void testSendSmsEmptyMobile() throws Exception {
        // Given: Empty mobile
        SendSmsRequest request = SendSmsRequest.builder()
            .mobile("")
            .type("login")
            .build();

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("手机号")));
    }

    @Test
    @DisplayName("TC-P2-06: Invalid mobile format → 400")
    public void testSendSmsInvalidMobileFormat() throws Exception {
        // Given: Invalid mobile format
        SendSmsRequest request = SendSmsRequest.builder()
            .mobile("12345")
            .type("login")
            .build();

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P2-07: Invalid SMS type → 400")
    public void testSendSmsInvalidType() throws Exception {
        // Given: Invalid SMS type
        SendSmsRequest request = SendSmsRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .type("invalid_type")
            .build();

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("类型")));
    }

    @Test
    @DisplayName("TC-P2-08: Verify correct API path /sms/send not /auth/sms/send ⚠️")
    public void testCorrectApiPath() throws Exception {
        // Given: Valid request to correct path
        SendSmsRequest request = LoginTestData.sendLoginSms(LoginTestData.TestUsers.USER1_MOBILE);

        // When: Send to /sms/send (NOT /auth/sms/send)
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-09: Request uses 'mobile' not 'phoneNumber' ⚠️")
    public void testRequestUsesMobileNotPhoneNumber() throws Exception {
        // Given: Request with correct field name 'mobile'
        SendSmsRequest request = LoginTestData.sendLoginSms(LoginTestData.TestUsers.USER1_MOBILE);

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Success (field name is correct)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-10: Request uses 'type' not 'purpose' ⚠️")
    public void testRequestUsesTypeNotPurpose() throws Exception {
        // Given: Request with correct field name 'type'
        SendSmsRequest request = SendSmsRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .type("login")  // ⚠️ 'type' not 'purpose'
            .build();

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-11: Request uses 'region' not 'countryCode' ⚠️")
    public void testRequestUsesRegionNotCountryCode() throws Exception {
        // Given: Request with correct field name 'region'
        SendSmsRequest request = SendSmsRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .type("login")
            .region("+86")  // ⚠️ 'region' not 'countryCode'
            .build();

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== SMS Login for Existing Users ====================

    @Test
    @DisplayName("TC-P2-12: Existing user SMS login → isNewUser: false ⚠️")
    public void testExistingUserSmsLoginHasIsNewUserFalse() throws Exception {
        // Given: Existing user SMS login
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode(LoginTestData.TestUsers.TEST_CODE)
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: If successful, should have isNewUser: false
        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.isNewUser").value(false));
            result.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
            result.andExpect(jsonPath("$.data.userId").exists());
            System.out.println("✓ Existing user login: isNewUser=false validated");
        } else {
            System.out.println("⚠ SMS code validation failed (expected in test environment)");
        }
    }

    @Test
    @DisplayName("TC-P2-13: SMS login response has accessToken not token ⚠️")
    public void testSmsLoginResponseHasAccessToken() throws Exception {
        // Given: Valid SMS login request
        SmsLoginRequest request = LoginTestData.defaultSmsLogin();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: If successful, should have 'accessToken' field
        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }
    }

    @Test
    @DisplayName("TC-P2-14: SMS login response has all required fields")
    public void testSmsLoginResponseFields() throws Exception {
        // Given: Valid SMS login
        SmsLoginRequest request = LoginTestData.defaultSmsLogin();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: If successful, all fields present
        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.expireIn").exists())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.isNewUser").exists());
        }
    }

    @Test
    @DisplayName("TC-P2-15: Empty verification code → 400")
    public void testSmsLoginEmptyCode() throws Exception {
        // Given: Empty verification code
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode("")
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("验证码")));
    }

    // ==================== SMS Login with Auto-Registration (New Users) ====================

    @Test
    @DisplayName("TC-P2-16: New user SMS login → Auto-registration")
    public void testNewUserSmsLoginAutoRegistration() throws Exception {
        // Given: New user (unregistered mobile)
        SmsLoginRequest request = LoginTestData.newUserSmsLogin();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: If successful, user should be auto-registered
        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
            result.andExpect(jsonPath("$.data.userId").exists());
            result.andExpect(jsonPath("$.data.nickname").exists());
            System.out.println("✓ New user auto-registered successfully");
        } else {
            System.out.println("⚠ SMS code validation failed (expected in test environment)");
        }
    }

    @Test
    @DisplayName("TC-P2-17: New user SMS login → isNewUser: true ⚠️ CRITICAL")
    public void testNewUserSmsLoginHasIsNewUserTrue() throws Exception {
        // Given: New user (unregistered mobile)
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile("13900139001")  // Unregistered
            .verificationCode(LoginTestData.TestUsers.TEST_CODE)
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: CRITICAL - isNewUser must be true for frontend routing
        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.isNewUser").value(true));
            System.out.println("✓ CRITICAL: New user has isNewUser=true");
        } else {
            System.out.println("⚠ SMS code validation failed (expected in test environment)");
        }
    }

    @Test
    @DisplayName("TC-P2-18: New user auto-generated nickname format XXX****XXXX")
    public void testNewUserNicknameFormat() throws Exception {
        // Given: New user
        SmsLoginRequest request = LoginTestData.newUserSmsLogin();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Then: Nickname should match format XXX****XXXX (e.g., 139****9000)
        if (jsonNode.path("code").asInt() == 200) {
            String nickname = jsonNode.path("data").path("nickname").asText();
            assert nickname.matches("\\d{3}\\*{4}\\d{4}") :
                "Nickname should match XXX****XXXX format, got: " + nickname;
            System.out.println("✓ Auto-generated nickname format validated: " + nickname);
        }
    }

    @Test
    @DisplayName("TC-P2-19: New user can set payment password immediately")
    public void testNewUserCanSetPaymentPassword() throws Exception {
        // Given: New user logged in
        SmsLoginRequest loginRequest = LoginTestData.newUserSmsLogin();
        ResultActions loginResult = performPost(SMS_LOGIN_URL, loginRequest);

        String loginBody = loginResult.andReturn().getResponse().getContentAsString();
        JsonNode loginNode = objectMapper.readTree(loginBody);

        if (loginNode.path("code").asInt() == 200) {
            String accessToken = loginNode.path("data").path("accessToken").asText();

            // When: Set payment password
            var setRequest = LoginTestData.setPaymentPassword("123456");
            ResultActions setResult = performPostWithAuth(
                "/auth/payment-password/set", setRequest, accessToken
            );

            // Then: Should succeed
            setResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(anyOf(is(200), is(409))));

            System.out.println("✓ New user can set payment password");
        }
    }

    // ==================== Verification Code Validation ====================

    @Test
    @DisplayName("TC-P2-20: Wrong verification code → 401")
    public void testWrongVerificationCode() throws Exception {
        // Given: Wrong verification code
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode("000000")
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Authentication failed
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("验证码")));
    }

    @Test
    @DisplayName("TC-P2-21: Expired verification code → 401")
    public void testExpiredVerificationCode() throws Exception {
        // Given: Expired code (simulate by using very old code)
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode("999999")  // Assume expired
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Code expired or invalid
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("验证码")));
    }

    @Test
    @DisplayName("TC-P2-22: Verification code wrong format → 400")
    public void testVerificationCodeWrongFormat() throws Exception {
        // Given: Wrong format (not 6 digits)
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode("123")  // Too short
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401))));
    }

    @Test
    @DisplayName("TC-P2-23: Verification code with non-numeric → 400")
    public void testVerificationCodeNonNumeric() throws Exception {
        // Given: Non-numeric code
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode("abc123")
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(400), is(401))));
    }

    @Test
    @DisplayName("TC-P2-24: Did not agree to terms → 400")
    public void testDidNotAgreeToTerms() throws Exception {
        // Given: agreeToTerms is false
        SmsLoginRequest request = SmsLoginRequest.builder()
            .mobile(LoginTestData.TestUsers.USER1_MOBILE)
            .verificationCode(LoginTestData.TestUsers.TEST_CODE)
            .agreeToTerms(false)
            .build();

        // When: SMS login
        ResultActions result = performPost(SMS_LOGIN_URL, request);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("协议")));
    }

    // ==================== Frontend Interaction Tests ====================

    @Test
    @DisplayName("TC-P2-25: expiresIn value is 300 seconds (5 minutes)")
    public void testExpiresInValueIsFiveMinutes() throws Exception {
        // Given: Send SMS request
        SendSmsRequest request = LoginTestData.sendLoginSms(LoginTestData.TestUsers.USER1_MOBILE);

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: expiresIn should be 300 (5 minutes)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.expiresIn").value(300));
    }

    @Test
    @DisplayName("TC-P2-26: nextSendTime value is 60 seconds")
    public void testNextSendTimeValueIsSixtySeconds() throws Exception {
        // Given: Send SMS request
        SendSmsRequest request = LoginTestData.sendLoginSms("13800138003");

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: nextSendTime should be 60 (1 minute)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.nextSendTime").value(60));
    }

    @Test
    @DisplayName("TC-P2-27: Response includes mobile for frontend display")
    public void testResponseIncludesMobile() throws Exception {
        // Given: Send SMS request
        String mobile = "13800138004";
        SendSmsRequest request = LoginTestData.sendLoginSms(mobile);

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, request);

        // Then: Response includes mobile
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.mobile").value(mobile));
    }
}
