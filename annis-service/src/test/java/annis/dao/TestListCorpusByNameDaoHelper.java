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

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestListCorpusByNameDaoHelper {

	@Test
	public void createSql() {
		final ListCorpusByNameDaoHelper listCorpusByNameDaoHelper = new ListCorpusByNameDaoHelper();
		
		// a few dummy corpus names
		List<String> corpusNames = Arrays.asList("a", "b", "c");
		
		String expected = "SELECT tmp.id FROM\n"
      + "(\n"
      + "SELECT id, 0::int AS sourceIdx FROM corpus WHERE name='a' AND top_level IS TRUE\n"
      + "UNION\n"
      + "SELECT id, 1::int AS sourceIdx FROM corpus WHERE name='b' AND top_level IS TRUE\n"
      + "UNION\n"
      + "SELECT id, 2::int AS sourceIdx FROM corpus WHERE name='c' AND top_level IS TRUE"
      + ") AS tmp\n"
      + "ORDER BY tmp.sourceIdx";
    
		assertEquals(expected, listCorpusByNameDaoHelper.createSql(corpusNames));
	}
}
