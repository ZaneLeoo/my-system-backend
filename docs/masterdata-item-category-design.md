# 存货分类表与业务模块设计

状态：已实现。业务模块、分类 CRUD、VXE 树表与编辑/详情抽屉已接入；数据库与菜单使用独立增量脚本初始化。

## 已确认的方向

- 先建立存货分类，再建立存货档案；分类使用树形结构、手工独立编码。
- 表前缀 `md_` 表示 Master Data，即跨采购、库存等模块共用的基础档案。
- 保留四个创建/更新审计字段和 `del_flag`，逻辑删除使用 MP 的 `@TableLogic(value = "0", delval = "2")`。
- 基础档案采用列表/树表 + 编辑和详情抽屉；单据采用独立编辑/详情页。

## 分类表结构

以下列出核心字段，完整 DDL（含 CHECK 约束、树锁表及菜单初始化）以增量脚本为准。

```sql
CREATE TABLE md_item_category (
  category_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '存货分类ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级分类ID，0为顶级',
  category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
  category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同级显示顺序',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0启用，1停用',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  remark VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '编辑版本号',
  create_by VARCHAR(64) NOT NULL COMMENT '创建账号',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_by VARCHAR(64) DEFAULT NULL COMMENT '最后修改账号',
  update_time DATETIME DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (category_id),
  UNIQUE KEY uk_md_item_category_code (category_code),
  KEY idx_md_item_category_parent (parent_id, del_flag, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='存货分类';
```

编码去除首尾空格后校验，长度 1～64，按数据库排序规则大小写不敏感唯一；逻辑删除后保留编码，不自动复用。名称必填、长度不超过 100，不作为唯一键，同名分类通过编码区分。排序值为非负整数。同级按排序值、ID 稳定排序。

只存 `parent_id`，不额外保存祖先路径、层级和叶子标记；这些值从分类关系推导，避免移动节点时维护多份状态。顶级使用虚拟 ID 0，不插入虚拟根记录。

### 树与状态规则

- 上级必须存在且未删除；禁止把自身或后代设为上级。树关系变更在 Service 事务内验证。
- 启用节点的祖先必须启用；存在启用子节点时拒绝直接停用父节点。启用子节点前要求先启用上级，不自动改变其他分类状态。
- 存在未删除子节点时禁止删除；以后存货表接入后，再增加存货引用校验。首期不查询尚不存在的存货表。
- 删除使用 MP 逻辑删除；修改与正常详情查询均排除已删除记录，不开放恢复、级联删除或复用编码入口。
- 分类首期为全系统共享档案，由角色权限控制维护；不套用部门数据范围或引入多租户模型。
- 最大深度为 32 层，移动节点时同时检查整个子树的深度。
- `md_item_category_lock` 保存一条固定锁记录。所有新增、修改、删除在事务开始时锁住该行，再读取最新分类树校验，避免并发互相移动成环、删除父节点与新增子节点竞态。锁随事务结束释放，不依赖 JVM 单实例锁。
- 修改和删除必须携带当前 `version`。修改成功后递增版本，旧页面提交被拒绝；删除不开放版本绕过入口。

## 后端模块与业务代码

新增 Maven 模块 `my-system-business`，父工程登记模块和依赖版本，`ruoyi-admin` 引用它。业务模块依赖 `ruoyi-common`，不反向依赖启动模块。继续使用现有单体部署、Spring Security、Druid 和 MyBatis 会话工厂。

分类功能放在 `com.ruoyi.business.masterdata.category`，内部按以下职责划分：

| 层         | 内容                                                                             |
| ---------- | -------------------------------------------------------------------------------- |
| controller | `ItemCategoryController`，请求校验、权限、操作日志、若依响应格式                 |
| dto        | 查询条件、新增和修改请求；不接受客户端审计人员、时间和删除标记                   |
| vo         | 分类详情、列表记录及树选择节点                                                   |
| domain     | `ItemCategory`，`@TableName("md_item_category")`、自增 `@TableId`、`@TableLogic` |
| service    | `ItemCategoryService` 与实现，承载唯一性、父子关系、状态、删除检查和事务         |
| mapper     | `ItemCategoryMapper extends BaseMapper<ItemCategory>`，复杂查询再添加 XML        |

新增业务实体继承公共 `BusinessEntity`，由其父类 `AuditEntity` 提供四个审计字段。若依 `BaseEntity` 含查询辅助属性，维持原有用法。ID、分类状态、备注和版本号仍在分类实体中声明。统一约定见 [业务持久化与接口约定](business-persistence-conventions.md)。

