package org.dromara.common.location.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.dromara.common.location.domain.entity.City;

import java.io.Serial;
import java.io.Serializable;

/**
 * 城市选择业务对象
 * City Select BO
 *
 * @author XiangYuPai Team
 */
@Data
@AutoMapper(target = City.class, reverseConvertGenerate = false)
public class CitySelectBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 城市代码
     */
    @NotBlank(message = "城市代码不能为空")
    private String cityCode;

    /**
     * 用户ID (可选，用于记录用户的城市选择历史)
     */
    private Long userId;
}
