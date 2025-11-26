package org.dromara.appbff.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 服务评价列表VO
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Data
@Schema(description = "服务评价列表")
public class ServiceReviewListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "服务ID")
    private Long serviceId;

    @Schema(description = "总评价数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    @Schema(description = "评价统计")
    private ServiceDetailVO.ReviewSummaryVO summary;

    @Schema(description = "评价标签")
    private List<ServiceDetailVO.ReviewTagVO> tags;

    @Schema(description = "评价列表")
    private List<ServiceDetailVO.ReviewItemVO> list;
}
