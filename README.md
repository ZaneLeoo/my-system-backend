# MySystem Backend

基于官方 [RuoYi-Vue](https://github.com/yangzongzhuan/RuoYi-Vue) `master` 分支，保留上游 Git 历史与 MIT 许可证。
当前基线：RuoYi 3.9.2、Spring Boot 4.1.0、JDK 17。上游说明见 [README.upstream.md](README.upstream.md)。

## 本地开发

需要 Docker Desktop、JDK 17+。项目自带 Maven Wrapper，不要求全局安装 Maven。

```powershell
# 首次克隆时执行；当前工作目录已生成本地 .env，无需覆盖。
Copy-Item .env.example .env
# 编辑 .env，为数据库、Redis 和 Token 设置自己的密码/密钥。

# 启动数据库、构建并运行后端。
.\scripts\dev.ps1
```

Windows 脚本会查找本机 JDK 17+，也可以通过 `$env:MY_SYSTEM_JAVA_HOME` 指定 JDK 路径。
其他系统设置 `JAVA_HOME` 后，在仓库根目录执行：

```sh
docker compose up -d --wait
./mvnw package
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端：http://localhost:8080。前端位于同级 `../Frontend`，默认：http://localhost:5173。
若依初始化账号：`admin` / `admin123`，首次登录后可修改密码。

## MySQL 与 Redis

| 服务 | 容器名 | 本机地址 |
| --- | --- | --- |
| MySQL 8.4 | my-system-mysql | 127.0.0.1:13306 |
| Redis 8.4 | my-system-redis | 127.0.0.1:16379 |

数据库名、用户名默认为 `my_system`，密码保存在被 Git 忽略的 `.env` 中。
Docker Compose 和 Spring Boot 读取同一份 `.env`。上传文件在 `data/upload`，日志在 `logs`。
MySQL 首次创建数据卷时，按顺序执行 `sql/ry_20260417.sql` 和 `sql/quartz.sql`。
数据卷、网络均使用 `my-system-` 前缀，数据库端口仅绑定本机。

```powershell
docker compose ps
docker compose logs --tail 100 mysql redis
docker compose stop
```

停止容器会保留数据卷。已有数据卷不会重复执行初始化 SQL。

## 格式化与构建

Spotless 使用 `google-java-format` 的 **GOOGLE** 规范：2 空格缩进，类、方法、控制语句的左花括号同行。
格式检查绑定 Maven `validate` 阶段，构建时自动检查。

```powershell
.\scripts\maven.ps1 spotless:apply
.\scripts\maven.ps1 spotless:check
.\scripts\maven.ps1 verify
```

在其他系统将上述命令替换为 `./mvnw ...`。编辑器 Java 格式化默认关闭，以 Spotless 为准。

## Git

- `origin`：`git@github.com:ZaneLeoo/my-system-backend.git`
- `upstream`：官方 RuoYi-Vue。
- 开发分支：`main`。
- 提交信息统一中文 Conventional Commits，例如 `feat(用户): 新增用户查询接口`。

首次克隆后执行 `git config core.hooksPath .githooks`，启用提交信息校验。
允许 `feat`、`fix`、`docs`、`style`、`refactor`、`perf`、`test`、`build`、`ci`、`chore`、`revert` 前缀，可按需添加提交体。
当前本机已为此仓库配置 GitHub SSH 443 端口，避免网络对 22 端口的限制。
