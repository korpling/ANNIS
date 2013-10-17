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
package annis.libgui;

import com.sun.jersey.api.client.Client;

public class AnnisUser
{
  private transient Client client;
  private String userName = "";
  private boolean remote = false;
  

  public AnnisUser(String userName, Client client)
  {
    this.userName = userName;
    this.client = client;
  }
  
  public AnnisUser(String userName, Client client, boolean remote)
  {
    this.userName = userName;
    this.client = client;
    this.remote = remote;
  }

  
  public String getUserName()
  {
    return userName;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public Client getClient()
  {
    return client;
  }

  public void setClient(Client client)
  {
    this.client = client;
  }

  /**
   * True if the user a remote user, thus cannot e.g. logout by itself
   * @return 
   */
  public boolean isRemote()
  {
    return remote;
  }

  public void setRemote(boolean remote)
  {
    this.remote = remote;
  }

  
  
}
