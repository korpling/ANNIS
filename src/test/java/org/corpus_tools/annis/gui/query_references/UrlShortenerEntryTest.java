package org.corpus_tools.annis.gui.query_references;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
    assertNotEquals(e1, "Someobject");

    UrlShortenerEntry e2 = new UrlShortenerEntry();

    assertEquals(e1, e2);

    e1.setCreated(new Date());
    assertNotEquals(e1, e2);
    e2.setCreated(e1.getCreated());
    assertEquals(e1, e2);

    e1.setId(UUID.randomUUID());
    assertNotEquals(e1, e2);
    e2.setId(e1.getId());
    assertEquals(e1, e2);

    e1.setOwner("someone");
    assertNotEquals(e1, e2);
    e2.setOwner(e1.getOwner());
    assertEquals(e1, e2);

    e1.setTemporaryUrl(URI.create("/"));
    assertNotEquals(e1, e2);
    e2.setTemporaryUrl(e1.getTemporaryUrl());
    assertEquals(e1, e2);

    e1.setUrl(URI.create("/target"));
    assertNotEquals(e1, e2);
    e2.setUrl(e1.getUrl());
    assertEquals(e1, e2);


  }

}
