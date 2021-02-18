/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.components.codemirror;

import com.vaadin.shared.ui.JavaScriptComponentState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlRootElement;
import org.corpus_tools.annis.api.model.GraphAnnisErrorAQLSyntaxError;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
// state of the AqlCodeEditor-component
public class AqlCodeEditorState extends JavaScriptComponentState {

    /**
     * Class that is suitable of transporting the parse error state via JSON to the client.
     */
    @XmlRootElement
    public static class ParseError implements Serializable {

        private static final long serialVersionUID = -5677925443009922921L;

        public int startLine; // NO_UCD (use private)

        public int startColumn; // NO_UCD (unused code)

        public int endLine; // NO_UCD (unused code)

        public int endColumn; // NO_UCD (unused code)

        public String message;

        public ParseError(GraphAnnisErrorAQLSyntaxError orig) {
            if (orig.getLocation() != null) {
                if (orig.getLocation().getStart() != null) {
                    this.startLine = orig.getLocation().getStart().getLine();
                    this.startColumn = orig.getLocation().getStart().getColumn();
                }
                if (orig.getLocation().getEnd() != null) {
                    this.endLine = orig.getLocation().getEnd().getLine();
                    this.endColumn = orig.getLocation().getEnd().getColumn();
                }
            }
            this.message = orig.getDesc();
        }
    }

    /**
     * An explictly {@link Serializable} {@link Comparator} for strings.
     */
    private static class StringComparator implements Comparator<String>, Serializable {

        private static final long serialVersionUID = -2949588142652208669L;

        @Override
        public int compare(String o1, String o2) {
            if (o1 == null || o2 == null) {
                throw new NullPointerException();
            }
            return o1.compareTo(o2);
        }

    }

    private static final long serialVersionUID = -9042515261512849313L;

    /**
     * The current text of the editor *
     */
    public String text = ""; // NO_UCD (use default)

    public String inputPrompt = ""; // NO_UCD (unused code)

    public final ArrayList<ParseError> errors = new ArrayList<>(); // NO_UCD (use default)

    public final TreeMap<String, Integer> nodeMappings = new TreeMap<>(new StringComparator()); // NO_UCD
                                                                                                // (use
                                                                                                // default)

    public String textareaClass; // NO_UCD (use default)

    /**
     * Everytime the server wants to set the {@link #text} variable this counter needs to be
     * increased.
     */
    public long serverRequestCounter = 0; // NO_UCD (use default)

}
