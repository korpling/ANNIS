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
package annis.gui.visualizers;

import annis.gui.resultview.VisualizerPanel;
import annis.gui.widgets.AutoHeightIFrame;
import com.vaadin.server.ConnectorResource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.rmi.server.UID;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for all iframe visualizer plugins
 *
 * @author Thomas Krause <krause@informatik.hu-berlin.>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public abstract class AbstractIFrameVisualizer extends AbstractVisualizer implements ResourcePlugin
{

  /**
   * Returns the character endocing for this particular Visualizer output. For
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
  public Component createComponent(final VisualizerInput vis)
  { 
    
    StreamResource asResource = new StreamResource(new StreamResource.StreamSource() 
    {
      @Override
      public InputStream getStream()
      {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        writeOutput(vis, outStream);
    
        return new ByteArrayInputStream(outStream.toByteArray());
      }
    }, UUID.randomUUID().toString());
    asResource.setMIMEType(getContentType());
    AutoHeightIFrame iframe = new AutoHeightIFrame(asResource);
    
    return iframe;
  }
}
