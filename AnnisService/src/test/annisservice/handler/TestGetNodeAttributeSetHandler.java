package annisservice.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import annisservice.ifaces.AnnisAttributeSet;

public class TestGetNodeAttributeSetHandler {

	@Test
	public void getResult() {
		GetNodeAttributeSetHandler getNodeAttributeSetHandler = new GetNodeAttributeSetHandler();
		DataSource dataSource = getDataSource();
		getNodeAttributeSetHandler.setDataSource(dataSource);
	
		List<Long> corpusList = new ArrayList<Long>();
		corpusList.add(1L);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getNodeAttributeSetHandler.CORPUS_LIST, corpusList);

		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));

		AnnisAttributeSet foo = getNodeAttributeSetHandler.getResult(args);
		
		Logger log = Logger.getLogger(this.getClass());
	}

	private DriverManagerDataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/dddquery");
		dataSource.setUsername("dddquery");
		dataSource.setPassword("dddquery");
		return dataSource;
	}
	
}
