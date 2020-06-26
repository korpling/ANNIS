/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CsvResultSetProvider {

    public static class DummySQLArray implements Array {

        private Object[] base;
        private int sqlType;
        private String sqlTypeName;

        public DummySQLArray(Object[] base, int sqlType, String sqlTypeName) {
            this.base = base;
            this.sqlType = sqlType;
            this.sqlTypeName = sqlTypeName;
        }

        @Override
        public void free() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getArray() throws SQLException {
            return base;
        }

        @Override
        public Object getArray(long arg0, int arg1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getArray(long arg0, int arg1, Map<String, Class<?>> arg2) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getArray(Map<String, Class<?>> arg0) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBaseType() throws SQLException {
            return sqlType;
        }

        @Override
        public String getBaseTypeName() throws SQLException {
            return sqlTypeName;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ResultSet getResultSet(long arg0, int arg1) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ResultSet getResultSet(long arg0, int arg1, Map<String, Class<?>> arg2) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ResultSet getResultSet(Map<String, Class<?>> arg0) throws SQLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CsvResultSetProvider.class);
    @Mock
    protected ResultSet rs;
    private CSVReader reader;
    private String[] current;
    private Map<String, Integer> header;

    boolean wasNull;

    public CsvResultSetProvider(InputStream csvStream) throws SQLException {
        MockitoAnnotations.initMocks(this);

        header = new HashMap<>();
        reader = new CSVReader(new InputStreamReader(csvStream), ';', '"');
        try {
            String[] firstLine = reader.readNext();
            for (int i = 0; firstLine != null && i < firstLine.length; i++) {
                header.put(firstLine[i], i);
            }
        } catch (IOException ex) {
            log.error(null, ex);
        }

        wasNull = false;

        // mock all needed methods of the ResultSet

        when(rs.next()).thenAnswer(invocation -> {
            try {
                current = reader.readNext();
                return current != null && current.length > 0;
            } catch (IOException ex) {
                return false;
            }
        });

        when(rs.wasNull()).thenAnswer(invocation -> wasNull);

        when(rs.findColumn(anyString())).thenAnswer(invocation -> {
            String arg = (String) invocation.getArguments()[0];
            return getColumnByName(arg) + 1;
        });

        // getter with column position as argument
        when(rs.getString(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return getStringValue(idx - 1);
        });

        when(rs.getLong(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return getLongValue(idx - 1);
        });

        when(rs.getInt(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return getIntValue(idx - 1);
        });

        when(rs.getBoolean(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return getBooleanValue(idx - 1);
        });

        when(rs.getArray(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return new DummySQLArray(getArrayValue(idx - 1, Types.VARCHAR), Types.VARCHAR, "VARCHAR");

        });

        when(rs.getObject(anyInt())).thenAnswer(invocation -> {
            int idx = (Integer) invocation.getArguments()[0];
            return getObjectValue(idx - 1);
        });

        // getter with column name as argument
        when(rs.getString(anyString()))
                .thenAnswer(invocation -> getStringValue(getColumnByName((String) invocation.getArguments()[0])));

        when(rs.getLong(anyString()))
                .thenAnswer(invocation -> getLongValue(getColumnByName((String) invocation.getArguments()[0])));

        when(rs.getInt(anyString()))
                .thenAnswer(invocation -> getIntValue(getColumnByName((String) invocation.getArguments()[0])));

        when(rs.getBoolean(anyString()))
                .thenAnswer(invocation -> getBooleanValue(getColumnByName((String) invocation.getArguments()[0])));

        when(rs.getArray(anyString())).thenAnswer(invocation -> {
            // HACK: we don't know how to get the type, use our knowledge
            if ("key".equalsIgnoreCase((String) invocation.getArguments()[0])) {
                return new DummySQLArray(
                        getArrayValue(getColumnByName((String) invocation.getArguments()[0]), Types.BIGINT),
                        Types.BIGINT, "BIGINT");
            } else {
                return new DummySQLArray(
                        getArrayValue(getColumnByName((String) invocation.getArguments()[0]), Types.VARCHAR),
                        Types.VARCHAR, "VARCHAR");
            }
        });

        when(rs.getObject(anyString()))
                .thenAnswer(invocation -> getObjectValue(getColumnByName((String) invocation.getArguments()[0])));

    }

    public Object[] getArrayValue(int column, int sqlType) {
        String str = getStringValue(column);
        if (StringUtils.startsWith(str, "{") && StringUtils.endsWith(str, "}")) {
            String stripped = str.substring(1, str.length() - 1);
            String[] split = stripped.split(",");

            if (sqlType == Types.BIGINT) {
                Long[] result = new Long[split.length];
                for (int i = 0; i < result.length; i++) {
                    try {
                        result[i] = Long.parseLong(split[i]);
                    } catch (NumberFormatException ex) {
                        log.error(null, ex);
                    }
                }
                return result;
            } else {
                // just return the string if requested so
                return split;
            }
        }

        return null;
    }

    public boolean getBooleanValue(int column) {
        String str = getStringValue(column);
        if (str != null) {
            try {
                if ("t".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str)) {
                    return true;
                }
            } catch (NumberFormatException ex) {
            }
        }

        return false;
    }

    public int getColumnByName(String name) {
        if (header.containsKey(name)) {
            return header.get(name);
        }
        return -1;
    }

    public int getIntValue(int column) {
        String str = getStringValue(column);
        if (str != null) {
            try {
                Integer l = Integer.parseInt(str);
                return l;
            } catch (NumberFormatException ex) {
            }
        }

        return 0;
    }

    public long getLongValue(int column) {
        String str = getStringValue(column);
        if (str != null) {
            try {
                Long l = Long.parseLong(str);
                return l;
            } catch (NumberFormatException ex) {
            }
        }

        return 0l;
    }

    public Object getObjectValue(int column) {
        String rawString = getStringValue(column);
        try {
            return Long.parseLong(rawString);
        } catch (NumberFormatException ex) {
            // ignore
        }

        return rawString;
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public String getStringValue(int column) {
        if (current != null && column >= 0) {
            String val = current[column];
            if (!"".equals(val)) {
                wasNull = false;
                return val;
            }
        }
        wasNull = true;
        return null;
    }
}
