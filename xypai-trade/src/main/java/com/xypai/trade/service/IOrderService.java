package com.xypai.trade.service;

import com.xypai.trade.domain.dto.OrderCreateDTO;
import com.xypai.trade.domain.dto.OrderQueryDTO;
import com.xypai.trade.domain.dto.OrderUpdateDTO;
import com.xypai.trade.domain.vo.OrderDetailVO;
import com.xypai.trade.domain.vo.OrderListVO;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IOrderService {

    /**
     * 创建订单
     *
     * @param orderCreateDTO 订单创建数据
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO orderCreateDTO);

    /**
     * 查询订单列表
     *
     * @param queryDTO 查询条件
     * @return 订单列表
     */
    List<OrderListVO> selectOrderList(OrderQueryDTO queryDTO);

    /**
     * 根据订单ID查询详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO selectOrderById(Long orderId);

    /**
     * 根据订单编号查询详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    OrderDetailVO selectOrderByNo(String orderNo);

    /**
     * 更新订单
     *
     * @param orderUpdateDTO 更新数据
     * @return 是否成功
     */
    boolean updateOrder(OrderUpdateDTO orderUpdateDTO);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId, String reason);

    /**
     * 确认完成订单
     *
     * @param orderId 订单ID
     * @param completionNote 完成备注
     * @return 是否成功
     */
    boolean completeOrder(Long orderId, String completionNote);

    /**
     * 开始服务
     *
     * @param orderId 订单ID
     * @param serviceNote 服务备注
     * @return 是否成功
     */
    boolean startService(Long orderId, String serviceNote);

    /**
     * 查询我购买的订单
     *
     * @param status 订单状态(可选)
     * @param limit 限制数量
     * @return 订单列表
     */
    List<OrderListVO> selectMyBuyOrders(Integer status, Integer limit);

    /**
     * 查询我出售的订单
     *
     * @param status 订单状态(可选)
     * @param limit 限制数量
     * @return 订单列表
     */
    List<OrderListVO> selectMySellOrders(Integer status, Integer limit);

    /**
     * 查询用户订单统计
     *
     * @param userId 用户ID(可选，不传则查询当前用户)
     * @return 统计信息
     */
    Map<String, Object> getUserOrderStats(Long userId);

    /**
     * 查询内容的订单统计
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    Map<String, Object> getContentOrderStats(Long contentId);

    /**
     * 查询即将超时的订单
     *
     * @param status 订单状态
     * @param timeoutHours 超时小时数
     * @return 订单列表
     */
    List<OrderListVO> selectTimeoutOrders(Integer status, Integer timeoutHours);

    /**
     * 批量处理超时订单
     *
     * @param orderIds 订单ID列表
     * @param newStatus 新状态
     * @return 处理数量
     */
    int batchHandleTimeoutOrders(List<Long> orderIds, Integer newStatus);

    /**
     * 查询销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 排行榜
     */
    List<Map<String, Object>> getSalesRanking(String startDate, String endDate, Integer limit);

    /**
     * 查询热门技能
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 限制数量
     * @return 热门技能列表
     */
    List<Map<String, Object>> getPopularSkills(String startDate, String endDate, Integer limit);

    /**
     * 查询收入统计
     *
     * @param sellerId 卖家ID(可选，不传则查询当前用户)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收入统计
     */
    Map<String, Object> getIncomeStats(Long sellerId, String startDate, String endDate);

    /**
     * 查询支出统计
     *
     * @param buyerId 买家ID(可选，不传则查询当前用户)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支出统计
     */
    Map<String, Object> getExpenseStats(Long buyerId, String startDate, String endDate);

    /**
     * 查询平台交易统计(管理员功能)
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易统计
     */
    Map<String, Object> getPlatformTradeStats(String startDate, String endDate);

    /**
     * 验证订单权限
     *
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param requireBuyer 是否要求买家权限
     * @param requireSeller 是否要求卖家权限
     * @return 是否有权限
     */
    boolean validateOrderPermission(Long orderId, Long userId, boolean requireBuyer, boolean requireSeller);

    /**
     * 检查订单是否可以执行指定操作
     *
     * @param orderId 订单ID
     * @param operation 操作类型
     * @return 是否可以执行
     */
    boolean canExecuteOperation(Long orderId, String operation);

    /**
     * 记录订单日志
     *
     * @param orderId 订单ID
     * @param actionType 操作类型
     * @param actionDesc 操作描述
     * @param operatorId 操作人ID
     * @param remark 备注
     * @return 是否成功
     */
    boolean recordOrderLog(Long orderId, String actionType, String actionDesc, Long operatorId, String remark);
}
