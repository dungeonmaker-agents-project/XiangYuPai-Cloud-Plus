# 🚨 紧急修复指南 - 立即执行

> **问题**: 服务运行的是旧代码，导致 NoClassDefFoundError  
> **时间**: 2025-10-25 10:30  
> **紧急程度**: ⚠️⚠️⚠️ 立即执行

---

## 🎯 问题根源

**服务正在运行旧的jar包，不是最新编译的代码！**

- Maven编译成功，但target/classes目录为空
- 服务仍然能运行，说明它加载的是旧的jar包
- 旧jar包中的Lombok Builder类不存在，导致运行时错误

---

## ⚡ 立即执行（3步）

### 步骤1：在IDEA中停止xypai-user服务

1. 找到IDEA窗口
2. 找到底部的"Run"或"Services"面板
3. 找到正在运行的 `XyPaiUserApplication`
4. 点击红色的 ■ (Stop) 按钮
5. 确认服务已停止

### 步骤2：在PowerShell中重新编译并打包

```powershell
cd C:\Users\Admin\Documents\GitHub\RuoYi-Cloud-Plus\xypai-user
mvn clean package -DskipTests
```

等待编译完成，应该看到：
```
[INFO] Building xypai-user 2.5.0
[INFO] BUILD SUCCESS
```

### 步骤3：在IDEA中重新启动服务

1. 在IDEA中找到 `XyPaiUserApplication.java`
2. 右键 → Run 'XyPaiUserApplication'
3. 等待服务启动成功（看到 "Started XyPaiUserApplication"）

---

## 🔍 如果仍有问题

### 检查是否有其他Java进程在运行

```powershell
# 查找所有Java进程
Get-Process | Where-Object { $_.ProcessName -eq "java" }

# 查找占用8080端口的进程
netstat -ano | findstr :8080
```

如果找到旧进程，记录PID（进程ID），然后：

```powershell
# 停止进程（替换12345为实际的PID）
Stop-Process -Id 12345 -Force
```

---

## ✅ 验证修复成功

### 1. 检查后端日志

启动成功后，应该看到：
```
2025-10-25 10:xx:xx [INFO] Started XyPaiUserApplication in x.xxx seconds
```

**不应该再看到**：
```
java.lang.NoClassDefFoundError: com/xypai/user/domain/vo/UserListVO$UserListVOBuilder
```

### 2. 测试API

```bash
# 测试精选用户API
GET http://localhost:8080/xypai-user/api/v1/homepage/featured-users?limit=5
```

✅ 期望响应：`200 OK`，返回用户列表  
❌ 不应看到：`500 Internal Server Error`

### 3. 检查前端

前端应该能正常加载用户数据，不再使用模拟数据。

---

## 🔧 如果编译仍然失败

如果 `mvn clean package` 仍然报错，执行：

```powershell
# 1. 从父项目根目录编译
cd C:\Users\Admin\Documents\GitHub\RuoYi-Cloud-Plus

# 2. 清理所有模块
mvn clean

# 3. 只编译xypai-user及其依赖
mvn install -pl xypai-user -am -DskipTests
```

---

## 📞 当前状态

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 代码修改 | ✅ 已完成 | UserProfileServiceImpl 添加import |
| 编译成功 | ⚠️ 部分 | Maven报告成功但class未生成 |
| 服务状态 | ❌ 运行旧代码 | 需要停止并重启 |
| API测试 | ❌ 返回500 | NoClassDefFoundError |

---

**⚡ 立即执行上述3个步骤，然后告诉我结果！**

