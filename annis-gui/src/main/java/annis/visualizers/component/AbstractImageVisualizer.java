/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.visualizers.component;

import annis.libgui.ImagePanel;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Embedded;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public abstract class AbstractImageVisualizer extends AbstractVisualizer<ImagePanel> {
  /**
   * 
   */
  private static final long serialVersionUID = -3055099196694903386L;

  @Override
  public ImagePanel createComponent(final VisualizerInput visInput, VisualizationToggle visToggle) {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeOutput(visInput, out);

    String fileName = "vis_" + UUID.randomUUID().toString() + ".png";
    StreamResource resource =
        new StreamResource(() -> new ByteArrayInputStream(out.toByteArray()), fileName);

    Embedded emb = new Embedded("", resource);
    emb.setMimeType(getContentType());
    emb.setSizeUndefined();
    emb.setStandby("loading image");
    emb.setAlternateText("Visualization of the result");

    return new ImagePanel(emb);
  }

  public abstract String getContentType();

  public abstract void writeOutput(VisualizerInput input, OutputStream outstream);


}
