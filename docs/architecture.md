# SynthraSim 工业仿真平台 - 项目架构文档

## 1. 技术架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                     前端（Vue/React）                         │
│              通过 HTTP + JWT Token 调用后端接口                │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP请求
                           │ Header: Authorization: Bearer {token}
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  synthrasim-admin（启动模块）                  │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │ Controller层 │  │ 验证码接口    │  │ 代码生成接口       │  │
│  │ (RESTful API)│  │ /captchaImage│  │ /tool/gen/*       │  │
│  └──────┬──────┘  └──────────────┘  └───────────────────┘  │
│         │                                                   │
│         │ 调用                                               │
│         ▼                                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │          synthrasim-framework（框架模块）              │    │
│  │                                                     │    │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────┐  │    │
│  │  │Security配置   │  │JWT过滤器     │  │权限切面    │  │    │
│  │  │(白名单/CORS)  │  │(Token解析)   │  │(@Requires │  │    │
│  │  │              │  │             │  │Permissions)│  │    │
│  │  └──────────────┘  └─────────────┘  └───────────┘  │    │
│  │  ┌──────────────┐  ┌─────────────┐  ┌───────────┐  │    │
│  │  │MyBatisPlus配置│  │Redis配置     │  │Knife4j配置│  │    │
│  │  │(分页/自动填充) │  │(序列化)      │  │(API文档)  │  │    │
│  │  └──────────────┘  └─────────────┘  └───────────┘  │    │
│  └─────────────────────────┬───────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           synthrasim-system（系统模块）                │    │
│  │                                                     │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │    │
│  │  │ Domain层  │  │ Mapper层 │  │ Service层         │  │    │
│  │  │ (实体类)  │  │ (数据访问)│  │ (业务逻辑)        │  │    │
│  │  └──────────┘  └──────────┘  └──────────────────┘  │    │
│  └─────────────────────────┬───────────────────────────┘    │
│                            │                                │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │           synthrasim-common（公共模块）                │    │
│  │                                                     │    │
│  │  常量定义 │ 工具类 │ 异常处理 │ 基础实体 │ Redis工具   │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
         ┌────────┐  ┌────────┐  ┌──────────┐
         │ MySQL  │  │ Redis  │  │ 文件存储  │
         │ :8086  │  │ :6379  │  │ (预留)    │
         └────────┘  └────────┘  └──────────┘
```

## 2. 模块依赖关系

```
synthrasim-admin（启动入口）
  ├── synthrasim-framework（框架配置）
  │     ├── synthrasim-system（业务逻辑）
  │     │     └── synthrasim-common（公共工具）
  │     └── MySQL驱动 / Druid连接池
  └── synthrasim-generator（代码生成）
        └── synthrasim-framework
```

**依赖规则：上层可以依赖下层，下层不能反向依赖上层。**

---

## 3. 各模块详细说明

### 3.1 synthrasim-common（公共模块）

**职责：** 提供全项目共享的基础设施，不包含任何业务逻辑。

| 包路径 | 内容 | 说明 |
|--------|------|------|
| `annotation/` | `@RequiresPermissions`、`@Log` | 自定义注解，标注在Controller方法上 |
| `constant/` | `Constants`、`HttpStatus` | 系统常量（Token前缀、Redis键名、状态码等） |
| `core/domain/` | `AjaxResult`、`BaseEntity` | 统一响应封装、实体基类（公共字段自动填充） |
| `core/domain/model/` | `LoginBody`、`RegisterBody` | 请求体DTO（登录/注册表单数据） |
| `core/page/` | `TableDataInfo` | 分页查询统一响应结构 |
| `core/redis/` | `RedisCache` | Redis操作工具（set/get/delete/expire） |
| `enums/` | `UserStatus` | 用户状态枚举（启用/禁用） |
| `exception/` | `ServiceException`、`GlobalExceptionHandler` | 业务异常 + 全局异常拦截 |
| `utils/` | `SecurityUtils`、`StringUtils`、`ServletUtils`、`IpUtils` | 工具类集合 |

**关键类解释：**

**AjaxResult** — 所有接口的统一返回格式：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```
- `AjaxResult.success(data)` → code=200
- `AjaxResult.error("错误信息")` → code=500
- `AjaxResult.error(401, "未认证")` → 自定义状态码

**BaseEntity** — 所有业务实体的父类，自动管理这4个字段：
```
create_by   → INSERT时自动填充当前用户ID
create_time → INSERT时自动填充当前时间
update_by   → INSERT/UPDATE时自动填充
update_time → INSERT/UPDATE时自动填充
```

**RedisCache** — 对RedisTemplate的简化封装：
```java
redisCache.setCacheObject("key", value, 30, TimeUnit.MINUTES); // 写入，30分钟过期
redisCache.getCacheObject("key");                               // 读取
redisCache.deleteObject("key");                                 // 删除
```

---

### 3.2 synthrasim-system（系统模块）

**职责：** 核心业务实体定义、数据库访问、业务逻辑处理。

#### Domain层（实体类，映射数据库表）

| 实体类 | 数据库表 | 说明 |
|--------|----------|------|
| `SysUser` | sys_user | 用户信息（用户名、密码、姓名、邮箱、手机等） |
| `SysRole` | sys_role | 角色（admin超级管理员、user普通用户等） |
| `SysUserRole` | sys_user_role | 用户-角色多对多关联 |
| `SysMenu` | sys_menu | 菜单/权限（菜单名称、权限标识 perms、菜单类型 M/C/F 等） |
| `SysRoleMenu` | sys_role_menu | 角色-菜单/权限多对多关联（某角色拥有某权限） |
| `SysOrganization` | sys_organization | 组织机构树（公司→部门→科室） |
| `SysLoginLog` | sys_login_log | 登录日志（操作类型、IP、时间等） |

#### Mapper层（数据访问，继承MyBatisPlus BaseMapper）

继承 `BaseMapper<T>` 后**自动获得**以下方法，无需手写SQL：
```java
insert(entity)              // 插入
deleteById(id)              // 按ID删除
updateById(entity)          // 按ID更新
selectById(id)              // 按ID查询
selectList(wrapper)         // 条件查询列表
selectPage(page, wrapper)   // 分页查询
```

自定义方法（用`@Select`注解写SQL）：
```java
// SysUserMapper - 支持用户名/手机号/邮箱三种方式登录
SysUser selectUserByUsername(String username);

// SysRoleMapper - 查询用户拥有的所有角色
List<SysRole> selectRolesByUserId(Long userId);

// SysMenuMapper - 根据用户ID查询该用户拥有的所有权限标识（用于登录时加载权限）
List<String> selectPermsByUserId(Long userId);  // 联查 sys_user_role → sys_role_menu → sys_menu.perms
```

#### Service层（业务逻辑）

| Service | 核心方法 | 说明 |
|---------|----------|------|
| `ISysUserService` | `selectUserByUsername()` | 登录时查询用户 |
| | `checkUsernameUnique()` | 注册时校验用户名唯一 |
| | `registerUser()` | 注册新用户 |
| | `updateUserProfile()` | 修改个人资料 |
| | `resetUserPwd()` | 修改密码 |
| | `insertUserAuth()` | 给用户分配角色 |
| `ISysRoleService` | `selectRolesByUserId()` | 查询用户角色 |
| | `selectRoleCodesByUserId()` | 获取角色编码集合（权限判断用） |
| `ISysLoginLogService` | `insertLoginLog()` | 记录登录日志 |
| | `selectLoginLogPage()` | 分页查询登录日志 |

---

### 3.3 synthrasim-framework（框架模块）

**职责：** 安全认证、全局配置、基础设施。**这是整个项目最核心的模块。**

#### 3.3.1 认证流程（JWT + Redis）

```
【登录】POST /login
  ①验证码校验 → ②Spring Security认证（查用户→校状态→加载权限→比对密码）
  → ③记录登录日志(sys_login_log, operationType=1)
  → ④生成Token(UUID存Redis + JWT返回前端)

【登出】POST /logout
  ①记录注销日志(sys_login_log, operationType=2) → ②删除Redis缓存 → ③返回成功

【后续请求】
  JwtAuthenticationFilter → 提取JWT → 解析UUID → Redis取LoginUser → 放入SecurityContext
```

#### 3.3.2 Security白名单

| 路径 | 说明 |
|------|------|
| `POST /login` | 用户登录 |
| `POST /register` | 用户注册 |
| `GET /captchaImage` | 获取验证码 |
| `/doc.html`、`/swagger-*`、`/v2/**`、`/webjars/**` | Knife4j接口文档 |
| `/druid/**` | Druid数据库监控 |

**其他所有接口 → 必须在Header中携带 `Authorization: Bearer {token}`。**

#### 3.3.3 权限校验注解

```java
@RequiresPermissions("system:user:list")                                                // 需要单个权限
@RequiresPermissions(value = {"system:user:add", "system:user:edit"}, logical = Logical.OR)  // 满足任一即可
```

admin角色拥有 `*:*:*` 通配权限，跳过所有校验。

#### 3.3.4 权限数据模型（sys_menu / sys_role_menu）

权限数据来自两张表及关联关系：

| 表名 | 说明 |
|------|------|
| **sys_menu** | 菜单/权限主表。核心字段：`menu_name`（名称）、`perms`（权限标识，如 `system:user:list`）、`menu_type`（M=目录/C=菜单/F=按钮）、`parent_id`（父菜单）、`path`（前端路由）等。权限校验时主要使用 `menu_type='F'` 的记录及其 `perms` 字段。 |
| **sys_role_menu** | 角色-菜单多对多中间表。字段：`role_id`、`menu_id`。一条记录表示「某角色拥有某菜单/权限」。 |

**权限查询链路（RBAC）：**

```
用户ID → sys_user_role（用户拥有哪些角色）
       → sys_role_menu（这些角色拥有哪些菜单/权限）
       → sys_menu（取 perms 字段，且 menu_type='F'、status=1）
       → 得到权限标识集合，如 ["system:user:list", "system:user:add"]
```

**与登录流程的衔接：**

- 登录时 `UserDetailsServiceImpl.loadUserByUsername()` 会调用 `SysMenuMapper.selectPermsByUserId(userId)`，按上述链路查出当前用户的所有权限标识。
- 若用户拥有 `admin` 角色，则直接赋予 `*:*:*`，不再查库。
- 得到的权限集合写入 `LoginUser.setPermissions()`，再经 `TokenService` 存入 Redis；后续请求由 `PermissionAspect` 根据 `@RequiresPermissions` 与 `LoginUser.getPermissions()` 做比对。

**权限标识规范（perms）：** 格式为 `{模块}:{业务}:{操作}`，例如：`system:user:list`、`system:user:add`、`system:role:list`。与 Controller 方法上的 `@RequiresPermissions("system:user:list")` 一一对应。

#### 3.3.5 配置类汇总

| 配置类 | 功能 |
|--------|------|
| `SecurityConfig` | Security过滤器链、白名单、CORS跨域、密码编码器 |
| `MybatisPlusConfig` | 分页插件、createTime/updateTime自动填充 |
| `RedisConfig` | Redis序列化（Key用String，Value用JSON） |
| `Knife4jConfig` | API文档分组、Token认证配置 |
| `KaptchaConfig` | 验证码图片样式（尺寸、字体、字符数等） |

---

### 3.4 synthrasim-admin（启动模块）— API接口完整清单

**职责：** Controller层 + Application入口 + 配置文件。最终打包为可执行JAR。

---

#### 3.4.1 用户认证（无需Token）

##### POST /login — 用户登录

| 项 | 内容 |
|----|------|
| 认证 | **不需要Token** |
| 请求体 | `application/json` |

```json
{
  "username": "admin",
  "password": "admin123",
  "code": "",
  "uuid": ""
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（支持用户名/手机号/邮箱） |
| password | String | 是 | 密码（明文，后端BCrypt比对） |
| code | String | 否 | 验证码（留空跳过验证码校验） |
| uuid | String | 否 | 验证码标识（与code配对使用，留空跳过） |

**成功响应：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**失败响应：**
```json
{ "code": 500, "msg": "用户名或密码错误" }
```

**副作用：** 写入 `sys_login_log` 表（成功和失败都记录，operationType=1）。

---

##### POST /register — 用户注册

| 项 | 内容 |
|----|------|
| 认证 | **不需要Token** |
| 请求体 | `application/json` |

```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13900001111",
  "code": "",
  "uuid": ""
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一，不可与已有用户重复） |
| password | String | 是 | 密码（明文，后端BCrypt加密存储） |
| realName | String | 否 | 真实姓名 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 手机号 |
| code | String | 否 | 验证码（留空跳过） |
| uuid | String | 否 | 验证码标识（留空跳过） |

**成功响应：**
```json
{ "code": 200, "msg": "注册成功" }
```

---

##### GET /captchaImage — 获取验证码

| 项 | 内容 |
|----|------|
| 认证 | **不需要Token** |
| 请求参数 | 无 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "uuid": "a1b2c3d4e5f6...",
  "img": "/9j/4AAQSkZJRg..."
}
```

| 字段 | 说明 |
|------|------|
| uuid | 验证码唯一标识，登录时原样回传 |
| img | 验证码图片的Base64编码，前端用 `<img src="data:image/jpg;base64,${img}">` 展示 |

---

##### POST /logout — 退出登录

| 项 | 内容 |
|----|------|
| 认证 | **需要Token**（Header中携带） |
| 请求体 | 无 |

**成功响应：**
```json
{ "code": 200, "msg": "退出成功" }
```

**副作用：** 写入 `sys_login_log` 表（operationType=2），删除Redis中的Token缓存。

---

##### GET /getInfo — 获取当前登录用户信息

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求参数 | 无 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "user": {
    "id": 1,
    "username": "admin",
    "realName": "超级管理员",
    "avatar": null,
    "email": "admin@synthrasim.com",
    "phone": "13800000000",
    "officePhone": null,
    "workLocation": null,
    "orgId": 1,
    "status": 1
  },
  "roles": ["admin"],
  "permissions": ["*:*:*"]
}
```

> **permissions 来源：** 登录时由 `UserDetailsServiceImpl` 根据用户角色，通过 `sys_user_role` → `sys_role_menu` → `sys_menu.perms` 查询得到；admin 角色固定为 `["*:*:*"]`。

---

#### 3.4.2 个人中心（需Token）

##### GET /system/user/profile/info — 获取个人资料

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求参数 | 无 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "超级管理员",
    "email": "admin@synthrasim.com",
    "phone": "13800000000",
    "officePhone": null,
    "workLocation": null,
    "orgId": 1,
    "status": 1
  }
}
```

---

##### PUT /system/user/profile/update — 修改个人资料

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求体 | `application/json` |

```json
{
  "username": "admin_new",
  "realName": "超级管理员改名",
  "email": "newemail@example.com",
  "phone": "13900009999",
  "officePhone": "010-12345678",
  "workLocation": "北京市海淀区"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 否 | 用户名 |
| realName | String | 否 | 真实姓名 |
| email | String | 否 | 电子邮箱（校验唯一性） |
| phone | String | 否 | 手机号码（校验唯一性） |
| officePhone | String | 否 | 办公电话 |
| workLocation | String | 否 | 工作地 |

> **更新策略：只更新实际传入的非空字段。** 未传入或传空字符串的字段保持数据库原值不变，不会被置空。例如只传 `{"realName":"新名字"}`，则只修改姓名，其他字段原封不动。

**成功响应：**
```json
{ "code": 200, "msg": "修改成功" }
```

---

##### PUT /system/user/profile/updatePwd — 修改密码

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求参数 | Query参数（拼在URL上） |

```
PUT /system/user/profile/updatePwd?oldPassword=admin123&newPassword=newpass456
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码（用于校验身份） |
| newPassword | String | 是 | 新密码（不能与旧密码相同） |

**成功响应：**
```json
{ "code": 200, "msg": "修改成功" }
```

**失败响应：**
```json
{ "code": 500, "msg": "修改密码失败，旧密码错误" }
{ "code": 500, "msg": "新密码不能与旧密码相同" }
```

---

##### PUT /system/user/profile/avatar — 修改头像

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求参数 | Query参数 |

```
PUT /system/user/profile/avatar?avatar=https://example.com/avatar/user1.jpg
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| avatar | String | 是 | 头像URL地址 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "修改成功",
  "imgUrl": "https://example.com/avatar/user1.jpg"
}
```

---

#### 3.4.3 用户管理（需Token + 权限）

##### GET /system/user/list — 查询用户列表（分页）

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:list` |
| 请求参数 | Query参数 |

```
GET /system/user/list?pageNum=1&pageSize=10&username=zhang&realName=张&status=1
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| username | String | 否 | null | 用户名（模糊查询） |
| realName | String | 否 | null | 姓名（模糊查询） |
| status | Integer | 否 | null | 状态筛选：0=禁用，1=启用 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "total": 2,
  "rows": [
    {
      "id": 1,
      "username": "admin",
      "realName": "超级管理员",
      "email": "admin@synthrasim.com",
      "phone": "13800000000",
      "orgId": 1,
      "status": 1,
      "createTime": "2026-03-09 10:00:00"
    }
  ]
}
```

---

##### GET /system/user/{userId} — 查询用户详情

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:query` |
| 路径参数 | `userId` — 用户ID |

```
GET /system/user/1
```

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "超级管理员",
    "email": "admin@synthrasim.com",
    "roles": [
      { "id": 1, "roleName": "超级管理员", "roleCode": "admin" }
    ]
  }
}
```

---

##### POST /system/user — 新增用户

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:add` |
| 请求体 | `application/json` |

```json
{
  "username": "lisi",
  "password": "123456",
  "realName": "李四",
  "email": "lisi@example.com",
  "phone": "13700001234",
  "officePhone": "010-87654321",
  "workLocation": "上海市浦东新区",
  "orgId": 2,
  "status": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码（明文，后端加密存储） |
| realName | String | 否 | 真实姓名 |
| email | String | 否 | 电子邮箱 |
| phone | String | 否 | 手机号码 |
| officePhone | String | 否 | 办公电话 |
| workLocation | String | 否 | 工作地 |
| orgId | Long | 否 | 所属组织机构ID |
| status | Integer | 否 | 账号状态：0=禁用，1=启用（默认1） |

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

##### PUT /system/user — 修改用户

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:edit` |
| 请求体 | `application/json` |

```json
{
  "id": 2,
  "realName": "张三丰",
  "email": "zsf@example.com",
  "phone": "13800005555",
  "officePhone": "021-11112222",
  "workLocation": "深圳市南山区",
  "orgId": 3,
  "status": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID（指定要修改哪个用户） |
| realName | String | 否 | 真实姓名 |
| email | String | 否 | 电子邮箱（校验唯一） |
| phone | String | 否 | 手机号码（校验唯一） |
| officePhone | String | 否 | 办公电话 |
| workLocation | String | 否 | 工作地 |
| orgId | Long | 否 | 组织机构ID |
| status | Integer | 否 | 状态 |

> 注意：password字段即使传入也会被后端忽略（置null），修改密码请用专用接口。

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

##### DELETE /system/user/{userIds} — 删除用户

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:remove` |
| 路径参数 | `userIds` — 用户ID（支持多个，逗号分隔） |

```
DELETE /system/user/3
DELETE /system/user/3,4,5
```

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

> 逻辑删除：将 `is_deleted` 标记为1，不物理删除数据。

---

##### PUT /system/user/resetPwd — 重置用户密码

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:resetPwd` |
| 请求体 | `application/json` |

```json
{
  "id": 2,
  "password": "newpass123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 用户ID |
| password | String | 是 | 新密码（明文，后端加密存储） |

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

##### PUT /system/user/authRole — 给用户分配角色

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:user:edit` |
| 请求参数 | Query参数 |

```
PUT /system/user/authRole?userId=2&roleIds=1,2
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| roleIds | Long[] | 是 | 角色ID数组（会先清除用户旧角色再重新分配） |

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

#### 3.4.4 角色管理（需Token + 权限）

##### GET /system/role/list — 查询角色列表（分页）

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:role:list` |
| 请求参数 | Query参数 |

```
GET /system/role/list?pageNum=1&pageSize=10
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "total": 2,
  "rows": [
    { "id": 1, "roleName": "超级管理员", "roleCode": "admin", "description": "拥有系统所有权限", "status": 1 },
    { "id": 2, "roleName": "普通用户", "roleCode": "user", "description": "普通用户角色", "status": 1 }
  ]
}
```

---

##### GET /system/role/all — 查询所有角色（不分页）

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | 无 |
| 请求参数 | 无 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "id": 1, "roleName": "超级管理员", "roleCode": "admin" },
    { "id": 2, "roleName": "普通用户", "roleCode": "user" }
  ]
}
```

---

##### GET /system/role/{roleId} — 查询角色详情

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:role:query` |
| 路径参数 | `roleId` — 角色ID |

```
GET /system/role/1
```

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { "id": 1, "roleName": "超级管理员", "roleCode": "admin", "description": "拥有系统所有权限", "status": 1 }
}
```

---

##### POST /system/role — 新增角色

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:role:add` |
| 请求体 | `application/json` |

```json
{
  "roleName": "项目经理",
  "roleCode": "pm",
  "description": "项目管理角色，可创建和管理项目",
  "status": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleCode | String | 是 | 角色编码（唯一，程序内部使用） |
| description | String | 否 | 角色描述 |
| status | Integer | 否 | 状态：0=禁用，1=启用（默认1） |

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

##### PUT /system/role — 修改角色

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:role:edit` |
| 请求体 | `application/json` |

```json
{
  "id": 2,
  "roleName": "高级用户",
  "roleCode": "user",
  "description": "高级用户角色，具备扩展操作权限",
  "status": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 角色ID |
| roleName | String | 否 | 角色名称 |
| roleCode | String | 否 | 角色编码 |
| description | String | 否 | 角色描述 |
| status | Integer | 否 | 状态 |

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

##### DELETE /system/role/{roleIds} — 删除角色

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | `system:role:remove` |
| 路径参数 | `roleIds` — 角色ID（支持多个，逗号分隔） |

```
DELETE /system/role/3
DELETE /system/role/3,4
```

**成功响应：**
```json
{ "code": 200, "msg": "操作成功" }
```

---

#### 3.4.5 登录日志（需Token）

##### GET /system/loginLog/list — 查询登录日志（分页）

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 权限 | 无（只能查自己的日志） |
| 请求参数 | Query参数 |

```
GET /system/loginLog/list?pageNum=1&pageSize=10
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "total": 5,
  "rows": [
    {
      "id": 1,
      "userId": 1,
      "username": "admin",
      "operationType": 1,
      "ipAddress": "127.0.0.1",
      "userAgent": "Mozilla/5.0...",
      "loginStatus": 1,
      "failReason": null,
      "operationTime": "2026-03-09 15:30:00"
    },
    {
      "id": 2,
      "userId": 1,
      "username": "admin",
      "operationType": 2,
      "ipAddress": "127.0.0.1",
      "loginStatus": 1,
      "failReason": null,
      "operationTime": "2026-03-09 17:00:00"
    }
  ]
}
```

| operationType值 | 含义 |
|-----------------|------|
| 1 | 登录 |
| 2 | 注销登录 |

| loginStatus值 | 含义 |
|---------------|------|
| 0 | 失败 |
| 1 | 成功 |

---

#### 3.4.6 代码生成（需Token）

##### GET /tool/gen/db/list — 查询数据库所有表

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 请求参数 | 无 |

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "tableName": "sys_user", "tableComment": "用户基本信息表", "createTime": "2026-03-09 10:00:00" },
    { "tableName": "biz_project", "tableComment": "项目表", "createTime": "2026-03-09 10:00:00" }
  ]
}
```

---

##### GET /tool/gen/column/{tableName} — 查询表字段信息

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 路径参数 | `tableName` — 数据库表名 |

```
GET /tool/gen/column/biz_project
```

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "columnName": "id", "columnComment": "主键ID", "columnType": "bigint", "javaType": "Long", "javaField": "id", "isPk": "1", "isIncrement": "1" },
    { "columnName": "project_name", "columnComment": "项目名称", "columnType": "varchar(128)", "javaType": "String", "javaField": "projectName", "isPk": "0" }
  ]
}
```

---

##### GET /tool/gen/preview/{tableName} — 预览生成代码

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 路径参数 | `tableName` — 数据库表名 |

```
GET /tool/gen/preview/biz_project
```

**成功响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "templates/vm/java/domain.java.vm": "package com.synthrasim.system.domain;\n\nimport ...",
    "templates/vm/java/mapper.java.vm": "package com.synthrasim.system.mapper;\n\n...",
    "templates/vm/java/service.java.vm": "...",
    "templates/vm/java/serviceImpl.java.vm": "...",
    "templates/vm/java/controller.java.vm": "...",
    "templates/vm/xml/mapper.xml.vm": "..."
  }
}
```

---

##### GET /tool/gen/download/{tableName} — 下载生成代码ZIP

| 项 | 内容 |
|----|------|
| 认证 | **需要Token** |
| 路径参数 | `tableName` — 数据库表名 |
| 请求参数 | Query参数（均可选） |

```
GET /tool/gen/download/biz_project?packageName=com.synthrasim.system&moduleName=system&author=张三
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| packageName | String | 否 | com.synthrasim.system | 生成代码的包路径 |
| moduleName | String | 否 | system | 模块名（影响Controller的URL前缀） |
| author | String | 否 | SynthraSim | 作者名（写入代码注释） |

**响应：** 直接返回ZIP文件流（浏览器自动下载 `biz_project_code.zip`）。

---

### 3.5 synthrasim-generator（代码生成模块）

**职责：** 读取数据库表结构，通过Velocity模板自动生成CRUD代码。

**为每张表自动生成6个文件：**

| 模板 | 生成物 | 示例（biz_project表） |
|------|--------|----------------------|
| domain.java.vm | 实体类 | `Project.java` |
| mapper.java.vm | Mapper接口 | `ProjectMapper.java` |
| service.java.vm | Service接口 | `IProjectService.java` |
| serviceImpl.java.vm | Service实现 | `ProjectServiceImpl.java` |
| controller.java.vm | Controller | `ProjectController.java` |
| mapper.xml.vm | MyBatis XML | `ProjectMapper.xml` |

---

## 4. Knife4j 接口文档使用指南

### 4.1 访问地址

http://localhost:8080/doc.html

### 4.2 测试步骤

#### 第一步：登录获取Token

左侧菜单 →「用户认证」→「用户登录」→ 填写请求体 → 点击「发送」：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

> code 和 uuid 留空即可跳过验证码。

复制响应中的 `token` 值。

#### 第二步：设置全局Token

点击页面顶部 **「Authorize」** 按钮 → 输入框填入：

```
Bearer eyJhbGciOiJIUzUxMiJ9.xxxxx
```

> 格式：`Bearer` + 空格 + token（Bearer是固定前缀，必须带）

点击确认，之后所有接口自动携带此Token。

#### 第三步：调用任意接口

设置Token后，所有需要认证的接口都可以直接测试。

### 4.3 默认账号

| 账号 | 密码 | 角色 | 权限 |
|------|------|------|------|
| admin | admin123 | 超级管理员(admin) | 所有权限 `*:*:*` |
| zhangsan | admin123 | 普通用户(user) | 基本权限 |

---

## 5. 配置文件说明

主配置文件：`synthrasim-admin/src/main/resources/application.yml`

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | jdbc:mysql://127.0.0.1:8086/synthrasim_server | 数据库地址 |
| `spring.datasource.username` | root | 数据库用户名 |
| `spring.datasource.password` | PSX18322002993 | 数据库密码 |
| `spring.redis.host` | 127.0.0.1 | Redis地址 |
| `spring.redis.port` | 6379 | Redis端口 |
| `spring.mvc.pathmatch.matching-strategy` | ant_path_matcher | MVC路径匹配策略（Knife4j需要） |
| `token.secret` | synthrasimSecretKey2026... | JWT签名密钥 |
| `token.expireTime` | 720 | Token有效期（分钟）=12小时 |
| `mybatis-plus.configuration.log-impl` | StdOutImpl | 控制台打印SQL（生产环境建议关闭） |
| `knife4j.enable` | true | 启用Knife4j接口文档 |

---

## 6. 开发规范

### 6.1 新增业务模块的标准步骤

以新增「项目管理」模块为例：

**方法一：使用代码生成器（推荐）**
1. 确保 `biz_project` 表已在数据库中创建
2. 访问 `GET /tool/gen/preview/biz_project` 预览代码
3. 访问 `GET /tool/gen/download/biz_project` 下载ZIP
4. 将生成的文件放入 `synthrasim-system` 对应目录

**方法二：手动创建**
1. `system/domain/` → `BizProject.java`（继承BaseEntity，加@TableName注解）
2. `system/mapper/` → `BizProjectMapper.java`（继承BaseMapper）
3. `system/service/` → `IBizProjectService.java`（继承IService）
4. `system/service/impl/` → `BizProjectServiceImpl.java`（继承ServiceImpl）
5. `admin/controller/` → `BizProjectController.java`

### 6.2 接口返回规范

```java
return AjaxResult.success();                    // {"code":200,"msg":"操作成功"}
return AjaxResult.success(data);                // {"code":200,"msg":"操作成功","data":{...}}
return AjaxResult.error("错误描述");              // {"code":500,"msg":"错误描述"}
return new TableDataInfo(list, total);           // {"code":200,"rows":[...],"total":100}
```
