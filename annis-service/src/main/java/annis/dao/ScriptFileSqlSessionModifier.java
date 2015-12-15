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

import annis.administration.AdministrationDao;
import annis.ql.parser.QueryData;
import org.springframework.jdbc.core.JdbcTemplate;

public class ScriptFileSqlSessionModifier implements SqlSessionModifier {

	// this SQL script will be executed before the query runs
	private String scriptFile;
	
	// dependencies
	private AdministrationDao administrationDao;

	// execute SQL from scriptFile
	public void modifySqlSession(JdbcTemplate jdbcTemplate,
			QueryData queryData) {
		administrationDao.executeSqlFromScript(scriptFile);
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public AdministrationDao getAdministrationDao() {
		return administrationDao;
	}

	public void setAdministrationDao(AdministrationDao administrationDao) {
		this.administrationDao = administrationDao;
	}

}
