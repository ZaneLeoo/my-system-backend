-- 独立参照权限；幂等执行，不修改业务表或其他角色授权。
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, create_by, create_time)
SELECT permissions.menu_name, parent.menu_id, 20, '#', 'F', permissions.perms, 'admin', UTC_TIMESTAMP() + INTERVAL 8 HOUR
FROM (
  SELECT '料品参照' AS menu_name, 'masterdata:item:reference' AS perms, 'masterdata:item:list' AS parent_perms
  UNION ALL SELECT '单位参照', 'masterdata:unit:reference', 'masterdata:unit:list'
  UNION ALL SELECT '分类参照', 'masterdata:category:reference', 'masterdata:category:list'
) permissions
JOIN sys_menu parent ON parent.perms = permissions.parent_perms
WHERE NOT EXISTS (SELECT 1 FROM sys_menu existing WHERE existing.perms = permissions.perms);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('masterdata:item:reference', 'masterdata:unit:reference', 'masterdata:category:reference')
AND NOT EXISTS (SELECT 1 FROM sys_role_menu assigned WHERE assigned.role_id = 1 AND assigned.menu_id = sys_menu.menu_id);
COMMIT;
