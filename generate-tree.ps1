# PowerShell script: Generate RuoYi-Cloud-Plus microservice project tree structure (COMPACT VERSION)
# Version: 3.0 - Optimized for AI Context
# Usage: powershell -ExecutionPolicy Bypass -File .\generate-tree.ps1 [-Depth <int>] [-ShowFiles] [-Help]
# Quick Run: powershell -ExecutionPolicy Bypass -File .\generate-tree.ps1

param(
    [int]$Depth = 10,
    [switch]$ShowFiles,
    [switch]$Help
)

if ($Help) {
    Write-Host ""
    Write-Host "RuoYi-Cloud-Plus Project Tree Generator v3.0 (Compact)" -ForegroundColor Cyan
    Write-Host "======================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "DESCRIPTION:" -ForegroundColor Yellow
    Write-Host "  Generates a COMPACT project structure optimized for AI context"
    Write-Host "  Focus: Architecture overview, not detailed file listings"
    Write-Host ""
    Write-Host "USAGE (if execution policy blocks):" -ForegroundColor Yellow
    Write-Host "  powershell -ExecutionPolicy Bypass -File .\generate-tree.ps1"
    Write-Host ""
    Write-Host "PARAMETERS:" -ForegroundColor Yellow
    Write-Host "  -Depth <int>      Directory depth (default: 10, shows all folders)"
    Write-Host "  -ShowFiles        Show file counts by type (directories always shown)"
    Write-Host "  -Help             Display this help"
    Write-Host ""
    Write-Host "OUTPUT: .cursor/rules/project-tree/project.mdc (compact version)"
    Write-Host ""
    exit 0
}

if ($Depth -lt 1 -or $Depth -gt 10) {
    Write-Host "Error: Depth must be between 1-10 (default: 10 for complete directory tree)" -ForegroundColor Red
    exit 1
}

$startTime = Get-Date

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "RuoYi-Cloud-Plus Compact Tree Generator v3.0" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Configuration: Depth=$Depth, ShowFiles=$ShowFiles" -ForegroundColor White
Write-Host ""

# Main modules to scan
$mainDirs = @(
    "ruoyi-api", "ruoyi-auth", "ruoyi-common", "ruoyi-example",
    "ruoyi-gateway", "ruoyi-modules", "ruoyi-visual",
    "xypai-chat", "xypai-content", "xypai-trade", "xypai-user",
    "script"
)

# Directories to skip
$excludeDirs = @(
    "target", "build", "node_modules", ".git", ".idea", ".vscode", ".cursor",
    ".settings", "bin", "obj", "out", "logs", "sessionStore", "data", "store"
)

# File extensions to count (not list individually)
$codeExtensions = @{
    "Java" = @(".java")
    "Config" = @(".xml", ".yml", ".yaml", ".properties")
    "SQL" = @(".sql")
    "Script" = @(".sh", ".bat", ".ps1")
    "Doc" = @(".md", ".txt")
}

# Directories where we should show individual Java files
$showFilesInDirs = @("controller", "impl", "vo")

# Get directory tree (directories only, with file summaries)
function Get-CompactTree {
    param([string]$Path, [int]$CurrentDepth = 0, [string]$Prefix = "", [int]$MaxDepth = 2)
    
    if ($CurrentDepth -gt $MaxDepth) { return @() }
    
    $items = @()
    
    try {
        # Get current directory name
        $currentDirName = Split-Path $Path -Leaf
        
        # Check if we should show files in this directory
        $shouldShowFiles = $showFilesInDirs -contains $currentDirName
        
        # Get children (directories and conditionally files)
        $children = Get-ChildItem -Path $Path -Force -ErrorAction SilentlyContinue | Where-Object {
            if ($_.PSIsContainer) {
                return $excludeDirs -notcontains $_.Name -and $_.Name -notlike ".*"
            }
            # Show files only if we're in controller/impl/vo directory and it's a .java file
            if ($shouldShowFiles -and $_.Extension -eq ".java") {
                return $true
            }
            return $false
        } | Sort-Object { $_.PSIsContainer }, Name -Descending
        
        $childCount = $children.Count
        $index = 0
        
        foreach ($child in $children) {
            $index++
            $isLast = ($index -eq $childCount)
            $connector = if ($isLast) { "+-- " } else { "|-- " }
            $newPrefix = if ($isLast) { "    " } else { "|   " }
            
            if ($child.PSIsContainer) {
                # It's a directory
                if ($ShowFiles) {
                    $fileCount = (Get-ChildItem -Path $child.FullName -File -ErrorAction SilentlyContinue).Count
                    if ($fileCount -gt 0) {
                        $items += "$Prefix$connector$($child.Name)\ [$fileCount files]"
                    } else {
                        $items += "$Prefix$connector$($child.Name)\"
                    }
                } else {
                    $items += "$Prefix$connector$($child.Name)\"
                }
                
                # Recurse into subdirectories
                if ($CurrentDepth -lt $MaxDepth) {
                    $items += Get-CompactTree -Path $child.FullName -CurrentDepth ($CurrentDepth + 1) -Prefix ($Prefix + $newPrefix) -MaxDepth $MaxDepth
                }
            } else {
                # It's a file (only .java files in controller/impl/vo directories)
                $items += "$Prefix$connector$($child.Name)"
            }
        }
    } catch {
        Write-Host "Warning: Could not access $Path" -ForegroundColor Yellow
    }
    
    return $items
}

