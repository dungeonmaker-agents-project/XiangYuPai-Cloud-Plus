---
title: APP用户
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# APP用户

描述：微服务权限管理系统, 具体包括XXX,XXX模块...

Base URLs:

# Authentication

# 用户资料

<a id="opIdgetUserProfile"></a>

## GET 获取用户资料

GET /api/v2/user/profile/{userId}

查询用户的完整资料信息（42个字段）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"nickname":"string","avatar":"string","avatarThumbnail":"string","backgroundImage":"string","gender":0,"genderDesc":"string","birthday":"2019-08-24","age":0,"ageRange":"string","cityId":0,"cityName":"string","location":"string","address":"string","ipLocation":"string","bio":"string","height":0,"weight":0,"bmi":0.1,"bmiLevel":"string","realName":"string","wechat":"string","wechatMasked":"string","wechatUnlockCondition":0,"wechatUnlockDesc":"string","canViewWechat":true,"isRealVerified":true,"isGodVerified":true,"isActivityExpert":true,"isVip":true,"isVipValid":true,"isPopular":true,"vipLevel":0,"vipExpireTime":"2019-08-24T14:15:22Z","onlineStatus":0,"onlineStatusDesc":"string","isOnline":true,"lastOnlineTime":"2019-08-24T14:15:22Z","profileCompleteness":0,"completenessLevel":"string","isProfileComplete":true,"lastEditTime":"2019-08-24T14:15:22Z","occupations":[{"id":0,"userId":0,"occupationCode":"string","occupationName":"string","category":"string","iconUrl":"string","sortOrder":0,"createdAt":"2019-08-24T14:15:22Z","isPrimary":true}],"stats":{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0},"isFollowed":true,"isMutualFollow":true,"isBlocked":true,"createdAt":"2019-08-24T14:15:22Z","updatedAt":"2019-08-24T14:15:22Z","version":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserProfileVO](#schemaruserprofilevo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateUserProfile"></a>

## PUT 更新用户资料

PUT /api/v2/user/profile/{userId}

更新用户的资料信息（支持分步骤编辑）

> Body 请求参数

```json
{
  "userId": 0,
  "nickname": "string",
  "avatar": "string",
  "avatarThumbnail": "string",
  "backgroundImage": "string",
  "gender": 3,
  "birthday": "2019-08-24",
  "cityId": 0,
  "location": "string",
  "address": "string",
  "bio": "string",
  "height": 140,
  "weight": 30,
  "realName": "string",
  "wechat": "string",
  "wechatUnlockCondition": 3,
  "onlineStatus": 3,
  "version": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|body|body|[UserProfileUpdateDTO](#schemauserprofileupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateOnlineStatus"></a>

## PUT 更新在线状态

PUT /api/v2/user/profile/{userId}/online-status

更新用户的在线状态（在线/离线/忙碌/隐身）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|onlineStatus|query|integer(int32)| 是 |在线状态|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetCurrentUserProfile"></a>

## GET 获取当前用户资料

GET /api/v2/user/profile/current

查询当前登录用户的完整资料

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"nickname":"string","avatar":"string","avatarThumbnail":"string","backgroundImage":"string","gender":0,"genderDesc":"string","birthday":"2019-08-24","age":0,"ageRange":"string","cityId":0,"cityName":"string","location":"string","address":"string","ipLocation":"string","bio":"string","height":0,"weight":0,"bmi":0.1,"bmiLevel":"string","realName":"string","wechat":"string","wechatMasked":"string","wechatUnlockCondition":0,"wechatUnlockDesc":"string","canViewWechat":true,"isRealVerified":true,"isGodVerified":true,"isActivityExpert":true,"isVip":true,"isVipValid":true,"isPopular":true,"vipLevel":0,"vipExpireTime":"2019-08-24T14:15:22Z","onlineStatus":0,"onlineStatusDesc":"string","isOnline":true,"lastOnlineTime":"2019-08-24T14:15:22Z","profileCompleteness":0,"completenessLevel":"string","isProfileComplete":true,"lastEditTime":"2019-08-24T14:15:22Z","occupations":[{"id":0,"userId":0,"occupationCode":"string","occupationName":"string","category":"string","iconUrl":"string","sortOrder":0,"createdAt":"2019-08-24T14:15:22Z","isPrimary":true}],"stats":{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0},"isFollowed":true,"isMutualFollow":true,"isBlocked":true,"createdAt":"2019-08-24T14:15:22Z","updatedAt":"2019-08-24T14:15:22Z","version":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserProfileVO](#schemaruserprofilevo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateCurrentUserProfile"></a>

## PUT 更新当前用户资料

PUT /api/v2/user/profile/current

更新当前登录用户的资料

> Body 请求参数

```json
{
  "userId": 0,
  "nickname": "string",
  "avatar": "string",
  "avatarThumbnail": "string",
  "backgroundImage": "string",
  "gender": 3,
  "birthday": "2019-08-24",
  "cityId": 0,
  "location": "string",
  "address": "string",
  "bio": "string",
  "height": 140,
  "weight": 30,
  "realName": "string",
  "wechat": "string",
  "wechatUnlockCondition": 3,
  "onlineStatus": 3,
  "version": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserProfileUpdateDTO](#schemauserprofileupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgoOnline"></a>

## PUT 用户上线

PUT /api/v2/user/profile/current/go-online

标记用户为在线状态

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgoOffline"></a>

## PUT 用户离线

PUT /api/v2/user/profile/current/go-offline

标记用户为离线状态

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgoInvisible"></a>

## PUT 用户隐身

PUT /api/v2/user/profile/current/go-invisible

标记用户为隐身状态

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdisUserOnline"></a>

## GET 检查用户是否在线

GET /api/v2/user/profile/{userId}/is-online

检查用户是否在线（5分钟内活跃）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":true}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RBoolean](#schemarboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetProfileCompleteness"></a>

## GET 获取资料完整度

GET /api/v2/user/profile/{userId}/completeness

查询用户的资料完整度信息（0-100分）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"currentScore":0,"level":"string","isComplete":true,"coreFieldsScore":0,"extendedFieldsScore":0,"suggestions":["string"],"completedItems":["string"],"remainingScore":0,"percentage":0,"progressColor":"string","message":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RProfileCompletenessVO](#schemarprofilecompletenessvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetCurrentUserCompleteness"></a>

## GET 获取当前用户资料完整度

GET /api/v2/user/profile/current/completeness

查询当前用户的资料完整度和完善建议

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"currentScore":0,"level":"string","isComplete":true,"coreFieldsScore":0,"extendedFieldsScore":0,"suggestions":["string"],"completedItems":["string"],"remainingScore":0,"percentage":0,"progressColor":"string","message":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RProfileCompletenessVO](#schemarprofilecompletenessvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 用户钱包

<a id="opIdunfreezeWallet"></a>

## PUT 解冻钱包

PUT /api/v1/wallet/{userId}/unfreeze

管理员解冻用户钱包

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdfreezeWallet"></a>

## PUT 冻结钱包

PUT /api/v1/wallet/{userId}/freeze

管理员冻结用户钱包

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|reason|query|string| 否 |冻结原因|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdtransfer"></a>

## POST 钱包转账

POST /api/v1/wallet/transfer

向其他用户转账

> Body 请求参数

```json
{
  "toUserId": 0,
  "amount": 0.01,
  "description": "string",
  "paymentPassword": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[WalletTransferDTO](#schemawallettransferdto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdrecharge"></a>

## POST 钱包充值

POST /api/v1/wallet/recharge

用户钱包余额充值

> Body 请求参数

```json
{
  "amount": 0.01,
  "paymentMethod": "string",
  "description": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[WalletRechargeDTO](#schemawalletrechargedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RString](#schemarstring)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserWalletInfo"></a>

## GET 获取指定用户钱包信息

GET /api/v1/wallet/{userId}

管理员查看指定用户钱包信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"balance":"string","balanceFen":0,"available":true,"version":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserWalletVO](#schemaruserwalletvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetTransactions"></a>

## GET 获取交易记录

GET /api/v1/wallet/transactions

分页查询用户交易流水

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|query|integer(int64)| 否 |用户ID|
|type|query|string| 否 |交易类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|
|refId|query|string| 否 |关联业务ID|
|pageSize|query|integer(int32)| 否 |分页大小|
|pageNum|query|integer(int32)| 否 |当前页数|
|orderByColumn|query|string| 否 |排序列|
|isAsc|query|string| 否 |排序的方向desc或者asc|

> 返回示例

> 200 Response

```
{"total":0,"rows":[{"id":0,"userId":0,"amount":"string","amountFen":0,"type":"string","typeDesc":"string","refId":"string","createdAt":"2019-08-24T14:15:22Z","isIncome":true,"formattedAmount":"string"}],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfoTransactionVO](#schematabledatainfotransactionvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetTransactionDetail"></a>

## GET 获取交易详情

GET /api/v1/wallet/transactions/{transactionId}

根据交易ID获取详细信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|transactionId|path|integer(int64)| 是 |交易ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"userId":0,"amount":"string","amountFen":0,"type":"string","typeDesc":"string","refId":"string","createdAt":"2019-08-24T14:15:22Z","isIncome":true,"formattedAmount":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RTransactionVO](#schemartransactionvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetWalletStatistics"></a>

## GET 获取钱包统计

GET /api/v1/wallet/statistics

获取用户钱包统计数据

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|startDate|query|string| 否 |统计开始时间|
|endDate|query|string| 否 |统计结束时间|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"property1":null,"property2":null}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RMapStringObject](#schemarmapstringobject)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetWalletInfo"></a>

## GET 获取钱包信息

GET /api/v1/wallet/info

获取当前用户的钱包详细信息

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"balance":"string","balanceFen":0,"available":true,"version":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserWalletVO](#schemaruserwalletvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 用户管理

<a id="opIdedit"></a>

## PUT 修改用户

PUT /api/v1/users

更新用户信息

> Body 请求参数

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "email": "user@example.com",
  "avatar": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0,
  "version": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserUpdateDTO](#schemauserupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdadd"></a>

## POST 新增用户

POST /api/v1/users

创建新用户

> Body 请求参数

```json
{
  "username": "string",
  "mobile": "string",
  "password": "string",
  "nickname": "string",
  "email": "user@example.com",
  "avatar": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserAddDTO](#schemauseradddto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdchangeStatus"></a>

## PUT 修改用户状态

PUT /api/v1/users/{userId}/status

启用/禁用用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|status|query|integer(int32)| 是 |状态值|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdresetPassword"></a>

## PUT 重置用户密码

PUT /api/v1/users/{userId}/reset-password

重置指定用户的密码

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdprofile"></a>

## GET 获取当前用户信息

GET /api/v1/users/profile

获取当前登录用户的详细信息

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","email":"string","realName":"string","location":"string","bio":"string","status":0,"statusDesc":"string","createdAt":"2019-08-24T14:15:22Z","version":0,"followed":true,"followingCount":0,"followersCount":0,"walletBalance":"string","maskedMobile":"string","maskedEmail":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserDetailVO](#schemaruserdetailvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateProfile"></a>

## PUT 更新当前用户信息

PUT /api/v1/users/profile

更新当前登录用户的信息

> Body 请求参数

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "email": "user@example.com",
  "avatar": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0,
  "version": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserUpdateDTO](#schemauserupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetInfo"></a>

## GET 获取用户详细信息

GET /api/v1/users/{userId}

根据用户ID获取详细信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","email":"string","realName":"string","location":"string","bio":"string","status":0,"statusDesc":"string","createdAt":"2019-08-24T14:15:22Z","version":0,"followed":true,"followingCount":0,"followersCount":0,"walletBalance":"string","maskedMobile":"string","maskedEmail":"string"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserDetailVO](#schemaruserdetailvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdlist"></a>

## GET 查询用户列表

GET /api/v1/users/list

分页查询用户列表信息

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|query|string| 否 |用户名（模糊查询）|
|mobile|query|string| 否 |手机号（模糊查询）|
|nickname|query|string| 否 |昵称（模糊查询）|
|status|query|integer(int32)| 否 |用户状态|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","status":0,"statusDesc":"string","createdAt":"2019-08-24T14:15:22Z","maskedMobile":"string"}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserListVO](#schemarlistuserlistvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdcheckUsername"></a>

## GET 检查用户名唯一性

GET /api/v1/users/check-username

检查用户名是否已被使用

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|query|string| 是 |用户名|
|userId|query|integer(int64)| 否 |用户ID(编辑时使用)|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":true}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RBoolean](#schemarboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdcheckMobile"></a>

## GET 检查手机号唯一性

GET /api/v1/users/check-mobile

检查手机号是否已被使用

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|mobile|query|string| 是 |手机号|
|userId|query|integer(int64)| 否 |用户ID(编辑时使用)|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":true}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RBoolean](#schemarboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdremove"></a>

## DELETE 删除用户

DELETE /api/v1/users/{userIds}

根据用户ID删除用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userIds|path|array[integer]| 是 |用户ID数组|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 用户统计

<a id="opIdincrementLikeCount"></a>

## PUT 增加点赞数

PUT /api/v1/users/stats/{userId}/like/increment

用户内容被点赞时调用（仅内部服务）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|count|query|integer(int32)| 否 |增加数量|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdincrementFollowerCount"></a>

## PUT 增加粉丝数

PUT /api/v1/users/stats/{userId}/follower/increment

用户被关注时调用（仅内部服务）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIddecrementFollowerCount"></a>

## PUT 减少粉丝数

PUT /api/v1/users/stats/{userId}/follower/decrement

用户被取消关注时调用（仅内部服务）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdincrementContentCount"></a>

## PUT 增加内容数

PUT /api/v1/users/stats/{userId}/content/increment

用户发布内容时调用（仅内部服务）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdrefreshCache"></a>

## POST 刷新统计缓存

POST /api/v1/users/stats/{userId}/refresh

从MySQL重新加载统计数据到Redis

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdinitUserStats"></a>

## POST 初始化用户统计

POST /api/v1/users/stats/init

为新用户创建统计记录

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|query|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetBatchUserStats"></a>

## POST 批量查询用户统计

POST /api/v1/users/stats/batch

批量获取多个用户的统计数据

> Body 请求参数

```json
[
  0
]
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|array[integer]| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserStatsVO](#schemarlistuserstatsvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserStats"></a>

## GET 获取用户统计

GET /api/v1/users/stats/{userId}

查询用户的统计数据（优先从Redis读取）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserStatsVO](#schemaruserstatsvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetQualityOrganizers"></a>

## GET 优质组局者排行

GET /api/v1/users/stats/quality-organizers

查询评分和成功率最高的组局者（TOP 10）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|limit|query|integer(int32)| 否 |数量限制|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserStatsVO](#schemarlistuserstatsvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetPopularUsers"></a>

## GET 人气用户排行

GET /api/v1/users/stats/popular

查询粉丝数最多的用户（TOP 10）

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|limit|query|integer(int32)| 否 |数量限制|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserStatsVO](#schemarlistuserstatsvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetCurrentUserStats"></a>

## GET 获取当前用户统计

GET /api/v1/users/stats/current

获取当前登录用户的统计数据

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"userId":0,"followerCount":0,"followingCount":0,"contentCount":0,"totalLikeCount":0,"totalCollectCount":0,"activityOrganizerCount":0,"activityParticipantCount":0,"activitySuccessCount":0,"activityCancelCount":0,"activityOrganizerScore":0,"activitySuccessRate":0,"lastSyncTime":"2019-08-24T14:15:22Z","isActive":true,"isPopular":true,"isQualityOrganizer":true,"followerFollowingRatio":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RUserStatsVO](#schemaruserstatsvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 职业标签

<a id="opIdgetUserOccupations"></a>

## GET 查询用户职业

GET /api/v1/occupation/user/{userId}

获取指定用户的所有职业标签

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":0,"userId":0,"occupationCode":"string","occupationName":"string","category":"string","iconUrl":"string","sortOrder":0,"createdAt":"2019-08-24T14:15:22Z","isPrimary":true}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserOccupationVO](#schemarlistuseroccupationvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateUserOccupations"></a>

## PUT 更新用户职业

PUT /api/v1/occupation/user/{userId}

批量更新用户的职业标签（最多5个）

> Body 请求参数

```json
{
  "occupationCodes": [
    "string"
  ],
  "keepSortOrder": true
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|body|body|[UserOccupationUpdateDTO](#schemauseroccupationupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetCurrentUserOccupations"></a>

## GET 查询当前用户职业

GET /api/v1/occupation/current

获取当前登录用户的所有职业标签

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"id":0,"userId":0,"occupationCode":"string","occupationName":"string","category":"string","iconUrl":"string","sortOrder":0,"createdAt":"2019-08-24T14:15:22Z","isPrimary":true}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListUserOccupationVO](#schemarlistuseroccupationvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateCurrentUserOccupations"></a>

## PUT 更新当前用户职业

PUT /api/v1/occupation/current

批量更新当前登录用户的职业标签

> Body 请求参数

```json
{
  "occupationCodes": [
    "string"
  ],
  "keepSortOrder": true
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserOccupationUpdateDTO](#schemauseroccupationupdatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdaddUserOccupation"></a>

## POST 添加职业标签

POST /api/v1/occupation/user/{userId}/add

为用户添加单个职业标签

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|occupationCode|query|string| 是 |职业编码|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserIdsByOccupation"></a>

## GET 查询职业用户列表

GET /api/v1/occupation/{occupationCode}/users

查询拥有指定职业的用户ID列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|occupationCode|path|string| 是 |职业编码|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[0]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListLong](#schemarlistlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdcountUsersByOccupation"></a>

## GET 统计职业用户数

GET /api/v1/occupation/{occupationCode}/count

统计拥有指定职业的用户总数

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|occupationCode|path|string| 是 |职业编码|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":0}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RInteger](#schemarinteger)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdhasOccupation"></a>

## GET 检查职业标签

GET /api/v1/occupation/user/{userId}/has

检查用户是否拥有指定职业标签

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|occupationCode|query|string| 是 |职业编码|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":true}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RBoolean](#schemarboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdlistAllOccupations"></a>

## GET 查询所有职业

GET /api/v1/occupation/list

获取所有启用的职业列表（按排序）

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"code":"string","name":"string","category":"string","iconUrl":"string","sortOrder":0,"status":0,"statusDesc":"string","createdAt":"2019-08-24T14:15:22Z","hasIcon":true}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListOccupationDictVO](#schemarlistoccupationdictvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdlistOccupationsByCategory"></a>

## GET 根据分类查询职业

GET /api/v1/occupation/category/{category}

查询指定分类下的所有职业

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|category|path|string| 是 |职业分类|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":[{"code":"string","name":"string","category":"string","iconUrl":"string","sortOrder":0,"status":0,"statusDesc":"string","createdAt":"2019-08-24T14:15:22Z","hasIcon":true}]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListOccupationDictVO](#schemarlistoccupationdictvo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdlistAllCategories"></a>

## GET 查询所有分类

GET /api/v1/occupation/categories

获取所有职业分类列表

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":["string"]}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RListString](#schemarliststring)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdremoveUserOccupation"></a>

## DELETE 删除职业标签

DELETE /api/v1/occupation/user/{userId}/remove

删除用户的单个职业标签

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|occupationCode|query|string| 是 |职业编码|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdclearUserOccupations"></a>

## DELETE 清空职业标签

DELETE /api/v1/occupation/user/{userId}/clear

删除用户的所有职业标签

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 认证用户管理

<a id="opIdvalidatePassword"></a>

## POST 验证用户密码

POST /api/v1/users/auth/validate-password

认证服务专用接口

> Body 请求参数

```json
{
  "username": "string",
  "password": "string",
  "type": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserValidateDTO](#schemauservalidatedto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":true}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RBoolean](#schemarboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdupdateLastLoginTime"></a>

## POST 更新用户最后登录时间

POST /api/v1/users/auth/update-login-time/{userId}

认证服务专用接口

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdautoRegisterUser"></a>

## POST 短信登录自动注册

POST /api/v1/users/auth/auto-register

认证服务专用接口，短信验证成功后自动创建用户

> Body 请求参数

```json
{
  "mobile": 13800138001,
  "source": "sms_login",
  "clientType": "web",
  "deviceId": "device_12345"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[AutoRegisterDTO](#schemaautoregisterdto)| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","status":0,"roles":["string"],"permissions":["string"],"lastLoginTime":"2019-08-24T14:15:22Z","createdAt":"2019-08-24T14:15:22Z"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RAuthUserVO](#schemarauthuservo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserByUsername"></a>

## GET 根据用户名获取用户信息

GET /api/v1/users/auth/username/{username}

认证服务专用接口

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|username|path|string| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","status":0,"roles":["string"],"permissions":["string"],"lastLoginTime":"2019-08-24T14:15:22Z","createdAt":"2019-08-24T14:15:22Z"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RAuthUserVO](#schemarauthuservo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserByMobile"></a>

## GET 根据手机号获取用户信息

GET /api/v1/users/auth/mobile/{mobile}

认证服务专用接口

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|mobile|path|string| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"id":0,"username":"string","mobile":"string","nickname":"string","avatar":"string","status":0,"roles":["string"],"permissions":["string"],"lastLoginTime":"2019-08-24T14:15:22Z","createdAt":"2019-08-24T14:15:22Z"}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RAuthUserVO](#schemarauthuservo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 用户关系

<a id="opIdfollowUser"></a>

## POST 关注用户

POST /api/v1/relations/follow/{targetUserId}

关注指定用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|targetUserId|path|integer(int64)| 是 |目标用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdunfollowUser"></a>

## DELETE 取消关注

DELETE /api/v1/relations/follow/{targetUserId}

取消关注指定用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|targetUserId|path|integer(int64)| 是 |目标用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdblockUser"></a>

## POST 拉黑用户

POST /api/v1/relations/block/{targetUserId}

拉黑指定用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|targetUserId|path|integer(int64)| 是 |目标用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdunblockUser"></a>

## DELETE 取消拉黑

DELETE /api/v1/relations/block/{targetUserId}

取消拉黑指定用户

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|targetUserId|path|integer(int64)| 是 |目标用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdbatchUnfollowUsers"></a>

## POST 批量取消关注

POST /api/v1/relations/batch-unfollow

批量取消关注多个用户

> Body 请求参数

```json
[
  0
]
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|array[integer]| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdbatchFollowUsers"></a>

## POST 批量关注

POST /api/v1/relations/batch-follow

批量关注多个用户

> Body 请求参数

```json
[
  0
]
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|array[integer]| 是 |none|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":null}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RVoid](#schemarvoid)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserRelationStatistics"></a>

## GET 获取指定用户关系统计

GET /api/v1/relations/{userId}/statistics

获取指定用户的关系统计数据

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"property1":0,"property2":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RMapStringLong](#schemarmapstringlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserFollowingList"></a>

## GET 获取指定用户关注列表

GET /api/v1/relations/{userId}/following

获取指定用户的关注列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|userId|query|integer(int64)| 否 |用户ID|
|targetUserId|query|integer(int64)| 否 |目标用户ID|
|type|query|integer(int32)| 否 |关系类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"total":0,"rows":[null],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfo](#schematabledatainfo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetUserFollowersList"></a>

## GET 获取指定用户粉丝列表

GET /api/v1/relations/{userId}/followers

获取指定用户的粉丝列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|path|integer(int64)| 是 |用户ID|
|userId|query|integer(int64)| 否 |用户ID|
|targetUserId|query|integer(int64)| 否 |目标用户ID|
|type|query|integer(int32)| 否 |关系类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"total":0,"rows":[null],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfo](#schematabledatainfo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetRelationStatistics"></a>

## GET 获取关系统计

GET /api/v1/relations/statistics

获取用户关系统计数据

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"property1":0,"property2":0}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RMapStringLong](#schemarmapstringlong)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetFollowingList"></a>

## GET 获取关注列表

GET /api/v1/relations/following

获取当前用户的关注列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|query|integer(int64)| 否 |用户ID|
|targetUserId|query|integer(int64)| 否 |目标用户ID|
|type|query|integer(int32)| 否 |关系类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"total":0,"rows":[null],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfo](#schematabledatainfo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetFollowersList"></a>

## GET 获取粉丝列表

GET /api/v1/relations/followers

获取当前用户的粉丝列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|query|integer(int64)| 否 |用户ID|
|targetUserId|query|integer(int64)| 否 |目标用户ID|
|type|query|integer(int32)| 否 |关系类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"total":0,"rows":[null],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfo](#schematabledatainfo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdcheckUserRelation"></a>

## GET 检查用户关系

GET /api/v1/relations/check/{targetUserId}

检查与指定用户的关系状态

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|targetUserId|path|integer(int64)| 是 |目标用户ID|

> 返回示例

> 200 Response

```
{"code":0,"msg":"string","data":{"property1":true,"property2":true}}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[RMapStringBoolean](#schemarmapstringboolean)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

<a id="opIdgetBlockedList"></a>

## GET 获取拉黑列表

GET /api/v1/relations/blocked

获取当前用户的拉黑列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|userId|query|integer(int64)| 否 |用户ID|
|targetUserId|query|integer(int64)| 否 |目标用户ID|
|type|query|integer(int32)| 否 |关系类型|
|beginTime|query|string| 否 |开始时间|
|endTime|query|string| 否 |结束时间|

> 返回示例

> 200 Response

```
{"total":0,"rows":[null],"code":0,"msg":"string"}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[TableDataInfo](#schematabledatainfo)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|string|

# 数据模型

<h2 id="tocS_UserProfileUpdateDTO">UserProfileUpdateDTO</h2>

<a id="schemauserprofileupdatedto"></a>
<a id="schema_UserProfileUpdateDTO"></a>
<a id="tocSuserprofileupdatedto"></a>
<a id="tocsuserprofileupdatedto"></a>

```json
{
  "userId": 0,
  "nickname": "string",
  "avatar": "string",
  "avatarThumbnail": "string",
  "backgroundImage": "string",
  "gender": 3,
  "birthday": "2019-08-24",
  "cityId": 0,
  "location": "string",
  "address": "string",
  "bio": "string",
  "height": 140,
  "weight": 30,
  "realName": "string",
  "wechat": "string",
  "wechatUnlockCondition": 3,
  "onlineStatus": 3,
  "version": 0
}

```

用户资料更新DTO（支持42个字段）

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||用户ID（更新时必填）|
|nickname|string|false|none||用户昵称(1-20字符)|
|avatar|string|false|none||头像URL|
|avatarThumbnail|string|false|none||头像缩略图URL|
|backgroundImage|string|false|none||背景图URL|
|gender|integer(int32)|false|none||性别(1=男,2=女,3=其他,0=未设置)|
|birthday|string(date)|false|none||生日|
|cityId|integer(int64)|false|none||所在城市ID|
|location|string|false|none||位置信息|
|address|string|false|none||详细地址|
|bio|string|false|none||个人简介(0-500字符)|
|height|integer(int32)|false|none||身高(140-200cm)|
|weight|integer(int32)|false|none||体重(30-150kg)|
|realName|string|false|none||真实姓名|
|wechat|string|false|none||微信号(6-20位)|
|wechatUnlockCondition|integer(int32)|false|none||微信解锁条件(0=公开,1=关注后可见,2=付费可见,3=私密)|
|onlineStatus|integer(int32)|false|none||在线状态(0=离线,1=在线,2=忙碌,3=隐身)|
|version|integer(int32)|false|none||乐观锁版本号|

<h2 id="tocS_RVoid">RVoid</h2>

<a id="schemarvoid"></a>
<a id="schema_RVoid"></a>
<a id="tocSrvoid"></a>
<a id="tocsrvoid"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": null
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|any|false|none||数据对象|

<h2 id="tocS_UserUpdateDTO">UserUpdateDTO</h2>

<a id="schemauserupdatedto"></a>
<a id="schema_UserUpdateDTO"></a>
<a id="tocSuserupdatedto"></a>
<a id="tocsuserupdatedto"></a>

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "email": "user@example.com",
  "avatar": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0,
  "version": 0
}

```

用户更新DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|true|none||用户ID|
|username|string|false|none||用户名|
|mobile|string|false|none||手机号|
|nickname|string|false|none||昵称|
|email|string(email)|false|none||邮箱|
|avatar|string|false|none||头像URL|
|realName|string|false|none||真实姓名|
|location|string|false|none||位置信息|
|bio|string|false|none||个人简介|
|status|integer(int32)|false|none||用户状态|
|version|integer(int32)|false|none||版本号（乐观锁）|

<h2 id="tocS_UserOccupationUpdateDTO">UserOccupationUpdateDTO</h2>

<a id="schemauseroccupationupdatedto"></a>
<a id="schema_UserOccupationUpdateDTO"></a>
<a id="tocSuseroccupationupdatedto"></a>
<a id="tocsuseroccupationupdatedto"></a>

```json
{
  "occupationCodes": [
    "string"
  ],
  "keepSortOrder": true
}

```

用户职业更新DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|occupationCodes|[string]|true|none||职业编码列表（最多5个）|
|keepSortOrder|boolean|false|none||是否保留原有排序<br /> true: 保留原有排序，只更新职业列表<br /> false: 按提交顺序重新排序|

<h2 id="tocS_WalletTransferDTO">WalletTransferDTO</h2>

<a id="schemawallettransferdto"></a>
<a id="schema_WalletTransferDTO"></a>
<a id="tocSwallettransferdto"></a>
<a id="tocswallettransferdto"></a>

```json
{
  "toUserId": 0,
  "amount": 0.01,
  "description": "string",
  "paymentPassword": "string"
}

```

钱包转账DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|toUserId|integer(int64)|true|none||接收方用户ID|
|amount|number|true|none||转账金额(元)|
|description|string|false|none||转账说明|
|paymentPassword|string|false|none||支付密码|

<h2 id="tocS_WalletRechargeDTO">WalletRechargeDTO</h2>

<a id="schemawalletrechargedto"></a>
<a id="schema_WalletRechargeDTO"></a>
<a id="tocSwalletrechargedto"></a>
<a id="tocswalletrechargedto"></a>

```json
{
  "amount": 0.01,
  "paymentMethod": "string",
  "description": "string"
}

```

钱包充值DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|amount|number|true|none||充值金额(元)|
|paymentMethod|string|false|none||支付方式|
|description|string|false|none||充值说明|

<h2 id="tocS_RString">RString</h2>

<a id="schemarstring"></a>
<a id="schema_RString"></a>
<a id="tocSrstring"></a>
<a id="tocsrstring"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": "string"
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|string|false|none||数据对象|

<h2 id="tocS_UserAddDTO">UserAddDTO</h2>

<a id="schemauseradddto"></a>
<a id="schema_UserAddDTO"></a>
<a id="tocSuseradddto"></a>
<a id="tocsuseradddto"></a>

```json
{
  "username": "string",
  "mobile": "string",
  "password": "string",
  "nickname": "string",
  "email": "user@example.com",
  "avatar": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0
}

```

用户新增DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||用户名|
|mobile|string|true|none||手机号|
|password|string|true|none||密码|
|nickname|string|false|none||昵称|
|email|string(email)|false|none||邮箱|
|avatar|string|false|none||头像URL|
|realName|string|false|none||真实姓名|
|location|string|false|none||位置信息|
|bio|string|false|none||个人简介|
|status|integer(int32)|false|none||用户状态|

<h2 id="tocS_RListUserStatsVO">RListUserStatsVO</h2>

<a id="schemarlistuserstatsvo"></a>
<a id="schema_RListUserStatsVO"></a>
<a id="tocSrlistuserstatsvo"></a>
<a id="tocsrlistuserstatsvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "userId": 0,
      "followerCount": 0,
      "followingCount": 0,
      "contentCount": 0,
      "totalLikeCount": 0,
      "totalCollectCount": 0,
      "activityOrganizerCount": 0,
      "activityParticipantCount": 0,
      "activitySuccessCount": 0,
      "activityCancelCount": 0,
      "activityOrganizerScore": 0,
      "activitySuccessRate": 0,
      "lastSyncTime": "2019-08-24T14:15:22Z",
      "isActive": true,
      "isPopular": true,
      "isQualityOrganizer": true,
      "followerFollowingRatio": 0
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[[UserStatsVO](#schemauserstatsvo)]|false|none||数据对象|

<h2 id="tocS_UserStatsVO">UserStatsVO</h2>

<a id="schemauserstatsvo"></a>
<a id="schema_UserStatsVO"></a>
<a id="tocSuserstatsvo"></a>
<a id="tocsuserstatsvo"></a>

```json
{
  "userId": 0,
  "followerCount": 0,
  "followingCount": 0,
  "contentCount": 0,
  "totalLikeCount": 0,
  "totalCollectCount": 0,
  "activityOrganizerCount": 0,
  "activityParticipantCount": 0,
  "activitySuccessCount": 0,
  "activityCancelCount": 0,
  "activityOrganizerScore": 0,
  "activitySuccessRate": 0,
  "lastSyncTime": "2019-08-24T14:15:22Z",
  "isActive": true,
  "isPopular": true,
  "isQualityOrganizer": true,
  "followerFollowingRatio": 0
}

```

用户统计VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||用户ID|
|followerCount|integer(int32)|false|none||粉丝数量|
|followingCount|integer(int32)|false|none||关注数量|
|contentCount|integer(int32)|false|none||发布内容数量|
|totalLikeCount|integer(int32)|false|none||获赞总数|
|totalCollectCount|integer(int32)|false|none||被收藏总数|
|activityOrganizerCount|integer(int32)|false|none||发起组局总数|
|activityParticipantCount|integer(int32)|false|none||参与组局总数|
|activitySuccessCount|integer(int32)|false|none||成功完成组局次数|
|activityCancelCount|integer(int32)|false|none||取消组局次数|
|activityOrganizerScore|number|false|none||组局信誉评分（5分制）|
|activitySuccessRate|number|false|none||组局成功率（百分比）|
|lastSyncTime|string(date-time)|false|none||最后同步时间|
|isActive|boolean|false|none||是否为活跃用户|
|isPopular|boolean|false|none||是否为人气用户|
|isQualityOrganizer|boolean|false|none||是否为优质组局者|
|followerFollowingRatio|number|false|none||粉丝关注比|

<h2 id="tocS_UserValidateDTO">UserValidateDTO</h2>

<a id="schemauservalidatedto"></a>
<a id="schema_UserValidateDTO"></a>
<a id="tocSuservalidatedto"></a>
<a id="tocsuservalidatedto"></a>

```json
{
  "username": "string",
  "password": "string",
  "type": "string"
}

```

用户验证DTO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|false|none||用户名或手机号|
|password|string|false|none||密码|
|type|string|false|none||验证类型|

<h2 id="tocS_RBoolean">RBoolean</h2>

<a id="schemarboolean"></a>
<a id="schema_RBoolean"></a>
<a id="tocSrboolean"></a>
<a id="tocsrboolean"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": true
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|boolean|false|none||数据对象|

<h2 id="tocS_AutoRegisterDTO">AutoRegisterDTO</h2>

<a id="schemaautoregisterdto"></a>
<a id="schema_AutoRegisterDTO"></a>
<a id="tocSautoregisterdto"></a>
<a id="tocsautoregisterdto"></a>

```json
{
  "mobile": 13800138001,
  "source": "sms_login",
  "clientType": "web",
  "deviceId": "device_12345"
}

```

自动注册请求

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|mobile|string|true|none||手机号|
|source|string|false|none||注册来源|
|clientType|string|false|none||客户端类型|
|deviceId|string|false|none||设备ID|

<h2 id="tocS_AuthUserVO">AuthUserVO</h2>

<a id="schemaauthuservo"></a>
<a id="schema_AuthUserVO"></a>
<a id="tocSauthuservo"></a>
<a id="tocsauthuservo"></a>

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "avatar": "string",
  "status": 0,
  "roles": [
    "string"
  ],
  "permissions": [
    "string"
  ],
  "lastLoginTime": "2019-08-24T14:15:22Z",
  "createdAt": "2019-08-24T14:15:22Z"
}

```

认证用户VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||用户ID|
|username|string|false|none||用户名|
|mobile|string|false|none||手机号|
|nickname|string|false|none||昵称|
|avatar|string|false|none||头像|
|status|integer(int32)|false|none||用户状态|
|roles|[string]|false|none||角色列表|
|permissions|[string]|false|none||权限列表|
|lastLoginTime|string(date-time)|false|none||最后登录时间|
|createdAt|string(date-time)|false|none||创建时间|

<h2 id="tocS_RAuthUserVO">RAuthUserVO</h2>

<a id="schemarauthuservo"></a>
<a id="schema_RAuthUserVO"></a>
<a id="tocSrauthuservo"></a>
<a id="tocsrauthuservo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "id": 0,
    "username": "string",
    "mobile": "string",
    "nickname": "string",
    "avatar": "string",
    "status": 0,
    "roles": [
      "string"
    ],
    "permissions": [
      "string"
    ],
    "lastLoginTime": "2019-08-24T14:15:22Z",
    "createdAt": "2019-08-24T14:15:22Z"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[AuthUserVO](#schemaauthuservo)|false|none||数据对象|

<h2 id="tocS_RUserProfileVO">RUserProfileVO</h2>

<a id="schemaruserprofilevo"></a>
<a id="schema_RUserProfileVO"></a>
<a id="tocSruserprofilevo"></a>
<a id="tocsruserprofilevo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "userId": 0,
    "nickname": "string",
    "avatar": "string",
    "avatarThumbnail": "string",
    "backgroundImage": "string",
    "gender": 0,
    "genderDesc": "string",
    "birthday": "2019-08-24",
    "age": 0,
    "ageRange": "string",
    "cityId": 0,
    "cityName": "string",
    "location": "string",
    "address": "string",
    "ipLocation": "string",
    "bio": "string",
    "height": 0,
    "weight": 0,
    "bmi": 0.1,
    "bmiLevel": "string",
    "realName": "string",
    "wechat": "string",
    "wechatMasked": "string",
    "wechatUnlockCondition": 0,
    "wechatUnlockDesc": "string",
    "canViewWechat": true,
    "isRealVerified": true,
    "isGodVerified": true,
    "isActivityExpert": true,
    "isVip": true,
    "isVipValid": true,
    "isPopular": true,
    "vipLevel": 0,
    "vipExpireTime": "2019-08-24T14:15:22Z",
    "onlineStatus": 0,
    "onlineStatusDesc": "string",
    "isOnline": true,
    "lastOnlineTime": "2019-08-24T14:15:22Z",
    "profileCompleteness": 0,
    "completenessLevel": "string",
    "isProfileComplete": true,
    "lastEditTime": "2019-08-24T14:15:22Z",
    "occupations": [
      {
        "id": 0,
        "userId": 0,
        "occupationCode": "string",
        "occupationName": "string",
        "category": "string",
        "iconUrl": "string",
        "sortOrder": 0,
        "createdAt": "2019-08-24T14:15:22Z",
        "isPrimary": true
      }
    ],
    "stats": {
      "userId": 0,
      "followerCount": 0,
      "followingCount": 0,
      "contentCount": 0,
      "totalLikeCount": 0,
      "totalCollectCount": 0,
      "activityOrganizerCount": 0,
      "activityParticipantCount": 0,
      "activitySuccessCount": 0,
      "activityCancelCount": 0,
      "activityOrganizerScore": 0,
      "activitySuccessRate": 0,
      "lastSyncTime": "2019-08-24T14:15:22Z",
      "isActive": true,
      "isPopular": true,
      "isQualityOrganizer": true,
      "followerFollowingRatio": 0
    },
    "isFollowed": true,
    "isMutualFollow": true,
    "isBlocked": true,
    "createdAt": "2019-08-24T14:15:22Z",
    "updatedAt": "2019-08-24T14:15:22Z",
    "version": 0
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[UserProfileVO](#schemauserprofilevo)|false|none||数据对象|

<h2 id="tocS_UserOccupationVO">UserOccupationVO</h2>

<a id="schemauseroccupationvo"></a>
<a id="schema_UserOccupationVO"></a>
<a id="tocSuseroccupationvo"></a>
<a id="tocsuseroccupationvo"></a>

```json
{
  "id": 0,
  "userId": 0,
  "occupationCode": "string",
  "occupationName": "string",
  "category": "string",
  "iconUrl": "string",
  "sortOrder": 0,
  "createdAt": "2019-08-24T14:15:22Z",
  "isPrimary": true
}

```

用户职业VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||记录ID|
|userId|integer(int64)|false|none||用户ID|
|occupationCode|string|false|none||职业编码|
|occupationName|string|false|none||职业名称|
|category|string|false|none||职业分类|
|iconUrl|string|false|none||图标URL|
|sortOrder|integer(int32)|false|none||排序顺序|
|createdAt|string(date-time)|false|none||创建时间|
|isPrimary|boolean|false|none||是否为主要职业|

<h2 id="tocS_UserProfileVO">UserProfileVO</h2>

<a id="schemauserprofilevo"></a>
<a id="schema_UserProfileVO"></a>
<a id="tocSuserprofilevo"></a>
<a id="tocsuserprofilevo"></a>

```json
{
  "userId": 0,
  "nickname": "string",
  "avatar": "string",
  "avatarThumbnail": "string",
  "backgroundImage": "string",
  "gender": 0,
  "genderDesc": "string",
  "birthday": "2019-08-24",
  "age": 0,
  "ageRange": "string",
  "cityId": 0,
  "cityName": "string",
  "location": "string",
  "address": "string",
  "ipLocation": "string",
  "bio": "string",
  "height": 0,
  "weight": 0,
  "bmi": 0.1,
  "bmiLevel": "string",
  "realName": "string",
  "wechat": "string",
  "wechatMasked": "string",
  "wechatUnlockCondition": 0,
  "wechatUnlockDesc": "string",
  "canViewWechat": true,
  "isRealVerified": true,
  "isGodVerified": true,
  "isActivityExpert": true,
  "isVip": true,
  "isVipValid": true,
  "isPopular": true,
  "vipLevel": 0,
  "vipExpireTime": "2019-08-24T14:15:22Z",
  "onlineStatus": 0,
  "onlineStatusDesc": "string",
  "isOnline": true,
  "lastOnlineTime": "2019-08-24T14:15:22Z",
  "profileCompleteness": 0,
  "completenessLevel": "string",
  "isProfileComplete": true,
  "lastEditTime": "2019-08-24T14:15:22Z",
  "occupations": [
    {
      "id": 0,
      "userId": 0,
      "occupationCode": "string",
      "occupationName": "string",
      "category": "string",
      "iconUrl": "string",
      "sortOrder": 0,
      "createdAt": "2019-08-24T14:15:22Z",
      "isPrimary": true
    }
  ],
  "stats": {
    "userId": 0,
    "followerCount": 0,
    "followingCount": 0,
    "contentCount": 0,
    "totalLikeCount": 0,
    "totalCollectCount": 0,
    "activityOrganizerCount": 0,
    "activityParticipantCount": 0,
    "activitySuccessCount": 0,
    "activityCancelCount": 0,
    "activityOrganizerScore": 0,
    "activitySuccessRate": 0,
    "lastSyncTime": "2019-08-24T14:15:22Z",
    "isActive": true,
    "isPopular": true,
    "isQualityOrganizer": true,
    "followerFollowingRatio": 0
  },
  "isFollowed": true,
  "isMutualFollow": true,
  "isBlocked": true,
  "createdAt": "2019-08-24T14:15:22Z",
  "updatedAt": "2019-08-24T14:15:22Z",
  "version": 0
}

```

用户资料VO（完整版42字段）

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||none|
|nickname|string|false|none||none|
|avatar|string|false|none||none|
|avatarThumbnail|string|false|none||none|
|backgroundImage|string|false|none||none|
|gender|integer(int32)|false|none||none|
|genderDesc|string|false|none||none|
|birthday|string(date)|false|none||none|
|age|integer(int32)|false|none||none|
|ageRange|string|false|none||none|
|cityId|integer(int64)|false|none||none|
|cityName|string|false|none||none|
|location|string|false|none||none|
|address|string|false|none||none|
|ipLocation|string|false|none||none|
|bio|string|false|none||none|
|height|integer(int32)|false|none||none|
|weight|integer(int32)|false|none||none|
|bmi|number(double)|false|none||none|
|bmiLevel|string|false|none||none|
|realName|string|false|none||none|
|wechat|string|false|none||none|
|wechatMasked|string|false|none||none|
|wechatUnlockCondition|integer(int32)|false|none||none|
|wechatUnlockDesc|string|false|none||none|
|canViewWechat|boolean|false|none||none|
|isRealVerified|boolean|false|none||none|
|isGodVerified|boolean|false|none||none|
|isActivityExpert|boolean|false|none||none|
|isVip|boolean|false|none||none|
|isVipValid|boolean|false|none||none|
|isPopular|boolean|false|none||none|
|vipLevel|integer(int32)|false|none||none|
|vipExpireTime|string(date-time)|false|none||none|
|onlineStatus|integer(int32)|false|none||none|
|onlineStatusDesc|string|false|none||none|
|isOnline|boolean|false|none||none|
|lastOnlineTime|string(date-time)|false|none||none|
|profileCompleteness|integer(int32)|false|none||none|
|completenessLevel|string|false|none||none|
|isProfileComplete|boolean|false|none||none|
|lastEditTime|string(date-time)|false|none||none|
|occupations|[[UserOccupationVO](#schemauseroccupationvo)]|false|none||职业标签列表|
|stats|[UserStatsVO](#schemauserstatsvo)|false|none||统计数据|
|isFollowed|boolean|false|none||是否已关注（当前用户视角）|
|isMutualFollow|boolean|false|none||是否互相关注|
|isBlocked|boolean|false|none||是否拉黑|
|createdAt|string(date-time)|false|none||none|
|updatedAt|string(date-time)|false|none||none|
|version|integer(int32)|false|none||none|

<h2 id="tocS_ProfileCompletenessVO">ProfileCompletenessVO</h2>

<a id="schemaprofilecompletenessvo"></a>
<a id="schema_ProfileCompletenessVO"></a>
<a id="tocSprofilecompletenessvo"></a>
<a id="tocsprofilecompletenessvo"></a>

```json
{
  "userId": 0,
  "currentScore": 0,
  "level": "string",
  "isComplete": true,
  "coreFieldsScore": 0,
  "extendedFieldsScore": 0,
  "suggestions": [
    "string"
  ],
  "completedItems": [
    "string"
  ],
  "remainingScore": 0,
  "percentage": 0,
  "progressColor": "string",
  "message": "string"
}

```

资料完整度VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||用户ID|
|currentScore|integer(int32)|false|none||当前完整度分数（0-100）|
|level|string|false|none||完整度等级（优秀/良好/一般/较差/极差）|
|isComplete|boolean|false|none||是否完整（≥80%）|
|coreFieldsScore|integer(int32)|false|none||核心字段得分（满分50）|
|extendedFieldsScore|integer(int32)|false|none||扩展字段得分（满分50）|
|suggestions|[string]|false|none||完善建议列表|
|completedItems|[string]|false|none||已完成项列表|
|remainingScore|integer(int32)|false|none||距离完整还需多少分|
|percentage|integer(int32)|false|none||完整度百分比（用于进度条显示）|
|progressColor|string|false|none||进度条颜色（success/warning/danger）|
|message|string|false|none||提示消息|

<h2 id="tocS_RProfileCompletenessVO">RProfileCompletenessVO</h2>

<a id="schemarprofilecompletenessvo"></a>
<a id="schema_RProfileCompletenessVO"></a>
<a id="tocSrprofilecompletenessvo"></a>
<a id="tocsrprofilecompletenessvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "userId": 0,
    "currentScore": 0,
    "level": "string",
    "isComplete": true,
    "coreFieldsScore": 0,
    "extendedFieldsScore": 0,
    "suggestions": [
      "string"
    ],
    "completedItems": [
      "string"
    ],
    "remainingScore": 0,
    "percentage": 0,
    "progressColor": "string",
    "message": "string"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[ProfileCompletenessVO](#schemaprofilecompletenessvo)|false|none||数据对象|

<h2 id="tocS_RUserWalletVO">RUserWalletVO</h2>

<a id="schemaruserwalletvo"></a>
<a id="schema_RUserWalletVO"></a>
<a id="tocSruserwalletvo"></a>
<a id="tocsruserwalletvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "userId": 0,
    "balance": "string",
    "balanceFen": 0,
    "available": true,
    "version": 0
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[UserWalletVO](#schemauserwalletvo)|false|none||数据对象|

<h2 id="tocS_UserWalletVO">UserWalletVO</h2>

<a id="schemauserwalletvo"></a>
<a id="schema_UserWalletVO"></a>
<a id="tocSuserwalletvo"></a>
<a id="tocsuserwalletvo"></a>

```json
{
  "userId": 0,
  "balance": "string",
  "balanceFen": 0,
  "available": true,
  "version": 0
}

```

用户钱包VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||用户ID|
|balance|string|false|none||余额(元)|
|balanceFen|integer(int64)|false|none||余额(分)|
|available|boolean|false|none||是否可用|
|version|integer(int32)|false|none||版本号|

<h2 id="tocS_TableDataInfoTransactionVO">TableDataInfoTransactionVO</h2>

<a id="schematabledatainfotransactionvo"></a>
<a id="schema_TableDataInfoTransactionVO"></a>
<a id="tocStabledatainfotransactionvo"></a>
<a id="tocstabledatainfotransactionvo"></a>

```json
{
  "total": 0,
  "rows": [
    {
      "id": 0,
      "userId": 0,
      "amount": "string",
      "amountFen": 0,
      "type": "string",
      "typeDesc": "string",
      "refId": "string",
      "createdAt": "2019-08-24T14:15:22Z",
      "isIncome": true,
      "formattedAmount": "string"
    }
  ],
  "code": 0,
  "msg": "string"
}

```

表格分页数据对象

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|total|integer(int64)|false|none||总记录数|
|rows|[[TransactionVO](#schematransactionvo)]|false|none||列表数据|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|

<h2 id="tocS_TransactionVO">TransactionVO</h2>

<a id="schematransactionvo"></a>
<a id="schema_TransactionVO"></a>
<a id="tocStransactionvo"></a>
<a id="tocstransactionvo"></a>

```json
{
  "id": 0,
  "userId": 0,
  "amount": "string",
  "amountFen": 0,
  "type": "string",
  "typeDesc": "string",
  "refId": "string",
  "createdAt": "2019-08-24T14:15:22Z",
  "isIncome": true,
  "formattedAmount": "string"
}

```

交易记录VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||交易ID|
|userId|integer(int64)|false|none||用户ID|
|amount|string|false|none||交易金额(元)|
|amountFen|integer(int64)|false|none||交易金额(分)|
|type|string|false|none||交易类型|
|typeDesc|string|false|none||交易类型描述|
|refId|string|false|none||关联业务ID|
|createdAt|string(date-time)|false|none||交易时间|
|isIncome|boolean|false|none||是否为收入|
|formattedAmount|string|false|none||格式化金额显示|

<h2 id="tocS_RTransactionVO">RTransactionVO</h2>

<a id="schemartransactionvo"></a>
<a id="schema_RTransactionVO"></a>
<a id="tocSrtransactionvo"></a>
<a id="tocsrtransactionvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "id": 0,
    "userId": 0,
    "amount": "string",
    "amountFen": 0,
    "type": "string",
    "typeDesc": "string",
    "refId": "string",
    "createdAt": "2019-08-24T14:15:22Z",
    "isIncome": true,
    "formattedAmount": "string"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[TransactionVO](#schematransactionvo)|false|none||数据对象|

<h2 id="tocS_RMapStringObject">RMapStringObject</h2>

<a id="schemarmapstringobject"></a>
<a id="schema_RMapStringObject"></a>
<a id="tocSrmapstringobject"></a>
<a id="tocsrmapstringobject"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "property1": null,
    "property2": null
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|object|false|none||数据对象|
|» **additionalProperties**|any|false|none||none|

<h2 id="tocS_RUserDetailVO">RUserDetailVO</h2>

<a id="schemaruserdetailvo"></a>
<a id="schema_RUserDetailVO"></a>
<a id="tocSruserdetailvo"></a>
<a id="tocsruserdetailvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "id": 0,
    "username": "string",
    "mobile": "string",
    "nickname": "string",
    "avatar": "string",
    "email": "string",
    "realName": "string",
    "location": "string",
    "bio": "string",
    "status": 0,
    "statusDesc": "string",
    "createdAt": "2019-08-24T14:15:22Z",
    "version": 0,
    "followed": true,
    "followingCount": 0,
    "followersCount": 0,
    "walletBalance": "string",
    "maskedMobile": "string",
    "maskedEmail": "string"
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[UserDetailVO](#schemauserdetailvo)|false|none||数据对象|

<h2 id="tocS_UserDetailVO">UserDetailVO</h2>

<a id="schemauserdetailvo"></a>
<a id="schema_UserDetailVO"></a>
<a id="tocSuserdetailvo"></a>
<a id="tocsuserdetailvo"></a>

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "avatar": "string",
  "email": "string",
  "realName": "string",
  "location": "string",
  "bio": "string",
  "status": 0,
  "statusDesc": "string",
  "createdAt": "2019-08-24T14:15:22Z",
  "version": 0,
  "followed": true,
  "followingCount": 0,
  "followersCount": 0,
  "walletBalance": "string",
  "maskedMobile": "string",
  "maskedEmail": "string"
}

```

用户详情VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||用户ID|
|username|string|false|none||用户名|
|mobile|string|false|none||手机号|
|nickname|string|false|none||昵称|
|avatar|string|false|none||头像URL|
|email|string|false|none||邮箱|
|realName|string|false|none||真实姓名|
|location|string|false|none||位置信息|
|bio|string|false|none||个人简介|
|status|integer(int32)|false|none||用户状态|
|statusDesc|string|false|none||用户状态描述|
|createdAt|string(date-time)|false|none||注册时间|
|version|integer(int32)|false|none||版本号（乐观锁）|
|followed|boolean|false|none||是否已关注（当前用户视角）|
|followingCount|integer(int64)|false|none||关注数|
|followersCount|integer(int64)|false|none||粉丝数|
|walletBalance|string|false|none||钱包余额（元）|
|maskedMobile|string|false|none||脱敏后的手机号|
|maskedEmail|string|false|none||脱敏后的邮箱|

<h2 id="tocS_RUserStatsVO">RUserStatsVO</h2>

<a id="schemaruserstatsvo"></a>
<a id="schema_RUserStatsVO"></a>
<a id="tocSruserstatsvo"></a>
<a id="tocsruserstatsvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "userId": 0,
    "followerCount": 0,
    "followingCount": 0,
    "contentCount": 0,
    "totalLikeCount": 0,
    "totalCollectCount": 0,
    "activityOrganizerCount": 0,
    "activityParticipantCount": 0,
    "activitySuccessCount": 0,
    "activityCancelCount": 0,
    "activityOrganizerScore": 0,
    "activitySuccessRate": 0,
    "lastSyncTime": "2019-08-24T14:15:22Z",
    "isActive": true,
    "isPopular": true,
    "isQualityOrganizer": true,
    "followerFollowingRatio": 0
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[UserStatsVO](#schemauserstatsvo)|false|none||数据对象|

<h2 id="tocS_RListUserListVO">RListUserListVO</h2>

<a id="schemarlistuserlistvo"></a>
<a id="schema_RListUserListVO"></a>
<a id="tocSrlistuserlistvo"></a>
<a id="tocsrlistuserlistvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": 0,
      "username": "string",
      "mobile": "string",
      "nickname": "string",
      "avatar": "string",
      "status": 0,
      "statusDesc": "string",
      "createdAt": "2019-08-24T14:15:22Z",
      "maskedMobile": "string"
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[[UserListVO](#schemauserlistvo)]|false|none||数据对象|

<h2 id="tocS_UserListVO">UserListVO</h2>

<a id="schemauserlistvo"></a>
<a id="schema_UserListVO"></a>
<a id="tocSuserlistvo"></a>
<a id="tocsuserlistvo"></a>

```json
{
  "id": 0,
  "username": "string",
  "mobile": "string",
  "nickname": "string",
  "avatar": "string",
  "status": 0,
  "statusDesc": "string",
  "createdAt": "2019-08-24T14:15:22Z",
  "maskedMobile": "string"
}

```

用户列表VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||用户ID|
|username|string|false|none||用户名|
|mobile|string|false|none||手机号（脱敏）|
|nickname|string|false|none||昵称|
|avatar|string|false|none||头像URL|
|status|integer(int32)|false|none||用户状态|
|statusDesc|string|false|none||用户状态描述|
|createdAt|string(date-time)|false|none||注册时间|
|maskedMobile|string|false|none||脱敏后的手机号|

<h2 id="tocS_RMapStringLong">RMapStringLong</h2>

<a id="schemarmapstringlong"></a>
<a id="schema_RMapStringLong"></a>
<a id="tocSrmapstringlong"></a>
<a id="tocsrmapstringlong"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "property1": 0,
    "property2": 0
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|object|false|none||数据对象|
|» **additionalProperties**|integer(int64)|false|none||none|

<h2 id="tocS_TableDataInfo">TableDataInfo</h2>

<a id="schematabledatainfo"></a>
<a id="schema_TableDataInfo"></a>
<a id="tocStabledatainfo"></a>
<a id="tocstabledatainfo"></a>

```json
{
  "total": 0,
  "rows": [
    null
  ],
  "code": 0,
  "msg": "string"
}

```

表格分页数据对象

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|total|integer(int64)|false|none||总记录数|
|rows|[any]|false|none||列表数据|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|

<h2 id="tocS_RMapStringBoolean">RMapStringBoolean</h2>

<a id="schemarmapstringboolean"></a>
<a id="schema_RMapStringBoolean"></a>
<a id="tocSrmapstringboolean"></a>
<a id="tocsrmapstringboolean"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": {
    "property1": true,
    "property2": true
  }
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|object|false|none||数据对象|
|» **additionalProperties**|boolean|false|none||none|

<h2 id="tocS_RListLong">RListLong</h2>

<a id="schemarlistlong"></a>
<a id="schema_RListLong"></a>
<a id="tocSrlistlong"></a>
<a id="tocsrlistlong"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    0
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[integer]|false|none||数据对象|

<h2 id="tocS_RInteger">RInteger</h2>

<a id="schemarinteger"></a>
<a id="schema_RInteger"></a>
<a id="tocSrinteger"></a>
<a id="tocsrinteger"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": 0
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|integer(int32)|false|none||数据对象|

<h2 id="tocS_RListUserOccupationVO">RListUserOccupationVO</h2>

<a id="schemarlistuseroccupationvo"></a>
<a id="schema_RListUserOccupationVO"></a>
<a id="tocSrlistuseroccupationvo"></a>
<a id="tocsrlistuseroccupationvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "id": 0,
      "userId": 0,
      "occupationCode": "string",
      "occupationName": "string",
      "category": "string",
      "iconUrl": "string",
      "sortOrder": 0,
      "createdAt": "2019-08-24T14:15:22Z",
      "isPrimary": true
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[[UserOccupationVO](#schemauseroccupationvo)]|false|none||数据对象|

<h2 id="tocS_OccupationDictVO">OccupationDictVO</h2>

<a id="schemaoccupationdictvo"></a>
<a id="schema_OccupationDictVO"></a>
<a id="tocSoccupationdictvo"></a>
<a id="tocsoccupationdictvo"></a>

```json
{
  "code": "string",
  "name": "string",
  "category": "string",
  "iconUrl": "string",
  "sortOrder": 0,
  "status": 0,
  "statusDesc": "string",
  "createdAt": "2019-08-24T14:15:22Z",
  "hasIcon": true
}

```

职业字典VO

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|string|false|none||职业编码|
|name|string|false|none||职业名称|
|category|string|false|none||职业分类|
|iconUrl|string|false|none||图标URL|
|sortOrder|integer(int32)|false|none||排序顺序|
|status|integer(int32)|false|none||状态|
|statusDesc|string|false|none||状态描述|
|createdAt|string(date-time)|false|none||创建时间|
|hasIcon|boolean|false|none||是否有图标|

<h2 id="tocS_RListOccupationDictVO">RListOccupationDictVO</h2>

<a id="schemarlistoccupationdictvo"></a>
<a id="schema_RListOccupationDictVO"></a>
<a id="tocSrlistoccupationdictvo"></a>
<a id="tocsrlistoccupationdictvo"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    {
      "code": "string",
      "name": "string",
      "category": "string",
      "iconUrl": "string",
      "sortOrder": 0,
      "status": 0,
      "statusDesc": "string",
      "createdAt": "2019-08-24T14:15:22Z",
      "hasIcon": true
    }
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[[OccupationDictVO](#schemaoccupationdictvo)]|false|none||数据对象|

<h2 id="tocS_RListString">RListString</h2>

<a id="schemarliststring"></a>
<a id="schema_RListString"></a>
<a id="tocSrliststring"></a>
<a id="tocsrliststring"></a>

```json
{
  "code": 0,
  "msg": "string",
  "data": [
    "string"
  ]
}

```

响应信息主体

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer(int32)|false|none||消息状态码|
|msg|string|false|none||消息内容|
|data|[string]|false|none||数据对象|

