package annis.libgui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HelperTest {

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

}
