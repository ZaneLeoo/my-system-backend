package com.ruoyi.business.signing.adapter.purchase;

import com.ruoyi.signing.model.SigningField;
import java.util.*;

/** 明确登记签署可用字段，不使用反射开放所有数据库字段。 */
public final class PurchaseOrderSigningFieldCatalog {
  private PurchaseOrderSigningFieldCatalog() {}

  /** 构建中文业务目录，键名用于长期模板绑定。 */
  public static List<SigningField> fields() {
    List<SigningField> result = new ArrayList<SigningField>();
    String[][] scalars = {
      {"orderNo", "订单号", "TEXT"},
      {"orderDate", "订单日期", "DATE"},
      {"supplierCode", "供应商编码", "TEXT"},
      {"supplierName", "供应商名称", "TEXT"},
      {"contactName", "联系人", "TEXT"},
      {"contactPhone", "联系电话", "TEXT"},
      {"address", "联系地址", "TEXT"},
      {"buyerName", "采购员", "TEXT"},
      {"deptName", "采购部门", "TEXT"},
      {"currency", "币种", "TEXT"},
      {"netAmount", "未税金额", "NUMBER"},
      {"tax", "税额", "NUMBER"},
      {"grossAmount", "价税合计", "NUMBER"},
      {"remark", "备注", "TEXT"},
      {"ourCompanyName", "我方机构名称", "TEXT"}
    };
    for (String[] value : scalars) {
      result.add(field(value[0], value[1], value[2]));
    }
    SigningField lines = field("lines", "订单明细", "TABLE");
    String[][] columns = {
      {"lineNo", "行号", "NUMBER"},
      {"itemCode", "料品编码", "TEXT"},
      {"itemName", "料品名称", "TEXT"},
      {"specification", "规格型号", "TEXT"},
      {"unitName", "单位", "TEXT"},
      {"quantity", "数量", "NUMBER"},
      {"netPrice", "未税单价", "NUMBER"},
      {"grossPrice", "含税单价", "NUMBER"},
      {"taxRate", "税率（百分数）", "NUMBER"},
      {"netAmount", "未税金额", "NUMBER"},
      {"tax", "税额", "NUMBER"},
      {"grossAmount", "价税合计", "NUMBER"},
      {"expectedDate", "预计交期", "DATE"},
      {"remark", "行备注", "TEXT"}
    };
    for (String[] value : columns) {
      lines.getColumns().add(field(value[0], value[1], value[2]));
    }
    result.add(lines);
    return result;
  }

  /** 创建一个显式业务字段。 */
  private static SigningField field(String key, String label, String type) {
    SigningField result = new SigningField();
    result.setKey(key);
    result.setLabel(label);
    result.setType(type);
    return result;
  }
}
