package annis.frontend.servlets.visualizers;

import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

/**
 *
 * @author thomas
 */
public class JUNGTreeVisualizer extends Visualizer
{

  @Override
  public void writeOutput(OutputStream outstream)
  {

    Document doc = getPaulaJDOM();
    DirectedGraph<PaulaVertex, PaulaEdge> g = generateGraphFromPaula(doc);

    DAGLayout<PaulaVertex,PaulaEdge> dagLayout = new DAGLayout<PaulaVertex, PaulaEdge>(g);

    VisualizationViewer<PaulaVertex,PaulaEdge> vv =
      new VisualizationViewer<PaulaVertex,PaulaEdge>(dagLayout);

    vv.setSize(dagLayout.getSize());
    vv.setBackground(Color.WHITE);
    vv.setDoubleBuffered(false);
   
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
    DirectedGraph<PaulaVertex, PaulaEdge> g = new DirectedSparseMultigraph<PaulaVertex, PaulaEdge>();

    // get all edges
    Iterator<Element> itRel = paula.getRootElement().getDescendants(new ElementFilter("_rel"));
    while(itRel.hasNext())
    {
      Element el = itRel.next();

      String src = el.getAttributeValue("_src");
      String dst = el.getAttributeValue("_dst");

      if(src != null && dst != null)
      {
        try
        {
          long srcL = Long.parseLong(src);
          long dstL = Long.parseLong(dst);

          PaulaEdge edge = new PaulaEdge();
          edge.src = srcL;
          edge.dst = dstL;

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

    return g;
  }

  public class PaulaEdge
  {
    public long src;
    public long dst;

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
