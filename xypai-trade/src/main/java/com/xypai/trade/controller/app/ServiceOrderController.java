package com.xypai.trade.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.trade.domain.dto.OrderCreateDTO;
import com.xypai.trade.domain.dto.OrderQueryDTO;
import com.xypai.trade.domain.dto.OrderUpdateDTO;
import com.xypai.trade.domain.vo.OrderDetailVO;
import com.xypai.trade.domain.vo.OrderListVO;
import com.xypai.trade.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 服务订单控制器
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "服务订单", description = "服务订单管理API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class ServiceOrderController extends BaseController {

    private final IOrderService orderService;

    /**
     * 查询订单列表
     */
    @Operation(summary = "查询订单列表", description = "分页查询订单列表信息")
    @GetMapping("/list")
    @SaCheckPermission("trade:order:list")
    public TableDataInfo<OrderListVO> list(OrderQueryDTO query) {
        List<OrderListVO> list = orderService.selectOrderList(query);
        return TableDataInfo.build(list);
    }

    /**
     * 获取订单详细信息
     */
    @Operation(summary = "获取订单详细信息", description = "根据订单ID获取详细信息")
    @GetMapping("/{orderId}")
    @SaCheckPermission("trade:order:query")
    @Log(title = "订单管理", businessType = BusinessType.OTHER)
    public R<OrderDetailVO> getInfo(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        return R.ok(orderService.selectOrderById(orderId));
    }

    /**
     * 创建订单
     */
    @Operation(summary = "创建订单", description = "创建新的服务订单")
    @PostMapping
    @SaCheckPermission("trade:order:add")
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody OrderCreateDTO orderCreateDTO) {
        Long orderId = orderService.createOrder(orderCreateDTO);
        return orderId != null ? R.ok() : R.fail();
    }

    /**
     * 修改订单
     */
    @Operation(summary = "修改订单", description = "更新订单信息")
    @PutMapping
    @SaCheckPermission("trade:order:edit")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody OrderUpdateDTO orderUpdateDTO) {
        return orderService.updateOrder(orderUpdateDTO) ? R.ok() : R.fail();
    }

    /**
     * 删除订单
     */
    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    @DeleteMapping("/{orderIds}")
    @SaCheckPermission("trade:order:remove")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "订单ID数组", required = true)
            @PathVariable Long[] orderIds) {
        // 删除功能暂未实现
        return R.fail("删除功能暂未开放");
    }

    /**
     * 支付订单
     */
    @Operation(summary = "支付订单", description = "支付指定订单")
    @PostMapping("/{orderId}/pay")
    @SaCheckPermission("trade:order:pay")
    @Log(title = "订单支付", businessType = BusinessType.UPDATE)
    public R<String> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "支付方式")
            @RequestParam(defaultValue = "wallet") String paymentMethod) {
        // 支付功能需要通过PaymentService实现
        return R.fail("请使用专门的支付接口");
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单", description = "取消指定订单")
    @PutMapping("/{orderId}/cancel")
    @SaCheckPermission("trade:order:edit")
    @Log(title = "取消订单", businessType = BusinessType.UPDATE)
    public R<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "取消原因")
            @RequestParam(required = false) String reason) {
        return orderService.cancelOrder(orderId, reason) ? R.ok() : R.fail();
    }

    /**
     * 确认开始服务
     */
    @Operation(summary = "确认开始服务", description = "卖家确认开始提供服务")
    @PutMapping("/{orderId}/start-service")
    @SaCheckPermission("trade:order:edit")
    @Log(title = "开始服务", businessType = BusinessType.UPDATE)
    public R<Void> startService(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId) {
        return orderService.startService(orderId, null) ? R.ok() : R.fail();
    }

    /**
     * 确认完成服务
     */
    @Operation(summary = "确认完成服务", description = "买家确认服务完成")
    @PutMapping("/{orderId}/complete")
    @SaCheckPermission("trade:order:edit")
    @Log(title = "完成订单", businessType = BusinessType.UPDATE)
    public R<Void> completeOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "评价分数")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "评价内容")
            @RequestParam(required = false) String review) {
        String completionNote = review != null ? review : "订单完成";
        return orderService.completeOrder(orderId, completionNote) ? R.ok() : R.fail();
    }

    /**
     * 申请退款
     */
    @Operation(summary = "申请退款", description = "买家申请订单退款")
    @PostMapping("/{orderId}/refund")
    @SaCheckPermission("trade:order:refund")
    @Log(title = "申请退款", businessType = BusinessType.UPDATE)
    public R<Void> requestRefund(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "退款原因", required = true)
            @RequestParam String reason) {
        // 退款功能需要通过PaymentService实现
        return R.fail("请使用专门的退款接口");
    }

    /**
     * 处理退款
     */
    @Operation(summary = "处理退款", description = "管理员处理退款申请")
    @PutMapping("/{orderId}/handle-refund")
    @SaCheckPermission("trade:order:admin")
    @Log(title = "处理退款", businessType = BusinessType.UPDATE)
    public R<Void> handleRefund(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId,
            @Parameter(description = "是否同意退款", required = true)
            @RequestParam Boolean approved,
            @Parameter(description = "处理说明")
            @RequestParam(required = false) String note) {
        // 退款处理功能需要通过PaymentService实现
        return R.fail("请使用专门的退款处理接口");
    }

    /**
     * 获取我的购买订单
     */
    @Operation(summary = "获取我的购买订单", description = "获取当前用户作为买家的订单列表")
    @GetMapping("/my-purchases")
    @SaCheckPermission("trade:order:query")
    public TableDataInfo<OrderListVO> getMyPurchases(
            @Parameter(description = "订单状态")
            @RequestParam(required = false) Integer status) {
        List<OrderListVO> list = orderService.selectMyBuyOrders(status, null);
        return TableDataInfo.build(list);
    }

    /**
     * 获取我的销售订单
     */
    @Operation(summary = "获取我的销售订单", description = "获取当前用户作为卖家的订单列表")
    @GetMapping("/my-sales")
    @SaCheckPermission("trade:order:query")
    public TableDataInfo<OrderListVO> getMySales(
            @Parameter(description = "订单状态")
            @RequestParam(required = false) Integer status) {
        List<OrderListVO> list = orderService.selectMySellOrders(status, null);
        return TableDataInfo.build(list);
    }

    /**
     * 获取订单统计
     */
    @Operation(summary = "获取订单统计", description = "获取订单统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("trade:order:query")
    public R<Map<String, Object>> getOrderStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(orderService.getUserOrderStats(null));
    }

    /**
     * 获取订单状态统计
     */
    @Operation(summary = "获取订单状态统计", description = "获取各状态订单数量统计")
    @GetMapping("/status-statistics")
    @SaCheckPermission("trade:order:query")
    public R<Map<String, Object>> getOrderStatusStatistics() {
        // 状态统计功能暂未实现
        return R.ok(Map.of("message", "功能开发中"));
    }

    /**
     * 获取热门服务
     */
    @Operation(summary = "获取热门服务", description = "获取订单量最多的服务")
    @GetMapping("/popular-services")
    @SaCheckPermission("trade:order:query")
    public TableDataInfo<Map<String, Object>> getPopularServices(
            @Parameter(description = "数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> list = orderService.getPopularSkills(null, null, limit);
        return TableDataInfo.build(list);
    }

    /**
     * 批量处理订单
     */
    @Operation(summary = "批量处理订单", description = "批量更新订单状态")
    @PutMapping("/batch-update")
    @SaCheckPermission("trade:order:admin")
    @Log(title = "批量处理订单", businessType = BusinessType.UPDATE)
    public R<Void> batchUpdateOrders(
            @Parameter(description = "订单ID列表", required = true)
            @RequestBody List<Long> orderIds,
            @Parameter(description = "目标状态", required = true)
            @RequestParam Integer targetStatus,
            @Parameter(description = "处理说明")
            @RequestParam(required = false) String note) {
        int count = orderService.batchHandleTimeoutOrders(orderIds, targetStatus);
        return count > 0 ? R.ok() : R.fail();
    }

    /**
     * 导出订单数据
     */
    @Operation(summary = "导出订单数据", description = "导出订单列表到Excel")
    @PostMapping("/export")
    @SaCheckPermission("trade:order:export")
    @Log(title = "导出订单", businessType = BusinessType.EXPORT)
    public R<String> exportOrders(OrderQueryDTO query) {
        // 导出功能暂未实现
        return R.ok("导出功能开发中", "");
    }
}
