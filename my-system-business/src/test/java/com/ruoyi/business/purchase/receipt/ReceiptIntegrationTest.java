package com.ruoyi.business.purchase.receipt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.business.inventory.controller.InventoryController;
import com.ruoyi.business.inventory.dto.InventoryQuery;
import com.ruoyi.business.inventory.service.*;
import com.ruoyi.business.masterdata.common.MasterDataTestSchema;
import com.ruoyi.business.masterdata.item.dto.SaveItemRequest;
import com.ruoyi.business.masterdata.item.service.ItemService;
import com.ruoyi.business.masterdata.item.service.impl.ItemServiceImpl;
import com.ruoyi.business.masterdata.item.service.support.ItemReadSupport;
import com.ruoyi.business.masterdata.location.dto.SaveLocationRequest;
import com.ruoyi.business.masterdata.location.service.LocationService;
import com.ruoyi.business.masterdata.supplier.dto.SaveSupplierRequest;
import com.ruoyi.business.masterdata.supplier.service.impl.SupplierServiceImpl;
import com.ruoyi.business.masterdata.unit.service.impl.UnitServiceImpl;
import com.ruoyi.business.masterdata.warehouse.dto.SaveWarehouseRequest;
import com.ruoyi.business.masterdata.warehouse.service.WarehouseService;
import com.ruoyi.business.purchase.arrival.controller.*;
import com.ruoyi.business.purchase.arrival.domain.*;
import com.ruoyi.business.purchase.arrival.dto.*;
import com.ruoyi.business.purchase.arrival.service.ArrivalService;
import com.ruoyi.business.purchase.arrival.vo.*;
import com.ruoyi.business.purchase.order.controller.OrderController;
import com.ruoyi.business.purchase.order.domain.*;
import com.ruoyi.business.purchase.order.dto.*;
import com.ruoyi.business.purchase.order.service.*;
import com.ruoyi.business.purchase.order.vo.*;
import com.ruoyi.business.purchase.receipt.controller.*;
import com.ruoyi.business.purchase.receipt.domain.*;
import com.ruoyi.business.purchase.receipt.dto.*;
import com.ruoyi.business.purchase.receipt.service.ReceiptService;
import com.ruoyi.business.purchase.receipt.vo.*;
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
class ReceiptIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private Connection connection;
  private JdbcTemplate jdbc;
  private OrderService service;
  private ArrivalService arrivalService;
  private MockMvc mvc;
  private ReceiptService receipts;
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
    arrivalService = context.getBean(ArrivalService.class);
    receipts = context.getBean(ReceiptService.class);
    jdbc.execute(
        "INSERT INTO md_warehouse(warehouse_id,warehouse_code,warehouse_name,create_by,create_time) VALUES(1,'W','收货仓库','fixture',NOW())");
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
    login(Set.of("*:*:*"));
    JsonMapper.Builder builder = JsonMapper.builder();
    context.getBean(JacksonConfig.class).localDateTimeCustomizer().customize(builder);
    mvc =
        MockMvcBuilders.standaloneSetup(
                context.getBean(OrderController.class),
                context.getBean(ArrivalController.class),
                context.getBean(OrderLineReferenceController.class),
                context.getBean(ReceiptController.class),
                context.getBean(ArrivalLineReferenceController.class),
                context.getBean(InventoryController.class))
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

  /** 草稿到冲销完整链路，来源到货与库存一致，重复命令不重复记账。 */
  @Test
  void lifecycleAndLedgerAreAtomic() {
    var arrival = confirmed("10");
    var draft = receipts.create(receipt(arrival, "6", null));
    assertTrue(draft.getReceiptNo().startsWith("PR"));
    assertFalse(draft.getLocationEnabled());
    assertEquals("tester", draft.getCreateBy());
    stock("0");
    receiptQuantities(arrival, "0", "0");
    var submitted = receiptAction(draft, ReceiptAction.SUBMIT, "");
    stock("0");
    receiptQuantities(arrival, "6", "0");
    assertThrows(ServiceException.class, () -> receiptAction(draft, ReceiptAction.SUBMIT, ""));
    var approved = receiptAction(submitted, ReceiptAction.APPROVE, "");
    stock("6");
    receiptQuantities(arrival, "0", "6");
    assertEquals(
        ReceiptProgress.PARTIALLY_RECEIVED,
        arrivalService.detail(arrival.getArrivalId()).getReceiptProgress());
    assertThrows(ServiceException.class, () -> receiptAction(submitted, ReceiptAction.APPROVE, ""));
    assertThrows(
        ServiceException.class,
        () -> action(arrivalService.detail(arrival.getArrivalId()), ArrivalAction.VOID, "有下游"));
    assertEquals(
        1, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    var reversed = receiptAction(approved, ReceiptAction.VOID, "测试冲销");
    assertEquals(ReceiptStatus.VOIDED, reversed.getStatus());
    stock("0");
    receiptQuantities(arrival, "0", "0");
    assertEquals(
        2, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    assertEquals(
        1,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM inventory_transaction WHERE reversal_of IS NOT NULL",
            Integer.class));
    assertEquals(
        0,
        jdbc.queryForObject(
                "SELECT SUM(quantity_change) FROM inventory_transaction", BigDecimal.class)
            .signum());
    assertEquals(
        ReceiptProgress.NOT_RECEIVED,
        arrivalService.detail(arrival.getArrivalId()).getReceiptProgress());
  }

  /** 拆分按来源汇总限额、禁止重复货位、不同维度独立记账。 */
  @Test
  void locationSplitUsesAggregateSourceLimit() {
    enableLocations();
    var arrival = confirmed("10");
    var input = receipt(arrival, "6", 1L);
    input.setLines(
        new ArrayList<>(List.of(input.getLines().get(0), receiptLine(arrival, "5", 2L))));
    assertThrows(ServiceException.class, () -> receipts.create(input));
    input.getLines().get(1).setQuantity(new BigDecimal("4"));
    input.getLines().get(1).setLocationId(1L);
    assertThrows(ServiceException.class, () -> receipts.create(input));
    input.getLines().get(1).setLocationId(2L);
    var draft = receipts.create(input);
    var confirmed =
        receiptAction(receiptAction(draft, ReceiptAction.SUBMIT, ""), ReceiptAction.APPROVE, "");
    stock("10");
    assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_balance", Integer.class));
    receiptQuantities(arrival, "0", "10");
    assertEquals(
        ReceiptProgress.FULLY_RECEIVED,
        arrivalService.detail(arrival.getArrivalId()).getReceiptProgress());
    assertEquals("A", confirmed.getLines().get(0).getLocationCode());
    var query = new ArrivalQuery();
    assertEquals(
        ReceiptProgress.FULLY_RECEIVED,
        arrivalService.list(query).getRows().get(0).getReceiptProgress());
  }

  /** 撤回和驳回只释放占用，草稿逻辑删除不记账。 */
  @Test
  void withdrawRejectAndDeleteReleaseOnlyPending() {
    var a = confirmed("10");
    var d = receipts.create(receipt(a, "3", null));
    var s = receiptAction(d, ReceiptAction.SUBMIT, "");
    var w = receiptAction(s, ReceiptAction.WITHDRAW, "");
    receiptQuantities(a, "0", "0");
    s = receiptAction(w, ReceiptAction.SUBMIT, "");
    var rejected = receiptAction(s, ReceiptAction.REJECT, "修改数量");
    receiptQuantities(a, "0", "0");
    assertEquals("修改数量", rejected.getReviewRemark());
    receipts.delete(rejected.getReceiptId(), rejected.getVersion());
    assertThrows(ServiceException.class, () -> receipts.detail(rejected.getReceiptId()));
    assertEquals(
        0, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    assertEquals(
        "tester",
        jdbc.queryForObject(
            "SELECT update_by FROM purchase_receipt WHERE receipt_id=?",
            String.class,
            d.getReceiptId()));
  }

  /** 数量精度、日期、客户端 ID、货位及仓库归属均在业务层拒绝。 */
  @Test
  void inputValidationAndWarehouseRules() {
    var a = confirmed("10");
    var r = receipt(a, "0", null);
    assertThrows(ConstraintViolationException.class, () -> receipts.create(r));
    r.getLines().get(0).setQuantity(new BigDecimal("1.0000001"));
    assertThrows(ConstraintViolationException.class, () -> receipts.create(r));
    r.getLines().get(0).setQuantity(BigDecimal.ONE);
    r.setReceiptDate(a.getArrivalDate().minusDays(1));
    assertThrows(ServiceException.class, () -> receipts.create(r));
    r.setReceiptDate(a.getArrivalDate());
    r.getLines().get(0).setLocationId(1L);
    assertThrows(ServiceException.class, () -> receipts.create(r));
    r.getLines().get(0).setLocationId(null);
    r.setReceiptId(1L);
    assertThrows(ServiceException.class, () -> receipts.create(r));
    r.setReceiptId(null);
    r.setVersion(0);
    assertThrows(ServiceException.class, () -> receipts.create(r));
    r.setVersion(null);
    enableLocations();
    assertThrows(ServiceException.class, () -> receipts.create(r));
    r.getLines().get(0).setLocationId(1L);
    jdbc.update("UPDATE md_location SET status='1' WHERE location_id=1");
    assertThrows(ServiceException.class, () -> receipts.create(r));
    jdbc.update("UPDATE md_location SET status='0',warehouse_id=2 WHERE location_id=1");
    assertThrows(ServiceException.class, () -> receipts.create(r));
  }

  /** 保留行 ID、逻辑删除移除行、版本冲突及快照不随档案更名。 */
  @Test
  void stableLineSnapshotsAndConflict() {
    enableLocations();
    var a = confirmed("10");
    var r = receipt(a, "4", 1L);
    r.setLines(new ArrayList<>(List.of(r.getLines().get(0), receiptLine(a, "2", 2L))));
    var d = receipts.create(r);
    var update = receipt(a, "5", 1L);
    update.setReceiptId(d.getReceiptId());
    update.setVersion(d.getVersion());
    update.getLines().get(0).setLineId(d.getLines().get(0).getLineId());
    var changed = receipts.update(update);
    assertEquals(d.getLines().get(0).getLineId(), changed.getLines().get(0).getLineId());
    assertEquals(
        1,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM purchase_receipt_line WHERE del_flag='2'", Integer.class));
    assertThrows(ServiceException.class, () -> receipts.update(update));
    jdbc.update("UPDATE md_item SET item_name='新料品名' WHERE item_id=1");
    assertEquals("料品", receipts.detail(d.getReceiptId()).getLines().get(0).getItemName());
    assertThrows(ServiceException.class, () -> receipts.delete(d.getReceiptId(), d.getVersion()));
  }

  /** 审核原子失败后来源占用、余额和流水均保持原状态。 */
  @Test
  void postingFailureRollsBackEverything() {
    var a = confirmed("10");
    var d = receipts.create(receipt(a, "3", null));
    var s = receiptAction(d, ReceiptAction.SUBMIT, "");
    jdbc.update(
        "UPDATE purchase_receipt SET version=2147483647 WHERE receipt_id=?", s.getReceiptId());
    var latest = receipts.detail(s.getReceiptId());
    assertThrows(ServiceException.class, () -> receiptAction(latest, ReceiptAction.APPROVE, ""));
    stock("0");
    receiptQuantities(a, "3", "0");
    assertEquals(
        0, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    assertEquals(ReceiptStatus.SUBMITTED, receipts.detail(s.getReceiptId()).getStatus());
  }

  /** 当前库存不足或余额溢出时整单拒绝，原流水不被修改。 */
  @Test
  void insufficientStockAndOverflowRollback() {
    var a = confirmed("10");
    var s = receiptAction(receipts.create(receipt(a, "3", null)), ReceiptAction.SUBMIT, "");
    var d = receiptAction(s, ReceiptAction.APPROVE, "");
    jdbc.update("UPDATE inventory_balance SET quantity=1");
    assertThrows(ServiceException.class, () -> receiptAction(d, ReceiptAction.VOID, "库存不足"));
    receiptQuantities(a, "0", "3");
    assertEquals(
        1, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    jdbc.update("UPDATE inventory_balance SET quantity=99999999999999.999999");
    var second = receiptAction(receipts.create(receipt(a, "1", null)), ReceiptAction.SUBMIT, "");
    assertThrows(ServiceException.class, () -> receiptAction(second, ReceiptAction.APPROVE, ""));
    receiptQuantities(a, "1", "3");
  }

  /** 并发提交只能占用一次剩余额度；同一单并发审核只记账一次。 */
  @Test
  void concurrentReserveAndApprove() throws Exception {
    var a = confirmed("10");
    var x = receipts.create(receipt(a, "7", null));
    var y = receipts.create(receipt(a, "7", null));
    assertEquals(
        1,
        race(
            () -> {
              receiptAction(x, ReceiptAction.SUBMIT, "");
              return true;
            },
            () -> {
              receiptAction(y, ReceiptAction.SUBMIT, "");
              return true;
            }));
    var winner = receipts.detail(x.getReceiptId());
    if (winner.getStatus() != ReceiptStatus.SUBMITTED) winner = receipts.detail(y.getReceiptId());
    var submitted = winner;
    assertEquals(
        1,
        race(
            () -> {
              receiptAction(submitted, ReceiptAction.APPROVE, "");
              return true;
            },
            () -> {
              receiptAction(submitted, ReceiptAction.APPROVE, "");
              return true;
            }));
    stock("7");
    assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_balance", Integer.class));
    assertEquals(
        1, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
  }

  /** 不同单据并发首次记入同一无货位维度，不产生重复余额或丢失更新。 */
  @Test
  void concurrentFirstBalanceCreation() throws Exception {
    var a = confirmed("10");
    var x = receiptAction(receipts.create(receipt(a, "4", null)), ReceiptAction.SUBMIT, "");
    var y = receiptAction(receipts.create(receipt(a, "6", null)), ReceiptAction.SUBMIT, "");
    assertEquals(
        2,
        race(
            () -> {
              receiptAction(x, ReceiptAction.APPROVE, "");
              return true;
            },
            () -> {
              receiptAction(y, ReceiptAction.APPROVE, "");
              return true;
            }));
    stock("10");
    assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_balance", Integer.class));
    assertEquals(
        2, jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transaction", Integer.class));
    assertEquals(
        0,
        jdbc.queryForObject(
                "SELECT (SELECT SUM(quantity) FROM inventory_balance)-(SELECT SUM(quantity_change) FROM inventory_transaction)",
                BigDecimal.class)
            .signum());
  }

  /** 关闭订单不影响已审核到货后续入库；历史、库存和模式约束独立生效。 */
  @Test
  void closedOrderAndMasterReferences() {
    enableLocations();
    var a = confirmed("10");
    jdbc.update("UPDATE purchase_order SET status='CLOSED'");
    var d = receipts.create(receipt(a, "10", 1L));
    var warehouse = warehouseInput();
    warehouse.setLocationEnabled(false);
    assertThrows(
        ServiceException.class, () -> context.getBean(WarehouseService.class).update(warehouse));
    var approved =
        receiptAction(receiptAction(d, ReceiptAction.SUBMIT, ""), ReceiptAction.APPROVE, "");
    assertEquals(
        0,
        context
            .getBean(com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper.class)
            .warehouseArrivals(1L, true));
    warehouse.setLocationEnabled(true);
    warehouse.setStatus(EnableStatus.DISABLED);
    assertThrows(
        ServiceException.class, () -> context.getBean(WarehouseService.class).update(warehouse));
    var item = itemInput();
    item.setPurchaseEnabled(false);
    context.getBean(ItemService.class).update(item);
    item.setVersion(1);
    item.setStatus(EnableStatus.DISABLED);
    assertThrows(ServiceException.class, () -> context.getBean(ItemService.class).update(item));
    var location = new SaveLocationRequest();
    location.setLocationId(1L);
    location.setWarehouseId(1L);
    location.setLocationCode("A");
    location.setLocationName("货位A");
    location.setVersion(0);
    location.setStatus(EnableStatus.DISABLED);
    assertThrows(
        ServiceException.class, () -> context.getBean(LocationService.class).update(location));
    receiptAction(approved, ReceiptAction.VOID, "恢复库存");
    assertThrows(
        ServiceException.class, () -> context.getBean(LocationService.class).delete(1L, 0));
    assertThrows(
        ServiceException.class, () -> context.getBean(WarehouseService.class).delete(1L, 0));
  }

  /** 独立权限、字段绑定、分页、十进制字符串和排序白名单。 */
  @Test
  void permissionPagingAndJsonContract() throws Exception {
    var a = confirmed("10");
    var d =
        receiptAction(
            receiptAction(receipts.create(receipt(a, "4", null)), ReceiptAction.SUBMIT, ""),
            ReceiptAction.APPROVE,
            "");
    login(Set.of("purchase:arrival:reference"));
    mvc.perform(get("/purchase/arrival/reference-lines").param("pageSize", "1"))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.rows[0].availableQuantity").value("6.000000"))
        .andExpect(jsonPath("$.data.rows[0].createBy").doesNotExist());
    mvc.perform(get("/purchase/receipt/list")).andExpect(jsonPath("$.code").value(403));
    mvc.perform(get("/purchase/arrival/reference-lines/resolve").param("ids", "1,1"))
        .andExpect(jsonPath("$.code").value(500));
    login(Set.of("inventory:balance:list"));
    mvc.perform(get("/inventory/balance/list").param("pageSize", "1"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.rows[0].quantity").value("4.000000"));
    mvc.perform(get("/inventory/transaction/list")).andExpect(jsonPath("$.code").value(403));
    login(Set.of("inventory:transaction:list"));
    mvc.perform(
            get("/inventory/transaction/list")
                .param("beginTime", "2020-01-01 00:00:00")
                .param("sortField", "postedTime"))
        .andExpect(jsonPath("$.data.rows[0].createBy").value("tester"));
    mvc.perform(get("/inventory/transaction/list").param("sortField", "quantity"))
        .andExpect(jsonPath("$.code").value(500));
    login(Set.of("*:*:*"));
    mvc.perform(get("/purchase/receipt/" + d.getReceiptId()))
        .andExpect(jsonPath("$.data.lines[0].quantity").value("4.000000"))
        .andExpect(jsonPath("$.data.locationEnabled").value(false));
    mvc.perform(get("/purchase/receipt/list").param("sortField", "receipt_date desc; drop table"))
        .andExpect(jsonPath("$.code").value(500));
    assertNull(PageHelper.getLocalPage());
  }

  /** 普通 DTO 的 JSON 绑定、空白规范化和校验覆盖真实保存接口。 */
  @Test
  void jsonSaveAndPrecision() throws Exception {
    var a = confirmed("10");
    String body =
        """
{"receiptDate":"2026-10-18","supplierId":1,"warehouseId":1,"remark":"  中文入库  ","lines":[{"arrivalLineId":%d,"quantity":"1.25","remark":null}]}
"""
            .formatted(a.getLines().get(0).getLineId());
    mvc.perform(post("/purchase/receipt").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.remark").value("中文入库"))
        .andExpect(jsonPath("$.data.lines[0].remark").value(""));
    jdbc.update("UPDATE purchase_arrival_line SET quantity_decimal_places=1");
    assertThrows(ServiceException.class, () -> receipts.create(receipt(a, "1.25", null)));
  }

  /** 库存查询保留停用档案，不依赖参照权限，余额零值按开关显示。 */
  @Test
  void stockQueryAfterVoidAndDisabledMasters() {
    var a = confirmed("10");
    var d =
        receiptAction(
            receiptAction(receipts.create(receipt(a, "2", null)), ReceiptAction.SUBMIT, ""),
            ReceiptAction.APPROVE,
            "");
    receiptAction(d, ReceiptAction.VOID, "更正");
    jdbc.update("UPDATE md_item SET status='1'");
    var q = new InventoryQuery();
    var query = context.getBean(InventoryQueryService.class);
    assertEquals(0, query.balances(q).getTotal());
    q.setOnlyPositive(false);
    q.setKeyword("料品");
    assertEquals(1, query.balances(q).getTotal());
    q.setSortField("postedTime");
    q.setSortDirection("asc");
    assertEquals(2, query.transactions(q).getTotal());
    assertEquals("PURCHASE_RECEIPT_VOID", query.transactions(q).getRows().get(1).getBusinessType());
  }

  /** 来源作废与保存、仓库停用与审核并发时，状态约束不穿透。 */
  @Test
  void masterAndSourceRaces() throws Exception {
    var a = confirmed("10");
    assertEquals(
        1,
        race(
            () -> {
              receipts.create(receipt(a, "2", null));
              return true;
            },
            () -> {
              action(a, ArrivalAction.VOID, "来源作废");
              return true;
            }));
  }

  /** 多来源合并保存；跨供应商、跨仓库来源禁止混入。 */
  @Test
  void mergeAndSourceContext() {
    var a = confirmed("5");
    var b = confirmed("7");
    var r = receipt(a, "3", null);
    r.setLines(new ArrayList<>(List.of(r.getLines().get(0), receiptLine(b, "4", null))));
    var d = receipts.create(r);
    var confirmed =
        receiptAction(receiptAction(d, ReceiptAction.SUBMIT, ""), ReceiptAction.APPROVE, "");
    assertEquals(2, confirmed.getLines().size());
    stock("7");
    jdbc.update("UPDATE purchase_arrival SET warehouse_id=2 WHERE arrival_id=?", b.getArrivalId());
    assertThrows(ServiceException.class, () -> receipts.create(r));
    jdbc.update(
        "UPDATE purchase_arrival SET warehouse_id=1,supplier_id=2 WHERE arrival_id=?",
        b.getArrivalId());
    assertThrows(ServiceException.class, () -> receipts.create(r));
  }

  /** 货位停用与入库保存共用仓库锁，不出现停用后新增引用。 */
  @Test
  void locationDisableRace() throws Exception {
    enableLocations();
    var a = confirmed("10");
    var r = new SaveLocationRequest();
    r.setLocationId(1L);
    r.setWarehouseId(1L);
    r.setLocationCode("A");
    r.setLocationName("货位A");
    r.setVersion(0);
    r.setStatus(EnableStatus.DISABLED);
    assertEquals(
        1,
        race(
            () -> {
              receipts.create(receipt(a, "2", 1L));
              return true;
            },
            () -> {
              context.getBean(LocationService.class).update(r);
              return true;
            }));
  }

  /** NULL 货位唯一约束禁止重复维度，数据库独立兜底。 */
  @Test
  void nullLocationUniqueIndex() {
    var a = confirmed("10");
    receiptAction(
        receiptAction(receipts.create(receipt(a, "1", null)), ReceiptAction.SUBMIT, ""),
        ReceiptAction.APPROVE,
        "");
    assertThrows(
        org.springframework.dao.DataAccessException.class,
        () ->
            jdbc.update(
                "INSERT INTO inventory_balance(warehouse_id,item_id,unit_id,quantity,version,create_by,create_time) VALUES(1,1,1,0,0,'fixture',NOW())"));
  }

  /** 部分入库到货只约束尚未入库的相关料品，库存约束另外判断。 */
  @Test
  void completedItemDoesNotInheritOtherLinesPending() {
    jdbc.execute(
        "INSERT INTO md_item(item_id,item_code,item_name,category_id,base_unit_id,create_by,create_time) VALUES(2,'I2','料品二',1,1,'fixture',NOW())");
    var input = request();
    var second = line();
    second.setItemId(2L);
    input.getLines().add(second);
    var o = service.create(input);
    o = act(act(o, OrderAction.SUBMIT, ""), OrderAction.APPROVE, "");
    var ai = arrival(o, "10");
    var al = new ArrivalLineInput();
    al.setOrderLineId(o.getLines().get(1).getLineId());
    al.setQuantity(new BigDecimal("10"));
    ai.setLines(new ArrayList<>(List.of(ai.getLines().get(0), al)));
    var a = arrivalService.create(ai);
    a = action(action(a, ArrivalAction.SUBMIT, ""), ArrivalAction.APPROVE, "");
    jdbc.update("UPDATE purchase_order SET status='CLOSED'");
    receiptAction(
        receiptAction(receipts.create(receipt(a, "10", null)), ReceiptAction.SUBMIT, ""),
        ReceiptAction.APPROVE,
        "");
    var refs =
        context.getBean(
            com.ruoyi.business.masterdata.common.mapper.MasterDataReferenceMapper.class);
    assertEquals(0, refs.itemPurchaseDocuments(1L, true));
    assertTrue(refs.itemPurchaseDocuments(2L, true) > 0);
    assertTrue(refs.warehouseArrivals(1L, true) > 0);
    assertTrue(refs.itemStock(1L) > 0);
  }

  /** 创建已审核到货，作为入库唯一正式来源。 */
  private ArrivalView confirmed(String quantity) {
    var a = arrivalService.create(arrival(approved(quantity), quantity));
    return action(action(a, ArrivalAction.SUBMIT, ""), ArrivalAction.APPROVE, "");
  }

  /** 创建一条入库输入，数量保持十进制。 */
  private ReceiptLineInput receiptLine(ArrivalView a, String quantity, Long location) {
    var r = new ReceiptLineInput();
    r.setArrivalLineId(a.getLines().get(0).getLineId());
    r.setQuantity(new BigDecimal(quantity));
    r.setLocationId(location);
    return r;
  }

  /** 构造无内部字段的整单请求。 */
  private SaveReceiptRequest receipt(ArrivalView a, String quantity, Long location) {
    var r = new SaveReceiptRequest();
    r.setReceiptDate(a.getArrivalDate());
    r.setSupplierId(a.getSupplierId());
    r.setWarehouseId(a.getWarehouseId());
    r.setLines(new ArrayList<>(List.of(receiptLine(a, quantity, location))));
    return r;
  }

  /** 按当前版本执行入库命令。 */
  private ReceiptView receiptAction(ReceiptView view, ReceiptAction action, String reason) {
    var r = new ReceiptActionRequest();
    r.setVersion(view.getVersion());
    r.setReason(reason);
    return receipts.transition(view.getReceiptId(), action, r);
  }

  /** 断言真实余额总量，空表按零处理。 */
  private void stock(String expected) {
    assertEquals(
        0,
        jdbc.queryForObject(
                "SELECT COALESCE(SUM(quantity),0) FROM inventory_balance", BigDecimal.class)
            .compareTo(new BigDecimal(expected)));
  }

  /** 校验到货行累计数与操作期望相符。 */
  private void receiptQuantities(ArrivalView a, String pending, String received) {
    var r = arrivalService.detail(a.getArrivalId()).getLines().get(0);
    assertEquals(0, r.getPendingReceiptQuantity().compareTo(new BigDecimal(pending)));
    assertEquals(0, r.getReceivedQuantity().compareTo(new BigDecimal(received)));
  }

  /** 建立两个真实货位及货位管理仓库。 */
  private void enableLocations() {
    jdbc.update("UPDATE md_warehouse SET location_enabled=true WHERE warehouse_id=1");
    jdbc.execute(
        "INSERT INTO md_location(location_id,warehouse_id,location_code,location_name,create_by,create_time) VALUES(1,1,'A','货位A','fixture',NOW()),(2,1,'B','货位B','fixture',NOW())");
  }

  /** 创建指定数量的已审核订单。 */
  private OrderView approved(String quantity) {
    SaveOrderRequest input = request();
    input.getLines().get(0).setQuantity(new BigDecimal(quantity));
    return act(act(service.create(input), OrderAction.SUBMIT, ""), OrderAction.APPROVE, "");
  }

  /** 创建只含来源行、数量和收货信息的保存参数。 */
  private SaveArrivalRequest arrival(OrderView order, String quantity) {
    SaveArrivalRequest input = new SaveArrivalRequest();
    input.setArrivalDate(LocalDate.of(2026, 10, 18));
    input.setSupplierId(order.getSupplierId());
    input.setWarehouseId(1L);
    ArrivalLineInput line = new ArrivalLineInput();
    line.setOrderLineId(order.getLines().get(0).getLineId());
    line.setQuantity(new BigDecimal(quantity));
    input.setLines(new ArrayList<>(List.of(line)));
    return input;
  }

  /** 以详情构建带稳定到货行 ID 的更新参数。 */
  private SaveArrivalRequest editArrival(ArrivalView view) {
    SaveArrivalRequest input = new SaveArrivalRequest();
    input.setArrivalId(view.getArrivalId());
    input.setVersion(view.getVersion());
    input.setArrivalDate(view.getArrivalDate());
    input.setWarehouseId(view.getWarehouseId());
    input.setSupplierId(view.getSupplierId());
    input.setRemark(view.getRemark());
    input.setLines(
        view.getLines().stream()
            .map(
                row -> {
                  ArrivalLineInput line = new ArrivalLineInput();
                  line.setLineId(row.getLineId());
                  line.setOrderLineId(row.getOrderLineId());
                  line.setQuantity(row.getQuantity());
                  line.setRemark(row.getRemark());
                  return line;
                })
            .toList());
    return input;
  }

  /** 携带当前整单版本执行状态命令。 */
  private ArrivalView action(ArrivalView view, ArrivalAction action, String reason) {
    ArrivalActionRequest input = new ArrivalActionRequest();
    input.setVersion(view.getVersion());
    input.setReason(reason);
    return arrivalService.transition(view.getArrivalId(), action, input);
  }

  /** 检查来源订单行的两个独立执行数量。 */
  private void quantities(OrderView order, String pending, String arrived) {
    OrderLineView line = service.detail(order.getOrderId()).getLines().get(0);
    assertEquals(0, line.getPendingArrivalQuantity().compareTo(new BigDecimal(pending)));
    assertEquals(0, line.getArrivedQuantity().compareTo(new BigDecimal(arrived)));
  }

  /** 构造仓库停用请求，供引用规则与并发验证。 */
  private SaveWarehouseRequest warehouseInput() {
    SaveWarehouseRequest input = new SaveWarehouseRequest();
    input.setWarehouseId(1L);
    input.setVersion(0);
    input.setWarehouseCode("W");
    input.setWarehouseName("收货仓库");
    input.setLocationEnabled(false);
    input.setStatus(EnableStatus.DISABLED);
    return input;
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
    "com.ruoyi.business.purchase.receipt.mapper",
    "com.ruoyi.business.inventory.mapper",
    "com.ruoyi.business.purchase.order.mapper",
    "com.ruoyi.business.purchase.arrival.mapper",
    "com.ruoyi.business.masterdata.warehouse.mapper",
    "com.ruoyi.business.masterdata.location.mapper",
    "com.ruoyi.business.masterdata.common.mapper",
    "com.ruoyi.business.masterdata.category.mapper",
    "com.ruoyi.business.masterdata.unit.mapper",
    "com.ruoyi.business.masterdata.item.mapper",
    "com.ruoyi.business.masterdata.supplier.mapper"
  })
  @Import({
    ReceiptService.class,
    ReceiptController.class,
    ArrivalLineReferenceController.class,
    InventoryPostingService.class,
    InventoryQueryService.class,
    InventoryController.class,
    LocationService.class,
    MyBatisConfig.class,
    BusinessAuditHandler.class,
    OrderService.class,
    ArrivalService.class,
    ArrivalController.class,
    OrderLineReferenceController.class,
    WarehouseService.class,
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
