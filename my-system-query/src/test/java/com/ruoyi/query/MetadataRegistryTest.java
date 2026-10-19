package com.ruoyi.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.mapper.MetadataColumnMapper;
import com.ruoyi.query.mapper.MetadataTableMapper;
import com.ruoyi.query.metadata.*;
import com.ruoyi.query.spi.QueryRegistry;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** 目录独立登记、导入边界及已有配置保护。 */
class MetadataRegistryTest {
  /** 目录集合不能被外部修改，登记冲突和非法标识在启动时失败。 */
  @Test
  void registrationIsExplicitAndImmutable() {
    var registry = new MetadataRegistry(List.of(() -> Map.of("md_unit", "计量单位")));
    assertEquals(Set.of("md_unit"), registry.tables());
    assertEquals("计量单位", registry.displayName("md_unit"));
    assertThrows(UnsupportedOperationException.class, () -> registry.tables().add("sys_user"));
    assertThrows(
        IllegalStateException.class,
        () -> new MetadataRegistry(List.of(() -> Map.of("md_unit;DROP TABLE sys_user", "无效表"))));
    assertThrows(
        IllegalStateException.class,
        () ->
            new MetadataRegistry(
                List.of(() -> Map.of("md_unit", "计量单位"), () -> Map.of("md_unit", "冲突名称"))));
  }

  /** 未登记系统表即使实际存在也不得读取结构或导入。 */
  @Test
  void unregisteredTableNeverTouchesDatabase() {
    var jdbc = mock(JdbcTemplate.class);
    var tables = mock(MetadataTableMapper.class);
    var columns = mock(MetadataColumnMapper.class);
    var service =
        new MetadataService(
            jdbc,
            tables,
            columns,
            new MetadataRegistry(List.of(() -> Map.of("md_unit", "计量单位"))),
            new QueryRegistry(List.of()));
    assertThrows(ServiceException.class, () -> service.importTable("sys_user"));
    verifyNoInteractions(jdbc, tables, columns);
  }

  /** 未实现查询适配器的单位表仍可导入，收录本身不会创建查询能力。 */
  @Test
  void importsCatalogWithoutAnyQueryAdapter() {
    var jdbc = mock(JdbcTemplate.class);
    var tables = mock(MetadataTableMapper.class);
    var columns = mock(MetadataColumnMapper.class);
    var queries = new QueryRegistry(List.of());
    var registry = new MetadataRegistry(List.of(() -> Map.of("md_unit", "计量单位")));
    when(jdbc.queryForObject("SELECT DATABASE()", String.class)).thenReturn("my_system");
    when(jdbc.queryForList(anyString(), any(Object[].class)))
        .thenReturn(List.of(new HashMap<>(Map.of("tableName", "md_unit", "tableComment", ""))));
    var column = new MetadataColumn();
    column.setColumnName("unit_code");
    column.setColumnType("varchar(64)");
    column.setNullable(false);
    column.setColumnComment("单位编码");
    column.setOrdinalPosition(1);
    when(jdbc.query(
            anyString(),
            org.mockito.ArgumentMatchers.<RowMapper<MetadataColumn>>any(),
            any(Object[].class)))
        .thenReturn(List.of(column));
    doAnswer(
            invocation -> {
              MetadataTable table = invocation.getArgument(0);
              table.setTableId(7L);
              return 1;
            })
        .when(tables)
        .insert(any(MetadataTable.class));
    var service = new MetadataService(jdbc, tables, columns, registry, queries);
    var imported = service.importTable("md_unit");
    assertEquals("计量单位", imported.getDisplayName());
    assertEquals(0, imported.getVersion());
    assertEquals(7L, column.getTableId());
    assertEquals(MetadataService.signature(List.of(column)), imported.getStructureSignature());
    verify(columns).insert(column);
    assertTrue(queries.sourceTables().isEmpty());
    assertThrows(ServiceException.class, () -> queries.require("masterdata.unit"));

    when(tables.selectOne(any())).thenReturn(imported);
    clearInvocations(tables, columns, jdbc);
    imported.setDisplayName("已有自定义名称");
    imported.setVersion(9);
    assertSame(imported, service.importTable("md_unit"));
    assertEquals("已有自定义名称", imported.getDisplayName());
    assertEquals(9, imported.getVersion());
    verify(tables, never()).insert(any(MetadataTable.class));
    verifyNoInteractions(columns);
  }

  /** 候选表包含中文名称和已导入状态，查询使用绑定表名。 */
  @Test
  void candidatesDescribeImportStateWithoutQueryPermissions() {
    var jdbc = mock(JdbcTemplate.class);
    var tables = mock(MetadataTableMapper.class);
    when(jdbc.queryForObject("SELECT DATABASE()", String.class)).thenReturn("my_system");
    when(jdbc.queryForList(anyString(), any(Object[].class)))
        .thenReturn(List.of(new HashMap<>(Map.of("tableName", "md_unit", "tableComment", ""))));
    var imported = new MetadataTable();
    imported.setTableName("md_unit");
    when(tables.selectList(any())).thenReturn(List.of(imported));
    var service =
        new MetadataService(
            jdbc,
            tables,
            null,
            new MetadataRegistry(List.of(() -> Map.of("md_unit", "计量单位"))),
            new QueryRegistry(List.of()));
    var candidate = service.candidates().get(0);
    assertEquals("计量单位", candidate.get("displayName"));
    assertEquals(true, candidate.get("imported"));
    var bound = ArgumentCaptor.forClass(Object[].class);
    verify(jdbc).queryForList(contains("TABLE_NAME IN (?)"), bound.capture());
    assertArrayEquals(new Object[] {"md_unit"}, bound.getValue());
  }
}
