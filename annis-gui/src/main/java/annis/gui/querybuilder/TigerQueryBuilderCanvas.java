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

import annis.gui.QueryController;
import annis.gui.widgets.GripDragComponent;
import annis.gui.widgets.SimpleCanvas;
import annis.libgui.Helper;
import annis.service.objects.AnnisAttribute;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTargetDetails;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class TigerQueryBuilderCanvas extends Panel
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(TigerQueryBuilderCanvas.class);
  private SimpleCanvas canvas;
  private Map<NodeWindow, GripDragComponent> nodes;
  private List<EdgeWindow> edges;
  private AbsoluteLayout area;
  private AbsoluteDropHandler handler;
  private int number = 0;
  private NodeWindow preparedEdgeSource = null;
  private final QueryController controller;

  public TigerQueryBuilderCanvas(QueryController controller)
  {
    this.controller = controller;

    nodes = new HashMap<>();
    edges = new ArrayList<>();

    setSizeFull();
    setImmediate(true);

    area = new AbsoluteLayout();
    area.setWidth("2000px");
    area.setHeight("2000px");
    area.addStyleName("no-vertical-drag-hints");
    area.addStyleName("no-horizontal-drag-hints");
    area.addStyleName("no-box-drag-hints");

    canvas = new SimpleCanvas();
    canvas.setSizeFull();
    canvas.addStyleName("tigerquery-builder-canvas");

    handler = new AbsoluteDropHandler(this, area);

    DragAndDropWrapper areaPane = new DragAndDropWrapper(area);
    areaPane.setWidth("2000px");
    areaPane.setHeight("2000px");
    areaPane.setDropHandler(handler);

    area.addComponent(canvas, "top:0px;left:0px");

    setContent(areaPane);

    addStyleName("no-vertical-drag-hints");
    addStyleName("no-horizontal-drag-hints");
    addStyleName("no-box-drag-hints");
  }

  public void updateQuery()
  {
    controller.getState().getAql().setValue(getAQLQuery());
  }

  public Set<String> getAvailableAnnotationNames()
  {
    Set<String> result = new TreeSet<>();

    WebResource service = Helper.getAnnisWebResource();

    // get current corpus selection
    Set<String> corpusSelection = controller.getState().getSelectedCorpora().getValue();

    if (service != null && corpusSelection != null)
    {
      try
      {
        List<AnnisAttribute> atts = new LinkedList<>();
        
        for(String corpus : corpusSelection)
        {
          atts.addAll(
            service.path("query").path("corpora").path(corpus).path("annotations")
              .queryParam("fetchvalues", "false")
              .queryParam("onlymostfrequentvalues", "true")
              .get(new AnnisAttributeListType())
            );
        }

        for (AnnisAttribute a : atts)
        {
          if (a.getType() == AnnisAttribute.Type.node)
          {
            result.add(a.getName());
          }
        }

      }
      catch(UniformInterfaceException | ClientHandlerException ex)
      {
        log.error(null, ex);
      }
    }
    return result;
  }

  public void updateLinesAndEdgePositions()
  {
    canvas.getLines().clear();

    for (EdgeWindow e : edges)
    {
      GripDragComponent w1 = nodes.get(e.getSource());
      GripDragComponent w2 = nodes.get(e.getTarget());

      ComponentPosition p1 = area.getPosition(w1);
      ComponentPosition p2 = area.getPosition(w2);

      float x1 = p1.getLeftValue() + (w1.getWidth() / 2);
      float y1 = p1.getTopValue() + (w1.getHeight() / 2);
      float x2 = p2.getLeftValue() + (w2.getWidth() / 2);
      float y2 = p2.getTopValue() + (w2.getHeight() / 2);

      float v_x = x2 - x1;
      float v_y = y2 - y1;

      // add line
      canvas.getLines().add(new Line2D.Float(x1, y1, x2, y2));

      // set position on half of the line for the edge window      
      ComponentPosition posEdge = area.getPosition(e);

      float vectorLength = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2
        - y1, 2));
      float xM = x1 + (vectorLength / 2.0f) * ((x2 - x1) / vectorLength);
      float yM = y1 + (vectorLength / 2.0f) * ((y2 - y1) / vectorLength);

      double normV_x = v_x / vectorLength;
      double normV_y = v_y / vectorLength;

      double pos1_x = (2.1 * vectorLength / 3) * normV_x + x1;
      double pos1_y = (2.1 * vectorLength / 3) * normV_y + y1;
      double origDir = Math.atan2(normV_y, normV_x);

      double pos2_x = ((1 * vectorLength) / 3) * normV_x + x1;
      double pos2_y = ((1 * vectorLength) / 3) * normV_y + y1;

      canvas.getLines().addAll(createArrow(pos1_x, pos1_y, origDir, 20.0));
      canvas.getLines().addAll(createArrow(pos2_x, pos2_y, origDir, 20.0));

      posEdge.setLeftValue(xM - e.getWidth() / 2.0f);
      posEdge.setTopValue(yM - e.getHeight() / 2.0f);
    }

    canvas.markAsDirty();
  }

  private List<Line2D> createArrow(double x, double y, double direction,
    double arrowLength)
  {
    LinkedList<Line2D> result = new LinkedList<>();


    double dir1 = direction + Math.PI / 8.0;
    double dir2 = direction - Math.PI / 8.0;

    double end1_x = x - arrowLength * Math.cos(dir1);
    double end1_y = y - arrowLength * Math.sin(dir1);

    double end2_x = x - arrowLength * Math.cos(dir2);
    double end2_y = y - arrowLength * Math.sin(dir2);

    result.add(new Line2D.Double(x, y, end1_x, end1_y));
    result.add(new Line2D.Double(x, y, end2_x, end2_y));

    return result;
  }

  public void prepareAddingEdge(NodeWindow sourceNode)
  {
    preparedEdgeSource = sourceNode;
    for (NodeWindow w : nodes.keySet())
    {
      if (w != sourceNode)
      {
        w.setPrepareEdgeDock(true);
      }
    }
  }

  public void addEdge(NodeWindow target)
  {
    for (NodeWindow w : nodes.keySet())
    {
      w.setPrepareEdgeDock(false);
    }

    if (preparedEdgeSource != target)
    {
      boolean valid = true;
      for (EdgeWindow e : edges)
      {
        if (e.getSource() == preparedEdgeSource && e.getTarget() == target)
        {
          valid = false;
          break;
        }
      }
      if (valid)
      {
        EdgeWindow e = new EdgeWindow(this, preparedEdgeSource, target);
        e.setWidth("170px");
        e.setHeight("70px");
        e.addStyleName("tigerquery-builder-overlay");
        edges.add(e);
        area.addComponent(e);
        updateLinesAndEdgePositions();
        updateQuery();
      }
      else
      {
        Notification.show("There is already such an edge",
          Notification.Type.WARNING_MESSAGE);
      }
    }
  }

  public void deleteEdge(EdgeWindow e)
  {
    area.removeComponent(e);
    edges.remove(e);
    updateLinesAndEdgePositions();
    updateQuery();
  }

  public void addNode()
  {
    final NodeWindow n = new NodeWindow(number++, this);
   
    GripDragComponent panel = new GripDragComponent(n);
    panel.setWidth(NodeWindow.WIDTH, Layout.Unit.PIXELS);
    panel.setHeight(NodeWindow.HEIGHT, Layout.Unit.PIXELS);
    nodes.put(n, panel);
    area.addComponent(panel, "top:" + (10 + ((NodeWindow.HEIGHT+20) * (number-1))) + "px;left:10px");
//    area.addComponent(wrapper, "top:" + (10 + (120 * (number-1))) + "px;left:10px");
    updateQuery();
  }

  public void deleteNode(NodeWindow n)
  {
    LinkedList<EdgeWindow> edgesToRemove = new LinkedList<>();
    for (EdgeWindow e : edges)
    {
      if (e.getSource() == n || e.getTarget() == n)
      {
        edgesToRemove.add(e);
        area.removeComponent(e);
      }
    }

    edges.removeAll(edgesToRemove);

    area.removeComponent(nodes.get(n));
    nodes.remove(n);
    updateLinesAndEdgePositions();
    updateQuery();
  }

  public void clearAll()
  {
    for (EdgeWindow w : edges)
    {
      area.removeComponent(w);
    }
    edges.clear();

    for (GripDragComponent w : nodes.values())
    {
      area.removeComponent(w);
    }
    nodes.clear();
    number = 0;

    updateLinesAndEdgePositions();
    updateQuery();
  }

  private static class AbsoluteDropHandler implements DropHandler
  {

    private AbsoluteLayout layout;
    private TigerQueryBuilderCanvas parent;

    public AbsoluteDropHandler(TigerQueryBuilderCanvas parent,
      AbsoluteLayout layout)
    {
      this.layout = layout;
      this.parent = parent;
    }

    @Override
    public void drop(DragAndDropEvent event)
    {
      GripDragComponent.MouseEventTransferable t = (GripDragComponent.MouseEventTransferable) event.getTransferable();
      WrapperTargetDetails details = (WrapperTargetDetails) event.
        getTargetDetails();

      if (t == null || details == null)
      {
        return;
      }

      int xChange = details.getMouseEvent().getClientX()
        - t.getClientX();
      int yChange = details.getMouseEvent().getClientY()
        - t.getClientY();

      // Move the component in the absolute layout
      ComponentPosition pos =
        layout.getPosition(t.getSourceComponent().getParent());
      pos.setLeftValue(pos.getLeftValue() + xChange);
      pos.setTopValue(pos.getTopValue() + yChange);

      if (parent != null)
      { 
        parent.updateLinesAndEdgePositions();
        parent.requestRepaint();
        
      }

    }

    @Override
    public AcceptCriterion getAcceptCriterion()
    {
      return AcceptAll.get();
    }
  }

  public String getAQLQuery()
  {
    StringBuilder query = new StringBuilder();
    StringBuffer nodeIdentityOperations = new StringBuffer();
    Map<NodeWindow, Integer> nodeComponentMap =
      new HashMap<>();


    //creating node definitions
    int componentCount = 0;
    for (NodeWindow nodeWindow : nodes.keySet())
    {
      List<NodeWindow.ConstraintLayout> constraints =
        nodeWindow.getConstraints();

      if (componentCount++ > 0)
      {
        query.append(" & ");
      }

      if (constraints.size() > 0)
      {
        int nodeComponentCount = 0;
        for (NodeWindow.ConstraintLayout c : constraints)
        {
          if (nodeComponentCount++ > 0)
          {
            nodeIdentityOperations.append("\n& #").append(componentCount).append(
              " _=_ #").append(componentCount + 1);
            query.append(" & ");
            componentCount++;
          }
          String operator = c.getOperator().replace("~", "=");
          String quotes = c.getOperator().equals("=")
            || c.getOperator().equals("!=") ? "\"" : "/";
          String prefix = "";
          if (c.getName().trim().isEmpty() || c.getName().trim().equals("tok"))
          {
            if (operator.equals("!="))
            {
              if(c.getName().trim().isEmpty())
              {
                prefix = "tok" + c.getName() + operator;
              }
              else
              {
                prefix = c.getName() + operator;
              }
            }
          }
          else
          {
            prefix = c.getName() + operator;
          }
          if ("".equals(c.getValue()))
          {
            query.append(c.getName());
          }
          else
          {
            query.append(prefix).append(quotes).append(c.getValue()).append(
              quotes);
          }
        }
      }
      else
      {
        query.append("node");
      }
      nodeComponentMap.put(nodeWindow, componentCount);
    }
    query.append(nodeIdentityOperations);

    //appending node relations
    for (EdgeWindow edgeWindow : edges)
    {
      query.append("\n& ");
      query.append('#').append(nodeComponentMap.get(edgeWindow.getSource())).
        append(" ").append(edgeWindow.getOperator()).append(" ").append("#").
        append(nodeComponentMap.get(edgeWindow.getTarget()));
    }

    return query.toString();
  }

  private static class AnnisAttributeListType extends GenericType<List<AnnisAttribute>>
  {

    public AnnisAttributeListType()
    {
    }
  }
}