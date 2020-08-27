/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.libgui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class AnnisUser implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6391673569021316652L;


  private final String userName;
  /** Never store the password on the disk */
  private transient String password;
  /** Never stor the JWT token on the disk */
  private transient String token;

  public AnnisUser(String userName, String password, String token) {
    this.userName = userName;
    this.password = password;
    this.token = token;
  }


  public String getUserName() {
    return userName;
  }

  public String getToken() {
    return token;
  }

  public String getPassword() {
    return password;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // explicitly set the password to "null" to make findbugs happy
    this.password = null;
    this.token = null;
  }
}
