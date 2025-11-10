@echo off
chcp 65001 >nul
echo ==========================================
echo 清理xypai-trade/sql中的临时SQL脚本
echo ==========================================
echo.
echo ⚠️ 注意：主数据库脚本已迁移到
echo    dev_workspace\team\frank\sql\
echo.
echo 即将删除的文件（7个）:
echo   ❌ v7.1_service_order_upgrade.sql
echo   ❌ v7.1_service_review_create.sql
echo   ❌ v7.1_user_wallet_create.sql
echo   ❌ v7.1_transaction_create.sql
echo   ❌ v7.1_service_stats_create.sql
echo   ❌ 00_init_trade_database.sql
echo   ❌ init_database.bat
echo.
echo 保留的文件（3个）:
echo   ✅ 00_create_database.sql (通用)
echo   ✅ README.md (使用文档)
echo   ✅ SQL_REVIEW_REPORT.md (审查报告)
echo.
echo ==========================================
set /p confirm="确认删除？(Y/N): "

if /i "%confirm%" NEQ "Y" (
    echo.
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo 开始删除...

del /f /q v7.1_service_order_upgrade.sql 2>nul
if exist v7.1_service_order_upgrade.sql (
    echo ❌ 删除失败: v7.1_service_order_upgrade.sql
) else (
    echo ✅ 已删除: v7.1_service_order_upgrade.sql
)

del /f /q v7.1_service_review_create.sql 2>nul
if exist v7.1_service_review_create.sql (
    echo ❌ 删除失败: v7.1_service_review_create.sql
) else (
    echo ✅ 已删除: v7.1_service_review_create.sql
)

del /f /q v7.1_user_wallet_create.sql 2>nul
if exist v7.1_user_wallet_create.sql (
    echo ❌ 删除失败: v7.1_user_wallet_create.sql
) else (
    echo ✅ 已删除: v7.1_user_wallet_create.sql
)

del /f /q v7.1_transaction_create.sql 2>nul
if exist v7.1_transaction_create.sql (
    echo ❌ 删除失败: v7.1_transaction_create.sql
) else (
    echo ✅ 已删除: v7.1_transaction_create.sql
)

del /f /q v7.1_service_stats_create.sql 2>nul
if exist v7.1_service_stats_create.sql (
    echo ❌ 删除失败: v7.1_service_stats_create.sql
) else (
    echo ✅ 已删除: v7.1_service_stats_create.sql
)

del /f /q 00_init_trade_database.sql 2>nul
if exist 00_init_trade_database.sql (
    echo ❌ 删除失败: 00_init_trade_database.sql
) else (
    echo ✅ 已删除: 00_init_trade_database.sql
)

del /f /q init_database.bat 2>nul
if exist init_database.bat (
    echo ❌ 删除失败: init_database.bat
) else (
    echo ✅ 已删除: init_database.bat
)

echo.
echo ==========================================
echo ✅ 临时SQL脚本清理完成！
echo ==========================================
echo.
echo 当前目录保留文件:
dir /b *.sql *.md *.bat
echo.
echo 应该只剩下:
echo   ✅ 00_create_database.sql
echo   ✅ README.md
echo   ✅ SQL_REVIEW_REPORT.md
echo   ✅ MIGRATION_TO_DEV_WORKSPACE.md
echo   ✅ cleanup_old_scripts.bat (本脚本)
echo.
echo 主数据库脚本位置:
echo   📂 dev_workspace\team\frank\sql\
echo      ├── 02_create_tables.sql
echo      ├── 03_create_indexes.sql
echo      ├── 04_init_test_data.sql
echo      └── init_frank_database.bat
echo.
echo ==========================================
pause

