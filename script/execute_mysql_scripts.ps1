# ==========================================
# MySQL Script Execution Script
# ==========================================
# Purpose: Execute all MySQL initialization scripts step by step
# MySQL Connection: root/1123
# ==========================================

param(
    [string]$MySQLPath = "mysql",
    [string]$Host = "localhost",
    [string]$Port = "3306",
    [string]$User = "root",
    [string]$Password = "1123",
    [switch]$SkipRuoYi = $false,
    [switch]$SkipXYPAI = $false,
    [switch]$DryRun = $false
)

# Set error handling
$ErrorActionPreference = "Stop"

# Colors for output
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success { Write-ColorOutput Green $args }
function Write-Error { Write-ColorOutput Red $args }
function Write-Info { Write-ColorOutput Cyan $args }
function Write-Warning { Write-ColorOutput Yellow $args }

# Test MySQL connection
function Test-MySQLConnection {
    Write-Info "Testing MySQL connection..."
    try {
        $testCmd = "& '$MySQLPath' -h $Host -P $Port -u $User -p$Password -e 'SELECT VERSION();' 2>&1"
        $result = Invoke-Expression $testCmd
        if ($LASTEXITCODE -eq 0 -or $result -match "version") {
            Write-Success "✅ MySQL connection successful!"
            return $true
        }
    } catch {
        Write-Warning "⚠️  MySQL command not found in PATH. Trying common locations..."
        # Try common MySQL paths
        $commonPaths = @(
            "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
            "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
            "C:\xampp\mysql\bin\mysql.exe",
            "C:\wamp\bin\mysql\mysql8.0.xx\bin\mysql.exe"
        )
        foreach ($path in $commonPaths) {
            if (Test-Path $path) {
                $script:MySQLPath = $path
                Write-Success "✅ Found MySQL at: $path"
                return $true
            }
        }
        Write-Error "❌ MySQL not found. Please install MySQL or add it to PATH."
        Write-Info "💡 If using Docker, you may need to use: docker exec -i <container> mysql -u root -p1123"
        return $false
    }
    return $false
}

# Execute SQL script
function Invoke-SQLScript {
    param(
        [string]$ScriptPath,
        [string]$Database = ""
    )
    
    if (-not (Test-Path $ScriptPath)) {
        Write-Error "❌ Script not found: $ScriptPath"
        return $false
    }
    
    Write-Info "📄 Executing: $ScriptPath"
    
    if ($DryRun) {
        Write-Warning "🔍 DRY RUN - Would execute: $ScriptPath"
        return $true
    }
    
    try {
        $scriptDir = Split-Path $ScriptPath -Parent
        $scriptFile = Split-Path $ScriptPath -Leaf
        
        # Build MySQL command
        $mysqlCmd = if ($Database) {
            "& '$MySQLPath' -h $Host -P $Port -u $User -p$Password $Database < `"$ScriptPath`""
        } else {
            "& '$MySQLPath' -h $Host -P $Port -u $User -p$Password < `"$ScriptPath`""
        }
        
        # Execute script
        $output = Invoke-Expression $mysqlCmd 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "✅ Success: $scriptFile"
            return $true
        } else {
            Write-Error "❌ Failed: $scriptFile"
            Write-Error $output
            return $false
        }
    } catch {
        Write-Error "❌ Error executing $ScriptPath`: $_"
        return $false
    }
}

# Get script directory
$ScriptRoot = Split-Path -Parent $PSScriptRoot
$SQLRoot = Join-Path $ScriptRoot "sql"

Write-Info "=========================================="
Write-Info "MySQL Script Execution - RuoYi Cloud Plus"
Write-Info "=========================================="
Write-Info "Host: $Host"
Write-Info "User: $User"
Write-Info "Password: ****"
Write-Info "=========================================="
Write-Host ""

# Test connection
if (-not (Test-MySQLConnection)) {
    Write-Error "Cannot proceed without MySQL connection."
    exit 1
}

$successCount = 0
$failCount = 0

