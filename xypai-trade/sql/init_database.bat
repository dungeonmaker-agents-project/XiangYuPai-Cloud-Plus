@echo off
chcp 65001 >nul
echo ==========================================
echo XY相遇派 - xypai_trade 数据库初始化
echo ==========================================
echo.

set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_USER=root
set MYSQL_PASSWORD=root

echo 📌 MySQL连接信息:
echo    主机: %MYSQL_HOST%:%MYSQL_PORT%
echo    用户: %MYSQL_USER%
echo.

echo ▶ 步骤 1/5: 创建数据库...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% < 00_create_database.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 数据库创建失败！请检查MySQL连接信息
    pause
    exit /b 1
)
echo ✅ 数据库创建成功
echo.

echo ▶ 步骤 2/5: 创建订单表...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% xypai_trade < v7.1_service_order_upgrade.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 订单表创建失败！
    pause
    exit /b 1
)
echo ✅ 订单表创建成功
echo.

echo ▶ 步骤 3/5: 创建评价表...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% xypai_trade < v7.1_service_review_create.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 评价表创建失败！
    pause
    exit /b 1
)
echo ✅ 评价表创建成功
echo.

echo ▶ 步骤 4/5: 创建钱包表...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% xypai_trade < v7.1_user_wallet_create.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 钱包表创建失败！
    pause
    exit /b 1
)
echo ✅ 钱包表创建成功
echo.

echo ▶ 步骤 5/6: 创建交易流水表...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% xypai_trade < v7.1_transaction_create.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 交易流水表创建失败！
    pause
    exit /b 1
)
echo ✅ 交易流水表创建成功
echo.

echo ▶ 步骤 6/6: 创建服务统计表...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% xypai_trade < v7.1_service_stats_create.sql
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 服务统计表创建失败！
    pause
    exit /b 1
)
echo ✅ 服务统计表创建成功
echo.

echo ==========================================
echo ✅ xypai_trade 数据库初始化完成！
echo ==========================================
echo.
echo 已创建表:
echo   1. service_order   - 订单表（32字段）
echo   2. service_review  - 评价表（18字段）
echo   3. user_wallet     - 钱包表（乐观锁，9字段）
echo   4. transaction     - 交易流水（13字段）
echo   5. service_stats   - 服务统计（9字段）
echo.
echo 现在可以启动 xypai-trade 服务了！
echo ==========================================
echo.
pause

