# 基础档案只读参照

参照服务位于业务模块 `masterdata.reference`，用于单据选取档案，权限独立于档案列表及维护。接口只读，不授予增删改能力。

| 接口 | 权限 | 返回 |
| --- | --- | --- |
| `GET /masterdata/reference/items` | `masterdata:item:reference` | `R<PageResult<ItemReference>>` |
| `GET /masterdata/reference/items/resolve?ids=1,2` | `masterdata:item:reference` | `R<List<ItemReference>>` |
| `GET /masterdata/reference/items/category-options` | `masterdata:item:reference` | `R<List<CategoryTreeNode>>` |
| `GET /masterdata/reference/units` | `masterdata:unit:reference` | `R<List<UnitOption>>` |
| `GET /masterdata/reference/categories` | `masterdata:category:reference` | `R<List<CategoryTreeNode>>` |

料品关键词支持编码、名称、规格的 OR 包含匹配，其他条件按 AND 组合。分类条件包含自身及后代，`purchaseOnly=true` 限定允许采购；服务端始终限制新选列表为启用且未删除。默认编码、ID 升序，排序白名单为编码、名称、规格。分页复用 PageHelper 和公共分页类型，默认 20、最多 100。

`ItemReadSupport` 复用料品关联 Mapper SQL、分页和分类路径组装，档案维护服务及参照服务共用。批量回显接受 1～100 个正整数 ID，按输入顺序去重，单次关联查询补齐单位及分类名称，另一次分类查询组装路径；不存在及逻辑删除记录省略，停用旧值保留。

料品参照 VO 显式映射编码、名称、规格、分类、三种单位、允许采购、税率和状态，不返回审计、版本或删除标记。可空采购／销售单位和税率明确输出 null。参照结果仅供选择及预填，业务保存仍须校验引用有效性、权限和并发版本；没有跨单位换算关系。

## 授权与兼容

三个参照权限以按钮类型登记到对应档案菜单下。管理员默认授权，其他角色通过“系统管理 → 角色管理”分配。只允许参照时，关闭“父子联动”，仅选择参照节点，避免顺带授予列表等权限。分类权限沿用已有 `masterdata:category` 前缀。

料品选择窗口的分类筛选树使用料品参照权限，不额外要求分类参照权限。单位、分类通用参照接口各自要求独立权限。料品维护抽屉仍通过原有授权下的选项接口给组件提供选项，现有维护用户无需增加参照权限。原接口响应和权限保持兼容。

已有数据库执行 `scripts/apply-reference.ps1`。幂等脚本 `sql/migrations/20261016_02_reference.sql` 同时挂载为新 Docker 数据卷的第六个初始化入口，不修改业务表，不覆盖其他角色授权。权限变更后重新登录刷新。

## 验证

集成测试覆盖参照与维护权限隔离、关键词、启停和删除过滤、分类后代、分页排序、最小响应字段、批量回显顺序及边界、完整单位选项。真实 MySQL 检查菜单脚本重复执行，并建立关闭父子联动且仅拥有三个参照权限的临时角色，验证参照可访问、维护列表被拒绝；测试数据按创建 ID 和独立编码清理。
