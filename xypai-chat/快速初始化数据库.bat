@echo off
chcp 65001 >nul
echo ========================================
echo   XY相遇派 - Chat模块数据库初始化
echo ========================================
echo.

REM 设置MySQL连接参数（请根据实际情况修改）
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=root

echo 🔧 正在连接MySQL服务器...
echo 主机: %MYSQL_HOST%:%MYSQL_PORT%
echo 用户: %MYSQL_USER%
echo.

REM 检查MySQL是否可访问
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 无法连接到MySQL服务器
    echo 请检查:
    echo   1. MySQL服务是否已启动
    echo   2. 用户名密码是否正确
    echo   3. 修改本脚本中的连接参数
    pause
    exit /b 1
)

echo ✅ MySQL连接成功
echo.

REM 设置SQL文件目录
set SQL_DIR=eve_workspace\sql

echo 📂 SQL文件目录: %SQL_DIR%
echo.

REM 执行SQL脚本
echo ========================================
echo 步骤 1/5: 创建数据库
echo ========================================
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < "%SQL_DIR%\01_create_database.sql"
if %errorlevel% neq 0 (
    echo ❌ 创建数据库失败
    pause
    exit /b 1
)
echo ✅ 数据库创建成功
echo.

echo ========================================
echo 步骤 2/5: 创建数据表 (v7.0)
echo ========================================
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% xypai_chat < "%SQL_DIR%\02_create_tables_v7.0.sql"
if %errorlevel% neq 0 (
    echo ❌ 创建数据表失败
    pause
    exit /b 1
)
echo ✅ 数据表创建成功
echo.

echo ========================================
echo 步骤 3/5: 升级到 v7.1
echo ========================================
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% xypai_chat < "%SQL_DIR%\03_upgrade_to_v7.1.sql"
if %errorlevel% neq 0 (
    echo ❌ 升级失败
    pause
    exit /b 1
)
echo ✅ 升级到v7.1成功
echo.

echo ========================================
echo 步骤 4/5: 创建索引
echo ========================================
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% xypai_chat < "%SQL_DIR%\04_create_indexes.sql"
if %errorlevel% neq 0 (
    echo ❌ 创建索引失败
    pause
    exit /b 1
)
echo ✅ 索引创建成功
echo.

echo ========================================
echo 步骤 5/5: 初始化测试数据
echo ========================================
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% xypai_chat < "%SQL_DIR%\05_init_test_data.sql"
if %errorlevel% neq 0 (
    echo ⚠️  初始化测试数据失败（可选步骤）
) else (
    echo ✅ 测试数据初始化成功
)
echo.

echo ========================================
echo 🎉 数据库初始化完成！
echo ========================================
echo.
echo 数据库: xypai_chat
echo 字符集: utf8mb4_unicode_ci
echo.
echo 您现在可以启动 xypai-chat 服务了
echo.
pause



