package org.corpus_tools.annis.gui.admin.reflinks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
import org.junit.jupiter.api.Test;

class TemporaryUrlValueProviderTest {

  @Test
  void testApply() {

    TemporaryUrlValueProvider provider = new TemporaryUrlValueProvider();

    UrlShortenerEntry entry = new UrlShortenerEntry();
    assertEquals("", provider.apply(entry));

    entry.setTemporaryUrl(URI.create("/"));
    assertEquals("/", provider.apply(entry));

  }

}
