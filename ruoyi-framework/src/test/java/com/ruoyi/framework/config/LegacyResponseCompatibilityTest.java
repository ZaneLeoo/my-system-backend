package com.ruoyi.framework.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.framework.web.exception.BusinessExceptionHandler;
import com.ruoyi.framework.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 确保业务范围的 R 异常处理不会改变若依原有接口响应。 */
class LegacyResponseCompatibilityTest {
  /** 原接口的错误仍由全局 AjaxResult 处理器响应，不增加 R 的 null data。 */
  @Test
  void legacyControllerKeepsAjaxResultErrorContract() throws Exception {
    MockMvcBuilders.standaloneSetup(new LegacyController())
        .setControllerAdvice(new BusinessExceptionHandler(), new GlobalExceptionHandler())
        .build()
        .perform(get("/legacy-response-probe"))
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.msg").value("旧接口业务错误"))
        .andExpect(jsonPath("$.data").doesNotHaveJsonPath());
  }

  /** 位于若依框架包下的旧式接口测试替身。 */
  @RestController
  static class LegacyController {
    /** 触发旧接口业务异常。 */
    @GetMapping("/legacy-response-probe")
    public AjaxResult failure() {
      throw new ServiceException("旧接口业务错误");
    }
  }
}
