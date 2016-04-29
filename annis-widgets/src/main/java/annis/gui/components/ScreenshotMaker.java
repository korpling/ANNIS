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
package annis.gui.components;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.Validate;
import org.json.JSONException;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;

import elemental.json.JsonArray;

/**
 * A component that can make pure JavaScript based "screenshots".
 * 
 * Uses the http://html2canvas.hertzen.com/ library.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript({"vaadin://jquery.js", "html2canvas.js", "screenshotmaker.js"})
public class ScreenshotMaker extends AbstractJavaScriptExtension
{ 
  public ScreenshotMaker(final ScreenshotCallback callback)
  {
    Validate.notNull(callback);
    
    addFunction("finishedScreenshot", new JavaScriptFunction() 
    {
      @Override
      public void call(JsonArray arguments) throws JSONException
      {
        parseAndCallback(arguments.getString(0), callback);
      }
    });
  }

  @Override
  protected Class<? extends ClientConnector> getSupportedParentType()
  {
    return UI.class;
  }
  
  /**
   * Takes a raw string representing the result of the toDataURL() function
   * of the HTML5 canvas and calls the callback with a proper mime type 
   * and the bytes of the image.
   * 
   * @see  <a href="http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-canvas-todataurl">http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-canvas-todataurl</
   * @param rawImage The encoded image
   * @param callback The callback to call with the result.
   * @return 
   */
  private void parseAndCallback(String rawImage, ScreenshotCallback callback)
  {
    if(callback == null)
    {
      return;
    }
    
    // find the mime type
    final String[] typeInfoAndData = rawImage.split(",");
    String[] mimeAndEncoding = typeInfoAndData[0].replaceFirst("data:", "").split(";");
    if(typeInfoAndData.length == 2 &&
      mimeAndEncoding.length == 2 
      && "base64".equalsIgnoreCase(mimeAndEncoding[1]))
    {
      byte[] result = Base64.decodeBase64(typeInfoAndData[1]);
      callback.screenshotReceived(result, mimeAndEncoding[0]);
    }

  }
  
  public void makeScreenshot()
  {
    callFunction("makeScreenshot");
  }
  
  public static interface ScreenshotCallback
  {
    /**
     * Called whenever new data is available.
     * 
     * @param imageData The image as raw PNG data.
     */
    public void screenshotReceived(byte[] imageData, String mimeType);
  }
}
