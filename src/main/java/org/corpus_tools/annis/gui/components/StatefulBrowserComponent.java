/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.components;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.VerticalLayout;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedds a single HTML page and adds navigation to it's headers (if they have
 * an id).
 *
 * This is e.g. useful for documentation.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class StatefulBrowserComponent extends VerticalLayout {

    @JavaScript({ "vaadin://jquery.js", "statefulbrowsercomponent.js" })
    private class IFrameComponent extends AbstractJavaScriptComponent {

        /**
         * 
         */
        private static final long serialVersionUID = -4822333489764804388L;

        public IFrameComponent() {
            addFunction("urlChanged", arguments -> {
                getState().setSource(arguments.get(0).asString());
                getState().setLastScrollPos(0);
            });
            addFunction("scrolled", arguments -> getState().setLastScrollPos((int) arguments.getNumber(0)));
        }

        @Override
        public final IframeState getState() {
            return (IframeState) super.getState();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = -7089777149165739882L;

    private final static Logger log = LoggerFactory.getLogger(StatefulBrowserComponent.class);

    private final IFrameComponent iframe = new IFrameComponent();

    public StatefulBrowserComponent(URI externalURI) {
        iframe.setSizeFull();

        addComponent(iframe);

        setExpandRatio(iframe, 1.0f);

        setSource(externalURI);
    }

    private void setSource(URI externalURI) {
        iframe.getState().setSource(externalURI.toASCIIString());
    }
}
