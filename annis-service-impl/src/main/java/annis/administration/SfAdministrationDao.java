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

import org.apache.log4j.Logger;

/**
 *
 * @author thomas
 */
public class SfAdministrationDao extends DefaultAdministrationDao
{

  private static final Logger log = Logger.getLogger(ApAdministrationDao.class);
  
  @Override
  void analyzeFacts(long corpusID)
  {
    log.info("analyzing facts_node table for corpus with ID " + corpusID);
    getJdbcTemplate().getJdbcOperations().execute("ANALYZE facts_node_" + corpusID);
    
    log.info("analyzing facts_edge table for corpus with ID " + corpusID);
    getJdbcTemplate().getJdbcOperations().execute("ANALYZE facts_edge_" + corpusID);
  }
  
}
