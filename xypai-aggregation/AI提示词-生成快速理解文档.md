# AI Prompt：生成聚合服务快速理解文档

> 本文件提供 AI 提示词，用于为聚合层（BFF）子模块快速生成标准化文档。

---

## 📋 中文提示词

```
请为 xypai-aggregation/[子模块名] 生成一个"快速理解.md"文档。

背景信息：
- 本模块是 BFF（Backend For Frontend）聚合服务
- 职责：业务编排、数据聚合、不直接访问数据库
- 通过 Feign RPC 调用领域服务（xypai-user、xypai-content 等）

要求：
1. 文档总长度不超过 110 行
2. 必须包含以下 9 个章节（使用 emoji）：
   - 📌 核心定位（强调 BFF 模式、无数据库、RPC 聚合）
   - 🎯 主要功能（3-7 个聚合功能点，如"首页用户推荐"）
   - 🏗️ 技术栈（Spring Cloud OpenFeign + Redis）
   - 📁 核心目录结构（controller/service/client，不能有 mapper）
   - 🔑 核心接口（列出聚合接口，标注数据来源）
   - 🏛️ 微服务架构（BFF → 领域服务 → 数据库，三层架构图）
   - 🔥 技术亮点（BFF 模式、批量 RPC、缓存优化等）
   - 🚀 快速启动（列出依赖的领域服务）
   - 📌 注意事项（第一条强调无数据库连接）

3. 特别要求：
   - "核心定位"必须说明：不直接连接数据库，通过 Feign RPC 调用领域服务
   - "核心接口"表格增加"聚合数据来源"列（如：user + content + order）
   - "架构图"必须展示 BFF → 领域服务 → 数据库 三层结构
   - "技术亮点"必须包含 BFF 模式优势（减少前端请求次数）
   - "注意事项"第一条：**无数据库连接**（加粗）

参考示例：xypai-aggregation/xypai-app-bff/快速理解.md
```

---

## 📋 English Prompt

```
Please generate a "Quick Understanding.md" document for xypai-aggregation/[sub-module-name].

Context:
- This module is a BFF (Backend For Frontend) aggregation service
- Responsibilities: Business orchestration, data aggregation, NO direct database access
- Calls domain services via Feign RPC (xypai-user, xypai-content, etc.)

Requirements:
1. Total length ≤ 110 lines
2. Must include these 9 sections (with emoji):
   - 📌 Core Positioning (emphasize BFF pattern, no database, RPC aggregation)
   - 🎯 Main Features (3-7 aggregation features, e.g., "Home user recommendation")
   - 🏗️ Tech Stack (Spring Cloud OpenFeign + Redis)
   - 📁 Core Directory Structure (controller/service/client, NO mapper)
   - 🔑 Core APIs (list aggregation endpoints, mark data sources)
   - 🏛️ Microservice Architecture (BFF → Domain Services → Database, 3-tier diagram)
   - 🔥 Technical Highlights (BFF pattern, batch RPC, caching, etc.)
   - 🚀 Quick Start (list required domain services)
   - 📌 Important Notes (first item: NO database connection, bold)

3. Special Requirements:
   - "Core Positioning" must state: No direct database connection, calls domain services via Feign RPC
   - "Core APIs" table adds "Aggregated Data Sources" column (e.g., user + content + order)
   - "Architecture Diagram" must show BFF → Domain Services → Database (3 tiers)
   - "Technical Highlights" must include BFF pattern benefits (reduce frontend requests)
   - First note in "Important Notes": **NO database connection** (bold)

Reference: xypai-aggregation/xypai-app-bff/快速理解.md
```

---

## 🎯 使用场景

### 场景 1：为新的 BFF 服务生成文档

```
请为 xypai-aggregation/xypai-web-bff 生成"快速理解.md"文档。

额外信息：
- 端口：9410
- 用途：为 Web 客户端提供聚合接口
- 核心功能：首页大屏展示、SEO 数据聚合、Web 推荐算法
- 调用服务：xypai-user, xypai-content, xypai-order

要求同上。
```

### 场景 2：批量生成 BFF 文档

```
请为以下 BFF 服务生成"快速理解.md"文档：
1. xypai-web-bff (9410) - Web 端聚合服务
2. xypai-admin-bff (9420) - 后台管理聚合服务

每个文档要求同上。
```

---

## 💡 文档优化建议

### 如果遗漏 BFF 特征：

```
请更新文档，强化 BFF 模式特征：
1. "核心定位"增加说明：不直接访问数据库，通过 RPC 调用领域服务
2. "技术亮点"增加：前端请求从 N 次减少到 1 次
3. "架构图"展示三层结构：BFF → 领域服务 → 数据库
```

### 如果"核心接口"表格缺少数据来源：

```
请在"核心接口"表格增加"聚合数据来源"列，示例：
| 接口 | 说明 | 聚合数据来源 |
|------|------|-------------|
| GET /api/home/feed | 首页推荐 | user + content |
```

---

## 📚 相关文件

- `xypai-aggregation/快速理解.md` - 聚合层总体文档
- `xypai-aggregation/xypai-app-bff/快速理解.md` - App BFF 示例文档
- `xypai-aggregation/README.md` - 聚合层架构说明
