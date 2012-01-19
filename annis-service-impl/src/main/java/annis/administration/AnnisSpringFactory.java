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

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * Providing static creators for Spring beans.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisSpringFactory
{
  private String tableLayout;
  public CorpusAdministration createCorpusAdministration()
  {
    try
    {
       SchemeType type = SchemeType.valueOf(tableLayout.toUpperCase());
       return type.getAdminClazz().newInstance();
    }
    catch (InstantiationException ex)
    {
      Logger.getLogger(AnnisSpringFactory.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    catch (IllegalAccessException ex)
    {
      Logger.getLogger(AnnisSpringFactory.class.getName()).
        log(Level.SEVERE, null, ex);
    }
    catch(IllegalArgumentException ex)
    {
      Logger.getLogger(AnnisSpringFactory.class.getName()).log(Level.SEVERE,
        "invalid table layout \"" + tableLayout + "\" in configuration",ex);
    }
    return new FullFactsCorpusAdministration();
  }

  public String getTableLayout()
  {
    return tableLayout;
  }

  public void setTableLayout(String tableLayout)
  {
    this.tableLayout = tableLayout;
  }
  
  
  
}
