/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
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
import annis.cache.FilesystemCache;
import annis.cache.CacheException;
import annis.cache.CacheInitializationException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.frontend.servlets.visualizers.Visualizer;

import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;

/**
 * This servlet dispatches visualization requests to the according visualizer Classes.<br/><br>
 * The passed GET parameters are:
 * <ol>
 * 
 * <li>spanId (mandatory)</li>
 * <li>textId (mandatory)</li>
 * <li>namespace (mandatory)</li>
 * <li>a list of mark:COLOR=NODEID[,NODEID]* parameters, where COLOR identifies the HTML color 
 * that will be used to mark the nodes passed after the '='</li>
 * </ol>
 * 
 * <p><strong>It currently does not use the AnnisResolver Service. This is an urgent task to complete.</strong></p>
 * 
 * 
 * @author Karsten Huetter
 *
 */
public class VisualizerServlet extends HttpServlet
{

  private static final long serialVersionUID = -8182635617256833563L;

  @Override
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    OutputStream outStream = response.getOutputStream();
   
    String spanId = request.getParameter("spanId");
    String textId = request.getParameter("textId");

    String path2Dot = getInitParameter("DotPath");
    if(path2Dot == null || "".equals(path2Dot))
    {
      path2Dot = "dot";
    }

    if(spanId == null)
    {
      throw new NullPointerException("Parameter 'spanId' must no be null.");
    }

    String namespace = request.getParameter("namespace");
    if(namespace == null)
    {
      throw new NullPointerException("Parameter 'namespace' must no be null.");
    }

    if(textId == null)
    {
      throw new NullPointerException("Parameter 'textId' must no be null.");
    }

    Map<String, String> markableMap = new HashMap<String, String>();

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
        }
      }
    }

    try
    {
      ClassLoader classLoader = Visualizer.class.getClassLoader();
      boolean isUseTextId = false;

      //String className = "annis.frontend.servlets.visualizers.PartiturVisualizer";
      String className = "annis.frontend.servlets.visualizers.partitur.PartiturVisualizer";
      if("tiger".equals(namespace))
      {
        //className = "annis.frontend.servlets.visualizers.JUNGTreeVisualizer";
        className = "annis.frontend.servlets.visualizers.TreeVisualizer";
      }
      else if("mmax".equals(namespace))
      {
        //className = "annis.frontend.servlets.visualizers.MmaxVisualizer";
        className = "annis.frontend.servlets.visualizers.CorefVisualizer";
        isUseTextId = true;
      }
      else if("paula".equals(namespace))
      {
        className = "annis.frontend.servlets.visualizers.PaulaVisualizer";
      }
      else if("paulatext".equals(namespace))
      {
        className = "annis.frontend.servlets.visualizers.PaulaVisualizer";
        isUseTextId = true;
      }
      else if("audio".equals(namespace))
      {
        className = "annis.frontend.servlets.visualizers.AudioVisualizer";
      }
      else if("urml".equals(namespace))
      {
        className = "annis.frontend.servlets.visualizers.PartiturVisualizer";
      }

      Visualizer visualizer = (Visualizer) classLoader.loadClass(className).newInstance();
      visualizer.setNamespace(namespace);
      visualizer.setMarkableMap(markableMap);
      visualizer.setContextPath(getServletContext().getContextPath());
      visualizer.setDotPath(path2Dot);

      response.setCharacterEncoding(visualizer.getCharacterEncoding());
      response.setContentType(visualizer.getContentType());

      if(isUseTextId)
      {
        //gather whole text from backend an use this for visualization
        try
        {
          AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
          visualizer.setPaula(service.getPaula(Long.parseLong(textId)));
        }
        catch(AnnisServiceFactoryException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      else
      {
        //we can use the cached span for visualization
        Cache cache = new FilesystemCache("Paula");
        visualizer.setPaula(cache.get(spanId));
      }

      visualizer.writeOutput(outStream);
    }
    catch(InstantiationException e1)
    {
      e1.printStackTrace(new PrintWriter(outStream));
    }
    catch(IllegalAccessException e1)
    {
      e1.printStackTrace(new PrintWriter(outStream));
    }
    catch(CacheInitializationException e)
    {
      e.printStackTrace(new PrintWriter(outStream));
    }
    catch(CacheException e)
    {
      e.printStackTrace(new PrintWriter(outStream));
    }
    catch(ClassNotFoundException e)
    {
      e.printStackTrace(new PrintWriter(outStream));
    }
  }
}
