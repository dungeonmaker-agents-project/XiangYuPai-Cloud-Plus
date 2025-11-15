package org.dromara.auth.test.api;

import org.dromara.auth.test.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Utility API Tests
 *
 * <p>Tests utility endpoints like phone registration check</p>
 *
 * @author XyPai Backend Team
 * @date 2025-11-14
 */
@DisplayName("Utility API Tests")
public class UtilityApiTest extends BaseControllerTest {

    private static final String CHECK_PHONE_URL = "/auth/check/phone";

    // ==================== Check Phone Registration Tests ====================

    @Test
    @DisplayName("TC-UTIL-01: Registered phone → isRegistered: true")
    public void testCheckPhoneRegistered() throws Exception {
        // Given: Registered phone number
        String payload = "{\"countryCode\":\"+86\",\"phoneNumber\":\"13800138000\"}";

        // When: Check phone
        ResultActions result = performPost(CHECK_PHONE_URL, payload);

        // Then: Should return isRegistered: true
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isRegistered").value(true));
    }

    @Test
    @DisplayName("TC-UTIL-02: Unregistered phone → isRegistered: false")
    public void testCheckPhoneUnregistered() throws Exception {
        // Given: Unregistered phone number
        String payload = "{\"countryCode\":\"+86\",\"phoneNumber\":\"19999999999\"}";

        // When: Check phone
        ResultActions result = performPost(CHECK_PHONE_URL, payload);

        // Then: Should return isRegistered: false
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.isRegistered").value(false));
    }

    @Test
    @DisplayName("TC-UTIL-03: Invalid phone format → 400")
    public void testCheckPhoneInvalidFormat() throws Exception {
        // Given: Invalid phone format
        String payload = "{\"countryCode\":\"+86\",\"phoneNumber\":\"123\"}";

        // When: Check phone
        ResultActions result = performPost(CHECK_PHONE_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-UTIL-04: Empty phone → 400")
    public void testCheckPhoneEmpty() throws Exception {
        // Given: Empty phone
        String payload = "{\"countryCode\":\"+86\",\"phoneNumber\":\"\"}";

        // When: Check phone
        ResultActions result = performPost(CHECK_PHONE_URL, payload);

        // Then: Validation error
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("TC-UTIL-05: Different country codes")
    public void testCheckPhoneDifferentCountryCodes() throws Exception {
        // Test with multiple country codes
        String[] countryCodes = {"+86", "+852", "+1", "+61"};

        for (String code : countryCodes) {
            // Given: Different country code
            String payload = String.format(
                "{\"countryCode\":\"%s\",\"phoneNumber\":\"1234567890\"}",
                code
            );

            // When: Check phone
            ResultActions result = performPost(CHECK_PHONE_URL, payload);

            // Then: Request should be processed (200 or 400 depending on validation)
            result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(anyOf(is(200), is(400))));
        }
    }
}
