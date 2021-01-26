package org.corpus_tools.annis.visualizers.iframe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.server.VaadinSession;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.ServletContext;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.libgui.VisualizationToggle;
import org.corpus_tools.annis.libgui.visualizers.VisualizerInput;
import org.corpus_tools.annis.visualizers.iframe.CorefVisualizer;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorefVisualizerTest {


  private CorefVisualizer vis;

  private AnnisUI ui;
  private VisualizerInput visInput;
  private VisualizationToggle visToggle;
  private VaadinSession session;
  private ServletContext servletContext;

  @BeforeEach
  void setup() {

    vis = new CorefVisualizer();

    // Init mocks
    visInput = mock(VisualizerInput.class);
    visToggle = mock(VisualizationToggle.class);
    ui = mock(AnnisUI.class);
    session = mock(VaadinSession.class);
    servletContext = mock(ServletContext.class);

    when(visInput.getUI()).thenReturn(ui);
    when(ui.getSession()).thenReturn(session);
    when(ui.getServletContext()).thenReturn(servletContext);
  }

  @Test
  void tokenGenerated() {

    // Use the example document from Salt
    SaltProject project = SampleGenerator.createSaltProject();
    SDocument doc = project.getCorpusGraphs().get(0).getDocuments().get(0);
    SDocumentGraph graph = doc.getDocumentGraph();

    when(visInput.getDocument()).thenReturn(doc);

    // Create the resource as string
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    vis.writeOutput(visInput, outStream);
    String content = new String(outStream.toByteArray(), StandardCharsets.UTF_8);
    assertNotNull(content);

    // Parse the HTML and check that all token have been generated as td cell
    Elements tokenElements = Jsoup.parse(content).select("table.token td");
    List<SToken> tokens = graph.getSortedTokenByText();
    assertEquals(tokens.size(), tokenElements.size());

    for (int i = 0; i < tokens.size(); i++) {
      SToken tok = tokens.get(i);
      assertEquals(graph.getText(tok), tokenElements.get(i).text().trim());
    }
  }
}
