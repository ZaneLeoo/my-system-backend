package com.ruoyi.business.signing.controller;

import static com.ruoyi.signing.application.SigningSupport.*;

import com.ruoyi.business.signing.config.SigningProperties;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.signing.model.SigningEvent;
import com.ruoyi.signing.provider.esign.EsignCallbackVerifier;
import com.ruoyi.signing.spi.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 精确匿名入口，原始报文验签并持久化成功后才响应 200。 */
@RestController
@RequestMapping("/signing/callback/esign")
public class EsignCallbackController {
  private final SigningRepository repository;
  private final SigningUnitOfWork transactions;
  private final SigningJsonCodec json;
  private final EsignCallbackVerifier verifier;

  /** 注入可靠收件箱和当前应用配置。 */
  public EsignCallbackController(
      SigningRepository repository,
      SigningUnitOfWork transactions,
      SigningJsonCodec json,
      SigningProperties config) {
    this.repository = repository;
    this.transactions = transactions;
    this.json = json;
    this.verifier = new EsignCallbackVerifier(config);
  }

  /**
   * 接收签署和文件模板通知，不在回调内执行平台网络调用。
   *
   * @param request 原始通知请求
   * @return 真实 HTTP 状态；验签失败 401，持久化失败 503
   */
  @Anonymous
  @PostMapping
  public ResponseEntity<String> receive(HttpServletRequest request) {
    try {
      byte[] body;
      try (InputStream in = request.getInputStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[8192];
        int size;
        while ((size = in.read(buffer)) != -1) {
          if (out.size() + size > 1024 * 1024) {
            return ResponseEntity.status(413).body("too large");
          }
          out.write(buffer, 0, size);
        }
        body = out.toByteArray();
      }
      Map<String, String> query = new TreeMap<String, String>();
      for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
        if (entry.getValue().length != 1) {
          return ResponseEntity.badRequest().body("invalid query");
        }
        query.put(entry.getKey(), entry.getValue()[0]);
      }
      String appId = request.getHeader("X-Tsign-Open-App-Id");
      if (!verifier.verify(
          appId,
          request.getHeader("X-Tsign-Open-SIGNATURE-ALGORITHM"),
          request.getHeader("X-Tsign-Open-TIMESTAMP"),
          request.getHeader("X-Tsign-Open-SIGNATURE"),
          query,
          body)) {
        return ResponseEntity.status(401).body("invalid signature");
      }
      String payload = new String(body, StandardCharsets.UTF_8);
      Map<String, Object> parsed = object(json.read(payload, Map.class));
      SigningEvent event = new SigningEvent();
      event.setId(id());
      event.setDeduplicationKey(hash(appId + "\n" + payload));
      event.setKind(text(parsed, "action"));
      event.setActor("esign:callback");
      event.setPayload(payload);
      transactions.execute(() -> repository.insertEvent(event));
      return ResponseEntity.ok()
          .header("Content-Type", "application/json")
          .body("{\"code\":\"200\",\"msg\":\"success\"}");
    } catch (Exception error) {
      return ResponseEntity.status(503).body("retry later");
    }
  }

  /**
   * 即使参数解析失败也不能被全局业务异常处理包装成 HTTP 200。
   *
   * @param error 框架或请求异常
   * @return 通知失败响应
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> failed(Exception error) {
    return ResponseEntity.status(503).body("retry later");
  }
}
