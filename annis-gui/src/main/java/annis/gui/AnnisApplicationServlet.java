/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Window;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Extends the default application servlet to serve some extra javascript files.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisApplicationServlet extends ApplicationServlet
{

  @Override
  protected void writeAjaxPageHtmlVaadinScripts(Window window, String themeName,
    Application application, BufferedWriter page, String appUrl, String themeUri,
    String appId, HttpServletRequest request) throws ServletException,
    IOException
  {
    page.write("<script type=\"text/javascript\">\n");
    page.write("//<![CDATA[\n");
    page.write(
      "document.write(\"<script language='javascript' src='./jquery/jquery-1.7.2.min.js'><\\/script>\");\n");
    page.write("//]]>\n</script>\n");

    super.writeAjaxPageHtmlVaadinScripts(window, themeName, application, page,
      appUrl, themeUri, appId, request);
  }
}
