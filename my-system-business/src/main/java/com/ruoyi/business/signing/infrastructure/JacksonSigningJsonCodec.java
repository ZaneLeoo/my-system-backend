package com.ruoyi.business.signing.infrastructure;

import com.ruoyi.signing.api.SigningException;
import com.ruoyi.signing.spi.SigningJsonCodec;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 当前宿主使用 Jackson 3；公共模块不需要携带任何 Jackson 依赖。 */
@Component
public class JacksonSigningJsonCodec implements SigningJsonCodec {
  private final JsonMapper mapper = JsonMapper.builder().build();

  /** {@inheritDoc} */
  public String write(Object value) {
    return mapper.writeValueAsString(value);
  }

  /** {@inheritDoc} */
  public <T> T read(String value, Class<T> type) {
    try {
      return mapper.readValue(value, type);
    } catch (RuntimeException e) {
      throw new SigningException("签署数据格式不正确");
    }
  }
}
