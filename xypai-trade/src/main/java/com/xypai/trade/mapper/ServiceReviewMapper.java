package com.xypai.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.trade.domain.entity.ServiceReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 服务评价Mapper接口
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Mapper
public interface ServiceReviewMapper extends BaseMapper<ServiceReview> {

    /**
     * 根据订单ID查询评价
     *
     * @param orderId 订单ID
     * @return 评价信息
     */
    ServiceReview selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询内容的评价统计
     *
     * @param contentId 内容ID
     * @return 统计信息（total_reviews, avg_rating, good_rate等）
     */
    Map<String, Object> selectContentReviewStats(@Param("contentId") Long contentId);

    /**
     * 查询用户收到的评价统计
     *
     * @param revieweeId 被评价人ID
     * @return 统计信息
     */
    Map<String, Object> selectUserReviewStats(@Param("revieweeId") Long revieweeId);

    /**
     * 查询好评率
     *
     * @param contentId 内容ID
     * @return 好评率（百分比）
     */
    BigDecimal selectGoodRate(@Param("contentId") Long contentId);

    /**
     * 查询评分分布
     *
     * @param contentId 内容ID
     * @return 评分分布统计
     */
    Map<String, Object> selectRatingDistribution(@Param("contentId") Long contentId);

    /**
     * 增加点赞数
     *
     * @param reviewId 评价ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 减少点赞数
     *
     * @param reviewId 评价ID
     * @return 影响行数
     */
    int decrementLikeCount(@Param("reviewId") Long reviewId);
}

