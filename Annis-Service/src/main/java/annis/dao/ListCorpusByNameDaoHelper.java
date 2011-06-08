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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;

public class ListCorpusByNameDaoHelper extends ParameterizedSingleColumnRowMapper<Long> {

	public String createSql(List<String> corpusNames) {
		Validate.notEmpty(corpusNames, "Need at least one corpus name");
		
		// turn corpus names into sql strings (enclosed with ')
		List<String> corpusNamesSqlStrings = new ArrayList<String>();
		for (String corpus : corpusNames) {
			corpusNamesSqlStrings.add("'" + corpus + "'");
		}
		
		// build sql query
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT id FROM corpus WHERE name IN ( ");
		sb.append(StringUtils.join(corpusNamesSqlStrings, ", "));
		sb.append(" ) AND top_level = 't'");
		return sb.toString();
	}
	
}
