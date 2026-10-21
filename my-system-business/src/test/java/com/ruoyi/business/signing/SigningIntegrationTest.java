package com.ruoyi.business.signing;

import static com.ruoyi.signing.application.SigningSupport.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ruoyi.business.signing.config.SigningProperties;
import com.ruoyi.business.signing.controller.*;
import com.ruoyi.business.signing.infrastructure.*;
import com.ruoyi.business.signing.persistence.*;
import com.ruoyi.business.signing.persistence.mapper.SigningMapper;
import com.ruoyi.signing.api.*;
import com.ruoyi.signing.api.command.*;
import com.ruoyi.signing.api.result.*;
import com.ruoyi.signing.application.*;
import com.ruoyi.signing.model.*;
import com.ruoyi.signing.model.enums.*;
import com.ruoyi.signing.provider.esign.*;
import com.ruoyi.signing.spi.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mock.web.MockHttpServletRequest;

/** 独立数据库验证持久化、版本、回调和恢复，不启动应用服务器。 */
class SigningIntegrationTest {
  private java.sql.Connection schemaConnection;
  private SigningRepository repository;
  private SigningMapper mapper;
  private SigningProvider provider;
  private SigningJsonCodec json;
  private SigningUnitOfWork transactions;
  private DefaultSigningService service;
  private DefaultSigningTemplateService templates;
  private SigningJobProcessor jobs;
  private JdbcTemplate jdbc;
  private LocalSigningFileStore files;
  private SigningProperties config;
  private Path directory;
  private AtomicReference<SigningStatus> remote;
  private SigningSealProvider sealProvider;

