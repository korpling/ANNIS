package de.deutschdiachrondigital.dddquery.helper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.administration.CorpusAdministration;
import annis.administration.SpringAnnisAdministrationDao;
import annis.dao.AnnisDao;
import de.deutschdiachrondigital.dddquery.parser.DddQueryParser;

// FIXME: is this needed?
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
	
	public AnnisDao getSqlGenerator() {
		return (AnnisDao) ctx.getBean("sqlGenerator");
	}
	
	public void runService() {
		if (_ctx == null) {
			_ctx = ctx;
			ctx = new ClassPathXmlApplicationContext(new String[] { "rmi.xml" }, _ctx);
		}
		// output on stdout interferes with starting the service
//		System.out.println("service running...");
	}
	
	public CorpusAdministration getAdministration() {
		return (CorpusAdministration) ctx.getBean("administration");
	}

	public SpringAnnisAdministrationDao getImportCorpus() {
		return (SpringAnnisAdministrationDao) ctx.getBean("importCorpus");
	}
	
}
