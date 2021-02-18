package org.corpus_tools.annis.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

class ServiceStarterDesktopTest {


  @Test
  void testUnpackToml() {
    TomlParseResult toml = Toml.parse("[main]\n" + "test =\"value\"\n" + "b = [42, 23]");
    Map<String, Object> unpacked = ServiceStarterDesktop.unpackToml(toml);

    assertEquals(1, unpacked.size());
    assertTrue(unpacked.get("main") instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> main = (Map<String, Object>) unpacked.get("main");
    assertEquals(2, main.size());

    assertEquals("value", main.get("test"));
    assertTrue(main.get("b") instanceof List);
    @SuppressWarnings("unchecked")
    List<Object> b = (List<Object>) main.get("b");
    assertEquals(2, b.size());
    assertEquals(42l, b.get(0));
    assertEquals(23l, b.get(1));
  }

}
