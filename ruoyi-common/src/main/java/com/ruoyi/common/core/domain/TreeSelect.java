package com.ruoyi.common.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.utils.StringUtils;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 * Treeselect树结构实体类
 *
 * @author ruoyi
 */
@Getter
@Setter
public class TreeSelect implements Serializable {
  /** Java 序列化版本号。 */
  private static final long serialVersionUID = 1L;

  /** 节点ID */
  private Long id;

  /** 节点名称 */
  private String label;

  /** 节点禁用 */
  private boolean disabled = false;

  /** 子节点 */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TreeSelect> children;

  public TreeSelect() {}

  public TreeSelect(SysDept dept) {
    this.id = dept.getDeptId();
    this.label = dept.getDeptName();
    this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
    this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
  }

  public TreeSelect(SysMenu menu) {
    this.id = menu.getMenuId();
    this.label = menu.getMenuName();
    this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
  }
}
