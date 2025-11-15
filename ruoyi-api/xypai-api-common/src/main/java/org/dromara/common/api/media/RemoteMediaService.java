package org.dromara.common.api.media;

import org.dromara.common.core.domain.R;

/**
 * 媒体服务远程调用接口
 * Remote Media Service Interface
 *
 * <p>用途：其他微服务通过Dubbo调用此接口使用媒体服务功能</p>
 * <p>实现：xypai-common模块实现此接口</p>
 *
 * @author XiangYuPai Team
 */
public interface RemoteMediaService {

    // ==================== 文件查询 ====================

    /**
     * 根据文件ID获取文件URL
     *
     * @param fileId 文件ID
     * @return 文件URL
     */
    R<String> getFileUrl(Long fileId);

    /**
     * 根据文件MD5查找文件（用于秒传）
     *
     * @param md5 文件MD5值
     * @return 文件URL（如果存在）
     */
    R<String> findFileByMd5(String md5);

    // ==================== 文件删除 ====================

    /**
     * 删除媒体文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID（权限校验）
     * @return 是否成功
     */
    R<Boolean> deleteFile(Long fileId, Long userId);

    /**
     * 批量删除媒体文件
     *
     * @param fileIds 文件ID列表
     * @param userId  用户ID（权限校验）
     * @return 是否成功
     */
    R<Boolean> batchDeleteFiles(Long[] fileIds, Long userId);

    // ==================== 文件验证 ====================

    /**
     * 验证文件是否属于指定用户
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return true=属于该用户，false=不属于
     */
    R<Boolean> verifyFileOwnership(Long fileId, Long userId);

    /**
     * 验证文件是否存在
     *
     * @param fileId 文件ID
     * @return true=存在，false=不存在
     */
    R<Boolean> fileExists(Long fileId);

    // ==================== 业务关联 ====================

    /**
     * 关联文件到业务对象
     *
     * @param fileId  文件ID
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 是否成功
     */
    R<Boolean> bindFileToBiz(Long fileId, String bizType, Long bizId);

    /**
     * 查询业务对象关联的文件
     *
     * @param bizType 业务类型
     * @param bizId   业务ID
     * @return 文件URL列表
     */
    R<String[]> getFilesByBiz(String bizType, Long bizId);
}
