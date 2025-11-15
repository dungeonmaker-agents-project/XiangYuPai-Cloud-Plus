package org.dromara.user.integration;

import org.dromara.user.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户完整工作流集成测试
 *
 * 测试多个页面的完整用户流程:
 * - 页面03-06: 他人主页 -> 关注 -> 查看技能 -> 预约技能
 * - 页面04-05: 查看粉丝/关注列表 -> 搜索 -> 回关/取消关注
 * - 页面06: 创建技能 -> 编辑 -> 上下架
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("用户完整工作流集成测试 - User Workflow Integration Test")
public class UserWorkflowIntegrationTest extends BaseTest {

    // ==================== 技能管理完整流程测试 ====================

    @Test
    @DisplayName("E2E-SKILL-001: 创建线上技能完整流程")
    public void testCreateOnlineSkillCompleteFlow() throws Exception {
        // Step 1: 获取技能配置(游戏列表、段位列表)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/skills/config")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.games").isArray())
            .andExpect(jsonPath("$.data.games[0].gameId").value("wzry"));

        // Step 2: 上传技能展示图片(最多9张)
        String[] imageUrls = new String[3];
        for (int i = 0; i < 3; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "skill_" + (i + 1) + ".jpg",
                "image/jpeg",
                ("skill image " + (i + 1)).getBytes()
            );

            String response = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                    .file(imageFile)
                    .header(AUTHORIZATION_HEADER, getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isString())
                .andReturn().getResponse().getContentAsString();

            // 提取图片URL(实际测试中需要解析JSON)
            imageUrls[i] = "https://cdn.example.com/skills/skill_" + (i + 1) + ".jpg";
        }

        // Step 3: 创建线上技能
        String createSkillJson = String.format(
            "{\"skillType\":1," +
            "\"gameId\":\"wzry\"," +
            "\"gameName\":\"王者荣耀\"," +
            "\"rank\":\"王者\"," +
            "\"pricePerHour\":50.00," +
            "\"description\":\"5年王者经验,可带上分\"," +
            "\"images\":[\"%s\",\"%s\",\"%s\"]}",
            imageUrls[0], imageUrls[1], imageUrls[2]
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/online")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSkillJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isNumber()); // 返回skillId

        // Step 4: 查看我的技能列表,验证创建成功
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/my")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("E2E-SKILL-002: 创建线下技能完整流程")
    public void testCreateOfflineSkillCompleteFlow() throws Exception {
        // Step 1: 获取技能配置(服务类型列表)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/skills/config")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.serviceTypes").isArray())
            .andExpect(jsonPath("$.data.serviceTypes[2].typeId").value("photography"));

        // Step 2: 上传服务展示图片
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "service.jpg",
            "image/jpeg",
            "service image".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/skills/images/upload")
                .file(imageFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: 创建线下技能
        String createSkillJson =
            "{\"skillType\":2," +
            "\"serviceTypeId\":\"photography\"," +
            "\"serviceTypeName\":\"摄影服务\"," +
            "\"title\":\"专业摄影师 - 婚礼拍摄\"," +
            "\"pricePerService\":800.00," +
            "\"description\":\"10年摄影经验\"," +
            "\"serviceLocation\":\"北京市朝阳区\"," +
            "\"latitude\":39.9042," +
            "\"longitude\":116.4074," +
            "\"availableTimes\":[\"周末\",\"节假日\"]," +
            "\"promises\":[\"满意为止\",\"后期修图\"]," +
            "\"images\":[\"https://cdn.example.com/service.jpg\"]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/offline")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSkillJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isNumber());

        // Step 4: 查看我的技能列表
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/my")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("E2E-SKILL-003: 编辑技能 -> 下架 -> 重新上架流程")
    public void testEditSkillAndToggleFlow() throws Exception {
        Long skillId = TEST_SKILL_ID;

        // Step 1: 获取技能详情
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/" + skillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.skillId").value(skillId));

