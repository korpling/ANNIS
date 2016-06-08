package annis;

import static org.junit.Assert.fail;

import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimelineReconstructorTest
{
  
  private final static Logger log = LoggerFactory.getLogger(TimelineReconstructorTest.class);

  @Before
  public void setUp() throws Exception
  {
  }

  /**
   * Tests a sample dialog reconstruction.
   * The dialog is this one: https://korpling.org/annis3/?id=44b60a56-31da-4469-b438-62fdb67f28f1
   */
  @Test
  public void test()
  {
    SDocumentGraph docGraph = SaltUtil.loadDocumentGraph(URI.createURI(getClass().getResource("SampleDialog.salt").toString()));
    
    TimelineReconstructor.removeVirtualTokenization(docGraph);

    // instructor_dipl, instructor_norm, instructee_dipl, instructee_norm, instructee_extra, break
    Assert.assertEquals(6, docGraph.getTextualDSs().size());
  }

}
