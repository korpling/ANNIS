/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.controlpanel.ControlPanel;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author thomas
 */
@PluginImplementation
public class TigerQueryBuilderPlugin implements QueryBuilderPlugin<TigerQueryBuilderPlugin.TigerQueryBuilder>
{

  @Override
  public String getShortName()
  {
    return "tigersearch";
  }

  @Override
  public String getCaption()
  {
    return "General (TigerSearch alike)";
  }

  @Override
  public TigerQueryBuilder createComponent(ControlPanel controlPanel)
  {
    return new TigerQueryBuilder(controlPanel);
  }
  
  public static class TigerQueryBuilder extends Panel implements Button.ClickListener
  {

    private Button btAddNode;
    private Button btClearAll;
    private TigerQueryBuilderCanvas queryBuilder;

    public TigerQueryBuilder(ControlPanel controlPanel)
    { 
      setStyleName(ChameleonTheme.PANEL_BORDERLESS);
      
      VerticalLayout layout = new VerticalLayout();
      setContent(layout);
      layout.setSizeFull();
      setSizeFull();
      
      layout.setMargin(false);
      
      // TODO: re-enable context help for TigerQueryBuilder (vaadin7)
//      final ContextHelp help = new ContextHelp();
//      layout.addComponent(help);

      HorizontalLayout toolbar = new HorizontalLayout();
      //toolbar.addStyleName("toolbar");

      btAddNode = new Button("Add node", (Button.ClickListener) this);
      btAddNode.setStyleName(ChameleonTheme.BUTTON_SMALL);
      btAddNode.setDescription("<strong>Create Node</strong><br />"
        + "Click here to add a new node specification window.<br />"
        + "To move the node, click and hold left mouse button, then move the mouse.");
      toolbar.addComponent(btAddNode);

      btClearAll = new Button("Clear all", (Button.ClickListener) this);
      btClearAll.setStyleName(ChameleonTheme.BUTTON_SMALL);
      btClearAll.setDescription("<strong>Clear all</strong><br />"
        + "Click here to delete all node specification windows and reset the query builder.");
      toolbar.addComponent(btClearAll);

      final Button btHelp = new Button();
      btHelp.setStyleName(ChameleonTheme.BUTTON_LINK);
      btHelp.setIcon(new ThemeResource("../runo/icons/16/help.png"));
      btHelp.addListener(new Button.ClickListener() 
      {
        @Override
        public void buttonClick(ClickEvent event)
        {
//          help.showHelpFor(btHelp);
        }
      });
      toolbar.addComponent(btHelp);

      toolbar.setWidth("-1px");
      toolbar.setHeight("-1px");

      layout.addComponent(toolbar);

      queryBuilder = new TigerQueryBuilderCanvas(controlPanel);
//      help.addHelpForComponent(btHelp,
//        "Click “Add node” to add a search term. "
//        + "You can move nodes freely by dragging\n"
//        + "them for your convenience. Click “add” to insert some annotation criteria for the\n"
//        + "search term. The field on the left of the node annotation will show annotation\n"
//        + "names from the selected corpora. The operator in the middle can be set to equals\n"
//        + "‘=’, does not equal ‘!=’ and similarly for pattern searches to ‘~’ (regular\n"
//        + "expression match) and ‘!~’ (does not equal regular expression). The field on the\n"
//        + "right gives annotation values or regular expressions.<br />"
//        + "Adding multiple nodes makes it possible to use the ‘Edge’ button. Click on ‘Edge’\n"
//        + "in one node and then on ‘Dock’ in another to connect search terms. Choose an\n"
//        + "operator from the list on the line connecting the edges to determine e.g. if one\n"
//        + "node should occur before the other, etc. For details on the meaning and usage of\n"
//        + "each operator, see the tutorial tab above.",
//        Placement.BELOW);
      layout.addComponent(queryBuilder);

      layout.setExpandRatio(queryBuilder, 1.0f);
    }

    @Override
    public void buttonClick(ClickEvent event)
    {

      if(event.getButton() == btAddNode)
      {
        queryBuilder.addNode();
      }
      else if(event.getButton() == btClearAll)
      {
        queryBuilder.clearAll();
      }

    }
  }
}
