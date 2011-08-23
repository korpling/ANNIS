/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin
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
package annis.frontend.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import annis.frontend.servlets.visualizers.ResourcePlugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import org.apache.commons.lang.StringUtils;

/**
 * This servlet fetches resources for e.g. visualizers.

 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 *
 */
@PluginImplementation
public class ResourceServlet extends HttpServlet implements Plugin
{

  private static final long serialVersionUID = -8182635617256833563L;
  private static final Map<String, ResourcePlugin> resourceRegistry =
    Collections.synchronizedMap(new HashMap<String, ResourcePlugin>());

  @Override
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    OutputStream outStream = response.getOutputStream();

    String completePath = request.getPathInfo();
    
    if(completePath == null)
    {
      response.sendError(404, "must provide a valid and existing path with a vistype");
      return;
    }
    
    // remove trailing /
    completePath = completePath.substring(1);
    
    String[] pathComponents = completePath.split("/");
    
    String vistype = pathComponents[0];
    
    if(pathComponents.length < 2)
    {
      response.sendError(404, "must provide a valid and existing path");
      return;
    } 
    
    String path = 
      StringUtils.join(
      Arrays.copyOfRange(pathComponents, 1, pathComponents.length), "/");
    
    // get the visualizer for this vistype
    ResourcePlugin vis = resourceRegistry.get(vistype);
    if(vis == null)
    {
      response.sendError(500, "There is no resource with the short name " + vistype);
    }
    else if(path.endsWith(".class"))
    {
      response.sendError(403, "illegal class path access");
    }
    else
    {
      URL resource = vis.getClass().getResource(path);
      if(resource == null)
      {
        response.sendError(404, path + " not found");
      }
      else
      {
        // check if it is new
        URLConnection resourceConnection = resource.openConnection();
        long resourceLastModified = resourceConnection.getLastModified();
        long requestLastModified = request.getDateHeader("If-Modified-Since");
        if(requestLastModified != -1 && resourceLastModified <= requestLastModified)
        {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        else
        {
          response.addDateHeader("Last-Modified", resourceLastModified);
          // expires in one minute per default
          response.addDateHeader("Expires", new Date().getTime() + 60000);
          
          // not in cache, stream out
          String mimeType = getServletContext().getMimeType(path);
          response.setContentType(mimeType);
          if(mimeType.startsWith("text/"))
          {
            response.setCharacterEncoding("UTF-8");
          }
          OutputStream bufferedOut = new BufferedOutputStream(outStream);
          InputStream resourceInStream = new BufferedInputStream(resource.openStream());

          int v = -1;
          while((v = resourceInStream.read()) != -1)
          {
            bufferedOut.write(v);
          }
          resourceInStream.close();
          bufferedOut.flush();
          outStream.flush();
        }
      }
    }
  
  }

  @PluginLoaded
  public void newResourceAdded(ResourcePlugin vis)
  {
    resourceRegistry.put(vis.getShortName(), vis);
  }
}
