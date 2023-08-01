package org.corpus_tools.annis.gui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.gui.graphml.DocumentGraphMapper;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

class TimelineReconstructorTest {

  /**
   * Tests a sample dialog reconstruction from mapping.
   * 
   * The test GraphML file is a subgraph from the "BeMaTaC_L1_2013-02.1" corpus. It can be
   * re-generated by executing the following REST subgraph query
   * 
   * <pre>
   curl --request POST \
  --url http://localhost:5711/v1/corpora/BeMaTaC_L1_2013-02.1/subgraph \
  --header 'Content-Type: application/json' \
  --data '{
  "node_ids": [
    "BeMaTaC_L1_2013-02.1/2011-12-14-A#sSpan5106",
    "BeMaTaC_L1_2013-02.1/2011-12-14-A#sSpan5107"
  ],
  "left": 3,
  "right": 13
  }'
   * </pre>
   * 
   * A rendered version can be seen at
   * https://korpling.german.hu-berlin.de/annis3/?id=520fb158-559d-40ca-924d-68d233cd1dce
   * 
   * The GraphML which was generated by ANNIS is loaded and the virtual tokenization is removed. It
   * is checked if
   * <ul>
   * <li>the newly created tokenization is correct</li>
   * <li>spans cover the correct token</li>
   * </ul>
   * 
   * @throws XMLStreamException
   * @throws IOException
   */
  @Test
  void testBematacDialogMapping() throws IOException, XMLStreamException {

    File sampleDialogFile =
        ResourceUtils.getFile(this.getClass().getResource("SampleDialog.graphml"));
    SDocumentGraph docGraph = DocumentGraphMapper.map(sampleDialogFile);

    Map<String, String> anno2order = new HashMap<>();
    anno2order.put("default_ns::instructee_utt", "instructee_dipl");
    anno2order.put("default_ns::instructor_utt", "instructor_dipl");

    Set<String> segmentations = new TreeSet<>();
    // This is needed to be queried from the REST service but given for our example graph
    segmentations.add("instructor_dipl");
    segmentations.add("instructor_norm");
    segmentations.add("instructee_dipl");
    segmentations.add("instructee_norm");

    TimelineReconstructor.removeVirtualTokenization(docGraph, segmentations, anno2order);

    // instructor_dipl, instructor_norm, instructee_dipl, instructee_norm, instructee_extra, break
    List<STextualDS> texts = docGraph.getTextualDSs();
    assertEquals(4, texts.size());

    STextualDS instructorDipl = findTextualDSByName("instructor_dipl", texts);
    assertNotNull(instructorDipl);
    assertEquals("in Richtung des Toasters gehst ja gehst", instructorDipl.getText());

    DataSourceSequence<Integer> seq = new DataSourceSequence<>();
    seq.setDataSource(instructorDipl);
    seq.setStart(instructorDipl.getStart());
    seq.setEnd(instructorDipl.getEnd());
    List<SToken> instructorDiplToken =
        docGraph.getSortedTokenByText(docGraph.getTokensBySequence(seq));
    assertEquals(7, instructorDiplToken.size());
    assertEquals("in", docGraph.getText(instructorDiplToken.get(0)));
    assertEquals("Richtung", docGraph.getText(instructorDiplToken.get(1)));
    assertEquals("des", docGraph.getText(instructorDiplToken.get(2)));
    assertEquals("Toasters", docGraph.getText(instructorDiplToken.get(3)));
    assertEquals("gehst", docGraph.getText(instructorDiplToken.get(4)));
    assertEquals("ja", docGraph.getText(instructorDiplToken.get(5)));
    assertEquals("gehst", docGraph.getText(instructorDiplToken.get(6)));

    // check that the other real spans are now connected with the token
    List<SNode> uttNode = docGraph.getNodesByName("sSpan1928");
    assertNotNull(uttNode);
    assertEquals(1, uttNode.size());
    SAnnotation uttAnno = uttNode.get(0).getAnnotation("default_ns::instructor_utt");
    assertNotNull(uttAnno);
    assertEquals("utt", uttAnno.getValue_STEXT());
    List<SRelation> uttOutRelations = uttNode.get(0).getOutRelations();
    assertNotNull(uttOutRelations);
    assertEquals(5, uttOutRelations.size());
    for (SRelation rel : uttOutRelations) {
      assertTrue(rel instanceof SSpanningRelation);
      assertEquals(instructorDipl, Helper.getTextualDSForNode((SNode) rel.getTarget(), docGraph));
    }


    STextualDS instructorNorm = findTextualDSByName("instructor_norm", texts);
    assertNotNull(instructorNorm);
    assertEquals("in Richtung des Toasters gehst ja gehst", instructorNorm.getText());

    STextualDS instructeeDipl = findTextualDSByName("instructee_dipl", texts);
    assertNotNull(instructeeDipl);
    assertEquals("mhm ich geh in Richtung des Toasters okay", instructeeDipl.getText());

    STextualDS instructeeNorm = findTextualDSByName("instructee_norm", texts);
    assertNotNull(instructeeNorm);
    assertEquals("ich gehe in Richtung des Toasters okay", instructeeNorm.getText());
  }

