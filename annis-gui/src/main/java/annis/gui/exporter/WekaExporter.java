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
package annis.gui.exporter;

import com.sun.jersey.api.client.WebResource;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class WekaExporter implements Exporter, Serializable
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(WekaExporter.class);

  @Override
  public void convertText(String queryAnnisQL, int contextLeft, int contextRight,
    Set<String> corpora, String keysAsString, String argsAsString,
    WebResource annisResource, Writer out)
  {
    //this is a full result export
    try
    {
      InputStream result = annisResource.path("search").path("matrix")
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .queryParam("q", queryAnnisQL).get(InputStream.class);
      
      try
      {
        int c;
        while( (c = result.read()) > -1)
        {
          out.write(c);
        }
      }
      finally
      {
        result.close();
      }
      
      out.flush();
    }
    catch (Exception ex)
    {
      log.error(null, ex);
    }
  }
}
