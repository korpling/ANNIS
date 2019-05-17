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

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import annis.ServiceConfig;

public class DBProvider {
    
    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);
    
    public enum DB {
        CORPUS_REGISTRY("corpus_registry.sqlite3"),
        SERVICE_DATA("service_data.sqlite3");
        
        final String filename;
        
        DB(String filename) {
            this.filename = filename;
        }
    }

    private final QueryRunner queryRunner = new QueryRunner();

    public Connection createConnection(DB db) throws SQLException {
        return createConnection(db, false);
    }

    public File getDBFile(DB db) {
        return new File(getGraphANNISDir(), db.filename);
    }

    public Connection createConnection(DB db, boolean readonly) throws SQLException {
        // TODO: use a connection pool
        // TODO: split into two databases, one "corpus_registry" and a
        // "service_data" file
        File dbFile = getDBFile(db);
        return createConnection(dbFile, readonly);
    }

    public Connection createConnection(File dbFile, boolean readonly) throws SQLException {
        SQLiteConfig conf = new SQLiteConfig();
        conf.setReadOnly(readonly);
        SQLiteDataSource source = new SQLiteDataSource(conf);
        source.setUrl("jdbc:sqlite:" + dbFile.getAbsolutePath() + "?journal_mode=wal");
        return source.getConnection();
    }

    public File getANNISDir() {
        String path = cfg.dataPath();
        if(path == null || path.isEmpty()) {
            return new File(System.getProperty("user.home"), ".annis");
        } else {
            return new File(path);
        }
    }

    public File getGraphANNISDir() {
        return new File(getANNISDir(), "v4");
    }

    public QueryRunner getQueryRunner() {
        return queryRunner;
    }
}
