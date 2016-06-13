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
import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISFormatHelper
{

  private static final Logger log = LoggerFactory.
    getLogger(ANNISFormatHelper.class);

  
  /**
   * List all corpora of a ZIP file and their paths.
   * 
   * @param zip
   * @return
   * @throws IOException 
   */
  public static Map<String, ZipEntry> corporaInZipfile(ZipFile zip) throws IOException
  {
    Map<String, ZipEntry> result = new HashMap<>();
    
    for(ZipEntry e : getANNISEntry(zip, "corpus"))
    {
      String name = extractToplevelCorpusNames(zip.getInputStream(e));
      result.put(name, e);
    }
    
    return result;
  }
  
  public static Map<String, ZipEntry> corporaInZipfile(File f) throws IOException
  {
    Map<String, ZipEntry> result = new HashMap<>();
    try
    (ZipFile zip = new ZipFile(f)) 
    {
      result.putAll(corporaInZipfile(zip));
    }
    
    return result;
  }
  
  public static Map<String, File> corporaInDirectory(File d) throws IOException
  {
    Map<String, File> result = new HashMap<>();
    

    FluentIterable<File> it = Files.fileTreeTraverser().postOrderTraversal(d);
    for(File f : it)
    {
      if("corpus.annis".equalsIgnoreCase(f.getName()) || "corpus.tab".equalsIgnoreCase(f.getName()))
      {
        String toplevelName = extractToplevelCorpusNames(new FileInputStream(f));
        result.put(toplevelName, f.getParentFile());
      }
    }
    
    if (result.isEmpty())
    {
      throw new IOException("no corpus found in " + d.getCanonicalPath());
    }

    return result;
  }
  
  /**
   * Extract the name of the toplevel corpus from the content of the
   * corpus.tab file.
   *
   * @param corpusTabContent
   * @return
   */
  public static String extractToplevelCorpusNames(InputStream corpusTabContent)
  {
    String result = null;

    try(CSVReader csv = new CSVReader(new InputStreamReader(
        corpusTabContent, "UTF-8"), '\t'))
    {
      
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
   * Find the directories containing the real ANNIS tab files for a zip file.
   *
   * @param file
   * @param table The table to search for.
   * @param fileEndings The possible endings of corpus tab files (if null "tab" and "annis" are used as
   * default.
   * @return
   */
  public static List<ZipEntry> getANNISEntry(ZipFile file, String table,
    String ... fileEndings)
  {
    List<ZipEntry> allMatchingEntries = new ArrayList<>();
    
    if (fileEndings == null || fileEndings.length == 0)
    {
      fileEndings = new String[] {"tab", "annis"};
    }
    
    final List<String> fullNames = new LinkedList<>();
    for(String e : fileEndings)
    {
      fullNames.add(table + "." + e);
    }
    
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
          for(String n : fullNames)
          {
            if(n.equalsIgnoreCase(name) || entry.getName().endsWith("/" + n))
            {
              allMatchingEntries.add(entry);
            }
          }
        }
      }
    }
    return allMatchingEntries;
  }
  
}
