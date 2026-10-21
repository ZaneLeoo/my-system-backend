package com.ruoyi.business.masterdata.category.service;

import com.ruoyi.business.masterdata.category.dto.CategoryQuery;
import com.ruoyi.business.masterdata.category.dto.SaveCategoryRequest;
import com.ruoyi.business.masterdata.category.vo.CategoryTreeNode;
import com.ruoyi.business.masterdata.category.vo.CategoryView;
import java.util.List;

/** 分类查询及树结构维护契约。 */
public interface ItemCategoryService {
  /**
   * 查询匹配记录及其祖先，保持分类层级。
   *
   * @param query 分类编码、名称、状态及白名单排序条件
   * @return 符合条件的料品分类列表
   */
  List<CategoryView> list(CategoryQuery query);

  /**
   * 查询上级选择树；编辑时排除当前节点及后代。
   *
   * @param excludedId 编辑中的分类 ID，排除该节点及其后代；新增时可为空
   * @return 排除当前节点及后代后的分类选择树，保留虚拟根节点
   */
  List<CategoryTreeNode> tree(Long excludedId);

  /**
   * 查询未删除的分类详情，不存在时返回业务错误。
   *
   * @param id 料品分类 ID
   * @return 最新的料品分类详情及当前版本
   */
  CategoryView detail(Long id);

  /**
   * 校验并创建分类，返回包含生成 ID 和审计信息的详情。
   *
   * @param request 待新增的料品分类字段
   * @return 新增后的料品分类详情，包含生成的 ID 和审计信息
   */
  CategoryView create(SaveCategoryRequest request);

  /**
   * 校验树结构、状态及版本后修改分类。
   *
   * @param request 待修改的料品分类字段，包含主键和当前版本
   */
  void update(SaveCategoryRequest request);

  /**
   * 校验无子分类及版本后逻辑删除，同时记录删除审计。
   *
   * @param id 料品分类 ID
   * @param version 读取时的记录版本，用于拒绝过期页面操作
   */
  void delete(Long id, Integer version);
}
