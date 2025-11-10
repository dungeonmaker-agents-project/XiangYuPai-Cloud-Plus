package com.xypai.user.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.user.domain.dto.TransactionQueryDTO;
import com.xypai.user.domain.dto.WalletRechargeDTO;
import com.xypai.user.domain.dto.WalletTransferDTO;
import com.xypai.user.domain.vo.TransactionVO;
import com.xypai.user.domain.vo.UserWalletVO;
import com.xypai.user.service.IUserWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户钱包控制�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "用户钱包", description = "用户钱包管理API")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Validated
public class UserWalletController extends BaseController {

    private final IUserWalletService userWalletService;

    /**
     * 获取用户钱包信息
     */
    @Operation(summary = "获取钱包信息", description = "获取当前用户的钱包详细信息")
    @GetMapping("/info")
    @SaCheckPermission("user:wallet:query")
    public R<UserWalletVO> getWalletInfo() {
        return R.ok(userWalletService.getUserWallet());
    }

    /**
     * 获取指定用户钱包信息
     */
    @Operation(summary = "获取指定用户钱包信息", description = "管理员查看指定用户钱包信息")
    @GetMapping("/{userId}")
    @SaCheckPermission("user:wallet:admin")
    public R<UserWalletVO> getUserWalletInfo(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return R.ok(userWalletService.getUserWalletByUserId(userId));
    }

    /**
     * 钱包充�?
     */
    @Operation(summary = "钱包充值", description = "用户钱包余额充值")
    @PostMapping("/recharge")
    @SaCheckPermission("user:wallet:recharge")
    @Log(title = "钱包充值", businessType = BusinessType.INSERT)
    public R<String> recharge(@Validated @RequestBody WalletRechargeDTO rechargeDTO) {
        return R.ok("充值订单创建成功", userWalletService.createRechargeOrder(rechargeDTO));
    }

    /**
     * 钱包转账
     */
    @Operation(summary = "钱包转账", description = "向其他用户转账")
    @PostMapping("/transfer")
    @SaCheckPermission("user:wallet:transfer")
    @Log(title = "钱包转账", businessType = BusinessType.INSERT)
    public R<Void> transfer(@Validated @RequestBody WalletTransferDTO transferDTO) {
        boolean result = userWalletService.transferMoney(transferDTO);
        return result ? R.ok("转账成功") : R.fail("转账失败");
    }

    /**
     * 获取交易记录
     */
    @Operation(summary = "获取交易记录", description = "分页查询用户交易流水")
    @GetMapping("/transactions")
    @SaCheckPermission("user:wallet:query")
    public TableDataInfo<TransactionVO> getTransactions(TransactionQueryDTO query, PageQuery pageQuery) {
        return userWalletService.getUserTransactions(query, pageQuery);
    }

    /**
     * 获取交易详情
     */
    @Operation(summary = "获取交易详情", description = "根据交易ID获取详细信息")
    @GetMapping("/transactions/{transactionId}")
    @SaCheckPermission("user:wallet:query")
    public R<TransactionVO> getTransactionDetail(
            @Parameter(description = "交易ID", required = true)
            @PathVariable Long transactionId) {
        return R.ok(userWalletService.getTransactionById(transactionId));
    }

    /**
     * 获取钱包统计信息
     */
    @Operation(summary = "获取钱包统计", description = "获取用户钱包统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("user:wallet:query")
    public R<Map<String, Object>> getWalletStatistics(
            @Parameter(description = "统计开始时间")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "统计结束时间")
            @RequestParam(required = false) String endDate) {
        return R.ok(userWalletService.getWalletStatistics(startDate, endDate));
    }

    /**
     * 冻结钱包
     */
    @Operation(summary = "冻结钱包", description = "管理员冻结用户钱包")
    @PutMapping("/{userId}/freeze")
    @SaCheckPermission("user:wallet:admin")
    @Log(title = "冻结钱包", businessType = BusinessType.UPDATE)
    public R<Void> freezeWallet(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "冻结原因")
            @RequestParam(required = false) String reason) {
        boolean result = userWalletService.freezeWallet(userId, reason);
        return result ? R.ok("冻结成功") : R.fail("冻结失败");
    }

    /**
     * 解冻钱包
     */
    @Operation(summary = "解冻钱包", description = "管理员解冻用户钱包")
    @PutMapping("/{userId}/unfreeze")
    @SaCheckPermission("user:wallet:admin")
    @Log(title = "解冻钱包", businessType = BusinessType.UPDATE)
    public R<Void> unfreezeWallet(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = userWalletService.unfreezeWallet(userId);
        return result ? R.ok("解冻成功") : R.fail("解冻失败");
    }
}
