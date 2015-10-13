/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.requesthandler;

import annis.libgui.visualizers.IFrameResource;
import annis.libgui.visualizers.IFrameResourceMap;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * A request handler that delivers visualization resources.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ResourceRequestHandler implements RequestHandler
{
  
  private final String prefix;
  
  public ResourceRequestHandler(String urlPrefix)
  {
    this.prefix = urlPrefix + "/vis-iframe-res/";
  }

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if (request.getPathInfo() != null && request.getPathInfo().
      startsWith(prefix))
    {
      String uuidString = StringUtils.removeStart(request.getPathInfo(),
        prefix);
      UUID uuid = UUID.fromString(uuidString);
      IFrameResourceMap map = VaadinSession.getCurrent().
        getAttribute(IFrameResourceMap.class);
      if (map == null)
      {
        response.setStatus(404);
      }
      else
      {
        IFrameResource res = map.get(uuid);
        if (res != null)
        {
          response.setStatus(200);
          response.setContentType(res.getMimeType());
          response.getOutputStream().write(res.getData());
        }
      }
      return true;
    }
    return false;
  }
  
}
