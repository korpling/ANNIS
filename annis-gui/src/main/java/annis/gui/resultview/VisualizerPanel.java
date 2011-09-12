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
package annis.gui.resultview;

import annis.gui.widgets.AutoHeightIFrame;
import annis.resolver.ResolverEntry;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author thomas
 *
 */
public class VisualizerPanel extends Panel
{

  public static final ThemeResource ICON_COLLAPSE = new ThemeResource("icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource("icon-expand.gif");

  private AutoHeightIFrame iframe;

  public VisualizerPanel(final ResolverEntry entry)
  {

    this.setSizeFull();
    
    ((VerticalLayout) getContent()).setMargin(false);
    ((VerticalLayout) getContent()).setSpacing(false);

    final Button btEntry = new Button(entry.getDisplayName());
    btEntry.setIcon(ICON_EXPAND);
    btEntry.setStyleName(BaseTheme.BUTTON_LINK);
    btEntry.addListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        if(btEntry.getIcon() == ICON_EXPAND)
        {
          // expand
          if(iframe == null)
          {
            // TODO: calculate real url
            iframe = new AutoHeightIFrame("/index.html");
            addComponent(iframe);
          }
          
          btEntry.setIcon(ICON_COLLAPSE);
          iframe.setVisible(true);
        }
        else if(btEntry.getIcon() == ICON_COLLAPSE)
        {
          // collapse
          if(iframe != null)
          {
            iframe.setVisible(false);
          }
          btEntry.setIcon(ICON_EXPAND);
        }
      }
    });
    addComponent(btEntry);

  }

}
