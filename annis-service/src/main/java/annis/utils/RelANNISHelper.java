/*
 * Copyright 2013 SFB 632.
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
package annis.utils;

import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class RelANNISHelper
{

  private static final Logger log = LoggerFactory.
    getLogger(RelANNISHelper.class);

  /**
   * Extract the name of the toplevel corpus name from the content of the
   * corpus.tab file.
   *
   * @return
   */
  public static String extractToplevelCorpusName(InputStream corpusTabContent)
  {
    String result = null;

    try
    {
      CSVReader csv = new CSVReader(new InputStreamReader(
        corpusTabContent, "UTF-8"), '\t');
      String[] line;
      int maxPost = Integer.MIN_VALUE;
      int minPre = Integer.MAX_VALUE;

      while ((line = csv.readNext()) != null)
      {
        if (line.length >= 6 && "CORPUS".equalsIgnoreCase(line[2]))
        {
          int pre = Integer.parseInt(line[4]);
          int post = Integer.parseInt(line[5]);

          if (pre <= minPre && post >= maxPost)
          {
            minPre = pre;
            maxPost = post;
            result = line[1];
          }
        }
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      log.error(null, ex);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return result;
  }

  /**
   * Find the directory containing the real relannis tab files for a zip file.
   *
   * @param file
   * @param table The table to search for.
   * @param fileEnding The ending of corpus tab files (if null "tab" is used as
   * default.
   * @return
   */
  public static ZipEntry getRelANNISEntry(ZipFile file, String table,
    String fileEnding)
  {
    if (fileEnding == null)
    {
      fileEnding = "tab";
    }

    final String fullName = table + "." + fileEnding;

    Enumeration<? extends ZipEntry> entries = file.entries();
    while (entries.hasMoreElements())
    {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory())
      {
        String name = entry.getName();
        if (name != null)
        {
          name = name.replaceAll("\\\\", "/");
          if (fullName.equalsIgnoreCase(name) || entry.getName().endsWith(
            "/" + fullName))
          {
            return entry;
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Search the folder containing a special tab-file file in the zip file and return all
   * other zip entries that are subelements of the folder.
   * @param file
   * @param table The table name which should be used as indicator.
   * @param fileEnding The file ending of the file to search for
   * @return A list containing all zip entries that belong the the relANNIS folder
   */
  public static List<ZipEntry> getRelANNISContent(ZipFile file, String table,
    String fileEnding)
  {
    List<ZipEntry> result = new LinkedList<ZipEntry>(); 
    ZipEntry entry = getRelANNISEntry(file, table, fileEnding);
    
    if(entry != null)
    {
      // replace all "\" with "/" in case a bogus zip programm did it wrong
      String completeEntryName = entry.getName().replaceAll("\\/", "/");
      // "navigate" one level up
      String prefix = completeEntryName.substring(0, ("/" + table + "." + fileEnding).length()-1);
      
      // find all entries that match the prefix
      Enumeration<? extends ZipEntry> zipEnum = file.entries();
      while(zipEnum.hasMoreElements())
      {
        ZipEntry e = zipEnum.nextElement();
        if(e.getName().replaceAll("\\/", "/").startsWith(prefix))
        {
          result.add(zipEnum.nextElement());
        }
      }
       
    }
    
    return result;
  }
  
}
