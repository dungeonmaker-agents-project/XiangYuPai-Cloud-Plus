package org.dromara.appbff.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 首页 Feed 流查询 DTO
 *
 * @author XyPai Team
 * @date 2025-11-20
 * @updated 2025-11-29 添加 sortBy 参数支持多种排序方式
 */
@Data
@Schema(description = "首页用户推荐查询参数")
public class HomeFeedQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Feed 类型: online-线上, offline-线下
     */
    @Schema(description = "推荐类型: online-线上, offline-线下", example = "offline")
    private String type;

    /**
     * 排序方式:
     * - nearby: 附近用户（按距离由近到远，需要传入经纬度）
     * - top_rated: 评分最高（按评分由高到低）
     * - latest: 最新注册/上线（按登录时间由新到旧）
     */
    @Schema(description = "排序方式: nearby-附近, top_rated-评分最高, latest-最新", example = "nearby")
    private String sortBy;

    /**
     * 页码
     */
    @Schema(description = "页码（从1开始）", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 城市代码
     */
    @Schema(description = "城市代码（如：440300表示深圳）", example = "440300")
    private String cityCode;

    /**
     * 区域代码
     */
    @Schema(description = "区县代码", example = "440304")
    private String districtCode;

    /**
     * 当前用户经度（用于计算附近用户距离）
     */
    @Schema(description = "当前用户经度", example = "114.0579")
    private Double longitude;

    /**
     * 当前用户纬度（用于计算附近用户距离）
     */
    @Schema(description = "当前用户纬度", example = "22.5431")
    private Double latitude;
}
