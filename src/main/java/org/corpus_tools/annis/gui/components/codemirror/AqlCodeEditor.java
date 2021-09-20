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

import com.google.common.base.Objects;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.event.FieldEvents;
import elemental.json.JsonArray;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.corpus_tools.annis.api.model.BadRequestError;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.gui.components.codemirror.AqlCodeEditorState.ParseError;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A code editor component for the ANNIQ Query Language.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@JavaScript({"vaadin://jquery.js", "lib/codemirror.js", "mode/aql/aql.js",
        "lib/edit/matchbrackets.js", "lib/lint/lint.js", "lib/display/placeholder.js",
        "AqlCodeEditor.js"})
@StyleSheet({"lib/codemirror.css", "lib/lint/lint.css", "AqlCodeEditor.css"})
// basic server-side component
public class AqlCodeEditor extends AbstractJavaScriptComponent
        implements FieldEvents.TextChangeNotifier, Property.Viewer, Property.ValueChangeListener {

    private class TextChangedFunction implements JavaScriptFunction {

        private static final long serialVersionUID = 3889013888386596452L;

        @Override
        public void call(JsonArray args) throws JSONException {
            log.debug("TextChangedFunction \"{}\"", args.getString(0));
            getState().text = args.getString(0);
            getPropertyDataSource().setValue(args.getString(0));

            final String textCopy = dataSource.getValue();
            final int cursorPos = (int) args.getNumber(1);
            fireEvent(new FieldEvents.TextChangeEvent(AqlCodeEditor.this) {

                private static final long serialVersionUID = 1L;

                @Override
                public int getCursorPosition() {
                    return cursorPos;
                }

                @Override
                public String getText() {
                    return textCopy;
                }
            });
        }
    }

    private static final long serialVersionUID = 6912362703243923469L;

    private static final Logger log = LoggerFactory.getLogger(AqlCodeEditor.class);

    private int timeout;

    private Property<String> dataSource;

    public AqlCodeEditor() {
        addFunction("textChanged", new TextChangedFunction());
        addStyleName("aql-code-editor");

        AqlCodeEditor.this.setPropertyDataSource(new ObjectProperty<String>("", String.class));

        // init to one so the client (which starts with 0) at initialization always uses
        // the the values provided by the server state
        AqlCodeEditor.this.getState().serverRequestCounter = 1;
    }

    @Override
    public void addTextChangeListener(FieldEvents.TextChangeListener listener) {
        addListener(FieldEvents.TextChangeListener.EVENT_ID, FieldEvents.TextChangeEvent.class,
                listener, FieldEvents.TextChangeListener.EVENT_METHOD);
    }

    @Override
    public Property<String> getPropertyDataSource() {
        return this.dataSource;
    }

    @Override
    protected AqlCodeEditorState getState() {
        return (AqlCodeEditorState) super.getState();
    }

    public String getTextareaStyle() {
        return getState().textareaClass == null ? "" : getState().textareaClass;
    }

    public int getTextChangeTimeout() {
        return this.timeout;
    }

    public String getValue() {
        return dataSource.getValue();
    }

    private TreeMap<String, Integer> mapQueryNodes(List<QueryAttributeDescription> nodes) {
        TreeMap<String, Integer> result = new TreeMap<>();
        Map<Integer, TreeSet<Integer>> alternative2Nodes = new HashMap<>();

        int nodeIdx = 1;
        for (QueryAttributeDescription n : nodes) {
            TreeSet<Integer> orderedNodeSet = alternative2Nodes.get(n.getAlternative());
            if (orderedNodeSet == null) {
                orderedNodeSet = new TreeSet<>();
                alternative2Nodes.put(n.getAlternative(), orderedNodeSet);
            }
            // the nodes list is already ordered by the occurrence of the node in the AQL
            // query stream
            orderedNodeSet.add(nodeIdx++);
        }

        for (TreeSet<Integer> orderedNodeSet : alternative2Nodes.values()) {
            int newID = 1;
            for (Integer idx : orderedNodeSet) {
                result.put("" + idx, newID);
                newID++;
            }
        }

        return result;
    }

    @Override
    public void removeTextChangeListener(FieldEvents.TextChangeListener listener) {
        removeListener(FieldEvents.TextChangeListener.EVENT_ID, FieldEvents.TextChangeEvent.class,
                listener);
    }

    public void setError(BadRequestError error) {
        getState().errors.clear();
        if (error != null) {
            if (error.getAqLSyntaxError() != null) {
                getState().errors.add(new ParseError(error.getAqLSyntaxError()));
            }
            if (error.getAqLSemanticError() != null) {
                getState().errors.add(new ParseError(error.getAqLSemanticError()));
            }
        }
        markAsDirty();
    }

    public void setInputPrompt(String prompt) {
        getState().inputPrompt = prompt;
        markAsDirty();
    }

    public void setNodes(List<QueryAttributeDescription> nodes) {
        getState().nodeMappings.clear();
        getState().optionalNodes.clear();
        if (nodes != null) {
            getState().nodeMappings.putAll(mapQueryNodes(nodes));
            int i = 1;
            for (QueryAttributeDescription d : nodes) {
              if (Objects.equal(true, d.getOptional())) {
                getState().optionalNodes.add(i);
              }
              i++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        if (newDataSource == null) {
            throw new IllegalArgumentException("Data source must not be null");
        }
        if (newDataSource.getType() != String.class) {
            throw new IllegalArgumentException("Data source must be of type String");
        }

        if (this.dataSource instanceof Property.ValueChangeNotifier) {
            ((Property.ValueChangeNotifier) this.dataSource).removeValueChangeListener(this);
        }

        this.dataSource = newDataSource;

        if (newDataSource instanceof Property.ValueChangeNotifier) {
            ((Property.ValueChangeNotifier) this.dataSource).addValueChangeListener(this);
        }

    }

    public void setTextareaStyle(String style) {
        getState().textareaClass = "".equals(style) ? null : style;
    }

    public void setTextChangeTimeout(int timeout) {
        callFunction("setChangeDelayTime", timeout);
        this.timeout = timeout;
    }

    public void setValue(String value) {
        dataSource.setValue(value);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        log.debug("valueChange \"{}\"/\"{}", event.getProperty().getValue(),
                this.dataSource.getValue());
        String oldText = getState().text;
        String newText = this.dataSource.getValue();

        if (oldText == null || !oldText.equals(newText)) {
            getState().text = newText;
            // this is a server side state change and we have to explicitly tell the client
            // we want to change the text
            getState().serverRequestCounter++;

            log.debug("invalidating \"{}\"/\"{}\"", oldText, getState().text);
            markAsDirty();
        }
    }

}
