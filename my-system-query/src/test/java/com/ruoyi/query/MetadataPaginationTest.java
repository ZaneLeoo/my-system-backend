package com.ruoyi.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInterceptor;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.query.mapper.MetadataTableMapper;
import com.ruoyi.query.metadata.MetadataRegistry;
import com.ruoyi.query.metadata.MetadataService;
import com.ruoyi.query.metadata.MetadataTableQuery;
import com.ruoyi.query.metadata.MetadataTableView;
import com.ruoyi.query.spi.QueryAdapter;
import com.ruoyi.query.spi.QueryRegistry;
import com.ruoyi.query.spi.QuerySource;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.ibatis.session.SqlSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

/** 使用真实 Mapper 和分页插件验证目录查询边界与总数。 */
class MetadataPaginationTest {
  private SqlSession session;
  private MetadataService service;

  /** 创建独立内存目录及足以跨页的数据，不访问业务数据库。 */
  @BeforeEach
  void setup() throws Exception {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL("jdbc:h2:mem:metadata_" + UUID.randomUUID() + ";MODE=MySQL;IGNORECASE=TRUE");
    JdbcTemplate jdbc = new JdbcTemplate(source);
    MybatisConfiguration configuration = new MybatisConfiguration();
    configuration.addMapper(MetadataTableMapper.class);
    PageInterceptor pagination = new PageInterceptor();
    Properties properties = new Properties();
    properties.setProperty("helperDialect", "h2");
    pagination.setProperties(properties);
    MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
    factory.setDataSource(source);
    factory.setConfiguration(configuration);
    factory.setPlugins(pagination);
    session = factory.getObject().openSession(true);
    session.getConnection();
    jdbc.execute(
        "CREATE TABLE sys_metadata_table(table_id BIGINT AUTO_INCREMENT PRIMARY KEY, schema_name VARCHAR(100), table_name VARCHAR(100), table_comment VARCHAR(200), display_name VARCHAR(100), structure_signature VARCHAR(64), available BOOLEAN, synchronized_time TIMESTAMP, version INT, create_by VARCHAR(64), create_time TIMESTAMP, update_by VARCHAR(64), update_time TIMESTAMP)");
    for (int i = 0; i < 25; i++) {
      jdbc.update(
          "INSERT INTO sys_metadata_table(table_name,display_name) VALUES(?,?)",
          "archive_%02d".formatted(i),
          "档案%02d".formatted(i));
    }
    jdbc.update(
        "INSERT INTO sys_metadata_table(table_name,display_name) VALUES('md_item','料品档案'),('mdXitem','其他资料'),('audit_100%','比例!档案')");
    service =
        new MetadataService(
            jdbc,
            session.getMapper(MetadataTableMapper.class),
            null,
            new MetadataRegistry(List.of()),
            new QueryRegistry(List.of()));
  }

  /** 关闭连接及分页上下文。 */
  @AfterEach
  void cleanup() {
    PageHelper.clearPage();
    if (session != null) {
      session.close();
    }
  }

  /** 分页在数据库执行，总数保留，超出末页返回空列表。 */
  @Test
  void pagesKeepFilteredTotalAndStableOrder() {
    MetadataTableQuery query = new MetadataTableQuery();
    query.setKeyword("档案");
    query.setPageSize(10);
    query.setPageNum(2);
    PageResult<MetadataTableView> result = service.page(query);
    assertEquals(27, result.getTotal());
    assertEquals(10, result.getRows().size());
    assertEquals("archive_10", result.getRows().get(0).getTableName());
    query.setPageNum(4);
    assertTrue(service.page(query).getRows().isEmpty());
    assertEquals(27, service.page(query).getTotal());
    assertNull(PageHelper.getLocalPage());
  }

  /** 中英文匹配、空白清理和字面通配符都作用于完整目录。 */
  @Test
  void keywordIsBoundAndWildcardsAreLiteral() {
    MetadataTableQuery query = new MetadataTableQuery();
    query.setKeyword("  MD_ITEM  ");
    assertEquals("MD_ITEM", query.getKeyword());
    assertEquals(1, service.page(query).getTotal());
    query.setKeyword("料品");
    assertEquals("md_item", service.page(query).getRows().get(0).getTableName());
    for (String literal : List.of("100%", "!", "' OR 1=1 --")) {
      query.setKeyword(literal);
      assertEquals(literal.startsWith("'") ? 0 : 1, service.page(query).getTotal());
    }
    query.setKeyword("  ");
    assertNull(query.getKeyword());
    assertEquals(28, service.page(query).getTotal());
  }

  /** 列表接入状态独立于配置维护权限和目录导入状态，且布尔属性名称稳定。 */
  @Test
  void pageSeparatesCatalogFromQueryRegistration() {
    QueryAdapter adapter = mock(QueryAdapter.class);
    when(adapter.key()).thenReturn("masterdata.item");
    when(adapter.sources())
        .thenReturn(java.util.Map.of("main", new QuerySource("md_item", "i", "料品", null)));
    MetadataService connected =
        new MetadataService(
            null,
            session.getMapper(MetadataTableMapper.class),
            null,
            new MetadataRegistry(List.of()),
            new QueryRegistry(List.of(adapter)));
    MetadataTableQuery query = new MetadataTableQuery();
    query.setKeyword("md");
    PageResult<MetadataTableView> page = connected.page(query);
    assertEquals(2, page.getTotal());
    assertTrue(
        page.getRows().stream()
            .filter(row -> row.getTableName().equals("md_item"))
            .findFirst()
            .orElseThrow()
            .isQueryRegistered());
    assertFalse(
        page.getRows().stream()
            .filter(row -> row.getTableName().equals("mdXitem"))
            .findFirst()
            .orElseThrow()
            .isQueryRegistered());
    String json = tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(page);
    assertTrue(json.contains("\"queryRegistered\":true"));
    assertTrue(json.contains("\"queryRegistered\":false"));
    assertFalse(json.contains("structureSignature"));
  }

  /** HTTP 参数校验及服务端分页限制不能被非法页大小绕过。 */
  @Test
  void rejectsInvalidPaginationAndOversizedKeyword() {
    MetadataTableQuery query = new MetadataTableQuery();
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      assertTrue(factory.getValidator().validate(query).isEmpty());
      query.setPageSize(101);
      assertFalse(factory.getValidator().validate(query).isEmpty());
      assertThrows(ServiceException.class, () -> service.page(query));
      query.setPageSize(20);
      query.setKeyword("a".repeat(201));
      assertFalse(factory.getValidator().validate(query).isEmpty());
    }
  }

  /** 查询失败也清理分页标记，避免污染随后普通查询。 */
  @Test
  void mapperFailureClearsPagination() {
    MetadataTableMapper mapper = mock(MetadataTableMapper.class);
    when(mapper.selectList(any())).thenThrow(new IllegalStateException("模拟查询失败"));
    MetadataService failing = new MetadataService(null, mapper, null, null, null);
    assertThrows(IllegalStateException.class, () -> failing.page(new MetadataTableQuery()));
    assertNull(PageHelper.getLocalPage());
  }
}
