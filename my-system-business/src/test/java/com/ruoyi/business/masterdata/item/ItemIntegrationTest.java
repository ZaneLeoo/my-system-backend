package com.ruoyi.business.masterdata.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.service.impl.ItemCategoryServiceImpl;
import com.ruoyi.business.masterdata.item.controller.ItemController;
import com.ruoyi.business.masterdata.item.dto.*;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.service.impl.ItemServiceImpl;
import com.ruoyi.business.masterdata.item.vo.ItemView;
import com.ruoyi.business.masterdata.unit.dto.SaveUnitRequest;
import com.ruoyi.business.masterdata.unit.service.UnitService;
import com.ruoyi.business.masterdata.unit.service.impl.UnitServiceImpl;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.config.JacksonConfig;
import com.ruoyi.framework.config.MyBatisConfig;
import com.ruoyi.framework.mybatis.BusinessAuditHandler;
import com.ruoyi.framework.web.exception.BusinessExceptionHandler;
import com.ruoyi.framework.web.service.PermissionService;
import java.math.BigDecimal;
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

/** 单位档案的真实 MP、分页、事务、权限和 MVC 集成验证。 */
class ItemIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private ItemService service;
  private UnitService units;
  private ItemCategoryService categories;
  private Long categoryId;
  private Long unitId;
  private JdbcTemplate jdbc;
  private Connection schemaConnection;
  private MockMvc mvc;
  private static final String BASE = "/masterdata/item";

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
    Path migration = Path.of("../sql/migrations/20261015_01_unit.sql");
    if (!Files.exists(migration)) migration = Path.of("sql/migrations/20261015_01_unit.sql");
    String schema =
        Files.readString(migration)
            .split("START TRANSACTION;")[0]
            .replaceAll("(?m)^SET NAMES.*;", "")
            .replaceAll("ENGINE=InnoDB[^;]*", "");
    schemaConnection = source.getConnection();
    try (var sql = schemaConnection.createStatement()) {
      for (String statement : schema.split(";")) if (!statement.isBlank()) sql.execute(statement);
    }
    com.ruoyi.business.masterdata.common.MasterDataTestSchema.create(schemaConnection);
    service = context.getBean(ItemService.class);
    units = context.getBean(UnitService.class);
    categories = context.getBean(ItemCategoryService.class);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login("creator", Set.of("*:*:*"));
    categoryId = categories.create(category("ROOT", 0L)).getCategoryId();
    unitId = units.create(unit("EA")).getUnitId();
    JsonMapper.Builder builder = JsonMapper.builder();
    context.getBean(JacksonConfig.class).localDateTimeCustomizer().customize(builder);
    mvc =
        MockMvcBuilders.standaloneSetup(context.getBean(ItemController.class))
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

  /** 可空字段可清除，三个单位互相独立，审计及旧版本保护有效。 */
  @Test
  void independentUnitsNullableTaxAuditAndVersions() {
    Long purchase = units.create(unit("BOX")).getUnitId();
    Long sales = units.create(unit("KG")).getUnitId();
    SaveItemRequest input = request(" A ");
    input.setItemName(" 中文料品 ");
    input.setSpecification(" 10mm ");
    input.setPurchaseUnitId(purchase);
    input.setSalesUnitId(sales);
    input.setDefaultTaxRate(new BigDecimal("13.25"));
    ItemView created = service.create(input);
    assertEquals("A", created.getItemCode());
    assertEquals("中文料品", created.getItemName());
    assertEquals("10mm", created.getSpecification());
    assertEquals(purchase, created.getPurchaseUnitId());
    assertEquals(sales, created.getSalesUnitId());
    assertEquals("BOX", created.getPurchaseUnitName());
    SaveItemRequest edit = edit(created);
    edit.setPurchaseUnitId(null);
    edit.setSalesUnitId(null);
    edit.setDefaultTaxRate(null);
    edit.setPurchaseEnabled(false);
    edit.setRemark(null);
    login("editor", Set.of("*:*:*"));
    service.update(edit);
    ItemView current = service.detail(created.getItemId());
    assertNull(current.getPurchaseUnitId());
    assertNull(current.getSalesUnitId());
    assertNull(current.getDefaultTaxRate());
    assertFalse(current.getPurchaseEnabled());
    assertEquals("", current.getRemark());
    assertEquals("creator", current.getCreateBy());
    assertEquals("editor", current.getUpdateBy());
    assertEquals(created.getCreateTime(), current.getCreateTime());
    assertEquals(1, current.getVersion());
    assertThrows(ServiceException.class, () -> service.update(edit));
    assertThrows(ServiceException.class, () -> service.delete(current.getItemId(), 0));
    service.delete(current.getItemId(), 1);
    assertThrows(ServiceException.class, () -> service.detail(current.getItemId()));
    assertEquals(
        "2",
        jdbc.queryForObject(
            "SELECT del_flag FROM md_item WHERE item_id=?", String.class, current.getItemId()));
    assertEquals(
        "editor",
        jdbc.queryForObject(
            "SELECT update_by FROM md_item WHERE item_id=?", String.class, current.getItemId()));
    assertThrows(ServiceException.class, () -> service.create(request("a")));
  }

  /** 名称允许重复、编码占用包含删除记录，新建不能指定 ID 或版本。 */
  @Test
  void uniquenessAndCreateBoundary() {
    service.create(request("A"));
    service.create(request("B"));
    assertThrows(ServiceException.class, () -> service.create(request("a")));
    SaveItemRequest invalid = request("C");
    invalid.setItemId(999L);
    assertThrows(ServiceException.class, () -> service.create(invalid));
    invalid.setItemId(null);
    invalid.setVersion(0);
    assertThrows(ServiceException.class, () -> service.create(invalid));
    ItemView row = service.list(new ItemQuery()).getRows().get(0);
    SaveItemRequest edit = edit(row);
    edit.setVersion(null);
    assertThrows(ServiceException.class, () -> service.update(edit));
  }

  /** 任意分类层级可归类，父级筛选含后代，总数及跨页排序正确。 */
  @Test
  void descendantsPaginationAndSorting() {
    Long child = categories.create(category("CHILD", categoryId)).getCategoryId();
    for (String code : List.of("D", "A", "C", "B", "E")) {
      SaveItemRequest r = request(code);
      if (!code.equals("A")) r.setCategoryId(child);
      service.create(r);
    }
    ItemQuery query = new ItemQuery();
    query.setCategoryId(categoryId);
    query.setPageSize(2);
    query.setPageNum(2);
    var result = service.list(query);
    assertEquals(5, result.getTotal());
    assertEquals(List.of("C", "D"), result.getRows().stream().map(ItemView::getItemCode).toList());
    assertEquals("ROOT / CHILD", result.getRows().get(0).getCategoryPath());
    query.setSortDirection("desc");
    assertEquals(
        List.of("C", "B"),
        service.list(query).getRows().stream().map(ItemView::getItemCode).toList());
    query.setCategoryId(child);
    assertEquals(4, service.list(query).getTotal());
    query.setItemCode("%");
    assertEquals(0, service.list(query).getTotal());
    query.setItemCode(null);
    query.setPurchaseEnabled(false);
    assertEquals(0, service.list(query).getTotal());
    query.setSortField("item_code;DROP TABLE md_item");
    assertThrows(ServiceException.class, () -> service.list(query));
    assertNull(PageHelper.getLocalPage());
  }

  /** 三种单位引用均阻止停用和删除，停用料品可保留旧引用但不可重新启用。 */
  @Test
  void referenceGuardsAndInactiveReferences() {
    Long purchase = units.create(unit("BOX")).getUnitId();
    Long sales = units.create(unit("KG")).getUnitId();
    SaveItemRequest request = request("A");
    request.setPurchaseUnitId(purchase);
    request.setSalesUnitId(sales);
    ItemView created = service.create(request);
    for (Long id : List.of(unitId, purchase, sales)) {
      SaveUnitRequest unit = unitEdit(id);
      unit.setStatus(EnableStatus.DISABLED);
      assertThrows(ServiceException.class, () -> units.update(unit));
      assertThrows(ServiceException.class, () -> units.delete(id, 0));
    }
    SaveCategoryRequest category = categoryEdit(categoryId);
    category.setStatus(CategoryStatus.DISABLED);
    assertThrows(ServiceException.class, () -> categories.update(category));
    assertThrows(ServiceException.class, () -> categories.delete(categoryId, 0));
    SaveItemRequest edit = edit(created);
    edit.setStatus(EnableStatus.DISABLED);
    service.update(edit);
    units.update(disabledUnit(unitId));
    categories.update(category);
    assertThrows(ServiceException.class, () -> units.delete(unitId, 1));
    assertThrows(ServiceException.class, () -> categories.delete(categoryId, 1));
    edit = edit(service.detail(created.getItemId()));
    edit.setRemark("保留停用引用");
    service.update(edit);
    SaveItemRequest enable = edit(service.detail(created.getItemId()));
    enable.setStatus(EnableStatus.ENABLED);
    assertThrows(ServiceException.class, () -> service.update(enable));
    assertThrows(ServiceException.class, () -> service.create(request("B")));
    service.delete(created.getItemId(), 2);
    units.delete(unitId, 1);
    categories.delete(categoryId, 1);
  }

  /** JSON 绑定清理空白，税率精度、范围及排序白名单由接口拒绝。 */
  @Test
  void mvcBindingValidationAndPermissions() throws Exception {
    mvc.perform(get(BASE + "/list")).andExpect(jsonPath("$.code").value(200));
    String payload =
        "{\"itemCode\":\" X \",\"itemName\":\" 中文 \",\"categoryId\":"
            + categoryId
            + ",\"baseUnitId\":"
            + unitId
            + ",\"status\":\"0\",\"purchaseEnabled\":false,\"remark\":null,\"defaultTaxRate\":null}";
    mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(payload))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.itemCode").value("X"))
        .andExpect(jsonPath("$.data.purchaseEnabled").value(false))
        .andExpect(
            jsonPath("$.data.createTime")
                .value(
                    org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")));
    for (String tax : List.of("100.01", "-1", "1.001"))
      mvc.perform(
              post(BASE)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(payload.replace("\"defaultTaxRate\":null", "\"defaultTaxRate\":" + tax)))
          .andExpect(jsonPath("$.code").value(500));
    mvc.perform(get(BASE + "/list").param("pageSize", "101"))
        .andExpect(jsonPath("$.code").value(500));
    mvc.perform(get(BASE + "/list").param("sortField", "bad"))
        .andExpect(jsonPath("$.code").value(500));
    mvc.perform(get(BASE + "/list").param("itemName", " 中文 ").param("purchaseEnabled", "false"))
        .andExpect(jsonPath("$.data.total").value(1));
    login("reader", Set.of("masterdata:item:list"));
    mvc.perform(get(BASE + "/unit-options")).andExpect(jsonPath("$.code").value(200));
    mvc.perform(get(BASE + "/category-options"))
        .andExpect(jsonPath("$.data[0].disabled").value(false));
    assertThrows(
        AccessDeniedException.class,
        () -> context.getBean(ItemController.class).create(request("Z")));
    login("outsider", Set.of("masterdata:unit:list"));
    assertThrows(
        AccessDeniedException.class, () -> context.getBean(ItemController.class).unitOptions());
  }

  /** 选项不受管理分页上限影响，保留停用单位供历史值展示。 */
  @Test
  void completeOptionsIncludeDisabled() {
    for (int i = 0; i < 101; i++) {
      SaveUnitRequest r = unit("U" + i);
      r.setStatus(EnableStatus.DISABLED);
      units.create(r);
    }
    assertEquals(102, service.unitOptions().size());
    assertTrue(
        service.unitOptions().stream().anyMatch(u -> u.getStatus() == EnableStatus.DISABLED));
  }

  /** 同一旧版本仅一个写操作成功，并发停用或删除不能穿透引用保护。 */
  @Test
  void concurrentWritesAndReferenceChanges() throws Exception {
    ItemView row = service.create(request("A"));
    assertEquals(
        1,
        race(
            () -> {
              service.update(edit(row));
              return true;
            },
            () -> {
              service.delete(row.getItemId(), 0);
              return true;
            }));
    for (int i = 0; i < 4; i++) {
      Long id = units.create(unit("RACE" + i)).getUnitId();
      SaveItemRequest r = request("RACE" + i);
      r.setBaseUnitId(id);
      final int operation = i;
      assertEquals(
          1,
          race(
              () -> {
                service.create(r);
                return true;
              },
              () -> {
                if (operation % 2 == 0) units.update(disabledUnit(id));
                else units.delete(id, 0);
                return true;
              }));
      assertEquals(
          0,
          jdbc.queryForObject(
              "SELECT COUNT(*) FROM md_item i JOIN md_unit u ON i.base_unit_id=u.unit_id WHERE i.del_flag='0' AND i.status='0' AND (u.status='1' OR u.del_flag='2')",
              Integer.class));
    }
    for (int i = 0; i < 2; i++) {
      Long id = categories.create(category("RACEC" + i, 0L)).getCategoryId();
      SaveItemRequest r = request("CAT" + i);
      r.setCategoryId(id);
      final int operation = i;
      assertEquals(
          1,
          race(
              () -> {
                service.create(r);
                return true;
              },
              () -> {
                if (operation == 0) {
                  SaveCategoryRequest c = categoryEdit(id);
                  c.setStatus(CategoryStatus.DISABLED);
                  categories.update(c);
                } else categories.delete(id, 0);
                return true;
              }));
      assertEquals(
          0,
          jdbc.queryForObject(
              "SELECT COUNT(*) FROM md_item i JOIN md_item_category c ON i.category_id=c.category_id WHERE i.del_flag='0' AND i.status='0' AND (c.status='1' OR c.del_flag='2')",
              Integer.class));
    }
  }

  /** 构建普通 Java 料品请求并复用 Setter 规范化。 */
  private SaveItemRequest request(String code) {
    SaveItemRequest r = new SaveItemRequest();
    r.setItemCode(code);
    r.setItemName("同名料品");
    r.setCategoryId(categoryId);
    r.setBaseUnitId(unitId);
    r.setStatus(EnableStatus.ENABLED);
    return r;
  }

  /** 根据最新详情构造可维护字段。 */
  private SaveItemRequest edit(ItemView view) {
    SaveItemRequest r = request(view.getItemCode());
    r.setItemId(view.getItemId());
    r.setVersion(view.getVersion());
    r.setItemName(view.getItemName());
    r.setSpecification(view.getSpecification());
    r.setCategoryId(view.getCategoryId());
    r.setBaseUnitId(view.getBaseUnitId());
    r.setPurchaseUnitId(view.getPurchaseUnitId());
    r.setSalesUnitId(view.getSalesUnitId());
    r.setDefaultTaxRate(view.getDefaultTaxRate());
    r.setPurchaseEnabled(view.getPurchaseEnabled());
    r.setStatus(view.getStatus());
    return r;
  }

  /** 创建单位参数。 */
  private SaveUnitRequest unit(String code) {
    SaveUnitRequest r = new SaveUnitRequest();
    r.setUnitCode(code);
    r.setUnitName(code);
    r.setStatus(EnableStatus.ENABLED);
    return r;
  }

  /** 取得单位最新版本。 */
  private SaveUnitRequest unitEdit(Long id) {
    var view = units.detail(id);
    SaveUnitRequest r = unit(view.getUnitCode());
    r.setUnitId(id);
    r.setVersion(view.getVersion());
    return r;
  }

  /** 构造停用单位参数。 */
  private SaveUnitRequest disabledUnit(Long id) {
    SaveUnitRequest r = unitEdit(id);
    r.setStatus(EnableStatus.DISABLED);
    return r;
  }

  /** 创建分类参数。 */
  private SaveCategoryRequest category(String code, Long parent) {
    return new SaveCategoryRequest(null, parent, code, code, 0, CategoryStatus.ENABLED, "", null);
  }

  /** 取得分类最新版本。 */
  private SaveCategoryRequest categoryEdit(Long id) {
    var view = categories.detail(id);
    return new SaveCategoryRequest(
        id,
        view.getParentId(),
        view.getCategoryCode(),
        view.getCategoryName(),
        view.getSortOrder(),
        view.getStatus(),
        view.getRemark(),
        view.getVersion());
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
    "com.ruoyi.business.masterdata.unit.mapper",
    "com.ruoyi.business.masterdata.common.mapper",
    "com.ruoyi.business.masterdata.item.mapper",
    "com.ruoyi.business.masterdata.category.mapper"
  })
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    UnitServiceImpl.class,
    ItemController.class,
    ItemServiceImpl.class,
    ItemCategoryServiceImpl.class,
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
