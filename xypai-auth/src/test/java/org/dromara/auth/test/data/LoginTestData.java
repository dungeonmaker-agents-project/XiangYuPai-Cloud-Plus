package org.dromara.auth.test.data;

import lombok.Builder;
import lombok.Data;

/**
 * Test Data Builder - Login Requests
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public class LoginTestData {

    /**
     * 密码登录请求数据
     */
    @Data
    @Builder
    public static class PasswordLoginRequest {
        @Builder.Default
        private String countryCode = "+86";
        private String mobile;
        private String password;
        @Builder.Default
        private Boolean agreeToTerms = true;
        @Builder.Default
        private String clientId = "app";
        @Builder.Default
        private String grantType = "app_password";
    }

    /**
     * SMS登录请求数据
     */
    @Data
    @Builder
    public static class SmsLoginRequest {
        @Builder.Default
        private String countryCode = "+86";
        private String mobile;
        private String verificationCode;
        @Builder.Default
        private Boolean agreeToTerms = true;
        @Builder.Default
        private String clientId = "app";
        @Builder.Default
        private String grantType = "app_sms";
    }

    /**
     * 发送SMS请求数据
     */
    @Data
    @Builder
    public static class SendSmsRequest {
        private String mobile;
        private String type;  // login, register, reset
        @Builder.Default
        private String region = "+86";
    }

    /**
     * 测试用户数据
     */
    public static class TestUsers {
        // 已注册用户1
        public static final String USER1_MOBILE = "13800138000";
        public static final String USER1_PASSWORD = "password123";
        public static final Long USER1_ID = 1001L;
        public static final String USER1_NICKNAME = "测试用户1";

        // 已注册用户2
        public static final String USER2_MOBILE = "13800138001";
        public static final String USER2_PASSWORD = "test456789";
        public static final Long USER2_ID = 1002L;

        // 未注册用户（用于测试自动注册）
        public static final String NEW_USER_MOBILE = "13900139000";

        // 测试验证码
        public static final String TEST_CODE = "123456";
    }

    /**
     * 创建默认密码登录请求
     */
    public static PasswordLoginRequest defaultPasswordLogin() {
        return PasswordLoginRequest.builder()
            .mobile(TestUsers.USER1_MOBILE)
            .password(TestUsers.USER1_PASSWORD)
            .build();
    }

    /**
     * 创建默认SMS登录请求
     */
    public static SmsLoginRequest defaultSmsLogin() {
        return SmsLoginRequest.builder()
            .mobile(TestUsers.USER1_MOBILE)
            .verificationCode(TestUsers.TEST_CODE)
            .build();
    }

    /**
     * 创建新用户SMS登录请求
     */
    public static SmsLoginRequest newUserSmsLogin() {
        return SmsLoginRequest.builder()
            .mobile(TestUsers.NEW_USER_MOBILE)
            .verificationCode(TestUsers.TEST_CODE)
            .build();
    }

    /**
     * 创建发送登录验证码请求
     */
    public static SendSmsRequest sendLoginSms(String mobile) {
        return SendSmsRequest.builder()
            .mobile(mobile)
            .type("login")
            .build();
    }

    /**
     * 创建发送重置密码验证码请求
     */
    public static SendSmsRequest sendResetSms(String mobile) {
        return SendSmsRequest.builder()
            .mobile(mobile)
            .type("reset")
            .build();
    }

    /**
     * 检查手机号请求数据
     */
    @Data
    @Builder
    public static class CheckPhoneRequest {
        @Builder.Default
        private String countryCode = "+86";
        private String phoneNumber;
    }

    /**
     * 设置支付密码请求数据
     */
    @Data
    @Builder
    public static class SetPaymentPasswordRequest {
        private String paymentPassword;
        private String confirmPassword;
    }

    /**
     * 更新支付密码请求数据
     */
    @Data
    @Builder
    public static class UpdatePaymentPasswordRequest {
        private String oldPaymentPassword;
        private String newPaymentPassword;
        private String confirmPassword;
    }

    /**
     * 验证支付密码请求数据
     */
    @Data
    @Builder
    public static class VerifyPaymentPasswordRequest {
        private String paymentPassword;
    }

    /**
     * 验证码验证请求数据（忘记密码流程）
     */
    @Data
    @Builder
    public static class VerifyCodeRequest {
        @Builder.Default
        private String countryCode = "+86";
        private String mobile;
        private String verificationCode;
    }

    /**
     * 重置密码请求数据
     */
    @Data
    @Builder
    public static class ResetPasswordRequest {
        @Builder.Default
        private String countryCode = "+86";
        private String mobile;
        private String verificationCode;
        private String newPassword;
    }

    /**
     * 刷新Token请求数据
     */
    @Data
    @Builder
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    /**
     * 创建检查手机号请求
     */
    public static CheckPhoneRequest checkPhone(String phoneNumber) {
        return CheckPhoneRequest.builder()
            .phoneNumber(phoneNumber)
            .build();
    }

    /**
     * 创建设置支付密码请求
     */
    public static SetPaymentPasswordRequest setPaymentPassword(String password) {
        return SetPaymentPasswordRequest.builder()
            .paymentPassword(password)
            .confirmPassword(password)
            .build();
    }

    /**
     * 创建更新支付密码请求
     */
    public static UpdatePaymentPasswordRequest updatePaymentPassword(String oldPassword, String newPassword) {
        return UpdatePaymentPasswordRequest.builder()
            .oldPaymentPassword(oldPassword)
            .newPaymentPassword(newPassword)
            .confirmPassword(newPassword)
            .build();
    }

    /**
     * 创建验证支付密码请求
     */
    public static VerifyPaymentPasswordRequest verifyPaymentPassword(String password) {
        return VerifyPaymentPasswordRequest.builder()
            .paymentPassword(password)
            .build();
    }

    /**
     * 创建验证码验证请求
     */
    public static VerifyCodeRequest verifyCode(String mobile, String code) {
        return VerifyCodeRequest.builder()
            .mobile(mobile)
            .verificationCode(code)
            .build();
    }

    /**
     * 创建重置密码请求
     */
    public static ResetPasswordRequest resetPassword(String mobile, String code, String newPassword) {
        return ResetPasswordRequest.builder()
            .mobile(mobile)
            .verificationCode(code)
            .newPassword(newPassword)
            .build();
    }

    /**
     * 创建刷新Token请求
     */
    public static RefreshTokenRequest refreshToken(String refreshToken) {
        return RefreshTokenRequest.builder()
            .refreshToken(refreshToken)
            .build();
    }
}
