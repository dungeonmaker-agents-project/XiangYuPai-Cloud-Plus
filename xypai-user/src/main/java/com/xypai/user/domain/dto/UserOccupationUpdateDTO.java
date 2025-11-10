package com.xypai.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户职业更新DTO
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOccupationUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职业编码列表（最多5个）
     */
    @NotNull(message = "职业列表不能为空")
    @Size(max = 5, message = "最多只能选择5个职业")
    private List<String> occupationCodes;

    /**
     * 是否保留原有排序
     * true: 保留原有排序，只更新职业列表
     * false: 按提交顺序重新排序
     */
    @Builder.Default
    private Boolean keepSortOrder = false;
}

