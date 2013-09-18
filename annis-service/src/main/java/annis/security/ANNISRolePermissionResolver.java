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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ANNISRolePermissionResolver implements RolePermissionResolver
{
  private final static org.slf4j.Logger log = LoggerFactory.getLogger(ANNISRolePermissionResolver.class);
  
  private String defaultUserRole = "user";
  private String anonymousUserRole = "anonymous";
  
  private File groupsFile;
  private Properties groups;
  private Date lastTimeReloaded = null;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  
  private void checkConfiguration()
  {
    boolean reload = false;
    
    // get a read lock that does not block anyone else to find out if we need an update
    lock.readLock().lock();
    try
    {
      if(groupsFile == null)
      {
        return;
      }

      if( lastTimeReloaded == null || FileUtils.isFileNewer(groupsFile, lastTimeReloaded))
      {
        reload = true;
      }
    }
    finally
    {
      lock.readLock().unlock();
    }
    
    if(reload)
    {
      reloadGroups();
    }
  }
  
  private void reloadGroups()
  {
    lock.writeLock().lock();
    try
    {
      FileInputStream inStream = null;
      groups = new Properties();
      try
      {
        inStream = new FileInputStream(groupsFile);
        groups.load(inStream);
        lastTimeReloaded = new Date(groupsFile.lastModified());
      }
      catch (IOException ex)
      {
        log.error(null, ex);
      }
      finally
      {
        if(inStream != null)
        {
          try
          {
            inStream.close();
          }
          catch(IOException ex)
          {
            log.error("Could not close group file stream", ex);
          }
        }
      }
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }
  
  @Override
  public Collection<Permission> resolvePermissionsInRole(String roleString)
  {
    HashSet<Permission> perms = new HashSet<Permission>();
    
    if("*".equals(roleString))
    {
      perms.add(new WildcardPermission("query:*:*"));
      perms.add(new WildcardPermission("meta:*"));
    }
    else
    {
      if(roleString.equals(defaultUserRole))
      {
        // every user can get/set its user configuration
        perms.add(new WildcardPermission("admin:*:userconfig"));
      }
      else if(roleString.equals(anonymousUserRole))
      {
        // every anonymous user can get/set its user configuration
        perms.add(new WildcardPermission("admin:read:userconfig"));
      }
      checkConfiguration();

      lock.readLock().lock();
      try
      {
        String corporaRaw = groups.getProperty(roleString);
        if(corporaRaw != null)
        {
          String[] corpora = corporaRaw.split("\\s*,\\s*");
          for(String c : corpora)
          {
            perms.add(new WildcardPermission("query:*:" + c.trim()));
            perms.add(new WildcardPermission("meta:" + c.trim()));
          }
        }
      }
      finally
      {
        lock.readLock().unlock();
      }
    }
    
    return perms;
  }

  public String getResourcePath()
  {
    return groupsFile.getAbsolutePath();
  }

  public void setResourcePath(String resourcePath)
  {
    lock.writeLock().lock();
    try
    {
      this.groupsFile = new File(resourcePath, "groups");
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  public String getDefaultUserRole()
  {
    return defaultUserRole;
  }

  public void setDefaultUserRole(String defaultUserRole)
  {
    this.defaultUserRole = defaultUserRole;
  }

  public String getAnonymousUserRole()
  {
    return anonymousUserRole;
  }

  public void setAnonymousUserRole(String anonymousUserRole)
  {
    this.anonymousUserRole = anonymousUserRole;
  }
  
  
  
  
  
}
