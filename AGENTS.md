# 开发约定

- 提交信息使用中文 Conventional Commits：`feat(模块): 中文描述`，必要时添加提交体。
- Java 使用 JDK 17+，格式以 Spotless 的 GOOGLE 规范为准，左花括号同行、2 空格缩进。
- Java 实体、DTO、VO 使用普通类，不使用 `record`，不引入 Lombok；新增或修改的方法补齐 Javadoc。
- 新业务实体按需继承 `AuditEntity` 或 `BusinessEntity`，统一使用公共审计及逻辑删除约定，见 `docs/business-persistence-conventions.md`。
- `com.ruoyi.business` 下的新业务接口统一返回 `R<T>`，无业务数据使用 `R<Void>`；错误响应由业务范围的异常处理器返回 `R`。若依原有接口保持现状。
- 修改 Java 后执行 `./mvnw spotless:apply`，提交前执行 `./mvnw verify`。
- Windows 可使用 `scripts/maven.ps1` 自动选择 JDK，传入相同 Maven 参数。
- 本地 MySQL / Redis 使用本仓库 Docker Compose；资源均使用 `my-system-` 前缀。
- 密码、Token 密钥和本地路径放在被 Git 忽略的 `.env`，提交时保留 `.env.example`。
- 保留上游 MIT 许可证和作者说明。
