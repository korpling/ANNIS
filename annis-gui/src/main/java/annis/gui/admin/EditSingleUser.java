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
package annis.gui.admin;

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

/**
 * UI to edit the properties of a single user.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@DesignRoot
public class EditSingleUser extends Panel
{

  private Label lblUser;

  private Button btSave;

  private Button btCancel;

  private PopupTwinColumnSelect groupSelector;

  private PopupTwinColumnSelect permissionSelector;

  private OptionalDateTimeField expirationSelector;

  public EditSingleUser(final FieldGroup fields,
    IndexedContainer groupsContainer,
    IndexedContainer permissionsContainer)
  {
    Design.read(EditSingleUser.this);

    groupSelector.setSelectableContainer(groupsContainer);
    permissionSelector.setSelectableContainer(permissionsContainer);
    
    lblUser.setValue((String) fields.getItemDataSource().getItemProperty("name").getValue());

    // bind the fields
    fields.bind(groupSelector, "groups");
    fields.bind(permissionSelector, "permissions");
    fields.bind(expirationSelector, "expires");
    
    // events
    btSave.addClickListener(new Button.ClickListener()
    {
      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        try
        {
          fields.commit();
        }
        catch (FieldGroup.CommitException ex)
        {
        }


        HasComponents parent = getParent();
        if (parent instanceof Window)
        {
          ((Window) parent).close();
        }
      }
    });
    btSave.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);

    btCancel.addClickListener(new Button.ClickListener()
    {

      @Override
      public void buttonClick(Button.ClickEvent event)
      {
        fields.discard();

        HasComponents parent = getParent();
        if (parent instanceof Window)
        {
          ((Window) parent).close();
        }
      }
    });

    expirationSelector.setCheckboxCaption("expires");

  }

}
