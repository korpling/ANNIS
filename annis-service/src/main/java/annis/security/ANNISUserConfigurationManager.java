/*
 * Copyright 2014 SFB 632.
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

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * Allows to read and write user and group information in a centralized and safe
 * way.
 *
 * It has a global lock to ensure that read/write operations are not interfering
 * which each other.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISUserConfigurationManager
{

  private final static org.slf4j.Logger log = LoggerFactory.getLogger(
    ANNISUserConfigurationManager.class);

  private String resourcePath;

  private File groupsFile;

  private Map<String, Group> groups;

  private Date lastTimeReloaded = null;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private void checkConfiguration()
  {
    boolean reload = false;

    // get a read lock that does not block anyone else to find out if we need an update
    lock.readLock().lock();
    try
    {
      if (groupsFile == null)
      {
        return;
      }

      if (lastTimeReloaded == null || FileUtils.isFileNewer(groupsFile,
        lastTimeReloaded))
      {
        reload = true;
      }
    }
    finally
    {
      lock.readLock().unlock();
    }

    if (reload)
    {
      reloadGroups();
    }
  }

  private void reloadGroups()
  {
    lock.writeLock().lock();
    try
    {

      Properties propGroups = new Properties();
      try (FileInputStream inStream = new FileInputStream(groupsFile);)
      {

        propGroups.load(inStream);
        lastTimeReloaded = new Date(groupsFile.lastModified());

        groups = new HashMap<>();
        for (String k : propGroups.stringPropertyNames())
        {
          groups.put(k, new Group(k, propGroups.getProperty(k)));
        }

      }
      catch (IOException ex)
      {
        log.error(null, ex);
      }
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  public ImmutableMap<String, Group> getGroups()
  {
    checkConfiguration();
    return ImmutableMap.copyOf(groups);
  }

  public User getUser(String userName)
  {
    // load user info from file
    if (resourcePath != null)
    {

      lock.readLock().lock();
      try
      {
        File userDir = new File(resourcePath, "users");
        if (userDir.isDirectory())
        {
          // get the file which corresponds to the user
          File userFile = new File(userDir.getAbsolutePath(), userName);
          return getUserFromFile(userFile);
        }
      }
      finally
      {
        lock.readLock().unlock();
      }
    } // end if resourcePath not null

    return null;
  }
  
  /**
   * Internal helper function to parse a user file.
   * It assumes the calling function already has handled the locking.
   * @param userFile
   * @return 
   */
  private User getUserFromFile(File userFile)
  {
    if (userFile.isFile() && userFile.canRead())
    {
      try (FileInputStream userFileIO = new FileInputStream(userFile);)
      {
        Properties userProps = new Properties();
        userProps.load(userFileIO);
        return new User(userFile.getName(), userProps);
      }
      catch (IOException ex)
      {
        log.error(null, ex);
      }
    }
    return null;
  }

  public List<User> listAllUsers()
  {
    List<User> result = new LinkedList<>();
    // load user info from file
    if (resourcePath != null)
    {

      lock.readLock().lock();
      try
      {
        File userDir = new File(resourcePath, "users");
        if (userDir.isDirectory())
        {
          // get all the files within this directory
          for(File f : userDir.listFiles())
          {
            // the filename is the user name, so check it
            User u = getUserFromFile(f);
            if(u != null)
            {
              result.add(u);
            }
          }
        }
      }
      finally
      {
        lock.readLock().unlock();
      }
    } // end if resourcePath not null

    return result;
  }

  public String getResourcePath()
  {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath)
  {
    lock.writeLock().lock();
    this.resourcePath = resourcePath;
    try
    {
      this.groupsFile = new File(resourcePath, "groups");
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }
}
