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

package annis.gui.admin.view;

import annis.security.Group;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface GroupListView
{
  public void addListener(Listener listener);
  
  public void setGroupList(Collection<Group> groups);
  
  public void emptyNewGroupNameTextField();
  
  public void addAvailableCorpusNames(Collection<String> corpusNames);
  
  public void setLoadingAnimation(boolean show);
  
  public interface Listener
  {
    public void groupUpdated(Group user);
    
    public void addNewGroup(String groupName);
    public void deleteGroups(Set<String> groupName);
  }
}
