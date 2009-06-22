package annis.dao;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DaoTiming {

	private static Logger log = Logger.getLogger(DaoTiming.class);
	
	@SuppressWarnings("unused")
	@Pointcut("execution(* annis.dao.AnnisDao.*(..))")
	private void daoMethod() { }
	
	@Around("daoMethod()")
	public Object logTiming(ProceedingJoinPoint pjp) throws Throwable {
		// get current time
		long start = new Date().getTime();
		
		// execute method
		Object result = pjp.proceed();
		
		// log time needed for method
		StringBuffer sb = new StringBuffer();
		sb.append(pjp.getSignature().getName());
		sb.append("(");
		sb.append(StringUtils.join(pjp.getArgs(), ", "));
		sb.append(") took ");
		sb.append(new Date().getTime() - start);
		sb.append(" ms");
		log.info(sb);
		
		// return method return value
		return result;
	}
	
}
