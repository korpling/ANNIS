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
package annis.gui.visualizers.component;

import annis.gui.ImagePanel;
import annis.gui.VisualizationToggle;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Embedded;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public abstract class AbstractImageVisualizer extends AbstractVisualizer<ImagePanel>
{
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractImageVisualizer.class);

  @Override
  public ImagePanel createComponent(final VisualizerInput visInput, VisualizationToggle visToggle)
  {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeOutput(visInput, out);

    String fileName = "vis_" + UUID.randomUUID().toString() + ".png";
    StreamResource resource = new StreamResource(new StreamResource.StreamSource()
      {
        @Override
        public InputStream getStream()
        {
          return new ByteArrayInputStream(out.toByteArray());
        }
      }, fileName);

    Embedded emb = new Embedded("", resource);
    emb.setMimeType(getContentType());
    emb.setSizeFull();
    emb.setStandby("loading image");
    emb.setAlternateText("Visualization of the result");
    
    return new ImagePanel(emb);
  }
  
  public abstract void writeOutput(VisualizerInput input, OutputStream outstream);
  public abstract String getContentType();
 
  
}
