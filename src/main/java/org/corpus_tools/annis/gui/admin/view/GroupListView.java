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

package org.corpus_tools.annis.gui.admin.view;

import java.util.Collection;
import java.util.Set;
import org.corpus_tools.annis.api.model.Group;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface GroupListView {
    public interface Listener {
        public void addNewGroup(String groupName);

        public void deleteGroups(Set<String> groupName);

        public void groupUpdated(Group user);
    }

    public void addAvailableCorpusNames(Collection<String> corpusNames);

    public void addListener(Listener listener);

    public void emptyNewGroupNameTextField();

    public void setGroupList(Collection<Group> groups);

    public void setLoadingAnimation(boolean show);
}
