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
package annis.gui.components.screenshot;

import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.Notification;
import org.json.JSONArray;
import org.json.JSONException;


import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * A component that can make pure JavaScript based "screenshots".
 * 
 * Uses the http://html2canvas.hertzen.com/ library.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript({"jquery-1.9.1.min.js", "html2canvas.js", "screenshotmaker.js"})
public class ScreenshotMaker extends AbstractJavaScriptComponent
{
  public ScreenshotMaker()
  {
    addFunction("finishedScreenshot", new JavaScriptFunction() 
    {
      @Override
      public void call(JSONArray arguments) throws JSONException
      {
        Notification.show("!!!Screenshot finished!!!");
      }
    });
  }
  
  public void makeScreenshot(ScreenshotCallback callback)
  {
    callFunction("makeScreenshot");
  }
  
  public static class ScreenshotCallback
  {
    
  }
}
