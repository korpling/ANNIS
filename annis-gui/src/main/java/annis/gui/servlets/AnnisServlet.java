/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.requesthandler.ShortenerRequestHandler;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import javax.servlet.ServletException;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class AnnisServlet extends VaadinServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7915386484002647283L;

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();

        getService().addSessionInitListener(event -> {
            VaadinSession session = event.getSession();
            event.getSession().addRequestHandler(new ShortenerRequestHandler());
        });
    }

}
