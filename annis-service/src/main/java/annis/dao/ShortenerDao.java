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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

/**
 * A DAO for retrieving and adding URL shortener information from the database.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ShortenerDao extends AbstractDao
{

  /**
   * 
   * @param str
   * @param userName
   * @return 
   */
  @Transactional(readOnly = false)
  public UUID shorten(String str, String userName)
  {
    // check if the string to shorten was already shortened before
    UUID result = getExistingShortID(str);
    if(result == null)
    {
      // no, this string is new
      
      // find a new random identifier for that string
      
      int numberOfTries = 0;
      while(result == null)
      {
        Preconditions.checkState(numberOfTries < 1000, "Can't find a new random "
          + "ID that is not already taken even after trying 1000 times. "
          + "Will abort since it seems that no new shortener IDs are available.");
        
        UUID randomUUID = UUID.randomUUID();
        long existing = 
          getJdbcTemplate().queryForObject("SELECT count(*) FROM url_shortener WHERE id = ?", Long.class, randomUUID);
        if(existing == 0l) 
        {
          result = randomUUID;
        }
        numberOfTries++;
      }
      
      getJdbcTemplate().update("INSERT INTO url_shortener(id, \"owner\", created, url) VALUES(?, ?, ?, ?)",
        result, userName, new Date(), str);

    }
    return result;
  }
  
  @Transactional(readOnly = true)
  public String unshorten(UUID id)
  {
    List<String> result = getJdbcTemplate().queryForList(
      "SELECT url FROM url_shortener WHERE id=? LIMIT 1", String.class, id);
    return result.isEmpty() ? null : result.get(0);
  }
  
  private UUID getExistingShortID(String str)
  {
    List<UUID> result = getJdbcTemplate().queryForList(
      "SELECT id FROM url_shortener WHERE url=? LIMIT 1", UUID.class, str);
    return result.isEmpty() ? null : result.get(0);
  }
  
}
