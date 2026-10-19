package com.ruoyi.business.model;

import static org.junit.jupiter.api.Assertions.*;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysNotice;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 数据类使用 Lombok 后，保留既有 JSON 字段及 Getter 上的校验契约。 */
class ModelCompatibilityTest {
  @Test
  void passwordRemainsWriteOnly() {
    JsonMapper mapper = JsonMapper.builder().build();
    SysUser user =
        mapper.readValue(
            "{\"userName\":\"tester\",\"password\":\"local-test-secret\"}", SysUser.class);

    assertEquals("local-test-secret", user.getPassword());
    JsonNode json = mapper.valueToTree(user);
    assertEquals("tester", json.get("userName").asString());
    assertFalse(json.has("password"));
  }

  @Test
  void noticeReadFlagKeepsItsOriginalJsonName() {
    JsonMapper mapper = JsonMapper.builder().build();
    SysNotice notice = mapper.readValue("{\"isRead\":true}", SysNotice.class);

    assertTrue(notice.getIsRead());
    JsonNode json = mapper.valueToTree(notice);
    assertTrue(json.get("isRead").asBoolean());
    assertFalse(json.has("read"));
  }

  @Test
  void getterValidationStillRejectsInvalidUserAndNotice() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      SysUser user = new SysUser();
      user.setUserName(" ");
      user.setEmail("invalid-email");
      Set<String> invalidProperties =
          factory.getValidator().validate(user).stream()
              .map(violation -> violation.getPropertyPath().toString())
              .collect(Collectors.toSet());
      assertTrue(invalidProperties.contains("userName"));
      assertTrue(invalidProperties.contains("email"));

      SysNotice notice = new SysNotice();
      notice.setNoticeTitle(" ");
      assertTrue(
          factory.getValidator().validate(notice).stream()
              .anyMatch(violation -> violation.getPropertyPath().toString().equals("noticeTitle")));
    }
  }
}
