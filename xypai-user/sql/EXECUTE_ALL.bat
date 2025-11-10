@echo off
REM ==========================================
REM xypai-user 数据库一键部署脚本
REM ==========================================
REM 负责人: Bob
REM 日期: 2025-10-20
REM 说明: Windows批处理脚本，自动执行所有SQL
REM ==========================================

echo.
echo ========================================
echo   xypai-user 数据库一键部署工具
echo ========================================
echo.

REM 设置MySQL连接参数
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root

REM 提示输入密码
echo 请输入MySQL密码:
set /p MYSQL_PASSWORD=

echo.
echo ----------------------------------------
echo 开始执行数据库部署...
echo ----------------------------------------
echo.

REM 1. 创建数据库
echo [1/5] 创建数据库 xypai_user...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < 01_create_database.sql
if errorlevel 1 (
    echo ❌ 创建数据库失败！
    pause
    exit /b 1
)
echo ✅ 数据库创建成功
echo.

REM 2. 创建表
echo [2/5] 创建8张表...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < 02_create_tables.sql
if errorlevel 1 (
    echo ❌ 创建表失败！
    pause
    exit /b 1
)
echo ✅ 表创建成功
echo.

REM 3. 创建索引
echo [3/5] 创建索引...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < 03_create_indexes.sql
if errorlevel 1 (
    echo ❌ 创建索引失败！
    pause
    exit /b 1
)
echo ✅ 索引创建成功
echo.

REM 4. 初始化测试数据
echo [4/5] 导入测试数据...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < 04_init_test_data.sql
if errorlevel 1 (
    echo ❌ 导入数据失败！
    pause
    exit /b 1
)
echo ✅ 测试数据导入成功
echo.

REM 5. 验证数据库
echo [5/5] 验证数据库...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASSWORD% < 99_verify.sql
if errorlevel 1 (
    echo ❌ 验证失败！
    pause
    exit /b 1
)
echo ✅ 数据库验证通过
echo.

echo ========================================
echo   ✅ 数据库部署完成！
echo ========================================
echo.
echo 数据库名称: xypai_user
echo 表数量: 8张
echo 测试用户: 10个
echo 职业类型: 20种
echo.
echo 下一步：
echo 1. 修改 Nacos 配置中的数据库密码
echo 2. 启动 xypai-user 服务
echo 3. 访问 http://localhost:9401/doc.html 查看API文档
echo.
pause

