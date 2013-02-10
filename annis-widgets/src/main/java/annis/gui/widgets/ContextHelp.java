/*
 * Copyright 2013 SFB 632.
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
package annis.gui.widgets;

import com.vaadin.client.ComponentConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

/**
 * A wrapper that will provide a context help icon for the extended component.
 * 
 * The help will be the description of the extended component.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ContextHelp extends AbstractExtension
{
  
//  private static final String template = 
//    "<div location=\"component\"></div>"
//      + "<div location=\"help\" style=\"float:right\"></div>"
//    + "<div location=\"help\" style=\"clear:right\"></div>";
//
//  public ContextHelp(final Component wrappedComponent)
//  {    
//    setTemplateContents(template);
//    
//    addComponent(wrappedComponent, "component");
//    Button btHelp = new Button();
//    btHelp.setStyleName(BaseTheme.BUTTON_LINK);
//    btHelp.setIcon(new ThemeResource("../runo/icons/16/help.png"));
//    
//    btHelp.addClickListener(new Button.ClickListener() 
//    {
//      @Override
//      public void buttonClick(ClickEvent event)
//      {
//        String caption = "Help";
//        if(wrappedComponent.getCaption() != null 
//          && !wrappedComponent.getCaption().isEmpty())
//        {
//          caption = "Help for \"" + wrappedComponent.getCaption() + "\"";
//        }
//        Notification notify = new Notification(caption, Notification.Type.HUMANIZED_MESSAGE);
//        notify.setHtmlContentAllowed(true);
//        notify.setDescription(wrappedComponent.getDescription());
//        notify.show(UI.getCurrent().getPage());
//      }
//    });
//    
//    addComponent(btHelp, "help");
//  }

  public void extend(AbstractComponent target)
  {
    super.extend(target);
  }

  
  
  
}
