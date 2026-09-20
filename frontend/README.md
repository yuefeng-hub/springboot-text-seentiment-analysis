# 在线文本情感分析系统 - Vue3 前端

基于 **Vue3 + Vite + Element Plus + ECharts** 的前端界面，配套 SpringBoot 后端使用。

## 功能页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录 / 注册 | /login | 登录注册一体化 |
| 情感分析 | /analyze | 输入文本 → 显示正/负/中性 + 得分 + 置信度 + 情感词 |
| 历史记录 | /history | 分页查看、删除自己的分析记录 |
| 统计分析 | /stats | ECharts：情感分布饼图 + 近7日趋势折线图 |
| 词典管理 | /dict | 仅 ADMIN 可见：新增/删除情感词，实时生效 |

## 启动步骤

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器（默认 http://localhost:5173，自动打开浏览器）
npm run dev
```

> 前提：SpringBoot 后端已在 `http://localhost:8080` 运行。
> Vite 已配置 `/api` 代理到 `8080`，无需处理跨域。

## 管理员账号

后端首次启动自动创建：`admin / admin123`（角色 ADMIN，可看到"词典管理"菜单）

## 生产构建

```bash
npm run build   # 产物在 dist/，可部署到 Nginx 等静态服务器
```