  /**
   * Tests a sample dialog reconstruction from namespace.
   * 
   * It uses the same data as {@link #testBematacDialogMapping()}, but the annotation names have
   * been adjusted to use the namespace. The GraphML which was generated by ANNIS is loaded and the
   * virtual tokenization is removed. It is checked if
   * <ul>
   * <li>the newly created tokenization is correct</li>
   * <li>spans cover the correct token</li>
   * </ul>
   * 
   * @throws XMLStreamException
   * @throws IOException
   */
  @Test
  void testBematacDialogNamespace() throws IOException, XMLStreamException {

    File sampleDialogFile =
        ResourceUtils.getFile(this.getClass().getResource("SampleDialogNamespace.graphml"));
    SDocumentGraph docGraph = DocumentGraphMapper.map(sampleDialogFile);


    Set<String> segmentations = new TreeSet<>();
    // This is needed to be queried from the REST service but given for our example graph
    segmentations.add("instructor_dipl");
    segmentations.add("instructor_norm");
    segmentations.add("instructee_dipl");
    segmentations.add("instructee_norm");

    TimelineReconstructor.removeVirtualTokenizationUsingNamespace(docGraph, segmentations);

    // instructor_dipl, instructor_norm, instructee_dipl, instructee_norm, instructee_extra, break
    List<STextualDS> texts = docGraph.getTextualDSs();
    assertEquals(4, texts.size());

    STextualDS instructorDipl = findTextualDSByName("instructor_dipl", texts);
    assertNotNull(instructorDipl);
    assertEquals("in Richtung des Toasters gehst ja gehst", instructorDipl.getText());

    DataSourceSequence<Integer> seq = new DataSourceSequence<>();
    seq.setDataSource(instructorDipl);
    seq.setStart(instructorDipl.getStart());
    seq.setEnd(instructorDipl.getEnd());
    List<SToken> instructorDiplToken =
        docGraph.getSortedTokenByText(docGraph.getTokensBySequence(seq));
    assertEquals(7, instructorDiplToken.size());
    assertEquals("in", docGraph.getText(instructorDiplToken.get(0)));
    assertEquals("Richtung", docGraph.getText(instructorDiplToken.get(1)));
    assertEquals("des", docGraph.getText(instructorDiplToken.get(2)));
    assertEquals("Toasters", docGraph.getText(instructorDiplToken.get(3)));
    assertEquals("gehst", docGraph.getText(instructorDiplToken.get(4)));
    assertEquals("ja", docGraph.getText(instructorDiplToken.get(5)));
    assertEquals("gehst", docGraph.getText(instructorDiplToken.get(6)));

    // check that the other real spans are now connected with the token
    List<SNode> uttNode = docGraph.getNodesByName("sSpan1928");
    assertNotNull(uttNode);
    assertEquals(1, uttNode.size());
    SAnnotation uttAnno = uttNode.get(0).getAnnotation("instructor_dipl::instructor_utt");
    assertNotNull(uttAnno);
    assertEquals("utt", uttAnno.getValue_STEXT());
    List<SRelation> uttOutRelations = uttNode.get(0).getOutRelations();
    assertNotNull(uttOutRelations);
    assertEquals(5, uttOutRelations.size());
    for (SRelation rel : uttOutRelations) {
      assertTrue(rel instanceof SSpanningRelation);
      assertEquals(instructorDipl, Helper.getTextualDSForNode((SNode) rel.getTarget(), docGraph));
    }


    STextualDS instructorNorm = findTextualDSByName("instructor_norm", texts);
    assertNotNull(instructorNorm);
    assertEquals("in Richtung des Toasters gehst ja gehst", instructorNorm.getText());

    STextualDS instructeeDipl = findTextualDSByName("instructee_dipl", texts);
    assertNotNull(instructeeDipl);
    assertEquals("mhm ich geh in Richtung des Toasters okay", instructeeDipl.getText());

    STextualDS instructeeNorm = findTextualDSByName("instructee_norm", texts);
    assertNotNull(instructeeNorm);
    assertEquals("ich gehe in Richtung des Toasters okay", instructeeNorm.getText());
  }

  private STextualDS findTextualDSByName(String name, List<STextualDS> texts) {
    if (texts != null) {
      for (STextualDS t : texts) {
        if (Objects.equals(name, t.getName())) {
          return t;
        }
      }
    }

    return null;
  }

}
