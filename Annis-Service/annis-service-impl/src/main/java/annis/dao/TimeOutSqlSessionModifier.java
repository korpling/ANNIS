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

import org.springframework.jdbc.core.JdbcTemplate;

import annis.ql.parser.QueryData;

public class TimeOutSqlSessionModifier implements SqlSessionModifier {

	private int timeout;
	
	public void modifySqlSession(JdbcTemplate jdbcTemplate, QueryData queryData) {
		if (timeout > 0)
			jdbcTemplate.update("SET statement_timeout TO " + timeout);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
