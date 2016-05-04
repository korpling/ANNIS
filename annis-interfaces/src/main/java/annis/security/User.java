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

import annis.CaseSensitiveOrder;
import annis.adapter.DateTimeAdapter;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.io.Serializable;
import java.util.Properties;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Represents a user.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@XmlRootElement
public class User implements Serializable
{
  
  private String name;
  private String passwordHash;
  private TreeSet<String> groups = new TreeSet<>(CaseSensitiveOrder.INSTANCE);
  private TreeSet<String> permissions = new TreeSet<>(CaseSensitiveOrder.INSTANCE);
  private DateTime expires;
 
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
   * @param name
   * @param props
   */
  public User(String name, Properties props)
  {
    this.name = name;
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
    
    String dateRaw = props.getProperty("expires");
    if(dateRaw != null)
    {
      try
      {
        expires = DateTime.parse(dateRaw);
      }
      catch(IllegalArgumentException ex)
      {
        expires = null;
      }
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

  @XmlElement(name="group")
  public TreeSet<String> getGroups()
  {
    return groups;
  }

  public void setGroups(TreeSet<String> groups)
  {
    this.groups = groups;
  }

  public TreeSet<String> getPermissions()
  {
    return permissions;
  }

  @XmlElement(name="permission")
  public void setPermissions(TreeSet<String> permissions)
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
    if(passwordHash != null)
    {
      props.put("password", passwordHash);
    }
    if(groups != null && !groups.isEmpty())
    {
      props.put("groups", Joiner.on(',').join(groups));
    }
    if(permissions != null && !permissions.isEmpty())
    {
      props.put("permissions", Joiner.on(',').join(permissions));
    }
    if(expires != null)
    {
      props.put("expires", ISODateTimeFormat.date().print(expires));
    }
    return props;
  }

  @XmlJavaTypeAdapter(DateTimeAdapter.class)
  public DateTime getExpires()
  {
    return expires;
  }

  public void setExpires(DateTime expires)
  {
    this.expires = expires;
  }
  
  
}
