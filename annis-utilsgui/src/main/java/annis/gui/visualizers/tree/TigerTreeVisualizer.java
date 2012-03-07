/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.visualizers.tree;

import annis.MatchedNodeColors;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import annis.gui.visualizers.Visualizer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.tree.backends.staticimg.AbstractImageGraphicsItem;
import annis.gui.visualizers.tree.backends.staticimg.Java2dBackend;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.Edge;
import annis.service.ifaces.AnnisResult;
import edu.uci.ics.jung.graph.DirectedGraph;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class TigerTreeVisualizer extends Visualizer
{

  private VisualizerInput input = new VisualizerInput();
  private static final int SIDE_MARGIN = 20;
  private static final int TOP_MARGIN = 40;
  private static final int TREE_DISTANCE = 40;
  private final Java2dBackend backend;
  private final DefaultLabeler labeler;
  private final DefaultStyler styler;
  private final AnnisGraphTools graphtools;

  public class DefaultStyler implements TreeElementStyler
  {

    private final BasicStroke DEFAULT_PEN_STYLE = new BasicStroke(1);
    public static final int LABEL_PADDING = 2;
    public static final int HEIGHT_STEP = 40;
    public static final int TOKEN_SPACING = 15;
    public static final int VEDGE_OVERLAP_THRESHOLD = 20;
    private final Java2dBackend backend;

    public DefaultStyler(Java2dBackend backend_)
    {
      backend = backend_;
    }

    public int getLabelPadding()
    {
      return LABEL_PADDING;
    }

    public GraphicsBackend.Font getFont(AnnisNode n)
    {
      if(n.isToken())
      {
        return backend.getFont(Font.SANS_SERIF, 12, java.awt.Font.PLAIN);
      }
      else
      {
        return backend.getFont(Font.SANS_SERIF, 15, java.awt.Font.BOLD);
      }

    }

    public GraphicsBackend.Font getFont(Edge e)
    {
      return backend.getFont(Font.SANS_SERIF, 10, java.awt.Font.PLAIN);
    }

    @Override
    public Shape getShape(AnnisNode n)
    {
      if(isQueryMatch(n))
      {
        // get CSS color name
        String backColorName = input.getMarkableMap().get("" + n.getId());
        Color backColor = Color.RED;
        try
        {
          backColor = MatchedNodeColors.valueOf(backColorName).getColor();
        }
        catch(IllegalArgumentException ex)
        {
        }

        if(n.isToken())
        {
          return new Shape.Rectangle(Color.WHITE, backColor, DEFAULT_PEN_STYLE, getLabelPadding());
        }
        else
        {
          return new Shape.Ellipse(Color.WHITE, backColor, DEFAULT_PEN_STYLE, getLabelPadding());
        }
      }
      else
      {
        if(n.isToken())
        {
          return new Shape.Invisible(getLabelPadding());
        }
        else
        {
          return new Shape.Ellipse(Color.BLACK, Color.WHITE, DEFAULT_PEN_STYLE, getLabelPadding());
        }
      }
    }

    private boolean isQueryMatch(AnnisNode n)
    {
      return input.getMarkableExactMap().containsKey(Long.toString(n.getId()));
    }

    @Override
    public Shape getShape(Edge e)
    {
      if(AnnisGraphTools.hasEdgeSubtype(e, AnnisGraphTools.SECEDGE_SUBTYPE, input))
      {
        return new Shape.Rectangle(getEdgeColor(e), Color.WHITE, DEFAULT_PEN_STYLE, getLabelPadding());
      }
      else
      {
        return new Shape.Rectangle(new Color(0.4f, 0.4f, 0.4f), Color.WHITE, DEFAULT_PEN_STYLE, getLabelPadding());
      }
    }

    @Override
    public Color getTextBrush(AnnisNode n)
    {
      if(isQueryMatch(n))
      {
        return Color.WHITE;
      }
      else
      {
        return Color.BLACK;
      }
    }

    @Override
    public Color getTextBrush(Edge n)
    {
      return Color.BLACK;
    }

    @Override
    public int getHeightStep()
    {
      return HEIGHT_STEP;
    }

    @Override
    public Color getEdgeColor(Edge e)
    {
      if(AnnisGraphTools.hasEdgeSubtype(e, AnnisGraphTools.SECEDGE_SUBTYPE, input))
      {
        return new Color(0.5f, 0.5f, 0.8f, 0.7f);
      }
      else
      {
        return new Color(0.3f, 0.3f, 0.3f);
      }
    }

    @Override
    public int getTokenSpacing()
    {
      return TOKEN_SPACING;
    }

    @Override
    public int getVEdgeOverlapThreshold()
    {
      return VEDGE_OVERLAP_THRESHOLD;
    }

    @Override
    public Stroke getStroke(Edge e)
    {
      if(AnnisGraphTools.hasEdgeSubtype(e, AnnisGraphTools.SECEDGE_SUBTYPE, input))
      {
        return new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]
          {
            2, 2
          }, 0);
      }
      else
      {
        return new BasicStroke(2);
      }
    }
  }

  private class DefaultLabeler implements TreeElementLabeler
  {

    @Override
    public String getLabel(AnnisNode n)
    {
      if(n.isToken())
      {
        String spannedText = n.getSpannedText();
        if(spannedText == null || "".equals(spannedText))
        {
          spannedText = " ";
        }
        return spannedText;
      }
      else
      {
        return extractAnnotation(n.getNodeAnnotations(),
          input.getMappings().getProperty("node_anno_ns", input.getNamespace()),
          input.getMappings().getProperty("node_key", "cat"));
      }
    }

    @Override
    public String getLabel(Edge e)
    {
      return extractAnnotation(e.getAnnotations(),
        input.getMappings().getProperty("edge_anno_ns", input.getNamespace()),
        input.getMappings().getProperty("edge_key", "func"));
    }

    private String extractAnnotation(Set<Annotation> annotations, String namespace, String featureName)
    {
      for(Annotation a : annotations)
      {
        if(a.getNamespace().equals(namespace) && a.getName().equals(featureName))
        {
          return a.getValue();
        }
      }
      return "--";
    }
  }

  public TigerTreeVisualizer()
  {
    backend = new Java2dBackend();
    labeler = new DefaultLabeler();
    styler = new DefaultStyler(backend);
    graphtools = new AnnisGraphTools();
  }

  @Override
  public String getShortName()
  {
    return "tree";
  }

  
  
  @Override
  public void writeOutput(VisualizerInput input, OutputStream outstream)
  {
    this.input = input;
    AnnisResult result = input.getResult();
    List<AbstractImageGraphicsItem> layouts = new LinkedList<AbstractImageGraphicsItem>();

    double width = 0;
    double maxheight = 0;

    for(DirectedGraph<AnnisNode, Edge> g : graphtools.getSyntaxGraphs(input))
    {
      ConstituentLayouter<AbstractImageGraphicsItem> cl = new ConstituentLayouter<AbstractImageGraphicsItem>(
        g, backend, labeler, styler, input);

      AbstractImageGraphicsItem item = cl.createLayout(
        new LayoutOptions(VerticalOrientation.TOP_ROOT, AnnisGraphTools.detectLayoutDirection(result.getGraph())));

      Rectangle2D treeSize = item.getBounds();

      maxheight = Math.max(maxheight, treeSize.getHeight());
      width += treeSize.getWidth();
      layouts.add(item);
    }

    BufferedImage image = new BufferedImage(
      (int) (width + (layouts.size() - 1) * TREE_DISTANCE + 2 * SIDE_MARGIN),
      (int) (maxheight + 2 * TOP_MARGIN), BufferedImage.TYPE_INT_ARGB);
    Graphics2D canvas = createCanvas(image);
    double xOffset = SIDE_MARGIN;
    for(AbstractImageGraphicsItem item : layouts)
    {
      AffineTransform t = canvas.getTransform();
      Rectangle2D bounds = item.getBounds();
      canvas.translate(xOffset, TOP_MARGIN + maxheight - bounds.getHeight());
      renderTree(item, canvas);
      xOffset += bounds.getWidth() + TREE_DISTANCE;
      canvas.setTransform(t);
    }
    try
    {
      ImageIO.write(image, "png", outstream);
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  private void renderTree(AbstractImageGraphicsItem item, Graphics2D canvas)
  {
    List<AbstractImageGraphicsItem> allItems = new ArrayList<AbstractImageGraphicsItem>();
    item.getAllChildren(allItems);

    Collections.sort(allItems, new Comparator<AbstractImageGraphicsItem>()
    {

      @Override
      public int compare(AbstractImageGraphicsItem o1,
        AbstractImageGraphicsItem o2)
      {
        return o1.getZValue() - o2.getZValue();
      }
    });

    for(AbstractImageGraphicsItem c : allItems)
    {
      c.draw(canvas);
    }
  }

  private Graphics2D createCanvas(BufferedImage image)
  {
    Graphics2D canvas = (Graphics2D) image.getGraphics();
    canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    return canvas;
  }

  @Override
  public String getContentType()
  {
    return "image/png";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "ISO-8859-1";
  }
}
