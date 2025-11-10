package com.xypai.trade.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.trade.domain.entity.UserWallet;
import com.xypai.trade.service.IWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 钱包控制器
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Tag(name = "用户钱包", description = "钱包管理API")
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Validated
public class WalletController extends BaseController {

    private final IWalletService walletService;

    /**
     * 获取我的钱包信息
     */
    @Operation(summary = "获取我的钱包信息", description = "查询当前用户的钱包余额、金币等信息")
    @GetMapping("/my-wallet")
    @SaCheckPermission("trade:wallet:query")
    public R<Map<String, Object>> getMyWallet() {
        Long userId = LoginHelper.getUserId();
        UserWallet wallet = walletService.getOrCreateWallet(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", wallet.getUserId());
        result.put("balance", wallet.getBalanceYuan());
        result.put("balanceFormatted", wallet.getFormattedBalance());
        result.put("frozen", wallet.getFrozenYuan());
        result.put("coinBalance", wallet.getCoinBalance());
        result.put("totalAssets", wallet.getTotalAssetsYuan());
        result.put("totalIncome", wallet.getTotalIncomeYuan());
        result.put("totalExpense", wallet.getTotalExpenseYuan());
        result.put("netProfit", wallet.getNetProfitYuan());
        
        return R.ok(result);
    }

    /**
     * 查询用户钱包
     */
    @Operation(summary = "查询用户钱包", description = "根据用户ID查询钱包信息")
    @GetMapping("/{userId}")
    @SaCheckPermission("trade:wallet:query")
    public R<UserWallet> getWallet(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        UserWallet wallet = walletService.getWallet(userId);
        return R.ok(wallet);
    }

    /**
     * 充值
     */
    @Operation(summary = "充值", description = "用户充值余额")
    @PostMapping("/recharge")
    @SaCheckPermission("trade:wallet:recharge")
    @Log(title = "钱包充值", businessType = BusinessType.UPDATE)
    public R<Void> recharge(
            @Parameter(description = "充值金额（元）", required = true)
            @RequestParam Double amount,
            @Parameter(description = "支付方式", required = true)
            @RequestParam String paymentMethod) {
        Long userId = LoginHelper.getUserId();
        Long amountFen = (long) (amount * 100);
        
        // TODO: 调用支付接口，获取支付结果后再充值
        
        boolean success = walletService.rechargeBalance(userId, amountFen, "recharge", null, 
                "充值" + amount + "元");
        
        return success ? R.ok("充值成功") : R.fail("充值失败");
    }

    /**
     * 提现
     */
    @Operation(summary = "提现", description = "用户提现到银行卡")
    @PostMapping("/withdraw")
    @SaCheckPermission("trade:wallet:withdraw")
    @Log(title = "钱包提现", businessType = BusinessType.UPDATE)
    public R<Void> withdraw(
            @Parameter(description = "提现金额（元）", required = true)
            @RequestParam Double amount,
            @Parameter(description = "提现方式")
            @RequestParam(defaultValue = "bankcard") String withdrawMethod) {
        Long userId = LoginHelper.getUserId();
        Long amountFen = (long) (amount * 100);
        
        // TODO: 实名认证校验、提现限额校验
        
        boolean success = walletService.deductBalance(userId, amountFen, "withdraw", null, 
                "提现" + amount + "元");
        
        return success ? R.ok("提现申请已提交") : R.fail("提现失败");
    }

    /**
     * 转账
     */
    @Operation(summary = "转账", description = "转账给其他用户")
    @PostMapping("/transfer")
    @SaCheckPermission("trade:wallet:transfer")
    @Log(title = "钱包转账", businessType = BusinessType.UPDATE)
    public R<Void> transfer(
            @Parameter(description = "转账目标用户ID", required = true)
            @RequestParam Long toUserId,
            @Parameter(description = "转账金额（元）", required = true)
            @RequestParam Double amount,
            @Parameter(description = "转账说明")
            @RequestParam(required = false) String description) {
        Long fromUserId = LoginHelper.getUserId();
        Long amountFen = (long) (amount * 100);
        
        boolean success = walletService.transfer(fromUserId, toUserId, amountFen, 
                description != null ? description : "转账");
        
        return success ? R.ok("转账成功") : R.fail("转账失败");
    }

    /**
     * 检查余额是否足够
     */
    @Operation(summary = "检查余额是否足够", description = "检查用户余额是否满足指定金额")
    @GetMapping("/check-balance")
    @SaCheckPermission("trade:wallet:query")
    public R<Map<String, Object>> checkBalance(
            @Parameter(description = "金额（元）", required = true)
            @RequestParam Double amount) {
        Long userId = LoginHelper.getUserId();
        Long amountFen = (long) (amount * 100);
        
        boolean enough = walletService.hasEnoughBalance(userId, amountFen);
        UserWallet wallet = walletService.getWallet(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasEnough", enough);
        result.put("currentBalance", wallet.getBalanceYuan());
        result.put("requiredAmount", amount);
        result.put("shortage", enough ? 0 : (amount - wallet.getBalanceYuan().doubleValue()));
        
        return R.ok(result);
    }

    /**
     * 查询钱包统计
     */
    @Operation(summary = "查询钱包统计", description = "查询平台钱包总览统计")
    @GetMapping("/statistics")
    @SaCheckPermission("trade:wallet:admin")
    public R<Map<String, Object>> getWalletStats() {
        Map<String, Object> stats = walletService.getWalletStats();
        return R.ok(stats);
    }
}

