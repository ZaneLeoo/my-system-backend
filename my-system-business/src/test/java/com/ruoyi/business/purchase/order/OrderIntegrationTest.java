package com.ruoyi.business.purchase.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.business.masterdata.common.MasterDataTestSchema;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.service.impl.ItemServiceImpl;
import com.ruoyi.business.masterdata.item.service.support.ItemReadSupport;
import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.service.SupplierService;
import com.ruoyi.business.masterdata.supplier.service.impl.SupplierServiceImpl;
import com.ruoyi.business.masterdata.unit.service.UnitService;
import com.ruoyi.business.masterdata.unit.service.impl.UnitServiceImpl;
import com.ruoyi.business.purchase.order.controller.OrderController;
import com.ruoyi.business.purchase.order.domain.*;
import com.ruoyi.business.purchase.order.dto.*;
import com.ruoyi.business.purchase.order.service.*;
import com.ruoyi.business.purchase.order.vo.*;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.EnableStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.config.JacksonConfig;
import com.ruoyi.framework.config.MyBatisConfig;
import com.ruoyi.framework.mybatis.BusinessAuditHandler;
import com.ruoyi.framework.web.exception.BusinessExceptionHandler;
import com.ruoyi.framework.web.service.PermissionService;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.json.JsonMapper;

