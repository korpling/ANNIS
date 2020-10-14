package annis.visualizers.component.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import annis.gui.AnnisUI;
import annis.gui.UIConfig;
import annis.libgui.Helper;
import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.Match;
import com.vaadin.server.VaadinSession;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class TigerTreeVisualizerTest {

  private AnnisUI ui;
  private VisualizerInput visInput;
  private VisualizationToggle visToggle;
  private VaadinSession session;
  private ServletContext servletContext;
  private UIConfig config = new UIConfig();


  private TigerTreeVisualizer vis;

  @BeforeEach
  void setup() throws FontFormatException, IOException {

    vis = new TigerTreeVisualizer();

    // Init mocks
    visInput = mock(VisualizerInput.class);
    visToggle = mock(VisualizationToggle.class);
    ui = mock(AnnisUI.class);
    session = mock(VaadinSession.class);
    servletContext = mock(ServletContext.class);

    when(visInput.getUI()).thenReturn(ui);

    when(ui.getSession()).thenReturn(session);
    when(ui.getServletContext()).thenReturn(servletContext);
    when(ui.getConfig()).thenReturn(config);

  }

  @Test
  void compareSampleImage() throws IOException {

    // Use the example document from Salt
    SaltProject project = SampleGenerator.createSaltProject();
    SDocument doc = project.getCorpusGraphs().get(0).getDocuments().get(0);
    SDocumentGraph graph = doc.getDocumentGraph();

    // Mark the root node as match
    Match m = new Match(
        Arrays.asList(graph.getRootsByRelation(SALT_TYPE.SDOMINANCE_RELATION).get(0).getId()));
    Helper.addMatchToDocumentGraph(m, graph);

    when(visInput.getDocument()).thenReturn(doc);
    Map<String, String> mappings = new LinkedHashMap<>();
    mappings.put("node_ns", "syntax");
    mappings.put("node_key", "const");
    mappings.put("edge_type", "null");
    when(visInput.getMappings()).thenReturn(mappings);

    // Create the resource as byte array
    File tmpFile = File.createTempFile("annis-tree-test-", ".png");
    try (FileOutputStream outStream = new FileOutputStream(tmpFile)) {
      vis.writeOutput(visInput, outStream);
    }
    // Create an image so we can compare the image data instead of the binary png format
    BufferedImage actualImage = ImageIO.read(tmpFile);

    // Load the expected image as resource
    ClassPathResource expectedRes =
        new ClassPathResource("example_constituent_tree.png", this.getClass());
    BufferedImage expectedImage = ImageIO.read(expectedRes.getInputStream());

    // Compare both images pixel by pixel
    assertEquals(expectedImage.getWidth(), actualImage.getWidth(),
        String.format("Width for image %s is wrong", tmpFile.getAbsolutePath()));
    assertEquals(expectedImage.getHeight(), actualImage.getHeight(),
        String.format("Height for image %s is wrong", tmpFile.getAbsolutePath()));

    for (int x = 0; x < expectedImage.getWidth(); x++) {
      for (int y = 0; y < expectedImage.getHeight(); y++) {
        assertEquals(expectedImage.getRGB(x, y), actualImage.getRGB(x, y), String.format(
            "Pixel value at (%d,%d) for image %s was wrong", x, y, tmpFile.getAbsolutePath()));
      }
    }

  }

}
