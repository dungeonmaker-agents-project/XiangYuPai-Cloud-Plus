package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.UnlockWechatDTO;
import org.dromara.appbff.domain.vo.MomentsListVO;
import org.dromara.appbff.domain.vo.OtherUserProfileVO;
import org.dromara.appbff.domain.vo.ProfileInfoVO;
import org.dromara.appbff.domain.vo.UnlockWechatResultVO;
import org.dromara.appbff.domain.vo.UserSkillsListVO;
import org.dromara.appbff.service.OtherUserProfileService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.dto.WechatUnlockDto;
import org.dromara.appuser.api.domain.vo.OtherUserProfileVo;
import org.dromara.appuser.api.domain.vo.RemoteAppUserVo;
import org.dromara.appuser.api.domain.vo.UserDetailInfoVo;
import org.dromara.appuser.api.domain.vo.UserSkillVo;
import org.dromara.appuser.api.domain.vo.UserSkillsPageResult;
import org.dromara.appuser.api.domain.vo.WechatUnlockResultVo;
import org.dromara.content.api.RemoteContentService;
import org.dromara.content.api.domain.vo.RemoteMomentPageResult;
import org.dromara.content.api.domain.vo.RemoteMomentVo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对方主页服务实现类
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtherUserProfileServiceImpl implements OtherUserProfileService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @DubboReference
    private RemoteContentService remoteContentService;

    @Override
    public OtherUserProfileVO getOtherUserProfile(Long targetUserId, Long currentUserId, Double latitude, Double longitude) {
        log.info("获取对方主页数据: targetUserId={}, currentUserId={}", targetUserId, currentUserId);

        try {
            // 调用RPC获取数据
            OtherUserProfileVo rpcResult = remoteAppUserService.getOtherUserProfileData(
                targetUserId, currentUserId, latitude, longitude
            );

            if (rpcResult == null) {
                log.warn("用户不存在: targetUserId={}", targetUserId);
                return null;
            }

            // 转换为BFF层VO
            return convertToOtherUserProfileVO(rpcResult);
        } catch (Exception e) {
            log.error("获取对方主页数据失败: targetUserId={}, error={}", targetUserId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ProfileInfoVO getProfileInfo(Long targetUserId, Long currentUserId) {
        log.info("获取用户资料详情: targetUserId={}, currentUserId={}", targetUserId, currentUserId);

        try {
            // 调用RPC获取数据
            UserDetailInfoVo rpcResult = remoteAppUserService.getUserDetailInfo(targetUserId, currentUserId);

            if (rpcResult == null) {
                log.warn("用户资料不存在: targetUserId={}", targetUserId);
                return null;
            }

            // 转换为BFF层VO
            return convertToProfileInfoVO(rpcResult);
        } catch (Exception e) {
            log.error("获取用户资料详情失败: targetUserId={}, error={}", targetUserId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public UserSkillsListVO getUserSkills(Long targetUserId, Long currentUserId, Integer pageNum, Integer pageSize) {
        log.info("获取用户技能列表: targetUserId={}, pageNum={}, pageSize={}", targetUserId, pageNum, pageSize);

        try {
            // 调用RPC获取数据
            UserSkillsPageResult rpcResult = remoteAppUserService.getUserSkillsList(
                targetUserId, currentUserId, pageNum, pageSize
            );

            if (rpcResult == null) {
                return createEmptySkillsList();
            }

            // 转换为BFF层VO
            return convertToUserSkillsListVO(rpcResult);
        } catch (Exception e) {
            log.error("获取用户技能列表失败: targetUserId={}, error={}", targetUserId, e.getMessage(), e);
            return createEmptySkillsList();
        }
    }

    @Override
    public UnlockWechatResultVO unlockWechat(Long currentUserId, UnlockWechatDTO dto) {
        log.info("解锁微信: currentUserId={}, targetUserId={}", currentUserId, dto.getTargetUserId());

        try {
            // 构建RPC请求
            WechatUnlockDto rpcDto = WechatUnlockDto.builder()
                .userId(currentUserId)
                .targetUserId(dto.getTargetUserId())
                .unlockType(dto.getUnlockType())
                .paymentPassword(dto.getPaymentPassword())
                .build();

            // 调用RPC
            WechatUnlockResultVo rpcResult = remoteAppUserService.unlockWechat(rpcDto);

            // 转换为BFF层VO
            return UnlockWechatResultVO.builder()
                .success(rpcResult.getSuccess())
                .wechat(rpcResult.getWechat())
                .cost(rpcResult.getCost())
                .remainingCoins(rpcResult.getRemainingCoins())
                .failReason(rpcResult.getFailReason())
                .build();
        } catch (Exception e) {
            log.error("解锁微信失败: currentUserId={}, targetUserId={}, error={}",
                currentUserId, dto.getTargetUserId(), e.getMessage(), e);
            return UnlockWechatResultVO.builder()
                .success(false)
                .failReason("系统错误，请稍后重试")
                .build();
        }
    }

    @Override
    public boolean followUser(Long currentUserId, Long targetUserId) {
        log.info("关注用户: currentUserId={}, targetUserId={}", currentUserId, targetUserId);

        try {
            return remoteAppUserService.followUser(currentUserId, targetUserId);
        } catch (Exception e) {
            log.error("关注用户失败: currentUserId={}, targetUserId={}, error={}",
                currentUserId, targetUserId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unfollowUser(Long currentUserId, Long targetUserId) {
        log.info("取消关注: currentUserId={}, targetUserId={}", currentUserId, targetUserId);

        try {
            return remoteAppUserService.unfollowUser(currentUserId, targetUserId);
        } catch (Exception e) {
            log.error("取消关注失败: currentUserId={}, targetUserId={}, error={}",
                currentUserId, targetUserId, e.getMessage(), e);
            return false;
        }
    }

    // ==================== 转换方法 ====================

    private OtherUserProfileVO convertToOtherUserProfileVO(OtherUserProfileVo rpc) {
        OtherUserProfileVO.LevelVO levelVO = null;
        if (rpc.getLevel() != null) {
            levelVO = OtherUserProfileVO.LevelVO.builder()
                .value(rpc.getLevel().getValue())
                .name(rpc.getLevel().getName())
                .icon(rpc.getLevel().getIcon())
                .build();
        }

        return OtherUserProfileVO.builder()
            .userId(rpc.getUserId())
            .avatar(rpc.getAvatar())
            .coverUrl(rpc.getCoverUrl())
            .nickname(rpc.getNickname())
            .gender(rpc.getGender())
            .age(rpc.getAge())
            .bio(rpc.getBio())
            .level(levelVO)
            .isVerified(rpc.getIsRealVerified())
            .isExpert(rpc.getIsGodVerified())
            .isVip(rpc.getIsVip())
            .isOnline(rpc.getIsOnline())
            .isAvailable(rpc.getIsAvailable())
            .distance(rpc.getDistanceDisplay())
            .followerCount(rpc.getStats() != null ? rpc.getStats().getFansCount() : 0)
            .followingCount(rpc.getStats() != null ? rpc.getStats().getFollowingCount() : 0)
            .likesCount(rpc.getStats() != null ? rpc.getStats().getLikesCount() : 0)
            .isFollowed("following".equals(rpc.getFollowStatus()) || "mutual".equals(rpc.getFollowStatus()))
            .followStatus(rpc.getFollowStatus())
            .canMessage(rpc.getCanMessage())
            .canUnlockWechat(rpc.getCanUnlockWechat())
            .wechatUnlocked(rpc.getWechatUnlocked())
            .unlockPrice(rpc.getUnlockPrice())
            .build();
    }

    private ProfileInfoVO convertToProfileInfoVO(UserDetailInfoVo rpc) {
        return ProfileInfoVO.builder()
            .userId(rpc.getUserId())
            .residence(rpc.getResidence())
            .ipLocation(rpc.getIpLocation())
            .height(rpc.getHeight())
            .weight(rpc.getWeight())
            .occupation(rpc.getOccupation())
            .wechat(rpc.getWechat())
            .wechatUnlocked(rpc.getWechatUnlocked())
            .birthday(rpc.getBirthday())
            .zodiac(rpc.getZodiac())
            .age(rpc.getAge())
            .bio(rpc.getBio())
            .build();
    }

    private UserSkillsListVO convertToUserSkillsListVO(UserSkillsPageResult rpc) {
        List<UserSkillsListVO.SkillItemVO> items = Collections.emptyList();

        if (rpc.getList() != null) {
            items = rpc.getList().stream()
                .map(this::convertToSkillItemVO)
                .collect(Collectors.toList());
        }

        return UserSkillsListVO.builder()
            .list(items)
            .total(rpc.getTotal())
            .hasMore(rpc.getHasMore())
            .build();
    }

    private UserSkillsListVO.SkillItemVO convertToSkillItemVO(UserSkillVo rpc) {
        UserSkillsListVO.MediaDataVO mediaData = null;
        if (rpc.getMediaData() != null) {
            mediaData = UserSkillsListVO.MediaDataVO.builder()
                .coverUrl(rpc.getMediaData().getCoverUrl())
                .images(rpc.getMediaData().getImages())
                .build();
        }

        UserSkillsListVO.SkillInfoVO skillInfo = null;
        if (rpc.getSkillInfo() != null) {
            skillInfo = UserSkillsListVO.SkillInfoVO.builder()
                .name(rpc.getSkillName())
                .rank(rpc.getSkillInfo().getGameRank())
                .tags(rpc.getSkillInfo().getTags())
                .build();
        }

        UserSkillsListVO.PriceVO priceData = null;
        if (rpc.getPriceData() != null) {
            priceData = UserSkillsListVO.PriceVO.builder()
                .amount(rpc.getPriceData().getAmount())
                .unit(rpc.getPriceData().getUnit())
                .displayText(rpc.getPriceData().getDisplayText())
                .build();
        }

        return UserSkillsListVO.SkillItemVO.builder()
            .id(rpc.getSkillId())
            .mediaData(mediaData)
            .skillInfo(skillInfo)
            .priceData(priceData)
            .build();
    }

    private UserSkillsListVO createEmptySkillsList() {
        return UserSkillsListVO.builder()
            .list(Collections.emptyList())
            .total(0L)
            .hasMore(false)
            .build();
    }

    // ==================== 动态列表相关方法实现 ====================

    @Override
    public MomentsListVO getUserMoments(Long targetUserId, Long currentUserId, Integer pageNum, Integer pageSize) {
        log.info("获取用户动态列表: targetUserId={}, currentUserId={}, pageNum={}, pageSize={}",
            targetUserId, currentUserId, pageNum, pageSize);

        try {
            // 调用RPC获取动态列表
            RemoteMomentPageResult rpcResult = remoteContentService.getUserMomentList(
                targetUserId, currentUserId, pageNum, pageSize
            );

            if (rpcResult == null || rpcResult.getList() == null || rpcResult.getList().isEmpty()) {
                return createEmptyMomentsList();
            }

            // 获取作者信息（因为RPC返回的动态数据不包含作者详细信息）
            // 这里所有动态都是同一个用户的，所以只需要查一次用户信息
            RemoteAppUserVo authorInfo = null;
            try {
                authorInfo = remoteAppUserService.getUserBasicInfo(targetUserId, currentUserId);
            } catch (Exception e) {
                log.warn("获取作者信息失败: targetUserId={}, error={}", targetUserId, e.getMessage());
            }

            // 转换为BFF层VO
            RemoteAppUserVo finalAuthorInfo = authorInfo;
            List<MomentsListVO.MomentItemVO> items = rpcResult.getList().stream()
                .map(rpc -> convertToMomentItemVO(rpc, finalAuthorInfo))
                .collect(Collectors.toList());

            return MomentsListVO.builder()
                .list(items)
                .hasMore(rpcResult.getHasMore())
                .total(rpcResult.getTotal())
                .build();

        } catch (Exception e) {
            log.error("获取用户动态列表失败: targetUserId={}, error={}", targetUserId, e.getMessage(), e);
            return createEmptyMomentsList();
        }
    }

    @Override
    public boolean likeMoment(Long currentUserId, Long momentId) {
        log.info("点赞动态: currentUserId={}, momentId={}", currentUserId, momentId);

        try {
            return remoteContentService.likeMoment(currentUserId, momentId);
        } catch (Exception e) {
            log.error("点赞动态失败: currentUserId={}, momentId={}, error={}",
                currentUserId, momentId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unlikeMoment(Long currentUserId, Long momentId) {
        log.info("取消点赞动态: currentUserId={}, momentId={}", currentUserId, momentId);

        try {
            return remoteContentService.unlikeMoment(currentUserId, momentId);
        } catch (Exception e) {
            log.error("取消点赞动态失败: currentUserId={}, momentId={}, error={}",
                currentUserId, momentId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 转换为动态项VO
     */
    private MomentsListVO.MomentItemVO convertToMomentItemVO(RemoteMomentVo rpc, RemoteAppUserVo authorInfo) {
        // 媒体数据
        MomentsListVO.MediaDataVO mediaData = MomentsListVO.MediaDataVO.builder()
            .coverUrl(rpc.getCoverUrl())
            .aspectRatio(rpc.getAspectRatio())
            .duration(rpc.getDuration())
            .build();

        // 文本数据
        MomentsListVO.TextDataVO textData = MomentsListVO.TextDataVO.builder()
            .title(rpc.getTitle())
            .build();

        // 作者数据（优先使用查询到的作者信息，否则使用RPC返回的基本信息）
        MomentsListVO.AuthorDataVO authorData;
        if (authorInfo != null) {
            authorData = MomentsListVO.AuthorDataVO.builder()
                .userId(String.valueOf(authorInfo.getUserId()))
                .avatar(authorInfo.getAvatar())
                .nickname(authorInfo.getNickname())
                .build();
        } else {
            authorData = MomentsListVO.AuthorDataVO.builder()
                .userId(String.valueOf(rpc.getAuthorId()))
                .avatar(rpc.getAuthorAvatar())
                .nickname(rpc.getAuthorNickname())
                .build();
        }

        // 统计数据
        MomentsListVO.StatsDataVO statsData = MomentsListVO.StatsDataVO.builder()
            .likeCount(rpc.getLikeCount())
            .isLiked(rpc.getIsLiked())
            .build();

        return MomentsListVO.MomentItemVO.builder()
            .id(String.valueOf(rpc.getId()))
            .type(rpc.getType())
            .mediaData(mediaData)
            .textData(textData)
            .authorData(authorData)
            .statsData(statsData)
            .build();
    }

    /**
     * 创建空的动态列表
     */
    private MomentsListVO createEmptyMomentsList() {
        return MomentsListVO.builder()
            .list(Collections.emptyList())
            .hasMore(false)
            .total(0L)
            .build();
    }
}
