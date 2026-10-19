package com.ruoyi.query.metadata;

import java.time.LocalDateTime;

/** 数据目录列表投影，接入状态实时来自查询适配器，不持久化到物理目录。 */
public class MetadataTableView {
  private Long tableId;
  private String tableName;
  private String tableComment;
  private String displayName;
  private Boolean available;
  private LocalDateTime synchronizedTime;
  private Integer version;
  private boolean queryRegistered;

  /** 将目录实体和独立查询能力组合为管理页投影。 */
  public static MetadataTableView from(MetadataTable table, boolean queryRegistered) {
    var view = new MetadataTableView();
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

  /** 返回目录 ID。 */
  public Long getTableId() {
    return tableId;
  }

  /** 设置目录 ID。 */
  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  /** 返回物理表名。 */
  public String getTableName() {
    return tableName;
  }

  /** 设置物理表名。 */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /** 返回原始表注释。 */
  public String getTableComment() {
    return tableComment;
  }

  /** 设置原始表注释。 */
  public void setTableComment(String tableComment) {
    this.tableComment = tableComment;
  }

  /** 返回中文目录名称。 */
  public String getDisplayName() {
    return displayName;
  }

  /** 设置中文目录名称。 */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /** 返回目录结构是否有效。 */
  public Boolean getAvailable() {
    return available;
  }

  /** 设置目录结构是否有效。 */
  public void setAvailable(Boolean available) {
    this.available = available;
  }

  /** 返回最近同步时间。 */
  public LocalDateTime getSynchronizedTime() {
    return synchronizedTime;
  }

  /** 设置最近同步时间。 */
  public void setSynchronizedTime(LocalDateTime synchronizedTime) {
    this.synchronizedTime = synchronizedTime;
  }

  /** 返回目录并发版本。 */
  public Integer getVersion() {
    return version;
  }

  /** 设置目录并发版本。 */
  public void setVersion(Integer version) {
    this.version = version;
  }

  /** 返回该表是否被已登记查询适配器用作数据来源。 */
  public boolean isQueryRegistered() {
    return queryRegistered;
  }

  /** 设置查询适配器接入状态。 */
  public void setQueryRegistered(boolean queryRegistered) {
    this.queryRegistered = queryRegistered;
  }
}
