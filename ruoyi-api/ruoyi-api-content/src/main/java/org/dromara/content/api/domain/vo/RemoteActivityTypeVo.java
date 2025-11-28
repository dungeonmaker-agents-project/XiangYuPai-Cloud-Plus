package org.dromara.content.api.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 活动类型VO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
