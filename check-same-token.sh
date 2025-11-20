#!/bin/bash

echo "========================================="
echo "🔍 Same-Token Redis 诊断工具"
echo "========================================="
echo ""

# Redis 连接信息
REDIS_HOST="127.0.0.1"
REDIS_PORT="6379"
REDIS_PASSWORD="ruoyi123"

echo "📋 步骤1: 检查 Redis 连接"
redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD PING
if [ $? -ne 0 ]; then
    echo "❌ Redis 连接失败！"
    exit 1
fi
echo "✅ Redis 连接成功"
echo ""

echo "📋 步骤2: 检查 database 0"
redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD -n 0 <<EOF
ECHO "当前 database: 0"
ECHO ""
ECHO "🔑 查找所有 same-token 相关的 key:"
KEYS *same*
ECHO ""
ECHO "🔑 查找所有 satoken 相关的 key:"
KEYS satoken:*
ECHO ""
ECHO "📋 获取我们存储的 Same-Token:"
GET satoken:var:same-token
ECHO ""
EOF

echo ""
echo "📋 步骤3: 检查其他 database (1-15)"
for db in {1..15}; do
    result=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD -n $db KEYS "*same*" 2>/dev/null)
    if [ ! -z "$result" ]; then
        echo "⚠️  发现 database $db 中有 same-token 相关的 key:"
        echo "$result"
        echo ""
    fi
done

echo "========================================="
echo "✅ 诊断完成"
echo "========================================="
