/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.security;

import annis.service.ifaces.AnnisCorpus;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class AnnisUser extends Properties
{

  public final static String SURNAME = "surname";
  public final static String GIVEN_NAME = "given_name";
  public final static String GROUPS = "groups";
  public final static String PASSWORD = "password";
  
  
  private Map<Long,AnnisCorpus> corpusList;
  private String userName = "";
  
  public String getSurName()
  {
    return getProperty(SURNAME);
  }

  public void setSurName(String firstName)
  {
    setProperty(SURNAME, firstName);
  }

  /** (Almost) empty constructor, use this to load a stored property file*/
  public AnnisUser(String userName)
  {
    this.corpusList = new TreeMap<Long,AnnisCorpus>();
    this.userName = userName;
  }

  /** legacy construct */
  public AnnisUser(String userName, String surName, String givenName)
  {
    this.corpusList = new TreeMap<Long,AnnisCorpus>();
    this.userName = userName;
    setSurName(surName);
    setGivenName(givenName);
  }

  public Map<Long, AnnisCorpus> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(Map<Long, AnnisCorpus> corpusList)
  {
    this.corpusList = corpusList;
  }

  public Set<Long> getCorpusIdList()
  {
    return corpusList.keySet();
  }
  
  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public String getGivenName()
  {
    return getProperty(GIVEN_NAME);
  }

  public void setGivenName(String givenName)
  {
    setProperty(GIVEN_NAME, givenName);
  }

  public String getPassword()
  {
    return getProperty(PASSWORD);
  }

  public void setPassword(String password)
  {
    setProperty(PASSWORD, password);
  }

  @Override
  public boolean equals(Object obj)
  {
    if(obj == null)
    {
      return false;
    }
    if(getClass() != obj.getClass())
    {
      return false;
    }
    final AnnisUser other = (AnnisUser) obj;
    if((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 83 * hash + (this.userName != null ? this.userName.hashCode() : 0);
    return hash;
  }
  
  
}
