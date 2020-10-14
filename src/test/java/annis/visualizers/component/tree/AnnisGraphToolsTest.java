package annis.visualizers.component.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashSet;
import java.util.Set;
import org.corpus_tools.salt.SaltFactory;
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

}
