package org.dromara.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dromara.common.core.domain.R;
import org.dromara.user.domain.entity.User;
import org.dromara.user.domain.dto.*;
import org.dromara.user.domain.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户服务接口
 * User Service Interface
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
public interface IUserService extends IService<User> {

    /**
     * 获取用户资料（个人主页）
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    R<UserProfileVo> getUserProfile(Long userId);

    /**
     * 获取他人主页
     *
     * @param userId       当前用户ID
     * @param targetUserId 目标用户ID
     * @return 用户资料
     */
    R<UserProfileVo> getOtherUserProfile(Long userId, Long targetUserId);

    /**
     * 更新昵称
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 结果
     */
    R<Void> updateNickname(Long userId, UpdateNicknameDto dto);

    /**
     * 更新性别
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 结果
     */
    R<Void> updateGender(Long userId, UpdateGenderDto dto);

    /**
     * 更新生日
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 结果
     */
    R<Void> updateBirthday(Long userId, UpdateBirthdayDto dto);

    /**
     * 更新用户资料
     *
     * @param userId 用户ID
     * @param dto    DTO
     * @return 结果
     */
    R<Void> updateUserProfile(Long userId, UserUpdateDto dto);

    /**
     * 上传头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    R<String> uploadAvatar(Long userId, MultipartFile file);

    /**
     * 批量获取用户简化信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    R<List<UserSimpleVo>> batchGetUserSimpleInfo(List<Long> userIds);

    /**
     * 通过手机号获取用户
     *
     * @param mobile      手机号
     * @param countryCode 国家区号
     * @return 用户信息
     */
    User getUserByMobile(String mobile, String countryCode);

    /**
     * 创建用户（RPC调用）
     *
     * @param userId      用户ID
     * @param mobile      手机号
     * @param countryCode 国家区号
     * @param nickname    昵称
     * @param avatar      头像
     * @return 用户ID
     */
    Long createUser(Long userId, String mobile, String countryCode, String nickname, String avatar);

    /**
     * 更新最后登录信息
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 结果
     */
    boolean updateLastLoginInfo(Long userId, String loginIp);

    /**
     * 获取用户动态列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 动态列表
     */
    R<PostListVo> getUserPosts(Long userId, Integer page, Integer pageSize);

    /**
     * 获取用户收藏列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    R<FavoriteListVo> getUserFavorites(Long userId, Integer page, Integer pageSize);

    /**
     * 获取用户点赞列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 点赞列表
     */
    R<LikeListVo> getUserLikes(Long userId, Integer page, Integer pageSize);

    /**
     * 获取用户详细资料（包含技能）
     *
     * @param userId 用户ID
     * @return 用户详细资料
     */
    R<ProfileInfoVo> getUserProfileInfo(Long userId);
}
