package de.deutschdiachrondigital.dddquery.helper;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annisservice.administration.CorpusAdministration;
import annisservice.administration.AnnisDatabaseUtils;

import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;
import de.deutschdiachrondigital.dddquery.sql.GraphMatcher;

public class BeanFactory {

	private static ApplicationContext ctx;
	private static ApplicationContext _ctx;
	
	public BeanFactory() {
		if (ctx == null) 
			ctx = new ClassPathXmlApplicationContext("annis-service.xml");
	}
	
	public DddQueryParser getDddQueryParser() {
		return (DddQueryParser) ctx.getBean("dddQueryParser");
	}
	
	public GraphMatcher getSqlGenerator() {
		return (GraphMatcher) ctx.getBean("sqlGenerator");
	}
	
	public Shell getShell() {
		return (Shell) ctx.getBean("shell");
	}
	
	public Benchmark getBenchmark() {
		return (Benchmark) ctx.getBean("benchmark");
	}
	
	public void runService() {
		if (_ctx == null) {
			_ctx = ctx;
			ctx = new ClassPathXmlApplicationContext(new String[] { "rmi.xml" }, _ctx);
		}
		// output on stdout interferes with starting the service
//		System.out.println("service running...");
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getTreeTests() {
		return (Map<String, String>) ctx.getBean("treeTests");
	}

	public CorpusAdministration getAdministration() {
		return (CorpusAdministration) ctx.getBean("administration");
	}

	public AnnisDatabaseUtils getImportCorpus() {
		return (AnnisDatabaseUtils) ctx.getBean("importCorpus");
	}
	
}
