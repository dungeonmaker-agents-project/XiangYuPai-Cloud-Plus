package org.dromara.common.report.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.report.domain.entity.Report;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 举报记录视图对象
 * Report VO
 *
 * @author XiangYuPai Team
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Report.class)
public class ReportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 举报ID
     */
    @ExcelProperty(value = "举报ID")
    private Long id;

    /**
     * 举报用户ID
     */
    private Long reporterId;

    /**
     * 被举报用户ID
     */
    private Long reportedUserId;

    /**
     * 被举报内容类型
     */
    @ExcelProperty(value = "内容类型")
    private String contentType;

    /**
     * 被举报内容ID
     */
    private Long contentId;

    /**
     * 举报原因类型
     */
    @ExcelProperty(value = "举报原因")
    private String reasonType;

    /**
     * 举报详细说明
     */
    @ExcelProperty(value = "详细说明")
    private String reasonDetail;

    /**
     * 证据截图URLs
     */
    private String evidenceUrls;

    /**
     * 审核状态
     */
    @ExcelProperty(value = "审核状态")
    private Integer status;

    /**
     * 审核结果
     */
    @ExcelProperty(value = "审核结果")
    private Integer reviewResult;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date reviewedAt;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "举报时间")
    private Date createTime;
}
