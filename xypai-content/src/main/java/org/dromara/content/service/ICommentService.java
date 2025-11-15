package org.dromara.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.content.domain.dto.CommentListQueryDTO;
import org.dromara.content.domain.dto.CommentPublishDTO;
import org.dromara.content.domain.vo.CommentListVO;

/**
 * 评论服务接口
 *
 * @author XiangYuPai
 */
public interface ICommentService {

    /**
     * 获取评论列表
     *
     * @param queryDTO 查询参数
     * @param userId 当前用户ID
     * @return 评论列表
     */
    Page<CommentListVO> getCommentList(CommentListQueryDTO queryDTO, Long userId);

    /**
     * 发布评论
     *
     * @param publishDTO 发布参数
     * @param userId 用户ID
     * @return 评论详情
     */
    CommentListVO publishComment(CommentPublishDTO publishDTO, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long commentId, Long userId);

}
