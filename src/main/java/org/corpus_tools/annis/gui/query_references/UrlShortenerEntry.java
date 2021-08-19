package org.corpus_tools.annis.gui.query_references;

import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.corpus_tools.annis.gui.hibernate.type.URIType;
import org.hibernate.annotations.TypeDef;

@Entity
@TypeDef(name = "uri", defaultForType = URI.class, typeClass = URIType.class)
public class UrlShortenerEntry {

  @Id
  private UUID id;

  @Column
  private String owner;
  @Column
  private Date created;

  @Column(nullable = false, unique = true, length = 16 * 1024 * 1024)
  private URI url;

  @Column(length = 16 * 1024 * 1024)
  private URI temporaryUrl;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public Date getCreated() {
    return created;
  }

  public URI getTemporaryUrl() {
    return temporaryUrl;
  }

  public URI getUrl() {
    return url;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setUrl(URI url) {
    this.url = url;
  }

  public void setTemporaryUrl(URI temporaryUrl) {
    this.temporaryUrl = temporaryUrl;
  }

  @Override
  public int hashCode() {
    return Objects.hash(created, id, owner, temporaryUrl, url);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UrlShortenerEntry other = (UrlShortenerEntry) obj;
    return Objects.equals(created, other.created) && Objects.equals(id, other.id)
        && Objects.equals(owner, other.owner) && Objects.equals(temporaryUrl, other.temporaryUrl)
        && Objects.equals(url, other.url);
  }
}
