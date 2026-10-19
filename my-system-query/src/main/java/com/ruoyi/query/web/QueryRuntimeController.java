package com.ruoyi.query.web;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.query.engine.QueryRuntimeService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** 所有运行时方法通过业务适配器检查列表权限。 */
@RestController
@RequestMapping("/query/runtime/{queryKey}")
public class QueryRuntimeController {
  private final QueryRuntimeService service;

  /** 注入业务授权后的运行时服务。 */
  public QueryRuntimeController(QueryRuntimeService service) {
    this.service = service;
  }

  /**
   * 读取当前业务查询已发布的界面配置。
   *
   * <p>按当前登录用户检查对应业务列表权限；只返回构建查询控件需要的描述，不暴露物理列名、表别名或关联 SQL。
   *
   * @param queryKey 稳定业务查询编码，例如 masterdata.item 或 purchase.order
   * @return 当前发布版本及可用于构建查询界面的安全字段描述
   * @throws com.ruoyi.common.exception.ServiceException 查询未发布、已停用或字段目录失效时
   */
  @GetMapping("/metadata")
  public R<?> metadata(@PathVariable String queryKey) {
    return R.ok(service.metadata(queryKey));
  }

  /**
   * 搜索当前发布字段允许使用的选项。
   *
   * <p>字段和提供器必须已开放，并校验业务列表权限；选项允许包含未删除的停用档案，以便查询历史记录。
   *
   * @param queryKey 稳定业务查询编码，例如 masterdata.item 或 purchase.order
   * @param field 当前查询配置中的稳定字段标识
   * @param keyword 编码或名称搜索关键字，可为空
   * @param page 从 1 开始的选项页码，默认 1，最大 100000
   * @return 字段可选值的当前页及匹配总数
   * @throws com.ruoyi.common.exception.ServiceException 字段未开放、选项来源未登记或分页参数无效时
   */
  @GetMapping("/options/{field}")
  public R<?> options(
      @PathVariable String queryKey,
      @PathVariable String field,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") Integer page) {
    return R.ok(service.options(queryKey, field, keyword, null, page));
  }

  /**
   * 批量回显当前查询字段已保存的选项值。
   *
   * <p>每次接受 1～100 个不同正整数 ID，按业务列表权限读取；未找到的值不生成虚假选项，客户端应保留失效值提示。
   *
   * @param queryKey 稳定业务查询编码，例如 masterdata.item 或 purchase.order
   * @param field 当前查询配置中的稳定字段标识
   * @param ids 批量回显的不同记录 ID，每次最多 100 个
   * @return 指定查询值对应的选项及标签，不存在的值不会被伪造
   * @throws com.ruoyi.common.exception.ServiceException 字段未开放，或 ID 数量、格式及去重校验失败时
   */
  @GetMapping("/options/{field}/resolve")
  public R<?> resolve(
      @PathVariable String queryKey, @PathVariable String field, @RequestParam List<Long> ids) {
    return R.ok(service.options(queryKey, field, null, ids, 1));
  }
}
