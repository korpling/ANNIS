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
package annis.gui;

import annis.gui.beans.CitationProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import java.io.Serializable;

/**
 *
 * @author thomas
 */
public class CitationLinkGenerator implements Table.ColumnGenerator,
  Button.ClickListener
{
  @Override
  public Object generateCell(Table source, Object itemId, Object columnId)
  {
    Button btLink = new Button();
    btLink.setStyleName(BaseTheme.BUTTON_LINK);
    btLink.setIcon(FontAwesome.LINK);
    btLink.setDescription("show citation link");
    btLink.addListener(this);

    if(itemId instanceof CitationProvider)
    {
      final CitationProvider citationProvider = (CitationProvider) itemId;
      btLink.addListener(new LinkClickListener(citationProvider));
    }

    return btLink;
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
  }

  private static class LinkClickListener implements Button.ClickListener, Serializable
  {

    private final CitationProvider citationProvider;

    public LinkClickListener(CitationProvider citationProvider)
    {
      this.citationProvider = citationProvider;
    }

    @Override
    public void buttonClick(ClickEvent event)
    {

      if(citationProvider != null)
      {
        CitationWindow c =
          new CitationWindow(
          citationProvider.getQuery(),
          citationProvider.getCorpora(),
          citationProvider.getLeftContext(),
          citationProvider.getRightContext());
        UI.getCurrent().addWindow(c);
        c.center();
      }
      else
      {
        Notification.show("Internal error",
          "No valid citation link was found",
          Notification.Type.WARNING_MESSAGE);
      }
      
    }
  }
}
