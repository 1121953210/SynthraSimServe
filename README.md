# SynthraSim 工业仿真平台后端

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.17 | 基础框架 |
| Java | 11+ | 开发语言 |
| MyBatis Plus | 3.5.4 | ORM增强框架 |
| Spring Security | 5.7.x | 安全认证框架 |
| JWT | 0.9.1 | 无状态Token认证 |
| Redis | - | Token缓存、验证码存储 |
| MySQL | 8.0+ | 关系型数据库 |
| Druid | 1.2.20 | 数据库连接池 |
| Knife4j | 4.3.0 | API接口文档 |

## 项目结构

```
├── pom.xml                      # Maven父POM
├── sql/init.sql                 # 数据库初始化脚本
├── database_design.md           # 数据库设计文档
├── synthrasim-common/           # 公共模块（工具类、常量、异常、基础实体）
├── synthrasim-system/           # 系统模块（用户、角色、组织、登录日志）
├── synthrasim-framework/        # 框架模块（Security、JWT、MyBatisPlus配置）
├── synthrasim-generator/        # 代码生成模块（自动生成CRUD代码）
└── synthrasim-admin/            # 启动模块（Controller、Application入口）
```

## 快速开始

### 1. 环境准备

- JDK 11 或更高版本
- Maven 3.6+
- MySQL 8.0+（端口 8086）
- Redis（端口 6379）

### 2. 初始化数据库

```bash
mysql -h 127.0.0.1 -P 8086 -u root -p < sql/init.sql
```

### 3. 修改配置（如需调整）

编辑 `synthrasim-admin/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:8086/synthrasim_server?...
    username: root
    password: PSX18322002993
  redis:
    host: 127.0.0.1
    port: 6379
```

### 4. 编译运行

```bash
mvn clean package -DskipTests
java -jar synthrasim-admin/target/synthrasim-admin.jar
```

或在 IntelliJ IDEA 中直接运行 `SynthraSimApplication.java`。

### 5. 访问

- API文档：http://localhost:8080/doc.html
- 默认管理员：`admin` / `admin123`

## IDEA 导入方式

1. **File → Open** → 选择项目根目录（包含 `pom.xml` 的目录）
2. IDEA 会自动识别 Maven 项目并加载依赖
3. 等待索引完成后，右键 `SynthraSimApplication.java` → Run
