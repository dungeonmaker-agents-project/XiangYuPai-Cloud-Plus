package org.dromara.appbff.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 首页 Feed 流查询 DTO
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@Data
public class HomeFeedQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Feed 类型: online-线上, offline-线下
     */
    private String type;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 区域代码
     */
    private String districtCode;

    /**
     * 当前用户经度
     */
    private Double longitude;

    /**
     * 当前用户纬度
     */
    private Double latitude;

}
