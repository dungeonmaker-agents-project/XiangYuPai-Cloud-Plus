package org.dromara.common.dubbo;

import org.dromara.common.location.dubbo.RemoteLocationServiceImplTest;
import org.dromara.common.media.dubbo.RemoteMediaServiceImplTest;
import org.dromara.common.notification.dubbo.RemoteNotificationServiceImplTest;
import org.dromara.common.report.dubbo.RemoteReportServiceImplTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Dubbo RPC服务单元测试套件
 * Dubbo RPC Service Unit Test Suite
 *
 * <p>运行所有Dubbo实现类的单元测试</p>
 *
 * @author XiangYuPai Team
 */
@Suite
@SuiteDisplayName("Dubbo RPC Services Test Suite")
@SelectClasses({
    RemoteLocationServiceImplTest.class,
    RemoteMediaServiceImplTest.class,
    RemoteNotificationServiceImplTest.class,
    RemoteReportServiceImplTest.class
})
public class DubboServiceTestSuite {
    // 测试套件入口类
    // 运行此类将执行所有Dubbo服务的单元测试
}
