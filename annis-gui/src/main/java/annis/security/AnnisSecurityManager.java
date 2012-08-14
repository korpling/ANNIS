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

import java.io.IOException;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;

public interface AnnisSecurityManager
{

  public final static String FALLBACK_USER = "demo";
  
  /**
   * Login with username and password
   * @param userName
   * @param password
   * @param demoFallbackEnabled If true login will not complain if no 
   *                            configuration is set and instead allow all 
   *                            corpora for the "demo" user.
   * @return
   * @throws NamingException
   * @throws AuthenticationException 
   */
  public AnnisUser login(String userName, String password, boolean demoFallbackEnabled) throws AuthenticationException;

  public void setProperties(Properties properties);

  public void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException;
  
  public void updateUserCorpusList(AnnisUser user, boolean demoFallbackEnabled) throws AuthenticationException;
}