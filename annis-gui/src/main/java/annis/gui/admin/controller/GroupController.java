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

import annis.gui.CriticalServiceQueryException;
import annis.gui.ServiceQueryException;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.view.GroupListView;
import annis.gui.admin.view.UIView;
import annis.gui.admin.view.UserListView;
import annis.gui.admin.model.CorpusManagement;
import annis.security.Group;
import com.google.common.base.Joiner;
import com.sun.jersey.api.client.WebResource;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class GroupController implements GroupListView.Listener,
  UIView.Listener
{
  private final GroupManagement model;
  private final CorpusManagement corpusModel;
  private final GroupListView view;
  private final UIView uiView;
  private final UserListView userView;

  public GroupController(GroupManagement model,
    CorpusManagement corpusModel,
    GroupListView view, UIView uiView,
    UserListView userView)
  {
    this.model = model;
    this.corpusModel = corpusModel;
    this.view = view;
    this.uiView = uiView;
    this.userView = userView;
    
    this.view.addListener(GroupController.this);
    this.uiView.addListener(GroupController.this);
  }

  private void fetchDataFromService()
  {
    if(model.fetchFromService())
    {
      view.setGroupList(model.getGroups());
    }
    
    else
    {
      uiView.showError("Cannot get the group list", null);
      view.setGroupList(new LinkedList<Group>());
    }
    
    try
    {
      corpusModel.fetchFromService();
      view.setAvailableCorpusNames(corpusModel.getCorpusNames());
    }
    catch(CriticalServiceQueryException ex)
    {
      uiView.showWarning(ex.getMessage(), ex.getDescription());
    }
    catch (ServiceQueryException ex)
    {
      uiView.showInfo(ex.getMessage(), ex.getDescription());
    }
    
    updateUserUI();
    
  }
  
  private void updateUserUI()
  {
    Set<String> names = new TreeSet<>(model.getGroupNames());
    names.add("*");
    userView.setAvailableGroupNames(names);
  }

  @Override
  public void attached()
  {
    fetchDataFromService();
  }

  @Override
  public void loginChanged(WebResource annisRootResource)
  {
    model.setRootResource(annisRootResource);
    corpusModel.setRootResource(annisRootResource);
    fetchDataFromService();
  }

  @Override
  public void groupUpdated(Group user)
  {
    model.createOrUpdateGroup(user);
  }

  @Override
  public void addNewGroup(String groupName)
  {
    if(groupName == null || groupName.isEmpty())
    {
      uiView.showError("Group name is empty", null);
    }
    else if(model.getGroup(groupName) != null)
    {
      uiView.showError("Group already exists", null);
    }
    else
    {
      Group g = new Group(groupName);
      model.createOrUpdateGroup(g);
      view.setGroupList(model.getGroups());
      view.emptyNewGroupNameTextField();
      
      updateUserUI();
    }
  }

  @Override
  public void deleteGroups(Set<String> groupName)
  {
    for(String g : groupName)
    {
      model.deleteGroup(g);
    }
    view.setGroupList(model.getGroups());
    
    if(groupName.size() == 1)
    {
      uiView.showInfo("Group \"" + groupName.iterator().next() +  "\" was deleted", null);
    }
    else
    {
      uiView.showInfo("Deleted groups: " + Joiner.on(", ").join(groupName), null);
    }
    
    updateUserUI();
  }

  @Override
  public void selectedTabChanged(Object selectedTab)
  {
    if(selectedTab == view)
    {
      fetchDataFromService();
    }
  }
  
  
  
  
}
