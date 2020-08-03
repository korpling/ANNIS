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
package annis.gui.admin.controller;

import annis.gui.CriticalServiceQueryException;
import annis.gui.ServiceQueryException;
import annis.gui.admin.model.CorpusManagement;
import annis.gui.admin.model.GroupManagement;
import annis.gui.admin.view.GroupListView;
import annis.gui.admin.view.UIView;
import annis.security.Group;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.FutureCallback;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GroupController implements GroupListView.Listener, UIView.Listener {

  private static final long serialVersionUID = -6490890559663078065L;

  private final GroupManagement model;

  private final CorpusManagement corpusModel;

  private final GroupListView view;

  private final UIView uiView;

  private boolean isLoggedIn = false;

  private boolean viewIsActive = false;

  public GroupController(GroupManagement model, CorpusManagement corpusModel, GroupListView view,
      UIView uiView, boolean isLoggedIn) {
    this.model = model;
    this.corpusModel = corpusModel;
    this.view = view;
    this.uiView = uiView;
    this.isLoggedIn = isLoggedIn;

    this.view.addListener(GroupController.this);
    this.uiView.addListener(GroupController.this);
  }

  @Override
  public void addNewGroup(String groupName) {
    if (groupName == null || groupName.isEmpty()) {
      uiView.showError("Group name is empty", null);
    } else if (model.getGroup(groupName) != null) {
      uiView.showError("Group already exists", null);
    } else {
      Group g = new Group(groupName);
      model.createOrUpdateGroup(g);
      view.setGroupList(model.getGroups());
      view.emptyNewGroupNameTextField();

      updateUserUI();
    }
  }

  private void clearModel() {
    model.clear();
    corpusModel.clear();
    view.setGroupList(model.getGroups());
  }

  @Override
  public void deleteGroups(Set<String> groupName) {
    for (String g : groupName) {
      model.deleteGroup(g);
    }
    view.setGroupList(model.getGroups());

    if (groupName.size() == 1) {
      uiView.showInfo("Group \"" + groupName.iterator().next() + "\" was deleted", null);
    } else {
      uiView.showInfo("Deleted groups: " + Joiner.on(", ").join(groupName), null);
    }

    updateUserUI();
  }

  private void fetchDataFromService() {
    view.setLoadingAnimation(true);
    uiView.runInBackground(() -> {
      boolean result = model.fetchFromService();
      corpusModel.fetchFromService();
      return result;
    }, new FutureCallback<Boolean>() {

      @Override
      public void onFailure(Throwable ex) {
        view.setLoadingAnimation(false);
        if (ex instanceof CriticalServiceQueryException) {
          uiView.showWarning(ex.getMessage(),
              ((CriticalServiceQueryException) ex).getDescription());
        } else if (ex instanceof ServiceQueryException) {
          uiView.showInfo(ex.getMessage(), ((ServiceQueryException) ex).getDescription());
        } else {
          uiView.showWarning("Cannot get the group list", ex.getMessage());
          view.setGroupList(new LinkedList<Group>());
        }
        updateUserUI();
      }

      @Override
      public void onSuccess(Boolean result) {
        view.setLoadingAnimation(false);
        if (result) {
          view.setGroupList(model.getGroups());
        } else {
          uiView.showWarning("Cannot get the group list", null);
          view.setGroupList(new LinkedList<Group>());
        }

        view.addAvailableCorpusNames(corpusModel.getCorpora());

        updateUserUI();
      }
    });

  }

  @Override
  public void groupUpdated(Group user) {
    model.createOrUpdateGroup(user);
  }

  @Override
  public void loadedTab(Object selectedTab) {
    viewIsActive = selectedTab == view;
    if (isLoggedIn && viewIsActive) {
      fetchDataFromService();
    }
  }

  @Override
  public void loginChanged(boolean isLoggedIn) {
    this.isLoggedIn = isLoggedIn;
    if (model.getWebResourceProvider() != null) {
      model.getWebResourceProvider().invalidateWebResource();
    }
    if (corpusModel.getClientProvider() != null) {
      corpusModel.getClientProvider().invalidateWebResource();
    }
    if (isLoggedIn && viewIsActive) {
      fetchDataFromService();
    } else {
      clearModel();
    }
  }

  private void updateUserUI() {
    Set<String> names = new TreeSet<>(model.getGroupNames());
    names.add("*");
  }

}
