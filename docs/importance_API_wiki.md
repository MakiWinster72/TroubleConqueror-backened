# 错题重要性管理 API 接口文档

本文档说明错题重要性管理相关功能的后端 API 接口。

## 1. 核心 API 接口

所有错题相关的 API 都以 `/trouble/question` 为前缀。

---

## 2. 查询错题列表（支持重要性筛选）

### 2.1 接口信息

- **功能**: 查询当前登录用户的错题列表，支持按重要性等级筛选
- **HTTP 方法**: `GET`
- **URL**: `/trouble/question/list`
- **权限**: `trouble:question:list`

### 2.2 查询参数 (Query Parameters)

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| pageNum | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认10 | 10 |
| questionContent | String | 否 | 题目内容（模糊查询） | "数学" |
| questionType | String | 否 | 题目类型 | "选择题" |
| tags | String | 否 | 标签（模糊查询） | "函数" |
| **importance** | **Integer** | **否** | **重要性等级（1=低，2=中，3=高）** | **2** |
| status | String | 否 | 状态（0=正常，1=删除） | "0" |

### 2.3 响应示例

```json
{
  "total": 100,
  "rows": [
    {
      "questionId": 1,
      "userId": 1,
      "questionContent": "这是一道数学题",
      "questionImages": "image1.jpg,image2.jpg",
      "answerContent": "答案是A",
      "answerImages": null,
      "questionType": "选择题",
      "tags": "数学,函数",
      "importance": 2,
      "status": "0",
      "isFavorite": 0,
      "createTime": "2024-01-01 10:00:00",
      "updateTime": "2024-01-01 10:00:00"
    }
  ],
  "code": 200,
  "msg": "查询成功"
}
```

### 2.4 调用示例

**JavaScript (Vue/Axios)**:
```javascript
import request from '@/utils/request'

// 查询所有错题
const queryAll = {
  pageNum: 1,
  pageSize: 10
}

// 查询高重要性错题
const queryHigh = {
  pageNum: 1,
  pageSize: 10,
  importance: 3  // 3=高重要性
}

// 查询中等重要性错题
const queryMedium = {
  pageNum: 1,
  pageSize: 10,
  importance: 2  // 2=中等重要性
}

// 查询低重要性错题
const queryLow = {
  pageNum: 1,
  pageSize: 10,
  importance: 1  // 1=低重要性
}

request({
  url: '/trouble/question/list',
  method: 'get',
  params: queryHigh
}).then(response => {
  console.log("查询结果", response);
})
```

**cURL**:
```bash
# 查询所有错题
curl -X GET "http://localhost:8080/trouble/question/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 查询高重要性错题
curl -X GET "http://localhost:8080/trouble/question/list?pageNum=1&pageSize=10&importance=3" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 3. 更新错题重要性

### 3.1 接口信息

- **功能**: 更新指定错题的重要性等级
- **HTTP 方法**: `PUT`
- **URL**: `/trouble/question/importance/{questionId}`
- **权限**: `trouble:question:edit`

### 3.2 路径参数 (Path Variables)

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| questionId | Long | 是 | 错题ID | 1 |

### 3.3 请求参数 (Request Parameters)

| 参数名 | 类型 | 必填 | 说明 | 取值范围 | 示例值 |
|--------|------|------|------|----------|--------|
| importance | Integer | 是 | 重要性等级 | 1, 2, 3 | 3 |

**重要性等级说明**:
- `1` = 低重要性
- `2` = 中等重要性（默认值）
- `3` = 高重要性

### 3.4 请求示例

**JavaScript (Vue/Axios)**:
```javascript
import request from '@/utils/request'

// 将错题ID为1的错题设置为高重要性
request({
  url: '/trouble/question/importance/1',
  method: 'put',
  params: {
    importance: 3  // 3=高重要性
  }
}).then(response => {
  console.log("更新成功", response);
}).catch(error => {
  console.error("更新失败", error);
})
```

**cURL**:
```bash
curl -X PUT "http://localhost:8080/trouble/question/importance/1?importance=3" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

### 3.5 响应示例

**成功响应**:
```json
{
  "msg": "更新重要性成功",
  "code": 200
}
```

**失败响应**:
```json
{
  "msg": "重要性等级必须为1（低）、2（中）或3（高）",
  "code": 500
}
```

或

```json
{
  "msg": "错题不存在",
  "code": 500
}
```

或

```json
{
  "msg": "无权限修改该错题",
  "code": 500
}
```

### 3.6 错误码说明

