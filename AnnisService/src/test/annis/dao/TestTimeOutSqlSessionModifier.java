package annis.dao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class TestTimeOutSqlSessionModifier {

	// object under test
	private TimeOutSqlSessionModifier timeOutSqlSessionModifier;

	// injected Spring JDBC helper
	private @Mock SimpleJdbcTemplate simpleJdbcTemplate;
	
	@Before
	public void setup() {
		initMocks(this);
		timeOutSqlSessionModifier = new TimeOutSqlSessionModifier();
	}
	
	@Test
	public void sessionTimeout() {
		// time out after 100 seconds
		int timeout = 100;
		timeOutSqlSessionModifier.setTimeout(timeout);
		
		// call (query data not needed)
		timeOutSqlSessionModifier.modifySqlSession(simpleJdbcTemplate, null);
		
		// verify correct session timeout
		verify(simpleJdbcTemplate).update("SET statement_timeout TO " + timeout);
	}
	
	@Test
	public void noTimeout() {
		// 0 indicates no timeout
		timeOutSqlSessionModifier.setTimeout(0);
		
		// call
		timeOutSqlSessionModifier.modifySqlSession(simpleJdbcTemplate, null);
		
		// verify that nothing has happened
		verifyNoMoreInteractions(simpleJdbcTemplate);
	}
	
}
