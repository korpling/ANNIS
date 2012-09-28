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
import com.google.gwt.user.client.Element;
import com.vaadin.terminal.gwt.client.Util;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class VVideoPlayer extends VMediaPlayerBase
{
  private static String CLASSNAME = "v-audioplayer";
  
  public VVideoPlayer()
  {
    super(Document.get().createVideoElement());
    setStyleName(CLASSNAME);
  }
  
  private void updateSizeFromMetadata(double width, double height)
  {
    getMedia().getStyle().setWidth(width, Style.Unit.PX);
    getMedia().getStyle().setHeight(height, Style.Unit.PX);
    Util.notifyParentOfSizeChange(this, true);
  }
  private native void updateDimensionsWhenMetadataLoaded(Element el)
  /*-{
      var media = $wnd.$(el);
      var self = this;
      
      media.on('loadedmetadata', $entry(function(e) 
      {
        self.@cannis.gui.widgets.gwt.client.VVideoPlayer::updateSizeFromMetadata(II)(el.videoWidth, el.videoHeight);
      }));
  }-*/;
}
