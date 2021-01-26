package org.corpus_tools.annis.visualizers.component.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.vaadin.server.VaadinSession;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.UIConfig;
import org.corpus_tools.annis.libgui.Helper;
import org.corpus_tools.annis.libgui.VisualizationToggle;
import org.corpus_tools.annis.libgui.visualizers.VisualizerInput;
import org.corpus_tools.annis.service.objects.Match;
import org.corpus_tools.annis.visualizers.component.tree.TigerTreeVisualizer;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.eclipse.emf.common.util.URI;
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

    // Load the example tree
    ClassPathResource docFile = new ClassPathResource(
        "example_constituent_tree/rootCorpus/subCorpus1/doc1.salt", TigerTreeVisualizerTest.class);
    SDocument doc = SaltFactory.createSDocument();
    doc.loadDocumentGraph(URI.createURI(docFile.getURI().toASCIIString()));
    SDocumentGraph graph = doc.getDocumentGraph();

    // Mark the root node as match
    Match m = Match
        .parseFromString(graph.getRootsByRelation(SALT_TYPE.SDOMINANCE_RELATION).get(0).getId());
    Helper.addMatchToDocumentGraph(m, graph);

    when(visInput.getDocument()).thenReturn(doc);
    Map<String, String> mappings = new LinkedHashMap<>();
    mappings.put("node_ns", "syntax");
    mappings.put("node_key", "const");
    mappings.put("edge_type", "edge");
    mappings.put("secedge_type", "secedge");
    mappings.put("edge_key", "func");

    when(visInput.getMappings()).thenReturn(mappings);

    // Create the resource as byte array
    File tmpFile = File.createTempFile("org.corpus_tools.annis-tree-test-", ".png");
    try (FileOutputStream outStream = new FileOutputStream(tmpFile)) {
      vis.writeOutput(visInput, outStream);
    }
    // Create an image so we can compare the image data instead of the binary png format
    BufferedImage actualImage = ImageIO.read(tmpFile);

    // Load the expected image as resource
    ClassPathResource expectedRes =
        new ClassPathResource("example_constituent_tree.png", this.getClass());
    BufferedImage expectedImage = ImageIO.read(expectedRes.getInputStream());

    // Compare image appearance with default thresholds
    ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
    // Anti-aliasing difference can lead to different pixels, allow some variation
    imageComparison.setAllowingPercentOfDifferentPixels(0.5);
    ImageComparisonResult comparisonResult = imageComparison.compareImages();
    assertEquals(ImageComparisonState.MATCH, comparisonResult.getImageComparisonState(), String
        .format("%f percent image difference detected", comparisonResult.getDifferencePercent()));
  }

}
