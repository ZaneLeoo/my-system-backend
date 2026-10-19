package com.ruoyi.system.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 公告已读记录表 sys_notice_read
 *
 * @author ruoyi
 */
@Getter
@Setter
public class SysNoticeRead {
  /** 主键 */
  private Long readId;

  /** 公告ID */
  private Long noticeId;

  /** 用户ID */
  private Long userId;

  /** 阅读时间 */
  private Date readTime;

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("readId", getReadId())
        .append("noticeId", getNoticeId())
        .append("userId", getUserId())
        .append("readTime", getReadTime())
        .toString();
  }
}