| 错误信息 | 说明 | 解决方案 |
|---------|------|---------|
| 重要性等级必须为1（低）、2（中）或3（高） | importance 参数不在有效范围内 | 确保 importance 值为 1、2 或 3 |
| 错题不存在 | 指定的 questionId 不存在 | 检查 questionId 是否正确 |
| 无权限修改该错题 | 错题不属于当前登录用户 | 确保只能修改自己的错题 |
| 更新重要性失败 | 数据库更新失败 | 检查数据库连接和权限 |

---

## 4. 修改错题（支持更新重要性）

### 4.1 接口信息

- **功能**: 修改错题信息，包括重要性等级
- **HTTP 方法**: `PUT`
- **URL**: `/trouble/question`
- **权限**: `trouble:question:edit`

### 4.2 请求体 (Request Body)

**JSON 格式**:
```json
{
  "questionId": 1,
  "questionContent": "修改后的题目内容",
  "answerContent": "修改后的答案",
  "questionType": "选择题",
  "tags": "数学,函数",
  "importance": 3,
  "remark": "备注信息"
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| questionId | Long | 是 | 错题ID | 1 |
| questionContent | String | 否 | 题目内容 | "这是一道数学题" |
| answerContent | String | 否 | 答案内容 | "答案是A" |
| questionType | String | 否 | 题目类型 | "选择题" |
| tags | String | 否 | 标签 | "数学,函数" |
| **importance** | **Integer** | **否** | **重要性等级（1=低，2=中，3=高）** | **3** |
| remark | String | 否 | 备注 | "重要题目" |

### 4.3 调用示例

**JavaScript (Vue/Axios)**:
```javascript
import request from '@/utils/request'

const updateData = {
  questionId: 1,
  questionContent: "修改后的题目内容",
  importance: 3  // 同时更新重要性
}

request({
  url: '/trouble/question',
  method: 'put',
  data: updateData
}).then(response => {
  console.log("修改成功", response);
})
```

**cURL**:
```bash
curl -X PUT "http://localhost:8080/trouble/question" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 1,
    "questionContent": "修改后的题目内容",
    "importance": 3
  }'
```

---

## 5. 新增错题（支持设置重要性）

### 5.1 接口信息

- **功能**: 创建新的错题记录，可设置重要性等级
- **HTTP 方法**: `POST`
- **URL**: `/trouble/question`
- **权限**: `trouble:question:add`

### 5.2 请求体 (Request Body)

**JSON 格式**:
```json
{
  "questionContent": "这是一道新的错题",
  "questionImages": "image1.jpg,image2.jpg",
  "answerContent": "这是答案",
  "answerImages": "answer1.jpg",
  "questionType": "解答题",
  "tags": "数学,函数",
  "importance": 2,
  "remark": "备注信息"
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| questionContent | String | 是 | 题目内容 | - |
| questionImages | String | 否 | 题目图片（多个用逗号分隔） | null |
| answerContent | String | 否 | 答案内容 | null |
| answerImages | String | 否 | 答案图片（多个用逗号分隔） | null |
| questionType | String | 否 | 题目类型 | "未区分" |
| tags | String | 否 | 标签（多个用逗号分隔） | null |
| **importance** | **Integer** | **否** | **重要性等级（1=低，2=中，3=高）** | **2** |
| remark | String | 否 | 备注 | null |

### 5.3 调用示例

**JavaScript (Vue/Axios)**:
```javascript
import request from '@/utils/request'

const newQuestion = {
  questionContent: "这是一道新的错题",
  answerContent: "这是答案",
  questionType: "解答题",
  tags: "数学,函数",
  importance: 3  // 设置为高重要性
}

request({
  url: '/trouble/question',
  method: 'post',
  data: newQuestion
}).then(response => {
  console.log("添加成功", response);
})
```

---

## 6. 重要性等级说明

| 等级值 | 等级名称 | 说明 |
|--------|---------|------|
| 1 | 低重要性 | 一般错题，优先级较低 |
| 2 | 中等重要性 | 普通错题，默认重要性等级 |
| 3 | 高重要性 | 重要错题，需要重点关注 |

---

## 7. 注意事项

1. **权限验证**: 所有接口都需要用户登录，且只能操作属于当前用户的错题
2. **参数验证**: 更新重要性接口会验证 importance 参数必须在 1-3 之间
3. **默认值**: 新增错题时，如果不指定 importance，默认为 2（中等重要性）
4. **筛选功能**: 查询接口支持按 importance 筛选，可以与其他筛选条件组合使用
5. **数据完整性**: 修改错题时，可以只更新部分字段（包括 importance），未提供的字段不会被修改

---

## 8. 更新日志

### Version 1.0 (2024-01-XX)
- 新增 `importance` 字段到错题表
- 查询接口支持按重要性筛选
- 新增更新重要性专用接口
- 修改和新增接口支持设置重要性

