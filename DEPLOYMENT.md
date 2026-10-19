# V-Blog 服务器部署

当前部署由服务器上的 Caddy 提供公网 HTTP 入口，转发到本机 `127.0.0.1:8088`。Docker Compose 管理 Nginx 前端、Spring Boot 后端和 MySQL。数据库和上传文件分别持久化在 `data/mysql` 与 `data/uploads`，容器更新不会删除它们。

## 一次性准备

1. 在服务器安装 Docker Engine 与 Compose 插件，保留已有的 Caddy 服务。
2. 把本仓库文件复制到 `/opt/vblog`，并创建 `.env`，填写 `PUBLIC_HOST`、`MYSQL_ROOT_PASSWORD`、`DB_PASSWORD`、`JWT_SECRET`。两个数据库密码应不同，JWT 密钥至少 32 个随机字符。`.env` 权限设为 `600`。
3. 创建 `private/02-data.sql`。首次空库部署时可为空文件；迁移已有数据时放入使用 `mysqldump --single-transaction --no-create-info` 导出的 SQL。将文件所有者设为 `root:999`、权限设为 `640`，使容器内的 MySQL 用户能读取。**只在 MySQL 数据目录为空时，镜像才执行建表和数据导入。**
4. 将原有图片、视频复制到 `data/uploads/images`、`data/uploads/videos`。若使用非 root 用户运行后端容器，应给该用户写权限。
5. Caddy 将公网 `:80` 反向代理到 `127.0.0.1:8088`。首次上线前检查服务器安全组放行 TCP 80。

## 启动与更新

```bash
cd /opt/vblog
sudo docker compose up -d --build
sudo docker compose ps
```

修改代码后重新复制文件并再次执行 `sudo docker compose up -d --build`。上传目录和 MySQL 数据目录仍保留。不要使用 `docker compose down -v` 或删除 `data/`，否则会丢失数据。

## 备份

定期备份 `data/uploads`，并从数据库容器导出 SQL。上传文件与数据库中的路径应作为同一份备份恢复。`private/02-data.sql` 只用于首次初始化；初始化成功后可清空该文件，同时保留空文件供 Compose 挂载。

当前只使用 IP 地址，访问地址为 `http://129.204.193.234/`。如要启用受信任的 HTTPS，建议先配置真实域名指向服务器，再由 Caddy 申请和续期证书。

## 2026-10-19 更新：低内存部署

本次在本机构建前端静态文件与后端 JAR，服务器只通过 `deploy/backend.runtime.Dockerfile`、`deploy/web.runtime.Dockerfile` 构建运行镜像，不在服务器执行 Maven 或 npm 编译。后端构建排除历史 `vblog_upload` 资源副本；运行时继续使用 `data/uploads` 持久化目录。

服务器使用 `compose.override.yaml`（内容对应 `deploy/compose.memory.yaml`）：后端容器限制 768 MiB、JVM 堆上限 384 MiB，前端容器限制 128 MiB，并开启日志轮转。数据库服务本次未重建。

现有镜像一键启动：

```bash
cd /opt/vblog
sudo docker compose up -d --no-build --pull never
```

后续更新继续在本机生成产物，上传至临时构建目录后按两个 runtime Dockerfile 构建 `vblog-backend:latest`、`vblog-web:latest`，然后执行：

```bash
sudo docker compose up -d --no-deps --no-build --pull never backend
sudo docker compose up -d --no-deps --no-build --pull never web
```

每次更新前备份数据库；验证公网页面、媒体与数据完整性后，删除当次安装包、临时构建目录及旧应用镜像。不要清理 `data/mysql`、`data/uploads` 或执行卷清理。此次更新前的数据库备份仅保存在本地 `private/server-db-before-20261019.sql.gz`，不纳入版本管理。


## 社区演示与安全更新

演示数据通过 scripts/generate_demo_seed.py 生成 SQL，仅对保留的 demo_* 虚构账号插入内容与互动；重复执行不会增加重复行。将生成的 SQL 经标准输入送到数据库容器，执行成功后删除服务器临时 SQL。不要用本地数据库覆盖线上数据库。

本次安全更新升级了 JWT 校验，已有会话需要重新登录；详细修复和剩余 HTTP 链路风险见 SECURITY_REVIEW.md。上线前数据库备份位于本地 private/server-db-before-security-20261019.sql.gz。
