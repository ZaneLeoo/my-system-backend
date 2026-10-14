package com.ruoyi.business.masterdata.category.service;

import com.ruoyi.business.masterdata.category.dto.CategoryQuery;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.category.vo.CategoryView;
import java.util.List;

/** 分类查询及树结构维护契约。 */
public interface ItemCategoryService {
  /** 查询匹配记录及其祖先，保持分类层级。 */
  List<CategoryView> list(CategoryQuery query);

  /** 查询上级选择树；编辑时排除当前节点及后代。 */
  List<CategoryTreeNode> tree(Long excludedId);

  /** 查询未删除的分类详情，不存在时返回业务错误。 */
  CategoryView detail(Long id);

  /** 校验并创建分类，返回包含生成 ID 和审计信息的详情。 */
  CategoryView create(SaveCategoryRequest request);

  /** 校验树结构、状态及版本后修改分类。 */
  void update(SaveCategoryRequest request);

  /** 校验无子分类及版本后逻辑删除，同时记录删除审计。 */
  void delete(Long id, Integer version);
}
