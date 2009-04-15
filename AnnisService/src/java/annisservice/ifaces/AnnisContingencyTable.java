package annisservice.ifaces;

import java.io.Serializable;

public interface AnnisContingencyTable extends Serializable, JSONAble {
	public void setValue(int row, int column, String value) throws IndexOutOfBoundsException;
	public String getValue(int row, int column) throws IndexOutOfBoundsException;
	public int getColumnCount();
	public int getSize();
}