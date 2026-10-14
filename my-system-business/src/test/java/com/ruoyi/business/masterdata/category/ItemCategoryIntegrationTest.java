package com.ruoyi.business.masterdata.category;

import static org.junit.jupiter.api.Assertions.*;

import com.ruoyi.business.config.BusinessAuditHandler;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 使用真实手工 MP 工厂、事务、审计与方法权限的分类集成测试。 */
class ItemCategoryIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private ItemCategoryService service;
  private JdbcTemplate jdbc;
  private Connection schemaConnection;

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
    service = context.getBean(ItemCategoryService.class);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login("creator", Set.of("*:*:*"));
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
    assertEquals("A", created.categoryCode());
    assertEquals("中文料品", created.categoryName());
    assertEquals("creator", created.createBy());
    assertNotNull(created.createTime());
    login("editor", Set.of("*:*:*"));
    service.update(request(created.categoryId(), 0L, "A", "中文修改", CategoryStatus.ENABLED, 0));
    CategoryView edited = service.detail(created.categoryId());
    assertEquals(1, edited.version());
    assertEquals("creator", edited.createBy());
    assertEquals("editor", edited.updateBy());
    login("deleter", Set.of("*:*:*"));
    service.delete(created.categoryId(), 1);
    assertThrows(ServiceException.class, () -> service.detail(created.categoryId()));
    assertTrue(service.list(new CategoryQuery(null, null, null)).isEmpty());
    Map<String, Object> deleted =
        jdbc.queryForMap(
            "SELECT del_flag, update_by, category_name FROM md_item_category WHERE category_id = ?",
            created.categoryId());
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
            request(null, root.categoryId(), "CHILD", "钢材", CategoryStatus.ENABLED, null));
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
                    root.categoryId(),
                    root.categoryId(),
                    "ROOT",
                    "原料",
                    CategoryStatus.ENABLED,
                    0)));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    root.categoryId(),
                    child.categoryId(),
                    "ROOT",
                    "原料",
                    CategoryStatus.ENABLED,
                    0)));
    assertThrows(ServiceException.class, () -> service.delete(root.categoryId(), 0));
    assertTrue(service.tree(root.categoryId()).isEmpty());
    var rows = service.list(new CategoryQuery(null, "钢材", null));
    assertEquals(2, rows.size());
    assertEquals(1, rows.stream().filter(CategoryView::matched).count());
    assertEquals(
        child.categoryId(),
        rows.stream().filter(CategoryView::matched).findFirst().orElseThrow().categoryId());
  }

  /** 验证启停约束以及旧页面修改和删除的版本冲突。 */
  @Test
  void stateAndStaleVersionsAreRejected() {
    CategoryView root =
        service.create(request(null, 0L, "ROOT", "原料", CategoryStatus.ENABLED, null));
    CategoryView child =
        service.create(
            request(null, root.categoryId(), "CHILD", "钢材", CategoryStatus.ENABLED, null));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(root.categoryId(), 0L, "ROOT", "原料", CategoryStatus.DISABLED, 0)));
    service.update(
        request(child.categoryId(), root.categoryId(), "CHILD", "钢材", CategoryStatus.DISABLED, 0));
    service.update(request(root.categoryId(), 0L, "ROOT", "原料", CategoryStatus.DISABLED, 0));
    assertTrue(service.tree(null).get(0).disabled());
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    child.categoryId(),
                    root.categoryId(),
                    "CHILD",
                    "钢材",
                    CategoryStatus.ENABLED,
                    1)));
    assertThrows(
        ServiceException.class,
        () ->
            service.create(
                request(null, root.categoryId(), "NEW", "新分类", CategoryStatus.ENABLED, null)));
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(child.categoryId(), 0L, "CHILD", "旧编辑", CategoryStatus.DISABLED, 0)));
    assertThrows(ServiceException.class, () -> service.delete(child.categoryId(), 0));
    assertEquals("钢材", service.detail(child.categoryId()).categoryName());
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
          executor.submit(() -> moveAfterSignal(start, first, second.categoryId()));
      Future<Boolean> moveSecond =
          executor.submit(() -> moveAfterSignal(start, second, first.categoryId()));
      start.countDown();
      int succeeded =
          (moveFirst.get(10, TimeUnit.SECONDS) ? 1 : 0)
              + (moveSecond.get(10, TimeUnit.SECONDS) ? 1 : 0);
      assertEquals(1, succeeded);
      assertEquals(1, service.tree(null).size());
      assertEquals(1, service.tree(null).get(0).children().size());
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
                  last == null ? 0L : last.categoryId(),
                  "LEVEL_" + depth,
                  "层级" + depth,
                  CategoryStatus.ENABLED,
                  null));
    }
    Long deepestId = last.categoryId();
    assertThrows(
        ServiceException.class,
        () ->
            service.create(
                request(null, deepestId, "OVERFLOW", "超深度", CategoryStatus.ENABLED, null)));
    CategoryView subtree =
        service.create(request(null, 0L, "SUBTREE", "子树", CategoryStatus.ENABLED, null));
    service.create(request(null, subtree.categoryId(), "LEAF", "叶子", CategoryStatus.ENABLED, null));
    Long almostDeepestId = service.detail(deepestId).parentId();
    assertThrows(
        ServiceException.class,
        () ->
            service.update(
                request(
                    subtree.categoryId(),
                    almostDeepestId,
                    "SUBTREE",
                    "子树",
                    CategoryStatus.ENABLED,
                    0)));
    assertEquals(0L, service.detail(subtree.categoryId()).parentId());
  }

  /** 验证权限代理拒绝未授权入口，并验证请求字段约束。 */
  @Test
  void controllerPermissionsAndRequestValidationAreActive() {
    login("reader", Set.of("masterdata:category:list"));
    ItemCategoryController controller = context.getBean(ItemCategoryController.class);
    assertEquals(200, controller.list(new CategoryQuery(null, null, null)).get("code"));
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

  /** 在隔离认证上下文中等待并执行一次移动。 */
  private boolean moveAfterSignal(CountDownLatch start, CategoryView category, Long parentId)
      throws InterruptedException {
    login("concurrent", Set.of("*:*:*"));
    try {
      assertTrue(start.await(5, TimeUnit.SECONDS));
      service.update(
          request(
              category.categoryId(),
              parentId,
              category.categoryCode(),
              category.categoryName(),
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
  @MapperScan("com.ruoyi.business.masterdata.category.mapper")
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    ItemCategoryServiceImpl.class,
    ItemCategoryController.class,
    PermissionService.class
  })
  static class TestConfig {
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
