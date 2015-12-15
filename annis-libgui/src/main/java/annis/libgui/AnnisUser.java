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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class AnnisUser implements Serializable
{
  private transient Client client;
  
  private final String userName;
  /** Never store the password on the disk */
  private transient String password;
  private final boolean remote;
  
  public AnnisUser(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
    this.remote = false;
  }
  
  public AnnisUser(String userName, String password, boolean remote)
  {
    this.userName = userName;
    this.password = password;
    this.remote = remote;
  }

  
  public String getUserName()
  {
    return userName;
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    // explicitly set the password to "null" to make findbugs happy
    this.password = null;
  }


  public Client getClient() throws LoginDataLostException
  {
    if(client == null)
    {
      if(remote == true)
      {
        // treat as anonymous user
        client = Helper.createRESTClient();
      }
      else
      {
        if(password == null)
        {
          throw new LoginDataLostException();
        }
        client = Helper.createRESTClient(userName, password);
      }
    }
    return client;
  }


  /**
   * True if the user a remote user, thus cannot e.g. logout by itself
   * @return 
   */
  public boolean isRemote()
  {
    return remote;
  }
}
