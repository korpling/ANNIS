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

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Set;
import org.apache.commons.io.IOUtils;
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
      WebResource res = annisResource.path("search").path("matrix")
        .queryParam("corpora", StringUtils.join(corpora, ","))
        .queryParam("q", queryAnnisQL);
      
      
      if(argsAsString.startsWith("metakeys="))
      {
        res = res.queryParam("metakeys", argsAsString.substring("metakeys".length()+1));
      }
      
      InputStream result = res.get(InputStream.class);
      try
      {
        IOUtils.copy(result, out);
      }
      finally
      {
        result.close();
      }
      
      out.flush();
    }
    catch(UniformInterfaceException ex)
    {
      log.error(null, ex);
      Notification n = new Notification("Service exception", ex.getResponse().getEntity(String.class),
        Notification.Type.WARNING_MESSAGE, true);
      n.show(Page.getCurrent());
    }
    catch(ClientHandlerException ex)
    {
      log.error(null, ex);
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
  }
}
