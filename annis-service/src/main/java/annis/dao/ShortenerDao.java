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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.date.DateFormatUtils;

import com.google.common.base.Preconditions;

/**
 * A DAO for retrieving and adding URL shortener information from the database.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ShortenerDao extends AbstractDao {

  private static final Logger log = LoggerFactory.getLogger(ShortenerDao.class);

  /**
   * 
   * @param str
   * @param userName
   * @return 
   */
  public UUID shorten(String str, String userName) {
    UUID result = null;

    try (Connection conn = createConnection(DB.SERVICE_DATA)) {
      conn.setAutoCommit(false);

      // check if the string to shorten was already shortened before
      result = getExistingShortID(conn, str);
      if (result == null) {
        // no, this string is new

        // find a new random identifier for that string

        int numberOfTries = 0;
        while (result == null) {
          Preconditions.checkState(numberOfTries < 1000,
              "Can't find a new random " + "ID that is not already taken even after trying 1000 times. "
                  + "Will abort since it seems that no new shortener IDs are available.");

          UUID randomUUID = UUID.randomUUID();
          int existing = getQueryRunner().query(conn, "SELECT count(*) FROM url_shortener WHERE id = ?",
              new ScalarHandler<>(1), randomUUID);
          if (existing == 0) {
            result = randomUUID;
          }
          numberOfTries++;
        }

        getQueryRunner().update(conn, "INSERT INTO url_shortener(id, \"owner\", created, url) VALUES(?, ?, ?, ?)", result,
            userName, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date()), str);
        conn.commit();
      }
    } catch (SQLException ex) {
      log.error("Could not shorten URL {} for user {}", str, userName, ex);
    }
    return result;
  }

  public String unshorten(UUID id) {
    try (Connection conn = createConnection(DB.SERVICE_DATA, true)) {
      
      List<String> result = getQueryRunner().query(conn, "SELECT url FROM url_shortener WHERE id=? LIMIT 1",
          new ColumnListHandler<>(1), id);

      return result.isEmpty() ? null : result.get(0);
    } catch (SQLException ex) {
      log.error("Could not unshorten URL with ID {}", id.toString(), ex);
    }

    return null;
  }

  private UUID getExistingShortID(Connection conn, String str) throws SQLException {

    List<String> result = getQueryRunner().query(conn, "SELECT id FROM url_shortener WHERE url=? LIMIT 1",
        new ColumnListHandler<>(1), str);
    return result.isEmpty() ? null : UUID.fromString(result.get(0));
  }

}
