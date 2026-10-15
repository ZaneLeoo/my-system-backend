package com.ruoyi.framework.config;

import static org.junit.jupiter.api.Assertions.*;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.constant.DateTimeConstants;
import com.ruoyi.common.constant.LogicDeleteConstants;
import com.ruoyi.common.core.domain.AuditEntity;
import com.ruoyi.common.core.domain.BusinessEntity;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.mybatis.BusinessAuditHandler;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** 验证公共基类可用于分类之外的实体，并保护创建信息及操作者身份。 */
class BusinessAuditIntegrationTest {
  /** UTC 部署下新增、更新及逻辑删除共用的填充路径仍采样北京时间。 */
  @Test
  @ResourceLock("java.util.TimeZone.default")
  void auditTimeDoesNotDependOnJvmDefaultZone() {
    TimeZone previous = TimeZone.getDefault();
    try {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
      login("timezone-probe");
      BusinessAuditHandler handler = new BusinessAuditHandler();
      Probe probe = new Probe();
      LocalDateTime before = LocalDateTime.now(DateTimeConstants.BUSINESS_ZONE);
      handler.insertFill(SystemMetaObject.forObject(probe));
      LocalDateTime after = LocalDateTime.now(DateTimeConstants.BUSINESS_ZONE);
      assertFalse(probe.getCreateTime().isBefore(before));
      assertFalse(probe.getCreateTime().isAfter(after));
      assertEquals(probe.getCreateTime(), probe.getUpdateTime());
      LocalDateTime created = probe.getCreateTime();
      probe.setUpdateTime(LocalDateTime.of(2000, 1, 1, 0, 0));
      before = LocalDateTime.now(DateTimeConstants.BUSINESS_ZONE);
      handler.updateFill(SystemMetaObject.forObject(probe));
      after = LocalDateTime.now(DateTimeConstants.BUSINESS_ZONE);
      assertFalse(probe.getUpdateTime().isBefore(before));
      assertFalse(probe.getUpdateTime().isAfter(after));
      assertEquals(created, probe.getCreateTime());
    } finally {
      TimeZone.setDefault(previous);
    }
  }

  /** 每个用例结束后清除线程身份，避免影响旧实体兼容性测试。 */
  @AfterEach
  void clearIdentity() {
    SecurityContextHolder.clearContext();
  }

