package annis.visualizers.component.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import annis.libgui.visualizers.VisualizerInput;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Arrays;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PDFPanelTest {

  VisualizerInput input;

  PDFPanel fixture;

  @BeforeEach
  void setup() throws FontFormatException, IOException {

    // Init mocks
    input = mock(VisualizerInput.class);


    // Create PDFPanel object to test
    fixture = new PDFPanel(input, "-1");
  }

  @Test
  void testBinaryPath() throws ApiException {
    SCorpusGraph corpusGraph = SampleGenerator.createCorpusStructure();
    SDocument doc = corpusGraph.getDocuments().get(0);
    
    when(input.getDocument()).thenReturn(doc);
    when(input.getContextPath()).thenReturn("/context");

    // Make sure the document has an assigned PDF file
    CorporaApi api = mock(CorporaApi.class);
    when(api.listFiles(anyString(), anyString()))
        .thenReturn(Arrays.asList("test.pdf"));

    assertEquals("/context/Binary?toplevelCorpusName=rootCorpus&file=test.pdf",
        fixture.getBinaryPath(api));
  }
}
