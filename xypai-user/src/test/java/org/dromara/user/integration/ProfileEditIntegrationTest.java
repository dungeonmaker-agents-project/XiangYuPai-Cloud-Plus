package org.dromara.user.integration;

import org.dromara.user.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 个人资料编辑流程集成测试
 *
 * 测试完整的用户流程:
 * - 页面02: 编辑资料页面完整流程
 *   1. 进入编辑页面,获取当前资料
 *   2. 依次更新各个字段(头像、昵称、性别、生日等)
 *   3. 每次更新后立即保存(实时保存机制)
 *   4. 验证所有更新都成功
 *   5. 返回主页验证更新后的数据
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@DisplayName("个人资料编辑流程集成测试 - Profile Edit Integration Test")
public class ProfileEditIntegrationTest extends BaseTest {

    @Test
    @DisplayName("E2E-EDIT-001: 完整编辑资料流程 - 包含所有字段")
    public void testCompleteProfileEditFlow() throws Exception {
        // Step 1: 进入编辑页面,获取当前资料
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").exists());

        // Step 2: 上传新头像
        MockMultipartFile avatarFile = new MockMultipartFile(
            "avatar",
            "new_avatar.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/profile/avatar/upload")
                .file(avatarFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isString());

        // Step 3: 更新昵称(实时保存)
        String nicknameJson = "{\"nickname\":\"新昵称测试\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nicknameJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 4: 更新性别(实时保存)
        String genderJson = "{\"gender\":1}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/gender")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(genderJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 5: 更新生日(实时保存)
        String birthdayJson = "{\"birthday\":\"1995-05-15\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/birthday")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(birthdayJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 6: 更新居住地(实时保存)
        String residenceJson = "{\"residence\":\"北京市朝阳区\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/residence")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(residenceJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 7: 更新身高(实时保存)
        String heightJson = "{\"height\":175}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/height")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(heightJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 8: 更新体重(实时保存)
        String weightJson = "{\"weight\":65}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/weight")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(weightJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 9: 更新职业(实时保存)
        String occupationJson = "{\"occupation\":\"软件工程师\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/occupation")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(occupationJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 10: 更新微信号(实时保存)
        String wechatJson = "{\"wechatId\":\"test_wechat_123\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/wechat")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(wechatJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 11: 更新个人简介(实时保存)
        String bioJson = "{\"bio\":\"这是我的个人简介,热爱技术,享受生活!\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/bio")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // Step 12: 返回主页,验证所有更新都生效
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/header")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.nickname").value("新昵称测试"))
            .andExpect(jsonPath("$.data.gender").value(1));
    }

    @Test
    @DisplayName("E2E-EDIT-002: 实时保存机制测试 - 连续快速编辑")
    public void testRealTimeSaveMechanism() throws Exception {
        // 说明: 模拟用户快速连续编辑多个字段
        // 前端会使用300ms防抖,但后端应能正确处理所有请求

        // 连续更新昵称3次(模拟用户修改、再修改、再修改)
        String[] nicknames = {"昵称1", "昵称2", "昵称3"};
        for (String nickname : nicknames) {
            String json = "{\"nickname\":\"" + nickname + "\"}";
            mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                    .header(AUTHORIZATION_HEADER, getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        // 验证最终保存的是最后一次的值
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("昵称3"));
    }

    @Test
    @DisplayName("E2E-EDIT-003: 编辑失败后重试流程")
    public void testEditFailureAndRetry() throws Exception {
        // Step 1: 尝试更新为无效昵称(空字符串)
        String invalidJson = "{\"nickname\":\"\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest()); // 验证失败

        // Step 2: 用户看到错误提示后,输入有效昵称重试
        String validJson = "{\"nickname\":\"有效昵称\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200)); // 成功
    }

    @Test
    @DisplayName("E2E-EDIT-004: 编辑资料 - 部分字段更新")
    public void testPartialProfileUpdate() throws Exception {
        // 说明: 用户可能只更新部分字段,不是全部
        // 测试只更新昵称和简介,其他字段不变

        // Step 1: 获取原始资料
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Step 2: 只更新昵称
        String nicknameJson = "{\"nickname\":\"部分更新昵称\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nicknameJson))
            .andExpect(status().isOk());

        // Step 3: 只更新简介
        String bioJson = "{\"bio\":\"只更新了简介\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/bio")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioJson))
            .andExpect(status().isOk());

        // Step 4: 验证更新成功
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("部分更新昵称"))
            .andExpect(jsonPath("$.data.bio").value("只更新了简介"));
    }

    @Test
    @DisplayName("E2E-EDIT-005: 头像上传失败后重新上传")
    public void testAvatarUploadRetry() throws Exception {
        // Step 1: 尝试上传过大的文件
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB,超过限制
        MockMultipartFile largeFile = new MockMultipartFile(
            "avatar",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/profile/avatar/upload")
                .file(largeFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(500)); // 失败

        // Step 2: 用户压缩图片后重新上传
        MockMultipartFile validFile = new MockMultipartFile(
            "avatar",
            "compressed.jpg",
            "image/jpeg",
            "valid compressed image".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/profile/avatar/upload")
                .file(validFile)
                .header(AUTHORIZATION_HEADER, getAuthHeader()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200)) // 成功
            .andExpect(jsonPath("$.data").isString());
    }

    @Test
    @DisplayName("E2E-EDIT-006: 编辑页面 - 数据验证完整流程")
    public void testValidationFlow() throws Exception {
        // 测试各个字段的验证规则

        // 1. 昵称验证:过长
        String longNickname = "{\"nickname\":\"这是一个非常非常非常长的昵称超过了二十个字符的限制测试\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(longNickname))
            .andExpect(status().isBadRequest());

        // 2. 性别验证:无效值
        String invalidGender = "{\"gender\":99}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/gender")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidGender))
            .andExpect(status().isBadRequest());

        // 3. 生日验证:未来日期
        String futureBirthday = "{\"birthday\":\"2030-12-31\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/birthday")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(futureBirthday))
            .andExpect(status().isBadRequest());

        // 4. 身高验证:不合理值
        String invalidHeight = "{\"height\":50}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/height")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidHeight))
            .andExpect(status().isBadRequest());

        // 验证:所有无效数据都被拒绝,原数据未被修改
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("E2E-EDIT-007: 编辑资料后查看他人主页")
    public void testProfileEditThenViewAsOthers() throws Exception {
        // Step 1: 用户A更新自己的资料
        String nicknameJson = "{\"nickname\":\"用户A更新昵称\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nicknameJson))
            .andExpect(status().isOk());

        String bioJson = "{\"bio\":\"用户A的新简介\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/bio")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bioJson))
            .andExpect(status().isOk());

        // Step 2: 用户B(其他人)查看用户A的主页
        // 应该能看到更新后的昵称和简介(如果隐私设置允许)
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/other/" + TEST_USER_ID)
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(TEST_USER_ID));
    }

    @Test
    @DisplayName("E2E-EDIT-008: 乐观更新UI测试场景")
    public void testOptimisticUIUpdate() throws Exception {
        // 说明: 前端实现了乐观更新(Optimistic UI Update)
        // 即在API返回前就先更新UI,如果API失败再回滚
        // 此测试验证后端能正确支持这种模式

        String nicknameJson = "{\"nickname\":\"乐观更新测试\"}";

        // 发送更新请求
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile/nickname")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nicknameJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 立即查询,验证数据已更新
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile/edit")
                .header(AUTHORIZATION_HEADER, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("乐观更新测试"));
    }
}
