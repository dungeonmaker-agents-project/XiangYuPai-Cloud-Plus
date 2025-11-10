package com.xypai.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.trade.domain.dto.OrderCreateDTO;
import com.xypai.trade.domain.dto.OrderQueryDTO;
import com.xypai.trade.domain.dto.OrderUpdateDTO;
import com.xypai.trade.domain.entity.ServiceOrder;
import com.xypai.trade.domain.vo.OrderDetailVO;
import com.xypai.trade.domain.vo.OrderListVO;
import com.xypai.trade.mapper.ServiceOrderMapper;
import com.xypai.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final ServiceOrderMapper serviceOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 验证买家不能是卖家本人
        if (currentUserId.equals(orderCreateDTO.getSellerId())) {
            throw new ServiceException("不能购买自己的服务");
        }

        // 🔧 双写策略：同时构建data字段和具体字段
        Map<String, Object> data = buildOrderData(orderCreateDTO);
        
        // 计算金额（分）
        Long amountFen = orderCreateDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        Long baseFee = orderCreateDTO.getBaseFee() != null ? 
                      orderCreateDTO.getBaseFee().multiply(BigDecimal.valueOf(100)).longValue() : amountFen;
        Long personFee = orderCreateDTO.getPersonFee() != null ? 
                        orderCreateDTO.getPersonFee().multiply(BigDecimal.valueOf(100)).longValue() : 0L;
        Long platformFee = orderCreateDTO.getPlatformFee() != null ? 
                          orderCreateDTO.getPlatformFee().multiply(BigDecimal.valueOf(100)).longValue() : 
                          (long)(amountFen * 0.05); // 默认5%平台服务费
        Long discountAmount = orderCreateDTO.getDiscountAmount() != null ? 
                             orderCreateDTO.getDiscountAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L;
        Long actualAmount = baseFee + personFee - discountAmount;

        // 解析服务时间
        LocalDateTime serviceTime = null;
        if (StringUtils.isNotBlank(orderCreateDTO.getServiceTime())) {
            try {
                serviceTime = LocalDateTime.parse(orderCreateDTO.getServiceTime(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                log.warn("服务时间解析失败：{}", orderCreateDTO.getServiceTime());
            }
        }

        ServiceOrder order = ServiceOrder.builder()
                .buyerId(currentUserId)
                .sellerId(orderCreateDTO.getSellerId())
                .contentId(orderCreateDTO.getContentId())
                // 新字段（优先使用）
                .serviceType(orderCreateDTO.getServiceType() != null ? orderCreateDTO.getServiceType() : 1)
                .serviceName(orderCreateDTO.getServiceName())
                .serviceTime(serviceTime)
                .serviceDuration(orderCreateDTO.getServiceDuration())
                .participantCount(orderCreateDTO.getParticipantCount() != null ? orderCreateDTO.getParticipantCount() : 1)
                // 费用明细
                .amount(amountFen)
                .baseFee(baseFee)
                .personFee(personFee)
                .platformFee(platformFee)
                .discountAmount(discountAmount)
                .actualAmount(actualAmount)
                // 联系信息
                .contactName(orderCreateDTO.getContactName())
                .contactPhone(orderCreateDTO.getContactPhone())
                .specialRequest(orderCreateDTO.getSpecialRequest())
                // 旧字段
                .duration(orderCreateDTO.getDuration())
                .status(ServiceOrder.Status.PENDING_PAYMENT.getCode())
                // 兼容旧data字段（双写）
                .data(data)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = serviceOrderMapper.insert(order);
        if (result <= 0) {
            throw new ServiceException("创建订单失败");
        }

        // 生成订单编号
        order.generateOrderNo();
        serviceOrderMapper.updateById(order);

        // 记录订单日志
        recordOrderLog(order.getId(), "CREATE", "创建订单", currentUserId, 
                      String.format("用户创建新订单，服务类型：%s，实付：%.2f元", 
                              order.getServiceTypeDesc(), order.getActualAmountYuan()));

        log.info("✅ 创建订单成功 [双写模式]，订单号：{}，买家：{}，卖家：{}，实付金额：{}", 
                order.getOrderNo(), currentUserId, orderCreateDTO.getSellerId(), order.getFormattedActualAmount());
        
        return order.getId();
    }

    @Override
    public List<OrderListVO> selectOrderList(OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<ServiceOrder> queryWrapper = buildQueryWrapper(queryDTO);
        
        // 根据排序方式构建查询
        if ("amount_asc".equals(queryDTO.getOrderBy())) {
            queryWrapper.orderByAsc(ServiceOrder::getAmount);
        } else if ("amount_desc".equals(queryDTO.getOrderBy())) {
            queryWrapper.orderByDesc(ServiceOrder::getAmount);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(ServiceOrder::getCreatedAt);
        }

        List<ServiceOrder> orders = serviceOrderMapper.selectList(queryWrapper);
        return convertToListVOs(orders);
    }

    @Override
    public OrderDetailVO selectOrderById(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !validateOrderPermission(orderId, currentUserId, false, false)) {
            throw new ServiceException("无权限查看该订单");
        }

        return convertToDetailVO(order);
    }

    @Override
    public OrderDetailVO selectOrderByNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new ServiceException("订单编号不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        return convertToDetailVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(OrderUpdateDTO orderUpdateDTO) {
        if (orderUpdateDTO.getId() == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder existOrder = serviceOrderMapper.selectById(orderUpdateDTO.getId());
        if (existOrder == null) {
            throw new ServiceException("订单不存在");
        }

        // 权限验证 - 只有买家可以修改订单
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(existOrder.getBuyerId())) {
            throw new ServiceException("只有买家可以修改订单");
        }

        // 状态验证 - 只有待付款状态可以修改
        if (!existOrder.isPendingPayment()) {
            throw new ServiceException("只有待付款状态的订单可以修改");
        }

        // 🔧 双写策略：构建更新数据（data字段 + 具体字段）
        Map<String, Object> data = mergeOrderData(existOrder.getData(), orderUpdateDTO);

        // 计算更新后的费用
        Long baseFee = orderUpdateDTO.getBaseFee() != null ? 
                      orderUpdateDTO.getBaseFee().multiply(BigDecimal.valueOf(100)).longValue() : null;
        Long personFee = orderUpdateDTO.getPersonFee() != null ? 
                        orderUpdateDTO.getPersonFee().multiply(BigDecimal.valueOf(100)).longValue() : null;
        Long platformFee = orderUpdateDTO.getPlatformFee() != null ? 
                          orderUpdateDTO.getPlatformFee().multiply(BigDecimal.valueOf(100)).longValue() : null;
        Long discountAmount = orderUpdateDTO.getDiscountAmount() != null ? 
                             orderUpdateDTO.getDiscountAmount().multiply(BigDecimal.valueOf(100)).longValue() : null;
        
        // 重新计算实际支付金额
        Long actualAmount = null;
        if (baseFee != null || personFee != null || discountAmount != null) {
            long base = baseFee != null ? baseFee : (existOrder.getBaseFee() != null ? existOrder.getBaseFee() : 0);
            long person = personFee != null ? personFee : (existOrder.getPersonFee() != null ? existOrder.getPersonFee() : 0);
            long discount = discountAmount != null ? discountAmount : (existOrder.getDiscountAmount() != null ? existOrder.getDiscountAmount() : 0);
            actualAmount = base + person - discount;
        }

        // 解析服务时间
        LocalDateTime serviceTime = null;
        if (StringUtils.isNotBlank(orderUpdateDTO.getServiceTime())) {
            try {
                serviceTime = LocalDateTime.parse(orderUpdateDTO.getServiceTime(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                log.warn("服务时间解析失败：{}", orderUpdateDTO.getServiceTime());
            }
        }

        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderUpdateDTO.getId())
                // 新字段
                .serviceType(orderUpdateDTO.getServiceType())
                .serviceName(orderUpdateDTO.getServiceName())
                .serviceTime(serviceTime)
                .serviceDuration(orderUpdateDTO.getServiceDuration())
                .participantCount(orderUpdateDTO.getParticipantCount())
                // 费用明细
                .amount(orderUpdateDTO.getAmount() != null ? 
                       orderUpdateDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue() : null)
                .baseFee(baseFee)
                .personFee(personFee)
                .platformFee(platformFee)
                .discountAmount(discountAmount)
                .actualAmount(actualAmount)
                // 联系信息
                .contactName(orderUpdateDTO.getContactName())
                .contactPhone(orderUpdateDTO.getContactPhone())
                .specialRequest(orderUpdateDTO.getSpecialRequest())
                // 旧字段
                .duration(orderUpdateDTO.getDuration())
                .status(orderUpdateDTO.getStatus())
                // 兼容旧data字段（双写）
                .data(data)
                .updatedAt(LocalDateTime.now())
                .version(orderUpdateDTO.getVersion())
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            throw new ServiceException("更新订单失败，可能是并发修改，请重试");
        }

        // 记录订单日志
        recordOrderLog(orderUpdateDTO.getId(), "UPDATE", "更新订单", currentUserId, 
                      String.format("用户修改订单信息 [双写模式]"));

        log.info("✅ 更新订单成功 [双写模式]，订单ID：{}", orderUpdateDTO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, String reason) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.canCancel()) {
            throw new ServiceException("订单当前状态不允许取消");
        }

        // 权限验证
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !validateOrderPermission(orderId, currentUserId, true, true)) {
            throw new ServiceException("无权限取消该订单");
        }

        LocalDateTime cancelTime = LocalDateTime.now();
        
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.CANCELLED.getCode())
                // 新字段：记录取消信息
                .cancelReason(reason)
                .cancelTime(cancelTime)
                .updatedAt(cancelTime)
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            throw new ServiceException("取消订单失败");
        }

        // 记录订单日志
        recordOrderLog(orderId, "CANCEL", "取消订单", currentUserId, 
                      StringUtils.isNotBlank(reason) ? "取消原因：" + reason : "用户取消订单");

        log.info("✅ 取消订单成功，订单ID：{}，原因：{}", orderId, reason);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, String completionNote) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.canComplete()) {
            throw new ServiceException("订单当前状态不允许完成");
        }

        // 权限验证 - 买家和卖家都可以确认完成
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !validateOrderPermission(orderId, currentUserId, true, true)) {
            throw new ServiceException("无权限操作该订单");
        }

        LocalDateTime completedTime = LocalDateTime.now();
        
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.COMPLETED.getCode())
                // 新字段：记录完成时间
                .completedAt(completedTime)
                .updatedAt(completedTime)
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            throw new ServiceException("确认完成订单失败");
        }

        // 转账给卖家（这里简化处理）
        // TODO: 调用钱包服务转账给卖家（actualAmount - platformFee）

        // 记录订单日志
        recordOrderLog(orderId, "COMPLETE", "确认完成", currentUserId, 
                      StringUtils.isNotBlank(completionNote) ? "完成备注：" + completionNote : "确认订单完成");

        log.info("✅ 确认完成订单成功，订单ID：{}，完成时间：{}", orderId, completedTime);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startService(Long orderId, String serviceNote) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.isPaid()) {
            throw new ServiceException("订单未付款，无法开始服务");
        }

        // 权限验证 - 只有卖家可以开始服务
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(order.getSellerId())) {
            throw new ServiceException("只有卖家可以开始服务");
        }

        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.IN_SERVICE.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            throw new ServiceException("开始服务失败");
        }

        // 记录订单日志
        recordOrderLog(orderId, "START_SERVICE", "开始服务", currentUserId, 
                      StringUtils.isNotBlank(serviceNote) ? "服务备注：" + serviceNote : "卖家开始提供服务");

        log.info("开始服务成功，订单ID：{}", orderId);
        return true;
    }

    @Override
    public List<OrderListVO> selectMyBuyOrders(Integer status, Integer limit) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        List<ServiceOrder> orders = serviceOrderMapper.selectBuyerOrders(
                currentUserId, status, limit != null ? limit : 20);
        return convertToListVOs(orders);
    }

    @Override
    public List<OrderListVO> selectMySellOrders(Integer status, Integer limit) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        List<ServiceOrder> orders = serviceOrderMapper.selectSellerOrders(
                currentUserId, status, limit != null ? limit : 20);
        return convertToListVOs(orders);
    }

    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        Long targetUserId = userId != null ? userId : LoginHelper.getUserId();
        if (targetUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        Map<String, Object> buyStats = serviceOrderMapper.selectUserOrderStats(targetUserId, "buyer");
        Map<String, Object> sellStats = serviceOrderMapper.selectUserOrderStats(targetUserId, "seller");

        Map<String, Object> result = new HashMap<>();
        result.put("buyStats", buyStats);
        result.put("sellStats", sellStats);
        result.put("userId", targetUserId);

        return result;
    }

    @Override
    public Map<String, Object> getContentOrderStats(Long contentId) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        return serviceOrderMapper.selectContentOrderStats(contentId);
    }

    @Override
    public List<OrderListVO> selectTimeoutOrders(Integer status, Integer timeoutHours) {
        List<ServiceOrder> orders = serviceOrderMapper.selectTimeoutOrders(
                status, timeoutHours != null ? timeoutHours : 24);
        return convertToListVOs(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchHandleTimeoutOrders(List<Long> orderIds, Integer newStatus) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }

        return serviceOrderMapper.batchUpdateStatus(orderIds, newStatus, LocalDateTime.now());
    }

    @Override
    public List<Map<String, Object>> getSalesRanking(String startDate, String endDate, Integer limit) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        return serviceOrderMapper.selectSalesRanking(start, end, limit != null ? limit : 10);
    }

    @Override
    public List<Map<String, Object>> getPopularSkills(String startDate, String endDate, Integer limit) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        return serviceOrderMapper.selectPopularSkills(start, end, limit != null ? limit : 10);
    }

    @Override
    public Map<String, Object> getIncomeStats(Long sellerId, String startDate, String endDate) {
        Long targetSellerId = sellerId != null ? sellerId : LoginHelper.getUserId();
        if (targetSellerId == null) {
            throw new ServiceException("卖家ID不能为空");
        }

        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        return serviceOrderMapper.selectIncomeStats(targetSellerId, start, end);
    }

    @Override
    public Map<String, Object> getExpenseStats(Long buyerId, String startDate, String endDate) {
        Long targetBuyerId = buyerId != null ? buyerId : LoginHelper.getUserId();
        if (targetBuyerId == null) {
            throw new ServiceException("买家ID不能为空");
        }

        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        return serviceOrderMapper.selectExpenseStats(targetBuyerId, start, end);
    }

    @Override
    public Map<String, Object> getPlatformTradeStats(String startDate, String endDate) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        return serviceOrderMapper.selectPlatformTradeStats(start, end);
    }

    @Override
    public boolean validateOrderPermission(Long orderId, Long userId, boolean requireBuyer, boolean requireSeller) {
        if (orderId == null || userId == null) {
            return false;
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        if (requireBuyer && requireSeller) {
            // 要求是买家或卖家
            return userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId());
        } else if (requireBuyer) {
            // 要求是买家
            return userId.equals(order.getBuyerId());
        } else if (requireSeller) {
            // 要求是卖家
            return userId.equals(order.getSellerId());
        } else {
            // 买家或卖家都可以
            return userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId());
        }
    }

    @Override
    public boolean canExecuteOperation(Long orderId, String operation) {
        if (orderId == null || StringUtils.isBlank(operation)) {
            return false;
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        switch (operation.toUpperCase()) {
            case "CANCEL":
                return order.canCancel();
            case "REFUND":
                return order.canRefund();
            case "COMPLETE":
                return order.canComplete();
            case "PAY":
                return order.isPendingPayment();
            case "START_SERVICE":
                return order.isPaid();
            default:
                return false;
        }
    }

    @Override
    public boolean recordOrderLog(Long orderId, String actionType, String actionDesc, Long operatorId, String remark) {
        // TODO: 实现订单日志记录功能
        // 这里可以将日志记录到数据库或日志文件
        log.info("订单日志 - 订单ID：{}，操作：{}，操作人：{}，备注：{}", 
                orderId, actionType, operatorId, remark);
        return true;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ServiceOrder> buildQueryWrapper(OrderQueryDTO query) {
        return Wrappers.lambdaQuery(ServiceOrder.class)
                .eq(query.getBuyerId() != null, ServiceOrder::getBuyerId, query.getBuyerId())
                .eq(query.getSellerId() != null, ServiceOrder::getSellerId, query.getSellerId())
                .eq(query.getContentId() != null, ServiceOrder::getContentId, query.getContentId())
                .eq(query.getStatus() != null, ServiceOrder::getStatus, query.getStatus())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        ServiceOrder::getCreatedAt, query.getBeginTime(), query.getEndTime());
    }

    /**
     * 构建订单数据
     */
    private Map<String, Object> buildOrderData(OrderCreateDTO dto) {
        Map<String, Object> data = new HashMap<>();
        
        if (StringUtils.isNotBlank(dto.getServiceDescription())) {
            data.put("service_description", dto.getServiceDescription());
        }
        if (StringUtils.isNotBlank(dto.getServiceRequirements())) {
            data.put("service_requirements", dto.getServiceRequirements());
        }
        if (StringUtils.isNotBlank(dto.getExpectedStartTime())) {
            data.put("expected_start_time", dto.getExpectedStartTime());
        }
        if (StringUtils.isNotBlank(dto.getExpectedEndTime())) {
            data.put("expected_end_time", dto.getExpectedEndTime());
        }
        if (StringUtils.isNotBlank(dto.getContactInfo())) {
            data.put("contact_info", dto.getContactInfo());
        }
        if (StringUtils.isNotBlank(dto.getSpecialRequest())) {
            data.put("special_request", dto.getSpecialRequest());
        }
        
        if (dto.getExtraData() != null) {
            data.putAll(dto.getExtraData());
        }
        
        return data;
    }

    /**
     * 合并订单数据
     */
    private Map<String, Object> mergeOrderData(Map<String, Object> existData, OrderUpdateDTO dto) {
        Map<String, Object> data = existData != null ? new HashMap<>(existData) : new HashMap<>();
        
        if (StringUtils.isNotBlank(dto.getServiceDescription())) {
            data.put("service_description", dto.getServiceDescription());
        }
        if (StringUtils.isNotBlank(dto.getServiceRequirements())) {
            data.put("service_requirements", dto.getServiceRequirements());
        }
        if (StringUtils.isNotBlank(dto.getExpectedStartTime())) {
            data.put("expected_start_time", dto.getExpectedStartTime());
        }
        if (StringUtils.isNotBlank(dto.getExpectedEndTime())) {
            data.put("expected_end_time", dto.getExpectedEndTime());
        }
        if (StringUtils.isNotBlank(dto.getContactInfo())) {
            data.put("contact_info", dto.getContactInfo());
        }
        if (StringUtils.isNotBlank(dto.getSpecialRequest())) {
            data.put("special_request", dto.getSpecialRequest());
        }
        
        if (dto.getExtraData() != null) {
            data.putAll(dto.getExtraData());
        }
        
        return data;
    }

    /**
     * 转换为列表VO
     */
    private List<OrderListVO> convertToListVOs(List<ServiceOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }

        return orders.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表VO
     */
    private OrderListVO convertToListVO(ServiceOrder order) {
        return OrderListVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .contentId(order.getContentId())
                .amount(order.getAmountYuan())
                .formattedAmount(order.getFormattedAmount())
                .duration(order.getDuration())
                .status(order.getStatus())
                .statusDesc(order.getStatusDesc())
                .serviceDescription(order.getServiceDescription())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .canCancel(order.canCancel())
                .canRefund(order.canRefund())
                .canComplete(order.canComplete())
                .canReview(order.isCompleted()) // 完成后可以评价
                .build();
    }

    /**
     * 转换为详情VO
     */
    private OrderDetailVO convertToDetailVO(ServiceOrder order) {
        Map<String, Object> data = order.getData();
        
        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .contentId(order.getContentId())
                .amount(order.getAmountYuan())
                .formattedAmount(order.getFormattedAmount())
                .duration(order.getDuration())
                .status(order.getStatus())
                .statusDesc(order.getStatusDesc())
                .serviceDescription(order.getServiceDescription())
                .serviceRequirements(order.getServiceRequirements())
                .expectedStartTime(getDataValue(data, "expected_start_time"))
                .expectedEndTime(getDataValue(data, "expected_end_time"))
                .contactInfo(getDataValue(data, "contact_info"))
                .specialRequirements(getDataValue(data, "special_request"))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .version(order.getVersion())
                .canCancel(order.canCancel())
                .canRefund(order.canRefund())
                .canComplete(order.canComplete())
                .canReview(order.isCompleted())
                .extraData(data)
                .build();
    }

    /**
     * 从数据中获取值
     */
    private String getDataValue(Map<String, Object> data, String key) {
        return data != null ? (String) data.get(key) : null;
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                log.warn("日期时间解析失败：{}", dateTimeStr);
                return null;
            }
        }
    }
}
