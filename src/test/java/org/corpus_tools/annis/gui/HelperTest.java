package org.corpus_tools.annis.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.corpus_tools.annis.gui.Helper;
import org.junit.jupiter.api.Test;

class HelperTest {

  private static final String RAWTEXT_QUERY =
      "(a#tok) & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc";
  private static final String WHOLEDOCUMENT_QUERY =
      "(n#node) & doc#annis:node_name=/corpus\\x2Fdoc/ & #n @* #doc";

  @Test
  void testRightToLeft() {
    assertFalse(Helper.containsRTLText("Anise"));
    assertTrue(Helper.containsRTLText("אניס"));
    assertTrue(Helper.containsRTLText("يانسون"));
    assertTrue(Helper.containsRTLText("test cשּ"));
    assertTrue(Helper.containsRTLText("test ﻕ "));
    assertTrue(Helper.containsRTLText("test ﭦ"));
    assertFalse(Helper.containsRTLText(null));
    assertFalse(Helper.containsRTLText(""));
  }

  @Test
  void testBuildAQLDocumentQuery() {
    List<String> docPath = Arrays.asList("corpus", "doc");
    // Test the full document fallback query
    assertEquals(WHOLEDOCUMENT_QUERY,
        Helper.buildDocumentQuery(docPath, null, false));
    // Test the raw text query
    assertEquals(RAWTEXT_QUERY,
        Helper.buildDocumentQuery(docPath, null, true));
    // Test the node annotation filter
    assertEquals(
        "(a#tok | a#pos | a#something) & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc",
        Helper.buildDocumentQuery(docPath, Arrays.asList("pos", "something"), true));
   // Check invalid node annotation names lead to falling back to the whole document query
   assertEquals(WHOLEDOCUMENT_QUERY,
       Helper.buildDocumentQuery(docPath, Arrays.asList("pos", "myanno=something"), false));
   // When giving giving both a node annotation filter argument and using raw text, the node filter
   // query should be returned
   assertEquals("(a#tok | a#ns:pos) & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc",
       Helper.buildDocumentQuery(docPath, Arrays.asList("ns:pos"), true));

    
  }

}
