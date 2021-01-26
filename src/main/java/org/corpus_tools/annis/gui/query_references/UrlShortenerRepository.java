package org.corpus_tools.annis.gui.query_references;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface UrlShortenerRepository extends CrudRepository<UrlShortenerEntry, UUID> {

  public List<UrlShortenerEntry> findByUrl(URI url);

}
