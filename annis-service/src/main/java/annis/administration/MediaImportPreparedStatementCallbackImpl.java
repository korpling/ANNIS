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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

/**
 *
 * @author benjamin
 */
public class MediaImportPreparedStatementCallbackImpl implements
  PreparedStatementCallback<Boolean>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(MediaImportPreparedStatementCallbackImpl.class);
  
  private FileInputStream fileStream;
  private File file;
  private String mimeType;
  private long corpusRef;

  public MediaImportPreparedStatementCallbackImpl(String absolutePath, long corpusRef, Map<String,String> mimeTypeMapping)
  {
    try
    {
      this.file = new File(absolutePath);
      fileStream = new FileInputStream(file);
      
      String fileEnding = FilenameUtils.getExtension(absolutePath);
      if(mimeTypeMapping.containsKey(fileEnding))
      {
        this.mimeType = mimeTypeMapping.get(fileEnding);
      }
      else
      {
        this.mimeType = new MimetypesFileTypeMap().getContentType(file);
      }
      this.corpusRef = corpusRef;
    }
    catch (FileNotFoundException ex)
    {
     log.error(null, ex);
    }
  }

  @Override
  public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException,
    DataAccessException
  {
    // this method is not implemented for long as file-lenght, so we need to cast to int
    ps.setBinaryStream(1, fileStream, (int) file.length());
    ps.setLong(2, this.corpusRef);
    ps.setString(3, this.mimeType);
    ps.setString(4, file.getName());
    ps.executeUpdate();
    try
    {
      fileStream.close();
    }
    catch (IOException ex)
    {
     log.error(null, ex);
    }
    return true;
  }
}
