# 采购订单一期

## 范围与入口

业务位于 `my-system-business` 的 `purchase.order` 包，包含采购订单主表、明细、试算和状态操作。币种固定人民币，不产生库存，不包含到货、入库、付款及审批流程引擎。

已有数据库执行 `powershell -File scripts/apply-purchase-order.ps1`；脚本幂等，不覆盖已有档案、参数和菜单。新数据卷由 Docker Compose 挂载 `sql/migrations/20261017_02_purchase_order.sql`。默认管理员有权限，其他角色需分配对应功能权限及供应商、料品参照权限。当前查询按功能授权，可查看全部订单，不按部门隔离。

## 数据与快照

`purchase_order` 保存单号、订单日期、供应商及联系信息、采购员和部门快照、币种、计价方式、状态、金额汇总、精度规则、版本以及提交／审核／关闭记录。`purchase_order_line` 保存稳定行 ID、业务行号、料品和库存单位快照、数量、两种单价、税率、金额、预计到货日期、备注。两表继承公共审计与逻辑删除约定，不增加物理外键。

正式单号在新增事务中由北京时间创建日期和自增 ID 生成，允许断号，修改订单日期不改号。明细最多 200 行，同一料品允许重复；调序保留行 ID，移除明细逻辑删除。主表版本保护整单，每次修改或状态操作递增，过期版本拒绝。

历史详情展示保存的名称、规格、单位及人员快照，档案更名不会改写历史订单。采购员与部门取首次创建时当前有效账号及部门。首次保存后计价方式、精度规则固定。

## 金额与精度

数量与单价最多 6 位小数，金额 2 位，四舍五入 `HALF_UP`。系统参数 `purchase.order.quantityDecimalPlaces`、`purchase.order.priceDecimalPlaces` 为 0～6，默认 6；服务端首次保存时读取并留存快照。无效配置明确报错，旧单不随参数变化重算规则。

输入的数量必须大于零，价格可为零，超过有效小数位拒绝保存，不对输入静默舍入。税率采用百分数，0～100，最多两位小数，空值必须补填，不能当作零。数量、单价和金额以十进制字符串传输，服务端使用 `BigDecimal`。以下公式中的税率先除以 100：

- 含税：`gross_amount = round(quantity × gross_price, 2)`，`net_amount = round(gross_amount ÷ (1 + rate), 2)`，`tax = gross_amount - net_amount`。
- 未税：`net_amount = round(quantity × net_price, 2)`，`tax = round(net_amount × rate, 2)`，`gross_amount = net_amount + tax`。

另一口径的单价按本单单价精度换算，只作展示，不参与金额计算。整单汇总已经舍入的各行金额。超出数据库范围明确拒绝。

## 状态及接口

接口前缀 `/purchase/order`，统一 `R<T>`；权限前缀 `purchase:order`。

| 接口 | 权限 | 说明 |
| --- | --- | --- |
| `GET /list` | list | 分页、包含查询、供应商／状态／日期筛选及排序白名单 |
| `GET /{id}` | query | 主表与明细快照 |
| `GET /defaults` | add、edit 或 query | 当前人员、部门及新单精度 |
| `POST /calculate` | add 或 edit | 无副作用试算，旧单使用已存精度 |
| `POST /`、`PUT /` | add、edit | 新增或修改草稿，返回落库详情 |
| `DELETE /{id}?version=…` | remove | 仅删除草稿 |
| `POST /{id}/submit` | submit | 草稿→已提交 |
| `POST /{id}/withdraw` | withdraw | 已提交→草稿 |
| `POST /{id}/approve` | approve | 已提交→已审核 |
| `POST /{id}/reject` | reject | 已提交→草稿，原因必填 |
| `POST /{id}/close` | close | 已审核→已关闭，原因必填 |

分页默认 20，最大 100。排序只允许单号、订单日期、修改时间及价税合计，追加 ID 稳定排序。分页上下文在 `finally` 清理。保存与状态操作统一记录若依操作日志，不新增业务历史子表。

## 引用与并发

保存、提交、审核校验供应商、料品、分类和库存单位有效且启用，料品须允许采购。订单单位固定为库存单位，不进行跨单位数量转换。

未关闭订单阻止供应商／料品停用及关闭料品采购开关。未删除订单阻止供应商／料品／单位删除及料品库存单位变更。公共引用 Mapper 承接这些查询，档案 Service 不依赖订单 Service。

所有相关写入先取得分类树事务锁，再结合订单、供应商和按 ID 排序的单位／料品行锁。持锁后的当前读用于状态和引用校验，主表、明细、金额及版本在一个事务中提交，失败整体回滚。

## 验证

`OrderIntegrationTest` 覆盖两种计价方式、舍入尾差、精度快照、溢出、稳定行 ID、回滚、版本与状态、权限、分页及排序白名单、审计和逻辑删除、档案引用及并发竞态。交付同时回归分类、单位、料品、供应商和若依原有分页；真实 MySQL 验证中文、索引、单号及增量脚本幂等。
