package annis.frontend.servlets.visualizers.graph;

import annis.frontend.servlets.MatchedNodeColors;
import annis.frontend.servlets.visualizers.*;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author thomas
 */
public class GraphVisualizer extends Visualizer
{

  @Override
  public void writeOutput(OutputStream outstream)
  {
    DirectedGraph<AnnisNode, Edge> g = generateGraphFromModel(getResult().getGraph());
    
    SuperDAGLayout dagLayout = new SuperDAGLayout(g);
    dagLayout.initialize();

    VisualizationViewer<AnnisNode, Edge> vv =
      new VisualizationViewer<AnnisNode, Edge>(dagLayout);

    vv.setSize(dagLayout.getSize().width, dagLayout.getSize().height);
    vv.setBackground(Color.WHITE);
    vv.setDoubleBuffered(false);

    // vertex label
    vv.getRenderContext().setVertexLabelTransformer(new Transformer<AnnisNode, String>()
    {

      @Override
      public String transform(AnnisNode input)
      {
        StringBuilder result = new StringBuilder();

        if(input.isToken())
        {
          result.append(input.getSpannedText());
        }
        else if(input.getNodeAnnotations().size() > 0)
        {
          {
            for(Annotation a : input.getNodeAnnotations())
            {
              if(getNamespace().equals(a.getNamespace()))
              {
                result.append(a.getValue());
                result.append("\n");
              }
            }
          }
        }
        else
        {
          // use ID as fallback
          result.append(input.getId());
        }
        return result.toString();
      }
    });

    final Map<String, String> markableMapFinal = getMarkableExactMap();

    VertexLabelAsShapeRenderer<AnnisNode, Edge> vertexTrans =
      new VertexLabelAsShapeRenderer<AnnisNode, Edge>(vv.getRenderContext());
    vv.getRenderContext().setVertexShapeTransformer(vertexTrans);
    vv.getRenderer().setVertexLabelRenderer(vertexTrans);
    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<AnnisNode, Paint>()
    {

      @Override
      public Paint transform(AnnisNode input)
      {
        String idAsString = Long.toString(input.getId());

        if(markableMapFinal.containsKey(idAsString))
        {
          String markerColor = markableMapFinal.get(idAsString);
          MatchedNodeColors color = MatchedNodeColors.Red;
          try
          {
            color = MatchedNodeColors.valueOf(markerColor);
          }
          catch(IllegalArgumentException ex)
          {

          }
          return color.getColor();
          
        }
        else
        {
          return Color.white;
        }
      }
    });
    vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<AnnisNode, Paint>()
    {

      @Override
      public Paint transform(AnnisNode input)
      {
        return new Color(255, 255, 255, 0);
      }
    });

    // render edge labels
    vv.getRenderContext().setEdgeLabelTransformer(new Transformer<Edge, String>()
    {

      @Override
      public String transform(Edge input)
      {
        StringBuilder result = new StringBuilder();

        for(Annotation a : input.getAnnotations())
        {
          if(getNamespace().equals(a.getNamespace())
            && !"--".equals(a.getValue()))
          {
            result.append(a.getValue());
            result.append("\n");
          }
        }

        return result.toString();
      }
    });
    vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(true);

    // straight lines
    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<AnnisNode, Edge>());

    // create new image to paint on
    BufferedImage image = new BufferedImage(vv.getWidth(), vv.getHeight(),
      BufferedImage.TYPE_INT_RGB);

    // paint graph on image
    Graphics2D graphics = image.createGraphics();
    graphics.setBackground(Color.WHITE);
    vv.paint(graphics);

    try
    {
      ImageIO.write(image, "png", outstream);
      outstream.flush();
    }
    catch(IOException ex)
    {
      Logger.getLogger(GraphVisualizer.class.getName()).log(Level.SEVERE, null, ex);
    }


  }

  @Override
  public String getContentType()
  {
    return "image/png";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "latin1";
  }

  public DirectedGraph<AnnisNode, Edge> generateGraphFromModel(AnnotationGraph annoGraph)
  {
    DirectedGraph<AnnisNode, Edge> g =
      new DirectedOrderedSparseMultigraph<AnnisNode, Edge>();

    List<AnnisNode> sortedTokenList = annoGraph.getTokens();
    Collections.sort(sortedTokenList, new Comparator<AnnisNode>() {

      @Override
      public int compare(AnnisNode o1, AnnisNode o2)
      {
        return o1.getTokenIndex().compareTo(o2.getTokenIndex());
      }
    });
    // get all token (so that they have the right order)
    for(AnnisNode tok : sortedTokenList)
    {
      g.addVertex(tok);
    }

    // get all dominance edges (and other nodes that you may find on your way)
    for(Edge e : annoGraph.getEdges())
    {
      // filter  namespace
      boolean nsFound = false;
      for(Annotation a : e.getAnnotations())
      {
        if(getNamespace().equals(a.getNamespace()))
        {
          nsFound = true;
          break;
        }
      }

      if(nsFound)
      {
        // check if already existing
        if(e.getDestination() != null)
        {
          g.addVertex(e.getDestination());
        }
        if(e.getSource() != null)
        {
          g.addVertex(e.getSource());
        }
        // don't add null and only if not already existing
        if(e.getDestination() != null && e.getSource() != null
          && g.findEdge(e.getSource(), e.getDestination()) == null)
        {
          g.addEdge(e, e.getSource(), e.getDestination());
        }
      }
    }

    return g;
  }
}
