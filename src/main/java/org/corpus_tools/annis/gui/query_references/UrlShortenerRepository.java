package org.corpus_tools.annis.gui.query_references;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlShortenerRepository extends JpaRepository<UrlShortenerEntry, UUID> {

  public List<UrlShortenerEntry> findByUrl(URI url);

}
