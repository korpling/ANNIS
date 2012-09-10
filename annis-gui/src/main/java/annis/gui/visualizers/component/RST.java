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
package annis.gui.visualizers.component;

import annis.gui.resultview.VisualizerPanel;
import annis.gui.visualizers.AbstractVisualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.widgets.JITWrapper;
import com.vaadin.ui.Component;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class RST extends AbstractVisualizer<RST.RSTImpl>
{

  @Override
  public String getShortName()
  {
    return "rst";
  }

  @Override
  public RSTImpl createComponent(VisualizerInput visInput)
  {
    return new RSTImpl(visInput);
  }

  public class RSTImpl extends JITWrapper
  {

    private final Logger log = LoggerFactory.getLogger(RSTImpl.class);

    private RSTImpl(VisualizerInput visInput)
    {
      log.info("initialized rest");
    }
  }
}
