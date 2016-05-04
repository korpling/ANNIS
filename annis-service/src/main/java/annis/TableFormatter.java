/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TableFormatter {

	private final static Logger log = LoggerFactory.getLogger(TableFormatter.class);
	
	public String formatAsTable(List<?> list, String... fields) {
		if (list.isEmpty())
			return "(empty)";

		// special column handling for lists
		// discard fields argument and print all list entries instead
		boolean isList = list.get(0) instanceof List<?>;
		if (isList)
			fields = fieldNamesForListItems(list);
		
		if (fields.length == 0)
			return "(no columns to print)";
		
		// turn vararg fields into list, because we may want to remove non-existing fields later
		List<String> columns = new ArrayList<>(Arrays.asList(fields));
		
		// column sizes have to be pre-computed
		Map<String, Integer> columnSizes = new HashMap<>();
		for (String column : columns)
			updateColumnSize(columnSizes, column, column);
		
		// loop through values to determine sizes
		// save values, while at it
		List<Map<String, String>> rows = new ArrayList<>();
		for (Object item : list) {
			Map<String, String> row = new HashMap<>();
			
			// again, special handling for lists
			if (isList) {
				List<?> listItem = (List<?>) item;
				for (int i = 0; i < listItem.size(); ++i) {
					String column = String.valueOf(i);
					String value = listItem.get(i).toString();
					row.put("#" + column, value);
					updateColumnSize(columnSizes, column, value);
				}
			} else {
				for (String column : new ArrayList<>(columns)) {
					String value = fieldValue(item, column, columns);
					if (value == null)
						continue;
					row.put(column, value);
					updateColumnSize(columnSizes, column, value);
				}
			}
			
			rows.add(row);
		}
		
		// it is possible that every column was removed earlier, because it was not a field
		if (columns.isEmpty())
			return "(no columns found)";

		// print table
		StringBuffer sb = new StringBuffer();
		printHeader(sb, columns, columnSizes);
		printValues(sb, rows, columns, columnSizes);
		return sb.toString();
	}

	// determines the maximum size of the list entries and creates as many columns
	@SuppressWarnings("unchecked")
	private String[] fieldNamesForListItems(List<?> list) {
		int size = 0;
		for (List<?> item : (List<List<?>>) list) {
			size = Math.max(size, item.size());
		}

		String[] fields = new String[size];
		for (int i = 0; i < size; ++i)
			fields[i] = "#" + String.valueOf(i);
		
		return fields;
	}

	// updates the size of column with value.length
	private void updateColumnSize(Map<String, Integer> columnSizes, String column, String value) {
		int length = value.length();
		if ( ! columnSizes.containsKey(column) || columnSizes.get(column) < length)
			columnSizes.put(column, length);
	}

	// get value of item.fieldName
	private String fieldValue(Object item, String fieldName, List<String> columns) {
		Class<?> clazz = item.getClass();
		String value = null;
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			Object object = field.get(item);
			value = object != null ? object.toString() : "";
		} catch (SecurityException e) {
			log.warn("can't access " + clazz.getName() + "." + fieldName, e);
		} catch (NoSuchFieldException e) {
			log.warn("Can't print " + clazz.getName() + "." + fieldName + ", because it does not exist.", e);
			columns.remove(fieldName);
		} catch (IllegalArgumentException e) {
			log.warn("can't access " + clazz.getName() + "." + fieldName, e);
		} catch (IllegalAccessException e) {
			log.warn("can't access " + clazz.getName() + "." + fieldName, e);
		}
		return value;
	}

	private void printHeader(StringBuffer sb, List<String> columns,
			Map<String, Integer> columnSizes) {
		for (String column : columns) {
			sb.append(pad(column, columnSizes.get(column)));
			sb.append(" | ");
		}
		sb.setLength(sb.length() - " | ".length());
		sb.append("\n");
		for (String column : columns) {
			for (int i = 0; i < columnSizes.get(column); ++i)
				sb.append("-");
			sb.append("-+-");
		}
		sb.setLength(sb.length() - "-+-".length());
		sb.append("\n");
	}

	private void printValues(StringBuffer sb, List<Map<String, String>> rows,
			List<String> columns, Map<String, Integer> columnSizes) {
		for (Map<String, String> row : rows) {
			for (String column : columns) {
				sb.append(pad(row.get(column), columnSizes.get(column)));
				sb.append(" | ");
			}
			sb.setLength(sb.length() - " | ".length());
			sb.append("\n");
		}
	}

	private String pad(Object o, int length) {
		String s = o != null ? o.toString() : "";
		if (s.length() >= length)
			return s;

		StringBuffer padded = new StringBuffer();
		for (int i = 0; i < length - s.length(); ++i)
			padded.append(" ");
		padded.append(s);
		return padded.toString();
	}
	
}
