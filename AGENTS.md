# 开发约定

- 提交信息使用中文 Conventional Commits：`feat(模块): 中文描述`，必要时添加提交体。
- Java 使用 JDK 17+，格式以 Spotless 的 GOOGLE 规范为准，左花括号同行、2 空格缩进。
- Java 的 `if`、`else`、`for`（含增强 for）、`while`、`do-while` 的语句体必须使用 `{}`，单行语句也不例外；允许正常的 `else if` 链。
- 禁止使用 `var`：局部变量、循环变量和 try-with-resources 资源变量写出具体类型；Lambda 可使用普通隐式参数，显式标注参数类型时也不得使用 `var`。
- Java 实体、DTO、VO 使用普通类，不使用 `record`，不引入 Lombok；新增或修改的方法补齐 Javadoc。
- Controller 的 HTTP 接口方法及 Java 接口的业务契约需提供完整 Javadoc：说明用途、参数及返回值，必要时说明状态、版本、权限、事务等关键约束和实际抛出的业务异常。已有契约的实现方法可使用 `{@inheritDoc}`，仅补充实现特有行为。
- Getter/Setter、简单属性方法及直观辅助方法使用简洁的一句 Javadoc，避免重复代码含义的冗长说明；不添加不存在的参数、返回值或异常标签。
- 新业务实体按需继承 `AuditEntity` 或 `BusinessEntity`，统一使用公共审计及逻辑删除约定，见 `docs/business-persistence-conventions.md`。
- `com.ruoyi.business` 下的新业务接口统一返回 `R<T>`，无业务数据使用 `R<Void>`；错误响应由业务范围的异常处理器返回 `R`。若依原有接口保持现状。
- 修改 Java 后执行 `./mvnw spotless:apply`，提交前执行 `./mvnw verify`。
- Maven `validate` 同时执行 Checkstyle，检查主代码及测试代码的控制语句花括号和显式类型；不得通过跳过检查或添加抑制来规避约定。
- Windows 可使用 `scripts/maven.ps1` 自动选择 JDK，传入相同 Maven 参数。
- 本地 MySQL / Redis 使用本仓库 Docker Compose；资源均使用 `my-system-` 前缀。
- 密码、Token 密钥和本地路径放在被 Git 忽略的 `.env`，提交时保留 `.env.example`。
- 保留上游 MIT 许可证和作者说明。
