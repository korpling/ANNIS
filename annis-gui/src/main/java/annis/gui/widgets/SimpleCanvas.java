/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.widgets.gwt.client.VSimpleCanvas;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import java.awt.geom.Line2D;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author thomas
 */
@ClientWidget(VSimpleCanvas.class)
public class SimpleCanvas extends AbstractComponent
{
  private List<Line2D> lines;
  
  public SimpleCanvas()
  {
    lines = new LinkedList<Line2D>();
  }
  
  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
    
    target.startTag("clear");
    target.endTag("clear");
    
    for(Line2D l : lines)
    {
      target.startTag("line");
      target.addAttribute("from_x", l.getX1());
      target.addAttribute("from_y", l.getY1());    
      target.addAttribute("to_x", l.getX2());
      target.addAttribute("to_y", l.getY2());
      target.endTag("line");
    }
  }

  public List<Line2D> getLines()
  {
    return lines;
  }

  public void setLines(List<Line2D> lines)
  {
    this.lines = lines;
  }
  
  
}
