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
package annis;

import annis.administration.CorpusAdministration;
import annis.sqlgen.dblayout.AbstractDatabaseLayout;
import java.util.List;

/**
 *
 * Providing static creators for Spring beans.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisSpringFactory
{

  private List<AbstractDatabaseLayout> availableLayouts;

  public AbstractDatabaseLayout getDatabaseLayout(String layoutName)
  {

    AbstractDatabaseLayout layout = null;

    if (layoutName != null)
    {
      for (AbstractDatabaseLayout l : availableLayouts)
      {
        if (layoutName.equals(l.getScriptAppendix()))
        {
          layout = l;
          break;
        }
      }
    }

    if (layout == null)
    {
      throw new IllegalArgumentException("invalid table layout \"" + layoutName
        + "\" in configuration");

    }
    else
    {
      return layout;
    }
  }
  
  public CorpusAdministration createCorpusAdministration(String layoutName)
  {

    AbstractDatabaseLayout layout = null;

    if (layoutName != null)
    {
      for (AbstractDatabaseLayout l : availableLayouts)
      {
        if (layoutName.equals(l.getScriptAppendix()))
        {
          layout = l;
          break;
        }
      }
    }

    if (layout == null)
    {
      throw new IllegalArgumentException("invalid table layout \"" + layoutName
        + "\" in configuration");

    }
    else
    {
      CorpusAdministration cadm = layout.createCorpusAdministration();
      cadm.setDbLayout(layout);
      return cadm;
    }
  }

  public List<AbstractDatabaseLayout> getAvailableLayouts()
  {
    return availableLayouts;
  }

  public void setAvailableLayouts(List<AbstractDatabaseLayout> availableLayouts)
  {
    this.availableLayouts = availableLayouts;
  }
  
  
}
