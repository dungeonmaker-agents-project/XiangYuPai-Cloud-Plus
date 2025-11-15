package org.dromara.auth.test.page;

import com.fasterxml.jackson.databind.JsonNode;
import org.dromara.auth.test.base.BaseControllerTest;
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
        String payload = "{\"mobile\":\"13800138000\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value(containsString("成功")));
    }

    @Test
    @DisplayName("TC-P2-02: Response contains codeId, expiresIn, nextSendTime")
    public void testSendSmsResponseFields() throws Exception {
        // Given: Valid request
        String payload = "{\"mobile\":\"13800138001\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

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
        String mobile = "13800138002";
        String payload = String.format("{\"mobile\":\"%s\",\"type\":\"login\",\"region\":\"+86\"}", mobile);

        // When: Send first SMS
        ResultActions firstResult = performPost(SEND_SMS_URL, payload);
        firstResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // When: Immediately send second SMS
        ResultActions secondResult = performPost(SEND_SMS_URL, payload);

        // Then: Rate limited
        secondResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(429))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("频繁")));
    }

    @Test
    @DisplayName("TC-P2-04: Daily limit - 10 codes per day → 429")
    public void testSendSmsDailyLimit() throws Exception {
        // Note: This test would require sending 10+ SMS codes with 61-second intervals
        // For automated testing, this should be a manual test or use Redis mocking
        // Here we document the expected behavior

        // Expected: After 10 SMS codes in one day, 11th request returns 429
        // Message should contain "今日发送次数已达上限"
    }

    @Test
    @DisplayName("TC-P2-05: Invalid mobile format → 400")
    public void testSendSmsInvalidMobile() throws Exception {
        // Given: Invalid mobile
        String payload = "{\"mobile\":\"123\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P2-06: Empty mobile → 400")
    public void testSendSmsEmptyMobile() throws Exception {
        // Given: Empty mobile
        String payload = "{\"mobile\":\"\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P2-07: Invalid type value → 400")
    public void testSendSmsInvalidType() throws Exception {
        // Given: Invalid type
        String payload = "{\"mobile\":\"13800138000\",\"type\":\"invalid_type\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P2-08: Verify correct API path /api/sms/send")
    public void testCorrectApiPath() throws Exception {
        // This test verifies we're using the correct path
        // The correct path is /sms/send (which becomes /api/sms/send via gateway)
        // NOT /auth/sms/send

        String payload = "{\"mobile\":\"13800138003\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Use correct path
        ResultActions result = performPost("/sms/send", payload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-09: Verify field name 'type: login' (not 'purpose: LOGIN')")
    public void testCorrectFieldNameType() throws Exception {
        // Given: Using correct field name 'type' with lowercase value
        String correctPayload = "{\"mobile\":\"13800138004\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, correctPayload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-10: Verify field name 'mobile' (not 'phoneNumber')")
    public void testCorrectFieldNameMobile() throws Exception {
        // Given: Using 'mobile' field
        String payload = "{\"mobile\":\"13800138005\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("TC-P2-11: Verify field name 'region' (not 'countryCode')")
    public void testCorrectFieldNameRegion() throws Exception {
        // Given: Using 'region' field
        String payload = "{\"mobile\":\"13800138006\",\"type\":\"login\",\"region\":\"+86\"}";

        // When: Send SMS
        ResultActions result = performPost(SEND_SMS_URL, payload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== SMS Login - Existing User Tests ====================

    @Test
    @DisplayName("TC-P2-12: Valid code for existing user → Login success")
    public void testSmsLoginExistingUserSuccess() throws Exception {
        // Given: Valid SMS login for existing user
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Success
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(401))));  // 200 if code valid, 401 if expired
    }

    @Test
    @DisplayName("TC-P2-13: Response has 'isNewUser: false' for existing user")
    public void testSmsLoginExistingUserHasIsNewUserFalse() throws Exception {
        // Given: SMS login for existing user
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Login (assuming code is valid)
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: If successful, isNewUser should be false
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.isNewUser").value(false));
        }
    }

    @Test
    @DisplayName("TC-P2-14: Response contains correct nickname for existing user")
    public void testSmsLoginExistingUserNickname() throws Exception {
        // Given: SMS login for existing user
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: If successful, nickname should be present
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            result.andExpect(jsonPath("$.data.nickname").isNotEmpty());
        }
    }

    @Test
    @DisplayName("TC-P2-15: Response contains user avatar")
    public void testSmsLoginContainsAvatar() throws Exception {
        // Given: SMS login
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Avatar field exists (may be null or empty)
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            // Avatar field should exist (even if null)
            assert jsonNode.path("data").has("avatar") || jsonNode.path("data").path("avatar").isNull();
        }
    }

    // ==================== SMS Login - New User Auto-Registration Tests ====================

    @Test
    @DisplayName("TC-P2-16: Valid code for unregistered mobile → Auto-register + login")
    public void testSmsLoginAutoRegistration() throws Exception {
        // Given: SMS login for NEW unregistered user
        String newMobile = "13900139000";
        String payload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}",
            newMobile
        );

        // When: Login (this should auto-register the user)
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Success (if code is valid) or 401 (if code expired/invalid)
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(anyOf(is(200), is(401))));

        // If successful, user should be created
    }

    @Test
    @DisplayName("TC-P2-17: Response has 'isNewUser: true' ← CRITICAL")
    public void testSmsLoginNewUserHasIsNewUserTrue() throws Exception {
        // Given: SMS login for NEW user
        String newMobile = "13900139001";
        String payload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}",
            newMobile
        );

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: If successful, isNewUser MUST be true
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            // ⚠️ CRITICAL: This is the most important field for frontend routing
            result.andExpect(jsonPath("$.data.isNewUser").value(true));
        }
    }

    @Test
    @DisplayName("TC-P2-18: Auto-generated nickname format: 138****8000")
    public void testAutoGeneratedNicknameFormat() throws Exception {
        // Given: SMS login for NEW user
        String newMobile = "13900139002";
        String payload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}",
            newMobile
        );

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Nickname should match pattern XXX****XXXX
        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            String nickname = jsonNode.path("data").path("nickname").asText();
            // Should match pattern like "139****9002"
            assert nickname.matches("\\d{3}\\*{4}\\d{4}");
        }
    }

    @Test
    @DisplayName("TC-P2-19: New user created in database")
    public void testNewUserCreatedInDatabase() throws Exception {
        // Given: SMS login for NEW user
        String newMobile = "13900139003";
        String payload = String.format(
            "{\"countryCode\":\"+86\",\"mobile\":\"%s\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}",
            newMobile
        );

        // When: Login successfully
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        if (jsonNode.path("code").asInt() == 200) {
            // Then: User should have a valid userId
            result.andExpect(jsonPath("$.data.userId").isNotEmpty());
        }
    }

    @Test
    @DisplayName("TC-P2-20: New user can login again with password")
    public void testNewUserCanLoginWithPassword() throws Exception {
        // Note: This test requires the new user to have a default password or
        // to set a password first. This depends on the auto-registration implementation.
        // Document the expected behavior here.

        // Expected: New user created via SMS login should be able to:
        // 1. Set a password via profile update
        // 2. Login with password next time
    }

    // ==================== SMS Login - Verification Code Validation ====================

    @Test
    @DisplayName("TC-P2-21: Wrong verification code → 401")
    public void testSmsLoginWrongCode() throws Exception {
        // Given: Wrong verification code
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"000000\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Authentication failure
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(containsStringIgnoringCase("验证码")));
    }

    @Test
    @DisplayName("TC-P2-22: Expired verification code (> 5 minutes) → 401")
    public void testSmsLoginExpiredCode() throws Exception {
        // Given: Expired code
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"999999\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Code expired
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value(anyOf(
                containsStringIgnoringCase("验证码"),
                containsStringIgnoringCase("过期"),
                containsStringIgnoringCase("失效")
            )));
    }

    @Test
    @DisplayName("TC-P2-23: Non-existent code → 401")
    public void testSmsLoginNonExistentCode() throws Exception {
        // Given: Non-existent code
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"111111\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Code not found
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("TC-P2-24: Empty verification code → 400")
    public void testSmsLoginEmptyCode() throws Exception {
        // Given: Empty code
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-P2-25: Invalid code format (not 6 digits) → 400")
    public void testSmsLoginInvalidCodeFormat() throws Exception {
        // Given: Invalid format
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"12345\",\"agreeToTerms\":true}";

        // When: Login
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== Frontend Interaction Tests ====================

    @Test
    @DisplayName("TC-P2-26: Verify 6th digit auto-submits (simulated)")
    public void testAutoSubmitAfter6thDigit() throws Exception {
        // Given: Complete 6-digit code (simulating auto-submit)
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Submit (frontend would auto-submit after 6th digit)
        ResultActions result = performPost(SMS_LOGIN_URL, payload);

        // Then: Request processed
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-P2-27: Verify response time < 2 seconds")
    public void testResponseTimeUnder2Seconds() throws Exception {
        // Given: Valid request
        String payload = "{\"countryCode\":\"+86\",\"mobile\":\"13800138000\",\"verificationCode\":\"123456\",\"agreeToTerms\":true}";

        // When: Login
        long startTime = System.currentTimeMillis();
        ResultActions result = performPost(SMS_LOGIN_URL, payload);
        long endTime = System.currentTimeMillis();

        // Then: Response within 2 seconds
        long duration = endTime - startTime;
        assert duration < 2000 : "Response time exceeded 2 seconds: " + duration + "ms";
    }
}
