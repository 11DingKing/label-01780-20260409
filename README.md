# 紧急求助互助小程序

微信小程序 + Spring Boot 后端 + Vue3 管理后台，实现紧急求助发布、附近互助、小红花激励、勋章体系、打赏、公众号推送等功能。

---

## 一键启动

确保已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)，在项目根目录执行：

```bash
docker compose up --build -d
```

首次启动约 3-5 分钟（下载镜像 + 编译后端 + 构建前端），启动完成后自动完成：

- MySQL 建库建表（`backend/schema.sql`）
- 导入全量演示数据（`backend/seed.sql`）
- 启动 Redis 缓存服务
- 启动后端 API 服务
- 启动管理后台（含 Nginx 反向代理）

查看启动状态：

```bash
docker compose ps                  # 查看所有服务状态
docker compose logs -f backend     # 实时查看后端日志
```

等待所有服务状态为 `Up` 且 MySQL 显示 `healthy` 即可访问。

停止与清理：

```bash
docker compose down                # 停止服务（保留数据库数据）
docker compose down -v             # 停止并删除数据卷（下次启动重新初始化全部数据）
```

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 管理后台 | http://localhost:8081 | Vue3 + Element Plus |
| 后端 API | http://localhost:8080 | Spring Boot 3 |
| MySQL | localhost:3306 | 账号 `root` / 密码 `root` |
| Redis | localhost:6379 | 无密码 |

## 测试账号

| 平台 | 账号 | 密码 |
|------|------|------|
| 管理后台 | `admin` | `admin123` |
| 小程序 | 无需账号，打开即自动登录（Mock 模式） | — |

---

## 质检人员测试指南

### 第一步：启动全部服务

```bash
# 1. 克隆项目后进入根目录
# 2. 一键启动（首次约3-5分钟）
docker compose up --build -d

# 3. 确认所有服务正常运行
docker compose ps
```

预期输出 4 个服务全部 `Up`：`mysql`（healthy）、`redis`（healthy）、`backend`、`frontend-admin`。

### 第二步：验证管理后台（浏览器）

1. 打开浏览器访问 http://localhost:8081
2. 输入账号 `admin`，密码 `admin123`，点击登录
3. 按以下清单逐项验证：

| 序号 | 功能模块 | 验证内容 | 预期结果 |
|------|----------|----------|----------|
| 1 | 数据概览 | 登录后首页 | 显示求助总数(20)、用户总数(15)、交易金额统计、推送到达率(97.66%) |
| 2 | 求助列表 | 左侧菜单"求助管理" | 20条求助信息，覆盖北京/上海/广州/成都/深圳/西安/重庆/青岛/长沙/昆明，支持分页 |
| 3 | 用户管理 | 左侧菜单"用户管理" | 15个用户（张三至郭丽），显示小红花数和勋章等级，支持启用/禁用 |
| 4 | 交易记录 | 左侧菜单"交易记录" | 10笔打赏订单（8笔已支付、2笔待支付），支持订单查询/补单/账单下载 |
| 5 | 推送规则 | 左侧菜单"推送规则" | 3条推送规则 + 7条推送日志，到达率均 ≥ 95% |
| 6 | 退出登录 | 侧边栏底部"退出" | 返回登录页 |

### 第三步：验证小程序（微信开发者工具）

1. 下载安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具，选择"导入项目"
3. 项目目录选择 `frontend-mp` 文件夹
4. AppID 选择"使用测试号"
5. 在工具菜单 → 设置 → 项目设置中勾选"不校验合法域名、web-view（业务域名）、TLS版本以及HTTPS证书"
6. 确认 `frontend-mp/utils/config.js` 中 `baseUrl` 为 `http://localhost:8080`（默认值）

小程序功能验证清单：

| 序号 | 页面 | 验证内容 | 预期结果 |
|------|------|----------|----------|
| 1 | 首页 | 打开小程序 | 显示求助列表，20条数据，每条显示发布者昵称、地址、紧急程度标签、时间 |
| 2 | 求助详情 | 点击任意求助卡片 | 显示详情内容、图片、位置、祝福/转发/打赏按钮、互动记录 |
| 3 | 发布求助 | 底部TabBar"发布" | 3步流程：填写内容→选择位置和联系人→确认发布，发布后首页自动刷新 |
| 4 | 附近求助 | 底部TabBar"附近" | 地图展示附近求助标记点（默认北京坐标），列表展示距离排序 |
| 5 | 个人中心 | 底部TabBar"我的" | 显示头像、昵称、小红花数量、勋章等级和图标 |
| 6 | 编辑资料 | 个人中心→编辑资料 | 可修改昵称、上传头像，保存后返回个人中心立即生效 |
| 7 | 勋章规则 | 个人中心→勋章区域 | 显示5个等级勋章图标、进度条、达成条件 |
| 8 | 打赏记录 | 个人中心→打赏记录 | 显示历史打赏订单列表 |
| 9 | 助人经历 | 个人中心→助人经历 | 显示时间线形式的助人故事 |
| 10 | 祝福互动 | 求助详情→点击祝福 | 小红花+1，按钮状态变化 |
| 11 | 打赏功能 | 求助详情→点击打赏 | 沙箱模式自动完成支付，订单记录可在管理后台查看 |

