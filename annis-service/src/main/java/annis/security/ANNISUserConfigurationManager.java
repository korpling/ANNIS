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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
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

  private final Map<String, Group> groups = new TreeMap<>();

  private Date lastTimeReloaded = null;
  
  private Set<String> useShortenerWithoutLogin;
  
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
      reloadGroupsFromFile();
    }
  }

  private void reloadGroupsFromFile()
  {
    lock.writeLock().lock();
    try
    {

      Properties propGroups = new Properties();
      try (FileInputStream inStream = new FileInputStream(groupsFile);)
      {

        propGroups.load(inStream);
        lastTimeReloaded = new Date(groupsFile.lastModified());

        groups.clear();
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
    ImmutableMap<String, Group> result = ImmutableMap.of();
    lock.readLock().lock();
    try
    {
      result = ImmutableMap.copyOf(groups);
    }
    finally
    {
      lock.readLock().unlock();
    }
    return result;
  }
  
  public boolean writeGroup(Group group)
  {
    if (groupsFile != null)
    {

      lock.writeLock().lock();
      try
      {
        // make sure to have the newest version of the groups
        reloadGroupsFromFile();
        
        // update/create the new group
        groups.put(group.getName(), group);
        
        return writeGroupFile();
        
      }
      finally
      {
        lock.writeLock().unlock();
      }
    } // end if resourcePath not null
    return false;
  }
  
  private boolean writeGroupFile()
  {
    if (groupsFile != null)
    {

      lock.writeLock().lock();
      try
      {
        
        Properties props = new Properties();
        for(Group g : groups.values())
        {
          props.put(g.getName(), Joiner.on(',').join(g.getCorpora()));
        }
        
        try(FileOutputStream outStream = new FileOutputStream(groupsFile))
        {
          props.store(outStream, "");
          outStream.close();

          // update the last modification time
          lastTimeReloaded = new Date(groupsFile.lastModified());
          return true;
        }
        catch(IOException ex)
        {
          log.error("Could not write groups file", ex);
        }
      }
      finally
      {
        lock.writeLock().unlock();
      }
    } // end if resourcePath not null
    return false;
  }
  
  /**
   * Writes the user to the disk
   * @param user
   * @return True if successful.
   */
  public boolean writeUser(User user)
  {
    // save user info to file
    if (resourcePath != null)
    {

      lock.writeLock().lock();
      try
      {
        File userDir = new File(resourcePath, "users");
        if (userDir.isDirectory())
        {
          // get the file which corresponds to the user
          File userFile = new File(userDir.getAbsolutePath(), user.getName());
          Properties props = user.toProperties();
          try (FileOutputStream out = new FileOutputStream(userFile))
          {
            props.store(out, "");
            return true;
          }
          catch (IOException ex)
          {
            log.error("Could not write users file", ex);
          }          
        }
      }
      finally
      {
        lock.writeLock().unlock();
      }
    } // end if resourcePath not null
    return false;
  }
  
  /**
   * Deletes the user from the disk
   * @param userName
   * @return True if successful.
   */
  public boolean deleteUser(String userName)
  {
    // load user info from file
    if (resourcePath != null)
    {

      lock.writeLock().lock();
      try
      {
        File userDir = new File(resourcePath, "users");
        if (userDir.isDirectory())
        {
          // get the file which corresponds to the user
          File userFile = new File(userDir.getAbsolutePath(), userName);
          return userFile.delete();
        }
      }
      finally
      {
        lock.writeLock().unlock();
      }
    } // end if resourcePath not null
    return false;
  }
  
  /**
   * Deletes the group from the disk
   * @param groupName
   * @return True if successful.
   */
  public boolean deleteGroup(String groupName)
  {
    if (groupsFile != null)
    {
      lock.writeLock().lock();
      try
      {
        reloadGroupsFromFile();
        groups.remove(groupName);
        return writeGroupFile();
      }
      finally
      {
        lock.writeLock().unlock();
      }
    } // end if resourcePath not null
    return false;
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
    try
    {
      this.resourcePath = resourcePath;
      this.groupsFile = new File(resourcePath, "groups");
    }
    finally
    {
      lock.writeLock().unlock();
    }
  }

  public Set<String> getUseShortenerWithoutLogin()
  {
    return useShortenerWithoutLogin;
  }

  public void setUseShortenerWithoutLogin(Set<String> useShortenerWithoutLogin)
  {
    this.useShortenerWithoutLogin = useShortenerWithoutLogin;
  }

  
}
