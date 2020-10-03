/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.widgets;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;
import java.util.Map;

/**
 *
 * @author Benjamin Weißenfels {@literal <b.pixeldrama@gmail.com>}
 */
@JavaScript({"vaadin://jquery.js", "rst_vis.js"})
public class JITWrapper extends AbstractComponent implements LegacyComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -2869486466171899170L;

  private String visData = null;

  private Map<String, String> mappings = null;

  public JITWrapper() {
    super();
  }

  @Override
  public void changeVariables(Object source, Map<String, Object> variables) {}

  @Override
  public void paintContent(PaintTarget target) throws PaintException {
    target.addAttribute("visData", visData);

    if (mappings.size() > 0) {
      target.addAttribute("mappings", mappings);
    }
  }

  public void setProperties(Map<String, String> props) {
    this.mappings = props;
  }

  public void setVisData(String visData) {
    this.visData = visData;
  }


}
