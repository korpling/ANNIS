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

import annis.gui.widgets.gwt.client.VJITWrapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@ClientWidget(VJITWrapper.class)
public class JITWrapper extends AbstractComponent
{

  String testJSON = "{"
    + "\"id\": \"node1\","
    + "\"name\" : \"node1\","
    + "\"data\": {},"
    + "\"children\": [{"
      + "\"id\": \"node2\","
      + "\"name\": \"node2\","
      + "\"data\": {},"
      + "\"children\":[]"
    + "},{"
      + "\"id\": \"node3\","
      + "\"name\": \"node3\","
      + "\"data\": {},"
      + "\"children\":[]"
      + "}"
    + "]"
    + "}";

  public JITWrapper()
  {
    super();
  }

  @Override
  public void paintContent(PaintTarget target)
    throws PaintException
  {
    super.paintContent(target);

    target.addAttribute("testJSON", testJSON);
  }
}