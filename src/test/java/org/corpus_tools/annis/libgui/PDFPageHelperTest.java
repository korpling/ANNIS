package org.corpus_tools.annis.libgui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.corpus_tools.annis.libgui.PDFPageHelper;
import org.corpus_tools.annis.libgui.visualizers.VisualizerInput;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
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

    SNode node = SaltFactory.createSNode();
    node.createAnnotation("test", "name", "value");

  }

  @Test
  void getPageFromAnnotationNoNamespace() {
    when(visInput.getNamespace()).thenReturn(null);

    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);

    pageNode.createAnnotation(null, "page", "23");
    assertEquals("23", pdfPageHelper.getPageFromAnnotation(pageNode));

    SToken pageNode2 = graph.getTokens().get(1);
    pageNode2.createAnnotation("some_ns", "page", "42");
    assertEquals("42", pdfPageHelper.getPageFromAnnotation(pageNode2));

    SToken notPage1 = graph.getTokens().get(2);
    notPage1.createAnnotation("some_ns", "not-a-page", "42");
    assertNull(pdfPageHelper.getPageFromAnnotation(notPage1));

    SToken notPage2 = graph.getTokens().get(3);
    notPage2.removeLayer(notPage2.getLayers().iterator().next());
    notPage2.createAnnotation(null, "not-a-page", "42");
    assertNull(pdfPageHelper.getPageFromAnnotation(notPage2));

  }

  @Test
  void getPageFromAnnotationWithNamespace() {

    when(visInput.getNamespace()).thenReturn("morphology");
    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);
    
    // Test that namespace is checked
    pageNode.createAnnotation("some_ns", "page", "42");
    assertEquals("42", pdfPageHelper.getPageFromAnnotation(pageNode));

    SToken notPage1 = graph.getTokens().get(1);
    notPage1.createAnnotation("some_ns", "not-a-page", "42");
    assertNull(pdfPageHelper.getPageFromAnnotation(notPage1));
  }

  @Test
  void getPageFromAnnotationWithNonExistingNamespace() {

    when(visInput.getNamespace()).thenReturn("another_ns");
    PDFPageHelper pdfPageHelper = new PDFPageHelper(visInput);

    // Test that namespace is checked
    assertNull(pdfPageHelper.getPageFromAnnotation(pageNode));
  }

}