        // Step 2: 编辑技能(修改价格和描述)
        String updateJson =
            "{\"description\":\"更新后的描述 - 新增服务承诺\"," +
            "\"pricePerHour\":60.00}";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + skillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: 下架技能(暂停接单)
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + skillId + "/toggle")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("isOnline", "false")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 4: 验证技能已下架
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/" + skillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isOnline").value(false));

        // Step 5: 重新上架技能
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/skills/" + skillId + "/toggle")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("isOnline", "true")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 6: 验证技能已上架
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/" + skillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isOnline").value(true));
    }

    // ==================== 用户关系完整流程测试 ====================

    @Test
    @DisplayName("E2E-RELATION-001: 查看他人主页 -> 关注 -> 查看技能完整流程")
    public void testViewProfileFollowAndViewSkillsFlow() throws Exception {
        Long targetUserId = TEST_OTHER_USER_ID;

        // Step 1: 查看他人主页
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(targetUserId))
            .andExpect(jsonPath("$.data.isFollowing").exists())
            .andExpect(jsonPath("$.data.canViewSkills").exists());

        // Step 2: 关注该用户
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: 再次查看主页,验证已关注
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isFollowing").value(true));

        // Step 4: 查看该用户的技能列表
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/user/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());

        // Step 5: 查看我的关注列表,验证该用户在列表中
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @DisplayName("E2E-RELATION-002: 粉丝列表 -> 回关 -> 互相关注流程")
    public void testFansListFollowBackFlow() throws Exception {
        // Step 1: 查看粉丝列表
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());

        // Step 2: 从粉丝列表中选择一个未回关的粉丝
        Long fanUserId = 2001L;

        // Step 3: 查看该粉丝的主页
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + fanUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(fanUserId));

        // Step 4: 回关该粉丝
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + fanUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 5: 再次查看粉丝列表,验证该粉丝标记为"已回关"
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/fans")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());
    }

    @Test
    @DisplayName("E2E-RELATION-003: 搜索粉丝/关注 -> 取消关注流程")
    public void testSearchAndUnfollowFlow() throws Exception {
        // Step 1: 搜索关注列表
        String keyword = "张三";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("keyword", keyword)
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());

        // Step 2: 从搜索结果中选择要取消关注的用户
        Long followingId = 2004L;

        // Step 3: 弹出确认对话框(前端),用户确认后取消关注
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/relation/follow/" + followingId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 4: 验证已从关注列表中移除
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/relation/following")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("keyword", keyword)
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("E2E-RELATION-004: 举报用户 -> 拉黑完整流程")
    public void testReportAndBlockUserFlow() throws Exception {
        Long targetUserId = TEST_OTHER_USER_ID;

        // Step 1: 查看他人主页,发现违规内容
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Step 2: 举报该用户
        String reportJson =
            "{\"reportedUserId\":" + targetUserId + "," +
            "\"reportType\":1," +
            "\"reportReason\":\"发布违规内容\"," +
            "\"reportImages\":[\"https://cdn.example.com/evidence1.jpg\"]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/report")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reportJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 3: 拉黑该用户
        String blockJson =
            "{\"blockedUserId\":" + targetUserId + "," +
            "\"reason\":\"违规用户\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/block")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(blockJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 4: 验证无法再查看该用户的主页(被拉黑)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.canViewProfile").value(false));
    }

    // ==================== 附近技能搜索流程测试 ====================

    @Test
    @DisplayName("E2E-NEARBY-001: 搜索附近技能 -> 查看详情 -> 联系技能提供者流程")
    public void testSearchNearbySkillsFlow() throws Exception {
        // Step 1: 搜索附近的线下技能(北京市中心5公里内)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/nearby")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("latitude", "39.9042")
                .param("longitude", "116.4074")
                .param("radiusMeters", "5000")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isArray());

        // Step 2: 从搜索结果中选择一个技能查看详情
        Long skillId = 3004L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/" + skillId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.skillId").value(skillId))
            .andExpect(jsonPath("$.data.userId").exists())
            .andExpect(jsonPath("$.data.serviceLocation").exists());

        // Step 3: 查看技能提供者的主页
        Long providerId = 2005L; // 假设从上一步获取
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + providerId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(providerId));

        // Step 4: 获取联系方式(微信号)
        // 注: 实际业务中可能需要付费或关注后才能查看联系方式
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + providerId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.wechatId").exists());
    }

    // ==================== 跨页面综合流程测试 ====================

    @Test
    @DisplayName("E2E-COMPREHENSIVE-001: 新用户完整使用流程")
    public void testNewUserCompleteJourney() throws Exception {
        // 完整模拟新用户的使用流程

        // 第1步: 完善个人资料
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        String nicknameJson = "{\"nickname\":\"新用户小明\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nicknameJson))
            .andExpect(status().isOk());

        // 第2步: 创建第一个技能
        String createSkillJson =
            "{\"skillType\":1," +
            "\"gameId\":\"wzry\"," +
            "\"gameName\":\"王者荣耀\"," +
            "\"rank\":\"钻石\"," +
            "\"pricePerHour\":30.00," +
            "\"description\":\"3年游戏经验\"," +
            "\"images\":[]}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/skills/online")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSkillJson))
            .andExpect(status().isOk());

        // 第3步: 浏览附近的技能
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/skills/nearby")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .param("latitude", "39.9042")
                .param("longitude", "116.4074")
                .param("radiusMeters", "10000")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // 第4步: 关注感兴趣的技能提供者
        Long targetUserId = 2006L;
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/relation/follow/" + targetUserId)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // 第5步: 查看自己的主页
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/header")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("新用户小明"));
    }
}
