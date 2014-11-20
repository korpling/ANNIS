/*
 * Copyright 2014 SFB 632.
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
package annis;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static helper methods that give access to the version of ANNIS.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class VersionInfo
{

  private final static Logger log = LoggerFactory.getLogger(VersionInfo.class);

  private final static Properties versionProperties = new Properties();
  
  static
  {
    try (InputStream is = VersionInfo.class.getResourceAsStream(
      "version.properties"))
    {
      versionProperties.load(is);
    }
    catch (IOException ex)
    {
      log.warn("Could not load the version information", ex);
    }
  }

  /**
   * Get a humand readable summary of the version of this build.
   *
   * @return
   */
  public static String getVersion()
  {
    String rev = getBuildRevision();
    Date date = getBuildDate();
    StringBuilder result = new StringBuilder();

    result.append(getReleaseName());
    if (!"".equals(rev) || date != null)
    {
      result.append(" (");

      boolean added = false;
      if (!"".equals(rev))
      {
        result.append("rev. ");
        result.append(rev);
        added = true;
      }
      if (date != null)
      {
        result.append(added ? ", built " : "");

        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.append(d.format(date));
      }

      result.append(")");
    }

    return result.toString();
  }

  /**
   * Get the release name {@code (e.g. 3.1.0)}.
   *
   * @return the release name or empty string if unknown.
   */
  public static String getReleaseName()
  {
    return versionProperties.getProperty("version", "");
  }

  /**
   * Get the revision number of the versioning system for this build.
   *
   * @return
   */
  public static String getBuildRevision()
  {
    return versionProperties.getProperty("build_revision", "");
  }

  /**
   * Get the date when ANNIS was built.
   *
   * @return Either the date or {@code null} if unknown.
   */
  public static Date getBuildDate()
  {
    Date result = null;
    try
    {
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      String raw = versionProperties.getProperty("build_date");
      if(raw != null)
      {
        result = format.parse(raw);
      }
    }
    catch (ParseException ex)
    {
      log.debug(null, ex);
    }
    return result;
  }
}
