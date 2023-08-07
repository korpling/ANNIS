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

package org.corpus_tools.annis.gui.admin.model;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.corpus_tools.annis.api.model.Group;
import org.corpus_tools.annis.gui.converter.CaseSensitiveOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * A model for groups.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class GroupManagement implements Serializable {

  private static final long serialVersionUID = 7099096327534717378L;

  private final Logger log = LoggerFactory.getLogger(GroupManagement.class);

  private final Map<String, Group> groups = new TreeMap<>(CaseSensitiveOrder.INSTANCE);

  private ApiClientProvider apiClientProvider;

  public void clear() {
    groups.clear();
  }

  public void createOrUpdateGroup(Group newGroup) {
    if (apiClientProvider != null) {
      WebClient client = apiClientProvider.getWebClient();
      try {
        client.put().uri("/groups/{name}", newGroup.getName()).bodyValue(newGroup).retrieve()
            .bodyToMono(Void.class).block();
        groups.put(newGroup.getName(), newGroup);
      } catch (WebClientResponseException ex) {
        log.warn("Could not update group", ex);
      }

    }
  }

  public void deleteGroup(String groupName) {
    if (apiClientProvider != null) {
      WebClient client = apiClientProvider.getWebClient();
      try {
        client.delete().uri("/groups/{name}", groupName).retrieve().bodyToMono(Void.class).block();
        groups.remove(groupName);
      } catch (WebClientResponseException ex) {
        log.warn("Could not update group", ex);
      }

    }
  }

  public boolean fetchFromService() {
    if (apiClientProvider != null) {
      WebClient client = apiClientProvider.getWebClient();
      groups.clear();
      try {
        Iterable<Group> remoteGroups =
            client.get().uri("/groups").retrieve().bodyToFlux(Group.class).toIterable();
        for (Group g : remoteGroups) {
          groups.put(g.getName(), g);
        }
        return true;
      } catch (WebClientResponseException ex) {
        log.error("Could not get the list of groups", ex);
      }
    }
    return false;
  }

  public Group getGroup(String groupName) {
    return groups.get(groupName);
  }

  public ImmutableSet<String> getGroupNames() {
    return ImmutableSet.copyOf(groups.keySet());
  }

  public Collection<Group> getGroups() {
    return groups.values();
  }

  public ApiClientProvider getWebResourceProvider() {
    return apiClientProvider;
  }

  public void setWebResourceProvider(ApiClientProvider apiClientProvider) {
    this.apiClientProvider = apiClientProvider;
  }
}
