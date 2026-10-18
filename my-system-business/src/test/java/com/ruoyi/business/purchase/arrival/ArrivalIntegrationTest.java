package com.ruoyi.business.purchase.arrival;

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
class ArrivalIntegrationTest {
  private AnnotationConfigApplicationContext context;
  private Connection connection;
  private JdbcTemplate jdbc;
  private OrderService service;
  private ArrivalService arrivalService;
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
    arrivalService = context.getBean(ArrivalService.class);
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
                context.getBean(OrderLineReferenceController.class))
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

  /** 草稿不占额度，提交占用，审核确认，作废冲减，重复和过期命令均不得累计。 */
  @Test
  void lifecyclePostsOnceAndPreservesAudit() {
    OrderView order = approved("10");
    ArrivalView draft = arrivalService.create(arrival(order, "6"));
    assertTrue(draft.getArrivalNo().startsWith("PA"));
    assertEquals("tester", draft.getCreateBy());
    quantities(order, "0", "0");
    ArrivalView submitted = action(draft, ArrivalAction.SUBMIT, "");
    quantities(order, "6", "0");
    assertThrows(ServiceException.class, () -> action(draft, ArrivalAction.SUBMIT, ""));
    assertThrows(
        ServiceException.class,
        () -> arrivalService.delete(submitted.getArrivalId(), submitted.getVersion()));
    ArrivalView confirmed = action(submitted, ArrivalAction.APPROVE, "");
    quantities(order, "0", "6");
    assertEquals(
        ArrivalProgress.PARTIALLY_ARRIVED, service.detail(order.getOrderId()).getArrivalProgress());
    assertThrows(ServiceException.class, () -> action(submitted, ArrivalAction.APPROVE, ""));
    assertThrows(ServiceException.class, () -> action(confirmed, ArrivalAction.VOID, " "));
    ArrivalView voided = action(confirmed, ArrivalAction.VOID, "录入有误");
    assertEquals(ArrivalStatus.VOIDED, voided.getStatus());
    assertEquals("tester", voided.getVoidedBy());
    quantities(order, "0", "0");
    assertEquals(
        "tester",
        jdbc.queryForObject(
            "SELECT update_by FROM purchase_order_line WHERE line_id=?",
            String.class,
            order.getLines().get(0).getLineId()));
    assertEquals("tester", voided.getCreateBy());
    assertThrows(ServiceException.class, () -> action(voided, ArrivalAction.SUBMIT, ""));
    assertEquals(order.getVersion() + 3, service.detail(order.getOrderId()).getVersion());
  }

  /** 撤回和驳回准确释放占用，回到草稿后可编辑及实体逻辑删除。 */
  @Test
  void withdrawalRejectionAndDeletionReleaseCorrectly() {
    OrderView order = approved("10");
    ArrivalView draft = arrivalService.create(arrival(order, "4"));
    ArrivalView submitted = action(draft, ArrivalAction.SUBMIT, "");
    ArrivalView withdrawn = action(submitted, ArrivalAction.WITHDRAW, "");
    quantities(order, "0", "0");
    assertNull(withdrawn.getSubmittedTime());
    submitted = action(withdrawn, ArrivalAction.SUBMIT, "");
    ArrivalView forReject = submitted;
    assertThrows(ServiceException.class, () -> action(forReject, ArrivalAction.REJECT, ""));
    ArrivalView rejected = action(submitted, ArrivalAction.REJECT, "数量待复核");
    quantities(order, "0", "0");
    SaveArrivalRequest edit = editArrival(rejected);
    edit.getLines().get(0).setQuantity(new BigDecimal("3"));
    ArrivalView updated = arrivalService.update(edit);
    assertEquals(rejected.getLines().get(0).getLineId(), updated.getLines().get(0).getLineId());
    arrivalService.delete(updated.getArrivalId(), updated.getVersion());
    assertThrows(ServiceException.class, () -> arrivalService.detail(updated.getArrivalId()));
    assertEquals(
        "2",
        jdbc.queryForObject(
            "SELECT del_flag FROM purchase_arrival_line WHERE arrival_id=?",
            String.class,
            updated.getArrivalId()));
    assertEquals(
        "tester",
        jdbc.queryForObject(
            "SELECT update_by FROM purchase_arrival WHERE arrival_id=?",
            String.class,
            updated.getArrivalId()));
  }

  /** 两张草稿可以共存，但待审核额度和已确认数量不能超过订单。 */
  @Test
  void submissionsReserveRemainingQuantity() {
    OrderView order = approved("10");
    ArrivalView first = arrivalService.create(arrival(order, "6"));
    ArrivalView second = arrivalService.create(arrival(order, "6"));
    first = action(first, ArrivalAction.SUBMIT, "");
    ArrivalView secondDraft = second;
    assertThrows(ServiceException.class, () -> action(secondDraft, ArrivalAction.SUBMIT, ""));
    quantities(order, "6", "0");
    first = action(first, ArrivalAction.APPROVE, "");
    SaveArrivalRequest smaller = editArrival(second);
    smaller.getLines().get(0).setQuantity(new BigDecimal("4"));
    second = arrivalService.update(smaller);
    second = action(action(second, ArrivalAction.SUBMIT, ""), ArrivalAction.APPROVE, "");
    quantities(order, "0", "10");
    assertEquals(
        ArrivalProgress.FULLY_ARRIVED, service.detail(order.getOrderId()).getArrivalProgress());
    assertEquals(OrderStatus.APPROVED, service.detail(order.getOrderId()).getStatus());
    assertThrows(ServiceException.class, () -> arrivalService.create(arrival(order, "1")));
  }

  /** 多来源订单各自保留精度，重复来源、未审核来源和跨供应商明确拒绝。 */
  @Test
  void multipleOrdersAndReferenceValidation() {
    OrderView one = approved("10"), two = approved("20");
    SaveArrivalRequest input = arrival(one, "4");
    input.setLines(
        new ArrayList<>(List.of(input.getLines().get(0), arrival(two, "7").getLines().get(0))));
    ArrivalView result = arrivalService.create(input);
    result = action(action(result, ArrivalAction.SUBMIT, ""), ArrivalAction.APPROVE, "");
    quantities(one, "0", "4");
    quantities(two, "0", "7");
    assertEquals(2, result.getLines().size());
    SaveArrivalRequest duplicate = arrival(one, "1");
    duplicate.setLines(List.of(duplicate.getLines().get(0), duplicate.getLines().get(0)));
    assertThrows(ServiceException.class, () -> arrivalService.create(duplicate));
    OrderView draft = service.create(request());
    assertThrows(ServiceException.class, () -> arrivalService.create(arrival(draft, "1")));
    SaveArrivalRequest wrongSupplier = arrival(one, "1");
    wrongSupplier.setSupplierId(2L);
    jdbc.execute(
        "INSERT INTO md_supplier(supplier_id,supplier_code,supplier_name,create_by,create_time) VALUES(2,'OTHER','另一供应商','fixture',NOW())");
    assertThrows(ServiceException.class, () -> arrivalService.create(wrongSupplier));
    SaveArrivalRequest missing = arrival(one, "1");
    missing.getLines().get(0).setOrderLineId(999L);
    assertThrows(ServiceException.class, () -> arrivalService.create(missing));
  }

  /** 精度沿用来源订单，空白清理、数量范围和请求版本在服务端校验。 */
  @Test
  void precisionBindingAndSaveRollback() throws Exception {
    jdbc.update(
        "UPDATE sys_config SET config_value='2' WHERE config_key=?", OrderRules.QUANTITY_CONFIG);
    OrderView order = approved("10");
    assertThrows(ServiceException.class, () -> arrivalService.create(arrival(order, "1.123")));
    SaveArrivalRequest input = arrival(order, "1.230000");
    input.setRemark("  到货备注  ");
    ArrivalView saved = arrivalService.create(input);
    assertEquals("到货备注", saved.getRemark());
    SaveArrivalRequest edit = editArrival(saved);
    edit.setRemark("不能保存");
    edit.getLines().get(0).setLineId(999999L);
    assertThrows(ServiceException.class, () -> arrivalService.update(edit));
    assertEquals("到货备注", arrivalService.detail(saved.getArrivalId()).getRemark());
    SaveArrivalRequest bad = arrival(order, "0");
    assertThrows(ConstraintViolationException.class, () -> arrivalService.create(bad));
    input.setVersion(0);
    assertThrows(ServiceException.class, () -> arrivalService.create(input));
    mvc.perform(
            post("/purchase/arrival")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"arrivalDate\":\"2026-10-18\",\"supplierId\":1,\"warehouseId\":1,\"remark\":\"  中文  \",\"lines\":[{\"orderLineId\":"
                        + order.getLines().get(0).getLineId()
                        + ",\"quantity\":\"1.25\"}]}"))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.remark").value("中文"))
        .andExpect(jsonPath("$.data.lines[0].quantity").isString());
  }

  /** 单据或来源版本溢出时，反写过的数量与状态也必须一起回滚。 */
  @Test
  void failuresAfterPostingRollbackWholeTransaction() {
    OrderView order = approved("10");
    ArrivalView submitted =
        action(arrivalService.create(arrival(order, "5")), ArrivalAction.SUBMIT, "");
    jdbc.update(
        "UPDATE purchase_arrival SET version=? WHERE arrival_id=?",
        Integer.MAX_VALUE,
        submitted.getArrivalId());
    ArrivalView overflow = arrivalService.detail(submitted.getArrivalId());
    assertThrows(ServiceException.class, () -> action(overflow, ArrivalAction.APPROVE, ""));
    quantities(order, "5", "0");
    assertEquals(
        ArrivalStatus.SUBMITTED, arrivalService.detail(submitted.getArrivalId()).getStatus());
    jdbc.update(
        "UPDATE purchase_arrival SET version=? WHERE arrival_id=?",
        submitted.getVersion(),
        submitted.getArrivalId());
    jdbc.update(
        "UPDATE purchase_order SET version=? WHERE order_id=?",
        Integer.MAX_VALUE,
        order.getOrderId());
    assertThrows(ServiceException.class, () -> action(submitted, ArrivalAction.APPROVE, ""));
    quantities(order, "5", "0");
  }

  /** 未处理到货阻止关闭，已确认到货不阻止关闭，关闭不会改变到货历史。 */
  @Test
  void orderCloseAndSnapshots() {
    OrderView order = approved("10");
    ArrivalView draft = arrivalService.create(arrival(order, "3"));
    assertThrows(ServiceException.class, () -> act(order, OrderAction.CLOSE, "剩余不再采购"));
    ArrivalView submitted = action(draft, ArrivalAction.SUBMIT, "");
    assertThrows(
        ServiceException.class,
        () -> act(service.detail(order.getOrderId()), OrderAction.CLOSE, "停止采购"));
    ArrivalView confirmed = action(submitted, ArrivalAction.APPROVE, "");
    OrderView closed = act(service.detail(order.getOrderId()), OrderAction.CLOSE, "剩余不再采购");
    assertEquals(OrderStatus.CLOSED, closed.getStatus());
    assertThrows(ServiceException.class, () -> arrivalService.create(arrival(order, "1")));
    jdbc.update("UPDATE md_item SET item_name='改名' WHERE item_id=1");
    assertEquals(
        "料品", arrivalService.detail(confirmed.getArrivalId()).getLines().get(0).getItemName());
    SaveSupplierRequest supplier = supplierInput();
    supplier.setStatus(EnableStatus.DISABLED);
    assertThrows(
        ServiceException.class, () -> context.getBean(SupplierService.class).update(supplier));
    SaveItemRequest item = itemInput();
    item.setStatus(EnableStatus.DISABLED);
    assertThrows(ServiceException.class, () -> context.getBean(ItemService.class).update(item));
    assertThrows(
        ServiceException.class,
        () -> context.getBean(WarehouseService.class).update(warehouseInput()));
    action(confirmed, ArrivalAction.VOID, "错误到货");
    assertEquals(OrderStatus.CLOSED, service.detail(order.getOrderId()).getStatus());
    assertThrows(
        ServiceException.class, () -> context.getBean(WarehouseService.class).delete(1L, 0));
  }

  /** 独立参照权限可分页选行，却不能读取整单金额或执行维护。 */
  @Test
  void independentPermissionsAndReferenceFieldScope() throws Exception {
    OrderView order = approved("10");
    login(Set.of("purchase:order:reference"));
    mvc.perform(
            get("/purchase/order/reference-lines").param("supplierId", "1").param("pageSize", "1"))
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(
            jsonPath("$.data.rows[0].orderLineId").value(order.getLines().get(0).getLineId()))
        .andExpect(jsonPath("$.data.rows[0].netPrice").doesNotExist())
        .andExpect(jsonPath("$.data.rows[0].createBy").doesNotExist());
    mvc.perform(get(BASE + "/" + order.getOrderId())).andExpect(jsonPath("$.code").value(403));
    mvc.perform(
            get("/purchase/order/reference-lines/resolve")
                .param("ids", order.getLines().get(0).getLineId().toString()))
        .andExpect(jsonPath("$.data[0].orderLineId").value(order.getLines().get(0).getLineId()));
    login(Set.of("purchase:arrival:list"));
    mvc.perform(get("/purchase/order/reference-lines").param("supplierId", "1"))
        .andExpect(jsonPath("$.code").value(403));
    mvc.perform(
            post("/purchase/arrival/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"version\":0}"))
        .andExpect(jsonPath("$.code").value(403));
  }

  /** 全量筛选排序、来源订单关联筛选、批量参数与线程清理均可验证。 */
  @Test
  void pagingFilteringAndResolveValidation() {
    OrderView order = approved("10");
    for (int i = 0; i < 4; i++) arrivalService.create(arrival(order, "1"));
    ArrivalQuery query = new ArrivalQuery();
    query.setPageSize(2);
    query.setPageNum(2);
    query.setOrderId(order.getOrderId());
    assertEquals(4, arrivalService.list(query).getTotal());
    assertEquals(2, arrivalService.list(query).getRows().size());
    query.setSortField("arrival_no; DROP TABLE");
    assertThrows(ConstraintViolationException.class, () -> arrivalService.list(query));
    OrderLineReferenceQuery reference = new OrderLineReferenceQuery();
    reference.setSupplierId(1L);
    reference.setKeyword("料品");
    assertEquals(1, arrivalService.references(reference).getTotal());
    reference.setKeyword("%");
    assertEquals(0, arrivalService.references(reference).getTotal());
    assertThrows(ServiceException.class, () -> arrivalService.resolve(List.of(1L, 1L)));
    assertThrows(
        ServiceException.class, () -> arrivalService.resolve(Collections.nCopies(101, 1L)));
    assertNull(PageHelper.getLocalPage());
    jdbc.execute("ALTER TABLE purchase_arrival RENAME TO temporarily_unavailable");
    try {
      assertThrows(RuntimeException.class, () -> arrivalService.list(new ArrivalQuery()));
      assertNull(PageHelper.getLocalPage());
    } finally {
      jdbc.execute("ALTER TABLE temporarily_unavailable RENAME TO purchase_arrival");
    }
  }

  /** 两个收货人并发提交相同剩余额度，仅允许一个事务成功。 */
  @Test
  void competingSubmissionsCannotOverReceive() throws Exception {
    OrderView order = approved("10");
    ArrivalView first = arrivalService.create(arrival(order, "6")),
        second = arrivalService.create(arrival(order, "6"));
    assertEquals(
        1,
        race(
            () -> {
              action(first, ArrivalAction.SUBMIT, "");
              return true;
            },
            () -> {
              action(second, ArrivalAction.SUBMIT, "");
              return true;
            }));
    quantities(order, "6", "0");
  }

  /** 重复审核在并发场景下也只累计一次。 */
  @Test
  void duplicateApprovalHasOneWinner() throws Exception {
    OrderView order = approved("10");
    ArrivalView submitted =
        action(arrivalService.create(arrival(order, "6")), ArrivalAction.SUBMIT, "");
    assertEquals(
        1,
        race(
            () -> {
              action(submitted, ArrivalAction.APPROVE, "");
              return true;
            },
            () -> {
              action(submitted, ArrivalAction.APPROVE, "");
              return true;
            }));
    quantities(order, "0", "6");
  }

  /** 仓库停用与到货保存共用仓库行锁，不能同时穿透有效性与引用检查。 */
  @Test
  void warehouseDisableAndArrivalCreationHaveOneWinner() throws Exception {
    OrderView order = approved("10");
    assertEquals(
        1,
        race(
            () -> {
              arrivalService.create(arrival(order, "2"));
              return true;
            },
            () -> {
              context.getBean(WarehouseService.class).update(warehouseInput());
              return true;
            }));
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
