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
package annis.gui.querybuilder;

import annis.gui.widgets.SimpleCanvas;
import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTargetDetails;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thomas
 */
public class TigerQueryBuilder extends Panel implements Button.ClickListener
{

  private SimpleCanvas canvas;
  private List<DragAndDropWrapper> nodes;
  private AbsoluteLayout area;
  private AbsoluteDropHandler handler;
  private int number = 0;

  public TigerQueryBuilder()
  {

    nodes = new ArrayList<DragAndDropWrapper>();

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeUndefined();
    setSizeFull();

    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    Button btAddNode = new Button("Add node", (Button.ClickListener) this);
    btAddNode.setStyleName(ChameleonTheme.BUTTON_SMALL);
    toolbar.addComponent(btAddNode);

    toolbar.setWidth("100%");
    toolbar.setHeight("-1px");

    addComponent(toolbar);

    area = new AbsoluteLayout();
    area.setWidth("2000px");
    area.setHeight("2000px");
    area.addStyleName("no-vertical-drag-hints");
    area.addStyleName("no-horizontal-drag-hints");
    area.addStyleName("no-box-drag-hints");

    canvas = new SimpleCanvas();
    canvas.setWidth("2000px");
    canvas.setHeight("2000px");

    handler = new AbsoluteDropHandler(area, nodes, canvas);

    DragAndDropWrapper areaPane = new DragAndDropWrapper(area);
    areaPane.setDropHandler(handler);

    area.addComponent(canvas, "top:0px;left:0px");
    addComponent(areaPane);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {

    //final int number = nodes.size();
    NodeWindow n = new NodeWindow(number++);
    DragAndDropWrapper wrapper = new DragAndDropWrapper(n);
    nodes.add(wrapper);
    
    
    wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
    wrapper.setWidth("140px");
    wrapper.setHeight("140px");
    area.addComponent(wrapper, "top:" + (40 * (number + 1)) + "px;left:10px");
  }

  private static class AbsoluteDropHandler implements DropHandler
  {

    private AbsoluteLayout layout;
    private List<DragAndDropWrapper> nodes;
    private SimpleCanvas canvas;

    public AbsoluteDropHandler(AbsoluteLayout layout, List<DragAndDropWrapper> nodes,
      SimpleCanvas canvas)
    {
      this.layout = layout;
      this.nodes = nodes;
      this.canvas = canvas;
    }

    @Override
    public void drop(DragAndDropEvent event)
    {
      WrapperTransferable t = (WrapperTransferable) event.getTransferable();
      WrapperTargetDetails details = (WrapperTargetDetails) event.getTargetDetails();
      
      int xChange = details.getMouseEvent().getClientX()
        - t.getMouseDownEvent().getClientX();
      int yChange = details.getMouseEvent().getClientY()
        - t.getMouseDownEvent().getClientY();

      // Move the component in the absolute layout
      ComponentPosition pos =
        layout.getPosition(t.getSourceComponent());
      pos.setLeftValue(pos.getLeftValue() + xChange);
      pos.setTopValue(pos.getTopValue() + yChange);

      // connect each with each
      canvas.getLines().clear();
          
      for(int i = 0; i < nodes.size(); i++)
      {
        DragAndDropWrapper w1 = nodes.get(i);
        ComponentPosition p1 = layout.getPosition(w1);
        for(int j = i + 1; j < nodes.size(); j++)
        {
          DragAndDropWrapper w2 = nodes.get(j);
          ComponentPosition p2 = layout.getPosition(w2);
          canvas.getLines().add(new Line2D.Float(
            p1.getLeftValue() + (w1.getWidth()/2), p1.getTopValue() + (w1.getHeight()/2), 
            p2.getLeftValue() + (w2.getWidth()/2), p2.getTopValue() + (w2.getHeight()/2)));
        }
      }
      canvas.requestRepaint();

    }

    @Override
    public AcceptCriterion getAcceptCriterion()
    {
      return AcceptAll.get();
    }
  }
}
