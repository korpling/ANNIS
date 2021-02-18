/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
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
package org.corpus_tools.annis.gui.visualizers.iframe;

import com.vaadin.ui.Component;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.corpus_tools.annis.gui.VisualizationToggle;
import org.corpus_tools.annis.gui.visualizers.AbstractVisualizer;
import org.corpus_tools.annis.gui.visualizers.IFrameResource;
import org.corpus_tools.annis.gui.visualizers.IFrameResourceMap;
import org.corpus_tools.annis.gui.visualizers.ResourcePlugin;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.widgets.AutoHeightIFrame;

/**
 * Base class for all iframe visualizer plugins
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
public abstract class AbstractIFrameVisualizer extends AbstractVisualizer
    implements ResourcePlugin {

  /**
   * 
   */
  private static final long serialVersionUID = -1795397545146765258L;
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(AbstractIFrameVisualizer.class);

  @Override
  public Component createComponent(final VisualizerInput vis, VisualizationToggle visToggle) {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    writeOutput(vis, outStream);

    IFrameResource res = new IFrameResource();
    res.setData(outStream.toByteArray());
    res.setMimeType(getContentType());

    UUID uuid = UUID.randomUUID();
    vis.getUI().access(
        () -> vis.getUI().getSession().getAttribute(IFrameResourceMap.class).put(uuid, res));

    URI base;
    try {
      String ctx = vis.getUI().getServletContext().getContextPath();
      if (!ctx.endsWith("/")) {
        ctx = ctx + "/";
      }
      base = new URI(ctx);

    } catch (URISyntaxException e) {
      log.warn(
          "Getting context failed, falling back to using the complete URL, which will fail if ANNIS used with an instance URL",
          e);
      base = vis.getUI().getPage().getLocation();
    }
    AutoHeightIFrame iframe =
        new AutoHeightIFrame(base.resolve("vis-iframe-res/" + uuid.toString()));
    return iframe;
  }

  /**
   * Returns the character encoding for this particular Visualizer output. For more information see
   * {@link javax.servlet.ServletResponse#setCharacterEncoding(String)}. Must be overridden to
   * change default "utf-8".
   *
   * @return the CharacterEncoding
   */
  public String getCharacterEncoding() {
    return "utf-8";
  }

  public String getContentType() {
    return "text/html";
  }

  /**
   * Writes the final output to passed OutputStream. The stream should remain open.
   *
   * @param input The input from which the visualization should be generated from
   * @param outstream The OutputStream to be used
   */
  public abstract void writeOutput(VisualizerInput input, OutputStream outstream);

}
