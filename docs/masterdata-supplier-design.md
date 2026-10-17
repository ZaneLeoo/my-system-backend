# 供应商档案一期

供应商使用单表 `md_supplier`，代码放在现有 `my-system-business/masterdata.supplier` 包，不新增 Maven 模块。联系人直接作为主要联系信息保存在档案中；未来需要多联系人时再增加联系人子表。

| 字段 | 约定 |
| --- | --- |
| supplier_id | 自增 ID |
| supplier_code | 必填，最多 64 字符；大小写不敏感唯一，逻辑删除后仍占用 |
| supplier_name | 必填，最多 200 字符；允许同名，使用编码区分 |
| short_name | 可选，最多 100 字符 |
| contact_name | 可选，主要联系人，最多 64 字符 |
| contact_phone | 可选，最多 64 字符，允许座机、分机及国际号码 |
| address | 可选，最多 500 字符 |
| status、remark、version | 公共启停枚举、500 字符备注、并发版本 |
| 审计、逻辑删除 | 继承 `BusinessEntity`，北京时间输出 |

所有文本清理首尾空白，可选文本的 null 统一保存为空字符串。联系人及电话不强制成对填写。当前不包含供应商分类、银行账户、结算和付款条件，也没有虚假的采购订单引用校验；订单接入时补充已引用供应商的删除约束、停用后的使用规则和必要快照。

## 接口与事务

`/masterdata/supplier` 提供 `GET /list`、`GET /{id}`、`POST`、`PUT`、`DELETE /{id}?version=…`，分别使用 `masterdata:supplier:list/query/add/edit/remove` 权限，统一返回 `R<T>`。列表为 `R<PageResult<SupplierView>>`，默认每页 20 条、最多 100 条；编码、名称包含查询，状态精确匹配，条件按 AND 组合。仅允许编码、名称、修改时间排序，追加 ID 保持稳定，不拼接客户端 SQL。

新增不允许指定 ID、版本。修改和删除必须提供最新版本，在同一事务中取得供应商行锁后检查版本；更新递增版本，删除使用带实体的 MP 方法填充修改审计。编码唯一索引兜底并发冲突，名称没有唯一约束。

`GET /masterdata/reference/suppliers` 使用独立 `masterdata:supplier:reference` 权限，返回完整未删除参照选项，包括停用旧值，不受管理分页 100 条限制。不返回审计字段、内部删除标记和版本。前端 `SupplierPicker` 单选保存 ID，`change` 返回档案对象数组，支持完整选项传入、搜索、弹窗、本地分页、禁用和失效旧值展示。后续采购订单服务仍须校验所选供应商状态。

## 初始化

已有数据库执行 `scripts/apply-supplier.ps1`。增量脚本 `sql/migrations/20261017_01_supplier.sql` 幂等建立表、菜单及权限，不预置演示供应商、不覆盖已有数据，默认只授权管理员。新 Docker 数据卷通过第七个初始化挂载执行，已有数据卷不会因挂载新增脚本自动迁移。

前端菜单为“基础资料 → 供应商档案”，采用 VXE 列表和 Element Plus 编辑／详情抽屉，支持接口分页、列筛选、排序和列拖拽。新增菜单后重新登录刷新路由和权限。
