package annis.service;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AnnisServiceExceptionHandler {

	@SuppressWarnings("unused")
	@Pointcut("execution(* annis.service.AnnisService.*(..))")
	private void annisServiceMethod() { }
	
	@AfterThrowing(pointcut="annisServiceMethod()", throwing="e")
	public void convertException(Exception e) throws AnnisServiceException {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		throw new AnnisServiceException(e.getLocalizedMessage());
	}
	
}
