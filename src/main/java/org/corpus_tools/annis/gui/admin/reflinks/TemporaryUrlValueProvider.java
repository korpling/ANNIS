package org.corpus_tools.annis.gui.admin.reflinks;

import com.vaadin.data.ValueProvider;
import org.corpus_tools.annis.gui.query_references.UrlShortenerEntry;

class TemporaryUrlValueProvider
    implements ValueProvider<UrlShortenerEntry, String> {

  private static final long serialVersionUID = -3280719091298053436L;

  @Override
  public String apply(UrlShortenerEntry entry) {
    if (entry.getTemporaryUrl() == null) {
      return "";
    } else {
      return entry.getTemporaryUrl().toString();
    }
  }
}