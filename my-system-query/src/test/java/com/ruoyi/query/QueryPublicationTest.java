package com.ruoyi.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.definition.*;
import com.ruoyi.query.engine.*;
import com.ruoyi.query.mapper.*;
import com.ruoyi.query.metadata.*;
import com.ruoyi.query.protocol.*;
import com.ruoyi.query.spi.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

/** 发布读取、权限及 MVC 输入与持久化实体之间的边界验证。 */
class QueryPublicationTest {
  private QueryAdapter adapter;
  private QueryRegistry registry;
  private QuerySnapshot snapshot;
  private FieldSpec field;
  private MetadataColumn column;

  /** 目录入口只关联后端登记的主表和明细来源，不靠前端猜测表名。 */
  @Test
  void tableLinksFollowRegisteredSources() {
    var definitions = mock(QueryDefinitionMapper.class);
    var metadata = mock(MetadataService.class);
    var definition = new QueryDefinition();
    definition.setDefinitionId(7L);
    definition.setAdapterKey("test");
    definition.setDisplayName("采购订单");
    when(definitions.selectList(any())).thenReturn(List.of(definition));
    var main = new MetadataTable();
    main.setTableId(11L);
    main.setTableName("business");
    var lines = new MetadataTable();
    lines.setTableId(12L);
    lines.setTableName("business_line");
    when(metadata.list()).thenReturn(List.of(main, lines));
    when(adapter.sources())
        .thenReturn(
            Map.of(
                "main", new QuerySource("business", "b", "主表", null),
                "lines", new QuerySource("business_line", "l", "明细", "l.parent_id=b.id")));
    var service =
        new DefinitionService(
            definitions,
            mock(QueryFieldMapper.class),
            mock(QueryReleaseMapper.class),
            mock(MetadataColumnMapper.class),
            metadata,
            registry);
    var links = service.tableLinks();
    assertEquals(2, links.size());
    assertTrue(links.stream().anyMatch(link -> link.get("tableId").equals(11L)));
    assertTrue(links.stream().anyMatch(link -> link.get("tableId").equals(12L)));
    assertTrue(links.stream().allMatch(link -> link.get("definitionId").equals(7L)));
  }

  /** 准备固定后端能力和单个已发布字段。 */
  @BeforeEach
  void setup() {
    adapter = mock(QueryAdapter.class);
    when(adapter.key()).thenReturn("test");
    when(adapter.permission()).thenReturn("test:list");
    when(adapter.sources())
        .thenReturn(Map.of("main", new QuerySource("business", "b", "测试主表", null)));
    when(adapter.defaultSort()).thenReturn("name");
    registry = new QueryRegistry(List.of(adapter));
    field = new FieldSpec();
    field.setFieldKey("name");
    field.setSourceKey("main");
    field.setColumnId(1L);
    field.setColumnName("business_name");
    field.setColumnType("varchar(100)");
    field.setDataType("STRING");
    field.setLabel("名称");
    field.setComponent("TEXT");
    field.setOperators(List.of("eq", "like"));
    field.setDefaultOperator("like");
    field.setSortable(true);
    snapshot = new QuerySnapshot();
    snapshot.setQueryKey("test");
    snapshot.setDisplayName("业务查询");
    snapshot.setConfigVersion(1);
    snapshot.setFields(List.of(field));
    column = new MetadataColumn();
    column.setColumnId(1L);
    column.setColumnName("business_name");
    column.setColumnType("varchar(100)");
    column.setAvailable(true);
    column.setNullable(field.getNullable());
    login(Set.of("test:list"));
  }

  /** 清理当前线程认证。 */
  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  /** 直接构造已认证用户，独立于管理员权限。 */
  private void login(Set<String> permissions) {
    var user = new LoginUser();
    user.setPermissions(permissions);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
  }

  /** 保存字段参数忽略客户端审计值，也不会因返回日期格式而绑定失败。 */
  @Test
  void writeDtoNeverBindsAudit() {
    var request =
        JsonMapper.builder()
            .build()
            .readValue(
                "{\"version\":1,\"fields\":[{\"fieldKey\":\"name\",\"createTime\":\"2026-10-19 12:00:00\",\"createBy\":\"forged\"}]}",
                SaveDefinitionRequest.class);
    assertEquals("name", request.getFields().get(0).getFieldKey());
    assertFalse(
        com.ruoyi.common.core.domain.AuditEntity.class.isAssignableFrom(SaveFieldRequest.class));
  }

  /** 业务列表权限可以访问运行时，不要求管理或参照权限。 */
  @Test
  void independentBusinessPermission() {
    assertSame(adapter, registry.authorized("test"));
    login(Set.of("query:definition:list"));
    assertThrows(AccessDeniedException.class, () -> registry.authorized("test"));
    login(Set.of("*:*:*"));
    assertSame(adapter, registry.authorized("test"));
  }

