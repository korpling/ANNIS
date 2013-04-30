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
package annis.gui.flatquerybuilder;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.ComboBox;
import java.util.Map;

/**
 *
 * @author klotzmaz
 */
public class SensitiveComboBox extends ComboBox
{
  /*
   * Source Code found on: http://dev.vaadin.com/ticket/7436
   * (On 2013-03-07)
   */
   @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey("filter")) {
            final String text = variables.get("filter").toString();
            fireEvent(new TextChangeEvent(this) {
                @Override
                public String getText() {
                    return text;
                }
                @Override
                public int getCursorPosition() {
                    return text.length();
                }
            });
        }
        super.changeVariables(source, variables);
    }

    public void addListener(TextChangeListener listener) {
        addListener(TextChangeListener.EVENT_ID, TextChangeEvent.class,
                listener, TextChangeListener.EVENT_METHOD);
    }

    public void removeListener(TextChangeListener listener) {
        removeListener(TextChangeListener.EVENT_ID, TextChangeEvent.class,
                listener); 
    }
}