package annis.frontend.servlets.visualizers;

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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.collections15.Transformer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;

/**
 *
 * @author thomas
 */
public class JUNGTreeVisualizer extends Visualizer
{

  @Override
  public void writeOutput(OutputStream outstream)
  {
    final Document doc = getPaulaJDOM();
    DirectedGraph<PaulaVertex, PaulaEdge> g = generateGraphFromPaula(doc);

    DAGLayout<PaulaVertex, PaulaEdge> dagLayout = new DAGLayout<PaulaVertex, PaulaEdge>(g);
    dagLayout.initialize();
    dagLayout.done();

    VisualizationViewer<PaulaVertex, PaulaEdge> vv =
      new VisualizationViewer<PaulaVertex, PaulaEdge>(dagLayout);

    vv.setSize(dagLayout.getSize().width, dagLayout.getSize().height);
    vv.setBackground(Color.WHITE);
    vv.setDoubleBuffered(false);

    // use id as vertex label (just as a test)
    vv.getRenderContext().setVertexLabelTransformer(new Transformer<PaulaVertex, String>()
    {

      public String transform(PaulaVertex input)
      {
        if(input.cat != null)
        {
          return input.cat;
        }
        else if(input.value != null)
        {
          return input.value;
        }
        else
        {
          return Long.toString(input.id);
        }
      }
    });

    final Map<String, String> markableMapFinal = getMarkableMap();

    vv.getRenderContext().setVertexFillPaintTransformer(new Transformer<PaulaVertex, Paint>()
    {

      public Paint transform(PaulaVertex input)
      {
        String idAsString = Long.toString(input.id);

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
    vv.getRenderContext().setEdgeLabelTransformer(new Transformer<PaulaEdge, String>()
    {

      public String transform(PaulaEdge input)
      {
        return input.label;
      }
    });
    vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels(false);

    // straight lines
    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<PaulaVertex, PaulaEdge>());

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

  public DirectedGraph<PaulaVertex, PaulaEdge> generateGraphFromPaula(Document paula)
  {
    DirectedGraph<PaulaVertex, PaulaEdge> g = 
      new DirectedOrderedSparseMultigraph<PaulaVertex, PaulaEdge>();

    // matching namespace
    Namespace nsTree = Namespace.getNamespace(getNamespace(), getNamespace());

    // get all token (so that they have the right order)
    Iterator<Element> itToken = paula.getRootElement().getDescendants(new ElementFilter("tok"));
    while(itToken.hasNext())
    {
      Element e = itToken.next();
      PaulaVertex v = new PaulaVertex();
      String idAsString = e.getAttributeValue("_id");
      if(idAsString != null)
      {
        try
        {
          v.id = Long.parseLong(idAsString);
          g.addVertex(v);
        }
        catch(NumberFormatException ex)
        {
          // ignore
        }
      }
    }

    // get all edges (and other nodes that you may find on your way)
    Iterator<Element> itRel = paula.getRootElement().getDescendants(new ElementFilter("_rel"));
    while(itRel.hasNext())
    {
      Element el = itRel.next();

      String func = el.getAttributeValue("func", nsTree); // only get the one with the right namespace
      String src = el.getAttributeValue("_src");
      String dst = el.getAttributeValue("_dst");

      if(func != null && src != null && dst != null)
      {
        try
        {
          long srcL = Long.parseLong(src);
          long dstL = Long.parseLong(dst);

          PaulaEdge edge = new PaulaEdge();
          edge.src = srcL;
          edge.dst = dstL;
          edge.label = func;

          PaulaVertex vSrc = new PaulaVertex();
          vSrc.id = srcL;

          PaulaVertex vDst = new PaulaVertex();
          vDst.id = dstL;

          g.addVertex(vSrc);
          g.addVertex(vDst);
          g.addEdge(edge, vSrc, vDst);

        }
        catch(NumberFormatException ex)
        {
          // ignore
        }
      }
    }

    // find cat or value for every node
    for(PaulaVertex v : g.getVertices())
    {
      try
      {
        // try to find element with that id
        XPath p = XPath.newInstance("//*[@_id=" + v.id + "]");
        Element e = (Element) p.selectSingleNode(paula);
        if(e != null)
        {
          v.cat = e.getAttributeValue("cat", nsTree);
          v.value = e.getTextTrim();
        }
      }
      catch(JDOMException ex)
      {
        Logger.getLogger(JUNGTreeVisualizer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    return g;
  }

  public class PaulaEdge
  {

    public long src;
    public long dst;
    public String label;

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final PaulaEdge other = (PaulaEdge) obj;
      if(this.src != other.src)
      {
        return false;
      }
      if(this.dst != other.dst)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 5;
      hash = 89 * hash + (int) (this.src ^ (this.src >>> 32));
      hash = 89 * hash + (int) (this.dst ^ (this.dst >>> 32));
      return hash;
    }
  }

  public class PaulaVertex
  {

    public long id;
    public String cat;
    public String value;

    @Override
    public boolean equals(Object obj)
    {
      if(obj == null)
      {
        return false;
      }
      if(getClass() != obj.getClass())
      {
        return false;
      }
      final PaulaVertex other = (PaulaVertex) obj;
      if(this.id != other.id)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 3;
      hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
      return hash;
    }
  }
}
