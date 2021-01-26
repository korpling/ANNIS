package org.corpus_tools.annis.gui.visualizers.component.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.component.tree.AnnisGraphTools;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.core.SAnnotation;
import org.junit.jupiter.api.Test;

class AnnisGraphToolsTest {

  @Test
  void extractAnnotation() {
    assertNull(AnnisGraphTools.extractAnnotation(null, "some_ns", "func"));

    Set<SAnnotation> annos = new LinkedHashSet<>();
    SAnnotation annoFunc = SaltFactory.createSAnnotation();
    annoFunc.setNamespace("some_ns");
    annoFunc.setName("func");
    annoFunc.setValue("value");
    annos.add(annoFunc);

    assertEquals("value", AnnisGraphTools.extractAnnotation(annos, null, "func"));
    assertEquals("value", AnnisGraphTools.extractAnnotation(annos, "some_ns", "func"));

    assertNull(AnnisGraphTools.extractAnnotation(annos, "another_ns", "func"));
    assertNull(AnnisGraphTools.extractAnnotation(annos, "some_ns", "anno"));
    assertNull(AnnisGraphTools.extractAnnotation(annos, "another_ns", "anno"));
    assertNull(AnnisGraphTools.extractAnnotation(annos, null, "anno"));


  }

  @Test
  void isTerminalNullCheck() {
    assertFalse(AnnisGraphTools.isTerminal(null, null));
    VisualizerInput mockedVisInput = mock(VisualizerInput.class);
    assertFalse(AnnisGraphTools.isTerminal(null, mockedVisInput));
  }

  @Test
  void hasEdgeSubtypeForEmptyType() {

    SDominanceRelation rel1 = mock(SDominanceRelation.class);
    VisualizerInput input = mock(VisualizerInput.class);

    // When the type is empty, this should be treated like having no type (null) at all
    when(rel1.getType()).thenReturn("");
    Map<String, String> mappings = new LinkedHashMap<>();
    when(input.getMappings()).thenReturn(mappings);
    mappings.put("edge_type", "null");

    AnnisGraphTools tools = new AnnisGraphTools(input);
    assertTrue(tools.hasEdgeSubtype(rel1, "null"));

    SDominanceRelation rel2 = mock(SDominanceRelation.class);
    when(rel1.getType()).thenReturn(null);
    assertTrue(tools.hasEdgeSubtype(rel2, "null"));

  }

}
