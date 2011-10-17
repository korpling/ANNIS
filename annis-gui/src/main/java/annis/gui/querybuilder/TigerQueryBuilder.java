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
import com.vaadin.ui.themes.ChameleonTheme;
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
import com.vaadin.ui.Window.Notification;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thomas
 */
public class TigerQueryBuilder extends Panel implements Button.ClickListener
{

  private SimpleCanvas canvas;
  private Map<NodeWindow,DragAndDropWrapper> nodes;
  private List<EdgeWindow> edges;
  private AbsoluteLayout area;
  private AbsoluteDropHandler handler;
  private int number = 0;
  private Button btAddNode;
  private NodeWindow preparedEdgeSource = null;

  public TigerQueryBuilder()
  {

    nodes = new HashMap<NodeWindow, DragAndDropWrapper>();
    edges = new ArrayList<EdgeWindow>();

    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setSizeUndefined();
    setSizeFull();

    HorizontalLayout toolbar = new HorizontalLayout();
    toolbar.addStyleName("toolbar");
    btAddNode = new Button("Add node", (Button.ClickListener) this);
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

    handler = new AbsoluteDropHandler(this, area);

    DragAndDropWrapper areaPane = new DragAndDropWrapper(area);
    areaPane.setDropHandler(handler);

    area.addComponent(canvas, "top:0px;left:0px");
    addComponent(areaPane);
    
    addStyleName("no-vertical-drag-hints");
    addStyleName("no-horizontal-drag-hints");
    addStyleName("no-box-drag-hints");
  }

  public void updateLinesAndEdgePositions()
  {
    canvas.getLines().clear();

    for(EdgeWindow e : edges)
    {
      DragAndDropWrapper w1 = nodes.get(e.getSource());
      DragAndDropWrapper w2 = nodes.get(e.getTarget());

      ComponentPosition p1 = area.getPosition(w1);
      ComponentPosition p2 = area.getPosition(w2);

      float x1 = p1.getLeftValue() + (w1.getWidth() / 2);
      float y1 = p1.getTopValue() + (w1.getHeight() / 2);
      float x2 = p2.getLeftValue() + (w2.getWidth() / 2);
      float y2 = p2.getTopValue() + (w2.getHeight() / 2);
      
      // add line
      canvas.getLines().add(new Line2D.Float(x1, y1, x2, y2));

      // set position on half of the line for the edge window      
      ComponentPosition posEdge = area.getPosition(e);
      
      float vectorLength = (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
      float xM = x1 + (vectorLength/2.0f)*((x2-x1)/vectorLength);
      float yM = y1 + (vectorLength/2.0f)*((y2-y1) / vectorLength);
      
      posEdge.setLeftValue(xM-e.getWidth()/2.0f);
      posEdge.setTopValue(yM-e.getHeight()/2.0f);
    }

    canvas.requestRepaint();
  }

  @Override
  public void buttonClick(ClickEvent event)
  {

    if(event.getButton() == btAddNode)
    {
      addNode();
    }

  }

  public void prepareAddingEdge(NodeWindow sourceNode)
  {
    preparedEdgeSource = sourceNode;
    for(NodeWindow w : nodes.keySet())
    {
      if(w != sourceNode)
      {
        w.setPrepareEdgeDock(true);
      }
    }
  }

  public void addEdge(NodeWindow target)
  {
    for(NodeWindow w : nodes.keySet())
    {
      w.setPrepareEdgeDock(false);
    }

    if(preparedEdgeSource != target)
    {      
      boolean valid = true;
      for(EdgeWindow e : edges)
      {
        if(e.getSource() == preparedEdgeSource && e.getTarget() == target)
        {
          valid=false;
          break;
        }
      }
      if(valid)
      {
        EdgeWindow e = new EdgeWindow(this, preparedEdgeSource, target);
        e.setWidth("70px");
        e.setHeight("70px");
        edges.add(e);
        area.addComponent(e);
        updateLinesAndEdgePositions();
      }
      else
      {
        getWindow().showNotification("There is already such an edge", 
          Notification.TYPE_WARNING_MESSAGE);
      }
    }
  }
  
  public void deleteEdge(EdgeWindow e)
  {
    area.removeComponent(e);
    edges.remove(e);
    updateLinesAndEdgePositions();
  }

  public void addNode()
  {
    NodeWindow n = new NodeWindow(number++, this);
    DragAndDropWrapper wrapper = new DragAndDropWrapper(n);
    nodes.put(n, wrapper);


    wrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
    wrapper.setWidth("140px");
    wrapper.setHeight("140px");
    area.addComponent(wrapper, "top:" + (40 * (number + 1)) + "px;left:10px");

  }
  
  public void deleteNode(NodeWindow n)
  {
    LinkedList<EdgeWindow> edgesToRemove = new LinkedList<EdgeWindow>();
    for(EdgeWindow e : edges)
    {
      if(e.getSource() == n || e.getTarget() == n)
      {
        edgesToRemove.add(e);
        area.removeComponent(e);
      }
    }
    
    edges.removeAll(edgesToRemove);
    
    area.removeComponent(nodes.get(n));
    nodes.remove(n);
    updateLinesAndEdgePositions();
  }

  private static class AbsoluteDropHandler implements DropHandler
  {

    private AbsoluteLayout layout;
    private TigerQueryBuilder parent;

    public AbsoluteDropHandler(TigerQueryBuilder parent, AbsoluteLayout layout)
    {
      this.layout = layout;
      this.parent = parent;
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

      if(parent != null)
      {
        parent.updateLinesAndEdgePositions();
      }

    }

    @Override
    public AcceptCriterion getAcceptCriterion()
    {
      return AcceptAll.get();
    }
  }
}
