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
package annis.gui.exporter;

import org.springframework.stereotype.Component;

/**
 * Simple text exporter.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class SimpleTextExporter extends GeneralTextExporter {

    /**
     * 
     */
    private static final long serialVersionUID = -7160326173200872339L;

    @Override
    public String getHelpMessage() {
        return "The SimpleTextExporter exports only the plain text of every search result. " + "<p>"
                + "<strong>This exporter does not work well with dialog data "
                + "(corpora that have more than one primary text). " + "Use the GridExporter instead.</strong>"
                + "</p>";
    }

    @Override
    public boolean isAlignable() {
        return false;
    }

}
