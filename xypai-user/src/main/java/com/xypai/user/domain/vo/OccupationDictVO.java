package com.xypai.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职业字典VO
 *
 * @author Bob
 * @date 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupationDictVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 职业编码
     */
    private String code;

    /**
     * 职业名称
     */
    private String name;

    /**
     * 职业分类
     */
    private String category;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 是否有图标
     */
    private Boolean hasIcon;
}

