/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.libgui.visualizers;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import annis.gui.widgets.AutoHeightIFrame;
import annis.libgui.VisualizationToggle;

/**
 * Base class for all iframe visualizer plugins
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public abstract class AbstractIFrameVisualizer extends AbstractVisualizer implements ResourcePlugin
{
  /**
   * Returns the character encoding for this particular Visualizer output. For
   * more information see
   * {@link javax.servlet.ServletResponse#setCharacterEncoding(String)}. Must be
   * overridden to change default "utf-8".
   *
   * @return the CharacterEncoding
   */
  public String getCharacterEncoding()
  {
    return "utf-8";
  }

  public String getContentType()
  {
    return "text/html";
  }

  /**
   * Writes the final output to passed OutputStream. The stream should remain
   * open.
   *
   * @param input The input from which the visualization should be generated
   * from
   * @param outstream The OutputStream to be used
   */
  public abstract void writeOutput(VisualizerInput input, OutputStream outstream);

  @Override
  public Component createComponent(final VisualizerInput vis, VisualizationToggle visToggle)
  { 
    
    VaadinSession session = VaadinSession.getCurrent();
    if(session.getAttribute(IFrameResourceMap.class) == null)
    {
      session.setAttribute(IFrameResourceMap.class, new IFrameResourceMap());
    }
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    writeOutput(vis, outStream);
    
    IFrameResource res  = new IFrameResource();
    res.setData(outStream.toByteArray());
    res.setMimeType(getContentType());
    
    UUID uuid = UUID.randomUUID();
    session.getAttribute(IFrameResourceMap.class).put(uuid, res);
  
    URI base = UI.getCurrent().getPage().getLocation();
    AutoHeightIFrame iframe = 
      new AutoHeightIFrame(base.resolve("vis-iframe-res/" + uuid.toString()));
    return iframe;
  }
}
