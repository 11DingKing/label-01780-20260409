# 测试说明

## 后端 (backend)

- **框架**：JUnit 5 + Mockito，Spring Boot Test（`@WebMvcTest` 用于接口层）。
- **运行**：在项目根目录执行 `mvn test`（需安装 Maven 或使用 IDE 运行测试）。

### 覆盖范围

| 类型 | 类/模块 | 说明 |
|------|---------|------|
| 单元测试 | `Result`, `BizException` | 统一返回与业务异常 |
| 单元测试 | `JwtUtil` | Token 生成、校验、解析 |
| 单元测试 | `UserService` | 按 openid 查询、创建/更新用户、资料更新、勋章等级 |
| 单元测试 | `HelpRequestService` | 发布校验、详情、关闭权限、附近求助参数 |
| 单元测试 | `RedFlowerService` | 增加流水、用户不存在/金额≤0 边界、列表分页 |
| 单元测试 | `HelpInteractionService` | 祝福/转发成功与重复、求助不存在或已关闭 |
| 单元测试 | `TipOrderService` | 创建金额校验、支付成功回调、重复回调、handleNotify |
| 单元测试 | `GlobalExceptionHandler` | BizException 转 Result |
| 接口测试 | `AdminAuthController` | 登录缺参 400、成功 200+token、认证失败 401 |

### 日志

- 业务关键操作已加日志：用户注册、求助发布、祝福/转发、打赏到账、小红花到账、资料更新；异常由 `GlobalExceptionHandler` 与各 Service 的 `log.warn`/`log.error` 记录。

---

## 管理后台 (frontend-admin)

- **框架**：Vitest + @vue/test-utils，jsdom 环境。
- **运行**：在项目根目录执行 `npm run test`（单次）或 `npm run test:watch`（监听）。

### 覆盖范围

| 模块 | 说明 |
|------|------|
| `utils/format.js` | `formatDate`：空值/非法返回「—」、仅日期、日期+时间、时分补零 |
| `utils/message.js` | `toast`/`toastSuccess`/`toastWarning`/`toastError` 调用 ElMessage 且带 customClass；`confirm` 默认与自定义文案 |
| `stores/user.js` | token 初始、`setToken` 写入/清空与 localStorage 同步 |

---

## 交付检查清单

- [ ] 后端：`mvn test` 全部通过。
- [ ] 前端：`npm run test` 全部通过。
- [ ] 无控制台/测试框架报错或警告（可接受依赖 audit 提示）。
- [ ] 关键路径有日志，便于线上排查。
