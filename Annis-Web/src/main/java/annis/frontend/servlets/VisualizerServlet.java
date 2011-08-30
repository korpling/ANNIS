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
package annis.frontend.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import annis.cache.Cache;
import annis.cache.CacheException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.frontend.servlets.visualizers.VisualizerInput;
import annis.frontend.servlets.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;

import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import java.io.*;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;

/**
 * This servlet dispatches visualization requests to the according visualizer Classes.<br/><br>
 * The passed GET parameters are:
 * <ol>
 * 
 * <li>callbackId (mandatory)</li>
 * <li>textId (mandatory)</li>
 * <li>vistype (mandatory)</li>
 * <li>visId (mandatory)</li>
 * <li>namespace</li>
 * <li>a list of mark:COLOR=NODEID[,NODEID]* parameters, where COLOR identifies the HTML color 
 * that will be used to mark the nodes passed after the '='</li>
 * </ol>
 * 
 * <p><strong>It currently does not use the AnnisResolver Service. This is an urgent task to complete.</strong></p>
 * 
 * @author Karsten Huetter
 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 *
 */
@PluginImplementation
public class VisualizerServlet extends HttpServlet implements Plugin
{

  private static final long serialVersionUID = -8182635617256833563L;
  private static final Map<String, VisualizerPlugin> visualizerRegistry =
    Collections.synchronizedMap(new HashMap<String, VisualizerPlugin>());
  private static final Map<String, Date> resourceAddedDate =
    Collections.synchronizedMap(new HashMap<String, Date>());

  @Override
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    HttpSession session = request.getSession();
    OutputStream outStream = response.getOutputStream();

    String callbackId = checkAndGetMandatoryStringParam("callbackId", request);
    String textId = checkAndGetMandatoryStringParam("textId", request);
    String vistype = checkAndGetMandatoryStringParam("vistype", request);
    int visId = checkAndGetMandatoryIntParam("visId", request);

    String path2Dot = getInitParameter("DotPath");
    if(path2Dot == null || "".equals(path2Dot))
    {
      path2Dot = "dot";
    }

    Map<String, String> markableMap = new HashMap<String, String>();
    Map<String, String> markableExactMap = new HashMap<String, String>();

    //fetching node marker properties from query string to set up fill/colorMap
    Enumeration<String> parameterNamesEnum = request.getParameterNames();
    while(parameterNamesEnum.hasMoreElements())
    {
      String parameterName = parameterNamesEnum.nextElement();
      String parameterValue = request.getParameter(parameterName);
      String color = parameterName.replaceFirst("^.*?:", "");
      if(parameterValue != null)
      {
        String[] elementNames = parameterValue.split(",");
        for(String elementName : elementNames)
        {
          if(parameterName.startsWith("mark:"))
          {
            //set up colorMap
            markableMap.put(elementName, color);
          }
          else if(parameterName.startsWith("markExact:"))
          {
            markableExactMap.put(elementName, color);
          }
        }
      }
    }
    
    // default to grid if vistype is unknown
    if(!visualizerRegistry.containsKey(vistype))
    {
      vistype = "grid";
    }

    VisualizerInput input = new VisualizerInput();
    input.setNamespace(request.getParameter("namespace") == null ? "" : request.getParameter("namespace"));
    input.setMarkableMap(markableMap);
    input.setMarkableExactMap(markableExactMap);
    input.setContextPath(getServletContext().getContextPath());
    input.setAnnisRemoteServiceURL(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
    input.setDotPath(path2Dot);

    String template = getServletContext().getContextPath()
      + "/secure/Resource/" + vistype + "/%s";
    input.setResourcePathTemplate(template);


    VisualizerPlugin visualizer = visualizerRegistry.get(vistype);
    if(visualizer != null)
    {

      response.setCharacterEncoding(visualizer.getCharacterEncoding());
      response.setContentType(visualizer.getContentType());

      if(visualizer.isUsingText())
      {
        //gather whole text from backend an use this for visualization
        try
        {
          AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
          AnnisResult r = service.getAnnisResult(Long.parseLong(textId));
          input.setResult(r);
        }
        catch(AnnisServiceFactoryException e)
        {
          Logger.getLogger(VisualizerServlet.class.getName()).log(Level.SEVERE,
            "Could not generate ANNIS service from factory", e);
        }
        catch(Exception e)
        {
          Logger.getLogger(VisualizerServlet.class.getName()).log(Level.SEVERE,
            "General remote service exception", e);
        }
      }
      else
      {
        //we can use the cached span for visualization
        if(session.getAttribute(SearchResultServlet.FILESYSTEM_CACHE_RESULT) != null)
        {
          try
          {
            Cache cacheAnnisResult = (Cache) session.getAttribute(SearchResultServlet.FILESYSTEM_CACHE_RESULT);

            byte[] resultAsBytes = cacheAnnisResult.getBytes(callbackId);
            ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(resultAsBytes));

            input.setResult((AnnisResult) inStream.readObject());
            ResolverEntry[] resolverEntries = (ResolverEntry[]) inStream.readObject();

            if(resolverEntries != null && visId < resolverEntries.length)
            {
              input.setMappings(resolverEntries[visId].getMappings());
            }
            else
            {
              input.setMappings(new Properties());
            }
          }
          catch(ClassNotFoundException ex)
          {
            Logger.getLogger(VisualizerServlet.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch(CacheException ex)
          {
            Logger.getLogger(VisualizerServlet.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }

      visualizer.writeOutput(input, outStream);
      outStream.flush();
    }
    else
    {
      String error = "Could not find visualizer with type \"" + vistype + "\"";
      outStream.write(error.getBytes());
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      outStream.flush();
    } // if visualizer found in registry

  }

  private String checkAndGetMandatoryStringParam(String name, HttpServletRequest request)
  {
    String result = request.getParameter(name);
    if(result == null)
    {
      throw new NullPointerException("Parameter '" + name + "' must no be null.");
    }
    return result;
  }

  private int checkAndGetMandatoryIntParam(String name, HttpServletRequest request)
  {
    String asString = checkAndGetMandatoryStringParam(name, request);

    try
    {
      return Integer.parseInt(asString);
    }
    catch(NumberFormatException ex)
    {
      throw new NumberFormatException("Could not cast the parameter '" + name
        + "' to an integer (parameter value was '" + asString + "')");
    }
  }

  @PluginLoaded
  public void newVisualizerAdded(VisualizerPlugin vis)
  {
    visualizerRegistry.put(vis.getShortName(), vis);
    resourceAddedDate.put(vis.getShortName(), new Date());

  }
}
