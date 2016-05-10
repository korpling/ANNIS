/*
 * Copyright 2012 SFB 632.
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
package annis.security;

import java.util.Collection;
import java.util.HashSet;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISRolePermissionResolver implements RolePermissionResolver
{

  private ANNISUserConfigurationManager confManager;

  @Override
  public Collection<Permission> resolvePermissionsInRole(String roleString)
  {
    HashSet<Permission> perms = new HashSet<>();

    if ("*".equals(roleString))
    {
      perms.add(new WildcardPermission("query:*:*"));
      perms.add(new WildcardPermission("meta:*"));
    }
    else
    {
      if (Group.DEFAULT_USER_ROLE.equals(roleString))
      {
        // every user can read/write its user configuration
        perms.add(new WildcardPermission("admin:*:userconfig"));
      }
      else if (Group.ANONYMOUS.equals(roleString))
      {
        // every anonymous user can read its user configuration
        perms.add(new WildcardPermission("admin:read:userconfig"));
      }
      

      // add all corpora for this role
      Group group = confManager.getGroups().get(roleString);
      if (group != null)
      {
        for (String c : group.getCorpora())
        {
          perms.add(new WildcardPermission("query:*:" + c));
          perms.add(new WildcardPermission("meta:" + c));
        }
      }
    }
    return perms;
  }

  public ANNISUserConfigurationManager getConfManager()
  {
    return confManager;
  }

  public void setConfManager(ANNISUserConfigurationManager confManager)
  {
    this.confManager = confManager;
  }

}
