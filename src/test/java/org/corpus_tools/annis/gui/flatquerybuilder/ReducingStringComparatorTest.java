package org.corpus_tools.annis.gui.flatquerybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.corpus_tools.annis.gui.flatquerybuilder.ReducingStringComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReducingStringComparatorTest {

  private static final String DIACRITICS = "diacritics";
  private ReducingStringComparator rsc;

  @BeforeEach
  void setUp() throws Exception {
    this.rsc = new ReducingStringComparator();
  }

  @Test
  void testCompare() {
    // Dummy test
    assertEquals(0, rsc.compare("Ähm", "Ähm", DIACRITICS));
    assertTrue(rsc.compare("äh", "Ähm", DIACRITICS) < 0);

    // Combining characters should be treated the same
    assertEquals(0, rsc.compare("ëhm", "ehm", DIACRITICS));
    assertEquals(0, rsc.compare("ëhc", "ehÇ", DIACRITICS));
  }

  @Test
  void testStartsWith() {
    assertTrue(rsc.startsWith("ëhmerling", "ehm", DIACRITICS));
    assertTrue(rsc.startsWith("ëhc-verein", "ehÇ", DIACRITICS));
    assertFalse(rsc.startsWith("thistest", "something", DIACRITICS));
  }

  @Test
  void testContains() {
    assertTrue(rsc.contains("hhhhëhmerling", "ehm", DIACRITICS));
    assertTrue(rsc.contains("sdfëhc-verein", "ehÇ", DIACRITICS));
    assertFalse(rsc.contains("sdfëhc-verein", "nothere", DIACRITICS));
  }


}
