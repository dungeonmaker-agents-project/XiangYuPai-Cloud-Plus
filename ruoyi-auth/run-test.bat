@echo off
chcp 65001 >nul
REM ==========================================
REM SimpleSaTokenTest 快速启动脚本
REM ==========================================

echo.
echo ==========================================
echo   SimpleSaTokenTest 快速启动
echo ==========================================
echo.

REM 步骤1: 检查Redis
echo [步骤1/3] 检查Redis状态...
redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis 运行中
) else (
    echo ❌ Redis 未运行
    echo.
    echo 请先启动Redis:
    echo   redis-server
    echo.
    pause
    exit /b 1
)

echo.

REM 步骤2: 检查测试用户
echo [步骤2/3] 检查测试用户...
echo 提示: 如果用户不存在，请执行:
echo   mysql -u root -p ry-cloud ^< src\test\resources\test-data\app-test-user.sql
echo.

REM 步骤3: 运行测试
echo [步骤3/3] 运行测试...
echo.
echo ==========================================
echo   开始执行测试...
echo ==========================================
echo.

mvn test -Dtest=SimpleSaTokenTest

echo.
echo ==========================================
echo   测试完成
echo ==========================================
echo.

pause

