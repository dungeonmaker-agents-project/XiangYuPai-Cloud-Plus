package org.dromara.content.api.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 活动查询DTO (RPC)
 *
 * @author XiangYuPai
 */
@Data
public class RemoteActivityQueryDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 活动类型编码
     */
    private String typeCode;

    /**
     * 活动类型编码列表（多选）
     */
    private List<String> typeCodes;

    /**
     * 性别筛选: all, male, female
     */
    private String gender;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 排序方式: newest, start_time_asc, distance_asc
     */
    private String sortBy;

    /**
     * 用户经度（用于距离计算）
     */
    private Double longitude;

    /**
     * 用户纬度（用于距离计算）
     */
    private Double latitude;
}
