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

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.BeanContainer;
import com.vaadin.v7.data.util.GeneratedPropertyContainer;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.PropertyValueGenerator;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.ui.Grid;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.renderers.ButtonRenderer;
import com.vaadin.v7.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.corpus_tools.annis.api.model.Group;
import org.corpus_tools.annis.gui.admin.view.GroupListView;
import org.corpus_tools.annis.gui.converter.CommaSeperatedStringConverterSet;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GroupManagementPanel extends Panel implements GroupListView {

    public class AddGroupHandler implements Action.Handler {

        private static final long serialVersionUID = -4855324916352101750L;

        private final Action enterKeyShortcutAction = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

        private final Object registeredTarget;

        public AddGroupHandler(Object registeredTarget) {
            this.registeredTarget = registeredTarget;
        }

        @Override
        public Action[] getActions(Object target, Object sender) {
            return new Action[] { enterKeyShortcutAction };
        }

        @Override
        public void handleAction(Action action, Object sender, Object target) {
            if (action == enterKeyShortcutAction && target == registeredTarget) {
                handleAdd();
            }
        }
    }

    private class GroupCommitHandler implements FieldGroup.CommitHandler {

        private static final long serialVersionUID = -3816526526945191335L;
        private String groupName;

        public GroupCommitHandler(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {
            for (GroupListView.Listener l : listeners) {
                l.groupUpdated(groupsContainer.getItem(groupName).getBean());
            }
        }

        @Override
        public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {

        }

    }

    public static class StringPatternInSetFilter implements Container.Filter {
        /**
         * 
         */
        private static final long serialVersionUID = 4056375452214888364L;
        private final Object propertyId;
        private final String pattern;

        public StringPatternInSetFilter(Object propertyId, String pattern) {
            this.propertyId = propertyId;
            this.pattern = pattern.toLowerCase();
        }

        @Override
        public boolean appliesToProperty(Object propertyId) {
            return this.propertyId.equals(propertyId);
        }

        @Override
        public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
            Property<?> p = item.getItemProperty(propertyId);
            if (p.getValue() instanceof Set) {
                Set<?> val = (Set<?>) p.getValue();
                for (Object o : val) {
                    if ((o.toString().toLowerCase()).contains(pattern)) {
                        return true;
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }

            return false;
        }

    }

    private static final long serialVersionUID = -4159690760269810930L;

    private final List<GroupListView.Listener> listeners = new LinkedList<>();

    private final Grid groupsGrid = new Grid();

    private final TextField txtGroupName;

    private final ProgressBar progress;

    private final HorizontalLayout actionLayout;

    private final BeanContainer<String, Group> groupsContainer = new BeanContainer<>(Group.class);

    private final IndexedContainer corpusContainer = new IndexedContainer();

    public GroupManagementPanel() {
        groupsContainer.setBeanIdProperty("name");

        progress = new ProgressBar();
        progress.setCaption("Loading group list");
        progress.setIndeterminate(true);
        progress.setVisible(false);

        GeneratedPropertyContainer generated = new GeneratedPropertyContainer(groupsContainer);
        generated.addGeneratedProperty("edit", new PropertyValueGenerator<String>() {

            private static final long serialVersionUID = -4167110257296656329L;

            @Override
            public Class<String> getType() {
                return String.class;
            }

            @Override
            public String getValue(Item item, Object itemId, Object propertyId) {
                return "Edit";
            }
        });
        groupsGrid.setContainerDataSource(generated);
        groupsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        groupsGrid.setSizeFull();
        groupsGrid.setColumns("name", "edit", "corpora");

        Grid.HeaderRow filterRow = groupsGrid.appendHeaderRow();
        TextField groupFilterField = new TextField();
        groupFilterField.setInputPrompt("Filter");
        groupFilterField.addTextChangeListener(event -> {
            groupsContainer.removeContainerFilters("name");
            if (!event.getText().isEmpty()) {
                groupsContainer.addContainerFilter(new SimpleStringFilter("name", event.getText(), true, false));
            }
        });
        filterRow.getCell("name").setComponent(groupFilterField);

        TextField corpusFilterField = new TextField();
        corpusFilterField.setInputPrompt("Filter by corpus");
        corpusFilterField.addTextChangeListener(event -> {
            groupsContainer.removeContainerFilters("corpora");
            if (!event.getText().isEmpty()) {
                groupsContainer.addContainerFilter(new StringPatternInSetFilter("corpora", event.getText()));
            }
        });
        filterRow.getCell("corpora").setComponent(corpusFilterField);

        Grid.Column editColumn = groupsGrid.getColumn("edit");
        editColumn.setRenderer(new ButtonRenderer(event -> {
            Group g = groupsContainer.getItem(event.getItemId()).getBean();

            FieldGroup fields = new FieldGroup(groupsContainer.getItem(event.getItemId()));
            fields.addCommitHandler(new GroupCommitHandler(g.getName()));

            EditSingleGroup edit = new EditSingleGroup(fields, corpusContainer);

            Window w = new Window("Edit group \"" + g.getName() + "\"");
            w.setContent(edit);
            w.setModal(true);
            w.setWidth("500px");
            w.setHeight("250px");
            UI.getCurrent().addWindow(w);
        }));

        Grid.Column corporaColumn = groupsGrid.getColumn("corpora");

        corporaColumn.setConverter(new CommaSeperatedStringConverterSet());

        txtGroupName = new TextField();
        txtGroupName.setInputPrompt("New group name");

        Button btAddNewGroup = new Button("Add new group");
        btAddNewGroup.addClickListener(event -> handleAdd());
        btAddNewGroup.addStyleName(ChameleonTheme.BUTTON_DEFAULT);

        Button btDeleteGroup = new Button("Delete selected group(s)");
        btDeleteGroup.addClickListener(event -> {
            // get selected groups
            Set<String> selectedGroups = new TreeSet<>();
            for (Object id : groupsGrid.getSelectedRows()) {
                selectedGroups.add((String) id);
            }
            groupsGrid.getSelectionModel().reset();
            for (GroupListView.Listener l : listeners) {
                l.deleteGroups(selectedGroups);
            }
        });

        actionLayout = new HorizontalLayout(txtGroupName, btAddNewGroup, btDeleteGroup);

        VerticalLayout layout = new VerticalLayout(actionLayout, progress, groupsGrid);
        layout.setSizeFull();
        layout.setExpandRatio(groupsGrid, 1.0f);
        layout.setExpandRatio(progress, 1.0f);
        layout.setSpacing(true);
        layout.setMargin(new MarginInfo(true, false, false, false));

        layout.setComponentAlignment(actionLayout, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(progress, Alignment.TOP_CENTER);

        setContent(layout);
        setSizeFull();

        addActionHandler(new AddGroupHandler(txtGroupName));
    }

    @Override
    public void addAvailableCorpusNames(Collection<String> corpusNames) {
        for (String c : corpusNames) {
            corpusContainer.addItem(c);
        }
    }

    @Override
    public void addListener(GroupListView.Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void emptyNewGroupNameTextField() {
        txtGroupName.setValue("");
    }

    private void handleAdd() {
        for (GroupListView.Listener l : listeners) {
            l.addNewGroup(txtGroupName.getValue());
        }
    }

    @Override
    public void setGroupList(Collection<Group> groups) {
        groupsContainer.removeAllItems();
        groupsContainer.addAll(groups);
    }

    @Override
    public void setLoadingAnimation(boolean show) {
        progress.setVisible(show);
        groupsGrid.setVisible(!show);
        actionLayout.setEnabled(!show);
    }

}
