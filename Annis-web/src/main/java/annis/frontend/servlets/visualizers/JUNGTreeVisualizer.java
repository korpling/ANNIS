package annis.frontend.servlets.visualizers;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author thomas
 */
public class JUNGTreeVisualizer extends Visualizer
{

  @Override
  public void writeOutput(OutputStream outstream)
  {

    Graph<String, String> g = new SparseMultigraph<String, String>();

    g.addVertex("A");
    g.addVertex("B");
    g.addEdge("", "A", "B");

    FRLayout<String, String> l = new FRLayout<String, String>(g, new Dimension(100, 100));
    l.initialize();

    VisualizationViewer<String, String> vv =
      new VisualizationViewer<String, String>(l);

    vv.setBackground(Color.WHITE);
    vv.setDoubleBuffered(false);
    // create new image to paint on
    BufferedImage image = new BufferedImage(300, 300,
      BufferedImage.TYPE_INT_RGB);

    // paint graph on image
    Graphics2D graphics = image.createGraphics();
    graphics.setBackground(Color.GRAY);
    graphics.setPaint(Color.RED);
    graphics.fillRect(10, 10, 40, 100);
    //vv.paint(graphics);
    graphics.dispose();


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
}
