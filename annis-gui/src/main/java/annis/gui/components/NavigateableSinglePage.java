/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.components;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Embedds a single HTML page and adds navigation to it's headers (if they have
 * an id).
 *
 * This is e.g. usefull for documentation.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class NavigateableSinglePage extends VerticalLayout
{

  private final IFrameComponent iframe = new IFrameComponent();
  private final Label lblHeaderID = new Label();
  
  public NavigateableSinglePage()
  {
    lblHeaderID.setCaption("Selected header ID: ");
    
    lblHeaderID.setWidth("100%");
    lblHeaderID.setHeight("-1px");
    iframe.setSizeFull();
    
    addComponent(lblHeaderID);
    addComponent(iframe);
    
    setExpandRatio(iframe, 1.0f);
  }

  private void onScroll(String headerID)
  {
    lblHeaderID.setValue(headerID);
  }

  public void setSource(String source)
  {
    iframe.getState().setSource(source);
  }

  @JavaScript(
  {
    "vaadin://jquery.js", "navigateablesinglepage.js"
  })
  private class IFrameComponent extends AbstractJavaScriptComponent
  {

    public IFrameComponent()
    {
      addFunction("scrolled", new JavaScriptFunction()
      {

        @Override
        public void call(JSONArray arguments) throws JSONException
        {
          onScroll(arguments.getString(0));
        }
      });
    }

    @Override
    public final IframeState getState()
    {
      return (IframeState) super.getState();
    }
  }
}
