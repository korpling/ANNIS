package org.corpus_tools.annis.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class HelperTest {

  private static final String RAWTEXT_QUERY =
      "(a#tok | a#annis:node_type=\"datasource\") & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc";
  private static final String WHOLEDOCUMENT_QUERY =
      "n#annis:node_type & doc#annis:node_name=/corpus\\x2Fdoc/ & #n @* #doc";

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
    // Test the full document fallback query
    assertEquals(WHOLEDOCUMENT_QUERY, Helper.buildDocumentQuery("corpus/doc", null, false));
    // Test the raw text query
    assertEquals(RAWTEXT_QUERY, Helper.buildDocumentQuery("corpus/doc", null, true));
    // Test the node annotation filter
    assertEquals(
        "(a#tok | a#pos | a#something | a#annis:node_type=\"datasource\") & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc",
        Helper.buildDocumentQuery("corpus/doc", Arrays.asList("pos", "something"), true));
    // Test the node annotation filter with qualified annotation names
    assertEquals(
        "(a#tok | a#ns:pos | a#something | a#default:another | a#annis:node_type=\"datasource\") & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc",
        Helper.buildDocumentQuery("corpus/doc",
            Arrays.asList("ns::pos", "something", "default::another"), true));
    // Check invalid node annotation names lead to falling back to the whole document query
    assertEquals(WHOLEDOCUMENT_QUERY,
        Helper.buildDocumentQuery("corpus/doc", Arrays.asList("pos", "myanno=something"), false));
    // When giving giving both a node annotation filter argument and using raw text, the node filter
    // query should be returned
    assertEquals(
        "(a#tok | a#ns:pos | a#annis:node_type=\"datasource\") & doc#annis:node_name=/corpus\\x2Fdoc/ & #a @* #doc",
        Helper.buildDocumentQuery("corpus/doc", Arrays.asList("ns:pos"), true));


  }

}
