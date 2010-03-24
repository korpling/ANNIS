package annis.dao;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
public class DaoTransactionManagement {

	// use Spring transaction management facilities
	private PlatformTransactionManager transactionManager;

	@SuppressWarnings("unused")
	@Pointcut("execution(* annis.dao.AnnisDao.*(..))")
	private void daoMethod() { }
	
	@Around("daoMethod()")
	public Object wrapDaoMethodInTransaction(final ProceedingJoinPoint pjp) throws Throwable {
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		
		return transactionTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus status) {
				
				// XXX: ugly, but ProceedingJoinPoint.proceed() and 
				// TransactionTemplate.doInTransaction() have incompatible
				// signatures
				try {
					return pjp.proceed();
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}
			
		});
		
	}
	
	///// Getter / Setter
	
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
}
