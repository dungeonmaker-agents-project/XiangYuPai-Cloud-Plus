package org.dromara.payment.service;

import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.dto.VerifyPasswordDTO;
import org.dromara.payment.domain.vo.PaymentMethodVO;
import org.dromara.payment.domain.vo.PaymentResultVO;

/**
 * 支付服务接口
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public interface IPaymentService {

    /**
     * 执行支付
     *
     * @param dto 支付请求
     * @return 支付结果
     */
    PaymentResultVO executePayment(ExecutePaymentDTO dto);

    /**
     * 验证支付密码
     *
     * @param dto 验证请求
     * @return 支付结果
     */
    PaymentResultVO verifyPassword(VerifyPasswordDTO dto);

    /**
     * 获取支付方式列表
     *
     * @return 支付方式列表
     */
    PaymentMethodVO getPaymentMethods();
}
