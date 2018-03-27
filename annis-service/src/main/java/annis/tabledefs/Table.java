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
  
  public Table c(Column column) {
    Table copy = new Table(this);
    copy.columns.add(column);
    return copy;
  }
  
  public Table c(String name)
  {
    Table copy = new Table(this);
    copy.columns.add(new Column(name));
    return copy;
  }
  
  public Table c(String name, Column.Type type)
  {
    Table copy = new Table(this);
    copy.columns.add(new Column(name).type(type));
    return copy;
  }
  
  public Table c_int(String name)
  {
    return c(name, Column.Type.INTEGER);
  }
  
  public Table c_blob(String name)
  {
    return c(name, Column.Type.BLOB);
  }
  
  public Table c(String name, Column.Type type, boolean isUnique)
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
  
  public Table c_int_uniq(String name)
  {
    return c(name, Column.Type.INTEGER, true);
  }
  
  public ArrayList<Column> getColumns()
  {
    return new ArrayList<>(columns);
  }
  
  public ArrayList<Column> getNonKeyColumns()
  {
    ArrayList<Column> result = new ArrayList<>(columns.size());
    for(Column c : columns) {
      if(!c.isPrimaryKey()) {
        result.add(c);
      }
    }
    return result;
  }
  
  public String getName()
  {
    return name;
  }
  
}
