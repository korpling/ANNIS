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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.Validate;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.RolePermissionResolverAware;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.LoggerFactory;

/**
 * A realm for the property based user authentification and authorization pattern
 * used by ANNIS.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ANNISUserRealm extends AuthorizingRealm implements RolePermissionResolverAware
{
  private final static org.slf4j.Logger log = LoggerFactory.getLogger(ANNISUserRealm.class);
  
  private String resourcePath;
  private String defaultUserRole = "user";
  private String anonymousUser = "anonymous";

  public ANNISUserRealm()
  {
    // define a default credentials matcher
    HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME);
    matcher.setHashIterations(1);
    setCredentialsMatcher(matcher);
  }
  
  private Properties getPropertiesForUser(String userName)
  {
     // load user info from file
    if(resourcePath != null)
    {
        
      File userDir = new File(resourcePath, "users");
      if(userDir.isDirectory())
      {
        // get the file which corresponds to the user
        File userFile = new File(userDir.getAbsolutePath(), userName);
        FileInputStream userFileIO = null;
        try
        {
          Properties userProps = new Properties();

          userFileIO = new FileInputStream(userFile);
          userProps.load(userFileIO);
          
          return userProps;

        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
        finally
        {
          if(userFileIO != null)
          {
            try
            {
              userFileIO.close();
            }
            catch (IOException ex)
            {
              log.error(null, ex);
            }
          }
        }

      }
    } // end if resourcePath not null
    
    return null;
  }
  
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
  {
    Validate.isInstanceOf(String.class, principals.getPrimaryPrincipal());
    String userName = (String) principals.getPrimaryPrincipal();
    
    
    Set<String> roles = new TreeSet<String>();
    roles.add(userName);  
    if(!userName.equals(anonymousUser))
    {
      Properties userProps = getPropertiesForUser(userName);
    
      String groupsRaw = userProps.getProperty("groups", "").trim();
      roles.addAll(Arrays.asList(groupsRaw.split("\\s*,\\s*")));
      
      roles.add(defaultUserRole);
    }
    return new SimpleAuthorizationInfo(roles);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
  {
    Validate.isInstanceOf(String.class, token.getPrincipal());
    
    String userName = (String) token.getPrincipal();
    if(userName.equals(anonymousUser))
    {
      // for anonymous users the user name equals the Password, so hash the user name
      Sha256Hash hash = new Sha256Hash(userName);
      return new SimpleAuthenticationInfo(userName, hash.getBytes(), ANNISUserRealm.class.getName());
    }
    
    Properties userProps = getPropertiesForUser(userName);
    if(userProps != null)
    {
      String password = userProps.getProperty("password");
      if(password != null)
      {
        if(password.startsWith("$"))
        {
          Shiro1CryptFormat fmt = new Shiro1CryptFormat();
          Hash hashCredentials = fmt.parse(password);
          if(hashCredentials instanceof SimpleHash)
          {
            SimpleHash simpleHash = (SimpleHash) hashCredentials;

            Validate.isTrue(simpleHash.getIterations() == 1, "Hash iteration count must be 1 for every password hash!");

            // actually set the information from the user file
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
              userName,
              simpleHash.getBytes(), ANNISUserRealm.class.getName());
            info.setCredentialsSalt(simpleHash.getSalt());
            return info;
          }
        }
        else
        {
          // fallback unsalted hex hash
          SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
            token.getPrincipal(), password, ANNISUserRealm.class.getName());
          return info;
        }
        
      }
    }
    return null;
  }
  

  public String getResourcePath()
  {
    return resourcePath;
  }

  public void setResourcePath(String resourcePath)
  {
    this.resourcePath = resourcePath;
  }

  public String getDefaultUserRole()
  {
    return defaultUserRole;
  }

  public void setDefaultUserRole(String defaultUserRole)
  {
    this.defaultUserRole = defaultUserRole;
  }

  public String getAnonymousUser()
  {
    return anonymousUser;
  }

  public void setAnonymousUser(String anonymousUser)
  {
    this.anonymousUser = anonymousUser;
  }
  
}
