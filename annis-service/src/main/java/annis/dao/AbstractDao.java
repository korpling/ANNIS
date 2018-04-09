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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import annis.administration.FileAccessException;
import annis.administration.StatementController;
import annis.tabledefs.Column;
import annis.tabledefs.Table;
import annis.utils.DynamicDataSource;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Common functions used by all data access objects.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public abstract class AbstractDao extends DBProvider {
  private final static Logger log = LoggerFactory.getLogger(AbstractDao.class);

  private DynamicDataSource dataSource;
  private StatementController statementController;
  private String scriptPath;

  protected MapSqlParameterSource makeArgs() {
    return new MapSqlParameterSource();
  }

  public void registerGUICancelThread(StatementController statementCon) {
    this.statementController = statementCon;
  }


  public void createTableIfNotExists(Table table, File initialValuesCSV, Function<String[], String[]> lineModifier)
      throws SQLException {

    if (table == null) {
      return;
    }

    try (Connection conn = createSQLiteConnection()) {
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

        // create index for the columns
        for (Column c : table.getIndexedColumns()) {
          getQueryRunner().update(conn, "CREATE INDEX " + "idx_" + table.getName() + "_" + c.getName() + " ON "
              + table.getName() + " (" + c.getName() + ")");
        }

        conn.commit();
      }
    }
  }

  private void importCSVIntoTable(Connection conn, Table table, File csvFile, Function<String[], String[]> lineModifier)
      throws SQLException {

    try (
        CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8),
            '\t', (char) 0)) {

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
        try (PreparedStatement insertStmt = conn
            .prepareStatement("INSERT INTO " + "\"" + table.getName() + "\"" + "(" + Joiner.on(", ").join(columnNames)
                + ") " + " VALUES(" + Strings.repeat("?, ", firstLine.length - 1) + "?)")) {
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

  public void importSQLiteTable(Table table, File csvFile, Function<String[], String[]> lineModifier)
      throws SQLException {

    try (Connection conn = createSQLiteConnection()) {
      conn.setAutoCommit(false);
      importCSVIntoTable(conn, table, csvFile, lineModifier);
      conn.commit();
    }
  }


  public DynamicDataSource getDataSource() {
    return dataSource;
  }

  public String getScriptPath() {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath) {
    this.scriptPath = scriptPath;
  }

  /**
   * Registers a {@link PreparedStatement} to the {@link StatementController}.
   */
  private static class CancelableStatements implements PreparedStatementCreator, PreparedStatementCallback<Void> {

    private final String sqlQuery;

    private PreparedStatement statement;
    private final StatementController statementController;

    public CancelableStatements(String sql, StatementController statementController) {
      this.sqlQuery = sql;
      this.statementController = statementController;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
      if (statementController != null && statementController.isCancelled()) {
        throw new SQLException("process was cancelled");
      }

      statement = con.prepareCall(sqlQuery);
      if (statementController != null) {
        statementController.registerStatement(statement);
      }
      return statement;
    }

    @Override
    public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
      ps.execute();
      return null;
    }
  }

}
