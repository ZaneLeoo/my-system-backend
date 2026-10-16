package com.ruoyi.business.masterdata.category;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ruoyi.business.masterdata.category.controller.ItemCategoryController;
import com.ruoyi.business.masterdata.category.domain.CategoryConstants;
import com.ruoyi.business.masterdata.category.domain.CategoryStatus;
import com.ruoyi.business.masterdata.category.dto.CategoryQuery;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.service.ItemCategoryService;
import com.ruoyi.business.masterdata.category.service.impl.ItemCategoryServiceImpl;
import com.ruoyi.business.masterdata.category.vo.CategoryView;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.config.MyBatisConfig;
import com.ruoyi.framework.mybatis.BusinessAuditHandler;
import com.ruoyi.framework.web.exception.BusinessExceptionHandler;
import com.ruoyi.framework.web.exception.GlobalExceptionHandler;
import com.ruoyi.framework.web.service.PermissionService;
import jakarta.validation.Validation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
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

/** 使用真实手工 MP 工厂、事务、审计与方法权限的分类集成测试。 */
class ItemCategoryIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private ItemCategoryService service;
  private JdbcTemplate jdbc;
  private Connection schemaConnection;
  private MockMvc mvc;

  /** 为每个用例建立隔离数据库，并从生产增量脚本创建业务表。 */
  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL(
        "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
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
                    "mybatis.configLocation", "classpath:category-mybatis.xml")));
    context.registerBean(DataSource.class, () -> dataSource);
    context.register(TestConfig.class);
    context.refresh();
    jdbc = new JdbcTemplate(dataSource);
    Path migration = Path.of("../sql/migrations/20261014_01_item_category.sql");
    if (!Files.exists(migration)) {
      migration = Path.of("sql/migrations/20261014_01_item_category.sql");
    }
    String schema =
        Files.readString(migration)
            .split("START TRANSACTION;")[0]
            .replaceAll("(?m)^SET NAMES.*;", "")
            .replaceAll("ENGINE=InnoDB[^;]*", "");
    // H2 的 IN CHECK 表达式引用建表会话，整个用例保留该连接。
    schemaConnection = dataSource.getConnection();
    try (var sql = schemaConnection.createStatement()) {
      for (String statement : schema.split(";")) {
        if (!statement.isBlank()) {
          sql.execute(statement);
        }
      }
    }
    com.ruoyi.business.masterdata.common.MasterDataTestSchema.create(schemaConnection);
    service = context.getBean(ItemCategoryService.class);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login("creator", Set.of("*:*:*"));
    mvc =
        MockMvcBuilders.standaloneSetup(context.getBean(ItemCategoryController.class))
            .setControllerAdvice(new BusinessExceptionHandler(), new GlobalExceptionHandler())
            .build();
  }

  /** 清理当前线程身份并关闭测试上下文。 */
  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
    if (schemaConnection != null) {
      schemaConnection.close();
    }
    if (context != null) {
      context.close();
    }
  }

  /** 验证中文、规范化、审计、更新版本和 MP 逻辑删除。 */
  @Test
  void auditAndLogicalDeletionPreserveChineseData() {
    CategoryView created =
        service.create(request(null, 0L, " A ", " 中文料品 ", CategoryStatus.ENABLED, null));
    assertEquals("A", created.getCategoryCode());
    assertEquals("中文料品", created.getCategoryName());
    assertEquals("creator", created.getCreateBy());
    assertNotNull(created.getCreateTime());
    login("editor", Set.of("*:*:*"));
    service.update(request(created.getCategoryId(), 0L, "A", "中文修改", CategoryStatus.ENABLED, 0));
    CategoryView edited = service.detail(created.getCategoryId());
    assertEquals(1, edited.getVersion());
    assertEquals("creator", edited.getCreateBy());
    assertEquals("editor", edited.getUpdateBy());
    login("deleter", Set.of("*:*:*"));
    service.delete(created.getCategoryId(), 1);
    assertThrows(ServiceException.class, () -> service.detail(created.getCategoryId()));
    assertTrue(service.list(new CategoryQuery(null, null, null)).isEmpty());
    Map<String, Object> deleted =
        jdbc.queryForMap(
            "SELECT del_flag, update_by, category_name FROM md_item_category WHERE category_id = ?",
            created.getCategoryId());
    assertEquals("2", deleted.get("DEL_FLAG"));
    assertEquals("deleter", deleted.get("UPDATE_BY"));
    assertEquals("中文修改", deleted.get("CATEGORY_NAME"));
    assertThrows(
        ServiceException.class,
        () -> service.create(request(null, 0L, "A", "编码不能复用", CategoryStatus.ENABLED, null)));
  }

  /** 验证编码唯一、上级有效、自身/后代移动以及树选择排除规则。 */
  @Test
  void uniquenessAndTreeRelationshipsAreEnforced() {
    CategoryView root =
        service.create(request(null, 0L, "ROOT", "原料", CategoryStatus.ENABLED, null));
    CategoryView child =
        service.create(
            request(null, root.getCategoryId(), "CHILD", "钢材", CategoryStatus.ENABLED, null));
    assertThrows(
        ServiceException.class,
        () -> service.create(request(null, 0L, "ROOT", "重码", CategoryStatus.ENABLED, null)));
    assertThrows(
        ServiceException.class,
        () ->
            service.create(
                request(null, Long.MAX_VALUE, "INVALID", "无效上级", CategoryStatus.ENABLED, null)));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    root.getCategoryId(),
                    root.getCategoryId(),
                    "ROOT",
                    "原料",
                    CategoryStatus.ENABLED,
                    0)));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    root.getCategoryId(),
                    child.getCategoryId(),
                    "ROOT",
                    "原料",
                    CategoryStatus.ENABLED,
                    0)));
    assertThrows(ServiceException.class, () -> service.delete(root.getCategoryId(), 0));
    assertTrue(service.tree(root.getCategoryId()).isEmpty());
    var rows = service.list(new CategoryQuery(null, "钢材", null));
    assertEquals(2, rows.size());
    assertEquals(1, rows.stream().filter(CategoryView::isMatched).count());
    assertEquals(
        child.getCategoryId(),
        rows.stream().filter(CategoryView::isMatched).findFirst().orElseThrow().getCategoryId());
  }

  /** 验证启停约束以及旧页面修改和删除的版本冲突。 */
  @Test
  void stateAndStaleVersionsAreRejected() {
    CategoryView root =
        service.create(request(null, 0L, "ROOT", "原料", CategoryStatus.ENABLED, null));
    CategoryView child =
        service.create(
            request(null, root.getCategoryId(), "CHILD", "钢材", CategoryStatus.ENABLED, null));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(root.getCategoryId(), 0L, "ROOT", "原料", CategoryStatus.DISABLED, 0)));
    service.update(
        request(
            child.getCategoryId(),
            root.getCategoryId(),
            "CHILD",
            "钢材",
            CategoryStatus.DISABLED,
            0));
    service.update(request(root.getCategoryId(), 0L, "ROOT", "原料", CategoryStatus.DISABLED, 0));
    assertTrue(service.tree(null).get(0).isDisabled());
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    child.getCategoryId(),
                    root.getCategoryId(),
                    "CHILD",
                    "钢材",
                    CategoryStatus.ENABLED,
                    1)));
    assertThrows(
        ServiceException.class,
        () ->
            service.create(
                request(null, root.getCategoryId(), "NEW", "新分类", CategoryStatus.ENABLED, null)));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(child.getCategoryId(), 0L, "CHILD", "旧编辑", CategoryStatus.DISABLED, 0)));
    assertThrows(ServiceException.class, () -> service.delete(child.getCategoryId(), 0));
    assertEquals("钢材", service.detail(child.getCategoryId()).getCategoryName());
  }

  /** 两个线程互相移动根分类时仅允许一方成功，不能形成循环。 */
  @Test
  void concurrentMovesCannotCreateCycle() throws Exception {
    CategoryView first =
        service.create(request(null, 0L, "A", "分类A", CategoryStatus.ENABLED, null));
    CategoryView second =
        service.create(request(null, 0L, "B", "分类B", CategoryStatus.ENABLED, null));
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<Boolean> moveFirst =
          executor.submit(() -> moveAfterSignal(start, first, second.getCategoryId()));
      Future<Boolean> moveSecond =
          executor.submit(() -> moveAfterSignal(start, second, first.getCategoryId()));
      start.countDown();
      int succeeded =
          (moveFirst.get(10, TimeUnit.SECONDS) ? 1 : 0)
              + (moveSecond.get(10, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, succeeded);
      assertEquals(1, service.tree(null).size());
      assertEquals(1, service.tree(null).get(0).getChildren().size());
    } finally {
      executor.shutdownNow();
    }
  }

  /** 验证新增和整棵子树移动均遵守最大深度，而非仅检查被移动的节点。 */
  @Test
  void depthLimitIncludesDescendantsWhenMoving() {
    CategoryView last = null;
    for (int depth = 0; depth < CategoryConstants.MAX_DEPTH; depth++) {
      last =
          service.create(
              request(
                  null,
                  last == null ? 0L : last.getCategoryId(),
                  "LEVEL_" + depth,
                  "层级" + depth,
                  CategoryStatus.ENABLED,
                  null));
    }
    Long deepestId = last.getCategoryId();
    assertThrows(
        ServiceException.class,
        () ->
            service.create(
                request(null, deepestId, "OVERFLOW", "超深度", CategoryStatus.ENABLED, null)));
    CategoryView subtree =
        service.create(request(null, 0L, "SUBTREE", "子树", CategoryStatus.ENABLED, null));
    service.create(
        request(null, subtree.getCategoryId(), "LEAF", "叶子", CategoryStatus.ENABLED, null));
    Long almostDeepestId = service.detail(deepestId).getParentId();
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    subtree.getCategoryId(),
                    almostDeepestId,
                    "SUBTREE",
                    "子树",
                    CategoryStatus.ENABLED,
                    0)));
    assertEquals(0L, service.detail(subtree.getCategoryId()).getParentId());
  }

  /** 验证权限代理拒绝未授权入口，并验证请求字段约束。 */
  @Test
  void controllerPermissionsAndRequestValidationAreActive() {
    login("reader", Set.of("masterdata:category:list"));
    ItemCategoryController controller = context.getBean(ItemCategoryController.class);
    assertEquals(200, controller.list(new CategoryQuery(null, null, null)).getCode());
    assertThrows(AccessDeniedException.class, () -> controller.tree(null));
    assertThrows(AccessDeniedException.class, () -> controller.detail(1L));
    assertThrows(
        AccessDeniedException.class,
        () -> controller.create(request(null, 0L, "A", "新增", CategoryStatus.ENABLED, null)));
    assertThrows(
        AccessDeniedException.class,
        () -> controller.update(request(1L, 0L, "A", "修改", CategoryStatus.ENABLED, 0)));
    assertThrows(AccessDeniedException.class, () -> controller.delete(1L, 0));
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      assertFalse(
          factory.getValidator().validate(request(null, -1L, " ", " ", null, -1)).isEmpty());
      assertFalse(
          factory
              .getValidator()
              .validate(request(null, 0L, "A".repeat(65), "名称", CategoryStatus.ENABLED, null))
              .isEmpty());
    }
  }

  /** 经真实 MVC 属性绑定验证 Setter 规范化、查询条件和标准布尔 JSON 字段。 */
  @Test
  void javaBeansBindRequestsAndPreserveResponseFields() throws Exception {
    String base = "/masterdata/item-category";
    mvc.perform(get(base + "/list"))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data").isEmpty());
    mvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"parentId":0,"categoryCode":" ROOT ","categoryName":" 原料 ",
         "sortOrder":0,"status":"0","remark":null}
        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.categoryCode").value("ROOT"))
        .andExpect(jsonPath("$.data.categoryName").value("原料"))
        .andExpect(jsonPath("$.data.remark").value(""))
        .andExpect(jsonPath("$.data.status").value("0"))
        .andExpect(jsonPath("$.data.matched").value(true))
        .andExpect(jsonPath("$.data.isMatched").doesNotExist())
        .andExpect(jsonPath("$.data.delFlag").doesNotExist())
        .andExpect(jsonPath("$.data.createBy").value("creator"))
        .andExpect(jsonPath("$.data.createTime").isString());
    CategoryView root = service.list(new CategoryQuery("ROOT", null, null)).get(0);
    CategoryView child =
        service.create(
            request(null, root.getCategoryId(), "CHILD", "钢材", CategoryStatus.ENABLED, null));
    mvc.perform(
            get(base + "/list")
                .param("categoryCode", "CHILD")
                .param("categoryName", "钢材")
                .param("status", "0"))
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].matched").value(false))
        .andExpect(jsonPath("$.data[1].matched").value(true));
    mvc.perform(get(base + "/list").param("categoryName", "不存在的分类"))
        .andExpect(jsonPath("$.data").isEmpty());
    mvc.perform(get(base + "/tree").param("excludedId", child.getCategoryId().toString()))
        .andExpect(jsonPath("$.data[0].disabled").value(false))
        .andExpect(jsonPath("$.data[0].isDisabled").doesNotExist())
        .andExpect(jsonPath("$.data[0].children").isEmpty());

    SaveCategoryRequest fromSetters = new SaveCategoryRequest();
    fromSetters.setCategoryCode(" A ");
    fromSetters.setCategoryName(" 名称 ");
    fromSetters.setRemark(null);
    assertEquals("A", fromSetters.getCategoryCode());
    assertEquals("名称", fromSetters.getCategoryName());
    assertEquals("", fromSetters.getRemark());
    fromSetters.setRemark(" 备注 ");
    assertEquals("备注", fromSetters.getRemark());
  }

  /** 字段校验、格式错误、业务错误和无权限响应均走新业务 R 契约。 */
  @Test
  void invalidRequestsAndPermissionErrorsUseBusinessResponse() throws Exception {
    String base = "/masterdata/item-category";
    mvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"parentId":0,"categoryCode":" ","categoryName":"原料","sortOrder":0,"status":"0"}
        """))
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.data").value(nullValue()));
    mvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"parentId":0,"categoryCode":"A","categoryName":"原料","sortOrder":-1,"status":"0"}
        """))
        .andExpect(jsonPath("$.code").value(500));
    mvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
        {"parentId":0,"categoryCode":"A","categoryName":"原料","sortOrder":0,"status":"9"}
        """))
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.msg").value("请求格式不正确，请检查字段类型和枚举值"));
    mvc.perform(get(base + "/list").param("status", "9"))
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.data").value(nullValue()));
    mvc.perform(get(base + "/-1")).andExpect(jsonPath("$.code").value(500));
    mvc.perform(get(base + "/9999"))
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.msg").value("分类不存在或已删除"))
        .andExpect(jsonPath("$.data").value(nullValue()));
    mvc.perform(delete(base + "/1")).andExpect(jsonPath("$.msg").value("缺少必要参数：version"));
    login("reader", Set.of("masterdata:category:list"));
    mvc.perform(get(base + "/tree"))
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(jsonPath("$.data").value(nullValue()));
  }

  /** 在隔离认证上下文中等待并执行一次移动。 */
  private boolean moveAfterSignal(CountDownLatch start, CategoryView category, Long parentId)
      throws InterruptedException {
    login("concurrent", Set.of("*:*:*"));
    try {
      assertTrue(start.await(5, TimeUnit.SECONDS));
      service.update(
          request(
              category.getCategoryId(),
              parentId,
              category.getCategoryCode(),
              category.getCategoryName(),
              CategoryStatus.ENABLED,
              0));
      return true;
    } catch (ServiceException expected) {
      return false;
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  /** 构造受控保存请求，避免测试中隐式使用审计和删除字段。 */
  private SaveCategoryRequest request(
      Long id, Long parentId, String code, String name, CategoryStatus status, Integer version) {
    return new SaveCategoryRequest(id, parentId, code, name, 0, status, "", version);
  }

  /** 设置与项目一致的 LoginUser，供权限判断及审计填充使用。 */
  private void login(String username, Set<String> permissions) {
    SysUser user = new SysUser();
    user.setUserName(username);
    LoginUser loginUser = new LoginUser(2L, 1L, user, permissions);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
  }

  /** 集成真实业务组件，不启动外部 Redis、Web 或定时任务。 */
  @Configuration
  @EnableTransactionManagement
  @EnableMethodSecurity
  @MapperScan({
    "com.ruoyi.business.masterdata.category.mapper",
    "com.ruoyi.business.masterdata.common.mapper"
  })
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    ItemCategoryServiceImpl.class,
    ItemCategoryController.class,
    PermissionService.class
  })
  static class TestConfig {
    /** 与运行环境一致地启用 Controller 的方法级参数校验。 */
    @Bean
    static MethodValidationPostProcessor methodValidationPostProcessor() {
      return new MethodValidationPostProcessor();
    }

    /** 将真实工厂接入 Spring Mapper 代理。 */
    @Bean
    SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory factory) {
      return new SqlSessionTemplate(factory);
    }

    /** 为分类写入提供真实 JDBC 事务及行锁生命周期。 */
    @Bean
    PlatformTransactionManager transactionManager(DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }
}
