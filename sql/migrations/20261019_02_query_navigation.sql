-- 将原有两个页面菜单收敛为“系统工具 → 查询管理”，保留原权限标识及角色授权。
START TRANSACTION;
SET @query_navigation_now=NOW();
SET @query_navigation_root=(SELECT menu_id FROM sys_menu WHERE path='query' AND menu_name='查询管理' AND menu_type IN ('M','C') ORDER BY menu_id LIMIT 1);
SET @query_metadata_menu=(SELECT menu_id FROM sys_menu WHERE parent_id=@query_navigation_root AND perms='query:metadata:list' ORDER BY menu_id LIMIT 1);
SET @query_definition_menu=(SELECT menu_id FROM sys_menu WHERE parent_id=@query_navigation_root AND perms='query:definition:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_role_menu(role_id,menu_id) SELECT DISTINCT role_id,@query_navigation_root FROM sys_role_menu WHERE menu_id=@query_metadata_menu AND @query_navigation_root IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_role_menu existing WHERE existing.role_id=sys_role_menu.role_id AND existing.menu_id=@query_navigation_root);
UPDATE sys_menu SET menu_type='C',component='system/query/metadata/index',visible='0',status='0',update_by='admin',update_time=@query_navigation_now WHERE menu_id=@query_navigation_root AND (menu_type<>'C' OR component<>'system/query/metadata/index');
UPDATE sys_menu SET menu_name='查看数据目录',menu_type='F',path='#',component=NULL,update_by='admin',update_time=@query_navigation_now WHERE menu_id=@query_metadata_menu AND menu_type='C';
UPDATE sys_menu SET menu_name='查看查询配置',menu_type='F',path='#',component=NULL,update_by='admin',update_time=@query_navigation_now WHERE menu_id=@query_definition_menu AND menu_type='C';
UPDATE sys_menu SET parent_id=@query_navigation_root,update_by='admin',update_time=@query_navigation_now WHERE parent_id IN (@query_metadata_menu,@query_definition_menu) AND menu_type='F' AND @query_navigation_root IS NOT NULL;
UPDATE sys_menu SET parent_id=@query_navigation_root,update_by='admin',update_time=@query_navigation_now WHERE menu_id IN (@query_metadata_menu,@query_definition_menu) AND parent_id<>@query_navigation_root AND @query_navigation_root IS NOT NULL;
COMMIT;
