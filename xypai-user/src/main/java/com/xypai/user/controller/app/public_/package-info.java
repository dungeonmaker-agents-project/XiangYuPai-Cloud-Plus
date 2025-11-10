/**
 * 公开接口包
 * 
 * 此包中的所有Controller都是公开的，允许匿名访问（无需登录token）
 * 
 * 权限配置：
 * - 已在 SaTokenConfig 中配置白名单
 * - 已在 Gateway 中配置白名单
 * 
 * 安全要求：
 * - 所有接口只返回公开数据
 * - 不包含敏感信息（如手机号已脱敏）
 * - 无权限验证注解（@SaCheckPermission）
 * 
 * 用途：
 * - App首页展示
 * - 访客浏览
 * - 无需登录的功能
 * 
 * @author xypai
 * @since 2025-10-24
 */
package com.xypai.user.controller.app.public_;

