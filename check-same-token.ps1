# Same-Token Redis 诊断工具 (PowerShell 版本)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🔍 Same-Token Redis 诊断工具" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Redis 连接信息
$RedisHost = "127.0.0.1"
$RedisPort = "6379"
$RedisPassword = "ruoyi123"

# 步骤1: 检查 Redis 连接
Write-Host "📋 步骤1: 检查 Redis 连接" -ForegroundColor Yellow
$pingResult = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword PING 2>&1
if ($pingResult -like "*PONG*") {
    Write-Host "✅ Redis 连接成功" -ForegroundColor Green
} else {
    Write-Host "❌ Redis 连接失败！" -ForegroundColor Red
    Write-Host "   错误: $pingResult" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 步骤2: 检查 database 0
Write-Host "📋 步骤2: 检查 database 0 中的 Same-Token" -ForegroundColor Yellow
Write-Host ""

Write-Host "🔑 查找所有 same-token 相关的 key:" -ForegroundColor Cyan
$sameKeys = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword -n 0 KEYS "*same*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
if ($sameKeys) {
    Write-Host $sameKeys -ForegroundColor Green
} else {
    Write-Host "   (无)" -ForegroundColor Gray
}
Write-Host ""

Write-Host "🔑 查找所有 satoken 相关的 key:" -ForegroundColor Cyan
$satokenKeys = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword -n 0 KEYS "satoken:*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
if ($satokenKeys) {
    foreach ($key in $satokenKeys) {
        Write-Host "   $key" -ForegroundColor Green
    }
} else {
    Write-Host "   (无)" -ForegroundColor Gray
}
Write-Host ""

Write-Host "📋 获取 satoken:var:same-token 的值:" -ForegroundColor Cyan
$sameTokenValue = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword -n 0 GET "satoken:var:same-token" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
if ($sameTokenValue) {
    $shortValue = if ($sameTokenValue.Length -gt 50) { $sameTokenValue.Substring(0, 50) + "..." } else { $sameTokenValue }
    Write-Host "   ✅ 找到: $shortValue" -ForegroundColor Green
    Write-Host "   完整长度: $($sameTokenValue.Length) 字符" -ForegroundColor Gray
} else {
    Write-Host "   ❌ 未找到！" -ForegroundColor Red
}
Write-Host ""

# 步骤3: 检查 Sa-Token 自己存储的 same-token
Write-Host "📋 步骤3: 检查 Sa-Token 内部存储的 same-token" -ForegroundColor Yellow
$saInternalKey = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword -n 0 GET "satoken:login:same-token" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
if ($saInternalKey) {
    $shortValue = if ($saInternalKey.Length -gt 50) { $saInternalKey.Substring(0, 50) + "..." } else { $saInternalKey }
    Write-Host "   ✅ 找到 satoken:login:same-token: $shortValue" -ForegroundColor Green
} else {
    Write-Host "   (未找到 satoken:login:same-token)" -ForegroundColor Gray
}
Write-Host ""

# 步骤4: 检查其他 database
Write-Host "📋 步骤4: 检查其他 database (1-15)" -ForegroundColor Yellow
for ($db = 1; $db -le 15; $db++) {
    $keysInDb = & redis-cli -h $RedisHost -p $RedisPort -a $RedisPassword -n $db KEYS "*same*" 2>&1 | Where-Object { $_ -notlike "*Warning*" }
    if ($keysInDb -and $keysInDb.Count -gt 0) {
        Write-Host "   ⚠️  发现 database $db 中有 same-token 相关的 key:" -ForegroundColor Yellow
        Write-Host "   $keysInDb" -ForegroundColor Yellow
    }
}
Write-Host ""

# 步骤5: 对比两个 token 值
Write-Host "📋 步骤5: 对比 Token 值" -ForegroundColor Yellow
if ($sameTokenValue -and $saInternalKey) {
    if ($sameTokenValue -eq $saInternalKey) {
        Write-Host "   ✅ 两个 token 值相同" -ForegroundColor Green
    } else {
        Write-Host "   ❌ 两个 token 值不同！" -ForegroundColor Red
        Write-Host "   satoken:var:same-token:     $($sameTokenValue.Substring(0, [Math]::Min(50, $sameTokenValue.Length)))..." -ForegroundColor Red
        Write-Host "   satoken:login:same-token:   $($saInternalKey.Substring(0, [Math]::Min(50, $saInternalKey.Length)))..." -ForegroundColor Red
    }
} elseif ($sameTokenValue) {
    Write-Host "   ⚠️  只找到 satoken:var:same-token" -ForegroundColor Yellow
} elseif ($saInternalKey) {
    Write-Host "   ⚠️  只找到 satoken:login:same-token" -ForegroundColor Yellow
} else {
    Write-Host "   ❌ 两个 token 都未找到！" -ForegroundColor Red
}
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "✅ 诊断完成" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 下一步:" -ForegroundColor Yellow
Write-Host "   1. 如果未找到 token，检查 Gateway 是否正确启动" -ForegroundColor Gray
Write-Host "   2. 如果两个 token 不同，这就是问题所在！" -ForegroundColor Gray
Write-Host "   3. 检查 xypai-user 启动日志中的 Same-Token 验证日志" -ForegroundColor Gray
