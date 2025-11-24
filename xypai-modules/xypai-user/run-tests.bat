@echo off
REM =============================================
REM XiangYuPai User Module - Test Execution Script
REM 测试执行脚本 (Windows)
REM =============================================

echo.
echo ========================================
echo XiangYuPai User Module Test Suite
echo ========================================
echo.

REM 检查Maven是否安装
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found! Please install Maven first.
    exit /b 1
)

REM 显示菜单
echo Select test option:
echo.
echo 1. Run ALL tests
echo 2. Run Controller tests only
echo 3. Run Service tests only
echo 4. Run Integration tests only
echo 5. Run specific test class
echo 6. Generate test report
echo 7. Clean and test
echo 8. Exit
echo.

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" goto run_all
if "%choice%"=="2" goto run_controller
if "%choice%"=="3" goto run_service
if "%choice%"=="4" goto run_integration
if "%choice%"=="5" goto run_specific
if "%choice%"=="6" goto generate_report
if "%choice%"=="7" goto clean_test
if "%choice%"=="8" goto exit

echo Invalid choice!
goto end

:run_all
echo.
echo Running ALL tests...
echo ----------------------------------------
mvn test -pl xypai-user
goto end

:run_controller
echo.
echo Running Controller tests...
echo ----------------------------------------
mvn test -pl xypai-user -Dtest=*ControllerTest
goto end

:run_service
echo.
echo Running Service tests...
echo ----------------------------------------
mvn test -pl xypai-user -Dtest=*ServiceTest
goto end

:run_integration
echo.
echo Running Integration tests...
echo ----------------------------------------
mvn test -pl xypai-user -Dtest=*IntegrationTest
goto end

:run_specific
echo.
set /p testclass="Enter test class name (e.g., ProfileControllerTest): "
echo.
echo Running %testclass%...
echo ----------------------------------------
mvn test -pl xypai-user -Dtest=%testclass%
goto end

:generate_report
echo.
echo Generating test report...
echo ----------------------------------------
mvn test surefire-report:report -pl xypai-user
echo.
echo Report generated at: xypai-user\target\site\surefire-report.html
goto end

:clean_test
echo.
echo Cleaning and running tests...
echo ----------------------------------------
mvn clean test -pl xypai-user
goto end

:exit
echo.
echo Exiting...
exit /b 0

:end
echo.
echo ========================================
echo Test execution completed!
echo ========================================
echo.
pause
