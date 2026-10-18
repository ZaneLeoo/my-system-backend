package com.ruoyi.business.masterdata.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.business.masterdata.warehouse.controller.WarehouseController;
import com.ruoyi.business.masterdata.warehouse.dto.SaveWarehouseRequest;
import com.ruoyi.business.masterdata.warehouse.dto.WarehouseQuery;
import com.ruoyi.business.masterdata.warehouse.service.WarehouseService;
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

/** 单位档案的真实 MP、分页、事务、权限和 MVC 集成验证。 */
class StorageIntegrationTest {
  private com.ruoyi.business.masterdata.location.service.LocationService locations;
  private com.ruoyi.business.masterdata.reference.service.StorageReferenceService references;

  private AnnotationConfigApplicationContext context;
  private WarehouseService service;
  private JdbcTemplate jdbc;
  private Connection schemaConnection;
  private MockMvc mvc;
  private static final String BASE = "/masterdata/warehouse";

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
    Path migration = Path.of("../sql/migrations/20261017_03_warehouse_location.sql");
    if (!Files.exists(migration))
      migration = Path.of("sql/migrations/20261017_03_warehouse_location.sql");
    String schema =
        Files.readString(migration)
            .split("START TRANSACTION;")[0]
            .replaceAll("(?m)^SET NAMES.*;", "")
            .replaceAll("ENGINE=InnoDB[^;]*", "");
    schemaConnection = source.getConnection();
    try (var sql = schemaConnection.createStatement()) {
      for (String statement : schema.split(";")) if (!statement.isBlank()) sql.execute(statement);
    }

