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

import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AttributeListServlet extends HttpServlet
{

  private static final long serialVersionUID = -4188886565776492022L;

  @Override
  protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
  {
    doGet(arg0, arg1);
  }

  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setCharacterEncoding("utf-8");
    
    String corpusIdsParam = request.getParameter("corpusIds");

    List<Long> corpusIdList = new Vector<Long>();

    //gather selected corpora
    try
    {
      for (String corpusId : corpusIdsParam.split(","))
      {
        corpusIdList.add(Long.parseLong(corpusId));
      }
    }
    catch (NullPointerException e)
    {
      //We got no corpus Ids...
    }
    catch (NumberFormatException e)
    {
      //We got wrong Corpus Ids
    }

    /* Required Stuff */
    boolean scriptTag = false;
    String cb = request.getParameter("callback");
    if (cb != null)
    {
      scriptTag = true;
      response.setContentType("text/javascript");
    }
    else
    {
      response.setContentType("application/x-json");
    }
    Writer out = response.getWriter();
    if (scriptTag)
    {
      out.write(cb + "(");
    }
    /* END Required Stuff */

    try
    {
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      AnnisAttributeSet attributeList;

      // add all corpora if not choosen
      if (corpusIdsParam == null)
      {
        AnnisCorpusSet allCorpora = service.getCorpusSet();
        for (AnnisCorpus c : allCorpora)
        {
          corpusIdList.add(c.getId());
        }
      }

      attributeList = service.getAttributeSet(corpusIdList, true, true);

      // check if we should not add prefix
      if (request.getParameter("noprefix") != null)
      {
        for (AnnisAttribute att : attributeList)
        {
          String a = att.getName();
          if (a != null)
          {
            int found = a.indexOf(':');
            if (found > -1 && (found + 1) < a.length())
            {
              att.setName(a.substring(found + 1));
            }
          }
        }
      }

      out.write(attributeList.getJSON());
    }
    catch (Exception ex)
    {
      Logger.getLogger(AttributeListServlet.class.getName()).log(
        Level.SEVERE, "could not get attribute list", ex);
    }




    /* Required Stuff */
    if (scriptTag)
    {
      out.write(");");
    }
    /* END Required Stuff */

  }
}
