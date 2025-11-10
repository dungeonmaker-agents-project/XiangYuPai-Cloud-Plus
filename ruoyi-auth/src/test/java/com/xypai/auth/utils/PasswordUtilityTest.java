package com.xypai.auth.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具测试类 - 独立运行版本
 * 
 * 这个类可以直接用 main 方法运行，不需要 Spring Boot 环境
 * 用于快速生成和验证密码
 * 
 * 使用方法：
 * 1. 直接运行 main 方法
 * 2. 或在命令行: java PasswordUtilityTest.java
 * 
 * @author Test Generator
 * @date 2025-11-07
 */
public class PasswordUtilityTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       密码加密工具 - 独立运行版本");
        System.out.println("========================================\n");
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // ==================== 场景1: 生成新密码 ====================
        System.out.println("【场景1】生成新密码");
        System.out.println("----------------------------------------");
        
        String[] passwordsToGenerate = {
            "test123456",
            "password123",
            "Admin@123"
        };
        
        for (String password : passwordsToGenerate) {
            String encoded = encoder.encode(password);
            System.out.println("原始密码: " + password);
            System.out.println("加密密码: " + encoded);
            System.out.println("密码长度: " + encoded.length());
            System.out.println();
        }
        
        // ==================== 场景2: 验证密码 ====================
        System.out.println("\n【场景2】验证密码匹配");
        System.out.println("----------------------------------------");
        
        // 示例：验证密码是否匹配
        String rawPassword = "test123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("测试密码: " + rawPassword);
        System.out.println("加密后: " + encodedPassword);
        
        // 测试匹配
        boolean match1 = encoder.matches("test123456", encodedPassword);
        boolean match2 = encoder.matches("wrong_password", encodedPassword);
        
        System.out.println("验证 'test123456': " + (match1 ? "✅ 匹配" : "❌ 不匹配"));
        System.out.println("验证 'wrong_password': " + (match2 ? "✅ 匹配" : "❌ 不匹配"));
        
        // ==================== 场景3: 数据库密码验证 ====================
        System.out.println("\n【场景3】数据库密码验证");
        System.out.println("----------------------------------------");
        System.out.println("说明：将数据库中的密码复制到下方进行验证");
        System.out.println();
        
        // TODO: 从数据库复制完整的密码哈希值到这里
        String dbPasswordHash = "$2a$10$请从数据库复制完整密码";
        
        System.out.println("数据库密码: " + dbPasswordHash);
        System.out.println();
        
        // 尝试多个可能的密码
        String[] possiblePasswords = {
            "test123456",
            "password123",
            "Admin@123",
            "123456",
            "app_tester",
            "13900000001"
        };
        
        System.out.println("尝试匹配以下密码：");
        for (String testPwd : possiblePasswords) {
            try {
                boolean matches = encoder.matches(testPwd, dbPasswordHash);
                System.out.println("  " + testPwd + " -> " + (matches ? "✅ 匹配" : "❌ 不匹配"));
            } catch (Exception e) {
                System.out.println("  " + testPwd + " -> ⚠️ 验证出错（密码格式可能不正确）");
            }
        }
        
        // ==================== 场景4: 生成SQL更新语句 ====================
        System.out.println("\n【场景4】生成SQL更新语句");
        System.out.println("----------------------------------------");
        
        String newPassword = "test123456";
        String newPasswordEncoded = encoder.encode(newPassword);
        String mobile = "13900000001";
        
        System.out.println("为手机号 " + mobile + " 生成新密码：" + newPassword);
        System.out.println();
        System.out.println("执行以下SQL：");
        System.out.println("----------------------------------------");
        System.out.println("UPDATE user");
        System.out.println("SET password = '" + newPasswordEncoded + "',");
        System.out.println("    login_fail_count = 0,");
        System.out.println("    login_locked_until = NULL,");
        System.out.println("    updated_at = NOW()");
        System.out.println("WHERE mobile = '" + mobile + "' AND deleted = 0;");
        System.out.println("----------------------------------------");
        
        // ==================== 场景5: 批量生成测试账号密码 ====================
        System.out.println("\n【场景5】批量生成测试账号密码");
        System.out.println("----------------------------------------");
        
        String[][] testAccounts = {
            {"13900000001", "test123456"},
            {"13900000002", "test123456"},
            {"13900000003", "test123456"}
        };
        
        System.out.println("-- 批量更新测试账号密码为: test123456");
        for (String[] account : testAccounts) {
            String accountMobile = account[0];
            String accountPassword = account[1];
            String accountEncoded = encoder.encode(accountPassword);
            
            System.out.println("\n-- 账号: " + accountMobile);
            System.out.println("UPDATE user SET password = '" + accountEncoded + "', login_fail_count = 0 WHERE mobile = '" + accountMobile + "';");
        }
        
        // ==================== 使用说明 ====================
        System.out.println("\n\n========================================");
        System.out.println("           使用说明");
        System.out.println("========================================");
        System.out.println("1. 复制生成的加密密码到数据库");
        System.out.println("2. 使用原始密码登录测试");
        System.out.println("3. 如需验证数据库密码，将密码复制到【场景3】部分");
        System.out.println("4. 如登录失败次数过多，执行重置SQL：");
        System.out.println("   UPDATE user SET login_fail_count = 0, login_locked_until = NULL WHERE mobile = '手机号';");
        System.out.println("========================================\n");
    }
    
    /**
     * 自定义密码加密方法
     */
    public static String encryptPassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }
    
    /**
     * 自定义密码验证方法
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}

