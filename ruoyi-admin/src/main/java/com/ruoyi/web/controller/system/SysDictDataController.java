package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.ISysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {
  @Autowired private ISysDictDataService dictDataService;

  @Autowired private ISysDictTypeService dictTypeService;

  /**
   * 按筛选条件分页查询字典数据。
   *
   * @param dictData 字典数据筛选条件
   * @return 当前页记录及全部匹配记录的总数
   */
  @PreAuthorize("@ss.hasPermi('system:dict:list')")
  @GetMapping("/list")
  public TableDataInfo list(SysDictData dictData) {
    startPage();
    List<SysDictData> list = dictDataService.selectDictDataList(dictData);
    return getDataTable(list);
  }

  /**
   * 按筛选条件导出字典数据。
   *
   * @param response 用于写入下载文件或响应内容的 HTTP 响应
   * @param dictData 字典数据筛选条件
   */
  @Log(title = "字典数据", businessType = BusinessType.EXPORT)
  @PreAuthorize("@ss.hasPermi('system:dict:export')")
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysDictData dictData) {
    List<SysDictData> list = dictDataService.selectDictDataList(dictData);
    ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
    util.exportExcel(response, list, "字典数据");
  }

  /**
   * 查询字典数据详细
   *
   * @param dictCode 字典数据 ID
   * @return 匹配的记录详情
   */
  @PreAuthorize("@ss.hasPermi('system:dict:query')")
  @GetMapping(value = "/{dictCode}")
  public AjaxResult getInfo(@PathVariable Long dictCode) {
    return success(dictDataService.selectDictDataById(dictCode));
  }

  /**
   * 根据字典类型查询字典数据信息
   *
   * @param dictType 字典类型或筛选条件
   * @return 匹配的配置值或字典选项
   */
  @GetMapping(value = "/type/{dictType}")
  public AjaxResult dictType(@PathVariable String dictType) {
    List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
    if (StringUtils.isNull(data)) {
      data = new ArrayList<SysDictData>();
    }
    return success(data);
  }

  /**
   * 新增字典类型
   *
   * @param dict 待新增的字典信息
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:dict:add')")
  @Log(title = "字典数据", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysDictData dict) {
    dict.setCreateBy(getUsername());
    return toAjax(dictDataService.insertDictData(dict));
  }

  /**
   * 修改保存字典类型
   *
   * @param dict 待修改的字典信息及其标识
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:dict:edit')")
  @Log(title = "字典数据", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysDictData dict) {
    dict.setUpdateBy(getUsername());
    return toAjax(dictDataService.updateDictData(dict));
  }

  /**
   * 删除字典类型
   *
   * @param dictCodes 字典数据 ID 集合
   * @return 操作结果，失败时包含具体错误说明
   */
  @PreAuthorize("@ss.hasPermi('system:dict:remove')")
  @Log(title = "字典类型", businessType = BusinessType.DELETE)
  @DeleteMapping("/{dictCodes}")
  public AjaxResult remove(@PathVariable Long[] dictCodes) {
    dictDataService.deleteDictDataByIds(dictCodes);
    return success();
  }
}
