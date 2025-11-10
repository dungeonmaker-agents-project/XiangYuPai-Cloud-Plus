package com.xypai.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.content.domain.dto.ContentActionDTO;
import com.xypai.content.domain.dto.ContentActionQueryDTO;
import com.xypai.content.domain.entity.Content;
import com.xypai.content.domain.entity.ContentAction;
import com.xypai.content.domain.vo.ContentActionVO;
import com.xypai.content.domain.vo.ContentListVO;
import com.xypai.content.mapper.ContentActionMapper;
import com.xypai.content.mapper.ContentMapper;
import com.xypai.content.service.IContentActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容行为服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentActionServiceImpl implements IContentActionService {

    private final ContentActionMapper contentActionMapper;
    private final ContentMapper contentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeAction(ContentActionDTO actionDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 验证内容是否存在
        Content content = contentMapper.selectById(actionDTO.getContentId());
        if (content == null) {
            throw new ServiceException("内容不存在");
        }

        // 检查是否已经执行过该行为
        boolean alreadyExecuted = checkActionExists(actionDTO.getContentId(), currentUserId, actionDTO.getAction());
        if (alreadyExecuted && !isRepeatableAction(actionDTO.getAction())) {
            throw new ServiceException("您已经执行过该操作");
        }

        // 构建行为数据
        Map<String, Object> data = buildActionData(actionDTO);

        ContentAction action = ContentAction.builder()
                .contentId(actionDTO.getContentId())
                .userId(currentUserId)
                .action(actionDTO.getAction())
                .data(data)
                .createdAt(LocalDateTime.now())
                .build();

        int result = contentActionMapper.insert(action);
        if (result <= 0) {
            throw new ServiceException("执行操作失败");
        }

        log.info("执行内容行为成功，内容ID：{}，用户ID：{}，行为：{}", 
                actionDTO.getContentId(), currentUserId, actionDTO.getAction());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAction(Long contentId, Integer action) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        int result = contentActionMapper.deleteUserAction(contentId, currentUserId, action);
        log.info("取消内容行为，内容ID：{}，用户ID：{}，行为：{}", contentId, currentUserId, action);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeContent(Long contentId) {
        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.LIKE.getCode())
                .build();
        return executeAction(actionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeContent(Long contentId) {
        return cancelAction(contentId, ContentAction.Action.LIKE.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectContent(Long contentId) {
        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.COLLECT.getCode())
                .build();
        return executeAction(actionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectContent(Long contentId) {
        return cancelAction(contentId, ContentAction.Action.COLLECT.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commentContent(Long contentId, String commentContent, Long replyToId) {
        if (StringUtils.isBlank(commentContent)) {
            throw new ServiceException("评论内容不能为空");
        }

        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.COMMENT.getCode())
                .commentContent(commentContent)
                .replyToId(replyToId)
                .build();
        return executeAction(actionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId) {
        if (commentId == null) {
            throw new ServiceException("评论ID不能为空");
        }

        ContentAction comment = contentActionMapper.selectById(commentId);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }

        // 检查权限
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(comment.getUserId())) {
            throw new ServiceException("无权限删除该评论");
        }

        int result = contentActionMapper.deleteById(commentId);
        log.info("删除评论成功，评论ID：{}", commentId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shareContent(Long contentId) {
        return shareContent(contentId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shareContent(Long contentId, String shareTarget) {
        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.SHARE.getCode())
                .build();

        if (StringUtils.isNotBlank(shareTarget)) {
            Map<String, Object> extraData = new HashMap<>();
            extraData.put("share_target", shareTarget);
            actionDTO.setExtraData(extraData);
        }

        return executeAction(actionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean signupActivity(Long contentId, Map<String, Object> signupInfo) {
        // 验证是否为活动内容
        Content content = contentMapper.selectById(contentId);
        if (content == null || !content.isActivity()) {
            throw new ServiceException("该内容不是活动类型");
        }

        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.SIGNUP.getCode())
                .signupInfo(signupInfo)
                .build();
        return executeAction(actionDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSignup(Long contentId) {
        return cancelAction(contentId, ContentAction.Action.SIGNUP.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportContent(Long contentId, String reason) {
        if (StringUtils.isBlank(reason)) {
            throw new ServiceException("举报原因不能为空");
        }

        ContentActionDTO actionDTO = ContentActionDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.REPORT.getCode())
                .reportReason(reason)
                .build();
        return executeAction(actionDTO);
    }

    @Override
    public List<ContentActionVO> selectActionList(ContentActionQueryDTO query) {
        LambdaQueryWrapper<ContentAction> queryWrapper = Wrappers.lambdaQuery(ContentAction.class)
                .eq(query.getContentId() != null, ContentAction::getContentId, query.getContentId())
                .eq(query.getUserId() != null, ContentAction::getUserId, query.getUserId())
                .eq(query.getAction() != null, ContentAction::getAction, query.getAction())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        ContentAction::getCreatedAt, query.getBeginTime(), query.getEndTime())
                .orderByDesc(ContentAction::getCreatedAt);

        List<ContentAction> actions = contentActionMapper.selectList(queryWrapper);
        return convertToActionVOs(actions);
    }

    @Override
    public List<ContentActionVO> selectContentComments(Long contentId, Integer limit) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        List<ContentAction> comments = contentActionMapper.selectContentComments(
                contentId, limit != null ? limit : 20);
        return convertToActionVOs(comments);
    }

    @Override
    public List<ContentActionVO> selectCommentReplies(Long commentId) {
        if (commentId == null) {
            throw new ServiceException("评论ID不能为空");
        }

        List<ContentAction> replies = contentActionMapper.selectCommentReplies(commentId);
        return convertToActionVOs(replies);
    }

    @Override
    public List<ContentActionVO> selectUserCollections(Long userId, Integer limit) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        List<ContentAction> collections = contentActionMapper.selectUserCollections(
                targetUserId, limit != null ? limit : 20);
        return convertToActionVOs(collections);
    }

    @Override
    public List<ContentActionVO> selectActivitySignups(Long contentId) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        List<ContentAction> signups = contentActionMapper.selectActivitySignups(contentId);
        return convertToActionVOs(signups);
    }

    @Override
    public Map<String, Object> getContentActionStats(Long contentId) {
        if (contentId == null) {
            return new HashMap<>();
        }

        Map<Integer, Long> stats = contentActionMapper.selectActionStatsByContentId(contentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("likeCount", stats.getOrDefault(ContentAction.Action.LIKE.getCode(), 0L));
        result.put("commentCount", stats.getOrDefault(ContentAction.Action.COMMENT.getCode(), 0L));
        result.put("shareCount", stats.getOrDefault(ContentAction.Action.SHARE.getCode(), 0L));
        result.put("collectCount", stats.getOrDefault(ContentAction.Action.COLLECT.getCode(), 0L));
        result.put("viewCount", stats.getOrDefault(ContentAction.Action.VIEW.getCode(), 0L));
        result.put("signupCount", stats.getOrDefault(ContentAction.Action.SIGNUP.getCode(), 0L));
        
        return result;
    }

    @Override
    public Map<String, Object> getUserActionStatus(Long contentId, Long userId) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (contentId == null || targetUserId == null) {
            return new HashMap<>();
        }

        List<ContentAction> userActions = contentActionMapper.selectUserActionsOnContent(contentId, targetUserId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("liked", false);
        status.put("collected", false);
        status.put("signed", false);
        status.put("commented", false);
        status.put("shared", false);

        for (ContentAction action : userActions) {
            switch (ContentAction.Action.fromCode(action.getAction())) {
                case LIKE:
                    status.put("liked", true);
                    break;
                case COLLECT:
                    status.put("collected", true);
                    break;
                case SIGNUP:
                    status.put("signed", true);
                    break;
                case COMMENT:
                    status.put("commented", true);
                    break;
                case SHARE:
                    status.put("shared", true);
                    break;
                default:
                    break;
            }
        }

        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId) {
        // TODO: 实现评论点赞功能，需要扩展数据结�?
        log.info("点赞评论，评论ID：{}", commentId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId) {
        // TODO: 实现取消评论点赞功能
        log.info("取消点赞评论，评论ID：{}", commentId);
        return true;
    }

    @Override
    public List<ContentActionVO> selectUserActivities(Long userId, Integer limit) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        LambdaQueryWrapper<ContentAction> queryWrapper = Wrappers.lambdaQuery(ContentAction.class)
                .eq(ContentAction::getUserId, targetUserId)
                .orderByDesc(ContentAction::getCreatedAt)
                .last("LIMIT " + (limit != null ? limit : 20));

        List<ContentAction> activities = contentActionMapper.selectList(queryWrapper);
        return convertToActionVOs(activities);
    }

    @Override
    public List<ContentActionVO> selectHotComments(Long contentId, Integer limit) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        // TODO: 实现热门评论查询逻辑，按照点赞数排序
        List<ContentAction> comments = contentActionMapper.selectContentComments(
                contentId, limit != null ? limit : 10);
        
        return convertToActionVOs(comments);
    }

    /**
     * 检查行为是否已存在
     */
    private boolean checkActionExists(Long contentId, Long userId, Integer action) {
        LambdaQueryWrapper<ContentAction> queryWrapper = Wrappers.lambdaQuery(ContentAction.class)
                .eq(ContentAction::getContentId, contentId)
                .eq(ContentAction::getUserId, userId)
                .eq(ContentAction::getAction, action);

        return contentActionMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 检查是否为可重复执行的行为
     */
    private boolean isRepeatableAction(Integer action) {
        ContentAction.Action actionType = ContentAction.Action.fromCode(action);
        return actionType == ContentAction.Action.COMMENT ||
               actionType == ContentAction.Action.SHARE ||
               actionType == ContentAction.Action.VIEW;
    }

    /**
     * 构建行为数据
     */
    private Map<String, Object> buildActionData(ContentActionDTO dto) {
        Map<String, Object> data = new HashMap<>();

        if (ContentAction.Action.COMMENT.getCode().equals(dto.getAction())) {
            if (StringUtils.isNotBlank(dto.getCommentContent())) {
                data.put("content", dto.getCommentContent());
            }
            if (dto.getReplyToId() != null) {
                data.put("reply_to_id", dto.getReplyToId());
            }
            if (dto.getReplyToUserId() != null) {
                data.put("reply_to_user_id", dto.getReplyToUserId());
            }
        }

        if (ContentAction.Action.SIGNUP.getCode().equals(dto.getAction()) && dto.getSignupInfo() != null) {
            data.put("signup_info", dto.getSignupInfo());
        }

        if (ContentAction.Action.REPORT.getCode().equals(dto.getAction()) && StringUtils.isNotBlank(dto.getReportReason())) {
            data.put("report_reason", dto.getReportReason());
        }

        if (dto.getExtraData() != null) {
            data.putAll(dto.getExtraData());
        }

        return data.isEmpty() ? null : data;
    }

    /**
     * 转换为行为VO列表
     */
    private List<ContentActionVO> convertToActionVOs(List<ContentAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return new ArrayList<>();
        }

        List<ContentActionVO> result = new ArrayList<>();
        for (ContentAction action : actions) {
            ContentActionVO vo = convertToActionVO(action);
            result.add(vo);
        }
        return result;
    }

    /**
     * 转换为行为VO
     */
    private ContentActionVO convertToActionVO(ContentAction action) {
        return ContentActionVO.builder()
                .id(action.getId())
                .contentId(action.getContentId())
                .userId(action.getUserId())
                .action(action.getAction())
                .actionDesc(action.getActionDesc())
                .commentContent(action.getCommentContent())
                .replyToId(action.getReplyToId())
                .signupInfo(action.getSignupInfo())
                .createdAt(action.getCreatedAt())
                .extraData(action.getData())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean commentContent(ContentActionDTO commentDTO) {
        return executeAction(commentDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean signupActivity(ContentActionDTO signupDTO) {
        return executeAction(signupDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportContent(ContentActionDTO reportDTO) {
        return executeAction(reportDTO);
    }

    @Override
    public List<ContentActionVO> getContentLikes(Long contentId) {
        ContentActionQueryDTO query = ContentActionQueryDTO.builder()
                .contentId(contentId)
                .action(ContentAction.Action.LIKE.getCode())
                .build();
        return selectActionList(query);
    }

    @Override
    public List<ContentActionVO> getContentComments(Long contentId) {
        return selectContentComments(contentId, 50);
    }

    @Override
    public List<ContentActionVO> getActivitySignups(Long activityId) {
        return selectActivitySignups(activityId);
    }

    @Override
    public List<ContentActionVO> getMyCollections(Integer contentType) {
        return selectUserCollections(null, 50);
    }

    @Override
    public List<ContentActionVO> getMyComments() {
        return selectUserActivities(null, 50);
    }

    @Override
    public Map<String, Boolean> checkUserActionStatus(Long contentId) {
        Map<String, Object> status = getUserActionStatus(contentId, null);
        Map<String, Boolean> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : status.entrySet()) {
            result.put(entry.getKey(), (Boolean) entry.getValue());
        }
        return result;
    }

    @Override
    public Map<String, Long> getContentActionStatistics(Long contentId) {
        Map<String, Object> stats = getContentActionStats(contentId);
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            result.put(entry.getKey(), (Long) entry.getValue());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAction(Long actionId) {
        if (actionId == null) {
            throw new ServiceException("行为记录ID不能为空");
        }

        ContentAction action = contentActionMapper.selectById(actionId);
        if (action == null) {
            throw new ServiceException("行为记录不存在");
        }

        // 检查权限
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(action.getUserId())) {
            throw new ServiceException("无权限删除该行为记录");
        }

        int result = contentActionMapper.deleteById(actionId);
        log.info("删除行为记录成功，记录ID：{}", actionId);
        return result > 0;
    }

    // ============ APP专用接口实现（新增） ============

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long contentId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            throw new ServiceException("未登录");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.eq(ContentAction::getContentId, contentId);
        wrapper.eq(ContentAction::getAction, 1); // 1=点赞
        wrapper.last("LIMIT 1");

        ContentAction existingAction = contentActionMapper.selectOne(wrapper);

        if (existingAction != null) {
            // 已点赞，取消点赞
            contentActionMapper.deleteById(existingAction.getId());
            log.info("用户{}取消点赞内容{}", userId, contentId);
            return false; // 返回false表示取消点赞
        } else {
            // 未点赞，添加点赞
            return likeContent(contentId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long contentId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            throw new ServiceException("未登录");
        }

        // 检查是否已收藏
        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.eq(ContentAction::getContentId, contentId);
        wrapper.eq(ContentAction::getAction, 4); // 4=收藏
        wrapper.last("LIMIT 1");

        ContentAction existingAction = contentActionMapper.selectOne(wrapper);

        if (existingAction != null) {
            // 已收藏，取消收藏
            contentActionMapper.deleteById(existingAction.getId());
            log.info("用户{}取消收藏内容{}", userId, contentId);
            return false; // 返回false表示取消收藏
        } else {
            // 未收藏，添加收藏
            return collectContent(contentId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordShare(Long contentId) {
        return shareContent(contentId);
    }

    @Override
    public boolean checkLikeStatus(Long contentId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.eq(ContentAction::getContentId, contentId);
        wrapper.eq(ContentAction::getAction, 1); // 1=点赞
        wrapper.last("LIMIT 1");

        return contentActionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkCollectStatus(Long contentId) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.eq(ContentAction::getContentId, contentId);
        wrapper.eq(ContentAction::getAction, 4); // 4=收藏
        wrapper.last("LIMIT 1");

        return contentActionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Map<Long, Boolean> batchCheckLikeStatus(List<Long> contentIds) {
        Map<Long, Boolean> result = new HashMap<>();
        if (contentIds == null || contentIds.isEmpty()) {
            return result;
        }

        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            // 未登录，全部返回false
            for (Long contentId : contentIds) {
                result.put(contentId, false);
            }
            return result;
        }

        // 查询用户对这些内容的点赞记录
        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.in(ContentAction::getContentId, contentIds);
        wrapper.eq(ContentAction::getAction, 1); // 1=点赞

        List<ContentAction> actions = contentActionMapper.selectList(wrapper);
        Set<Long> likedContentIds = actions.stream()
                .map(ContentAction::getContentId)
                .collect(Collectors.toSet());

        // 填充结果
        for (Long contentId : contentIds) {
            result.put(contentId, likedContentIds.contains(contentId));
        }

        return result;
    }

    @Override
    public Map<Long, Boolean> batchCheckCollectStatus(List<Long> contentIds) {
        Map<Long, Boolean> result = new HashMap<>();
        if (contentIds == null || contentIds.isEmpty()) {
            return result;
        }

        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            // 未登录，全部返回false
            for (Long contentId : contentIds) {
                result.put(contentId, false);
            }
            return result;
        }

        // 查询用户对这些内容的收藏记录
        LambdaQueryWrapper<ContentAction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ContentAction::getUserId, userId);
        wrapper.in(ContentAction::getContentId, contentIds);
        wrapper.eq(ContentAction::getAction, 4); // 4=收藏

        List<ContentAction> actions = contentActionMapper.selectList(wrapper);
        Set<Long> collectedContentIds = actions.stream()
                .map(ContentAction::getContentId)
                .collect(Collectors.toSet());

        // 填充结果
        for (Long contentId : contentIds) {
            result.put(contentId, collectedContentIds.contains(contentId));
        }

        return result;
    }

    @Override
    public List<ContentListVO> getMyLikes() {
        // TODO: 实现获取我的点赞列表
        // 需要关联Content表返回ContentListVO
        log.warn("getMyLikes() 方法尚未实现完整逻辑");
        return new ArrayList<>();
    }

    @Override
    public List<ContentListVO> getMyCollects() {
        // TODO: 实现获取我的收藏列表
        // 需要关联Content表返回ContentListVO
        log.warn("getMyCollects() 方法尚未实现完整逻辑");
        return new ArrayList<>();
    }
}
