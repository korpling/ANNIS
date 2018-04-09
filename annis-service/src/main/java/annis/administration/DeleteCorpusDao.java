/*
 * Copyright 2012 SFB 632.
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
package annis.administration;

import com.google.common.base.Joiner;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class DeleteCorpusDao extends AbstractAdminstrationDao {

    private final static Logger log = LoggerFactory.getLogger(AdministrationDao.class);

    /**
     * Deletes a top level corpus, when it is already exists.
     *
     * @param corpusName
     */
    public void checkAndRemoveTopLevelCorpus(String corpusName) {
        if (existConflictingTopLevelCorpus(corpusName)) {
            log.info("delete conflicting corpus: {}", corpusName);
            deleteCorpora(Arrays.asList(corpusName));
        }
    }

    public void deleteCorpora(List<String> names) {

        if (names == null || names.isEmpty()) {
            return;
        }
        
        File dataDir = getRealDataDir();

        try (Connection conn = createSQLiteConnection()) {
            conn.setAutoCommit(false);

            log.info("deleting external data files");
            for (String corpusName : names) {
                List<String> filesToDelete = getQueryRunner().query(conn,
                        "SELECT filename FROM media_files\n" + "WHERE\n" + "  corpus_path = ? OR corpus_path like ?",
                        new ColumnListHandler<>(1), corpusName, corpusName + "/%");
                for (String fileName : filesToDelete) {
                    File f = new File(dataDir, fileName);
                    if (f.exists()) {
                        if (!f.delete()) {
                            log.warn("Could not delete {}", f.getAbsolutePath());
                        }
                    }
                }

                getQueryRunner().update(conn,
                        "DELETE FROM media_files\n" + "WHERE\n" + "  corpus_path = ? OR corpus_path like ?", corpusName,
                        corpusName + "/%");
            }

            log.info("deleting resolver entries");
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM resolver_vis_map WHERE corpus=?")) {
                for (String n : names) {
                    delStmt.setString(1, n);
                    delStmt.executeUpdate();
                }
            }

            log.info("deleting example query entries");
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM example_queries WHERE corpus=?")) {
                for (String n : names) {
                    delStmt.setString(1, n);
                    delStmt.executeUpdate();
                }
            }

            log.info("deleting texts");
            for (String corpusName : names) {
                getQueryRunner().update(conn,
                        "DELETE FROM text\n" + "WHERE\n" + "  corpus_path = ? OR corpus_path like ?", corpusName,
                        corpusName + "/%");
            }

            log.info("deleting from corpus info");
            try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM corpus_info WHERE name=?")) {
                for (String n : names) {
                    delStmt.setString(1, n);
                    delStmt.executeUpdate();
                }
            }
            
            log.info("deleting from graphANNIS");
            for (String corpusName : names) {
                getQueryDao().getCorpusStorageManager().deleteCorpus(corpusName);
                getQueryRunner().update(conn,
                        "DELETE FROM text\n" + "WHERE\n" + "  corpus_path = ? OR corpus_path like ?", corpusName,
                        corpusName + "/%");
            }
            
            conn.commit();

        } catch (SQLException ex) {
            log.error("Error when deleting corpus {}", Joiner.on(",").join(names), ex);
        }

        List<String> quotedNames = new LinkedList<>();
        for (String n : names) {
            quotedNames.add("'" + n + "'");
        }


    }

}
