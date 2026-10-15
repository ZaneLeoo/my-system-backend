# 新业务持久化与接口约定

## 实体继承

- `ruoyi-common` 的 `AuditEntity` 声明 `createBy`、`createTime`、`updateBy`、`updateTime` 和 MP 填充策略；时间使用 `LocalDateTime`。
- 需要逻辑删除的实体继承 `BusinessEntity`，获得 `delFlag` 默认值与 `@TableLogic`。删除标记集中在 `LogicDeleteConstants`：0 正常、2 删除，与若依既有约定一致。
- 不需要逻辑删除的审计实体直接继承 `AuditEntity`。ID、状态、编码、备注、版本号由具体实体声明，不要求所有表拥有相同业务字段。
- 若依原有 `BaseEntity` 包含查询辅助属性并使用 `Date`，保持现有使用方式，新业务实体不继承它。
- DTO、VO 不继承持久化基类；请求不接受客户端审计字段和删除标记。
- Java 使用普通类、显式构造方法与 Getter/Setter，不使用 `record` 或 Lombok。方法级 Javadoc 说明职责和关键约束。

## 审计写入

`ruoyi-framework` 的 `BusinessAuditHandler` 适用于所有 `AuditEntity` 子类。手工 `MyBatisConfig` 通过 `GlobalConfig` 显式接入它；不依赖仅声明 Bean 后的自动发现。

- 新增：使用当前登录用户名和一次采样的当前时间，填充创建及修改信息，覆盖调用方指定的审计值。
- 审计时间明确使用 `Asia/Shanghai`（UTC+8），不依赖 JVM 默认时区；数据库 `DATETIME` 和 `LocalDateTime` 保存北京时间的本地钟面值。
- 修改和逻辑删除：覆盖本次修改人、修改时间。创建字段设置 `updateStrategy = NEVER`，MP 自动生成的更新不会改写创建信息。
- 没有有效登录操作者时拒绝写入，不默认使用 `system`。定时任务、导入、Agent 等后续必须显式设计操作者上下文。
- 普通更新使用 `updateById(entity)` 或 `update(entity, wrapper)`，必须传入非空实体。逻辑删除使用 `deleteById(entity)`，保证删除审计填充。
- 纯 Wrapper 更新、手写 XML 和原生 SQL 不保证触发自动填充，调用方必须显式处理审计；手写查询也必须显式过滤逻辑删除字段。
- `@TableLogic` 只控制持久化标记；引用、子节点、单据状态和是否允许删除仍由业务 Service 校验，不提供绕过业务规则的统一删除入口。

## 请求和响应

- 新业务 Controller 统一返回项目已有的 `R<T>`，集合返回 `R<List<具体VO>>`，详情返回 `R<具体VO>`，无数据的操作返回 `R<Void>`，不使用原始 `R`。
- 成功与失败沿用现有业务状态码和 `code`、`msg`、`data` 字段；无数据时 `data` 为 null，前端仍按原请求拦截器处理。
- `BusinessExceptionHandler` 仅作用于 `com.ruoyi.business` 下的 Controller，负责统一业务、权限、校验、绑定和未知异常的 R 响应。若依旧 Controller 仍由原异常处理器处理。
- Entity 只负责持久化，VO 明确声明接口数据，避免继承关系变化导致数据库内部字段被意外返回。
- DTO 提供无参构造供 Spring/Jackson 属性绑定；保存参数的 Setter 统一清理编码、名称及备注的首尾空白，备注缺失或 null 时为 `""`。带参构造也复用 Setter，再由 Bean Validation 校验规范化后的值。
- 布尔属性使用 `isMatched()`、`isDisabled()` 等标准 Getter，JSON 字段仍为 `matched`、`disabled`。
- 普通业务分页使用公共 `PageQuery` 和 `PageResult<T>`，返回 `R<PageResult<具体VO>>`，其中 data 仅包含 rows/total；沿用 PageHelper，转换 VO 前保留总数并清理线程分页状态。排序字段在具体业务中建立白名单，不能直接信任客户端字段名。旧接口保留 `TableDataInfo`。
- 共用启停状态使用 `EnableStatus`（0 启用、1 停用）；既有分类状态类本期保持兼容，不据此迁移全部历史状态枚举。
- `LocalDateTime` 通过公共 Jackson 配置统一输出 `yyyy-MM-dd HH:mm:ss`，例如 `2026-02-02 12:00:00`，不附带 `T`、毫秒或时区后缀。序列化不对已有本地时间再加 8 小时；旧式 `Date` 继续沿用 `spring.jackson` 的 GMT+8 配置。此约定仅调整输出，已有 ISO 日期时间请求解析保持兼容。
