package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.user.domain.entity.AppUser;

/**
 * App用户Mapper接口
 *
 * <p>继承 MyBatis Plus BaseMapper，无需编写 XML 文件</p>
 * <p>自动获得以下方法：</p>
 * <ul>
 *     <li>insert(entity) - 插入</li>
 *     <li>deleteById(id) - 根据ID删除</li>
 *     <li>updateById(entity) - 根据ID更新</li>
 *     <li>selectById(id) - 根据ID查询</li>
 *     <li>selectOne(wrapper) - 查询单个</li>
 *     <li>selectList(wrapper) - 查询列表</li>
 *     <li>selectCount(wrapper) - 统计数量</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
public interface AppUserMapper extends BaseMapper<AppUser> {

    // ========================================
    // 使用 LambdaQueryWrapper 进行查询，无需自定义方法
    // ========================================

    // 示例：查询手机号对应的用户
    // LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
    // wrapper.eq(AppUser::getMobile, "13800138000")
    //        .eq(AppUser::getCountryCode, "+86");
    // AppUser user = appUserMapper.selectOne(wrapper);

    // 示例：更新登录信息
    // LambdaUpdateWrapper<AppUser> wrapper = new LambdaUpdateWrapper<>();
    // wrapper.eq(AppUser::getUserId, userId)
    //        .set(AppUser::getLastLoginTime, LocalDateTime.now())
    //        .set(AppUser::getLastLoginIp, ip)
    //        .setSql("login_count = login_count + 1");
    // appUserMapper.update(null, wrapper);
}
