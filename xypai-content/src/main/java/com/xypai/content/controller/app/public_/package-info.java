/**
 * 公开接口包（无需登录）
 * 
 * <p>此包下的所有控制器接口都是公开的，允许匿名访问。</p>
 * 
 * <p>使用场景：
 * <ul>
 *   <li>发现页面内容展示（热门、推荐、同城）</li>
 *   <li>内容搜索功能</li>
 *   <li>其他需要匿名访问的内容接口</li>
 * </ul>
 * </p>
 * 
 * <p>安全配置：
 * 已在 SaTokenConfig 中配置白名单路由：/api/v1/discovery/**
 * </p>
 * 
 * @author xypai
 * @date 2025-10-25
 */
package com.xypai.content.controller.app.public_;

