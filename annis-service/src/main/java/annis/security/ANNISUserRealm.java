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
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * A realm for the property based user authentification and authorization
 * pattern used by ANNIS.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISUserRealm extends AuthorizingRealm implements
  RolePermissionResolverAware
{

  private ANNISUserConfigurationManager confManager;

  private String defaultUserRole = Group.DEFAULT_USER_ROLE;

  private String anonymousUser = Group.ANONYMOUS;

  public ANNISUserRealm()
  {
    // define a default credentials matcher
    HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(
      Sha256Hash.ALGORITHM_NAME);
    matcher.setHashIterations(1);
    setCredentialsMatcher(matcher);
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
    PrincipalCollection principals)
  {
    Validate.isInstanceOf(String.class, principals.getPrimaryPrincipal());
    String userName = (String) principals.getPrimaryPrincipal();

    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

    User user = confManager.getUser(userName);
    
    if(user != null)
    {
      // only add any user role/permission if account is not expired
      if(user.getExpires() == null || user.getExpires().isAfterNow())
      { 
        info.addRole(userName);

        info.addRoles(user.getGroups());
        info.addRole(defaultUserRole);
        // add the permission to create url short IDs from every IP
        info.addStringPermission("shortener:create:*");       
        // add any manual given permissions
        info.addStringPermissions(user.getPermissions());
      }
    }
    else if(userName.equals(anonymousUser))
    {
      info.addRole(anonymousUser);
      if (confManager.getUseShortenerWithoutLogin() != null)
      {
        // add the permission to create url short IDs from the trusted IPs
        for(String trustedIPs : confManager.getUseShortenerWithoutLogin())
        {
          info.addStringPermission("shortener:create:" + trustedIPs.replaceAll(
            "[.:]", "_"));
        }
      }

    }
    return info;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
  {
    Validate.isInstanceOf(String.class, token.getPrincipal());

    String userName = (String) token.getPrincipal();
    if (userName.equals(anonymousUser))
    {
      // for anonymous users the user name equals the Password, so hash the user name
      Sha256Hash hash = new Sha256Hash(userName);
      return new SimpleAuthenticationInfo(userName, hash.getBytes(),
        ANNISUserRealm.class.getName());
    }

    User user = confManager.getUser(userName);
    if (user != null)
    { 
      String passwordHash = user.getPasswordHash();
      if (passwordHash != null)
      {
        if (passwordHash.startsWith("$"))
        {
          Shiro1CryptFormat fmt = new Shiro1CryptFormat();
          Hash hashCredentials = fmt.parse(passwordHash);
          if (hashCredentials instanceof SimpleHash)
          {
            SimpleHash simpleHash = (SimpleHash) hashCredentials;

            Validate.isTrue(simpleHash.getIterations() == 1,
              "Hash iteration count must be 1 for every password hash!");

            // actually set the information from the user file
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
              userName,
              simpleHash.getBytes(), ANNISUserRealm.class.getName());
            info.setCredentialsSalt(new SerializableByteSource(simpleHash.getSalt()));
            return info;
          }
        }
        else
        {
          // fallback unsalted hex hash
          SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(
            token.getPrincipal(), passwordHash, ANNISUserRealm.class.getName());
          return info;
        }

      }
    }
    return null;
  }
  
  public boolean updateUser(User user)
  {
    if(getConfManager().writeUser(user))
    {
      clearCacheForUser(user.getName());
      return true;
    }
    return false;
  }
  
  public void clearCacheForUser(String userName)
  {
    SimplePrincipalCollection principals = new SimplePrincipalCollection(userName, 
      ANNISUserRealm.class.getName());
    clearCache(principals);
  }

  public ANNISUserConfigurationManager getConfManager()
  {
    return confManager;
  }

  public void setConfManager(ANNISUserConfigurationManager confManager)
  {
    this.confManager = confManager;
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
