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
package org.corpus_tools.annis.gui.admin;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.ItemSorter;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.TwinColSelect;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.gui.CaseSensitiveOrder;
import org.corpus_tools.annis.gui.converter.CommaSeperatedStringConverterSet;
import org.corpus_tools.annis.gui.converter.TreeSetConverter;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@SuppressWarnings("rawtypes")
public class PopupTwinColumnSelect extends CustomField<Set> {

    public static class StringItemSorter implements ItemSorter {

        private static final long serialVersionUID = 1926594984666450829L;

        @Override
        public int compare(Object itemId1, Object itemId2) {
            if (itemId1 instanceof String && itemId2 instanceof String) {
                return CaseSensitiveOrder.INSTANCE.compare((String) itemId1, (String) itemId2);
            } else {
                return 0;
            }
        }

        @Override
        public void setSortProperties(Container.Sortable container, Object[] propertyId, boolean[] ascending) {

        }

    }

    public class UpdateContainerListener implements Property.ValueChangeListener {
        private static final long serialVersionUID = 9023840978945536294L;

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            Object val = event.getProperty().getValue();
            if (val instanceof Collection) {
                for (Object id : (Collection<?>) val) {
                    selectableContainer.addItem(id);
                }
            } else {
                selectableContainer.addItem(val);
            }
            selectableContainer.sort(new Object[0], new boolean[0]);
        }
    }

    private static final long serialVersionUID = -7972139316697398658L;

    private final HorizontalLayout layout;

    private final AbstractTextField txtValue;

    private final TwinColSelect selector;

    private IndexedContainer selectableContainer = new IndexedContainer();

    public PopupTwinColumnSelect() {

        txtValue = createTextField();
        txtValue.setConverter(new CommaSeperatedStringConverterSet());
        txtValue.setWidth("100%");
        txtValue.setPropertyDataSource(PopupTwinColumnSelect.this);

        selector = new TwinColSelect();
        selector.setConverter(new TreeSetConverter());
        selector.setNewItemsAllowed(false);
        selector.setLeftColumnCaption("Available");
        selector.setRightColumnCaption("Selected");
        selector.setContainerDataSource(selectableContainer);
        selector.setWidth("44em");
        selector.setPropertyDataSource(PopupTwinColumnSelect.this);

        PopupView popup = new PopupView("Select", selector);

        layout = new HorizontalLayout(popup, txtValue);
        layout.setExpandRatio(popup, 0.0f);
        layout.setExpandRatio(txtValue, 1.0f);
        layout.setWidth("100%");
        layout.setSpacing(true);

        addValueChangeListener(new UpdateContainerListener());
    }

    protected AbstractTextField createTextField() {
        return new TextField();
    }

    public IndexedContainer getSelectableContainer() {
        return selectableContainer;
    }

    @Override
    public Class<? extends Set> getType() {
        return Set.class;
    }

    @Override
    protected Component initContent() {
        return layout;
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        selector.setCaption(caption);
    }

    public void setSelectableContainer(IndexedContainer selectableContainer) {
        this.selectableContainer = selectableContainer;

        this.selectableContainer.setItemSorter(new StringItemSorter());
        this.selectableContainer.sort(new Object[0], new boolean[0]);

        this.selector.setContainerDataSource(this.selectableContainer);
    }

    @Override
    public void setValue(Set newFieldValue) throws ReadOnlyException {
        // always use a sorted TreeSet
        if (newFieldValue != null && !(newFieldValue instanceof TreeSet<?>)) {
            TreeSet<String> sortedSet = new TreeSet<String>(CaseSensitiveOrder.INSTANCE);
            for (Object v : newFieldValue) {
                if (v instanceof String) {
                    sortedSet.add((String) v);
                }
            }
            newFieldValue = sortedSet;
        }
        super.setValue(newFieldValue);
    }

}
