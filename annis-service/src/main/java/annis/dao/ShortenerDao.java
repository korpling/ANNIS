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

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.springframework.transaction.annotation.Transactional;

/**
 * A DAO for retrieving and adding URL shortener information from the database.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ShortenerDao extends AbstractDao
{
  private final static BaseEncoding encoding = BaseEncoding.base64Url().omitPadding();
  private final Random random = new Random();
  
  /**
   * 
   * @param str
   * @param userName
   * @return 
   */
  @Transactional(readOnly = false)
  public String shorten(String str, String userName)
  {
    // check if the string to shorten was already shortened before
    String result = getID(str);
    if(result == null)
    {
      // no, this string is new
      
      // find a new random identifier for that string
      Long newID = null;
      int numberOfTries = 0;
      while(newID == null)
      {
        Preconditions.checkState(numberOfTries < 1000, "Can't find a new random "
          + "ID that is not already taken even after trying 1000 times. "
          + "Will abort since it seems that no new shortener IDs are available.");
        
        long randomValue = random.nextLong();
        long existing = 
          getJdbcTemplate().queryForObject("SELECT count(*) FROM url_shortener WHERE id = ?", Long.class, randomValue);
        if(existing == 0l) 
        {
          newID = randomValue;
        }
        numberOfTries++;
      }
      
      getJdbcTemplate().update("INSERT INTO url_shortener(id, \"owner\", created, url) VALUES(?, ?, ?, ?)",
        newID, userName, new Date(), str);
      result = idFromInternal(newID);

    }
    return result;
  }
  
  @Transactional(readOnly = true)
  public String getLong(String id)
  {
    List<String> result = getJdbcTemplate().queryForList(
      "SELECT url FROM url_shortener WHERE id=? LIMIT 1", String.class, idToInternal(id));
    return result.isEmpty() ? null : result.get(0);
  }
  
  private String getID(String str)
  {
    List<Long> result = getJdbcTemplate().queryForList(
      "SELECT id FROM url_shortener WHERE url=? LIMIT 1", Long.class, str);
    return result.isEmpty() ? null : idFromInternal(result.get(0));
  }
  
  private static String idFromInternal(long val)
  {
    int numberOfBytes = Long.SIZE / Byte.SIZE;
    ByteBuffer buffer = ByteBuffer.allocate(numberOfBytes).putLong(val);
    return encoding.encode(buffer.array());
  }
  
  private static long idToInternal(String val)
  {
    Preconditions.checkNotNull(val);
    byte[] bytesForVal = encoding.decode(val);
    ByteBuffer buffer = ByteBuffer.wrap(bytesForVal);
    return buffer.getLong();
  }
}
