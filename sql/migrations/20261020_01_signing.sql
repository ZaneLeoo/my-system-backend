-- 电子签署：独立业务关联、可靠收件箱及持久化操作，不改变订单状态。
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS sign_task (
 task_id VARCHAR(36) NOT NULL PRIMARY KEY,
 business_type VARCHAR(64) NOT NULL,business_id VARCHAR(64) NOT NULL,
 provider_environment VARCHAR(150) NOT NULL,flow_id VARCHAR(100) NULL,
 status VARCHAR(16) NOT NULL,active_flag TINYINT NULL DEFAULT 1,
 position_session VARCHAR(36) NULL,version BIGINT NOT NULL DEFAULT 0,
 payload LONGTEXT NOT NULL COMMENT '受控领域快照，含合同业务内容',
 create_by VARCHAR(64) NOT NULL,create_time DATETIME NOT NULL,update_by VARCHAR(64) NOT NULL,update_time DATETIME NOT NULL,
 UNIQUE KEY uk_sign_active(business_type,business_id,active_flag),
 UNIQUE KEY uk_sign_flow(provider_environment,flow_id),UNIQUE KEY uk_sign_position(position_session),
 KEY idx_sign_history(business_type,business_id,create_time),CHECK(version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子签署任务';
CREATE TABLE IF NOT EXISTS sign_document (
 document_id VARCHAR(36) NOT NULL PRIMARY KEY,task_id VARCHAR(36) NOT NULL,kind VARCHAR(16) NOT NULL,payload LONGTEXT NOT NULL,create_time DATETIME NOT NULL,
 KEY idx_sign_document(task_id,kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同私有文件元信息';
CREATE TABLE IF NOT EXISTS sign_party (
 party_id VARCHAR(36) NOT NULL PRIMARY KEY,task_id VARCHAR(36) NOT NULL,payload LONGTEXT NOT NULL,KEY idx_sign_party(task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同双方主体与位置';
CREATE TABLE IF NOT EXISTS sign_template_binding (
 binding_id VARCHAR(36) NOT NULL PRIMARY KEY,business_type VARCHAR(64) NOT NULL,name VARCHAR(100) NOT NULL,version BIGINT NOT NULL DEFAULT 0,payload LONGTEXT NOT NULL,create_time DATETIME NOT NULL,update_time DATETIME NOT NULL,
 KEY idx_sign_binding(business_type),CHECK(version>=0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台模板业务映射';
CREATE TABLE IF NOT EXISTS sign_event (
 event_id VARCHAR(36) NOT NULL PRIMARY KEY,task_id VARCHAR(36) NULL,deduplication_key VARCHAR(64) NOT NULL,kind VARCHAR(100) NOT NULL,payload LONGTEXT NOT NULL,actor VARCHAR(64) NOT NULL,status VARCHAR(16) NOT NULL,next_run_time DATETIME NOT NULL,create_time DATETIME NOT NULL,
 UNIQUE KEY uk_sign_event(deduplication_key),KEY idx_sign_event_inbox(status,next_run_time),KEY idx_sign_event_task(task_id,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签署回调收件箱及操作事件';
CREATE TABLE IF NOT EXISTS sign_job (
 job_id VARCHAR(36) NOT NULL PRIMARY KEY,task_id VARCHAR(36) NOT NULL,type VARCHAR(32) NOT NULL,status VARCHAR(16) NOT NULL,attempt INT NOT NULL DEFAULT 0,token VARCHAR(36) NULL,lease_until DATETIME NULL,next_run_time DATETIME NOT NULL,last_error VARCHAR(500) NULL,create_time DATETIME NOT NULL,
 active_task VARCHAR(36) GENERATED ALWAYS AS (CASE WHEN status IN ('PENDING','RUNNING') THEN task_id ELSE NULL END) STORED,
 UNIQUE KEY uk_sign_job_active(active_task),KEY idx_sign_job_due(status,next_run_time),KEY idx_sign_job_task(task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签署持久化执行任务';
START TRANSACTION;
SET @sign_order_menu=(SELECT menu_id FROM sys_menu WHERE perms='purchase:order:list' ORDER BY menu_id LIMIT 1);
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,menu_type,perms,create_by,create_time)
SELECT x.name,@sign_order_menu,x.n,'#','F',x.perms,'admin',NOW() FROM (
 SELECT '查看电子签署' name,30 n,'signing:task:query' perms
 UNION ALL SELECT '发起电子签署',31,'signing:task:start'
 UNION ALL SELECT '撤销电子签署',32,'signing:task:revoke'
 UNION ALL SELECT '下载合同文件',33,'signing:task:download'
 UNION ALL SELECT '核对签署流程',34,'signing:task:reconcile'
 UNION ALL SELECT '配置签署模板',35,'signing:template:edit'
) x WHERE @sign_order_menu IS NOT NULL AND NOT EXISTS(SELECT 1 FROM sys_menu m WHERE m.perms=x.perms);
INSERT INTO sys_role_menu(role_id,menu_id) SELECT 1,m.menu_id FROM sys_menu m WHERE m.perms LIKE 'signing:%' AND NOT EXISTS(SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);
COMMIT;