# ==========================================
# Phase 1: RuoYi Base Modules
# ==========================================
if (-not $SkipRuoYi) {
    Write-Info "=========================================="
    Write-Info "Phase 1: RuoYi Base Modules"
    Write-Info "=========================================="
    Write-Host ""
    
    $ruoyiScripts = @(
        @{Path = "ry-config.sql"; Desc = "Nacos Configuration Database"},
        @{Path = "ry-cloud.sql"; Desc = "RuoYi Cloud Core Database"},
        @{Path = "ry-job.sql"; Desc = "RuoYi Job Database"},
        @{Path = "ry-seata.sql"; Desc = "Seata Database"},
        @{Path = "ry-workflow.sql"; Desc = "RuoYi Workflow Database"}
    )
    
    foreach ($script in $ruoyiScripts) {
        $scriptPath = Join-Path $SQLRoot $script.Path
        Write-Info "📦 $($script.Desc)"
        if (Invoke-SQLScript -ScriptPath $scriptPath) {
            $successCount++
        } else {
            $failCount++
            Write-Warning "⚠️  Continuing with next script..."
        }
        Write-Host ""
    }
}

# ==========================================
# Phase 2: XYPAI Custom Modules
# ==========================================
if (-not $SkipXYPAI) {
    Write-Info "=========================================="
    Write-Info "Phase 2: XYPAI Custom Modules"
    Write-Info "=========================================="
    Write-Host ""
    
    # 2.1 XYPAI Auth
    Write-Info "📦 XYPAI Auth Module"
    $authScript = Join-Path $ScriptRoot "..\xypai-security\sql\01_create_auth_user_table.sql"
    # First create database if it doesn't exist
    Write-Info "Creating database xypai_auth..."
    $createDbCmd = "& '$MySQLPath' -h $Host -P $Port -u $User -p$Password -e 'CREATE DATABASE IF NOT EXISTS xypai_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'"
    if (-not $DryRun) {
        Invoke-Expression $createDbCmd | Out-Null
    }
    if (Invoke-SQLScript -ScriptPath $authScript -Database "xypai_auth") {
        $successCount++
    } else {
        $failCount++
    }
    Write-Host ""
    
    # 2.2 XYPAI User
    Write-Info "📦 XYPAI User Module"
    $userScripts = @(
        "01_create_database.sql",
        "02_create_tables.sql",
        "03_create_indexes.sql"
    )
    foreach ($scriptName in $userScripts) {
        $scriptPath = Join-Path $ScriptRoot "..\xypai-user\sql\$scriptName"
        if (Invoke-SQLScript -ScriptPath $scriptPath) {
            if ($scriptName -eq "01_create_database.sql") {
                $successCount++
            }
        } else {
            $failCount++
        }
    }
    Write-Host ""
    
    # 2.3 XYPAI Trade
    Write-Info "📦 XYPAI Trade Module"
    $tradeScripts = @(
        "00_create_database.sql",
        "v7.1_service_order_upgrade.sql",
        "v7.1_service_review_create.sql",
        "v7.1_user_wallet_create.sql",
        "v7.1_transaction_create.sql",
        "v7.1_service_stats_create.sql"
    )
    foreach ($scriptName in $tradeScripts) {
        $scriptPath = Join-Path $ScriptRoot "..\xypai-trade\sql\$scriptName"
        if (Invoke-SQLScript -ScriptPath $scriptPath -Database "xypai_trade") {
            if ($scriptName -eq "00_create_database.sql") {
                $successCount++
            }
        } else {
            $failCount++
        }
    }
    Write-Host ""
    
    # 2.4 XYPAI Chat
    Write-Info "📦 XYPAI Chat Module"
    $chatScripts = @(
        "01_create_database.sql",
        "02_create_tables_v7.0.sql",
        "03_upgrade_to_v7.1.sql",
        "04_create_indexes.sql"
    )
    foreach ($scriptName in $chatScripts) {
        $scriptPath = Join-Path $ScriptRoot "..\xypai-chat\eve_workspace\sql\$scriptName"
        if (Invoke-SQLScript -ScriptPath $scriptPath -Database "xypai_chat") {
            if ($scriptName -eq "01_create_database.sql") {
                $successCount++
            }
        } else {
            $failCount++
        }
    }
    Write-Host ""
    
    # 2.5 XYPAI Content - Check if exists
    Write-Info "📦 XYPAI Content Module"
    Write-Warning "⚠️  xypai-content SQL scripts not found. Skipping..."
    Write-Host ""
}

# ==========================================
# Summary
# ==========================================
Write-Info "=========================================="
Write-Info "Execution Summary"
Write-Info "=========================================="
Write-Success "✅ Successful: $successCount"
if ($failCount -gt 0) {
    Write-Error "❌ Failed: $failCount"
}
Write-Info "=========================================="

if ($failCount -gt 0) {
    exit 1
} else {
    exit 0
}