  /** 通过真实手工 MP 工厂验证继承字段、更新保护、逻辑删除和无身份写入。 */
  @Test
  void inheritedFieldsAndSharedHandlerSupportDifferentEntityTypes() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MybatisPlusAutoConfiguration.class))
        .withUserConfiguration(MyBatisConfig.class, BusinessAuditHandler.class)
        .withBean(DataSource.class, () -> dataSource)
        .withPropertyValues(
            "mybatis.typeAliasesPackage=com.ruoyi.**.domain",
            "mybatis.mapperLocations=classpath*:mapper/system/*Mapper.xml",
            "mybatis.configLocation=classpath:mybatis/compatibility-config.xml")
        .run(
            context -> {
              assertNull(context.getStartupFailure());
              SqlSessionFactory factory = context.getBean(SqlSessionFactory.class);
              factory.getConfiguration().addMapper(ProbeMapper.class);
              factory.getConfiguration().addMapper(NoteMapper.class);
              try (Connection connection = dataSource.getConnection();
                  Statement sql = connection.createStatement()) {
                sql.execute(
                    "CREATE TABLE business_audit_probe (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), del_flag CHAR(1) DEFAULT '0', create_by VARCHAR(64) NOT NULL, create_time TIMESTAMP NOT NULL, update_by VARCHAR(64), update_time TIMESTAMP)");
                sql.execute(
                    "CREATE TABLE audit_note (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), create_by VARCHAR(64) NOT NULL, create_time TIMESTAMP NOT NULL, update_by VARCHAR(64), update_time TIMESTAMP)");
                try (SqlSession session = factory.openSession(true)) {
                  ProbeMapper mapper = session.getMapper(ProbeMapper.class);
                  NoteMapper noteMapper = session.getMapper(NoteMapper.class);
                  login("creator");
                  Probe probe = new Probe();
                  probe.setName("公共业务实体");
                  probe.setCreateBy("forged");
                  probe.setCreateTime(LocalDateTime.of(2000, 1, 1, 0, 0));
                  assertEquals(1, mapper.insert(probe));
                  Probe created = mapper.selectById(probe.getId());
                  assertEquals(LogicDeleteConstants.NOT_DELETED, created.getDelFlag());
                  assertEquals("creator", created.getCreateBy());
                  assertEquals("creator", created.getUpdateBy());
                  assertTrue(created.getCreateTime().getYear() > 2000);

                  AuditNote note = new AuditNote();
                  note.setName("仅需要审计的实体");
                  assertEquals(1, noteMapper.insert(note));
                  assertEquals("creator", noteMapper.selectById(note.getId()).getCreateBy());

                  login("editor");
                  Probe update = new Probe();
                  update.setId(probe.getId());
                  update.setName("公共实体更新");
                  update.setCreateBy("overwrite-attempt");
                  update.setCreateTime(LocalDateTime.of(2001, 1, 1, 0, 0));
                  update.setUpdateBy("stale-editor");
                  update.setUpdateTime(LocalDateTime.of(2001, 1, 1, 0, 0));
                  assertEquals(1, mapper.updateById(update));
                  Probe edited = mapper.selectById(probe.getId());
                  assertEquals("creator", edited.getCreateBy());
                  assertEquals(created.getCreateTime(), edited.getCreateTime());
                  assertEquals("editor", edited.getUpdateBy());
                  assertFalse(edited.getUpdateTime().isBefore(created.getUpdateTime()));
                  note.setName("审计实体更新");
                  assertEquals(1, noteMapper.updateById(note));
                  assertEquals("editor", noteMapper.selectById(note.getId()).getUpdateBy());

                  login("deleter");
                  assertEquals(1, mapper.deleteById(edited));
                  assertNull(mapper.selectById(probe.getId()));
                  assertEquals(0, mapper.updateById(update));
                  try (ResultSet row =
                      sql.executeQuery(
                          "SELECT del_flag, create_by, update_by, update_time FROM business_audit_probe")) {
                    assertTrue(row.next());
                    assertEquals(LogicDeleteConstants.DELETED, row.getString("del_flag"));
                    assertEquals("creator", row.getString("create_by"));
                    assertEquals("deleter", row.getString("update_by"));
                    assertNotNull(row.getTimestamp("update_time"));
                  }

                  SecurityContextHolder.clearContext();
                  Probe anonymous = new Probe();
                  anonymous.setName("禁止匿名新增");
                  assertMissingActor(() -> mapper.insert(anonymous));
                  assertMissingActor(() -> noteMapper.updateById(note));
                  Probe deletion = new Probe();
                  deletion.setId(probe.getId());
                  assertMissingActor(() -> mapper.deleteById(deletion));
                  try (ResultSet count =
                      sql.executeQuery("SELECT COUNT(*) FROM business_audit_probe")) {
                    assertTrue(count.next());
                    assertEquals(1, count.getInt(1));
                  }
                }
              }
            });
  }

  /** 验证写入失败源于缺少操作者，而非无关 SQL 或字段映射错误。 */
  private void assertMissingActor(org.junit.jupiter.api.function.Executable action) {
    Throwable cause = assertThrows(RuntimeException.class, action);
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    assertInstanceOf(ServiceException.class, cause);
  }

  /** 设置与项目一致的认证用户，供公共填充器读取。 */
  private void login(String username) {
    SysUser user = new SysUser();
    user.setUserName(username);
    LoginUser principal = new LoginUser(2L, 1L, user, Set.of("*:*:*"));
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  /** 测试逻辑删除业务实体的 MP Mapper。 */
  interface ProbeMapper extends BaseMapper<Probe> {}

  /** 测试仅含审计字段实体的 MP Mapper。 */
  interface NoteMapper extends BaseMapper<AuditNote> {}

  /** 与分类无关的业务实体，继承两层公共字段。 */
  @TableName("business_audit_probe")
  public static class Probe extends BusinessEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 返回测试主键。 */
    public Long getId() {
      return id;
    }

    /** 设置测试主键。 */
    public void setId(Long id) {
      this.id = id;
    }

    /** 返回测试名称。 */
    public String getName() {
      return name;
    }

    /** 设置测试名称。 */
    public void setName(String name) {
      this.name = name;
    }
  }

  /** 无逻辑删除字段的实体，验证审计能力可单独使用。 */
  @TableName("audit_note")
  public static class AuditNote extends AuditEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 返回测试主键。 */
    public Long getId() {
      return id;
    }

    /** 设置测试主键。 */
    public void setId(Long id) {
      this.id = id;
    }

    /** 返回测试名称。 */
    public String getName() {
      return name;
    }

    /** 设置测试名称。 */
    public void setName(String name) {
      this.name = name;
    }
  }
}
