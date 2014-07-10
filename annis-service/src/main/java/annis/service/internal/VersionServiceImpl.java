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

package annis.service.internal;

import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This service part only provides a method to get the version of this 
 * ANNIS instance.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Component
@Path("annis/version")
public class VersionServiceImpl
{
  private final Properties versionProperties = new Properties();
  
  private final static Logger log = LoggerFactory.getLogger(VersionServiceImpl.class);
  
  public VersionServiceImpl()
  {
    try
    {
      versionProperties.load(VersionServiceImpl.class.getResourceAsStream("version.properties"));
    }
    catch (IOException ex)
    {
      log.warn("Could not load the version of the ANNIS service", ex);
    }
  }
  
  @GET
  public String getFull()
  {
    String rev = getRevisionNumber();
    StringBuilder result = new StringBuilder();

    result.append(getReleaseName());
    if (!rev.isEmpty())
    {
      result.append(" (");

      result.append("rev. ");
      result.append(rev);


      result.append(")");
    }
    return result.toString();
  }
  
  @GET
  @Path("release")
  public String getReleaseName()
  {
    return versionProperties.getProperty("version", "UNKNOWN_RELEASE");
  }
  
  @GET
  @Path("revision")
  public String getRevisionNumber()
  {
    return versionProperties.getProperty("build_revision", "");
  }
}
