/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.administration;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 *
 * @author thomas
 */
public interface AdministrationDao
{
  public void setDataSource(DataSource dataSource);
  
  public void dropDatabase(String database);
  public List<Long> listToplevelCorpora();
  public void deleteCorpora(List<Long> ids);
  public void dropUser(String username);
  public void createUser(String username, String password);
  public void createDatabase(String database);
  
  public void setupDatabase();
  
  public void createSchema();
  public void populateSchema();
  public void createSchemaIndexes();
  
  public void importCorpus(String path);
  
  public List<Map<String, Object>> listCorpusStats();
  public List<Map<String, Object>> listTableStats();
  public List<String> listUsedIndexes();
  public List<String> listUnusedIndexes();
  
  public boolean executeSqlFromScript(String script);
  public boolean executeSqlFromScript(String script, MapSqlParameterSource args);
  
}
