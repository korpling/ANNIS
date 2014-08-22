/*
 * Copyright 2012 SFB 632.
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

import java.io.UnsupportedEncodingException;
import javax.servlet.ServletRequest;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentificate with a static anonymous user account when no real 
 * authentification was given.
 * If there is an authentification given this class will act like it's
 * parent class. The default value for password and user name are "anonymous".
 * You can change them with the {@link #anonymousUser} and {@link #anonymousPassword}
 * properties.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class BasicAuthOrAnonymousFilter extends BasicHttpAuthenticationFilter
{

  private static final Logger log = LoggerFactory.getLogger(BasicAuthOrAnonymousFilter.class);
  
  private String anonymousUser = Group.ANONYMOUS;
  private String anonymousPassword = Group.ANONYMOUS; 
  
  @Override
  protected String getAuthzHeader(ServletRequest request)
  {
    String result = super.getAuthzHeader(request);
    if(result == null)
    {
      try
      {
        // create an new one with anonymous user and password
        result = "Basic " 
          + Base64.encodeToString((anonymousUser + ":" + anonymousPassword).getBytes("UTF-8"));
      }
      catch (UnsupportedEncodingException ex)
      {
        log.error(null, ex);
      }
    }
    
    return result;
  }

  public String getAnonymousUser()
  {
    return anonymousUser;
  }

  public void setAnonymousUser(String anonymousUser)
  {
    this.anonymousUser = anonymousUser;
  }

  public String getAnonymousPassword()
  {
    return anonymousPassword;
  }

  public void setAnonymousPassword(String anonymousPassword)
  {
    this.anonymousPassword = anonymousPassword;
  }
  
}