    service = context.getBean(WarehouseService.class);
    locations =
        context.getBean(com.ruoyi.business.masterdata.location.service.LocationService.class);
    references =
        context.getBean(
            com.ruoyi.business.masterdata.reference.service.StorageReferenceService.class);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login("creator", Set.of("*:*:*"));
    JsonMapper.Builder builder = JsonMapper.builder();
    context.getBean(JacksonConfig.class).localDateTimeCustomizer().customize(builder);
    mvc =
        MockMvcBuilders.standaloneSetup(
                context.getBean(WarehouseController.class),
                context.getBean(
                    com.ruoyi.business.masterdata.location.controller.LocationController.class),
                context.getBean(
                    com.ruoyi.business.masterdata.reference.controller.StorageReferenceController
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

  /** 默认值、绑定、规范化、参数约束与响应字段均符合公开约定。 */
  @Test
  void bindingDefaultsAndValidation() throws Exception {
    mvc.perform(
            post(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"warehouseCode":" W1 ","warehouseName":" 中文仓库 ","remark":null,"createBy":"spoof"}
                    """))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.warehouseCode").value("W1"))
        .andExpect(jsonPath("$.data.locationEnabled").value(false))
        .andExpect(jsonPath("$.data.createBy").value("creator"))
        .andExpect(
            jsonPath("$.data.createTime")
                .value(
                    org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
        .andExpect(jsonPath("$.data.delFlag").doesNotExist());
    var invalid = warehouse(" ", true);
    assertThrows(
        jakarta.validation.ConstraintViolationException.class, () -> service.create(invalid));
    invalid.setWarehouseCode("X");
    invalid.setLocationEnabled(null);
    assertThrows(
        jakarta.validation.ConstraintViolationException.class, () -> service.create(invalid));
    var explicit = warehouse("ID", true);
    explicit.setWarehouseId(99L);
    assertThrows(ServiceException.class, () -> service.create(explicit));
    var query = new WarehouseQuery();
    query.setSortField("warehouse_code; DROP TABLE md_location");
    assertThrows(jakarta.validation.ConstraintViolationException.class, () -> service.list(query));
    query.setSortField(null);
    query.setPageSize(101);
    assertThrows(jakarta.validation.ConstraintViolationException.class, () -> service.list(query));
    assertNull(PageHelper.getLocalPage());
  }

  /** 同仓编码占用包含删除记录，不同仓库可同码，名称可重复。 */
  @Test
  void uniquenessAndImmutableWarehouse() {
    var w1 = service.create(warehouse("W1", true));
    var w2 = service.create(warehouse("W2", true));
    assertThrows(ServiceException.class, () -> service.create(warehouse("w1", true)));
    var a = locations.create(location(w1.getWarehouseId(), "A-01"));
    locations.create(location(w2.getWarehouseId(), "a-01"));
    assertThrows(
        ServiceException.class, () -> locations.create(location(w1.getWarehouseId(), "a-01")));
    var changed = location(w2.getWarehouseId(), "A-02");
    changed.setLocationId(a.getLocationId());
    changed.setVersion(0);
    assertThrows(ServiceException.class, () -> locations.update(changed));
    locations.delete(a.getLocationId(), 0);
    assertThrows(
        ServiceException.class, () -> locations.create(location(w1.getWarehouseId(), "A-01")));
    assertEquals(0, query(w1.getWarehouseId()).getTotal());
    service.delete(w1.getWarehouseId(), 0);
    assertThrows(ServiceException.class, () -> service.create(warehouse("W1", true)));
  }

  /** 父仓停用不改写货位状态，阻止新增及重新启用，但允许维护已有货位。 */
  @Test
  void disabledWarehouseKeepsLocationStatus() {
    var w = service.create(warehouse("W", true));
    var l = locations.create(location(w.getWarehouseId(), "A"));
    var off = warehouse("W", true);
    off.setWarehouseId(w.getWarehouseId());
    off.setVersion(0);
    off.setStatus(EnableStatus.DISABLED);
    service.update(off);
    assertEquals(EnableStatus.ENABLED, locations.detail(l.getLocationId()).getStatus());
    assertThrows(ServiceException.class, () -> locations.create(location(w.getWarehouseId(), "B")));
    var q = new com.ruoyi.business.masterdata.location.dto.LocationQuery();
    q.setWarehouseId(w.getWarehouseId());
    assertEquals(0, references.locations(q).getTotal());
    assertEquals(1, references.locations(w.getWarehouseId(), List.of(l.getLocationId())).size());
    var edit = location(w.getWarehouseId(), "A2");
    edit.setLocationId(l.getLocationId());
    edit.setVersion(0);
    locations.update(edit);
    edit.setVersion(1);
    edit.setStatus(EnableStatus.DISABLED);
    locations.update(edit);
    edit.setVersion(2);
    edit.setStatus(EnableStatus.ENABLED);
    assertThrows(ServiceException.class, () -> locations.update(edit));
    off.setStatus(EnableStatus.ENABLED);
    off.setVersion(1);
    service.update(off);
    locations.update(edit);
    assertEquals(1, references.locations(q).getTotal());
  }

  /** 关闭货位管理和删除仓库均受全部未删除货位保护，实体删除触发审计。 */
  @Test
  void versionsAuditAndParentGuards() {
    var w = service.create(warehouse("W", true));
    var l = locations.create(location(w.getWarehouseId(), "A"));
    var edit = warehouse("W", false);
    edit.setWarehouseId(w.getWarehouseId());
    edit.setVersion(0);
    assertThrows(ServiceException.class, () -> service.update(edit));
    assertThrows(ServiceException.class, () -> service.delete(w.getWarehouseId(), 0));
    var le = location(w.getWarehouseId(), "A");
    le.setLocationId(l.getLocationId());
    le.setVersion(0);
    le.setStatus(EnableStatus.DISABLED);
    locations.update(le);
    assertThrows(ServiceException.class, () -> locations.update(le));
    assertThrows(ServiceException.class, () -> locations.delete(l.getLocationId(), 0));
    assertThrows(ServiceException.class, () -> service.update(edit));
    login("deleter", Set.of("*:*:*"));
    locations.delete(l.getLocationId(), 1);
    assertEquals(
        "deleter",
        jdbc.queryForObject(
            "SELECT update_by FROM md_location WHERE location_id=?",
            String.class,
            l.getLocationId()));
    assertEquals(
        "2",
        jdbc.queryForObject(
            "SELECT del_flag FROM md_location WHERE location_id=?",
            String.class,
            l.getLocationId()));
    service.update(edit);
    assertEquals(1, service.detail(w.getWarehouseId()).getVersion());
    assertThrows(ServiceException.class, () -> service.delete(w.getWarehouseId(), 0));
    service.delete(w.getWarehouseId(), 1);
    assertEquals(
        "creator",
        jdbc.queryForObject(
            "SELECT create_by FROM md_warehouse WHERE warehouse_id=?",
            String.class,
            w.getWarehouseId()));
  }

  /** 分页在全量结果上排序，包含条件中的通配符按字面量处理。 */
  @Test
  void paginationAndFilters() {
    for (int i = 0; i < 23; i++) service.create(warehouse(String.format("W%02d", i), i % 2 == 0));
    service.create(warehouse("W_%", false));
    var q = new WarehouseQuery();
    q.setPageNum(2);
    q.setPageSize(10);
    q.setSortDirection("desc");
    var page = service.list(q);
    assertEquals(24, page.getTotal());
    assertEquals(10, page.getRows().size());
    assertNull(PageHelper.getLocalPage());
    q.setPageNum(1);
    q.setWarehouseCode("_%");
    assertEquals(1, service.list(q).getTotal());
    q.setWarehouseCode(null);
    q.setLocationEnabled(true);
    assertEquals(12, service.list(q).getTotal());
    var w = service.create(warehouse("LOC", true));
    for (int i = 0; i < 23; i++) locations.create(location(w.getWarehouseId(), "L" + i));
    var lq = new com.ruoyi.business.masterdata.location.dto.LocationQuery();
    lq.setWarehouseId(w.getWarehouseId());
    lq.setPageSize(10);
    lq.setPageNum(2);
    assertEquals(23, locations.list(lq).getTotal());
    assertEquals(10, locations.list(lq).getRows().size());
    assertNull(PageHelper.getLocalPage());
  }

  /** 参照权限不包含维护权限，货位上下文不泄露内部字段。 */
  @Test
  void independentPermissionsAndReferenceShape() throws Exception {
    var w = service.create(warehouse("W", true));
    var l = locations.create(location(w.getWarehouseId(), "A"));
    login("warehouse-picker", Set.of("masterdata:warehouse:reference"));
    mvc.perform(get("/masterdata/reference/warehouses"))
        .andExpect(jsonPath("$.data.rows[0].warehouseCode").value("W"))
        .andExpect(jsonPath("$.data.rows[0].version").doesNotExist())
        .andExpect(jsonPath("$.data.rows[0].createTime").doesNotExist());
    mvc.perform(get(BASE + "/list")).andExpect(jsonPath("$.code").value(403));
    login("location-picker", Set.of("masterdata:location:reference"));
    mvc.perform(
            get("/masterdata/location/warehouse-context")
                .param("warehouseId", w.getWarehouseId().toString()))
        .andExpect(jsonPath("$.data.warehouseId").value(w.getWarehouseId()))
        .andExpect(jsonPath("$.data.createBy").doesNotExist());
    mvc.perform(
            get("/masterdata/reference/locations")
                .param("warehouseId", w.getWarehouseId().toString()))
        .andExpect(jsonPath("$.data.rows[0].locationId").value(l.getLocationId()));
    mvc.perform(
            get("/masterdata/location/list").param("warehouseId", w.getWarehouseId().toString()))
        .andExpect(jsonPath("$.code").value(403));
    mvc.perform(get("/masterdata/reference/warehouses")).andExpect(jsonPath("$.code").value(403));
    mvc.perform(get("/masterdata/reference/locations"))
        .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));
    assertThrows(ServiceException.class, () -> references.warehouses(Collections.nCopies(101, 1L)));
    assertThrows(ServiceException.class, () -> references.warehouses(List.of(-1L)));
  }

  /** 批量回显按顺序去重并排除删除与跨仓记录。 */
  @Test
  void scopedResolveAndDisabledCandidates() {
    var w = service.create(warehouse("W", true));
    var other = service.create(warehouse("OTHER", false));
    var l = locations.create(location(w.getWarehouseId(), "A"));
    assertEquals(
        0, references.locations(other.getWarehouseId(), List.of(l.getLocationId())).size());
    assertEquals(
        1, references.warehouses(List.of(w.getWarehouseId(), w.getWarehouseId(), 9999L)).size());
    assertThrows(
        ServiceException.class, () -> locations.create(location(other.getWarehouseId(), "A")));
    locations.delete(l.getLocationId(), 0);
    assertTrue(references.locations(w.getWarehouseId(), List.of(l.getLocationId())).isEmpty());
  }

  /** 并发新增货位与关闭管理只允许一个成功，不能留下关闭管理的有效货位。 */
  @Test
  void concurrentCreateAndDisableLocations() throws Exception {
    var w = service.create(warehouse("W", true));
    var edit = warehouse("W", false);
    edit.setWarehouseId(w.getWarehouseId());
    edit.setVersion(0);
    assertEquals(
        1,
        race(
            () -> {
              locations.create(location(w.getWarehouseId(), "A"));
              return true;
            },
            () -> {
              service.update(edit);
              return true;
            }));
    assertTrue(
        service.detail(w.getWarehouseId()).getLocationEnabled()
            || query(w.getWarehouseId()).getTotal() == 0);
  }

  /** 并发删除父仓与创建货位只允许一个成功。 */
  @Test
  void concurrentCreateAndDeleteWarehouse() throws Exception {
    var w = service.create(warehouse("W", true));
    assertEquals(
        1,
        race(
            () -> {
              locations.create(location(w.getWarehouseId(), "A"));
              return true;
            },
            () -> {
              service.delete(w.getWarehouseId(), 0);
              return true;
            }));
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM md_location l LEFT JOIN md_warehouse w ON w.warehouse_id=l.warehouse_id WHERE l.del_flag='0' AND (w.warehouse_id IS NULL OR w.del_flag!='0')",
            Integer.class));
  }

  /** 并发创建相同仓库编码由唯一索引兜底并转换为业务错误。 */
  @Test
  void concurrentWarehouseCodeConflict() throws Exception {
    assertEquals(
        1,
        race(
            () -> {
              service.create(warehouse("SAME", true));
              return true;
            },
            () -> {
              service.create(warehouse("same", true));
              return true;
            }));
    assertEquals(1, service.list(new WarehouseQuery()).getTotal());
  }

  /** 同仓并发货位编码冲突受父锁和唯一索引共同保护。 */
  @Test
  void concurrentLocationCodeConflict() throws Exception {
    var warehouse = service.create(warehouse("W", true));
    assertEquals(
        1,
        race(
            () -> {
              locations.create(location(warehouse.getWarehouseId(), "SAME"));
              return true;
            },
            () -> {
              locations.create(location(warehouse.getWarehouseId(), "same"));
              return true;
            }));
    assertEquals(1, query(warehouse.getWarehouseId()).getTotal());
  }

  /** 两个旧页面修改同一货位只能有一个版本提交。 */
  @Test
  void concurrentLocationEdits() throws Exception {
    var w = service.create(warehouse("W", true));
    var l = locations.create(location(w.getWarehouseId(), "A"));
    var a = location(w.getWarehouseId(), "B");
    a.setLocationId(l.getLocationId());
    a.setVersion(0);
    var b = location(w.getWarehouseId(), "C");
    b.setLocationId(l.getLocationId());
    b.setVersion(0);
    assertEquals(
        1,
        race(
            () -> {
              locations.update(a);
              return true;
            },
            () -> {
              locations.update(b);
              return true;
            }));
    assertEquals(1, locations.detail(l.getLocationId()).getVersion());
  }

  /** 构建规范化仓库请求。 */
  private SaveWarehouseRequest warehouse(String code, boolean enabled) {
    var request = new SaveWarehouseRequest();
    request.setWarehouseCode(code);
    request.setWarehouseName(" 同名仓库 ");
    request.setLocationEnabled(enabled);
    return request;
  }

  /** 构建规范化货位请求。 */
  private com.ruoyi.business.masterdata.location.dto.SaveLocationRequest location(
      Long warehouseId, String code) {
    var request = new com.ruoyi.business.masterdata.location.dto.SaveLocationRequest();
    request.setWarehouseId(warehouseId);
    request.setLocationCode(code);
    request.setLocationName(" 同名货位 ");
    return request;
  }

  /** 获取所属仓库的全部有效货位计数。 */
  private com.ruoyi.common.core.page.PageResult<
          com.ruoyi.business.masterdata.location.vo.LocationView>
      query(Long warehouseId) {
    var query = new com.ruoyi.business.masterdata.location.dto.LocationQuery();
    query.setWarehouseId(warehouseId);
    return locations.list(query);
  }

  /** 同时启动两个独立事务，统计成功数量。 */
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
    "com.ruoyi.business.masterdata.warehouse.mapper",
    "com.ruoyi.business.masterdata.location.mapper"
  })
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    WarehouseService.class,
    WarehouseController.class,
    com.ruoyi.business.masterdata.location.service.LocationService.class,
    com.ruoyi.business.masterdata.location.controller.LocationController.class,
    com.ruoyi.business.masterdata.reference.service.StorageReferenceService.class,
    com.ruoyi.business.masterdata.reference.controller.StorageReferenceController.class,
    PermissionService.class,
    PageHelperAutoConfiguration.class,
    JacksonConfig.class
  })
  static class TestConfig {
    /** 在服务层和 MVC 层复用实际 Bean Validation。 */
    @Bean
    org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator() {
      return new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
    }

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
