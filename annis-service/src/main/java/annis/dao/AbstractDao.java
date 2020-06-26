/*
 * Copyright 2015 SFB 632.
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
package annis.dao;

import annis.DevelopConfig;
import annis.administration.StatementController;
import annis.tabledefs.Column;
import annis.tabledefs.Table;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common functions used by all data access objects.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public abstract class AbstractDao extends DBProvider {
    private final static Logger log = LoggerFactory.getLogger(AbstractDao.class);

    protected final DevelopConfig devCfg = ConfigFactory.create(DevelopConfig.class, System.getProperties(),
            System.getenv());

    private StatementController statementController;

    public void createTableIfNotExists(DB db, Table table, File initialValuesCSV,
            Function<String[], String[]> lineModifier) throws SQLException {

        if (table == null) {
            return;
        }

        try (Connection conn = createConnection(db)) {
            conn.setAutoCommit(false);

            // check if table exists
            int num_existing = getQueryRunner().query(conn,
                    "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", new ScalarHandler<>(1),
                    table.getName());

            if (num_existing == 0) {

                getQueryRunner().update(conn,
                        "CREATE TABLE " + table.getName() + " (" + Joiner.on(", ").join(table.getColumns()) + ")");

                if (initialValuesCSV != null) {
                    importCSVIntoTable(conn, table, false, false, initialValuesCSV, lineModifier);
                }

                // create combined indexes for the columns
                int index_nr = 1;
                for (ArrayList<Column> idx_columns : table.getIndexes()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < idx_columns.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(idx_columns.get(i).getName());
                    }
                    getQueryRunner().update(conn, "CREATE INDEX " + "idx_" + table.getName() + "_" + (index_nr++)
                            + " ON " + table.getName() + " (" + sb.toString() + ")");
                }

                conn.commit();
            }
        }
    }

    public void exportTableIntoCSV(Connection conn, Table table, boolean exportKeys, File csvFile,
            Function<String[], String[]> lineModifier) throws SQLException {

        try (CSVWriter csvWriter = new CSVWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8), '\t', (char) 0)) {

            List<Column> columns = exportKeys ? table.getColumns() : table.getNonKeyColumns();
            List<String> columnNames = new LinkedList<>();
            for (Column c : columns) {
                columnNames.add("\"" + c.getName() + "\"");
            }

            String sqlTemplate = "SELECT " + Joiner.on(", ").join(columnNames) + " FROM " + "\"" + table.getName()
                    + "\"";

            getQueryRunner().query(conn, sqlTemplate, (ResultSetHandler<Boolean>) rs -> {
                while (rs.next()) {
                    String[] line = new String[columnNames.size()];

                    for (int i = 0; i < line.length; i++) {
                        Object o = rs.getObject(i + 1);
                        if (o != null) {
                            line[i] = o.toString();
                        }
                    }

                    if (lineModifier != null) {
                        line = lineModifier.apply(line);
                    }

                    csvWriter.writeNext(line);
                }

                return true;

            });

        } catch (FileNotFoundException ex) {
            log.error("Could not find file", ex);
        } catch (IOException ex) {
            log.error("Could not read SQLite table", ex);
        }
    }

    public String getScriptPath() {
        return devCfg.scriptPath();
    }

    public void importCSVIntoTable(Connection conn, Table table, boolean importKeys, boolean deleteOld, File csvFile,
            Function<String[], String[]> lineModifier) throws SQLException {

        try (CSVReader csvReader = new CSVReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8), '\t', (char) 0)) {

            String[] firstLine = csvReader.readNext();
            if (lineModifier != null) {
                firstLine = lineModifier.apply(firstLine);
            }

            if (firstLine != null && firstLine.length >= 1) {
                List<Column> columns = importKeys ? table.getColumns() : table.getNonKeyColumns();
                Preconditions.checkArgument(columns.size() == firstLine.length,
                        "Import of table %s failed. " + "File '%s' should have %s columns but has %s.", table.getName(),
                        csvFile.getAbsolutePath(), columns.size(), firstLine.length);

                if (deleteOld) {
                    getQueryRunner().update(conn, "DELETE FROM \"" + table.getName() + "\"");
                }

                List<String> columnNames = new LinkedList<>();
                for (Column c : columns) {
                    columnNames.add("\"" + c.getName() + "\"");
                }

                String[] line = firstLine;
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO " + "\"" + table.getName() + "\"" + "(" + Joiner.on(", ").join(columnNames) + ") "
                                + " VALUES(" + Strings.repeat("?, ", firstLine.length - 1) + "?)")) {
                    while (line != null) {
                        for (int i = 0; i < line.length; i++) {
                            if (line[i] == null || line[i].equals("NULL")) {
                                insertStmt.setString(i + 1, null);
                            } else {
                                insertStmt.setString(i + 1, line[i]);
                            }
                        }
                        insertStmt.execute();
                        line = csvReader.readNext();
                        if (lineModifier != null) {
                            line = lineModifier.apply(line);
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            log.error("Could not find file", ex);
        } catch (IOException ex) {
            log.error("Could not write SQLite table", ex);
        }
    }

    public void importSQLiteTable(DB db, Table table, File csvFile, Function<String[], String[]> lineModifier)
            throws SQLException {

        try (Connection conn = createConnection(db)) {
            conn.setAutoCommit(false);
            importCSVIntoTable(conn, table, false, false, csvFile, lineModifier);
            conn.commit();
        }
    }

    public void registerGUICancelThread(StatementController statementCon) {
        this.statementController = statementCon;
    }

}
