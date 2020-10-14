package annis.libgui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import annis.libgui.visualizers.VisualizerInput;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PDFPageHelperTest {

  VisualizerInput visInput;
  private SDocumentGraph graph;
  private SNode pageNode;

  @BeforeEach
  void setUp() throws Exception {
    visInput = mock(VisualizerInput.class);

    SDocument document = SaltFactory.createSDocument();
    SampleGenerator.createTokens(document);
    graph = document.getDocumentGraph();
    pageNode = graph.getTokens().get(0);

  }

  @Test
  void getPageFromAnnotationNullSafety() {

    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);

    assertNull(pdfPageHelper.getPageFromAnnotation(null));
    assertNull(pdfPageHelper.getPageFromAnnotation(SaltFactory.createSNode()));
  }

  @Test
  void getPageFromAnnotatioNoNamespace() {

    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);

    pageNode.createAnnotation(null, "page", "23");
    assertEquals("23", pdfPageHelper.getPageFromAnnotation(pageNode));

  }

  @Test
  void getPageFromAnnotatioWithNamespace() {

    when(visInput.getNamespace()).thenReturn("morphology");
    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);
    
    // Test that namespace is checked
    pageNode.createAnnotation("some_ns", "page", "42");
    assertEquals("42", pdfPageHelper.getPageFromAnnotation(pageNode));
  }

  @Test
  void getPageFromAnnotatioWithNonExistingNamespace() {

    when(visInput.getNamespace()).thenReturn("another_ns");
    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);

    // Test that namespace is checked
    assertNull(pdfPageHelper.getPageFromAnnotation(pageNode));
  }

}
