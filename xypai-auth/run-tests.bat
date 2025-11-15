@echo off
REM ==============================================================================
REM XyPai Auth Module - Test Execution Script
REM ==============================================================================
REM This script helps you run backend tests for the xypai-auth module
REM
REM Prerequisites:
REM   - Maven 3.6+ installed and in PATH
REM   - MySQL, Redis, Nacos running
REM   - xypai-user service running (for RPC calls)
REM   - xypai-auth service running
REM
REM Usage:
REM   run-tests.bat [option]
REM
REM Options:
REM   all          - Run all tests (default)
REM   page         - Run page-based tests only
REM   api          - Run API tests only
REM   flow         - Run flow tests only
REM   password     - Run password login tests
REM   sms          - Run SMS login tests (most critical)
REM   payment      - Run payment password tests
REM   forgot       - Run forgot password tests
REM   newuser      - Run new user registration flow
REM   existing     - Run existing user login flow
REM   reset        - Run password reset flow
REM ==============================================================================

setlocal enabledelayedexpansion

REM Change to xypai-auth directory
cd /d "%~dp0"

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven not found in PATH!
    echo.
    echo Please install Maven or add it to your PATH:
    echo   1. Download from: https://maven.apache.org/download.cgi
    echo   2. Extract to: C:\Program Files\Apache\Maven
    echo   3. Add to PATH: C:\Program Files\Apache\Maven\bin
    echo.
    echo Alternatively, run tests from IntelliJ IDEA or Eclipse.
    echo See TEST_EXECUTION_STATUS.md for details.
    echo.
    pause
    exit /b 1
)

echo ==============================================================================
echo XyPai Auth Module - Test Execution
echo ==============================================================================
echo.

REM Get test option (default: all)
set TEST_OPTION=%1
if "%TEST_OPTION%"=="" set TEST_OPTION=all

echo Running tests: %TEST_OPTION%
echo.

REM Execute based on option
if "%TEST_OPTION%"=="all" (
    echo [INFO] Running ALL tests...
    mvn clean test
    goto :end
)

if "%TEST_OPTION%"=="page" (
    echo [INFO] Running PAGE tests only...
    mvn test -Dtest="org.dromara.auth.test.page.*"
    goto :end
)

if "%TEST_OPTION%"=="api" (
    echo [INFO] Running API tests only...
    mvn test -Dtest="org.dromara.auth.test.api.*"
    goto :end
)

if "%TEST_OPTION%"=="flow" (
    echo [INFO] Running FLOW tests only...
    mvn test -Dtest="org.dromara.auth.test.flow.*"
    goto :end
)

if "%TEST_OPTION%"=="password" (
    echo [INFO] Running Password Login tests...
    mvn test -Dtest=Page01_PasswordLoginTest
    goto :end
)

if "%TEST_OPTION%"=="sms" (
    echo [INFO] Running SMS Login tests ^(CRITICAL^)...
    mvn test -Dtest=Page02_SmsLoginTest
    goto :end
)

if "%TEST_OPTION%"=="payment" (
    echo [INFO] Running Payment Password tests...
    mvn test -Dtest=Page04_PaymentPasswordTest
    goto :end
)

if "%TEST_OPTION%"=="forgot" (
    echo [INFO] Running Forgot Password tests...
    mvn test -Dtest=Page03_ForgotPasswordFlowTest
    goto :end
)

if "%TEST_OPTION%"=="newuser" (
    echo [INFO] Running New User Registration Flow...
    mvn test -Dtest=NewUserRegistrationFlowTest
    goto :end
)

if "%TEST_OPTION%"=="existing" (
    echo [INFO] Running Existing User Login Flow...
    mvn test -Dtest=ExistingUserLoginFlowTest
    goto :end
)

if "%TEST_OPTION%"=="reset" (
    echo [INFO] Running Password Reset Flow...
    mvn test -Dtest=PasswordResetFlowTest
    goto :end
)

REM Unknown option
echo [ERROR] Unknown test option: %TEST_OPTION%
echo.
echo Valid options:
echo   all, page, api, flow, password, sms, payment, forgot, newuser, existing, reset
echo.
echo Example: run-tests.bat sms
echo.
pause
exit /b 1

:end
echo.
echo ==============================================================================
echo Test execution completed!
echo ==============================================================================
echo.
echo View detailed results in:
echo   - Console output above
echo   - target/surefire-reports/
echo.
echo For debugging, check logs:
echo   - logs/sys-info.log
echo.
pause