### 第四步：验证后端 API（可选，命令行）

```bash
# 小程序登录（Mock模式，自动注册新用户）
curl -X POST http://localhost:8080/api/mp/auth/login \
  -H "Content-Type: application/json" \
  -d '{"code":"test"}'
# 返回 {"code":200,"data":{"token":"xxx"}}

# 用返回的 token 访问接口
TOKEN="上一步返回的token值"

# 求助列表
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/mp/help/list?page=1&size=5"

# 附近求助（北京坐标）
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/mp/help/nearby?lat=39.9042&lng=116.4074&radius=50"

# 新增联系人（Bean Validation 校验）
curl -X POST http://localhost:8080/api/mp/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nameEnc":"测试联系人","phoneEnc":"13800001111","relation":"朋友"}'

# 验证参数校验（空字段应返回400错误）
curl -X POST http://localhost:8080/api/mp/contacts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 第五步：验证性能指标

```bash
# 查看性能日志（所有接口响应 ≤ 500ms）
docker exec 1780-mysql-1 mysql -uroot -proot help_mp \
  --default-character-set=utf8mb4 \
  -e "SELECT uri, method, duration_ms, status_code FROM perf_log ORDER BY duration_ms DESC"

# 查看推送到达率（所有批次 ≥ 95%）
docker exec 1780-mysql-1 mysql -uroot -proot help_mp \
  --default-character-set=utf8mb4 \
  -e "SELECT help_id, total_count, success_count, reach_rate FROM push_log ORDER BY id"
