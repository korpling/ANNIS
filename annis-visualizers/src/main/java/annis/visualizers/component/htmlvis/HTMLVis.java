/*
 * Copyright 2013 SFB 632.
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
package annis.visualizers.component.htmlvis;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import java.io.FileInputStream;
import java.io.IOException;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@PluginImplementation
public class HTMLVis extends AbstractVisualizer<Label>
{
  private static final Logger log = LoggerFactory.getLogger(HTMLVis.class);

  @Override
  public String getShortName()
  {
    return "htmlvis";
  }

  @Override
  public Label createComponent(VisualizerInput vi, VisualizationToggle vt)
  {
    Label lblResult = new Label("NOT IMPLEMENTED YET", ContentMode.HTML);
    try
    {
      Parser p = new Parser(new FileInputStream("vistest.config"));
    }
    catch (IOException ex)
    {
      log.error("Could not parse the HTML visualization configuration file", ex);
    }
    
    return lblResult;
  }
  
}
