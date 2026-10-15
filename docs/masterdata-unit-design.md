# 计量单位档案

## 数据与业务约定

- 独立表 `md_unit`，独立业务包 `com.ruoyi.business.masterdata.unit`，复用现有业务 Maven 模块。
- 编码 1～64、名称 1～100 字符，去除首尾空白；均按 `utf8mb4_unicode_ci` 唯一。逻辑删除后仍占用编码和名称，不提供恢复。
- 状态使用公共 `EnableStatus`：0 启用、1 停用。备注最大 500 字符，缺失或 null 统一为空字符串。
- 不设 `sort_order`，默认编码、ID 升序。保留版本号防止旧页面覆盖。暂不提供单位组、换算、数量精度和导入导出。
- 实体继承 `BusinessEntity`，复用审计及逻辑删除，时间按北京时间输出到秒。数据库初始数据显式填写 admin 操作者和 UTC+8 时间。
- 预置 PCS/件、EA/个、BOX/箱、KG/千克、G/克、T/吨、M/米、M2/平方米、M3/立方米、L/升；这些是可编辑的单位，不代表任何默认换算关系。
- 当前无料品引用表，暂不加入无效的引用检查；后续料品使用单位 ID 关联，届时添加引用与删除限制。

## 查询与写入

- `GET /masterdata/unit/list` 返回 `R<PageResult<UnitView>>`，数据形状为 `{code,msg,data:{rows,total}}`，不嵌套旧 `TableDataInfo` 的 code/msg。
- 公共 `PageQuery` 默认第 1 页、每页 20 条，页码须正数，每页 1～100 条。沿用 PageHelper，MP 查询返回的分页总数在 VO 转换前保留，finally 清理分页上下文。
- `unitCode`、`unitName` 为包含文本查询，通配字符按普通文本处理；`status` 精确匹配，条件按 AND 组合。`sortField` 仅接受 unitCode/unitName/updateTime，`sortDirection` 仅接受 asc/desc，追加 ID 保证稳定。
- 详情 GET `/{id}`、新增 POST、修改 PUT、删除 DELETE `/{id}?version=...`；响应分别为 `R<UnitView>`、`R<UnitView>`、`R<Void>`、`R<Void>`。
- 新增不能指定 ID、版本号；新增后读取实际落库值返回，避免数据库 DATETIME 精度与 Java 纳秒值造成响应不一致。
- 修改及删除在事务开始锁定对应有效单位行，检查最新版本。修改携带旧版本条件并递增版本；删除使用实体 MP 方法，触发公共审计填充。无须全局业务锁或新增乐观锁插件。
- 唯一性提前检查并由数据库索引兜底，并发重复不会泄露数据库内部错误。DTO、VO 使用普通 Java 类；请求不接受审计和删除标记。

## 前端与权限

- 动态菜单“基础资料 → 计量单位”，路由 `masterdata/unit/index`，组件名 `MasterDataUnit`。
- 权限 `masterdata:unit:list/query/add/edit/remove`，各入口独立校验，操作日志使用“计量单位”。默认仅授权管理员，其他角色显式分配。
- VXE 普通分页列表配 Element Plus 编辑/详情抽屉，不显示大标题和介绍。编码、名称、状态的列筛选与顶部查询共享条件；筛选、排序、分页均由接口执行。
- 支持列宽调整、六点手柄列顺序拖拽，操作列固定右侧，不持久化列偏好。点击编码查看，详情可进入编辑；显示只读审计信息。
- 未保存关闭需确认；刷新和保存沿用已生效的查询条件，未提交草稿不参与请求。失败保留上一份成功结果，最新请求生效，删除末页最后一条时回退有效页码。

## 初始化与验证

- 增量脚本 `sql/migrations/20261015_01_unit.sql`，已有数据库运行 `scripts/apply-unit.ps1`。新 Docker 数据卷自动执行该脚本；不重建现有数据卷。
- 重复执行不覆盖已有单位和菜单，不复活已删除单位。菜单授权后重新登录，刷新权限缓存。
- 后端集成测试覆盖分页、筛选、排序、绑定、权限、唯一约束、事务版本冲突、并发和删除审计；真实 MySQL 校验字符集及脚本幂等。
- 后端执行 Spotless 与 Maven verify；前端执行 Prettier、ESLint、生产构建并检查新增类型错误。浏览器测试、截图与临时验证脚本放在忽略目录，前端只提交正式代码和文档。