/** 真实会话工厂、行锁、分页、权限和 MVC 下的采购整单验证。 */
class OrderIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private Connection connection;
  private JdbcTemplate jdbc;
  private OrderService service;
  private MockMvc mvc;
  private static final String BASE = "/purchase/order";

  /** 使用生产增量脚本建立隔离数据库及最小账号、档案夹具。 */
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
                    "mybatis.typeAliasesPackage",
                    "com.ruoyi.**.domain",
                    "mybatis.mapperLocations",
                    "classpath*:mapper/none/*.xml",
                    "mybatis.configLocation",
                    "classpath:category-mybatis.xml",
                    "pagehelper.helperDialect",
                    "mysql")));
    context.registerBean(DataSource.class, () -> source);
    context.register(TestConfig.class);
    context.refresh();
    connection = source.getConnection();
    MasterDataTestSchema.create(connection);
    jdbc = new JdbcTemplate(source);
    jdbc.execute(
        "CREATE TABLE sys_user(user_id BIGINT PRIMARY KEY, dept_id BIGINT,user_name VARCHAR(64),nick_name VARCHAR(100),status CHAR(1),del_flag CHAR(1))");
    jdbc.execute(
        "CREATE TABLE sys_dept(dept_id BIGINT PRIMARY KEY, dept_name VARCHAR(100),status CHAR(1),del_flag CHAR(1))");
    jdbc.execute(
        "CREATE TABLE sys_config(config_id BIGINT AUTO_INCREMENT PRIMARY KEY,config_key VARCHAR(100),config_value VARCHAR(100))");
    jdbc.execute("INSERT INTO sys_user VALUES (2,1,'tester','测试采购员','0','0')");
    jdbc.execute("INSERT INTO sys_dept VALUES (1,'采购部','0','0')");
    jdbc.execute(
        "INSERT INTO sys_config(config_key,config_value) VALUES ('purchase.order.quantityDecimalPlaces','6'),('purchase.order.priceDecimalPlaces','6')");
    jdbc.execute(
        "INSERT INTO md_item_category(category_id,category_code,category_name,create_by,create_time) VALUES(1,'C','分类','fixture',NOW())");
    jdbc.execute(
        "INSERT INTO md_unit(unit_id,unit_code,unit_name,create_by,create_time) VALUES(1,'EA','个','fixture',NOW()),(2,'BOX','箱','fixture',NOW())");
    jdbc.execute(
        "INSERT INTO md_supplier(supplier_id,supplier_code,supplier_name,create_by,create_time) VALUES(1,'S','供应商','fixture',NOW())");
    jdbc.execute(
        "INSERT INTO md_item(item_id,item_code,item_name,category_id,base_unit_id,create_by,create_time) VALUES(1,'I','料品',1,1,'fixture',NOW())");
    service = context.getBean(OrderService.class);
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login(Set.of("*:*:*"));
    JsonMapper.Builder builder = JsonMapper.builder();
    context.getBean(JacksonConfig.class).localDateTimeCustomizer().customize(builder);
    mvc =
        MockMvcBuilders.standaloneSetup(context.getBean(OrderController.class))
            .setControllerAdvice(new BusinessExceptionHandler())
            .setMessageConverters(new JacksonJsonHttpMessageConverter(builder.build()))
            .build();
  }

  /** 清理线程、保留连接及会话上下文。 */
  @AfterEach
  void tearDown() throws Exception {
    PageHelper.clearPage();
    SecurityContextHolder.clearContext();
    RequestContextHolder.resetRequestAttributes();
    if (connection != null) connection.close();
    if (context != null) context.close();
  }

  /** 两种口径、零价、逐行舍入及禁止换算单价反算均符合公式。 */
  @Test
  void exactCalculationsAndRounding() {
    OrderCalculator calc = context.getBean(OrderCalculator.class);
    var included =
        calc.calculate(PriceTaxMode.TAX_INCLUDED, List.of(amount("10", "110", "10")), 6, 6);
    assertEquals(new BigDecimal("1000.00"), included.getNetAmount());
    assertEquals(new BigDecimal("100.00"), included.getTax());
    assertEquals(new BigDecimal("1100.00"), included.getGrossAmount());
    var excluded =
        calc.calculate(PriceTaxMode.TAX_EXCLUDED, List.of(amount("10", "100", "10")), 6, 6);
    assertEquals(included.getGrossAmount(), excluded.getGrossAmount());
    var tail =
        calc.calculate(PriceTaxMode.TAX_INCLUDED, List.of(amount("100000", "1", "10")), 6, 6);
    assertEquals(new BigDecimal("90909.09"), tail.getNetAmount());
    var half =
        calc.calculate(
            PriceTaxMode.TAX_EXCLUDED,
            List.of(amount("1", "0.105", "0"), amount("1", "0.105", "0")),
            6,
            6);
    assertEquals(new BigDecimal("0.22"), half.getGrossAmount());
    assertEquals(
        new BigDecimal("999999999999.00"),
        calc.calculate(PriceTaxMode.TAX_INCLUDED, List.of(amount("1", "999999999999", "0")), 6, 6)
            .getGrossAmount());
    assertThrows(
        ServiceException.class,
        () ->
            calc.calculate(
                PriceTaxMode.TAX_EXCLUDED, List.of(amount("1", "999999999999", "100")), 6, 6));
    assertThrows(
        ServiceException.class,
        () ->
            calc.calculate(
                PriceTaxMode.TAX_INCLUDED,
                List.of(amount("999999999", "999999999999", "0")),
                6,
                6));
    assertEquals(
        new BigDecimal("0.00"),
        calc.calculate(PriceTaxMode.TAX_INCLUDED, List.of(amount("10", "0", "0")), 6, 6)
            .getGrossAmount());
    assertThrows(
        ServiceException.class,
        () ->
            calc.calculate(
                PriceTaxMode.TAX_EXCLUDED,
                Collections.nCopies(200, amount("999999999", "999999999", "100")),
                6,
                6));
  }

  /** 精度取首次保存的服务端参数，后续编辑与试算不受参数变更影响。 */
  @Test
  void precisionSnapshotsAndInputValidation() {
    SaveOrderRequest input = request();
    input.getLines().get(0).setQuantity(new BigDecimal("1.123456"));
    OrderView created = service.create(input);
    assertEquals(6, created.getQuantityDecimalPlaces());
    jdbc.update(
        "UPDATE sys_config SET config_value='0' WHERE config_key=?", OrderRules.QUANTITY_CONFIG);
    SaveOrderRequest edit = edit(created);
    service.update(edit);
    assertThrows(ServiceException.class, () -> service.create(input));
    CalculationRequest preview = new CalculationRequest();
    preview.setOrderId(created.getOrderId());
    preview.setPriceTaxMode(created.getPriceTaxMode());
    preview.setLines(List.of(input.getLines().get(0)));
    assertNotNull(service.calculate(preview));
    preview.setPriceTaxMode(PriceTaxMode.TAX_EXCLUDED);
    assertThrows(ServiceException.class, () -> service.calculate(preview));
    jdbc.update(
        "UPDATE sys_config SET config_value='bad' WHERE config_key=?", OrderRules.PRICE_CONFIG);
    assertThrows(ServiceException.class, () -> service.defaults());
    assertNotNull(service.detail(created.getOrderId()));
    SaveOrderRequest invalid = request();
    invalid.getLines().get(0).setTaxRate(null);
    assertThrows(ConstraintViolationException.class, () -> service.create(invalid));
  }

  /** 行 ID 保持稳定、同料多行、调序和实体逻辑删除均生效。 */
  @Test
  void stableLinesAndWholeOrderRollback() {
    SaveOrderRequest input = request();
    input.setLines(new ArrayList<>(List.of(line(), line())));
    OrderView created = service.create(input);
    Long first = created.getLines().get(0).getLineId(),
        second = created.getLines().get(1).getLineId();
    SaveOrderRequest edit = edit(created);
    Collections.reverse(edit.getLines());
    edit.getLines().get(0).setRemark(" changed ");
    OrderView reordered = service.update(edit);
    assertEquals(second, reordered.getLines().get(0).getLineId());
    assertEquals(first, reordered.getLines().get(1).getLineId());
    SaveOrderRequest bad = edit(reordered);
    bad.getLines().get(0).setRemark("不应提交");
    bad.getLines().get(1).setLineId(999999L);
    assertThrows(ServiceException.class, () -> service.update(bad));
    assertEquals("changed", service.detail(created.getOrderId()).getLines().get(0).getRemark());
    assertEquals(1, service.detail(created.getOrderId()).getVersion());
    SaveOrderRequest remove = edit(reordered);
    remove.getLines().remove(1);
    remove.getLines().add(line());
    OrderView updated = service.update(remove);
    assertEquals(second, updated.getLines().get(0).getLineId());
    assertEquals(
        "2",
        jdbc.queryForObject(
            "SELECT del_flag FROM purchase_order_line WHERE line_id=?", String.class, first));
    assertThrows(ServiceException.class, () -> service.update(edit(reordered)));
    service.delete(updated.getOrderId(), updated.getVersion());
    assertThrows(ServiceException.class, () -> service.detail(updated.getOrderId()));
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM purchase_order_line WHERE order_id=? AND del_flag='0'",
            Integer.class,
            updated.getOrderId()));
    assertEquals(
        "tester",
        jdbc.queryForObject(
            "SELECT update_by FROM purchase_order WHERE order_id=?",
            String.class,
            updated.getOrderId()));
  }

  /** 状态、版本、原因、只读归属、计价口径和历史快照均受服务端保护。 */
  @Test
  void lifecycleAndHistoricalSnapshots() {
    OrderView order = service.create(request());
    assertTrue(order.getOrderNo().matches("PO\\d{18,}"));
    assertEquals("测试采购员", order.getBuyerName());
    assertEquals("采购部", order.getDeptName());
    SaveOrderRequest change = edit(order);
    change.setPriceTaxMode(PriceTaxMode.TAX_EXCLUDED);
    assertThrows(ServiceException.class, () -> service.update(change));
    order = act(order, OrderAction.SUBMIT, "");
    Long id = order.getOrderId();
    Integer version = order.getVersion();
    assertThrows(ServiceException.class, () -> service.delete(id, version));
    OrderActionRequest blank = new OrderActionRequest();
    blank.setVersion(version);
    assertThrows(ServiceException.class, () -> service.transition(id, OrderAction.REJECT, blank));
    order = act(order, OrderAction.WITHDRAW, "");
    order = act(order, OrderAction.SUBMIT, "");
    order = act(order, OrderAction.REJECT, "请补充价格");
    assertEquals(OrderStatus.DRAFT, order.getStatus());
    assertEquals("请补充价格", order.getReviewRemark());
    jdbc.update("UPDATE md_supplier SET supplier_name='供应商新名称' WHERE supplier_id=1");
    jdbc.update("UPDATE md_item SET item_name='料品新名称' WHERE item_id=1");
    order = act(order, OrderAction.SUBMIT, "");
    order = act(order, OrderAction.APPROVE, "");
    assertEquals("供应商", order.getSupplierName());
    assertEquals("料品", order.getLines().get(0).getItemName());
    order = act(order, OrderAction.CLOSE, "一期手工关闭");
    assertEquals(OrderStatus.CLOSED, order.getStatus());
    assertNotNull(order.getClosedTime());
    assertNotNull(order.getReviewedTime());
    SaveOrderRequest closedEdit = edit(order);
    assertThrows(ServiceException.class, () -> service.update(closedEdit));
  }

  /** 档案删除、停用及基本单位变更检查真实采购引用。 */
  @Test
  void referencesProtectMasterData() {
    OrderView order = service.create(request());
    SupplierService suppliers = context.getBean(SupplierService.class);
    SaveSupplierRequest supplier = supplierInput();
    supplier.setStatus(EnableStatus.DISABLED);
    assertThrows(ServiceException.class, () -> suppliers.update(supplier));
    assertThrows(ServiceException.class, () -> suppliers.delete(1L, 0));
    ItemService items = context.getBean(ItemService.class);
    SaveItemRequest item = itemInput();
    item.setStatus(EnableStatus.DISABLED);
    assertThrows(ServiceException.class, () -> items.update(item));
    item.setStatus(EnableStatus.ENABLED);
    item.setPurchaseEnabled(false);
    assertThrows(ServiceException.class, () -> items.update(item));
    item.setPurchaseEnabled(true);
    item.setBaseUnitId(2L);
    assertThrows(ServiceException.class, () -> items.update(item));
    assertThrows(ServiceException.class, () -> items.delete(1L, 0));
    assertThrows(ServiceException.class, () -> context.getBean(UnitService.class).delete(1L, 0));
    order =
        act(
            act(act(order, OrderAction.SUBMIT, ""), OrderAction.APPROVE, ""),
            OrderAction.CLOSE,
            "关闭");
    suppliers.update(supplier);
    item.setBaseUnitId(1L);
    item.setStatus(EnableStatus.DISABLED);
    items.update(item);
    assertThrows(ServiceException.class, () -> suppliers.delete(1L, 1));
    assertThrows(ServiceException.class, () -> items.delete(1L, 1));
  }

  /** 料品、单位、分类、供应商的当前状态和日期均校验，失败无半单。 */
  @Test
  void invalidReferencesAndDatesAreAtomic() {
    SaveOrderRequest wrong = request();
    wrong.getLines().get(0).setUnitId(2L);
    assertThrows(ServiceException.class, () -> service.create(wrong));
    wrong.getLines().get(0).setUnitId(1L);
    wrong.getLines().get(0).setExpectedDate(LocalDate.of(2020, 1, 1));
    assertThrows(ServiceException.class, () -> service.create(wrong));
    assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM purchase_order", Integer.class));
    for (String sql :
        List.of(
            "UPDATE md_supplier SET status='1' WHERE supplier_id=1",
            "UPDATE md_item SET purchase_enabled=false WHERE item_id=1",
            "UPDATE md_unit SET status='1' WHERE unit_id=1",
            "UPDATE md_item_category SET status='1' WHERE category_id=1")) {
      jdbc.execute(sql);
      assertThrows(ServiceException.class, () -> service.create(request()));
      jdbc.execute("UPDATE md_supplier SET status='0'");
      jdbc.execute("UPDATE md_item SET purchase_enabled=true");
      jdbc.execute("UPDATE md_unit SET status='0'");
      jdbc.execute("UPDATE md_item_category SET status='0'");
    }
  }

  /** MVC 绑定十进制字符串、JSON 输出及独立动作权限符合契约。 */
  @Test
  void mvcContractValidationAndPermissions() throws Exception {
    String json =
        """
      {"orderDate":"2026-10-17","supplierId":1,"priceTaxMode":"TAX_INCLUDED","buyerId":999,
      "lines":[{"itemId":1,"unitId":1,"quantity":"10","unitPrice":"110","taxRate":"10"}]}
      """;
    mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.buyerId").value(2))
        .andExpect(jsonPath("$.data.netAmount").value("1000.00"))
        .andExpect(jsonPath("$.data.tax").value("100.00"))
        .andExpect(jsonPath("$.data.lines[0].netPrice").isString())
        .andExpect(jsonPath("$.data.delFlag").doesNotExist());
    for (String invalid :
        List.of(
            json.replace("\"10\"}]", "null}]"),
            json.replace("\"110\"", "\"0.0000001\""),
            json.replace("TAX_INCLUDED", "other")))
      mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(invalid))
          .andExpect(jsonPath("$.code").value(500));
    Long id = service.list(new OrderQuery()).getRows().get(0).getOrderId();
    login(Set.of("purchase:order:list"));
    mvc.perform(get(BASE + "/list")).andExpect(jsonPath("$.code").value(200));
    mvc.perform(get(BASE + "/" + id)).andExpect(jsonPath("$.code").value(403));
    mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
        .andExpect(jsonPath("$.code").value(403));
    mvc.perform(delete(BASE + "/" + id).param("version", "0"))
        .andExpect(jsonPath("$.code").value(403));
    login(Set.of("purchase:order:submit"));
    mvc.perform(
            post(BASE + "/" + id + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":0}"))
        .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    mvc.perform(
            post(BASE + "/" + id + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":1}"))
        .andExpect(jsonPath("$.code").value(403));
  }

  /** 列表分页保留总数，排序过滤作用于所有记录，SQL 失败清理分页线程。 */
  @Test
  void paginationAndSortWhitelist() {
    for (int i = 0; i < 5; i++) service.create(request());
    OrderQuery query = new OrderQuery();
    query.setPageSize(2);
    query.setPageNum(2);
    assertEquals(5, service.list(query).getTotal());
    assertEquals(2, service.list(query).getRows().size());
    assertNull(PageHelper.getLocalPage());
    query.setSortField("supplier_name; DROP TABLE");
    assertThrows(ConstraintViolationException.class, () -> service.list(query));
    jdbc.execute("ALTER TABLE purchase_order RENAME TO unavailable_order");
    try {
      assertThrows(RuntimeException.class, () -> service.list(new OrderQuery()));
      assertNull(PageHelper.getLocalPage());
    } finally {
      jdbc.execute("ALTER TABLE unavailable_order RENAME TO purchase_order");
    }
  }

  /** 同版本的整单修改和删除最多一方成功。 */
  @Test
  void concurrentOrderMutationsHaveOneWinner() throws Exception {
    OrderView order = service.create(request());
    SaveOrderRequest edit = edit(order);
    assertEquals(
        1,
        race(
            () -> {
              service.update(edit);
              return true;
            },
            () -> {
              service.delete(order.getOrderId(), 0);
              return true;
            }));
  }

  /** 采购新增与供应商停用不能同时提交，验证等待行锁后的当前读。 */
  @Test
  void concurrentSupplierDisableCannotPassPurchaseCreation() throws Exception {
    SaveSupplierRequest supplier = supplierInput();
    supplier.setStatus(EnableStatus.DISABLED);
    assertEquals(
        1,
        race(
            () -> {
              service.create(request());
              return true;
            },
            () -> {
              context.getBean(SupplierService.class).update(supplier);
              return true;
            }));
  }

  /** 并发订单保存与料品停用由同一分类事务锁串行化。 */
  @Test
  void concurrentItemDisableCannotPassPurchaseCreation() throws Exception {
    SaveItemRequest item = itemInput();
    item.setStatus(EnableStatus.DISABLED);
    assertEquals(
        1,
        race(
            () -> {
              service.create(request());
              return true;
            },
            () -> {
              context.getBean(ItemService.class).update(item);
              return true;
            }));
  }

  /** 创建金额输入，保留十进制原始值。 */
  private AmountInput amount(String quantity, String price, String rate) {
    AmountInput result = new AmountInput();
    result.setQuantity(new BigDecimal(quantity));
    result.setUnitPrice(new BigDecimal(price));
    result.setTaxRate(new BigDecimal(rate));
    return result;
  }

  /** 创建最小合法订单行。 */
  private OrderLineInput line() {
    OrderLineInput line = new OrderLineInput();
    line.setItemId(1L);
    line.setUnitId(1L);
    line.setQuantity(new BigDecimal("10"));
    line.setUnitPrice(new BigDecimal("110"));
    line.setTaxRate(new BigDecimal("10"));
    return line;
  }

  /** 创建不含客户端内部字段的普通 Java 请求。 */
  private SaveOrderRequest request() {
    SaveOrderRequest input = new SaveOrderRequest();
    input.setOrderDate(LocalDate.of(2026, 10, 17));
    input.setSupplierId(1L);
    input.setPriceTaxMode(PriceTaxMode.TAX_INCLUDED);
    input.setLines(new ArrayList<>(List.of(line())));
    return input;
  }

  /** 使用详情构造更新参数，并保留现有行 ID。 */
  private SaveOrderRequest edit(OrderView view) {
    SaveOrderRequest result = request();
    result.setOrderId(view.getOrderId());
    result.setVersion(view.getVersion());
    result.setPriceTaxMode(view.getPriceTaxMode());
    result.setLines(
        new ArrayList<>(
            view.getLines().stream()
                .map(
                    row -> {
                      OrderLineInput line = line();
                      line.setLineId(row.getLineId());
                      line.setQuantity(row.getQuantity());
                      line.setUnitPrice(
                          view.getPriceTaxMode() == PriceTaxMode.TAX_INCLUDED
                              ? row.getGrossPrice()
                              : row.getNetPrice());
                      line.setTaxRate(row.getTaxRate());
                      line.setRemark(row.getRemark());
                      return line;
                    })
                .toList()));
    return result;
  }

  /** 执行带当前版本的状态操作。 */
  private OrderView act(OrderView order, OrderAction action, String reason) {
    OrderActionRequest request = new OrderActionRequest();
    request.setVersion(order.getVersion());
    request.setReason(reason);
    return service.transition(order.getOrderId(), action, request);
  }

  /** 供应商维护输入，供引用校验及竞态使用。 */
  private SaveSupplierRequest supplierInput() {
    SaveSupplierRequest input = new SaveSupplierRequest();
    input.setSupplierId(1L);
    input.setSupplierCode("S");
    input.setSupplierName("供应商");
    input.setStatus(EnableStatus.ENABLED);
    input.setVersion(0);
    return input;
  }

  /** 料品维护输入，供引用校验及竞态使用。 */
  private SaveItemRequest itemInput() {
    SaveItemRequest input = new SaveItemRequest();
    input.setItemId(1L);
    input.setItemCode("I");
    input.setItemName("料品");
    input.setCategoryId(1L);
    input.setBaseUnitId(1L);
    input.setPurchaseEnabled(true);
    input.setStatus(EnableStatus.ENABLED);
    input.setVersion(0);
    return input;
  }

  /** 并行运行两个独立事务并统计成功数。 */
  private int race(Supplier<Boolean> first, Supplier<Boolean> second) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (Supplier<Boolean> task : List.of(first, second))
        results.add(
            pool.submit(
                () -> {
                  login(Set.of("*:*:*"));
                  try {
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return task.get();
                  } catch (ServiceException expected) {
                    return false;
                  } finally {
                    SecurityContextHolder.clearContext();
                  }
                }));
      start.countDown();
      return (results.get(0).get(15, TimeUnit.SECONDS) ? 1 : 0)
          + (results.get(1).get(15, TimeUnit.SECONDS) ? 1 : 0);
    } finally {
      pool.shutdownNow();
    }
  }

  /** 设置非管理员身份，测试真实方法权限。 */
  private void login(Set<String> permissions) {
    SysUser user = new SysUser();
    user.setUserName("tester");
    user.setUserId(2L);
    user.setDeptId(1L);
    LoginUser principal = new LoginUser(2L, 1L, user, permissions);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  /** 接入生产 Mapper、事务、校验、权限和公共审计。 */
  @Configuration
  @EnableTransactionManagement
  @EnableMethodSecurity
  @MapperScan({
    "com.ruoyi.business.purchase.order.mapper",
    "com.ruoyi.business.masterdata.common.mapper",
    "com.ruoyi.business.masterdata.category.mapper",
    "com.ruoyi.business.masterdata.unit.mapper",
    "com.ruoyi.business.masterdata.item.mapper",
    "com.ruoyi.business.masterdata.supplier.mapper"
  })
  @Import({
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    OrderService.class,
    OrderCalculator.class,
    OrderController.class,
    ItemServiceImpl.class,
    ItemReadSupport.class,
    UnitServiceImpl.class,
    SupplierServiceImpl.class,
    PermissionService.class,
    PageHelperAutoConfiguration.class,
    JacksonConfig.class
  })
  static class TestConfig {
    /** 提供统一 Bean Validation。 */
    @Bean
    static LocalValidatorFactoryBean validator() {
      return new LocalValidatorFactoryBean();
    }

    /** 对 Controller 启用方法参数校验。 */
    @Bean
    static MethodValidationPostProcessor methodValidationPostProcessor() {
      return new MethodValidationPostProcessor();
    }

    /** 使用生产会话工厂。 */
    @Bean
    SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory factory) {
      return new SqlSessionTemplate(factory);
    }

    /** 使用真实数据库事务。 */
    @Bean
    PlatformTransactionManager transactionManager(DataSource source) {
      return new DataSourceTransactionManager(source);
    }
  }
}
