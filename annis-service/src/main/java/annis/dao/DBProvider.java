/*
 * Copyright 2018 Humboldt-Universität zu Berlin.
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
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public class DBProvider {

    private final QueryRunner queryRunner = new QueryRunner();

    public Connection createSQLiteConnection() throws SQLException {
        return createSQLiteConnection(false);
    }

    public File getDBFile() {
        return new File(getGraphANNISDir(), "annis.sqlite3");
    }

    public Connection createSQLiteConnection(boolean readonly) throws SQLException {
        // TODO: use a connection pool
        // TODO: split into two databases, one "corpus_registry" and a
        // "service_data" file
        File dbFile = getDBFile();
        return createSQLiteConnection(dbFile, readonly);
    }

    public Connection createSQLiteConnection(File dbFile, boolean readonly) throws SQLException {
        SQLiteConfig conf = new SQLiteConfig();
        SQLiteDataSource source = new SQLiteDataSource(conf);
        source.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        return source.getConnection();
    }

    public final File getANNISDir() {
        // TODO: make the annis folder location configurable
        // (like the external data dir)
        return new File(System.getProperty("user.home"), ".annis");
    }

    public final File getGraphANNISDir() {
        if (false) {
        }
        return new File(getANNISDir(), "graphannis");
    }

    public QueryRunner getQueryRunner() {
        return queryRunner;
    }
}
