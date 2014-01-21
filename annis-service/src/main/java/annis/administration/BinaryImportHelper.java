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

import annis.CommonHelper;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;

/**
 * Imports binary files.
 *
 * <p>Therefore the meta data of the files are stored in the database, while the
 * real data are store in a simple file directory.</p>
 *
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author Benjamin Weißenfels <p.pixeldrama@gmail.com>
 */
public class BinaryImportHelper implements
  PreparedStatementCallback<Boolean>
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    BinaryImportHelper.class);

  public static final String SQL = "INSERT INTO _media_files VALUES (?, ?, ?, ?)";

  private File fileSource;

  private File fileDestination;

  private String mimeType;

  private long corpusRef;

  public BinaryImportHelper(File f, File dataDir, String toplevelCorpusName, 
    long corpusRef,
    Map<String, String> mimeTypeMapping)
  {
    this.fileSource = f;

    // create a file-name in the form of "filename_toplevelcorpus_UUID.ending", thus we
    // need to split the file name into its components
    String baseName = FilenameUtils.getBaseName(fileSource.getName());
    String extension = FilenameUtils.getExtension(fileSource.getName());
    UUID uuid = UUID.randomUUID();
    
    String outputName = "";
    if(toplevelCorpusName == null)
    {
      outputName = baseName 
        + "_" + uuid.toString()
        + (extension.isEmpty() ? "" : "." + extension);
    }
    else
    {
      outputName = baseName 
        + "_" + CommonHelper.getSafeFileName(toplevelCorpusName)
        + "_" + uuid.toString()
        + (extension.isEmpty() ? "" : "." + extension);
    }
    
    fileDestination = new File(dataDir, outputName);


    String fileEnding = FilenameUtils.getExtension(f.getName());
    if (mimeTypeMapping.containsKey(fileEnding))
    {
      this.mimeType = mimeTypeMapping.get(fileEnding);
    }
    else
    {
      this.mimeType = new MimetypesFileTypeMap().getContentType(fileSource);
    }
    this.corpusRef = corpusRef;
  }

  /**
   * Imports binary files.
   *
   * @param file Specifies path to the file, including the filename.
   * @param dataDir Specifies the directory, where the file is copied to.
   * @param toplevelCorpusName Name of the toplevel corpus..
   * @param corpusRef Assigns the file to a specific corpus in the database.
   * @param mimeTypeMapping A map of default mime types.
   */
  public BinaryImportHelper(String file, File dataDir,
    String toplevelCorpusName,
    long corpusRef, Map<String, String> mimeTypeMapping)
  {
    this(new File(file), dataDir, toplevelCorpusName, corpusRef, mimeTypeMapping);
  }

  @Override
  public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException,
    DataAccessException
  {
    ps.setString(1, fileDestination.getName());
    ps.setLong(2, this.corpusRef);
    ps.setString(3, this.mimeType);
    ps.setString(4, fileSource.getName());
    ps.executeUpdate();
    try
    {
      FileUtils.copyFile(fileSource, fileDestination);
    }
    catch (IOException ex)
    {
      log.error("Could not copy file " + fileSource.getPath(), ex);
      return false;
    }

    return true;
  }
}
