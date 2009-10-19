package annis.frontend.servlets.visualizers.graph;

import annis.frontend.servlets.visualizers.*;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author thomas
 */
public class JUNGTreeVisualizer extends Visualizer
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
        
        // add annotations
        if(input.getNodeAnnotations().size() > 0)
        {
          for(Annotation a : input.getNodeAnnotations())
          {
            if(getNamespace().equals(a.getNamespace()))
            {
              result.append(a.getName());
              result.append(":");
              result.append(a.getValue());
              result.append("\n");
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

    final Map<String, String> markableMapFinal = getMarkableMap();

    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<AnnisNode, Paint>()
    {

      @Override
      public Paint transform(AnnisNode input)
      {
        String idAsString = Long.toString(input.getId());
        
        if(markableMapFinal.containsKey(idAsString))
        {
          String markerColor = markableMapFinal.get(idAsString);
          if("red".equalsIgnoreCase(markerColor))
          {
            return Color.red;
          }
          else
          {
            return Color.gray;
          }
        }
        else
        {
          return Color.lightGray;
        }
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
          result.append(a.getName());
          result.append(":");
          result.append(a.getValue());
          result.append("\n");
        }

        return result.toString();
      }
    });
    vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);

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
      Logger.getLogger(JUNGTreeVisualizer.class.getName()).log(Level.SEVERE, null, ex);
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

    // get all token (so that they have the right order)
    for(AnnisNode tok : annoGraph.getTokens())
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
        if(e.getDestination() != null)
        {
          g.addVertex(e.getDestination());
        }
        if(e.getSource() != null)
        {
          g.addVertex(e.getSource());
        }
        if(e.getDestination() != null && e.getSource() != null)
        {
          g.addEdge(e, e.getSource(), e.getDestination());
        }
      }
    }

    return g;
  }


}
