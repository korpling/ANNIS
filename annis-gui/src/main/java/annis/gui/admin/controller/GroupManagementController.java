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

package annis.gui.admin.controller;

import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.view.GroupManagementView;
import annis.gui.admin.view.UIView;
import annis.security.Group;
import annis.security.User;
import com.sun.jersey.api.client.WebResource;
import java.util.LinkedList;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupManagementController implements GroupManagementView.Listener,
  UIView.Listener
{
  private final GroupManagement model;
  private final GroupManagementView view;
  private final UIView uiView;

  public GroupManagementController(GroupManagement model,
    GroupManagementView view, UIView uiView)
  {
    this.model = model;
    this.view = view;
    this.uiView = uiView;
    
    this.view.addListener(GroupManagementController.this);
    this.uiView.addListener(GroupManagementController.this);
  }

  private void updateGroupList()
  {
    if(model.fetchGroups())
    {
      view.setGroupList(model.getGroups());
    }
    
    else
    {
      uiView.showError("Cannot get the group list");
      view.setGroupList(new LinkedList<Group>());
    }
  }

  @Override
  public void attached()
  {
    updateGroupList();
  }

  @Override
  public void loginChanged(WebResource annisRootResource)
  {
    model.setRootResource(annisRootResource);
    updateGroupList();
  }
  
  
  
  
}
