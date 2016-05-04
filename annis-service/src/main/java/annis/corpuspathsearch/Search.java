/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.corpuspathsearch;

import annis.utils.ANNISFormatHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for ANNIS corpora in file system locations.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class Search
{
  
  private static final Logger log = LoggerFactory.getLogger(Search.class);

  private final List<File> rootPaths;
  private final Map<String, File> corpusPaths;
  private boolean wasSearched;

  public Search(List<File> rootPaths)
  {
    this.rootPaths = rootPaths;
    this.corpusPaths = new TreeMap<>();
    this.wasSearched = false;
  }

  public void startSearch()
  {
    corpusPaths.clear();
    for (File f : rootPaths)
    {
      searchPath(f);
    }
    wasSearched = true;
  }

  private void searchPath(File path)
  {
    if (path != null && path.canRead())
    {
      if (path.isDirectory())
      {
        // search all subdirectories
        File[] children = path.listFiles();
        for (File f : children)
        {
          log.debug("seaching in "+ f.getPath() + " for corpora");
          searchPath(f);
        }
      }
      else if (path.isFile() && "corpus.tab".equals(path.getName()))
      {
        try
        {
          String toplevel = ANNISFormatHelper.extractToplevelCorpusNames(new FileInputStream(path));
          corpusPaths.put(toplevel, path);
        }
        catch (FileNotFoundException ex)
        {
          log.error(null, ex);
        }

      }
    }
  }

  public Map<String, File> getCorpusPaths()
  {
    return corpusPaths;
  }

  public boolean isWasSearched()
  {
    return wasSearched;
  }

  public void setWasSearched(boolean wasSearched)
  {
    this.wasSearched = wasSearched;
  }
  
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    for (Map.Entry<String, File> e : corpusPaths.entrySet())
    {
      sb.append(e.getKey());
      sb.append("\t");
      sb.append(e.getValue().getParentFile().getAbsolutePath());
      sb.append("\n");
    }

    return sb.toString();
  }
}
