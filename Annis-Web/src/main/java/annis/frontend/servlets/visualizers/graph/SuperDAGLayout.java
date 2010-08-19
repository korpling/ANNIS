/*
 *  Copyright 2009 thomas.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package annis.frontend.servlets.visualizers.graph;

import annis.model.AnnisNode;
import annis.model.Edge;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class SuperDAGLayout extends AbstractLayout<AnnisNode, Edge>
{

  protected transient Point m_currentPoint = new Point();
  protected Map<AnnisNode, Integer> basePositions = new HashMap<AnnisNode, Integer>();
  protected transient Set<AnnisNode> alreadyDone = new HashSet<AnnisNode>();
  /**
   * The default horizontal vertex spacing.  Initialized to 50.
   */
  public final static int DEFAULT_DISTX = 50;
  /**
   * The default vertical vertex spacing.  Initialized to 50.
   */
  public final static int DEFAULT_DISTY = 50;
  /**
   * The horizontal vertex spacing.  Defaults to {@code DEFAULT_XDIST}.
   */
  protected int distX = DEFAULT_DISTX;
  /**
   * The vertical vertex spacing.  Defaults to {@code DEFAULT_YDIST}.
   */
  protected int distY = DEFAULT_DISTY;

  public SuperDAGLayout(Graph<AnnisNode, Edge> graph)
  {
    super(graph);
  }

  @Override
  public void initialize()
  {
    if(size == null)
    {
       size = new Dimension(600,600);
    }
    buildTree();
  }

  @Override
  public void reset()
  {
    // TODO
  }

  private void buildTree()
  {
    this.m_currentPoint = new Point(0, 20);
    LinkedList<AnnisNode> roots = getRoots(graph);
    if(roots.size() > 0 && graph != null)
    {
      calculateDimensionX(roots);
      for(AnnisNode v : roots)
      {
        calculateDimensionX(v);
        m_currentPoint.x += this.basePositions.get(v) / 2 + 50;
        buildTree(v, this.m_currentPoint.x);
      }
    }
    
    // pull all token down to height of lowest token
    double lowestY = 0.0;
    for(AnnisNode n : graph.getVertices())
    {
      if(n.isToken())
      {
        lowestY = Math.max(lowestY, locations.get(n).getY());
      }
    }
    for(AnnisNode n : graph.getVertices())
    {
      if(n.isToken())
      {
        Point2D p = locations.get(n);
        p.setLocation(p.getX(), lowestY);
      }
    }

  }

  protected void buildTree(AnnisNode v, int x)
  {

    if(!alreadyDone.contains(v))
    {
      alreadyDone.add(v);

      //go one level further down
      this.m_currentPoint.y += this.distY;
      this.m_currentPoint.x = x;

      this.setCurrentPositionFor(v);

      int sizeXofCurrent = basePositions.get(v);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for(AnnisNode element : graph.getSuccessors(v))
      {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(element, startXofChild);
        lastX = lastX + sizeXofChild + distX;
      }
      this.m_currentPoint.y -= this.distY;
    }
  }

  /** Get all nodes without a incoming edge */
  private LinkedList<AnnisNode> getRoots(Graph<AnnisNode, Edge> graph)
  {
    LinkedList<AnnisNode> result = new LinkedList<AnnisNode>();

    if(graph != null)
    {
      for(AnnisNode n : graph.getVertices())
      {
        if(graph.getInEdges(n).isEmpty())
        {
          result.add(n);
        }
      }
    }
    return result;
  }

  private int calculateDimensionX(AnnisNode v)
  {

    int size = 0;
    int childrenNum = graph.getSuccessors(v).size();

    if(childrenNum != 0)
    {
      for(AnnisNode element : graph.getSuccessors(v))
      {
        size += calculateDimensionX(element) + distX;
      }
    }
    size = Math.max(0, size - distX);
    basePositions.put(v, size);

    return size;
  }

  private int calculateDimensionX(Collection<AnnisNode> roots)
  {

    int size = 0;
    for(AnnisNode v : roots)
    {
      int childrenNum = graph.getSuccessors(v).size();

      if(childrenNum != 0)
      {
        for(AnnisNode element : graph.getSuccessors(v))
        {
          size += calculateDimensionX(element) + distX;
        }
      }
      size = Math.max(0, size - distX);
      basePositions.put(v, size);
    }

    return size;
  }

  protected void setCurrentPositionFor(AnnisNode vertex)
  {
    int x = m_currentPoint.x;
    int y = m_currentPoint.y;
    if(x < 0)
    {
      size.width -= x;
    }

    if(x > size.width - distX)
    {
      size.width = x + distX;
    }

    if(y < 0)
    {
      size.height -= y;
    }
    if(y > size.height - distY)
    {
      size.height = y + distY;
    }
    locations.get(vertex).setLocation(m_currentPoint);

  }
}
