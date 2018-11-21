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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import annis.DevelopConfig;
import annis.ServiceConfig;
import annis.administration.StatementController;
import annis.tabledefs.Column;
import annis.tabledefs.Table;
import au.com.bytecode.opencsv.CSVReader;

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

    public void registerGUICancelThread(StatementController statementCon) {
        this.statementController = statementCon;
    }

  public void createTableIfNotExists(DB db, Table table, File initialValuesCSV, Function<String[], String[]> lineModifier)
      throws SQLException {

    if (table == null) {
      return;
    }

    try (Connection conn = createConnection(db)) {
      conn.setAutoCommit(false);

      // check if table exists
      int num_existing = getQueryRunner().query(conn,
          "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", new ScalarHandler<>(1), table.getName());

      if (num_existing == 0) {

        getQueryRunner().update(conn,
            "CREATE TABLE " + table.getName() + " (" + Joiner.on(", ").join(table.getColumns()) + ")");

        if (initialValuesCSV != null) {
          importCSVIntoTable(conn, table, initialValuesCSV, lineModifier);
        }
        
        // create combined indexes for the columns
        int index_nr = 1;
        for (ArrayList<Column> idx_columns : table.getIndexes()) {
          StringBuilder sb = new StringBuilder();
          for(int i=0; i < idx_columns.size(); i++) {
              if(i > 0) {
                  sb.append(", ");
              }
              sb.append(idx_columns.get(i).getName());
          }
          getQueryRunner().update(conn, "CREATE INDEX " + "idx_" + table.getName() + "_" + (index_nr++) + " ON "
              + table.getName() + " (" + sb.toString() + ")");
        }

        conn.commit();
      }
    }
  }

    private void importCSVIntoTable(Connection conn, Table table, File csvFile,
            Function<String[], String[]> lineModifier) throws SQLException {

        try (CSVReader csvReader = new CSVReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8), '\t', (char) 0)) {

            String[] firstLine = csvReader.readNext();
            if (lineModifier != null) {
                firstLine = lineModifier.apply(firstLine);
            }

            if (firstLine != null && firstLine.length >= 1) {
                List<Column> nonKeyColumns = table.getNonKeyColumns();
                Preconditions.checkArgument(nonKeyColumns.size() == firstLine.length,
                        "Import of table %s failed. " + "File '%s' should have %s columns but has %s.", table.getName(),
                        csvFile.getAbsolutePath(), nonKeyColumns.size(), firstLine.length);

                List<String> columnNames = new LinkedList<>();
                for (Column c : nonKeyColumns) {
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
            log.error("Could not read SQLite table", ex);
        }
    }

    public void importSQLiteTable(DB db, Table table, File csvFile, Function<String[], String[]> lineModifier)
            throws SQLException {

        try (Connection conn = createConnection(db)) {
            conn.setAutoCommit(false);
            importCSVIntoTable(conn, table, csvFile, lineModifier);
            conn.commit();
        }
    }

    public String getScriptPath() {
        return devCfg.scriptPath();
    }

}
