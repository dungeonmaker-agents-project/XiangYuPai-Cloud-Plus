package org.dromara.appbff.service;

import org.dromara.appbff.domain.dto.SubmitOrderDTO;
import org.dromara.appbff.domain.vo.OrderConfirmPreviewVO;
import org.dromara.appbff.domain.vo.OrderSubmitResultVO;
import org.dromara.appbff.domain.vo.UserBalanceVO;

/**
 * 订单确认服务接口
 *
 * <p>对应前端: OrderConfirmPage 订单确认页</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
public interface OrderConfirmService {

    /**
     * 获取订单确认预览
     *
     * <p>场景：用户从服务详情页点击"下单"后进入订单确认页</p>
     *
     * @param serviceId 技能服务ID
     * @param quantity 初始数量
     * @param userId 当前用户ID
     * @return 订单确认预览信息
     */
    OrderConfirmPreviewVO getOrderConfirmPreview(Long serviceId, Integer quantity, Long userId);

    /**
     * 更新价格预览
     *
     * <p>场景：用户调整数量后实时更新价格</p>
     *
     * @param serviceId 技能服务ID
     * @param quantity 新数量
     * @return 更新后的价格预览
     */
    OrderConfirmPreviewVO.PricePreview updatePricePreview(Long serviceId, Integer quantity);

    /**
     * 提交订单并支付
     *
     * <p>场景：用户确认订单信息后输入支付密码提交</p>
     * <p>流程：验证密码 → 创建订单 → 扣款 → 返回结果</p>
     *
     * @param dto 提交订单请求
     * @param userId 当前用户ID
     * @return 订单提交结果
     */
    OrderSubmitResultVO submitOrder(SubmitOrderDTO dto, Long userId);

    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    UserBalanceVO getUserBalance(Long userId);
}
