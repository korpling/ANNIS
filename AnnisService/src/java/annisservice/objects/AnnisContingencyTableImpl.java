package annisservice.objects;

import java.util.List;
import java.util.Vector;

import annisservice.ifaces.AnnisContingencyTable;

public class AnnisContingencyTableImpl implements AnnisContingencyTable {
	private static final long serialVersionUID = -5454466033408083951L;
	private List<List<String>> values = new Vector<List<String>>();
	
	public int getColumnCount() {
		try {
			return values.get(0).size();
		} catch (Exception e) {
			return 0;
		}
	}

	public int getSize() {
		return values.size();
	}

	public String getValue(int row, int column) throws IndexOutOfBoundsException {
		try {
			return values.get(row).get(column);
		} catch(NullPointerException e) {
			throw new IndexOutOfBoundsException();
		}
	}

	public void setValue(int row, int column, String value) throws IndexOutOfBoundsException {
		try {
			values.get(row).add(column, value);
		} catch (ArrayIndexOutOfBoundsException e) {
			List<String> list = new Vector<String>();
			list.add(column, value);
			values.add(row, list);
		}
	}
	
	public List<String> getColumnNamesList() {
		List<String> list = new Vector<String>();
		try {
			int columnCount = values.get(0).size();
			for(int i=0; i<columnCount; i++) {
				if(i == columnCount - 1) {
					list.add("w");
				} else {
					int marker = (i / 3) + 1;
					switch(i % 3) {
						case 0:
							list.add("#" + marker);
							break;
						case 1:
							list.add("count" + marker);
							break;
						case 2:
							list.add("total" + marker);
							break;
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			//ignore
		}
		return list;
	}
	
	public String getJSON() {
		StringBuffer json = new StringBuffer("{\n");
		int n = 0;
		List<String> columnNameList = getColumnNamesList();
		json.append("\tvalues: [");
		int i=0;
		for(List<String> row : values) {
			if(i++>0)
				json.append(",");
			json.append("\n\t\t{");
			int j=-1;
			for(String value : row) {
				if(++j>0)
					json.append(", ");
				json.append("\"" + columnNameList.get(j) + "\": \"" + value + "\"");
			}
			json.append("}");
		}
		return json.toString() + "\n\t]\n}";
	}
	
}
