package com.ruoyi.business.masterdata.supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.business.masterdata.supplier.controller.SupplierController;
import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.dto.SupplierQuery;
import com.ruoyi.business.masterdata.supplier.service.SupplierService;
import com.ruoyi.business.masterdata.supplier.service.impl.SupplierServiceImpl;
import com.ruoyi.business.masterdata.supplier.vo.SupplierView;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.config.JacksonConfig;
import com.ruoyi.framework.config.MyBatisConfig;
import com.ruoyi.framework.mybatis.BusinessAuditHandler;
import com.ruoyi.framework.web.exception.BusinessExceptionHandler;
import com.ruoyi.framework.web.service.PermissionService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.json.JsonMapper;

/** 供应商档案的真实 MP、分页、事务、权限和 MVC 集成验证。 */
class SupplierIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private SupplierService service;
  private JdbcTemplate jdbc;
  private Connection schemaConnection;
  private MockMvc mvc;
  private static final String BASE = "/masterdata/supplier";

  /** 从生产增量脚本建立隔离表，并启用实际分页及审计配置。 */
  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL(
        "jdbc:h2:mem:"
            + UUID.randomUUID()
            + ";MODE=MySQL;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
    context = new AnnotationConfigApplicationContext();
    context
        .getEnvironment()
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "test",
                Map.of(
                    "mybatis.typeAliasesPackage", "com.ruoyi.**.domain",
                    "mybatis.mapperLocations", "classpath*:mapper/none/*.xml",
                    "mybatis.configLocation", "classpath:category-mybatis.xml",
                    "pagehelper.helperDialect", "mysql")));
    context.registerBean(DataSource.class, () -> source);
    context.register(TestConfig.class);
    context.refresh();
    jdbc = new JdbcTemplate(source);
    Path migration = Path.of("../sql/migrations/20261017_01_supplier.sql");
    if (!Files.exists(migration)) migration = Path.of("sql/migrations/20261017_01_supplier.sql");
    String schema =
        Files.readString(migration)
            .split("START TRANSACTION;")[0]
            .replaceAll("(?m)^SET NAMES.*;", "")
            .replaceAll("ENGINE=InnoDB[^;]*", "");
    schemaConnection = source.getConnection();
    try (var sql = schemaConnection.createStatement()) {
      for (String statement : schema.split(";")) if (!statement.isBlank()) sql.execute(statement);
    }
    service = context.getBean(SupplierService.class);
    com.ruoyi.business.masterdata.common.MasterDataTestSchema.create(schemaConnection);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login("creator", Set.of("*:*:*"));
    JsonMapper.Builder builder = JsonMapper.builder();
    context.getBean(JacksonConfig.class).localDateTimeCustomizer().customize(builder);
    mvc =
        MockMvcBuilders.standaloneSetup(
                context.getBean(SupplierController.class),
                context.getBean(
                    com.ruoyi.business.masterdata.supplier.controller.SupplierReferenceController
                        .class))
            .setControllerAdvice(new BusinessExceptionHandler())
            .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
            .build();
  }

  /** 清理线程状态、数据库连接及 Spring 上下文。 */
  @AfterEach
  void tearDown() throws Exception {
    PageHelper.clearPage();
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
    if (schemaConnection != null) schemaConnection.close();
    if (context != null) context.close();
  }

  /** 审计保护、启停修改、版本递增和实体逻辑删除均有效。 */
  @Test
  void auditVersionsAndLogicalDeletion() {
    SaveSupplierRequest input = request(null, " KG ", " 千克 ", null);
    input.setRemark(null);
    SupplierView created = service.create(input);
    assertEquals("KG", created.getSupplierCode());
    assertEquals("千克", created.getSupplierName());
    assertEquals("", created.getRemark());
    assertEquals("creator", created.getCreateBy());
    login("editor", Set.of("*:*:*"));
    SaveSupplierRequest edit = request(created.getSupplierId(), "KG", "公斤", 0);
    edit.setStatus(EnableStatus.DISABLED);
    service.update(edit);
    SupplierView updated = service.detail(created.getSupplierId());
    assertEquals(1, updated.getVersion());
    assertEquals(EnableStatus.DISABLED, updated.getStatus());
    assertEquals(created.getCreateTime(), updated.getCreateTime());
    assertEquals("creator", updated.getCreateBy());
    assertEquals("editor", updated.getUpdateBy());
    assertThrows(ServiceException.class, () -> service.update(edit));
    assertThrows(ServiceException.class, () -> service.delete(created.getSupplierId(), 0));
    login("deleter", Set.of("*:*:*"));
    service.delete(created.getSupplierId(), 1);
    assertThrows(ServiceException.class, () -> service.detail(created.getSupplierId()));
    assertEquals(0, service.list(new SupplierQuery()).getTotal());
    var deleted =
        jdbc.queryForMap(
            "SELECT del_flag, create_by, update_by, update_time FROM md_supplier WHERE supplier_id=?",
            created.getSupplierId());
    assertEquals("2", deleted.get("DEL_FLAG"));
    assertEquals("creator", deleted.get("CREATE_BY"));
    assertEquals("deleter", deleted.get("UPDATE_BY"));
    assertNotNull(deleted.get("UPDATE_TIME"));
    assertThrows(ServiceException.class, () -> service.create(request(null, "KG", "新名称", null)));
    assertDoesNotThrow(() -> service.create(request(null, "NEW", "公斤", null)));
  }

  /** 编码不区分大小写唯一、名称允许重复，失败修改不改变版本及数据。 */
  @Test
  void uniquenessIncludesDeletedRowsAndRollsBackEdits() {
    service.create(request(null, "EA", "Each", null));
    SupplierView other = service.create(request(null, "KG", "千克", null));
    assertThrows(ServiceException.class, () -> service.create(request(null, "ea", "其他", null)));
    assertDoesNotThrow(() -> service.create(request(null, "NEW", "each", null)));
    assertThrows(
        ServiceException.class,
        () -> service.update(request(other.getSupplierId(), "ea", "新名称", 0)));
    assertEquals(0, service.detail(other.getSupplierId()).getVersion());
    assertEquals("千克", service.detail(other.getSupplierId()).getSupplierName());
    assertThrows(ServiceException.class, () -> service.create(request(1L, "A", "甲", null)));
    assertThrows(ServiceException.class, () -> service.create(request(null, "A", "甲", 0)));
    assertThrows(
        ServiceException.class,
        () -> service.update(request(other.getSupplierId(), "KG", "千克", null)));
  }

  /** 分页总数独立于页大小，排序作用于全部结果，组合条件及通配字符按文本处理。 */
  @Test
  void serverPaginationSortAndLiteralFilters() {
    for (String code : List.of("D", "A", "C", "B", "E"))
      service.create(request(null, code, "供应商" + code, null));
    SupplierQuery query = new SupplierQuery();
    query.setPageSize(2);
    query.setPageNum(2);
    var second = service.list(query);
    assertEquals(5, second.getTotal());
    assertEquals(
        List.of("C", "D"), second.getRows().stream().map(SupplierView::getSupplierCode).toList());
    query.setSortField("supplierName");
    query.setSortDirection("desc");
    assertEquals(
        List.of("C", "B"),
        service.list(query).getRows().stream().map(SupplierView::getSupplierCode).toList());
    query.setPageNum(99);
    assertTrue(service.list(query).getRows().isEmpty());
    assertEquals(5, service.list(query).getTotal());
    service.create(request(null, "X_%", "百分号供应商", null));
    query = new SupplierQuery();
    query.setSupplierCode("_%");
    query.setSupplierName("百分号");
    query.setStatus("0");
    assertEquals(1, service.list(query).getTotal());
    query.setStatus("1");
    assertEquals(0, service.list(query).getTotal());
    assertNull(PageHelper.getLocalPage());
    query.setSortField("supplierCode; DROP TABLE md_supplier");
    SupplierQuery invalid = query;
    assertThrows(ServiceException.class, () -> service.list(invalid));
    assertNull(PageHelper.getLocalPage());
  }

  /** SQL 执行失败后分页上下文仍清理，后续详情查询不受影响。 */
  @Test
  void paginationContextIsClearedAfterMapperFailure() {
    SupplierView supplier = service.create(request(null, "A", "甲", null));
    jdbc.execute("ALTER TABLE md_supplier RENAME TO unavailable_supplier");
    try {
      assertThrows(RuntimeException.class, () -> service.list(new SupplierQuery()));
      assertNull(PageHelper.getLocalPage());
    } finally {
      jdbc.execute("ALTER TABLE unavailable_supplier RENAME TO md_supplier");
    }
    assertEquals("甲", service.detail(supplier.getSupplierId()).getSupplierName());
  }

  /** MVC 绑定、泛型分页和北京时间字符串格式保持明确契约。 */
  @Test
  void mvcBindingAndResponseContract() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"supplierCode":" KG ","supplierName":" 千克 ","status":"0","remark":null}
        """))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.supplierCode").value("KG"))
        .andExpect(jsonPath("$.data.supplierName").value("千克"))
        .andExpect(jsonPath("$.data.remark").value(""))
        .andExpect(jsonPath("$.data.status").value("0"))
        .andExpect(
            jsonPath("$.data.createTime")
                .value(
                    org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
        .andExpect(jsonPath("$.data.delFlag").doesNotExist())
        .andExpect(jsonPath("$.data.sortOrder").doesNotExist());
    service.create(request(null, "EA", "个", null));
    mvc.perform(get(BASE + "/list").param("pageNum", "1").param("pageSize", "1"))
        .andExpect(jsonPath("$.data.total").value(2))
        .andExpect(jsonPath("$.data.rows.length()").value(1))
        .andExpect(jsonPath("$.data.rows[0].supplierCode").value("EA"))
        .andExpect(jsonPath("$.data.code").doesNotExist())
        .andExpect(jsonPath("$.data.msg").doesNotExist());
    mvc.perform(
            get(BASE + "/list")
                .param("supplierCode", " KG ")
                .param("supplierName", "千")
                .param("status", "0"))
        .andExpect(jsonPath("$.data.total").value(1));
  }

  /** 非法查询、JSON 和版本参数通过统一 R 错误响应拒绝。 */
  @Test
  void invalidInputUsesBusinessErrors() throws Exception {
    for (var entry :
        Map.of(
                "pageNum",
                "0",
                "pageSize",
                "101",
                "status",
                "9",
                "sortField",
                "remark",
                "sortDirection",
                "sideways")
            .entrySet()) {
      mvc.perform(get(BASE + "/list").param(entry.getKey(), entry.getValue()))
          .andExpect(jsonPath("$.code").value(500));
    }
    for (String json :
        List.of(
            "{\"supplierCode\":\" \",\"supplierName\":\"名称\",\"status\":\"0\"}",
            "{\"supplierCode\":\"A\",\"supplierName\":\"名称\",\"status\":\"9\"}",
            "{\"supplierCode\":\"A\",\"supplierName\":\"名称\"}",
            "{\"supplierCode\":\""
                + "A".repeat(65)
                + "\",\"supplierName\":\"名称\",\"status\":\"0\"}")) {
      mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
          .andExpect(jsonPath("$.code").value(500));
    }
    mvc.perform(delete(BASE + "/1")).andExpect(jsonPath("$.msg").value("缺少必要参数：version"));
    mvc.perform(get(BASE + "/-1")).andExpect(jsonPath("$.code").value(500));
    mvc.perform(get(BASE + "/9999")).andExpect(jsonPath("$.msg").value("供应商不存在或已删除"));
  }

  /** 联系信息通过 JSON 与 Setter 统一清理空白，允许独立填写和清空。 */
  @Test
  void contactFieldsNormalizeAndValidate() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"supplierCode":" S01 ","supplierName":" 测试供应商 ","shortName":" 测试 ",
        "contactName":" 张三 ","contactPhone":" +86 010-12345678 转 8 ",
        "address":" 北京市朝阳区 ","status":"0"}
        """))
        .andExpect(jsonPath("$.data.shortName").value("测试"))
        .andExpect(jsonPath("$.data.contactName").value("张三"))
        .andExpect(jsonPath("$.data.contactPhone").value("+86 010-12345678 转 8"))
        .andExpect(jsonPath("$.data.address").value("北京市朝阳区"));
    SupplierView created = service.list(new SupplierQuery()).getRows().get(0);
    SaveSupplierRequest edit =
        request(created.getSupplierId(), "S01", "测试供应商", created.getVersion());
    edit.setShortName(null);
    edit.setContactName(null);
    edit.setContactPhone("   ");
    edit.setAddress(null);
    service.update(edit);
    SupplierView cleared = service.detail(created.getSupplierId());
    assertEquals("", cleared.getShortName());
    assertEquals("", cleared.getContactName());
    assertEquals("", cleared.getContactPhone());
    assertEquals("", cleared.getAddress());
    for (var field :
        Map.of("shortName", 100, "contactName", 64, "contactPhone", 64, "address", 500)
            .entrySet()) {
      mvc.perform(
              post(BASE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"supplierCode\":\"NEW\",\"supplierName\":\"名称\",\"status\":\"0\",\""
                          + field.getKey()
                          + "\":\""
                          + "字".repeat(field.getValue() + 1)
                          + "\"}"))
          .andExpect(jsonPath("$.code").value(500));
    }
  }

  /** 参照权限独立有效，完整选项保留停用项、排除已删除项且不泄露内部字段。 */
  @Test
  void referencesHaveIndependentPermissionsAndCompleteProjection() throws Exception {
    for (int i = 0; i < 101; i++) service.create(request(null, "S" + i, "供应商" + i, null));
    SupplierView disabled = service.create(request(null, "OFF", "停用供应商", null));
    SaveSupplierRequest edit = request(disabled.getSupplierId(), "OFF", "停用供应商", 0);
    edit.setStatus(EnableStatus.DISABLED);
    service.update(edit);
    SupplierView deleted = service.create(request(null, "DEL", "删除供应商", null));
    service.delete(deleted.getSupplierId(), 0);
    login("reference", Set.of("masterdata:supplier:reference"));
    mvc.perform(get("/masterdata/reference/suppliers"))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.length()").value(102))
        .andExpect(jsonPath("$.data[0].supplierCode").value("OFF"))
        .andExpect(jsonPath("$.data[0].status").value("1"))
        .andExpect(jsonPath("$.data[0].createBy").doesNotExist())
        .andExpect(jsonPath("$.data[0].version").doesNotExist())
        .andExpect(jsonPath("$.data[0].delFlag").doesNotExist());
    mvc.perform(get(BASE + "/list")).andExpect(jsonPath("$.code").value(403));
    login("list", Set.of("masterdata:supplier:list"));
    mvc.perform(get("/masterdata/reference/suppliers")).andExpect(jsonPath("$.code").value(403));
    login("none", Set.of());
    mvc.perform(get("/masterdata/reference/suppliers")).andExpect(jsonPath("$.code").value(403));
  }

  /** 每个读写入口分别校验权限，按钮显隐不能替代服务端授权。 */
  @Test
  void permissionsGuardEveryEndpoint() throws Exception {
    login("reader", Set.of("masterdata:supplier:list"));
    SupplierController controller = context.getBean(SupplierController.class);
    assertEquals(200, controller.list(new SupplierQuery()).getCode());
    assertThrows(AccessDeniedException.class, () -> controller.detail(1L));
    assertThrows(
        AccessDeniedException.class, () -> controller.create(request(null, "A", "甲", null)));
    assertThrows(AccessDeniedException.class, () -> controller.update(request(1L, "A", "甲", 0)));
    assertThrows(AccessDeniedException.class, () -> controller.delete(1L, 0));
    mvc.perform(get(BASE + "/1")).andExpect(jsonPath("$.code").value(403));
  }

  /** 同编码并发新增和同版本并发修改均只允许一方成功。 */
  @Test
  void concurrentCreatesAndUpdatesHaveSingleWinner() throws Exception {
    assertEquals(
        1,
        race(
            () -> {
              service.create(request(null, "A", "甲", null));
              return true;
            },
            () -> {
              service.create(request(null, "A", "乙", null));
              return true;
            }));
    SupplierView supplier = service.list(new SupplierQuery()).getRows().get(0);
    assertEquals(
        1,
        race(
            () -> {
              service.update(request(supplier.getSupplierId(), "A", "修改甲", 0));
              return true;
            },
            () -> {
              service.update(request(supplier.getSupplierId(), "A", "修改乙", 0));
              return true;
            }));
    assertEquals(1, service.detail(supplier.getSupplierId()).getVersion());
    assertEquals(
        1,
        race(
            () -> {
              service.update(request(supplier.getSupplierId(), "A", "修改丙", 1));
              return true;
            },
            () -> {
              service.delete(supplier.getSupplierId(), 1);
              return true;
            }));
  }

  /** 让两个独立身份和事务同时进入业务方法，并汇总成功次数。 */
  private int race(Supplier<Boolean> first, Supplier<Boolean> second) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> tasks = new ArrayList<>();
      for (Supplier<Boolean> action : List.of(first, second)) {
        tasks.add(
            pool.submit(
                () -> {
                  login("concurrent", Set.of("*:*:*"));
                  try {
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return action.get();
                  } catch (ServiceException expected) {
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      }
      start.countDown();
      return (tasks.get(0).get(15, TimeUnit.SECONDS) ? 1 : 0)
          + (tasks.get(1).get(15, TimeUnit.SECONDS) ? 1 : 0);
    } finally {
      pool.shutdownNow();
    }
  }

  /** 通过 Setter 创建普通 Java 请求，使用与 JSON 相同的规范化规则。 */
  private SaveSupplierRequest request(Long id, String code, String name, Integer version) {
    SaveSupplierRequest request = new SaveSupplierRequest();
    request.setSupplierId(id);
    request.setSupplierCode(code);
    request.setSupplierName(name);
    request.setStatus(EnableStatus.ENABLED);
    request.setVersion(version);
    return request;
  }

  /** 设置与运行环境一致的登录身份。 */
  private void login(String username, Set<String> permissions) {
    SysUser user = new SysUser();
    user.setUserName(username);
    LoginUser principal = new LoginUser(2L, 1L, user, permissions);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  /** 启用真实事务、权限、工厂与 PageHelper 自动配置。 */
  @Configuration
  @EnableTransactionManagement
  @EnableMethodSecurity
  @MapperScan({
    "com.ruoyi.business.masterdata.supplier.mapper",
    "com.ruoyi.business.masterdata.common.mapper"
  })
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    SupplierServiceImpl.class,
    SupplierController.class,
    com.ruoyi.business.masterdata.supplier.controller.SupplierReferenceController.class,
    PermissionService.class,
    PageHelperAutoConfiguration.class,
    JacksonConfig.class
  })
  static class TestConfig {
    /** 对 Controller 启用方法参数校验。 */
    @Bean
    static MethodValidationPostProcessor methodValidationPostProcessor() {
      return new MethodValidationPostProcessor();
    }

    /** 将生产会话工厂接入 Spring Mapper。 */
    @Bean
    SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory factory) {
      return new SqlSessionTemplate(factory);
    }

    /** 提供 JDBC 事务和真实行锁生命周期。 */
    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }
}
