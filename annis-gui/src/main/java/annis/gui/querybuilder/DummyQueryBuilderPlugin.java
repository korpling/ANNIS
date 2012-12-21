/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.querybuilder;

import annis.gui.SearchWindow;
import annis.gui.controlpanel.ControlPanel;
import annis.gui.querybuilder.DummyQueryBuilderPlugin.DummyQueryBuilder;
import com.vaadin.ui.Panel;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class DummyQueryBuilderPlugin implements QueryBuilderPlugin<DummyQueryBuilder>
{

  @Override
  public String getShortName()
  {
    return "dummy";
  }

  @Override
  public String getCaption()
  {
    return "Non-Sense query builder";
  }
  
  

  @Override
  public DummyQueryBuilder createComponent(ControlPanel controlPanel)
  {
    return new DummyQueryBuilder(controlPanel);
  }
  
  public static class DummyQueryBuilder extends Panel
  {
    public DummyQueryBuilder(ControlPanel controlPanel)
    {
      controlPanel.setQuery("An apple a day keeps the doctor away.", null);
    }
  }
  
}