  /** 新建隔离 schema 和假平台；所有签署 SQL 均执行真实数据库访问。 */
  @BeforeEach
  void setup() throws Exception {
    org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
        new org.springframework.web.context.request.ServletRequestAttributes(
            new MockHttpServletRequest()));
    JdbcDataSource h2 = new JdbcDataSource();
    h2.setURL(
        "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
    javax.sql.DataSource source = h2;
    String mysqlUrl = System.getenv("SIGNING_TEST_URL");
    if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
      org.springframework.jdbc.datasource.DriverManagerDataSource mysql =
          new org.springframework.jdbc.datasource.DriverManagerDataSource();
      mysql.setUrl(mysqlUrl);
      mysql.setUsername(System.getenv("SIGNING_TEST_USER"));
      mysql.setPassword(System.getenv("SIGNING_TEST_PASSWORD"));
      source = mysql;
    }
    jdbc = new JdbcTemplate(source);
    schemaConnection = source.getConnection();
    String schema =
        new String(
            Files.readAllBytes(Paths.get("../sql/migrations/20261020_01_signing.sql")),
            StandardCharsets.UTF_8);
    schema = schema.substring(schema.indexOf("CREATE TABLE"), schema.indexOf("START TRANSACTION"));
    if (mysqlUrl == null || mysqlUrl.isEmpty()) {
      schema = schema.replaceAll("(?s) ENGINE=InnoDB[^;]*", "").replace(" STORED", "");
    } else {
      if (!schemaConnection.getCatalog().matches("my_system_signing_test_[a-f0-9]{8}")) {
        throw new IllegalStateException("拒绝使用非隔离数据库");
      }
      for (String table :
          Arrays.asList(
              "sign_job",
              "sign_event",
              "sign_party",
              "sign_document",
              "sign_template_binding",
              "sign_task")) {
        schemaConnection.createStatement().execute("DROP TABLE IF EXISTS " + table);
      }
    }
    for (String sql : schema.split(";")) {
      if (!sql.trim().isEmpty()) {
        schemaConnection.createStatement().execute(sql);
      }
    }
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(source);
    com.github.pagehelper.PageInterceptor pagination = new com.github.pagehelper.PageInterceptor();
    Properties paginationProperties = new Properties();
    paginationProperties.setProperty("helperDialect", "mysql");
    pagination.setProperties(paginationProperties);
    factoryBean.setPlugins(pagination);
    SqlSessionFactory factory = factoryBean.getObject();
    factory.getConfiguration().setMapUnderscoreToCamelCase(true);
    factory.getConfiguration().addMapper(SigningMapper.class);
    mapper = new SqlSessionTemplate(factory).getMapper(SigningMapper.class);
    DataSourceTransactionManager manager = new DataSourceTransactionManager(source);
    json = new JacksonSigningJsonCodec();
    repository = new MybatisSigningRepository(mapper, json, manager);
    transactions = new SpringSigningUnitOfWork(manager);
    config = new SigningProperties();
    directory = Files.createTempDirectory("signing-test-");
    config.setPrivateDirectory(directory.toString());
    files = new LocalSigningFileStore(config);
    provider = mock(SigningProvider.class);
    when(provider.environment()).thenReturn("test:app");
    when(provider.upload(anyString(), any())).thenReturn("file-1");
    when(provider.file(anyString()))
        .thenReturn(map("fileStatus", 5, "fileDownloadUrl", "https://example.org/test.pdf"));
    when(provider.download(anyString())).thenReturn("%PDF-test".getBytes(StandardCharsets.UTF_8));
    when(provider.createFlow(any(), anyString(), anyList())).thenReturn("flow-1");
    when(provider.signedFile(anyString(), anyString()))
        .thenReturn("%PDF-signed".getBytes(StandardCharsets.UTF_8));
    remote = new AtomicReference<>(SigningStatus.DRAFT);
    when(provider.detail(anyString()))
        .thenAnswer(
            call -> {
              assertFalse(
                  org.springframework.transaction.support.TransactionSynchronizationManager
                      .isActualTransactionActive(),
                  "平台请求不能进入本地事务");
              SigningFlowResult result = new SigningFlowResult();
              result.setStatus(remote.get());
              result.getFileIds().add("file-1");
              return result;
            });
    doAnswer(
            call -> {
              remote.set(SigningStatus.SIGNING);
              return null;
            })
        .when(provider)
        .start(anyString());
    doAnswer(
            call -> {
              remote.set(SigningStatus.REVOKED);
              return null;
            })
        .when(provider)
        .revoke(anyString(), anyString());
    SigningBusinessAdapter adapter =
        new SigningBusinessAdapter() {
          public String businessType() {
            return "purchase.order";
          }

          public BusinessSnapshot snapshot(String id) {
            BusinessSnapshot result = new BusinessSnapshot();
            result.setTitle("测试合同");
            result.setValues(
                map(
                    "orderNo",
                    "PO1",
                    "supplierName",
                    "测试供应商",
                    "contactName",
                    "订单联系人",
                    "contactPhone",
                    "13800000000",
                    "zero",
                    0,
                    "enabled",
                    false));
            return result;
          }

          public SigningParty defaultCounterparty(Map<String, Object> values) {
            return new com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningAdapter(
                    null, null)
                .defaultCounterparty(values);
          }

          public List<SigningField> fields() {
            SigningField field = new SigningField();
            field.setKey("orderNo");
            field.setLabel("订单号");
            field.setType("TEXT");
            return Collections.singletonList(field);
          }
        };
    Map<String, SigningBusinessAdapter> adapters =
        Collections.singletonMap("purchase.order", adapter);
    templates = new DefaultSigningTemplateService(repository, provider, json, adapters);
    sealProvider = mock(SigningSealProvider.class);
    service =
        new DefaultSigningService(
            repository,
            provider,
            transactions,
            files,
            json,
            adapters,
            new SigningSealService(sealProvider));
    jobs = new SigningJobProcessor(repository, provider, transactions, files, templates, service);
  }

  /** 仅清理本测试创建的独立临时目录。 */
  @AfterEach
  void cleanup() throws Exception {
    org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
    if (schemaConnection != null) {
      schemaConnection.close();
    }
    if (directory != null) {
      try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
        for (Path file :
            paths.sorted(Comparator.reverseOrder()).collect(java.util.stream.Collectors.toList())) {
          Files.deleteIfExists(file);
        }
      }
    }
  }

  /** 构造上传方式草稿。 */
  private SigningTask create() {
    CreateSigningTaskCommand command = new CreateSigningTaskCommand();
    command.setBusinessType("purchase.order");
    command.setBusinessId("1");
    command.setSource("UPLOAD");
    return service.create(command, "tester");
  }

  /** 为后台测试立即执行已排队的任务，避免真实等待。 */
  private boolean step() {
    jdbc.update(
        "UPDATE sign_job SET next_run_time=TIMESTAMP '2000-01-01 00:00:00' WHERE status='PENDING'");
    return jobs.runOnce();
  }

  /** 准备最终 PDF。 */
  private SigningTask prepared() {
    SigningTask task = create();
    service.prepare(
        task.getId(),
        task.getVersion(),
        "采购合同.docx",
        "word-bytes".getBytes(StandardCharsets.UTF_8),
        "tester");
    assertTrue(step());
    SigningTask ready = service.task(task.getId());
    assertEquals(DocumentStatus.READY, ready.getDocumentStatus());
    return ready;
  }

  /** 配置双方并确认位置。 */
  private SigningTask confirmed() {
    SigningTask task = prepared();
    return service.participants(task.getId(), task.getVersion(), parties(), true, "tester");
  }

  /** 构建双签署人夹具。 */
  private List<SigningParty> parties() {
    List<SigningParty> result = new ArrayList<>();
    for (String role : Arrays.asList("OUR", "COUNTERPARTY")) {
      SigningParty party = new SigningParty();
      party.setRole(role);
      party.setOrganizationName(role);
      party.setTransactorName("测试经办人");
      party.setTransactorAccount("13800000000");
      SigningPosition position = new SigningPosition();
      position.setPage(1);
      position.setX(100);
      position.setY(200);
      party.getPositions().add(position);
      result.add(party);
    }
    return result;
  }

  /** 默认签署人来自单据快照；读取默认值不覆盖用户已保存的信息。 */
  @Test
  void counterpartyDefaultsAreSuggestionsFromSavedSnapshot() {
    SigningTask task = prepared();
    SigningParty defaults = service.defaultCounterparty(task);
    assertEquals("测试供应商", defaults.getOrganizationName());
    assertEquals("订单联系人", defaults.getTransactorName());
    assertEquals("13800000000", defaults.getTransactorAccount());
    assertTrue(repository.parties(task.getId()).isEmpty());
    List<SigningParty> selected = parties();
    selected.get(1).setTransactorName("用户指定签署人");
    service.participants(task.getId(), task.getVersion(), selected, false, "tester");
    service.defaultCounterparty(service.task(task.getId()));
    assertEquals(
        "用户指定签署人",
        repository.parties(task.getId()).stream()
            .filter(party -> "COUNTERPARTY".equals(party.getRole()))
            .findFirst()
            .get()
            .getTransactorName());
    verify(provider, never()).createFlow(any(), anyString(), anyList());
  }

  /** 空联系人保留为空，不伪造签署身份。 */
  @Test
  void missingCounterpartyContactStaysEmpty() {
    SigningParty defaults =
        new com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningAdapter(null, null)
            .defaultCounterparty(map("supplierName", "测试供应商"));
    assertEquals("", defaults.getTransactorName());
    assertEquals("", defaults.getTransactorAccount());
  }

  /** 通用工作台仅查询授权场景，过滤在数据库分页前执行。 */
  @Test
  void workbenchFiltersAuthorizedScopesBeforePagination() {
    SigningTask first = create();
    CreateSigningTaskCommand command = new CreateSigningTaskCommand();
    command.setBusinessType("purchase.order");
    command.setBusinessId("2");
    command.setSource("UPLOAD");
    service.create(command, "tester");
    SigningTask hidden = json.read(json.write(first), SigningTask.class);
    hidden.setId(id());
    hidden.setBusinessType("restricted.business");
    repository.insertTask(hidden);
    com.ruoyi.business.purchase.order.service.OrderService orders =
        mock(com.ruoyi.business.purchase.order.service.OrderService.class);
    SigningAccess access =
        new SigningAccess(
            Collections.singletonList(
                new com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningSupport(
                    orders)));
    com.ruoyi.business.signing.service.SigningWorkbenchService workbench =
        new com.ruoyi.business.signing.service.SigningWorkbenchService(mapper, json, access);
    com.ruoyi.business.signing.dto.SigningTaskQuery query =
        new com.ruoyi.business.signing.dto.SigningTaskQuery();
    query.setPageSize(1);
    try {
      login(new HashSet<>(Arrays.asList("signing:task:query")));
      assertEquals(0, workbench.list(query).getTotal());
      login(new HashSet<>(Arrays.asList("signing:task:query", "purchase:order:query")));
      com.ruoyi.common.core.page.PageResult<com.ruoyi.business.signing.dto.SigningTaskSummary>
          page = workbench.list(query);
      assertEquals(2, page.getTotal());
      assertEquals(1, page.getRows().size());
      assertEquals("采购订单", page.getRows().get(0).getBusinessName());
      query.setPageNum(2);
      assertNotEquals(
          page.getRows().get(0).getId(), workbench.list(query).getRows().get(0).getId());
      query.setBusinessType("restricted.business");
      assertEquals(0, workbench.list(query).getTotal());
      query.setBusinessType("purchase.order");
      query.setStatus("COMPLETED");
      assertEquals(0, workbench.list(query).getTotal());
      assertNull(com.github.pagehelper.PageHelper.getLocalPage());
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
  }

  /** 来源查询保持权限叠加，强制选择已审核单据并保留分页总数。 */
  @Test
  void sourceSelectionUsesApprovedOrdersAndBusinessPermission() {
    com.ruoyi.business.purchase.order.service.OrderService orders =
        mock(com.ruoyi.business.purchase.order.service.OrderService.class);
    com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningSupport support =
        new com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningSupport(orders);
    com.ruoyi.business.signing.dto.SigningSourceQuery query =
        new com.ruoyi.business.signing.dto.SigningSourceQuery();
    query.setPageNum(2);
    query.setPageSize(10);
    query.setKeyword("PO测试");
    try {
      login(new HashSet<>(Arrays.asList("signing:template:edit")));
      assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> support.sources(query));
      verifyNoInteractions(orders);
      login(new HashSet<>(Arrays.asList("purchase:order:query")));
      com.ruoyi.business.purchase.order.vo.OrderView order =
          new com.ruoyi.business.purchase.order.vo.OrderView();
      order.setOrderId(9L);
      order.setOrderNo("PO测试9");
      order.setSupplierName("单据供应商快照");
      when(orders.list(any()))
          .thenReturn(
              new com.ruoyi.common.core.page.PageResult<>(Collections.singletonList(order), 21));
      com.ruoyi.common.core.page.PageResult<com.ruoyi.business.signing.dto.SigningSourceView> page =
          support.sources(query);
      assertEquals(21, page.getTotal());
      assertEquals("9", page.getRows().get(0).getId());
      assertEquals("单据供应商快照", page.getRows().get(0).getCounterpartyName());
      org.mockito.ArgumentCaptor<com.ruoyi.business.purchase.order.dto.OrderQuery> request =
          org.mockito.ArgumentCaptor.forClass(
              com.ruoyi.business.purchase.order.dto.OrderQuery.class);
      verify(orders).list(request.capture());
      assertEquals("APPROVED", request.getValue().getStatus());
      assertEquals(2, request.getValue().getPageNum());
      assertEquals("PO测试", request.getValue().getOrderNo());
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
  }

  /** 同一业务有效任务只允许一个。 */
  @Test
  void autoSealRequiresExplicitSelectionAndPreservesCoordinates() {
    SigningTask task = confirmed();
    assertThrows(
        SigningException.class,
        () -> service.start(task.getId(), task.getVersion(), true, null, "tester"));
    assertEquals(task.getVersion(), service.task(task.getId()).getVersion());
    when(sealProvider.detail("seal-1"))
        .thenAnswer(
            call -> {
              assertFalse(
                  org.springframework.transaction.support.TransactionSynchronizationManager
                      .isActualTransactionActive());
              return enabledSeal();
            });
    service.start(task.getId(), task.getVersion(), true, "seal-1", "tester");
    SigningParty ours =
        repository.parties(task.getId()).stream()
            .filter(value -> "OUR".equals(value.getRole()))
            .findFirst()
            .get();
    assertTrue(ours.isAutoSign());
    assertEquals("测试合同章", ours.getSealName());
    assertEquals(SigningSealType.CONTRACT, ours.getSealType());
    assertEquals(100, ours.getPositions().get(0).getX());
    assertEquals(
        task.getPositionDocumentHash(), service.task(task.getId()).getPositionDocumentHash());
    step();
    verify(sealProvider, times(2)).detail("seal-1");
    verify(provider).createFlow(any(), anyString(), anyList());
  }

  /** 构造平台确认的启用机构印章。 */
  private SigningSeal enabledSeal() {
    SigningSeal seal = new SigningSeal();
    seal.setId("seal-1");
    seal.setName("测试合同章");
    seal.setOrganizationId("our-org");
    seal.setType(SigningSealType.CONTRACT);
    seal.setStatus(SigningSealStatus.ENABLED);
    seal.setSelectable(true);
    return seal;
  }

  /** 排队后印章被停用时不创建流程，不换用默认章，也不反复重试。 */
  @Test
  void disabledSealBeforeWorkerPreventsCreation() {
    SigningTask task = confirmed();
    when(sealProvider.detail("seal-1")).thenReturn(enabledSeal());
    service.start(task.getId(), task.getVersion(), true, "seal-1", "tester");
    when(sealProvider.detail("seal-1"))
        .thenThrow(new SigningException("e签宝平台返回错误（31400100）： 印章不存在"));
    assertTrue(step());
    verify(provider, never()).createFlow(any(), anyString(), anyList());
    assertEquals(SigningOperationStatus.FAILED, service.task(task.getId()).getOperationStatus());
    assertTrue(service.task(task.getId()).getLastError().contains("31400100"));
    assertFalse(step());
  }

  /** 选章核验期间另一请求修改任务时，不保存选章或排队。 */
  @Test
  void concurrentVersionChangeRollsBackLaunchSelection() {
    SigningTask task = confirmed();
    when(sealProvider.detail("seal-1"))
        .thenAnswer(
            call -> {
              jdbc.update("UPDATE sign_task SET version=version+1 WHERE task_id=?", task.getId());
              return enabledSeal();
            });
    assertThrows(
        SigningException.class,
        () -> service.start(task.getId(), task.getVersion(), true, "seal-1", "tester"));
    for (SigningParty party : repository.parties(task.getId())) {
      assertFalse(party.isAutoSign());
      assertNull(party.getSealId());
    }
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM sign_job WHERE task_id=? AND type='CREATE_FLOW'",
            Integer.class,
            task.getId()));
  }

  /** 手动模式允许不选章且不访问印章管理权限；指定印章同样要核验。 */
  @Test
  void manualSigningSupportsOptionalSeal() {
    SigningTask task = confirmed();
    when(sealProvider.detail("seal-1")).thenReturn(enabledSeal());
    service.start(task.getId(), task.getVersion(), false, "seal-1", "tester");
    SigningParty ours =
        repository.parties(task.getId()).stream()
            .filter(value -> "OUR".equals(value.getRole()))
            .findFirst()
            .get();
    assertFalse(ours.isAutoSign());
    assertEquals("seal-1", ours.getSealId());
    assertEquals("测试合同章", ours.getSealName());
  }

  /** 参与方接口提交的印章名称、类型和自动签章标记均不能替代发起校验。 */
  @Test
  void participantsCannotInjectLaunchSettings() {
    SigningTask task = prepared();
    List<SigningParty> input = parties();
    input.get(0).setAutoSign(true);
    input.get(0).setSealId("forged-seal");
    input.get(0).setSealName("伪造平台默认章");
    service.participants(task.getId(), task.getVersion(), input, true, "tester");
    SigningParty ours =
        repository.parties(task.getId()).stream()
            .filter(value -> "OUR".equals(value.getRole()))
            .findFirst()
            .get();
    assertFalse(ours.isAutoSign());
    assertNull(ours.getSealId());
    assertNull(ours.getSealName());
    verifyNoInteractions(sealProvider);
  }

  /** 同一业务有效任务只允许一个。 */
  @Test
  void duplicateTaskDenied() {
    SigningTask task = create();
    assertThrows(SigningException.class, this::create);
    assertEquals(task.getId(), repository.findActiveTask("purchase.order", "1").getId());
    assertEquals(1, repository.history(task.getBusinessType(), task.getBusinessId()).size());
  }

  /** 草稿撤回后释放名额，已完成任务仍然占用名额。 */
  @Test
  void activeTaskLookupFollowsOccupancyRules() {
    SigningTask task = create();
    assertNotNull(repository.findActiveTask("purchase.order", "1"));
    service.action(task.getId(), task.getVersion(), "cancel", null, "tester");
    assertNull(repository.findActiveTask("purchase.order", "1"));

    SigningTask completed = confirmed();
    service.start(completed.getId(), completed.getVersion(), "tester");
    assertTrue(step());
    assertTrue(step());
    remote.set(SigningStatus.COMPLETED);
    assertTrue(step());
    assertTrue(step());
    assertEquals(
        SigningStatus.COMPLETED, repository.findActiveTask("purchase.order", "1").getStatus());
  }

  /** JSON 保留零、false、中文和布尔字段。 */
  @Test
  void jsonCompatibility() {
    SigningTask task = create();
    SigningTask restored = repository.findTask(task.getId());
    assertEquals(false, restored.getSnapshot().get("enabled"));
    assertEquals(0, ((Number) restored.getSnapshot().get("zero")).intValue());
    assertEquals("测试供应商", restored.getSnapshot().get("supplierName"));
  }

  /** 保存过期页面不会覆盖文件。 */
  @Test
  void staleVersionRejected() {
    SigningTask task = prepared();
    assertThrows(
        SigningException.class,
        () -> service.prepare(task.getId(), 0, "new.pdf", new byte[] {1}, "tester"));
    assertEquals(DocumentStatus.READY, service.task(task.getId()).getDocumentStatus());
  }

  /** 对完整上传文档扩展名和空文件做校验。 */
  @Test
  void uploadValidation() {
    SigningTask task = create();
    assertThrows(
        SigningException.class,
        () -> service.prepare(task.getId(), 0, "../../x.pdf", new byte[] {1}, "tester"));
    assertThrows(
        SigningException.class,
        () -> service.prepare(task.getId(), 0, "x.exe", new byte[] {1}, "tester"));
    assertThrows(
        SigningException.class,
        () -> service.prepare(task.getId(), 0, "x.pdf", new byte[0], "tester"));
  }

  /** 可以保存尚未定位的双方，但不能直接发起。 */
  @Test
  void positionsMustBeConfirmed() {
    SigningTask task = prepared();
    List<SigningParty> parties = parties();
    parties.forEach(party -> party.getPositions().clear());
    SigningTask saved =
        service.participants(task.getId(), task.getVersion(), parties, false, "tester");
    assertThrows(
        SigningException.class, () -> service.start(saved.getId(), saved.getVersion(), "tester"));
  }

  /** 重新生成最终文件后原确认必须失效。 */
  @Test
  void changingDocumentInvalidatesConfirmation() {
    SigningTask task = confirmed();
    SigningTask preparing =
        service.prepare(
            task.getId(),
            task.getVersion(),
            "new.pdf",
            "%PDF-new".getBytes(StandardCharsets.UTF_8),
            "tester");
    assertNull(preparing.getPositionDocumentHash());
    assertNull(preparing.getPositionSession());
  }

  /** 创建、启动、同步、归档分阶段执行且完成不能再建第二份。 */
  @Test
  void completeWorkflowArchivesPrivately() {
    SigningTask task = confirmed();
    service.start(task.getId(), task.getVersion(), "tester");
    assertTrue(step());
    assertNotNull(service.task(task.getId()).getFlowId());
    assertTrue(step());
    assertEquals(SigningStatus.SIGNING, service.task(task.getId()).getStatus());
    remote.set(SigningStatus.COMPLETED);
    assertTrue(step());
    assertTrue(step());
    assertEquals(ArchiveStatus.READY, service.task(task.getId()).getArchiveStatus());
    assertEquals(
        1,
        repository.documents(task.getId()).stream()
            .filter(doc -> doc.getKind().equals("SIGNED"))
            .count());
    assertThrows(SigningException.class, this::create);
    verify(provider, times(1)).createFlow(any(), anyString(), anyList());
  }

  /** 手动同步会提前已有的周期同步任务，不等待三十秒轮询间隔。 */
  @Test
  void manualSyncExpeditesScheduledPlatformSync() {
    SigningTask task = confirmed();
    service.start(task.getId(), task.getVersion(), "tester");
    assertTrue(step());
    assertTrue(step());

    SigningTask active = service.task(task.getId());
    assertEquals(SigningStatus.SIGNING, active.getStatus());
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM sign_job WHERE task_id=? AND type='SYNC' AND status='PENDING' AND next_run_time<=NOW()",
            Integer.class,
            task.getId()));

    service.action(task.getId(), active.getVersion(), "sync", null, "tester");

    assertEquals(
        1,
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM sign_job WHERE task_id=? AND type='SYNC' AND status='PENDING' AND next_run_time<=NOW()",
            Integer.class,
            task.getId()));
    assertEquals(SigningJobType.SYNC, repository.claim().getType());
  }

  /** 网络超时未知结果不自动重试创建。 */
  @Test
  void ambiguousCreationNeverRetries() {
    SigningTask task = confirmed();
    when(provider.createFlow(any(), anyString(), anyList()))
        .thenThrow(new SigningException("timeout", true));
    service.start(task.getId(), task.getVersion(), "tester");
    step();
    assertEquals(SigningOperationStatus.UNKNOWN, service.task(task.getId()).getOperationStatus());
    assertFalse(step());
    assertThrows(
        SigningException.class,
        () -> service.start(task.getId(), service.task(task.getId()).getVersion(), "tester"));
    verify(provider, times(1)).createFlow(any(), anyString(), anyList());
  }

  /** 创建进程中断租约过期，必须核对而不能重新调用平台。 */
  @Test
  void expiredCreateLeaseRequiresReconciliation() {
    SigningTask task = confirmed();
    service.start(task.getId(), task.getVersion(), "tester");
    SigningJob claimed = repository.claim();
    assertEquals(SigningJobType.CREATE_FLOW, claimed.getType());
    jdbc.update(
        "UPDATE sign_job SET lease_until=TIMESTAMP '2000-01-01 00:00:00' WHERE job_id=?",
        claimed.getId());
    assertNull(repository.claim());
    assertEquals(SigningOperationStatus.UNKNOWN, service.task(task.getId()).getOperationStatus());
    verify(provider, never()).createFlow(any(), anyString(), anyList());
  }

  /** 领取令牌过期后旧执行者不得提交。 */
  @Test
  void staleLeaseCannotFinish() {
    SigningTask task = create();
    repository.enqueue(task.getId(), SigningJobType.PREPARE);
    SigningJob first = repository.claim();
    jdbc.update(
        "UPDATE sign_job SET lease_until=TIMESTAMP '2000-01-01 00:00:00' WHERE job_id=?",
        first.getId());
    SigningJob second = repository.claim();
    assertNotEquals(first.getToken(), second.getToken());
    assertFalse(repository.owns(first));
    assertThrows(SigningException.class, () -> repository.finish(first, "DONE", null));
    assertTrue(repository.owns(second));
  }

  /** 回调重复只登记一次且必须有正确签名。 */
  @Test
  void callbackAcknowledgesOnlyAfterVerifiedPersistence() {
    config.setEnabled(true);
    config.setAppId("test-app");
    config.setAppSecret("test-secret");
    EsignCallbackController controller =
        new EsignCallbackController(repository, transactions, json, config);
    MockHttpServletRequest invalid = callback("{\"action\":\"UNKNOWN\"}", false);
    assertEquals(401, controller.receive(invalid).getStatusCode().value());
    assertEquals(
        200,
        controller.receive(callback("{\"action\":\"UNKNOWN\"}", true)).getStatusCode().value());
    assertEquals(
        200,
        controller.receive(callback("{\"action\":\"UNKNOWN\"}", true)).getStatusCode().value());
    assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sign_event", Integer.class));
  }

  /** 持久化故障返回可重试 HTTP 错误，不能误确认。 */
  @Test
  void callbackDbFailureReturns503() {
    config.setEnabled(true);
    config.setAppId("test-app");
    config.setAppSecret("test-secret");
    SigningRepository broken = mock(SigningRepository.class);
    when(broken.insertEvent(any())).thenThrow(new IllegalStateException("db down"));
    assertEquals(
        503,
        new EsignCallbackController(broken, transactions, json, config)
            .receive(callback("{}", true))
            .getStatusCode()
            .value());
  }

  /** 私有键不能使用相对路径穿越。 */
  @Test
  void fileTraversalRejected() {
    assertThrows(SigningException.class, () -> files.read("../secret"));
    SigningDocument stored = files.put("合同.pdf", "%PDF-test".getBytes(StandardCharsets.UTF_8));
    assertArrayEquals(
        "%PDF-test".getBytes(StandardCharsets.UTF_8), files.read(stored.getStorageKey()));
  }

  /** 平台控件类型发生变化时拒绝继续使用旧映射。 */
  @Test
  void templateChangesRejected() {
    when(provider.template("template-1"))
        .thenReturn(map("components", Arrays.asList(map("componentId", "c1", "componentType", 1))));
    SigningTemplateBinding binding = new SigningTemplateBinding();
    binding.setName("采购合同");
    binding.setBusinessType("purchase.order");
    binding.setTemplateId("template-1");
    TemplateMapping mapping = new TemplateMapping();
    mapping.setComponentId("c1");
    mapping.setComponentType("1");
    mapping.setFieldKey("orderNo");
    binding.getMappings().add(mapping);
    templates.save(binding, "tester");
    when(provider.template("template-1"))
        .thenReturn(map("components", Arrays.asList(map("componentId", "c1", "componentType", 2))));
    assertThrows(SigningException.class, () -> templates.values(binding, map("orderNo", "PO1")));
  }

  /** 两个事务同时发起草稿也只能一个取得有效占用。 */
  @Test
  void concurrentCreationHasSingleWinner() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Callable<Boolean> create =
          () -> {
            start.await();
            try {
              create();
              return true;
            } catch (SigningException error) {
              return false;
            }
          };
      Future<Boolean> one = pool.submit(create), two = pool.submit(create);
      start.countDown();
      assertNotEquals(one.get(), two.get());
      assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM sign_task", Integer.class));
    } finally {
      pool.shutdownNow();
    }
  }

  /** 用户取消未发起草稿释放唯一槽位，同时保留历史。 */
  @Test
  void cancelledDraftReleasesSlot() {
    SigningTask task = create();
    service.action(task.getId(), task.getVersion(), "cancel", null, "tester");
    assertEquals(SigningStatus.REVOKED, repository.findTask(task.getId()).getStatus());
    SigningTask replacement = create();
    assertNotEquals(task.getId(), replacement.getId());
    assertEquals(2, repository.history("purchase.order", "1").size());
  }

  /** 当前位置通知对应旧会话时，不能覆盖当前文件。 */
  @Test
  void stalePositionCallbackIsIgnored() {
    SigningTask task = prepared();
    SigningEvent event = new SigningEvent();
    event.setId(id());
    event.setDeduplicationKey(id());
    event.setKind("GET_SEAL_POSITION");
    event.setActor("esign:callback");
    event.setPayload(
        json.write(
            map(
                "action",
                "GET_SEAL_POSITION",
                "customBizNum",
                "old-session",
                "components",
                Collections.emptyList())));
    repository.insertEvent(event);
    jdbc.update(
        "UPDATE sign_event SET next_run_time=TIMESTAMP '2000-01-01 00:00:00' WHERE event_id=?",
        event.getId());
    assertEquals(1, repository.pendingEvents().size());
    new SigningEventProcessor(repository, json, provider, transactions, service).runOnce();
    assertNull(service.task(task.getId()).getPositionDocumentHash());
    assertEquals(
        "IGNORED",
        jdbc.queryForObject(
            "SELECT status FROM sign_event WHERE event_id=?", String.class, event.getId()));
  }

  /** 当前定位会话的回调会校验文件并回填双方签章坐标。 */
  @Test
  void currentPositionCallbackStoresCoordinates() {
    SigningTask task = prepared();
    List<SigningParty> parties = parties();
    for (SigningParty party : parties) {
      party.getPositions().clear();
    }
    task = service.participants(task.getId(), task.getVersion(), parties, false, "tester");
    task.setPositionSession("current-session");
    service.save(task, task.getVersion(), "tester");

    List<Object> components = new ArrayList<Object>();
    for (String role : Arrays.asList("OUR", "COUNTERPARTY")) {
      components.add(
          map(
              "fileId",
              "file-1",
              "componentType",
              6,
              "signerRole",
              role,
              "componentPosition",
              map(
                  "componentPageNum",
                  2,
                  "componentPositionX",
                  120.5,
                  "componentPositionY",
                  240.5)));
    }
    SigningEvent event = new SigningEvent();
    event.setId(id());
    event.setDeduplicationKey(id());
    event.setKind("GET_SEAL_POSITION");
    event.setActor("esign:callback");
    event.setPayload(
        json.write(
            map(
                "action",
                "GET_SEAL_POSITION",
                "customBizNum",
                "current-session",
                "components",
                components)));
    repository.insertEvent(event);
    jdbc.update(
        "UPDATE sign_event SET next_run_time=TIMESTAMP '2000-01-01 00:00:00' WHERE event_id=?",
        event.getId());

    new SigningEventProcessor(repository, json, provider, transactions, service).runOnce();

    Map<String, SigningParty> savedParties = new HashMap<String, SigningParty>();
    for (SigningParty party : repository.parties(task.getId())) {
      savedParties.put(party.getRole(), party);
    }
    for (String role : Arrays.asList("OUR", "COUNTERPARTY")) {
      SigningPosition position = savedParties.get(role).getPositions().get(0);
      assertEquals(2, position.getPage());
      assertEquals(120.5, position.getX());
      assertEquals(240.5, position.getY());
    }
    assertEquals(
        "DONE",
        jdbc.queryForObject(
            "SELECT status FROM sign_event WHERE event_id=?", String.class, event.getId()));
  }

  /** 兼容 SaaS API 回调使用的 flowId 字段并安排权威状态同步。 */
  @Test
  void legacyFlowCallbackMatchesFlowId() {
    SigningTask task = confirmed();
    service.start(task.getId(), task.getVersion(), "tester");
    assertTrue(step());
    assertTrue(step());
    assertEquals(SigningStatus.SIGNING, service.task(task.getId()).getStatus());

    List<String> eventIds = new ArrayList<String>();
    for (String action : Arrays.asList("SIGN_FLOW_UPDATE", "SIGN_FLOW_FINISH")) {
      SigningEvent event = new SigningEvent();
      event.setId(id());
      event.setDeduplicationKey(id());
      event.setKind(action);
      event.setActor("esign:callback");
      event.setPayload(json.write(map("action", action, "flowId", "flow-1")));
      repository.insertEvent(event);
      jdbc.update(
          "UPDATE sign_event SET next_run_time=TIMESTAMP '2000-01-01 00:00:00' WHERE event_id=?",
          event.getId());
      eventIds.add(event.getId());
    }

    new SigningEventProcessor(repository, json, provider, transactions, service).runOnce();

    for (String eventId : eventIds) {
      assertEquals(
          task.getId(),
          jdbc.queryForObject(
              "SELECT task_id FROM sign_event WHERE event_id=?", String.class, eventId));
      assertEquals(
          "DONE",
          jdbc.queryForObject(
              "SELECT status FROM sign_event WHERE event_id=?", String.class, eventId));
    }
  }

  /** 功能权限和来源查看权限必须同时满足，使用真实方法权限代理。 */
  @Test
  void permissionsAreIndependentAndAdditive() {
    com.ruoyi.business.purchase.order.service.OrderService orders =
        mock(com.ruoyi.business.purchase.order.service.OrderService.class);
    when(orders.detail(1L)).thenReturn(new com.ruoyi.business.purchase.order.vo.OrderView());
    try (org.springframework.context.annotation.AnnotationConfigApplicationContext context =
        new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
      context.register(MethodSecurityConfig.class);
      context.registerBean("ss", com.ruoyi.framework.web.service.PermissionService.class);
      context.registerBean(
          SigningTaskController.class,
          () ->
              new SigningTaskController(
                  service,
                  repository,
                  provider,
                  files,
                  transactions,
                  new SigningAccess(
                      Collections.singletonList(
                          new com.ruoyi.business.signing.adapter.purchase
                              .PurchaseOrderSigningSupport(orders))),
                  config));
      context.registerBean(
          SigningSealController.class,
          () ->
              new SigningSealController(
                  service,
                  new SigningSealService(sealProvider),
                  new SigningAccess(
                      Collections.singletonList(
                          new com.ruoyi.business.signing.adapter.purchase
                              .PurchaseOrderSigningSupport(orders))),
                  repository));
      context.refresh();
      SigningTaskController controller = context.getBean(SigningTaskController.class);
      login(new HashSet<>(Arrays.asList("signing:task:query")));
      assertThrows(
          com.ruoyi.common.exception.ServiceException.class,
          () -> controller.history("purchase.order", "1"));
      login(new HashSet<>(Arrays.asList("purchase:order:query")));
      assertThrows(
          org.springframework.security.access.AccessDeniedException.class,
          () -> controller.history("purchase.order", "1"));
      login(new HashSet<>(Arrays.asList("purchase:order:query", "signing:task:query")));
      assertEquals(0, controller.history("purchase.order", "1").getData().size());
      assertThrows(
          org.springframework.security.access.AccessDeniedException.class,
          () -> controller.availability("purchase.order", "1"));
      login(new HashSet<>(Arrays.asList("purchase:order:query", "signing:task:start")));
      assertTrue(controller.availability("purchase.order", "1").getData().isAvailable());
      SigningTask activeTask = create();
      com.ruoyi.business.signing.dto.SigningAvailabilityView availability =
          controller.availability("purchase.order", "1").getData();
      assertFalse(availability.isAvailable());
      assertEquals(activeTask.getId(), availability.getTaskId());
      assertEquals(SigningStatus.DRAFT, availability.getStatus());
      login(new HashSet<>(Arrays.asList("purchase:order:query", "signing:task:query")));
      assertThrows(
          org.springframework.security.access.AccessDeniedException.class,
          () -> controller.download("task", "file", false));
      SigningTask task = activeTask;
      SigningSealController sealController = context.getBean(SigningSealController.class);
      assertThrows(
          org.springframework.security.access.AccessDeniedException.class,
          () -> sealController.list(task.getId(), 1, 8, null));
      assertThrows(
          SigningException.class, () -> sealController.preview(task.getId(), "not-selected"));
      login(new HashSet<>(Arrays.asList("signing:task:start")));
      assertThrows(
          com.ruoyi.common.exception.ServiceException.class,
          () -> sealController.list(task.getId(), 1, 8, null));
      verifyNoInteractions(sealProvider);
      login(new HashSet<>(Arrays.asList("purchase:order:query", "signing:task:start")));
      when(sealProvider.list(1, 8, null)).thenReturn(new SigningSealPage());
      assertEquals(0, sealController.list(task.getId(), 1, 8, null).getData().getTotal());
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
  }

  /** 企业对方使用来源供应商快照，个人对方不携带企业身份。 */
  @Test
  void counterpartyIdentityComesFromBusinessSnapshot() {
    SigningTask task = prepared();
    config.setOrganizationName("我方机构");
    config.setOrganizationId("our-org");
    com.ruoyi.business.purchase.order.service.OrderService orders =
        mock(com.ruoyi.business.purchase.order.service.OrderService.class);
    when(orders.detail(1L)).thenReturn(new com.ruoyi.business.purchase.order.vo.OrderView());
    login(new HashSet<>(Arrays.asList("purchase:order:query", "signing:task:start")));
    try {
      List<SigningParty> parties = parties();
      parties.get(1).setOrganizationId("untrusted-org");
      com.ruoyi.business.signing.dto.SigningParticipantsRequest request =
          new com.ruoyi.business.signing.dto.SigningParticipantsRequest();
      request.setVersion(task.getVersion());
      request.setParties(parties);
      SigningTaskController controller =
          new SigningTaskController(
              service,
              repository,
              provider,
              files,
              transactions,
              new SigningAccess(
                  Collections.singletonList(
                      new com.ruoyi.business.signing.adapter.purchase.PurchaseOrderSigningSupport(
                          orders))),
              config);
      controller.parties(task.getId(), request);
      SigningParty counterparty =
          repository.parties(task.getId()).stream()
              .filter(value -> "COUNTERPARTY".equals(value.getRole()))
              .findFirst()
              .get();
      assertNull(counterparty.getOrganizationId());
      assertEquals("测试供应商", counterparty.getOrganizationName());

      SigningTask latest = service.task(task.getId());
      List<SigningParty> personalParties = repository.parties(task.getId());
      SigningParty personalCounterparty =
          personalParties.stream()
              .filter(value -> "COUNTERPARTY".equals(value.getRole()))
              .findFirst()
              .get();
      personalCounterparty.setPartyType(SigningPartyType.PERSON);
      personalCounterparty.setOrganizationName("客户端伪造机构");
      request.setVersion(latest.getVersion());
      request.setParties(personalParties);
      controller.parties(task.getId(), request);

      SigningParty savedPersonalCounterparty =
          repository.parties(task.getId()).stream()
              .filter(value -> "COUNTERPARTY".equals(value.getRole()))
              .findFirst()
              .get();
      assertEquals(SigningPartyType.PERSON, savedPersonalCounterparty.getPartyType());
      assertNull(savedPersonalCounterparty.getOrganizationName());
      assertNull(savedPersonalCounterparty.getOrganizationId());
    } finally {
      org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
  }

  /** 周期同步已运行时撤销请求明确拒绝，不能成功返回却丢失命令。 */
  @Test
  void activeSyncCannotSwallowRevoke() {
    SigningTask task = confirmed();
    service.start(task.getId(), task.getVersion(), "tester");
    step();
    step();
    jdbc.update(
        "UPDATE sign_job SET next_run_time=TIMESTAMP '2000-01-01 00:00:00' WHERE status='PENDING'");
    SigningJob running = repository.claim();
    assertEquals(SigningJobType.SYNC, running.getType());
    SigningTask latest = service.task(task.getId());
    assertThrows(
        SigningException.class,
        () -> service.action(task.getId(), latest.getVersion(), "revoke", "测试原因", "tester"));
    assertEquals(latest.getVersion(), service.task(task.getId()).getVersion());
    assertTrue(repository.owns(running));
  }

  /** 模板标量、动态表列及版本审计使用受控映射，零和 false 不丢失。 */
  @Test
  void dynamicTableAndScalarMappingPreserveValues() {
    SigningField flag = new SigningField();
    flag.setKey("enabled");
    flag.setLabel("是否允许");
    flag.setType("TEXT");
    SigningField lines = new SigningField();
    lines.setKey("lines");
    lines.setLabel("明细");
    lines.setType("TABLE");
    SigningField quantity = new SigningField();
    quantity.setKey("quantity");
    quantity.setLabel("数量");
    quantity.setType("NUMBER");
    lines.getColumns().add(quantity);
    SigningBusinessAdapter adapter =
        new SigningBusinessAdapter() {
          public String businessType() {
            return "purchase.order";
          }

          public BusinessSnapshot snapshot(String id) {
            return new BusinessSnapshot();
          }

          public List<SigningField> fields() {
            return Arrays.asList(flag, lines);
          }
        };
    DefaultSigningTemplateService service =
        new DefaultSigningTemplateService(
            repository, provider, json, Collections.singletonMap("purchase.order", adapter));
    when(provider.template("table-template"))
        .thenReturn(
            map(
                "components",
                Arrays.asList(
                    map("componentId", "flag", "componentType", 1),
                    map(
                        "componentId",
                        "lines",
                        "componentType",
                        18,
                        "componentSpecialAttribute",
                        map("tableContent", Arrays.asList(map("row", map("column1", ""))))))));
    SigningTemplateBinding binding = new SigningTemplateBinding();
    binding.setName("动态表合同");
    binding.setBusinessType("purchase.order");
    binding.setTemplateId("table-template");
    TemplateMapping flagMap = new TemplateMapping();
    flagMap.setComponentId("flag");
    flagMap.setComponentType("1");
    flagMap.setFieldKey("enabled");
    TemplateMapping linesMap = new TemplateMapping();
    linesMap.setComponentId("lines");
    linesMap.setComponentType("18");
    linesMap.setFieldKey("lines");
    linesMap.getColumns().put("column1", "quantity");
    binding.setMappings(Arrays.asList(flagMap, linesMap));
    service.save(binding, "creator");
    List<Map<String, Object>> filled =
        service.values(
            binding, map("enabled", false, "lines", Arrays.asList(map("quantity", "0"))));
    assertEquals("false", filled.get(0).get("componentValue"));
    List<?> rows = json.read((String) filled.get(1).get("componentValue"), List.class);
    assertEquals("0", object(object(rows.get(0)).get("row")).get("column1"));
    binding.setCreateBy("untrusted");
    service.save(binding, "editor");
    assertEquals("creator", binding.getCreateBy());
    assertEquals("editor", binding.getUpdateBy());
  }

  /** 参数验证与实际保存使用同一个去空白后的来源值。 */
  @Test
  void sourceWhitespaceIsNormalized() {
    CreateSigningTaskCommand command = new CreateSigningTaskCommand();
    command.setBusinessType("purchase.order");
    command.setBusinessId("1");
    command.setSource(" UPLOAD ");
    SigningTask task = service.create(command, "tester");
    assertEquals("UPLOAD", task.getSource());
    service.prepare(
        task.getId(),
        task.getVersion(),
        "contract.pdf",
        "%PDF-check".getBytes(StandardCharsets.UTF_8),
        "tester");
    assertTrue(step());
    assertEquals(DocumentStatus.READY, service.task(task.getId()).getDocumentStatus());
  }

  /** 当前用户夹具，不使用管理员通配权限。 */
  private void login(Set<String> permissions) {
    com.ruoyi.common.core.domain.entity.SysUser user =
        new com.ruoyi.common.core.domain.entity.SysUser();
    user.setUserId(2L);
    user.setUserName("tester");
    user.setNickName("测试本人");
    user.setPhonenumber("13800000001");
    com.ruoyi.common.core.domain.model.LoginUser login =
        new com.ruoyi.common.core.domain.model.LoginUser(2L, 1L, user, permissions);
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                login, "unused", Collections.emptyList()));
  }

  /** 只启用权限代理，不启动 Web 服务。 */
  @org.springframework.context.annotation.Configuration
  @org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
  static class MethodSecurityConfig {}

  /** 创建有固定时间戳的签名回调夹具。 */
  private MockHttpServletRequest callback(String body, boolean valid) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("POST");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    request.addHeader("X-Tsign-Open-App-Id", "test-app");
    request.addHeader("X-Tsign-Open-SIGNATURE-ALGORITHM", "hmac-sha256");
    request.addHeader("X-Tsign-Open-TIMESTAMP", "1729489875363");
    request.addHeader(
        "X-Tsign-Open-SIGNATURE",
        valid
            ? hex(
                EsignRequestSigner.hmac(
                    "test-secret", ("1729489875363" + body).getBytes(StandardCharsets.UTF_8)))
            : "invalid");
    return request;
  }
}
