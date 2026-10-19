package com.ruoyi.query.metadata;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 数据目录列表投影，接入状态实时来自查询适配器，不持久化到物理目录。 */
@Getter
@Setter
public class MetadataTableView {
  /** 元数据表目录 ID。 */
  private Long tableId;

  /** 开发者登记的数据库物理表名。 */
  private String tableName;

  /** 数据库原始表注释，中文显示名称可单独维护。 */
  private String tableComment;

  /** 中文显示名称，可独立于数据库原始注释维护。 */
  private String displayName;

  /** 对应的物理表或字段是否仍有效；失效目录保留以追踪配置引用。 */
  private Boolean available;

  /** 最近一次导入或同步目录的北京时间。 */
  private LocalDateTime synchronizedTime;

  /** 目录当前编辑版本。 */
  private Integer version;

  /** 该表是否已作为某个后端查询适配器的受控来源。 */
  private boolean queryRegistered;

  /** 将目录实体和独立查询能力组合为管理页投影。 */
  public static MetadataTableView from(MetadataTable table, boolean queryRegistered) {
    MetadataTableView view = new MetadataTableView();
    view.setTableId(table.getTableId());
    view.setTableName(table.getTableName());
    view.setTableComment(table.getTableComment());
    view.setDisplayName(table.getDisplayName());
    view.setAvailable(table.getAvailable());
    view.setSynchronizedTime(table.getSynchronizedTime());
    view.setVersion(table.getVersion());
    view.setQueryRegistered(queryRegistered);
    return view;
  }
}
