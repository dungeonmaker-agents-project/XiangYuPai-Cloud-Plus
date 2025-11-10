package com.xypai.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.trade.domain.entity.ServiceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务订单Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ServiceOrderMapper extends BaseMapper<ServiceOrder> {

    /**
     * 查询用户购买的订单
     *
     * @param buyerId 买家ID
     * @param status 订单状态(可选)
     * @param limit 限制数量
     * @return 订单列表
     */
    List<ServiceOrder> selectBuyerOrders(@Param("buyerId") Long buyerId, 
                                        @Param("status") Integer status, 
                                        @Param("limit") Integer limit);

    /**
     * 查询用户出售的订单
     *
     * @param sellerId 卖家ID
     * @param status 订单状态(可选)
     * @param limit 限制数量
     * @return 订单列表
     */
    List<ServiceOrder> selectSellerOrders(@Param("sellerId") Long sellerId, 
                                         @Param("status") Integer status, 
                                         @Param("limit") Integer limit);

    /**
     * 根据订单编号查询
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    ServiceOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户的订单统计
     *
     * @param userId 用户ID
     * @param role 角色(buyer=买家, seller=卖家)
     * @return 统计信息
     */
    Map<String, Object> selectUserOrderStats(@Param("userId") Long userId, 
                                            @Param("role") String role);

    /**
     * 查询内容的订单统计
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    Map<String, Object> selectContentOrderStats(@Param("contentId") Long contentId);

    /**
     * 查询即将超时的订单
     *
     * @param status 订单状态
     * @param timeoutHours 超时小时数
     * @return 订单列表
     */
    List<ServiceOrder> selectTimeoutOrders(@Param("status") Integer status, 
                                          @Param("timeoutHours") Integer timeoutHours);

    /**
     * 批量更新订单状态
     *
     * @param orderIds 订单ID列表
     * @param newStatus 新状态
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("orderIds") List<Long> orderIds, 
                         @Param("newStatus") Integer newStatus, 
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 排行榜列表
     */
    List<Map<String, Object>> selectSalesRanking(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate, 
                                                 @Param("limit") Integer limit);

    /**
     * 查询热门技能内容
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 热门内容列表
     */
    List<Map<String, Object>> selectPopularSkills(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, 
                                                  @Param("limit") Integer limit);

    /**
     * 查询收入统计
     *
     * @param sellerId 卖家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计
     */
    Map<String, Object> selectIncomeStats(@Param("sellerId") Long sellerId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 查询支出统计
     *
     * @param buyerId 买家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出统计
     */
    Map<String, Object> selectExpenseStats(@Param("buyerId") Long buyerId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 根据状态和时间范围统计订单数量
     *
     * @param status 订单状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单数量
     */
    Long countOrdersByStatusAndTime(@Param("status") Integer status, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 查询平台交易统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易统计
     */
    Map<String, Object> selectPlatformTradeStats(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
}
