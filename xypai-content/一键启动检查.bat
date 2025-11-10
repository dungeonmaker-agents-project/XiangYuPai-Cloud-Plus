@echo off
chcp 65001 >nul
echo ================================================
echo 🚀 xypai-content 启动前检查
echo ================================================
echo.

echo 📋 检查 1: Docker 容器状态
echo ------------------------------------------------
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr /i "mysql redis nacos"

if %errorlevel% neq 0 (
    echo ❌ Docker 容器未运行！
    echo.
    echo 💡 请先启动 Docker 容器：
    echo    cd script\docker
    echo    docker-compose up -d mysql redis nacos
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ Docker 容器运行正常
echo.

echo 📋 检查 2: Redis 连接测试
echo ------------------------------------------------
docker exec redis redis-cli -a ruoyi123 ping

if %errorlevel% neq 0 (
    echo ❌ Redis 连接失败！
    pause
    exit /b 1
)

echo ✅ Redis 连接正常
echo.

echo 📋 检查 3: MySQL 连接测试
echo ------------------------------------------------
docker exec mysql mysql -uroot -pruoyi123 -e "SELECT 1" >nul 2>&1

if %errorlevel% neq 0 (
    echo ❌ MySQL 连接失败！
    pause
    exit /b 1
)

echo ✅ MySQL 连接正常
echo.

echo 📋 检查 4: 数据库表检查
echo ------------------------------------------------
docker exec mysql mysql -uroot -pruoyi123 -e "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema='xypai_content';"

echo.

echo 📋 检查 5: Nacos 服务检查
echo ------------------------------------------------
curl -s http://localhost:8848/nacos/v1/console/health >nul 2>&1

if %errorlevel% neq 0 (
    echo ❌ Nacos 服务未启动！
    echo 💡 请确保 Nacos 已启动并可访问：http://localhost:8848/nacos
    pause
    exit /b 1
)

echo ✅ Nacos 服务正常
echo.

echo ================================================
echo ✅ 所有检查通过！
echo ================================================
echo.
echo 📝 配置清单：
echo   ✅ MySQL 密码：ruoyi123
echo   ✅ Redis 密码：ruoyi123
echo   ✅ Redis database：3
echo   ✅ 使用 RedissonClient（不是 RedisTemplate）
echo.
echo 🚀 下一步：
echo   1. 打开 IDE
echo   2. 运行 XyPaiContentApplication
echo   3. 查看启动日志
echo.
echo 📚 参考文档：
echo   - ALL_FIXES_SUMMARY.md（总结）
echo   - REDISTEMPLATE_TO_REDISSON_FIX.md（RedisTemplate修复）
echo   - STARTUP_CHECKLIST.md（启动清单）
echo.
pause

