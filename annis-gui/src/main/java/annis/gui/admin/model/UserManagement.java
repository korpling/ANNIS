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

package annis.gui.admin.model;

import annis.CaseSensitiveOrder;
import annis.security.User;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A model that manages users.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class UserManagement implements Serializable {
    private static final long serialVersionUID = 9003127192150738183L;

    private ApiClientProvider apiClientProvider;

    private final Map<String, User> users = new TreeMap<>(CaseSensitiveOrder.INSTANCE);
    private final TreeSet<String> usedGroupNames = new TreeSet<>();
    private final TreeSet<String> usedPermissions = new TreeSet<>();

    private final Logger log = LoggerFactory.getLogger(UserManagement.class);

    public void clear() {
        users.clear();
        usedGroupNames.clear();
        usedPermissions.clear();
    }

    public boolean createOrUpdateUser(User newUser) {
        if (apiClientProvider != null) {
            WebResource res = apiClientProvider.getWebResource().path("admin/users").path(newUser.getName());
            try {
                res.put(newUser);
                users.put(newUser.getName(), newUser);

                updateUsedGroupNamesAndPermissions();
                return true;
            } catch (UniformInterfaceException ex) {
                log.warn("Could not update user", ex);
            }
        }
        return false;
    }

    public void deleteUser(String userName) {
        if (apiClientProvider != null) {
            WebResource res = apiClientProvider.getWebResource().path("admin/users").path(userName);
            res.delete();
            users.remove(userName);
            updateUsedGroupNamesAndPermissions();
        }
    }

    public boolean fetchFromService() {
        if (apiClientProvider != null) {
            WebResource res = apiClientProvider.getWebResource().path("admin/users");
            users.clear();
            usedGroupNames.clear();
            usedPermissions.clear();
            try {
                List<User> list = res.get(new GenericType<List<User>>() {});
                for (User u : list) {
                    users.put(u.getName(), u);
                    usedGroupNames.addAll(u.getGroups());
                    usedPermissions.addAll(u.getPermissions());
                }
                return true;
            } catch (UniformInterfaceException ex) {
                log.error("Could not get the list of users", ex);
            }
        }
        return false;
    }

    public TreeSet<String> getUsedGroupNames() {
        return usedGroupNames;
    }

    public TreeSet<String> getUsedPermissions() {
        return usedPermissions;
    }

    public User getUser(String userName) {
        return users.get(userName);
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public ApiClientProvider getWebResourceProvider() {
        return apiClientProvider;
    }

    public User setPassword(String userName, String newPassword) {
        User newUser = null;

        if (apiClientProvider != null) {
            WebResource res = apiClientProvider.getWebResource().path("admin/users").path(userName).path("password");
            newUser = res.post(User.class, newPassword);
            if (newUser != null) {
                users.put(newUser.getName(), newUser);
            }
        }
        return newUser;

    }

    public void setWebResourceProvider(ApiClientProvider apiClientProvider) {
        this.apiClientProvider = apiClientProvider;
    }

    private void updateUsedGroupNamesAndPermissions() {
        usedGroupNames.clear();
        usedPermissions.clear();
        for (User u : users.values()) {
            usedGroupNames.addAll(u.getGroups());
            usedPermissions.addAll(u.getPermissions());
        }
    }

}
