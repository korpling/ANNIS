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
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.test.context.support.GenericXmlContextLoader;

/**
 * A simplified loader that ignores any user defined configuration files.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnisXmlContextLoader extends GenericXmlContextLoader
{
  
  private static final Logger log = LoggerFactory.getLogger(AnnisXmlContextLoader.class);

  
  @Override
  protected void prepareContext(GenericApplicationContext ctx)
  {
    super.customizeContext(ctx);
    
    /* 
     * Only use the local configuration, 
     * thus do not use AnnisXmlContextHelper.prepareContext()! 
     */
    MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
    try
    {
      sources.addFirst(new ResourcePropertySource("file:" + Utils.getAnnisFile(
        "conf/develop.properties").getAbsolutePath()));
      sources.addFirst(new ResourcePropertySource("file:" + Utils.getAnnisFile(
        "conf/annis-service.properties").getAbsolutePath()));
    }
    catch (IOException ex)
    {
      log.error("Could not load conf/annis-service.properties", ex);
    }
  }

  
}
