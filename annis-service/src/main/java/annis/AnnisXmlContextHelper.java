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
package annis;

import annis.utils.Utils;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * Helper class to allow loading the property files for the ANNIS service from
 * different locations.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnisXmlContextHelper
{
  private static final Logger log = LoggerFactory.getLogger(AnnisXmlContextHelper.class);

  /**
   * Adds the ANNIS specific {@link ResourcePropertySource} sources to the
   * {@link GenericApplicationContext}.
   * 
   * The annis-service.properties is loaded from the following locations (in order):
   * $ANNIS_HOME/conf/
   * $ANNIS_CFG or /etc/annis/
   * ~/.annis/
   * 
   * @param ctx 
   */
  public static void prepareContext(GenericApplicationContext ctx)
  {
    final String configFileName = "annis-service.properties";
    
    MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
    try
    {
      File fBase = new File(Utils.getAnnisFile(
        "conf/" + configFileName ).getAbsolutePath());
      if(fBase.canRead() && fBase.isFile())
      {
        sources.addFirst(new ResourcePropertySource("file:" + fBase.getCanonicalPath()));
      }
      
      String globalConfig = System.getenv("ANNIS_CFG");
      if(globalConfig == null)
      {
        globalConfig = "/etc/annis";
      }
      File fGlobal = new File(globalConfig + "/" + configFileName);
      if(fGlobal.canRead() && fGlobal.isFile())
      {
        sources.addFirst(new ResourcePropertySource("file:" + fBase.getCanonicalPath()));
      }
      
      String userConfig = System.getProperty("user.home") + "/.annis";
      File fUser = new File(userConfig + "/" + configFileName);
      if(fUser.canRead() && fUser.isFile())
      {
        sources.addFirst(new ResourcePropertySource("file:" + fUser.getCanonicalPath()));
      }
      
    }
    catch (IOException ex)
    {
      log.error("Could not load configuration", ex);
    }
  }
  
}
