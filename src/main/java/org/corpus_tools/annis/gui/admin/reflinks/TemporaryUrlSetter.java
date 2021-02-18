package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.Setter;
import java.net.URI;
import java.net.URISyntaxException;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.components.ExceptionDialog;
import org.corpus_tools.annis.gui.query_references.UrlShortener;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;

class TemporaryUrlSetter implements Setter<UrlShortenerEntry, String> {


  private static final long serialVersionUID = -1917211320939260447L;

  private final AnnisUI ui;
  private final DataProvider<UrlShortenerEntry, ?> provider;

  public TemporaryUrlSetter(AnnisUI ui, DataProvider<UrlShortenerEntry, ?> provider) {
    this.ui = ui;
    this.provider = provider;
  }

  @Override
  public void accept(UrlShortenerEntry entry, String value) {
    UrlShortener shortener = ui.getUrlShortener();

    try {
      URI temporary = value == null || value.isEmpty() ? null : new URI(value);
      entry.setTemporaryUrl(temporary);
      shortener.getRepo().save(entry);
      provider.refreshItem(entry);
    } catch (URISyntaxException ex) {
      ExceptionDialog.show(ex, ui);
    }
  }
}