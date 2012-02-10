/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class Utils
{

  public static File getAnnisConfigLocation()
  {
    String annisHome = System.getProperty("annis.home");
    Validate.notNull(annisHome);
    File fAnnisHome = new File(annisHome);
    return fAnnisHome;
  }
  
  public static File getConfigFile(String subpath)
  {
    File annisConfig = getAnnisConfigLocation();
    return new File(annisConfig, subpath);
  }
  
  public static String min(List<Long> runtimeList)
  {
    long min = Long.MAX_VALUE;
    for (long value : runtimeList)
    {
      min = Math.min(min, value);
    }
    return String.valueOf(min);
  }

  public static String max(List<Long> runtimeList)
  {
    long max = Long.MIN_VALUE;
    for (long value : runtimeList)
    {
      max = Math.max(max, value);
    }
    return String.valueOf(max);
  }

  public static String avg(List<Long> runtimeList)
  {
    if (runtimeList.isEmpty())
    {
      return "";
    }

    long sum = 0;
    for (long value : runtimeList)
    {
      sum += value;
    }
    return String.valueOf(sum / runtimeList.size());
  }

  public static Long[] split2Long(String text, char seperator)
  {
    String[] str = StringUtils.split(text, seperator);
    Long[] lng = new Long[str.length];

    for (int i = 0; i < lng.length; i++)
    {
      try
      {
        lng[i] = Long.parseLong(str[i]);
      }
      catch (NumberFormatException ex)
      {
        Logger.getLogger(Utils.class.getName()).log(Level.SEVERE,
          "Could not parse long value, assuming \"0\" as default", ex);
      }
    }

    return lng;
  }

  /** Hashes a string using SHA-256. */
  public static String calculateSHAHash(String s) throws
    NoSuchAlgorithmException, UnsupportedEncodingException
  {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(s.getBytes("UTF-8"));
    byte[] digest = md.digest();

    String hashVal = "";
    for (byte b : digest)
    {
      hashVal += String.format("%02x", b);
    }

    return hashVal;
  }
}
