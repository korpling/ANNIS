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
public class ApAdministrationDao extends DefaultAdministrationDao
{

  private static final Logger log = Logger.getLogger(ApAdministrationDao.class);

  @Override
  public void populateSchema()
  {
    super.populateSchema();

    log.info(
      "creating immutable functions for getting annotations from the annotation pool");
    executeSqlFromScript("functions_annopool_get.sql");
  }

}
