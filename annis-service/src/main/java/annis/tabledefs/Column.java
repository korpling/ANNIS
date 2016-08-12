package annis.tabledefs;

import java.io.Serializable;

/**
 * A column definition for SQLite.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 *
 */
@SuppressWarnings("serial")
public class Column implements Serializable
{
  private final String name;
  private String type = "TEXT";
  private boolean unique = false;
  
  public Column(String name)
  {
    this.name = name;
  }
  
  /**
   * Copy constructor.
   * @param orig
   */
  public Column(Column orig)
  {
    this.name = orig.name;
    this.type = orig.type;
    this.unique = orig.unique;
  }

  
  public Column type(String type)
  {
    Column copy = new Column(this);
    copy.type = type;
    return copy;
  }
  
  public Column unique()
  {
    Column copy = new Column(this);
    copy.unique = true;
    return copy;
  }
  
  public String getName()
  {
    return name;
  }
  
  public String getType()
  {
    return type;
  }
  
  public boolean isUnique()
  {
    return unique;
  }
  
  @Override
  public String toString()
  {
    return name + " " + type + (unique ? " UNIQUE" : "");
  }
}
