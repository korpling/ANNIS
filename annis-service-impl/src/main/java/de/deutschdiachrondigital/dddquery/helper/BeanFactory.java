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
package de.deutschdiachrondigital.dddquery.helper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import annis.administration.FullFactsCorpusAdministration;
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
	
	public FullFactsCorpusAdministration getAdministration() {
		return (FullFactsCorpusAdministration) ctx.getBean("administration");
	}

	public SpringAnnisAdministrationDao getImportCorpus() {
		return (SpringAnnisAdministrationDao) ctx.getBean("importCorpus");
	}
	
}