# Count files recursively
function Get-FileStats {
    param([string]$Path)
    
    $stats = @{}
    foreach ($category in $codeExtensions.Keys) {
        $stats[$category] = 0
    }
    
    try {
        Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
            $shouldInclude = $true
            foreach ($excludeDir in $excludeDirs) {
                if ($_.FullName -match [regex]::Escape($excludeDir)) {
                    $shouldInclude = $false
                    break
                }
            }
            return $shouldInclude
        } | ForEach-Object {
            foreach ($category in $codeExtensions.Keys) {
                foreach ($ext in $codeExtensions[$category]) {
                    if ($_.Extension -eq $ext) {
                        $stats[$category]++
                        break
                    }
                }
            }
        }
    } catch {}
    
    return $stats
}

# Build output
$output = @()
$output += "---"
$output += "alwaysApply: true"
$output += "---"
$output += ""
$output += "# RuoYi-Cloud-Plus Microservice Architecture (Compact)"
$output += ""
$output += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$output += "Depth: $Depth (default) | Purpose: Complete project overview for AI"
$output += ""
$output += "**Note:** This tree shows ALL directories/folders. Java files are listed ONLY in `controller/`, `impl/`, and `vo/` directories to show implemented features."
$output += ""

# Module descriptions (static, concise)
$output += "## Architecture Overview"
$output += ""
$output += "**Core Framework:**"
$output += "- ruoyi-api - API definitions & remote service interfaces"
$output += "- ruoyi-auth - Authentication service (login, token, captcha)"
$output += "- ruoyi-common - 30+ shared modules (redis, mybatis, satoken, web, etc.)"
$output += "- ruoyi-gateway - API Gateway (routing, filters, auth)"
$output += "- ruoyi-modules - Business modules (system, gen, job, resource, workflow)"
$output += "- ruoyi-visual - Monitoring (nacos, seata, snailjob, monitor)"
$output += ""
$output += "**Custom Business:**"
$output += "- xypai-chat - Chat/messaging service (WebSocket, conversations)"
$output += "- xypai-content - Content management (articles, media, comments)"
$output += "- xypai-trade - Trade/payment (orders, wallet, reviews)"
$output += "- xypai-user - User management (profiles, relations, stats)"
$output += ""
$output += "**Infrastructure:**"
$output += "- script - SQL scripts, Docker configs, Nacos configs"
$output += ""

# Process each module
$moduleCount = 0
foreach ($dir in $mainDirs) {
    if (Test-Path $dir) {
        $moduleCount++
        Write-Host "[$moduleCount/$($mainDirs.Count)] Processing $dir..." -ForegroundColor Yellow
        
        $output += "## $dir"
        $output += ""
        $output += "``````"
        $output += "$dir\"
        $output += Get-CompactTree -Path $dir -MaxDepth $Depth
        $output += "``````"
        $output += ""
        
        # File statistics
        if ($ShowFiles) {
            $stats = Get-FileStats -Path $dir
            $hasFiles = $false
            $statsLine = "Files: "
            foreach ($category in $stats.Keys | Sort-Object) {
                if ($stats[$category] -gt 0) {
                    $statsLine += "$category=$($stats[$category]) "
                    $hasFiles = $true
                }
            }
            if ($hasFiles) {
                $output += $statsLine.Trim()
                $output += ""
            }
        }
    }
}

# Root files (only important ones)
$importantFiles = Get-ChildItem -Path . -File | Where-Object {
    $_.Name -match "^(pom\.xml|README\.md|.*\.code-workspace|quick_deploy\.sh|LICENSE)$"
} | Select-Object -ExpandProperty Name

if ($importantFiles) {
    $output += "## Root Files"
    $output += ""
    $output += ($importantFiles | ForEach-Object { "- $_" }) -join "`n"
    $output += ""
}

# Summary
$output += "---"
$output += ""
$output += "**Note:** This is a complete directory tree (depth $Depth) showing ALL folders. "
$output += "Java files (.java) are listed in `controller/`, `impl/`, and `vo/` directories to show what features are implemented."
$output += ""

# Write output
if (-not (Test-Path ".cursor/rules/project-tree")) {
    New-Item -ItemType Directory -Path ".cursor/rules/project-tree" -Force | Out-Null
}

$outputPath = ".cursor/rules/project-tree/project.mdc"
$output -join "`r`n" | Out-File -FilePath $outputPath -Encoding UTF8

$outputSize = (Get-Item $outputPath).Length
$outputSizeKB = [math]::Round($outputSize / 1KB, 2)
$duration = ((Get-Date) - $startTime).TotalSeconds

Write-Host ""
Write-Host "Success!" -ForegroundColor Green
Write-Host "  File: $outputPath" -ForegroundColor White
Write-Host "  Size: $outputSizeKB KB (~$([math]::Round($outputSize / 50, 0)) lines)" -ForegroundColor White
Write-Host "  Time: $([math]::Round($duration, 2))s" -ForegroundColor White
Write-Host "  Modules: $moduleCount" -ForegroundColor White
Write-Host ""
Write-Host "Tips:" -ForegroundColor Cyan
Write-Host "  Less: powershell -ExecutionPolicy Bypass -File .\generate-tree.ps1 -Depth 1" -ForegroundColor Yellow
Write-Host "  More: powershell -ExecutionPolicy Bypass -File .\generate-tree.ps1 -Depth 3 -ShowFiles" -ForegroundColor Yellow
Write-Host ""
