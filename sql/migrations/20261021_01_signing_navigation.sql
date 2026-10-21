-- 独立电子签署入口；复用原权限菜单 ID，保留全部已有角色授权。
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
SELECT '电子签署',0,5,'signing',NULL,1,1,'M','0','0','','edit','admin',NOW()
WHERE NOT EXISTS(SELECT 1 FROM sys_menu WHERE parent_id=0 AND path='signing');
SET @sign_root=(SELECT menu_id FROM sys_menu WHERE parent_id=0 AND path='signing' ORDER BY menu_id LIMIT 1);
UPDATE sys_menu SET menu_name='签署任务',parent_id=@sign_root,order_num=1,path='task',component='signing/task/index',is_frame=1,is_cache=1,menu_type='C',visible='0',status='0',icon='list'
WHERE perms='signing:task:query';
UPDATE sys_menu SET menu_name='合同模板配置',parent_id=@sign_root,order_num=2,path='template',component='signing/template/index',is_frame=1,is_cache=1,menu_type='C',visible='0',status='0',icon='form'
WHERE perms='signing:template:edit';
SET @sign_tasks=(SELECT menu_id FROM sys_menu WHERE perms='signing:task:query' ORDER BY menu_id LIMIT 1);
UPDATE sys_menu SET parent_id=@sign_tasks
WHERE perms IN ('signing:task:start','signing:task:revoke','signing:task:download','signing:task:reconcile') AND @sign_tasks IS NOT NULL;
INSERT INTO sys_role_menu(role_id,menu_id)
SELECT DISTINCT rm.role_id,@sign_root FROM sys_role_menu rm JOIN sys_menu m ON m.menu_id=rm.menu_id
WHERE m.perms LIKE 'signing:%' AND NOT EXISTS(SELECT 1 FROM sys_role_menu old WHERE old.role_id=rm.role_id AND old.menu_id=@sign_root);
INSERT INTO sys_role_menu(role_id,menu_id)
SELECT 1,m.menu_id FROM sys_menu m WHERE (m.menu_id=@sign_root OR m.perms LIKE 'signing:%')
AND NOT EXISTS(SELECT 1 FROM sys_role_menu old WHERE old.role_id=1 AND old.menu_id=m.menu_id);
COMMIT;
