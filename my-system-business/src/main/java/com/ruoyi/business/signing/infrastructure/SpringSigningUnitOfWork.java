package com.ruoyi.business.signing.infrastructure;

import com.ruoyi.signing.spi.SigningUnitOfWork;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** 仅包裹本地数据读写，平台网络操作不能进入该边界。 */
@Component
public class SpringSigningUnitOfWork implements SigningUnitOfWork {
  private final TransactionTemplate transactions;

  /** 注入宿主事务管理器。 */
  public SpringSigningUnitOfWork(PlatformTransactionManager manager) {
    transactions = new TransactionTemplate(manager);
  }

  /** {@inheritDoc} */
  public <T> T execute(Supplier<T> action) {
    return transactions.execute(status -> action.get());
  }
}