  /** 重复登记不能覆盖已有能力。 */
  @Test
  void duplicateRegistration() {
    assertThrows(IllegalStateException.class, () -> new QueryRegistry(List.of(adapter, adapter)));
  }

  /** 对外元数据不暴露内部列名、字段 ID 和类型定义。 */
  @Test
  void publicMetadataIsLimited() {
    var runtime = new QueryRuntimeService(null, null, null, registry, new QueryCompiler());
    var result = runtime.publicMetadata(snapshot);
    String json = JSON.toJSONString(result);
    assertFalse(json.contains("business_name"));
    assertFalse(json.contains("columnId"));
    assertFalse(json.contains("columnType"));
    assertTrue(json.contains("名称"));
  }

  /** 缓存命中仍然检查当前用户权限和目录失效。 */
  @Test
  void cacheNeverBypassesAuthorizationOrSchema() {
    var definitions = mock(DefinitionService.class);
    var releases = mock(QueryReleaseMapper.class);
    var columns = mock(MetadataColumnMapper.class);
    var definition = new QueryDefinition();
    definition.setDefinitionId(1L);
    definition.setPublishedRevision(1);
    when(definitions.byKey("test")).thenReturn(definition);
    var release = new QueryRelease();
    release.setConfigSnapshot(JSON.toJSONString(snapshot));
    when(releases.selectOne(any())).thenReturn(release);
    when(columns.selectList(any())).thenReturn(List.of(column));
    var runtime =
        new QueryRuntimeService(definitions, releases, columns, registry, new QueryCompiler());
    assertEquals(1, runtime.published("test").getConfigVersion());
    login(Set.of("query:definition:list"));
    assertThrows(AccessDeniedException.class, () -> runtime.published("test"));
    login(Set.of("test:list"));
    column.setAvailable(false);
    assertThrows(ServiceException.class, () -> runtime.published("test"));
  }

  /** 已发布版本变更后拒绝过期请求，不执行适配器。 */
  @Test
  void staleRequestNeverExecutes() {
    var definitions = mock(DefinitionService.class);
    var releases = mock(QueryReleaseMapper.class);
    var columns = mock(MetadataColumnMapper.class);
    var definition = new QueryDefinition();
    definition.setDefinitionId(1L);
    definition.setPublishedRevision(1);
    when(definitions.byKey("test")).thenReturn(definition);
    var release = new QueryRelease();
    release.setConfigSnapshot(JSON.toJSONString(snapshot));
    when(releases.selectOne(any())).thenReturn(release);
    when(columns.selectList(any())).thenReturn(List.of(column));
    var runtime =
        new QueryRuntimeService(definitions, releases, columns, registry, new QueryCompiler());
    var request = new SearchRequest();
    request.setConfigVersion(0);
    assertEquals(
        409,
        assertThrows(ServiceException.class, () -> runtime.search("test", request, Object.class))
            .getCode());
    verify(adapter, never()).search(any(), any());
  }

  /** 发布校验拒绝不匹配字段类型的运算符。 */
  @Test
  void invalidDraftCannotPublish() {
    var definitions = mock(QueryDefinitionMapper.class);
    var fields = mock(QueryFieldMapper.class);
    var releases = mock(QueryReleaseMapper.class);
    var columns = mock(MetadataColumnMapper.class);
    var metadata = mock(MetadataService.class);
    var definition = new QueryDefinition();
    definition.setDefinitionId(1L);
    definition.setQueryKey("test");
    definition.setAdapterKey("test");
    definition.setPublishedRevision(0);
    when(definitions.selectById(1L)).thenReturn(definition);
    var table = new MetadataTable();
    table.setTableId(1L);
    table.setTableName("business");
    table.setAvailable(true);
    column.setTableId(1L);
    column.setDataType("STRING");
    when(metadata.list()).thenReturn(List.of(table));
    when(columns.selectList(any())).thenReturn(List.of(column));
    var draft = new QueryField();
    draft.setEnabled(true);
    draft.setFieldKey("name");
    draft.setSourceKey("main");
    draft.setColumnId(1L);
    draft.setLabel("名称");
    draft.setOperatorsJson("[\"rawSQL\"]");
    draft.setDefaultOperator("rawSQL");
    when(fields.selectList(any())).thenReturn(List.of(draft));
    var service = new DefinitionService(definitions, fields, releases, columns, metadata, registry);
    assertThrows(ServiceException.class, () -> service.draft(1L));
    verify(releases, never()).insert(any(QueryRelease.class));
  }
}
