package org.corpus_tools.annis.gui.query_references;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UrlShortenerEntryTest {

  @Test
  void testEquals() {
    UrlShortenerEntry e1 = new UrlShortenerEntry();

    assertEquals(e1, e1);
    assertFalse(e1.equals(null));
    assertFalse(e1.equals("Someobject"));

    UrlShortenerEntry e2 = new UrlShortenerEntry();

    assertEquals(e1, e2);

    e1.setCreated(new Date());
    assertFalse(e1.equals(e2));
    e2.setCreated(e1.getCreated());
    assertEquals(e1, e2);

    e1.setId(UUID.randomUUID());
    assertFalse(e1.equals(e2));
    e2.setId(e1.getId());
    assertEquals(e1, e2);

    e1.setOwner("someone");
    assertFalse(e1.equals(e2));
    e2.setOwner(e1.getOwner());
    assertEquals(e1, e2);

    e1.setTemporaryUrl(URI.create("/"));
    assertFalse(e1.equals(e2));
    e2.setTemporaryUrl(e1.getTemporaryUrl());
    assertEquals(e1, e2);

    e1.setUrl(URI.create("/target"));
    assertFalse(e1.equals(e2));
    e2.setUrl(e1.getUrl());
    assertEquals(e1, e2);


  }

}
