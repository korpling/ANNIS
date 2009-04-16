package annis.service.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Ignore;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Deprecated
public class TestGetNodeAttributeSetHandler {

	@Ignore
	public void getResult() {
		GetNodeAttributeSetHandler getNodeAttributeSetHandler = new GetNodeAttributeSetHandler();
		DataSource dataSource = getDataSource();
		getNodeAttributeSetHandler.setDataSource(dataSource);
	
		List<Long> corpusList = new ArrayList<Long>();
		corpusList.add(1L);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(getNodeAttributeSetHandler.CORPUS_LIST, corpusList);

		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss,SSS} [%t] %C{1} %p: %m\n")));

		getNodeAttributeSetHandler.getResult(args);
		
		// XXX: Test not finished
	}

	// FIXME: remove hard-coded dependency to database
	private DriverManagerDataSource getDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://localhost:5432/annis");
		dataSource.setUsername("annis");
		dataSource.setPassword("annis");
		return dataSource;
	}
	
}
