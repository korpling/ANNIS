package org.corpus_tools.annis.gui.visualizers.component.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.FontFormatException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class PDFPanelTest {

  VisualizerInput input;
  PDFPanel fixture;
  WebClient client;

  static MockWebServer mockServer;

  @BeforeAll
  static void setUpClass() throws IOException {
    mockServer = new MockWebServer();
    mockServer.start();
  }

  @AfterAll
  static void tearDownClass() throws IOException {
    mockServer.shutdown();
  }

  @BeforeEach
  void setup() throws FontFormatException, IOException {

    // Init mocks
    input = mock(VisualizerInput.class);


    // Create PDFPanel object to test
    fixture = new PDFPanel(input, "-1");

    String baseUrl = String.format("http://localhost:%s", mockServer.getPort());
    client = WebClient.create(baseUrl);
  }

  @Test
  void testBinaryPathOneFile() throws WebClientResponseException {
    SCorpusGraph corpusGraph = SampleGenerator.createCorpusStructure();
    SDocument doc = corpusGraph.getDocuments().get(0);
    
    when(input.getDocument()).thenReturn(doc);
    when(input.getContextPath()).thenReturn("/context");

    // Make sure the document has an assigned PDF file
    mockServer.enqueue(new MockResponse().setBody("[\"notapdf.webm\", \"test.pdf\"]")
        .addHeader("Content-Type", "application/json"));

    assertEquals("/context/Binary?toplevelCorpusName=rootCorpus&file=test.pdf",
        fixture.getBinaryPath(client));
  }

  @Test
  void testBinaryPathNoFile() throws WebClientResponseException {
    SCorpusGraph corpusGraph = SampleGenerator.createCorpusStructure();
    SDocument doc = corpusGraph.getDocuments().get(0);

    when(input.getDocument()).thenReturn(doc);
    when(input.getContextPath()).thenReturn("/context");

    // Make sure the document has an assigned PDF file
    mockServer
        .enqueue(new MockResponse().setBody("[]").addHeader("Content-Type", "application/json"));

    assertEquals("", fixture.getBinaryPath(client));
  }

  @Test
  void testBinaryPathApiExceptionThrown() throws WebClientResponseException
  {
    SCorpusGraph corpusGraph = SampleGenerator.createCorpusStructure();
    SDocument doc = corpusGraph.getDocuments().get(0);

    when(input.getDocument()).thenReturn(doc);
    when(input.getContextPath()).thenReturn("/context");
    AnnisUI ui = mock(AnnisUI.class);
    when(input.getUI()).thenReturn(ui);

    // Make sure the document has an assigned PDF file
    mockServer.enqueue(new MockResponse().setResponseCode(500).setBody("Invalid Network Access"));

    assertEquals("", fixture.getBinaryPath(client));
    verify(ui).addWindow(any(ExceptionDialog.class));
  }


}
