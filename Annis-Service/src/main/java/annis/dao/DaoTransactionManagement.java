/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
				status.setRollbackOnly();
				
				// XXX: ugly, but ProceedingJoinPoint.proceed() and 
				// TransactionTemplate.doInTransaction() have incompatible
				// signatures
				try {
					return pjp.proceed();
				} catch (Throwable t) {
					throw new RuntimeException(t.getLocalizedMessage(), t);
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
