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
package annis.gui.admin;

import annis.gui.admin.view.GroupListView;
import annis.gui.converter.CommaSeperatedStringConverterSet;
import annis.security.Group;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.Action;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupManagementPanel extends Panel
  implements GroupListView
{

  private final List<GroupListView.Listener> listeners = new LinkedList<>();

  private final Grid groupsGrid = new Grid();

  private final TextField txtGroupName;

  private final ProgressBar progress;

  private final HorizontalLayout actionLayout;

  private final BeanContainer<String, Group> groupsContainer = new BeanContainer<>(
    Group.class);

  private final IndexedContainer corpusContainer = new IndexedContainer();

  public GroupManagementPanel()
  {
    groupsContainer.setBeanIdProperty("name");

    progress = new ProgressBar();
    progress.setCaption("Loading group list");
    progress.setIndeterminate(true);
    progress.setVisible(false);

    GeneratedPropertyContainer generated = new GeneratedPropertyContainer(
      groupsContainer);
    generated.addGeneratedProperty("edit", new PropertyValueGenerator<String>()
    {

      @Override
      public String getValue(Item item, Object itemId, Object propertyId)
      {
        return "Edit";
      }

      @Override
      public Class<String> getType()
      {
        return String.class;
      }
    });
    groupsGrid.setContainerDataSource(generated);
    groupsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    groupsGrid.setSizeFull();
    groupsGrid.setColumns("name", "edit", "corpora");
    
    Grid.HeaderRow filterRow = groupsGrid.appendHeaderRow();
    TextField groupFilterField = new TextField();
    groupFilterField.setInputPrompt("Filter");
    groupFilterField.addTextChangeListener(new FieldEvents.TextChangeListener()
    {

      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        groupsContainer.removeContainerFilters("name");
        if(!event.getText().isEmpty())
        {
          groupsContainer.addContainerFilter(new SimpleStringFilter("name",
            event.getText(), true, false));
        }
      }
    });
    filterRow.getCell("name").setComponent(groupFilterField);
    
    TextField corpusFilterField = new TextField();
    corpusFilterField.setInputPrompt("Filter by corpus");
    corpusFilterField.addTextChangeListener(new FieldEvents.TextChangeListener()
    {

      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        groupsContainer.removeContainerFilters("corpora");
        if(!event.getText().isEmpty())
        {
          groupsContainer.addContainerFilter(new StringPatternInSetFilter("corpora", event.getText()));
        }
      }
    });
    filterRow.getCell("corpora").setComponent(corpusFilterField);
    
    Grid.Column editColumn = groupsGrid.getColumn("edit");
    editColumn.setRenderer(new ButtonRenderer(
      new ClickableRenderer.RendererClickListener()
      {

        @Override
        public void click(ClickableRenderer.RendererClickEvent event)
        {
          Group g = groupsContainer.getItem(event.getItemId()).getBean();

          FieldGroup fields = new FieldGroup(groupsContainer.getItem(event.
              getItemId()));
          fields.addCommitHandler(new GroupCommitHandler(g.getName()));

          EditSingleGroup edit = new EditSingleGroup(fields, corpusContainer);

          Window w = new Window("Edit group \"" + g.getName() + "\"");
          w.setContent(edit);
          w.setModal(true);
          w.setWidth("500px");
          w.setHeight("250px");
          UI.getCurrent().addWindow(w);
        }
      }));

    Grid.Column corporaColumn = groupsGrid.getColumn("corpora");;
    corporaColumn.setConverter(new CommaSeperatedStringConverterSet());

    txtGroupName = new TextField();
    txtGroupName.setInputPrompt("New group name");

    Button btAddNewGroup = new Button("Add new group");
    btAddNewGroup.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        handleAdd();
      }
    });
    btAddNewGroup.addStyleName(ChameleonTheme.BUTTON_DEFAULT);

    Button btDeleteGroup = new Button("Delete selected group(s)");
    btDeleteGroup.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        // get selected groups
        Set<String> selectedGroups = new TreeSet<>();
        for (Object id : groupsGrid.getSelectedRows())
        {
          selectedGroups.add((String) id);
        }
        groupsGrid.getSelectionModel().reset();
        for (GroupListView.Listener l : listeners)
        {
          l.deleteGroups(selectedGroups);
        }
      }
    });

    actionLayout = new HorizontalLayout(txtGroupName,
      btAddNewGroup, btDeleteGroup);

    VerticalLayout layout = new VerticalLayout(actionLayout, progress,
      groupsGrid);
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

  private void handleAdd()
  {
    for (GroupListView.Listener l : listeners)
    {
      l.addNewGroup(txtGroupName.getValue());
    }
  }

  @Override
  public void addListener(GroupListView.Listener listener)
  {
    listeners.add(listener);
  }

  @Override
  public void setGroupList(Collection<Group> groups)
  {
    groupsContainer.removeAllItems();
    groupsContainer.addAll(groups);
  }

  @Override
  public void emptyNewGroupNameTextField()
  {
    txtGroupName.setValue("");
  }

  @Override
  public void addAvailableCorpusNames(Collection<String> corpusNames)
  {
    for (String c : corpusNames)
    {
      corpusContainer.addItem(c);
    }
  }

  @Override
  public void setLoadingAnimation(boolean show)
  {
    progress.setVisible(show);
    groupsGrid.setVisible(!show);
    actionLayout.setEnabled(!show);
  }

  public class AddGroupHandler implements Action.Handler
  {

    private final Action enterKeyShortcutAction
      = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

    private final Object registeredTarget;

    public AddGroupHandler(Object registeredTarget)
    {
      this.registeredTarget = registeredTarget;
    }

    @Override
    public Action[] getActions(Object target, Object sender)
    {
      return new Action[]
      {
        enterKeyShortcutAction
      };
    }

    @Override
    public void handleAction(Action action, Object sender, Object target)
    {
      if (action == enterKeyShortcutAction && target == registeredTarget)
      {
        handleAdd();
      }
    }
  }

  private class GroupCommitHandler implements FieldGroup.CommitHandler
  {

    private String groupName;

    public GroupCommitHandler(String groupName)
    {
      this.groupName = groupName;
    }

    @Override
    public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException
    {

    }

    @Override
    public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException
    {
      for (GroupListView.Listener l : listeners)
      {
        l.groupUpdated(groupsContainer.getItem(groupName).getBean());
      }
    }

  }
  
  public static class StringPatternInSetFilter implements Container.Filter
  {
    private final Object propertyId;
    private final String pattern;

    public StringPatternInSetFilter(Object propertyId, String pattern)
    {
      this.propertyId = propertyId;
      this.pattern = pattern.toLowerCase();
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
    {
      Property<?> p = item.getItemProperty(propertyId);
      if(p.getValue() instanceof Set)
      {
        Set val = (Set) p.getValue();
        for(Object o : val)
        {
          if((o.toString().toLowerCase()).contains(pattern))
          {
            return true;
          }
       }
      }
      else
      {
        throw new UnsupportedOperationException();
      }
      
      return false;
    }

    @Override
    public boolean appliesToProperty(Object propertyId)
    {
      return this.propertyId.equals(propertyId);
    }
    
  }

}
