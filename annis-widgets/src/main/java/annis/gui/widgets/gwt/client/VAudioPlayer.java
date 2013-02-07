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
package annis.gui.widgets.gwt.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.UIDL;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VAudioPlayer extends VMediaPlayerBase
{

  private static String CLASSNAME = "v-audioplayer";

  public VAudioPlayer()
  {
    super(Document.get().createAudioElement());
    setStyleName(CLASSNAME);
    
  }

  @Override
  public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
  {
    if (client.updateComponent(this, uidl, true))
    {
      return;
    }
    super.updateFromUIDL(uidl, client);
    Style mediaStyle = getMedia().getStyle();

    // ensure control visibility
    if ((mediaStyle.getHeight() == null || "".equals(mediaStyle.getHeight())))
    {
      if (BrowserInfo.get().isChrome())
      {
        mediaStyle.setHeight(32, Style.Unit.PX);
      }
      else
      {
        mediaStyle.setHeight(25, Style.Unit.PX);
      }
    }
  }
}
