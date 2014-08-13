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
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a user.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class User
{
  private String name;
  private String passwordHash;
  private List<String> groups = new ArrayList<>();
  private List<String> permissions = new ArrayList<>();
 
  public User()
  {
    
  }
  
  public User(String name)
  {
    this.name = name;
  }
  
  /**
   * Constructs a represention from the contents of an an ANNIS
   * user file. 
   * @param props
   */
  public User(Properties props)
  {
    this.name = props.getProperty("name", "");
    this.passwordHash = props.getProperty("password");
    
    String groupsRaw = props.getProperty("groups", "");
    for(String g : Splitter.on(",").trimResults().omitEmptyStrings().split(groupsRaw))
    {
      this.groups.add(g);
    }
    // add manual permissions
    String permsRaw = props.getProperty("permissions", "");
    for(String g : Splitter.on(",").trimResults().omitEmptyStrings().split(permsRaw))
    {
      this.permissions.add(g);
    }
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getPasswordHash()
  {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash)
  {
    this.passwordHash = passwordHash;
  }

  public List<String> getGroups()
  {
    return groups;
  }

  public void setGroups(List<String> groups)
  {
    this.groups = groups;
  }

  public List<String> getPermissions()
  {
    return permissions;
  }

  public void setPermissions(List<String> permissions)
  {
    this.permissions = permissions;
  }
  
  
  
  
  /**
   * Constructs a represention that is equal to the content of an ANNIS
   * user file.
   * @return 
   */
  public Properties toProperties()
  {
    Properties props = new Properties();
    props.put("password", passwordHash);
    props.put("groups", Joiner.on(',').join(groups));
    if(permissions != null && !permissions.isEmpty())
    {
      props.put("permissions", Joiner.on(',').join(permissions));
    }
    return props;
  }
  
}