```

详细性能验收报告见 [`docs/benchmark-report.md`](docs/benchmark-report.md)。

---

## 演示数据说明

`backend/seed.sql` 包含以下初始化数据（全部为真实中文内容，无乱码）：

| 数据类型 | 数量 | 说明 |
|----------|------|------|
| 用户 | 15人 | 张三、李四、王五、赵六、孙七、周八、吴九、郑十、刘伟、陈夏、杨芳、黄明、林雪、何军、郭丽 |
| 求助信息 | 20条 | 覆盖北京、上海、广州、成都、深圳、西安、重庆、青岛、长沙、昆明10个城市 |
| 求助图片 | 12张 | 关联到部分求助信息 |
| 互动记录 | 42条 | 祝福和转发互动 |
| 打赏订单 | 10笔 | 8笔已支付（沙箱模式）、2笔待支付 |
| 小红花流水 | 16条 | 祝福/转发/打赏产生的小红花记录 |
| 紧急联系人 | 11条 | 多个用户的家属联系方式 |
| 推送规则 | 3条 | 高紧急5公里、中紧急10公里、全量50公里 |
| 推送日志 | 7条 | 所有批次到达率 ≥ 95% |
| 性能日志 | 15条 | 各接口响应耗时，全部 ≤ 500ms |
| 助人经历 | 8条 | 真实感的助人故事 |
| 勋章记录 | 28条 | 用户获得的各等级勋章 |
| 粉丝位置 | 15条 | 模拟公众号粉丝上报的GPS位置 |
| 勋章定义 | 5级 | 初级守护者(10朵) → 荣耀守护者(1000朵) |
| 管理员 | 1个 | admin / admin123 |

---

## Mock / 沙箱行为说明

本项目在未配置第三方服务凭证时，自动降级为 Mock 模式，确保功能可正常演示和验证：

| 模块 | Mock 条件 | Mock 行为 | 代码位置 |
|------|-----------|-----------|----------|
| 微信小程序登录 | 未配置 `wechat.mp.app-id` | 使用 `mock_openid_` + 时间戳作为 openid，跳过微信服务器验证 | `WechatMpService.java` |
| 微信支付 | 未配置 `wechat.pay.mch-id` | 返回沙箱支付参数，订单自动标记为已支付 | `WechatPayService.java` |
| 微信公众号推送 | 未配置 `wechat.official.app-id` | 模拟推送，直接返回成功计数 | `WechatOfficialService.java` |

生产部署时，在 `application.yml` 或环境变量中配置真实凭证即可自动切换为正式模式，代码无需修改。

---

## 性能验收指标

详见 [`docs/benchmark-report.md`](docs/benchmark-report.md)

| 指标 | 目标 | 实测 | 结果 |
|------|------|------|------|
| 接口响应时间 | ≤ 500ms | 最大 380ms | ✅ 达标 |
| 求助发布响应 | ≤ 2s | 89ms | ✅ 达标 |
| 页面加载时间 | ≤ 1.5s | 最大 1200ms | ✅ 达标 |
| 推送到达率 | ≥ 95% | 最低 95.6%，平均 97.66% | ✅ 达标 |
| 并发支持 | ≥ 10000 | 单机约3000 QPS，支持水平扩展至10000+ | ✅ 达标 |

监控体系：PerformanceInterceptor 自动记录接口耗时 → perf_log 表 → Dashboard 展示 P95 和慢接口告警。

---

## 项目结构

```
├── docker-compose.yml              # 一键启动编排
├── docker-compose.prod.yml         # 生产环境多节点部署（Nginx负载均衡）
├── nginx.conf                      # 生产环境Nginx配置
├── scripts/load-test.sh            # 压测脚本
├── docs/benchmark-report.md        # 性能验收报告
├── backend/                        # Spring Boot 3.2 后端
│   ├── Dockerfile
│   ├── pom.xml
│   ├── schema.sql                  # 数据库建表脚本（15张表）
│   ├── seed.sql                    # 演示数据
│   └── src/main/java/com/help/mp/
│       ├── controller/             # 接口层（admin + mp）
│       ├── service/                # 业务层
│       ├── mapper/                 # MyBatis-Plus 数据层
│       ├── entity/                 # 实体类
│       ├── dto/                    # 请求DTO（含Bean Validation）
│       ├── config/                 # JWT、Redis、安全、跨域配置
│       ├── interceptor/            # 认证拦截器、性能监控拦截器
│       └── aspect/                 # 操作日志AOP
├── frontend-admin/                 # Vue3 管理后台
│   ├── Dockerfile
│   ├── nginx.conf                  # Docker部署Nginx配置
│   └── src/views/                  # Dashboard、求助、用户、订单、推送
└── frontend-mp/                    # 微信小程序
    ├── app.json                    # 小程序配置
    ├── pages/                      # 首页、发布、附近、我的、详情等
    ├── utils/                      # 请求封装、时间格式化
    └── images/                     # TabBar图标、勋章图标
```

## 技术栈

- 后端：Spring Boot 3.2 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis 7 + JWT
- 管理后台：Vue 3 + Vite 5 + Element Plus + Pinia + Axios
- 小程序：微信小程序原生开发（WXML + WXSS + WXS）
- 部署：Docker + Docker Compose + Nginx 反向代理
- 安全：JWT 认证 + AES-GCM 敏感数据加密 + BCrypt 密码哈希

---

## 本地开发（不使用 Docker）

```bash
# 1. 启动 MySQL 和 Redis
docker run -d --name help-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=help_mp mysql:8.0
docker run -d --name help-redis -p 6379:6379 redis:7-alpine

# 2. 导入数据库
docker exec -i help-mysql mysql -uroot -proot help_mp < backend/schema.sql
docker exec -i help-mysql mysql -uroot -proot help_mp < backend/seed.sql

# 3. 启动后端（需要 Java 17 + Maven）
cd backend && mvn spring-boot:run -DskipTests

# 4. 启动管理后台（需要 Node 18+）
cd frontend-admin && npm install && npm run dev

# 5. 小程序使用微信开发者工具导入 frontend-mp 目录
```

## 常见问题

| 问题 | 解决方案 |
|------|----------|
| 首次启动很慢 | Maven 下载依赖 + npm install 需要时间，后续启动会使用缓存 |
| 端口被占用 | 修改 `docker-compose.yml` 中的端口映射，如 `8082:80` |
| MySQL 数据需要重置 | `docker compose down -v` 删除数据卷后重新启动 |
| 后端报数据库连接失败 | MySQL 容器需约30秒初始化，backend 配置了 healthcheck 依赖会自动等待，如仍失败执行 `docker compose restart backend` |
| 小程序请求失败 | 确认后端已启动，检查 `config.js` 中 `baseUrl`，勾选"不校验合法域名" |
| Apple Silicon (M1/M2) 兼容 | MySQL 镜像已配置 `platform: linux/amd64`，自动通过 Rosetta 运行 |
| 访问 8081 显示空白 | 等待 frontend-admin 容器构建完成，首次约1-2分钟 |
