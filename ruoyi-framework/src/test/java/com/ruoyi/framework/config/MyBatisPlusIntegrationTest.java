package com.ruoyi.framework.config;

import static org.junit.jupiter.api.Assertions.*;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.mapper.SysPostMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MyBatisPlusIntegrationTest {
  @Test
  void generatedLogicDeleteAndExistingXmlPaginationShareOneFactory() {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
    new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                MybatisPlusAutoConfiguration.class, PageHelperAutoConfiguration.class))
        .withUserConfiguration(MyBatisConfig.class)
        .withBean(DataSource.class, () -> dataSource)
        .withPropertyValues(
            "mybatis.typeAliasesPackage=com.ruoyi.**.domain",
            "mybatis.mapperLocations=classpath*:mapper/system/*Mapper.xml",
            "mybatis.configLocation=classpath:mybatis/compatibility-config.xml",
            "pagehelper.helperDialect=mysql")
        .run(
            context -> {
              assertNull(context.getStartupFailure());
              assertEquals(1, context.getBeansOfType(SqlSessionFactory.class).size());
              assertEquals(1, context.getBeansOfType(SqlSessionTemplate.class).size());
              SqlSessionFactory factory = context.getBean(SqlSessionFactory.class);
              assertTrue(
                  factory.getConfiguration().getInterceptors().stream()
                      .anyMatch(PageInterceptor.class::isInstance));
              factory.getConfiguration().addMapper(ProbeMapper.class);
              try (Connection connection = dataSource.getConnection();
                  Statement sql = connection.createStatement()) {
                sql.execute(
                    "create table mp_logic_probe (id bigint auto_increment primary key, name varchar(100), del_flag char(1) default '0')");
                sql.execute(
                    "create table sys_post (post_id bigint primary key, post_code varchar(64), post_name varchar(50), post_sort int, status char(1), create_by varchar(64), create_time timestamp, update_by varchar(64), update_time timestamp, remark varchar(500))");
                sql.execute(
                    "insert into sys_post(post_id,post_code,post_name,post_sort,status) values (1,'A','测试岗位一',1,'0'), (2,'B','测试岗位二',2,'0')");
                try (SqlSession session = factory.openSession(true)) {
                  ProbeMapper mapper = session.getMapper(ProbeMapper.class);
                  LogicProbe probe = new LogicProbe();
                  probe.setName("中文分类验证");
                  probe.setDelFlag("0");
                  assertEquals(1, mapper.insert(probe));
                  assertNotNull(probe.getId());
                  assertEquals("中文分类验证", mapper.selectById(probe.getId()).getName());
                  assertEquals(1, mapper.deleteById(probe.getId()));
                  assertNull(mapper.selectById(probe.getId()));
                  assertEquals(0L, mapper.selectCount(null));
                  probe.setName("禁止更新已删除记录");
                  assertEquals(0, mapper.updateById(probe));
                  try (ResultSet row =
                      sql.executeQuery("select name, del_flag from mp_logic_probe")) {
                    assertTrue(row.next());
                    assertEquals("2", row.getString("del_flag"));
                    assertEquals("中文分类验证", row.getString("name"));
                  }
                  try {
                    PageHelper.startPage(1, 1);
                    List<SysPost> posts =
                        session.getMapper(SysPostMapper.class).selectPostList(new SysPost());
                    assertEquals(1, posts.size());
                    assertEquals(2, new PageInfo<>(posts).getTotal());
                  } finally {
                    PageHelper.clearPage();
                  }
                }
              }
            });
  }

  interface ProbeMapper extends BaseMapper<LogicProbe> {}

  @TableName("mp_logic_probe")
  public static class LogicProbe {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDelFlag() {
      return delFlag;
    }

    public void setDelFlag(String delFlag) {
      this.delFlag = delFlag;
    }
  }
}
