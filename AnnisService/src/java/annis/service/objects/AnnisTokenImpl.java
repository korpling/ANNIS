package annis.service.objects;

import java.util.HashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import annis.service.ifaces.AnnisToken;

public class AnnisTokenImpl extends HashMap<String, String> implements AnnisToken
{

  private static final long serialVersionUID = 7148692986572108260L;
  private long id;
  private String text;
  private long left;
  private long right;
  private long tokenIndex;
  private long corpusId;

  public AnnisTokenImpl(long id, String text, long left, long right, long tokenIndex, long corpusId)
  {
    this.id = id;
    this.text = text;
    this.left = left;
    this.right = right;
    this.tokenIndex = tokenIndex;
    this.corpusId = corpusId;
  }

  public AnnisTokenImpl()
  {
  }

  public long getId()
  {
    return id;
  }

  public String getJSON()
  {
    throw new RuntimeException("Not implemented yet");
  }

  public String getText()
  {
    return text;
  }

  public long getLeft()
  {
    return left;
  }

  public long getRight()
  {
    return right;
  }

  public long getTokenIndex()
  {
    return tokenIndex;
  }

  @Override
  public boolean equals(Object o)
  {
    if(!(o instanceof AnnisTokenImpl))
    {
      return false;
    }
    AnnisTokenImpl other = (AnnisTokenImpl) o;

    return new EqualsBuilder().append(this.id, other.id).append(this.text, other.text).isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(id).append(text).toHashCode();
  }

  public void setId(long id)
  {
    // TODO Auto-generated method stub
  }

  public void setText(String text)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public String toString()
  {
    return "token: '" + text + "' " + super.toString();
  }

  @Override
  public long getCorpusId()
  {
    return this.corpusId;
  }

  @Override
  public void setCorpusId(long corpusId)
  {
    this.corpusId = corpusId;
  }
}
