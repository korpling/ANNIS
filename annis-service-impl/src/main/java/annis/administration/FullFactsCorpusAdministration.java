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
package annis.administration;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class FullFactsCorpusAdministration extends CorpusAdministration
{

  private Logger log = Logger.getLogger(this.getClass());
  @Autowired
  private SpringAnnisAdministrationDao administrationDao;
 
  @Override
  public SpringAnnisAdministrationDao getAdministrationDao()
  {
    return this.administrationDao;
  }

  @Override
  public void setAdministrationDao(SpringAnnisAdministrationDao administrationDao)
  {
    this.administrationDao = administrationDao;
  }

  @Override
  public SchemeType getSchemeType()
  {
    return SchemeType.FULLFACTS;
  }
  
  
  
}
