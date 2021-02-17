package org.corpus_tools.annis.gui.admin.reflinks;

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

  public TemporaryUrlSetter(AnnisUI ui) {
    this.ui = ui;
  }

  @Override
  public void accept(UrlShortenerEntry entry, String value) {
    UrlShortener shortener = ui.getUrlShortener();
    if (value == null || value.isEmpty()) {
      entry.setTemporaryUrl(null);
      shortener.getRepo().save(entry);
    } else {
      try {
        entry.setTemporaryUrl(new URI(value));
        shortener.getRepo().save(entry);
      } catch (URISyntaxException ex) {
        ExceptionDialog.show(ex, ui);
      }
    }
  }
}