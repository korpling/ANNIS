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
