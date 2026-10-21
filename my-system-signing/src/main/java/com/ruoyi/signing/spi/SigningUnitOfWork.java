package com.ruoyi.signing.spi;

/** 由宿主提供数据库事务。 */
public interface SigningUnitOfWork {
  /**
   * 执行本地原子事务，禁止在 action 内调用第三方网络。
   *
   * @param action 本地读写操作
   * @param <T> 返回值类型
   * @return 事务结果
   */
  <T> T execute(java.util.function.Supplier<T> action);
}
