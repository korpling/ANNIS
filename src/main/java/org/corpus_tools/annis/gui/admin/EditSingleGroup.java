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
package org.corpus_tools.annis.gui.admin;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.converter.Converter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * UI to edit the properties of a single user.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@DesignRoot
public class EditSingleGroup extends Panel {

    private static final long serialVersionUID = 4801977262300488107L;

    private Label lblGroup;

    private Button btSave;

    private Button btCancel;

    private PopupTwinColumnSelect corporaSelector;

    public EditSingleGroup(final FieldGroup fields, IndexedContainer corporaContainer) {
        Design.read(EditSingleGroup.this);

        corporaSelector.setSelectableContainer(corporaContainer);
        corporaSelector.setConverter(new Converter<Set, List>() {

          @Override
          public List convertToModel(Set value, Class<? extends List> targetType, Locale locale)
              throws ConversionException {
            return new ArrayList(value);
          }

          @Override
          public Set convertToPresentation(List value, Class<? extends Set> targetType,
              Locale locale) throws ConversionException {
            return new TreeSet(value);
          }

          @Override
          public Class<List> getModelType() {
            return List.class;
          }

          @Override
          public Class<Set> getPresentationType() {
            return Set.class;
          }
        });

        lblGroup.setValue((String) fields.getItemDataSource().getItemProperty("name").getValue());

        // bind the fields
        fields.bind(corporaSelector, "corpora");

        // events
        btSave.addClickListener(event -> {
            try {
                fields.commit();
            } catch (FieldGroup.CommitException ex) {
            }

            HasComponents parent = getParent();
            if (parent instanceof Window) {
                ((Window) parent).close();
            }
        });
        btSave.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);

        btCancel.addClickListener(event -> {
            fields.discard();

            HasComponents parent = getParent();
            if (parent instanceof Window) {
                ((Window) parent).close();
            }
        });
    }

}
