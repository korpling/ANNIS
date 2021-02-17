package org.corpus_tools.annis.gui.query_references;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class UrlShortenerEntry {

  @Id
  private UUID id;

  @Column
  private String owner;
  @Column
  private Date created;

  @Column(nullable = false, unique = true)
  @Lob
  private URI url;
  @Column
  @Lob
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
    final int prime = 31;
    int result = 1;
    result = prime * result + ((created == null) ? 0 : created.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    result = prime * result + ((temporaryUrl == null) ? 0 : temporaryUrl.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
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
    if (created == null) {
      if (other.created != null)
        return false;
    } else if (!created.equals(other.created))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    if (temporaryUrl == null) {
      if (other.temporaryUrl != null)
        return false;
    } else if (!temporaryUrl.equals(other.temporaryUrl))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }
}
