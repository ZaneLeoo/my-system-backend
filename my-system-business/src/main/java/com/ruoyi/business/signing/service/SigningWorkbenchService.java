package com.ruoyi.business.signing.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.business.signing.controller.SigningAccess;
import com.ruoyi.business.signing.dto.SigningTaskQuery;
import com.ruoyi.business.signing.dto.SigningTaskSummary;
import com.ruoyi.business.signing.persistence.mapper.SigningMapper;
import com.ruoyi.common.core.page.PageResult;
import com.ruoyi.signing.model.SigningTask;
import com.ruoyi.signing.spi.SigningJsonCodec;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 独立电子签工作台查询，不向签署核心引入分页与权限框架。 */
@Service
public class SigningWorkbenchService {
  private final SigningMapper mapper;
  private final SigningJsonCodec json;
  private final SigningAccess access;

  /** 注入宿主查询与访问边界。 */
  public SigningWorkbenchService(
      SigningMapper mapper, SigningJsonCodec json, SigningAccess access) {
    this.mapper = mapper;
    this.json = json;
    this.access = access;
  }

  /** 查询授权场景内的摘要，转换前保留总数并清理分页上下文。 */
  @Transactional(readOnly = true)
  public PageResult<SigningTaskSummary> list(SigningTaskQuery query) {
    List<String> types = access.accessibleTypes();
    if (types.isEmpty()) {
      return new PageResult<>();
    }
    try {
      PageHelper.startPage(query.getPageNum(), query.getPageSize());
      List<String> payloads = mapper.taskList(query, types);
      long total = new PageInfo<>(payloads).getTotal();
      List<SigningTaskSummary> rows = new ArrayList<>();
      for (String payload : payloads) {
        SigningTask task = json.read(payload, SigningTask.class);
        SigningTaskSummary row = new SigningTaskSummary();
        row.setId(task.getId());
        row.setBusinessType(task.getBusinessType());
        row.setBusinessName(access.support(task.getBusinessType()).businessName());
        row.setBusinessId(task.getBusinessId());
        row.setTitle(task.getTitle());
        row.setStatus(task.getStatus());
        row.setCreateBy(task.getCreateBy());
        row.setCreateTime(task.getCreateTime());
        row.setUpdateTime(task.getUpdateTime());
        rows.add(row);
      }
      return new PageResult<>(rows, total);
    } finally {
      PageHelper.clearPage();
    }
  }
}
