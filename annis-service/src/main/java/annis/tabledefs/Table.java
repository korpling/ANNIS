package annis.tabledefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A known table definition for SQLite.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 *
 */
@SuppressWarnings("serial")
public class Table implements Serializable
{

  private final String name;
  private final ArrayList<Column> columns;
  
  public Table(String name)
  {
    this.name = name;
    this.columns = new ArrayList<>();
  }
  
  public Table(Table orig)
  {
    this.name = orig.name;
    this.columns = new ArrayList<>(orig.columns);
  }
  
  public Table c(String name)
  {
    Table copy = new Table(this);
    copy.columns.add(new Column(name));
    return copy;
  }
  
  public Table c(String name, String type)
  {
    Table copy = new Table(this);
    copy.columns.add(new Column(name).type(type));
    return copy;
  }
  
  public Table c(String name, String type, boolean isUnique)
  {
    Table copy = new Table(this);
    Column newColumn = new Column(name).type(type);
    if(isUnique)
    {
      newColumn = newColumn.unique();
    }
    copy.columns.add(newColumn);
    return copy;
  }
  
  public ArrayList<Column> getColumns()
  {
    return new ArrayList<>(columns);
  }
  
  public String getName()
  {
    return name;
  }
  
}
