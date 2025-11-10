package com.xypai.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.trade.domain.dto.ReviewCreateDTO;
import com.xypai.trade.domain.dto.ReviewReplyDTO;
import com.xypai.trade.domain.entity.ServiceOrder;
import com.xypai.trade.domain.entity.ServiceReview;
import com.xypai.trade.domain.vo.ReviewDetailVO;
import com.xypai.trade.domain.vo.ReviewListVO;
import com.xypai.trade.mapper.ServiceOrderMapper;
import com.xypai.trade.mapper.ServiceReviewMapper;
import com.xypai.trade.service.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ServiceReviewMapper serviceReviewMapper;
    private final ServiceOrderMapper serviceOrderMapper;

    /**
     * 评价有效期（订单完成7天内）
     */
    private static final long REVIEW_DEADLINE_DAYS = 7;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(ReviewCreateDTO reviewCreateDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 1. 验证订单是否可以评价
        if (!canReview(reviewCreateDTO.getOrderId())) {
            throw new ServiceException("订单不可评价");
        }

        // 2. 检查是否已评价
        if (hasReviewed(reviewCreateDTO.getOrderId())) {
            throw new ServiceException("该订单已评价，不能重复评价");
        }

        // 3. 查询订单信息
        ServiceOrder order = serviceOrderMapper.selectById(reviewCreateDTO.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 4. 验证评价人是否为买家
        if (!currentUserId.equals(order.getBuyerId())) {
            throw new ServiceException("只有买家可以评价订单");
        }

        // 5. 处理图片列表
        String reviewImages = null;
        if (reviewCreateDTO.getReviewImages() != null && !reviewCreateDTO.getReviewImages().isEmpty()) {
            int limit = Math.min(reviewCreateDTO.getReviewImages().size(), 9);
            reviewImages = String.join(",", reviewCreateDTO.getReviewImages().subList(0, limit));
        }

        // 6. 构建评价实体
        ServiceReview review = ServiceReview.builder()
                .orderId(reviewCreateDTO.getOrderId())
                .contentId(order.getContentId())
                .serviceType(order.getServiceType() != null ? order.getServiceType() : 1)
                .reviewerId(currentUserId)
                .revieweeId(order.getSellerId())
                .ratingOverall(reviewCreateDTO.getRatingOverall())
                .ratingService(reviewCreateDTO.getRatingService())
                .ratingAttitude(reviewCreateDTO.getRatingAttitude())
                .ratingQuality(reviewCreateDTO.getRatingQuality())
                .reviewText(reviewCreateDTO.getReviewText())
                .reviewImages(reviewImages)
                .isAnonymous(reviewCreateDTO.getIsAnonymous() != null ? reviewCreateDTO.getIsAnonymous() : false)
                .likeCount(0)
                .status(ServiceReview.Status.PUBLISHED.getCode()) // 默认已发布（可配置为待审核）
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = serviceReviewMapper.insert(review);
        if (result <= 0) {
            throw new ServiceException("创建评价失败");
        }

        // 7. 异步更新服务统计（TODO: 通过消息队列）
        // updateServiceStats(order.getContentId(), review);

        log.info("✅ 创建评价成功，评价ID：{}，订单ID：{}，评分：{}", 
                review.getId(), reviewCreateDTO.getOrderId(), review.getFormattedRating());

        return review.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyReview(ReviewReplyDTO reviewReplyDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 1. 查询评价
        ServiceReview review = serviceReviewMapper.selectById(reviewReplyDTO.getReviewId());
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        // 2. 验证回复人是否为被评价人
        if (!currentUserId.equals(review.getRevieweeId())) {
            throw new ServiceException("只有被评价人可以回复评价");
        }

        // 3. 检查是否已回复
        if (review.hasReply()) {
            throw new ServiceException("该评价已回复，不能重复回复");
        }

        // 4. 更新回复内容
        review.addReply(reviewReplyDTO.getReplyText());
        
        int result = serviceReviewMapper.updateById(review);
        if (result <= 0) {
            throw new ServiceException("回复评价失败");
        }

        log.info("✅ 商家回复评价成功，评价ID：{}，回复人：{}", reviewReplyDTO.getReviewId(), currentUserId);

        return true;
    }

    @Override
    public ReviewDetailVO selectReviewById(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("评价ID不能为空");
        }

        ServiceReview review = serviceReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        return convertToDetailVO(review);
    }

    @Override
    public ReviewDetailVO selectReviewByOrderId(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceReview review = serviceReviewMapper.selectByOrderId(orderId);
        if (review == null) {
            return null; // 订单未评价返回null
        }

        return convertToDetailVO(review);
    }

    @Override
    public List<ReviewListVO> selectContentReviews(Long contentId, Integer status) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        LambdaQueryWrapper<ServiceReview> wrapper = Wrappers.lambdaQuery(ServiceReview.class)
                .eq(ServiceReview::getContentId, contentId)
                .eq(status != null, ServiceReview::getStatus, status)
                .orderByDesc(ServiceReview::getCreatedAt);

        List<ServiceReview> reviews = serviceReviewMapper.selectList(wrapper);
        return convertToListVOs(reviews);
    }

    @Override
    public List<ReviewListVO> selectUserReviews(Long revieweeId, Integer status) {
        if (revieweeId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        LambdaQueryWrapper<ServiceReview> wrapper = Wrappers.lambdaQuery(ServiceReview.class)
                .eq(ServiceReview::getRevieweeId, revieweeId)
                .eq(status != null, ServiceReview::getStatus, status)
                .orderByDesc(ServiceReview::getCreatedAt);

        List<ServiceReview> reviews = serviceReviewMapper.selectList(wrapper);
        return convertToListVOs(reviews);
    }

    @Override
    public List<ReviewListVO> selectMyReviews() {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        LambdaQueryWrapper<ServiceReview> wrapper = Wrappers.lambdaQuery(ServiceReview.class)
                .eq(ServiceReview::getReviewerId, currentUserId)
                .orderByDesc(ServiceReview::getCreatedAt);

        List<ServiceReview> reviews = serviceReviewMapper.selectList(wrapper);
        return convertToListVOs(reviews);
    }

    @Override
    public Map<String, Object> getContentReviewStats(Long contentId) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        return serviceReviewMapper.selectContentReviewStats(contentId);
    }

    @Override
    public Map<String, Object> getUserReviewStats(Long revieweeId) {
        if (revieweeId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        return serviceReviewMapper.selectUserReviewStats(revieweeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeReview(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("评价ID不能为空");
        }

        int result = serviceReviewMapper.incrementLikeCount(reviewId);
        if (result <= 0) {
            throw new ServiceException("点赞失败");
        }

        log.info("✅ 点赞评价成功，评价ID：{}", reviewId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeReview(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("评价ID不能为空");
        }

        int result = serviceReviewMapper.decrementLikeCount(reviewId);
        if (result <= 0) {
            throw new ServiceException("取消点赞失败");
        }

        log.info("✅ 取消点赞成功，评价ID：{}", reviewId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean hideReview(Long reviewId) {
        if (reviewId == null) {
            throw new ServiceException("评价ID不能为空");
        }

        ServiceReview review = serviceReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        review.hide();
        int result = serviceReviewMapper.updateById(review);
        
        log.info("✅ 隐藏评价成功，评价ID：{}", reviewId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReview(Long reviewId) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        ServiceReview review = serviceReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }

        // 只有评价人可以删除自己的评价
        if (!currentUserId.equals(review.getReviewerId())) {
            throw new ServiceException("只能删除自己的评价");
        }

        // 已回复的评价不能删除
        if (review.hasReply()) {
            throw new ServiceException("商家已回复的评价不能删除");
        }

        review.delete();
        int result = serviceReviewMapper.updateById(review);
        
        log.info("✅ 删除评价成功，评价ID：{}", reviewId);
        return result > 0;
    }

    @Override
    public boolean canReview(Long orderId) {
        if (orderId == null) {
            return false;
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        // 1. 订单必须已完成
        if (!order.isCompleted()) {
            log.warn("订单未完成，不能评价。订单ID：{}，状态：{}", orderId, order.getStatusDesc());
            return false;
        }

        // 2. 订单完成7天内可以评价
        if (order.getCompletedAt() != null) {
            LocalDateTime deadline = order.getCompletedAt().plusDays(REVIEW_DEADLINE_DAYS);
            if (LocalDateTime.now().isAfter(deadline)) {
                log.warn("订单已超过评价期限，不能评价。订单ID：{}，完成时间：{}", orderId, order.getCompletedAt());
                return false;
            }
        }

        // 3. 检查是否已评价
        if (hasReviewed(orderId)) {
            log.warn("订单已评价，不能重复评价。订单ID：{}", orderId);
            return false;
        }

        return true;
    }

    @Override
    public boolean hasReviewed(Long orderId) {
        if (orderId == null) {
            return false;
        }

        ServiceReview review = serviceReviewMapper.selectByOrderId(orderId);
        return review != null;
    }

    // ==========================================
    // 私有方法：数据转换
    // ==========================================

    /**
     * 转换为列表VO
     */
    private List<ReviewListVO> convertToListVOs(List<ServiceReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }

        return reviews.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表VO（简化版）
     */
    private ReviewListVO convertToListVO(ServiceReview review) {
        // 截断评价文字（最多100字）
        String truncatedText = review.getReviewText();
        if (truncatedText != null && truncatedText.length() > 100) {
            truncatedText = truncatedText.substring(0, 100) + "...";
        }

        // 图片列表（最多显示3张）
        List<String> images = review.getImageList();
        if (images.size() > 3) {
            images = images.subList(0, 3);
        }

        // 截断回复内容（最多50字）
        String truncatedReply = review.getReplyText();
        if (truncatedReply != null && truncatedReply.length() > 50) {
            truncatedReply = truncatedReply.substring(0, 50) + "...";
        }

        return ReviewListVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .reviewerNickname(review.getIsAnonymous() ? "匿名用户" : getUserNickname(review.getReviewerId()))
                .reviewerAvatar(review.getIsAnonymous() ? null : getUserAvatar(review.getReviewerId()))
                .ratingOverall(review.getRatingOverall())
                .starLevel(review.getStarLevel())
                .ratingLevel(review.getRatingLevel())
                .reviewText(truncatedText)
                .reviewImages(images)
                .hasImages(review.hasImages())
                .isAnonymous(review.getIsAnonymous())
                .likeCount(review.getLikeCount())
                .hasReply(review.hasReply())
                .replyText(truncatedReply)
                .createdAt(review.getCreatedAt())
                .build();
    }

    /**
     * 转换为详情VO
     */
    private ReviewDetailVO convertToDetailVO(ServiceReview review) {
        return ReviewDetailVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .contentId(review.getContentId())
                .serviceType(review.getServiceType())
                .serviceTypeDesc(review.getServiceTypeDesc())
                .reviewerId(review.getReviewerId())
                .reviewerNickname(review.getIsAnonymous() ? "匿名用户" : getUserNickname(review.getReviewerId()))
                .reviewerAvatar(review.getIsAnonymous() ? null : getUserAvatar(review.getReviewerId()))
                .revieweeId(review.getRevieweeId())
                .revieweeNickname(getUserNickname(review.getRevieweeId()))
                .ratingOverall(review.getRatingOverall())
                .ratingService(review.getRatingService())
                .ratingAttitude(review.getRatingAttitude())
                .ratingQuality(review.getRatingQuality())
                .starLevel(review.getStarLevel())
                .ratingLevel(review.getRatingLevel())
                .reviewText(review.getReviewText())
                .reviewImages(review.getImageList())
                .isAnonymous(review.getIsAnonymous())
                .likeCount(review.getLikeCount())
                .replyText(review.getReplyText())
                .replyTime(review.getReplyTime())
                .hasReply(review.hasReply())
                .status(review.getStatus())
                .statusDesc(review.getStatusDesc())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /**
     * 获取用户昵称（TODO: 调用用户服务）
     */
    private String getUserNickname(Long userId) {
        // TODO: 通过Feign调用用户服务获取昵称
        return "用户" + userId;
    }

    /**
     * 获取用户头像（TODO: 调用用户服务）
     */
    private String getUserAvatar(Long userId) {
        // TODO: 通过Feign调用用户服务获取头像
        return "https://cdn.xypai.com/default-avatar.png";
    }

    /**
     * 更新服务统计（TODO: 异步通过消息队列）
     */
    private void updateServiceStats(Long contentId, ServiceReview review) {
        // TODO: 发送消息到消息队列，异步更新ServiceStats表
        log.info("📤 发送评价统计更新消息，内容ID：{}，评分：{}", contentId, review.getRatingOverall());
    }
}

