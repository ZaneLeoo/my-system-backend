# 开发约定

- 提交信息使用中文 Conventional Commits：`feat(模块): 中文描述`，必要时添加提交体。
- Java 使用 JDK 17+，格式以 Spotless 的 GOOGLE 规范为准，左花括号同行、2 空格缩进。
- 修改 Java 后执行 `./mvnw spotless:apply`，提交前执行 `./mvnw verify`。
- Windows 可使用 `scripts/maven.ps1` 自动选择 JDK，传入相同 Maven 参数。
- 本地 MySQL / Redis 使用本仓库 Docker Compose；资源均使用 `my-system-` 前缀。
- 密码、Token 密钥和本地路径放在被 Git 忽略的 `.env`，提交时保留 `.env.example`。
- 保留上游 MIT 许可证和作者说明。
