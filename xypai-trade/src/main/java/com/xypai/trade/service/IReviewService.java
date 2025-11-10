package com.xypai.trade.service;

import com.xypai.trade.domain.dto.ReviewCreateDTO;
import com.xypai.trade.domain.dto.ReviewReplyDTO;
import com.xypai.trade.domain.vo.ReviewDetailVO;
import com.xypai.trade.domain.vo.ReviewListVO;

import java.util.List;
import java.util.Map;

/**
 * 评价服务接口
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
public interface IReviewService {

    /**
     * 创建评价
     *
     * @param reviewCreateDTO 评价创建DTO
     * @return 评价ID
     */
    Long createReview(ReviewCreateDTO reviewCreateDTO);

    /**
     * 商家回复评价
     *
     * @param reviewReplyDTO 回复DTO
     * @return 是否成功
     */
    boolean replyReview(ReviewReplyDTO reviewReplyDTO);

    /**
     * 查询评价详情
     *
     * @param reviewId 评价ID
     * @return 评价详情
     */
    ReviewDetailVO selectReviewById(Long reviewId);

    /**
     * 查询订单的评价
     *
     * @param orderId 订单ID
     * @return 评价详情
     */
    ReviewDetailVO selectReviewByOrderId(Long orderId);

    /**
     * 查询内容的评价列表
     *
     * @param contentId 内容ID
     * @param status 状态筛选
     * @return 评价列表
     */
    List<ReviewListVO> selectContentReviews(Long contentId, Integer status);

    /**
     * 查询用户收到的评价列表
     *
     * @param revieweeId 被评价人ID
     * @param status 状态筛选
     * @return 评价列表
     */
    List<ReviewListVO> selectUserReviews(Long revieweeId, Integer status);

    /**
     * 查询我发表的评价
     *
     * @return 评价列表
     */
    List<ReviewListVO> selectMyReviews();

    /**
     * 查询内容的评价统计
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    Map<String, Object> getContentReviewStats(Long contentId);

    /**
     * 查询用户的评价统计
     *
     * @param revieweeId 被评价人ID
     * @return 统计信息
     */
    Map<String, Object> getUserReviewStats(Long revieweeId);

    /**
     * 点赞评价
     *
     * @param reviewId 评价ID
     * @return 是否成功
     */
    boolean likeReview(Long reviewId);

    /**
     * 取消点赞
     *
     * @param reviewId 评价ID
     * @return 是否成功
     */
    boolean unlikeReview(Long reviewId);

    /**
     * 隐藏评价（管理员）
     *
     * @param reviewId 评价ID
     * @return 是否成功
     */
    boolean hideReview(Long reviewId);

    /**
     * 删除评价（软删除）
     *
     * @param reviewId 评价ID
     * @return 是否成功
     */
    boolean deleteReview(Long reviewId);

    /**
     * 检查订单是否可以评价
     *
     * @param orderId 订单ID
     * @return 是否可以评价
     */
    boolean canReview(Long orderId);

    /**
     * 检查订单是否已评价
     *
     * @param orderId 订单ID
     * @return 是否已评价
     */
    boolean hasReviewed(Long orderId);
}