审计账号采用当前登录用户名，与现有项目一致；服务端统一填充。业务接入时配置 `MetaObjectHandler` 和字段填充策略，并显式接入手工会话工厂的 `GlobalConfig`；不能仅声明填充器 Bean。验证新增、更新以及 `deleteById(entity)` 的删除审计更新。`@TableLogic` 本身不负责记录操作者，也不能代替引用检查或业务事务。

后续存货档案放在 `masterdata.item`；采购、库存各自组织业务包，不放入 `ruoyi-system`，也不为每张表单独创建 Maven 模块。

## 接口与菜单草案

接口前缀 `/masterdata/item-category`，权限前缀 `masterdata:category`：

| 方法/地址      | 行为                                             | 权限后缀 |
| -------------- | ------------------------------------------------ | -------- |
| `GET /list`    | 分类列表，返回平铺记录供 VXE 构建树表            | `list`   |
| `GET /tree`    | 上级选择树；编辑时排除自身及后代，停用节点不可选 | `query`  |
| `GET /{id}`    | 分类详情                                         | `query`  |
| `POST`         | 新增分类                                         | `add`    |
| `PUT`          | 修改分类及状态                                   | `edit`   |
| `DELETE /{id}?version={version}` | 校验后逻辑删除                         | `remove` |

成功及错误响应统一使用现有 `R<T>`，保留 `@PreAuthorize`、`@Log`。DTO、VO 均使用普通 Java 类。首期树形分类不分页，编码/名称/状态过滤时保留匹配节点的祖先以呈现层级；父节点仅用于上下文时不把它算作匹配结果。

前端菜单位于“基础资料 → 料品分类”，页面使用 VXE 树表和 Element Plus 编辑/详情抽屉，保持筛选及展开状态；上级选择使用树选择器。分类首期不加入导入、导出和树拖拽改父级，先验证基础规则。

SQL 作为独立增量脚本交付，不重跑若依初始化脚本，不删除或重建当前数据。分类后端验证通过后再开发分类前端。

## MP 接入现状与限制

- 已引入 Spring Boot 4 对应的 MP 3.5.17 starter，排除 PageHelper 间接引入的普通 MyBatis starter。
- 现有手工工厂改为 `MybatisSqlSessionFactoryBean`，保留原 `mybatis.*` 配置、XML Mapper 路径和 PageHelper 自动配置。
- 当前不新增 MP 分页拦截器，避免与 PageHelper 重复分页；树形分类也不需要分页插件。
- 逻辑删除通过实体注解逐表开启，不给现有若依实体批量添加注解。MP 生成的 CRUD 自动处理逻辑删除；手写 XML 和原生 SQL 必须明确过滤 `del_flag`。
- 隔离集成测试验证单一会话工厂、MP 自增新增及中文读取、删除后记录保留且标记为 2、MP 查询屏蔽删除记录、禁止更新删除记录，以及旧岗位 XML 的 PageHelper 分页。

分类验证覆盖重复编码、失效上级、自身/后代移动、启停限制、删除父节点、删除审计、版本冲突、无权限访问和并发互移。真实 MySQL 验证中文与大小写不敏感唯一性。

## 本地初始化与代码入口

- 增量脚本：`sql/migrations/20261014_01_item_category.sql`，可重复执行，不插入演示分类、不修改已有业务数据。新 Docker 数据卷会在若依脚本之后自动执行。
- 已有数据库：运行 `powershell -File scripts/apply-item-category.ps1`，使用容器内部数据库凭据，不在命令行展开密码。
- 菜单仅默认授权管理员角色；其他角色在角色管理中分配分类列表、查询、新增、修改、删除权限。重新登录刷新菜单与权限缓存。
- 后端：`my-system-business/src/main/java/com/ruoyi/business/masterdata/category`。
- 审计：`BusinessAuditHandler`，通过手工 `MyBatisConfig` 显式注册到 `GlobalConfig`。
- 前端：`src/views/masterdata/item-category/index.vue`，路由由数据库菜单动态加载，不新增硬编码路由。
- 集成测试使用隔离 H2 数据库；真实接口和浏览器验证脚本、截图保存在忽略的本地日志目录，前端提交只包含正式页面、API 和类型。

资料：[MP 安装说明](https://baomidou.com/getting-started/install/)、[逻辑删除说明](https://baomidou.com/guides/logic-delete/)。
