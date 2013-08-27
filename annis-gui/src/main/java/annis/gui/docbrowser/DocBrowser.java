/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.docbrowser;

import annis.gui.SearchUI;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;

/**
 * List documents for a specific corpus.
 *
 * @author benjamin
 */
public class DocBrowser extends Table
{

  // holds the vaadin session
  SearchUI ui;

  /**
   * Inits a new doc browser table.
   *
   * @param tabSheet The main tab sheet.
   * @param corpus The name of the corpus the documents.
   */
  public DocBrowser(TabSheet tabSheet, String corpus)
  {
    this.ui = ui;
    String caption = "doc browser " + corpus;
    tabSheet.addTab(this, caption);    
  }

  /**
   * Generates a link to the visualization configured the the corpus config.
   */
  private class DocColumnGenerator implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Label l = new Label((String) itemId);
      return l;
    }
  }  
}
