package org.dromara.content.service;

import org.dromara.content.domain.dto.ReportDTO;
import org.dromara.content.domain.vo.ReportVO;

/**
 * 举报服务接口
 *
 * @author XiangYuPai
 */
public interface IReportService {

    /**
     * 提交举报
     *
     * @param reportDTO 举报信息
     * @param userId    举报人ID
     * @return 举报结果
     */
    ReportVO submitReport(ReportDTO reportDTO, Long userId);

}
