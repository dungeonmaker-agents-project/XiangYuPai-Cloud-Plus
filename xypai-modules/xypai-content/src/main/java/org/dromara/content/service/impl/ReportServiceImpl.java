package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.content.domain.dto.ReportDTO;
import org.dromara.content.domain.entity.Report;
import org.dromara.content.domain.vo.ReportTypeVO;
import org.dromara.content.domain.vo.ReportVO;
import org.dromara.content.mapper.ReportMapper;
import org.dromara.content.service.IReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 举报服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    /**
     * 举报类型列表（按文档要求的顺序）
     */
    private static final List<ReportTypeVO> REPORT_TYPES = Arrays.asList(
        ReportTypeVO.builder().key("insult").label("辱骂引战").build(),
        ReportTypeVO.builder().key("porn").label("色情低俗").build(),
        ReportTypeVO.builder().key("fraud").label("诈骗").build(),
        ReportTypeVO.builder().key("illegal").label("违法犯罪").build(),
        ReportTypeVO.builder().key("fake").label("不实信息").build(),
        ReportTypeVO.builder().key("minor").label("未成年人相关").build(),
        ReportTypeVO.builder().key("uncomfortable").label("内容引人不适").build(),
        ReportTypeVO.builder().key("other").label("其他").build()
    );

    @Override
    public List<ReportTypeVO> getReportTypes() {
        return REPORT_TYPES;
    }

    @Override
    public ReportVO submitReport(ReportDTO reportDTO, Long userId) {
        // 1. 检查重复举报(24小时内同一用户对同一目标只能举报1次)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        Long count = reportMapper.selectCount(
            new LambdaQueryWrapper<Report>()
                .eq(Report::getUserId, userId)
                .eq(Report::getTargetType, reportDTO.getTargetType())
                .eq(Report::getTargetId, reportDTO.getTargetId())
                .ge(Report::getCreatedAt, oneDayAgo)
        );

        if (count > 0) {
            throw new ServiceException("24小时内已举报过该内容,请勿重复举报");
        }

        // 2. 转换举报图片列表为JSON字符串
        String evidenceImagesJson = null;
        if (reportDTO.getEvidenceImages() != null && !reportDTO.getEvidenceImages().isEmpty()) {
            try {
                evidenceImagesJson = objectMapper.writeValueAsString(reportDTO.getEvidenceImages());
            } catch (JsonProcessingException e) {
                log.error("转换举报图片列表失败", e);
                throw new ServiceException("举报图片格式错误");
            }
        }

        // 3. 创建举报记录
        Report report = Report.builder()
            .userId(userId)
            .targetType(reportDTO.getTargetType())
            .targetId(reportDTO.getTargetId())
            .reasonType(reportDTO.getReasonType())
            .description(reportDTO.getDescription())
            .evidenceImages(evidenceImagesJson)
            .status("pending")
            .build();

        reportMapper.insert(report);

        log.info("用户{}提交举报,目标类型:{},目标ID:{},举报类型:{}",
            userId, reportDTO.getTargetType(), reportDTO.getTargetId(), reportDTO.getReasonType());

        // 4. 返回举报结果
        return ReportVO.builder()
            .reportId(report.getId())
            .status(report.getStatus())
            .createdAt(report.getCreatedAt())
            .build();
    }

}
