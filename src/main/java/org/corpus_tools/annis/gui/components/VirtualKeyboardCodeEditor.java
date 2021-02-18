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
package org.corpus_tools.annis.gui.components;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.shared.JavaScriptExtensionState;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import elemental.json.impl.JreJsonNull;
import org.corpus_tools.annis.gui.components.codemirror.AqlCodeEditor;
import org.json.JSONException;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@StyleSheet({ "keyboard.css" })
@JavaScript({ "vaadin://jquery.js", "keyboard.js", "virtualkeyboard_codeeditor.js" })
public class VirtualKeyboardCodeEditor extends AbstractJavaScriptExtension {

    private class UpdateLangJSFunction implements JavaScriptFunction {

        /**
         * 
         */
        private static final long serialVersionUID = 3828724167697774835L;

        public UpdateLangJSFunction() {}

        @Override
        public void call(JsonArray arguments) throws JSONException {
            if (arguments.length() > 0 && !(arguments.get(0) instanceof JreJsonNull)) {
                getState().setKeyboardLayout(arguments.getString(0));
            } else {
                getState().setKeyboardLayout(null);
            }
        }
    }

    public static class VKState extends JavaScriptExtensionState {

        /**
         * 
         */
        private static final long serialVersionUID = 4249522664300067205L;
        private String keyboardLayout = "";

        public String getKeyboardLayout() {
            return keyboardLayout;
        }

        public void setKeyboardLayout(String keyboardLayout) {
            this.keyboardLayout = keyboardLayout;
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 6695962630237599561L;

    public VirtualKeyboardCodeEditor() {
        addFunction("updateLang", new UpdateLangJSFunction());
    }

    public void extend(AqlCodeEditor target) {
        super.extend(target);

    }

    @Override
    protected VKState getState() {
        return (VKState) super.getState();
    }

    @Override
    protected Class<? extends ClientConnector> getSupportedParentType() {
        return AqlCodeEditor.class;
    }

    public void setKeyboardLayout(String layout) {
        getState().setKeyboardLayout(layout);
    }

    public void show() {
        callFunction("show");
    }

}
