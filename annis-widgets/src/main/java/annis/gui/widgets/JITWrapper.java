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
package annis.gui.widgets;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@JavaScript({"vaadin://jquery.js", "rst_vis.js"})
public class JITWrapper extends AbstractComponent implements LegacyComponent
{

  private String visData = null;

  private Properties mappings = null;

  public JITWrapper()
  {
    super();
  }

  @Override
  public void paintContent(PaintTarget target)
    throws PaintException
  {
    target.addAttribute("visData", visData);
    
    if (mappings.size() > 0)
    {
      target.addAttribute("mappings", mappings);
    }
  }

  public void setVisData(String visData)
  {
    this.visData = visData;
  }

  public void setProperties(Properties props)
  {
    this.mappings = props;
  }

  @Override
  public void changeVariables(Object source,
    Map<String, Object> variables)
  {
  }
  
  
}