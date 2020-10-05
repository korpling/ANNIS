/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.servlets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author thomas
 */
@WebServlet(urlPatterns = "/Cite/*")
@Component
public class CitationRedirectionServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 5264592666872908858L;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CitationRedirectionServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            URI uri = new URI(req.getRequestURI());
            req.getSession().setAttribute("citation", uri.getPath());
            resp.sendRedirect(req.getContextPath() + "/");
          } catch (URISyntaxException | IOException ex) {
            log.error(null, ex);
            try {
              resp.sendError(500, ex.getMessage());
            } catch (IOException sendEx) {
              log.error("Could not send error to client", sendEx);
            }
        }
    }

}
