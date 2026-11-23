# Aurora Blog Backend

现代化博客系统后端 API，基于 Spring Boot + MySQL + MyBatis Plus 构建。

## 技术栈

- **Spring Boot 3.2.5** - Web 框架
- **MyBatis Plus 3.5.5** - ORM 框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存（可选）
- **JWT** - 身份认证

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis（可选）

### 2. 数据库配置

```bash
mysql -u root -p
```

执行 `src/main/resources/schema.sql` 初始化数据库：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aurora_blog?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package
java -jar target/aurora-blog-backend-1.0.0.jar
```

服务将在 `http://localhost:8080` 启动。

## API 接口

### Projects (作品)

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/projects` | 获取作品列表（分页） |
| GET | `/api/projects/published` | 获取已发布的作品 |
| GET | `/api/projects/{id}` | 获取单个作品详情 |
| POST | `/api/projects` | 创建新作品 |
| PUT | `/api/projects/{id}` | 更新作品 |
| DELETE | `/api/projects/{id}` | 删除作品 |
| PATCH | `/api/projects/{id}/toggle-publish` | 切换发布状态 |

### 查询参数

- `page` - 页码（默认：1）
- `size` - 每页数量（默认：20）
- `published` - 是否发布（true/false）
- `category` - 分类筛选
- `search` - 搜索关键词

### 示例请求

```bash
# 获取已发布的作品
curl http://localhost:8080/api/projects/published

# 分页查询
curl "http://localhost:8080/api/projects?page=1&size=10&published=true"

# 搜索
curl "http://localhost:8080/api/projects?search=电商"

# 创建作品
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Project",
    "titleZh": "新项目",
    "category": "WEB",
    "tags": ["Vue", "Spring Boot"],
    "year": "2024",
    "published": true
  }'
```

## 数据库表结构

### projects 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(255) | 项目标题（英文） |
| title_zh | VARCHAR(255) | 项目标题（中文） |
| category | VARCHAR(50) | 分类 |
| tags | JSON | 技术标签 |
| year | VARCHAR(4) | 年份 |
| image | TEXT | 封面图 |
| detail_image | TEXT | 详情图 |
| description | TEXT | 描述（英文） |
| description_zh | TEXT | 描述（中文） |
| live_url | TEXT | 在线地址 |
| github_url | TEXT | GitHub 地址 |
| published | BOOLEAN | 是否发布 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 项目结构

```
aurora-blog-backend/
├── src/main/java/com/aurora/
│   ├── AuroraBlogApplication.java    # 主应用类
│   ├── config/                        # 配置类
│   │   ├── CorsConfig.java           # 跨域配置
│   │   └── MyBatisPlusConfig.java    # MyBatis Plus 配置
│   ├── controller/                    # 控制器层
│   │   └── ProjectController.java
│   ├── entity/                        # 实体类
│   │   └── Project.java
│   ├── mapper/                        # 数据访问层
│   │   └── ProjectMapper.java
│   └── service/                       # 业务逻辑层
│       └── ProjectService.java
├── src/main/resources/
│   ├── application.yml                # 应用配置
│   └── schema.sql                     # 数据库脚本
└── pom.xml                            # Maven 配置
```

## 下一步

- [ ] 添加 JWT 认证
- [ ] 添加 Redis 缓存
- [ ] 添加更多的 API 接口（博客、下载资源等）
- [ ] 添加接口文档（Swagger/OpenAPI）

## License

MIT
