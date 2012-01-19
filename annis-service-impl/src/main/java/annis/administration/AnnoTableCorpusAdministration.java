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

import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author thomas
 */
public class AnnoTableCorpusAdministration extends CorpusAdministration
{
  @Autowired
  private SpringAnnisAdministrationDao administrationDao;
  private static final Logger log = Logger.getLogger(AnnoTableCorpusAdministration.class .getName());

  @Override
  public void initializeDatabase(String host, String port, String database,
    String user, String password, String defaultDatabase, String superUser,
    String superPassword)
  {
    super.initializeDatabase(host, port, database, user, password,
      defaultDatabase, superUser, superPassword);
    
    administrationDao.createFunctionAnnoGetter();
    
  }


  @Override
  public SpringAnnisAdministrationDao getAdministrationDao()
  {
    return administrationDao;
  }

  @Override
  public void setAdministrationDao(
    SpringAnnisAdministrationDao administrationDao)
  {
    this.administrationDao = administrationDao;
  }

  @Override
  public SchemeType getSchemeType()
  {
    return SchemeType.ANNO_TABLE;
  }
  
  
}
