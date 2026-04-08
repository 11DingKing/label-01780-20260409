# 兼容性说明

## 小程序端 (frontend-mp)

### 基础库要求
- 最低基础库版本：2.10.0
- 推荐基础库版本：≥2.25.0

### 系统兼容性
| 平台 | 最低版本 | 说明 |
|------|---------|------|
| iOS | 12.0+ | 使用标准 ES6 语法，无 ES2020+ 特性依赖 |
| Android | 8.0+ (API 26) | 微信内置 X5 内核，兼容性由微信保证 |

### 兼容性设计措施
1. **CSS 兼容**：使用 `rpx` 单位适配不同屏幕；未使用 CSS Grid（部分旧设备不支持），统一使用 Flexbox 布局。
2. **JS 兼容**：仅使用 ES6 语法（`const/let`、箭头函数、`Promise`、模板字符串），未使用可选链 `?.`、空值合并 `??` 等 ES2020 特性。
3. **API 兼容**：`wx.getLocation`、`wx.chooseMedia`、`wx.requestPayment` 等 API 均在基础库 2.10.0 中可用。
4. **安全区域**：打赏面板底部使用 `env(safe-area-inset-bottom)` 适配 iPhone X 及以上刘海屏。

### 已知限制
- `wx.chooseMedia` 需基础库 ≥2.10.0，低于此版本需降级为 `wx.chooseImage`。
- 朋友圈分享 `onShareTimeline` 需基础库 ≥2.11.3。

## 管理后台 (frontend-admin)

### 浏览器兼容性
| 浏览器 | 最低版本 |
|--------|---------|
| Chrome | 80+ |
| Firefox | 78+ |
| Safari | 13+ |
| Edge | 80+ |

### 技术栈
- Vue 3.x + Vite 5.x + Element Plus
- 构建目标：ES2015（Vite 默认）

## 后端 (backend)

### 运行环境
- JDK 17+
- MySQL 8.0+
- Redis 6.0+

### 性能设计
- 数据库索引覆盖高频查询（位置、状态、时间）
- 分页查询限制单页最大条数
- Redis 限流保护核心接口
- 接口性能监控（PerformanceInterceptor）自动记录耗时并告警

## 验证方式
1. **压测脚本**：`backend/src/test/scripts/load-test.sh`，支持 wrk/ab 工具
2. **性能监控**：`/api/admin/dashboard/perf` 接口提供 avg/p95/p99/慢请求统计
3. **推送到达率**：`/api/admin/push/logs` 接口提供每次推送的到达率数据
