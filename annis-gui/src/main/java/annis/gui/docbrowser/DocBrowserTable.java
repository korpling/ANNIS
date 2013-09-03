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

import annis.model.Annotation;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List documents for a specific corpus.
 *
 * @author benjamin
 */
public class DocBrowserTable extends Table
{

  private Logger log = LoggerFactory.getLogger(DocBrowserTable.class);

  private BeanItemContainer<Annotation> annoBean;

  private DocBrowserPanel parent;

  private static final ThemeResource EYE_ICON = new ThemeResource("eye.png");

  void setDocNames(List<Annotation> docs)
  {
    annoBean = new BeanItemContainer<Annotation>(docs);
    annoBean.addAll(docs);
    setContainerDataSource(annoBean);
    addGeneratedColumn("document name", new DocNameColumnGen());
    addGeneratedColumn("open visualizer", new DocViewColumn());
    setVisibleColumns(new Object[]
    {
      "document name", "open visualizer"
    });
    
    setColumnHeaders("document name", "");
    setColumnWidth("open visualizer", 16);
  }

  private DocBrowserTable(DocBrowserPanel parent)
  {

    this.parent = parent;

    // configure layout
    setSizeFull();
  }

  /**
   * Generates a link to the visualization configured the the corpus config.
   */
  private class DocNameColumnGen implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      Annotation a = (Annotation) itemId;
      Label l = new Label((String) a.getName());
      return l;
    }
  }

  private class DocViewColumn implements Table.ColumnGenerator
  {

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId)
    {
      String docName = ((Annotation) itemId).getName();
      Button openVis = new Button();
      openVis.setStyleName(BaseTheme.BUTTON_LINK);
      openVis.setIcon(EYE_ICON);
      openVis.setDescription("open visualizer with the full text of " + docName);
      openVis.addClickListener(new OpenVisualizerWindow(docName));
      return openVis;
    }
  }

  public static DocBrowserTable getDocBrowserTable(DocBrowserPanel parent)
  {
    DocBrowserTable docBrowserTable = new DocBrowserTable(parent);
    return docBrowserTable;
  }

  private class OpenVisualizerWindow implements Button.ClickListener
  {

    private String docName;

    public OpenVisualizerWindow(String docName)
    {
      this.docName = docName;
    }

    @Override
    public void buttonClick(Button.ClickEvent event)
    {

      parent.openVis(docName);
    }
  }
}
