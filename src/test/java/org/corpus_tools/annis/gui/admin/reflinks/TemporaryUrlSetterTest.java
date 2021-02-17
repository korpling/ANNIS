package org.corpus_tools.annis.gui.admin.reflinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.vaadin.data.provider.DataProvider;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;
import org.corpus_tools.annis.gui.query_references.UrlShortenerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemporaryUrlSetterTest {

  TemporaryUrlSetter fixture;
  AnnisUI ui;
  UrlShortener shortener;
  UrlShortenerRepository repo;
  DataProvider<UrlShortenerEntry, ?> provider;
  UrlShortenerEntry entry;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    entry = new UrlShortenerEntry();

    ui = mock(AnnisUI.class);
    shortener = mock(UrlShortener.class);
    repo = mock(UrlShortenerRepository.class);
    provider = mock(DataProvider.class);

    when(ui.getUrlShortener()).thenReturn(shortener);
    when(shortener.getRepo()).thenReturn(repo);

    this.fixture = new TemporaryUrlSetter(ui, provider);
  }

  @Test
  void testAcceptNull() {

    fixture.accept(entry, null);
    assertNull(entry.getTemporaryUrl());
    verify(repo).save(eq(entry));
    verify(provider).refreshItem(eq(entry));
    verifyNoMoreInteractions(repo);
    verifyNoMoreInteractions(provider);
  }

  @Test
  void testAcceptEmpty() {

    // Empty string should be handled as null value
    fixture.accept(entry, "");
    assertNull(entry.getTemporaryUrl());
    verify(repo).save(eq(entry));
    verify(provider).refreshItem(eq(entry));
    verifyNoMoreInteractions(repo);
    verifyNoMoreInteractions(provider);
  }

  @Test
  void testAcceptNonEmpty() {
    fixture.accept(entry, "/example");
    assertEquals("/example", entry.getTemporaryUrl().toASCIIString());
    verify(repo).save(eq(entry));
    verify(provider).refreshItem(eq(entry));
    verifyNoMoreInteractions(repo);
    verifyNoMoreInteractions(provider);
  }

  @Test
  void testAcceptInvalidUrl() {
    fixture.accept(entry, "file://\\\\\\\\INVALID");
    verifyNoMoreInteractions(repo);
    verifyNoMoreInteractions(provider);
  }

}
