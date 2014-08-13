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
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISUserConfigurationManager
{
  private final static org.slf4j.Logger log = LoggerFactory.getLogger(ANNISUserConfigurationManager.class);

  private String resourcePath;
  private File groupsFile;
  
  private Map<String, String> groups;
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
      
      Properties propGroups = new Properties();
      try(FileInputStream inStream = new FileInputStream(groupsFile);)
      {
        
        propGroups.load(inStream);
        lastTimeReloaded = new Date(groupsFile.lastModified());
        
        groups = new HashMap<>();
        for(String k : propGroups.stringPropertyNames())
        {
          groups.put(k, propGroups.getProperty(k));
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
  
  public ImmutableMap<String, String> getGroups()
  {
    checkConfiguration();
    return ImmutableMap.copyOf(groups);
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
