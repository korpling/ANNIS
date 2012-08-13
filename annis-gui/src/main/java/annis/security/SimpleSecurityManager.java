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

import annis.gui.Helper;
import annis.service.objects.AnnisCorpus;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import org.slf4j.LoggerFactory;

/**
 * A simple security manager using plain text files (Java-properties). <br>
 *
 * Every user is part of a group. Every group has certain access rights. Ther is
 * a "all-corpora" group.
 *
 * @author Thomas Krause <krause@informatik.hu-berlin.de>
 */
public class SimpleSecurityManager implements AnnisSecurityManager,
  Serializable
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleSecurityManager.class);
  public final static String CONFIG_PATH = "userconfig_path";
  private Properties properties;

  @Override
  public AnnisUser login(String userName, String password, boolean demoFallbackEnabled)
    throws AuthenticationException
  {
    if (properties == null)
    {
      throw new AuthenticationException("don't know where to search for the user configuration, "
        + "properties not set");
    }
    else if (!demoFallbackEnabled && !properties.containsKey(CONFIG_PATH))
    {
      throw new AuthenticationException("don't know where to search for the user configuration, "
        + "key \"" + CONFIG_PATH + "\" not set and demo fallback not enabled");
    }
    
    
    if (userName != null && !"".equals(userName) && password != null && !"".equals(password))
    {
      AnnisUser user = new AnnisUser(userName);
      updateUserCorpusList(user, demoFallbackEnabled);
      
      File configDir = properties.getProperty(CONFIG_PATH) == null ? null
        : new File(properties.getProperty(CONFIG_PATH));

      if (configDir != null && configDir.isDirectory())
      {
        File usersDir = new File(configDir.getAbsolutePath() + "/users/");
        
        if (usersDir.isDirectory())
        {
          try
          {
            File fileOfUser = new File(usersDir.getAbsolutePath() + "/" + userName);
            if (fileOfUser.isFile())
            {
              FileInputStream userStream = new FileInputStream(fileOfUser);
              try
              {
                user.load(userStream);
              }
              finally
              {
                userStream.close();
              }

              // check password
              String passwordAsSHA = Crypto.calculateSHAHash(password);

              if (passwordAsSHA.equalsIgnoreCase(user.getPassword()))
              {

                updateUserCorpusList(user, demoFallbackEnabled);  

                // finally return the user
                return user;

              }
              else
              {
                throw new AuthenticationException("invalid user name or password");
              }
            }
          }
          catch (NoSuchAlgorithmException ex)
          {
            log.error(null, ex);
          }
          catch (UnsupportedEncodingException ex)
          {
            log.error(null, ex);
          }
          catch (IOException ex)
          {
            log.error(null, ex);
          }
        }

      }
      else if (demoFallbackEnabled && FALLBACK_USER.equals(userName))
      {
        // return the user with all corpora enabled if no configuration is given
        return user;
      }

    }
    // not authorized
    throw new AuthenticationException("invalid user name or password");
  }
  
  
  @Override
  public void updateUserCorpusList(AnnisUser user, boolean demoFallbackEnabled) throws AuthenticationException
  {
    if (properties == null)
    {
      throw new AuthenticationException("don't know where to search for the user configuration, "
        + "properties not set");
    }
    else if (!demoFallbackEnabled && !properties.containsKey(CONFIG_PATH))
    {
      throw new AuthenticationException("don't know where to search for the user configuration, "
        + "key \"" + CONFIG_PATH + "\" not set and demo fallback not enabled");
    }

    user.getCorpusList().clear();

    File configDir = properties.getProperty(CONFIG_PATH) == null ? null
      : new File(properties.getProperty(CONFIG_PATH));

    if (configDir != null && configDir.isDirectory())
    {
      File usersDir = new File(configDir.getAbsolutePath() + "/users/");
      File groupsFile = new File(configDir.getAbsolutePath() + "/groups");

      if (groupsFile.isFile() && usersDir.isDirectory())
      {
        try
        {
          Properties groupProps = new Properties();
          FileInputStream groupsStream = new FileInputStream(groupsFile);
          try
          {
            groupProps.load(groupsStream);
          }
          finally
          {
            groupsStream.close();
          }

          File fileOfUser = new File(usersDir.getAbsolutePath() + "/" + user.getUserName());
          if (fileOfUser.isFile())
          {

            FileInputStream userStream = new FileInputStream(fileOfUser);
            try
            {
              user.load(userStream);
            }
            finally
            {
              userStream.close();
            }

            // get corpora by traversing the groups of this user
            String groupNames = user.getProperty(AnnisUser.GROUPS);
            if (groupNames != null)
            {

              String[] allGroups = groupNames.split("\\s*,\\s*");
              TreeMap<String, AnnisCorpus> userCorpora = new TreeMap<String, AnnisCorpus>();
              for (String g : allGroups)
              {
                if ("*".equals(g))
                {
                  // superuser, has all available corpora
                  userCorpora.putAll(getAllAvailableCorpora());

                  break;
                }
                else
                {
                  String groupCorporaAsString = groupProps.getProperty(g, "");
                  String[] corporaOfGroup = groupCorporaAsString.split("\\s*,\\s*");

                  Map<String, AnnisCorpus> allCorpora = getAllAvailableCorpora();

                  for (String groupCorpusName : corporaOfGroup)
                  {
                    try
                    {
                      AnnisCorpus c = allCorpora.get(groupCorpusName);
                      if (c != null)
                      {
                        userCorpora.put(c.getName(), c);
                      }
                    }
                    catch (NumberFormatException ex)
                    {
                      // ignore
                    }
                  }

                }
              }

              // create user object
              user.setCorpusList(userCorpora);

            }
          }
        }
        catch (UnsupportedEncodingException ex)
        {
          log.error(null, ex);
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }

    }
    else if (demoFallbackEnabled && FALLBACK_USER.equals(user.getUserName()))
    {
      // add all corpora to fallback user
      TreeMap<String, AnnisCorpus> userCorpora = new TreeMap<String, AnnisCorpus>();
      userCorpora.putAll(getAllAvailableCorpora());
      user.setCorpusList(userCorpora);
    }

  }

  private Map<String, AnnisCorpus> getAllAvailableCorpora()
  {
    TreeMap<String, AnnisCorpus> result = new TreeMap<String, AnnisCorpus>();
    try
    {
      String url = properties.getProperty("AnnisWebService.URL", "http://localhost:5711/annis");
      WebResource res = Helper.createAnnisWebResource(url);
      
      List<AnnisCorpus> corpora = res.path("corpora").get(new GenericType<List<AnnisCorpus>>(){});
      for(AnnisCorpus corpus : corpora)
      {
        result.put(corpus.getName(), corpus);
      }
    }
    catch (Exception e)
    {
    }
    return result;
  }

  @Override
  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  @Override
  public void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException
  {
    File configDir = new File(properties.getProperty(CONFIG_PATH));
    if (configDir.isDirectory())
    {
      File usersDir = new File(configDir.getAbsolutePath() + "/users/");
      File fileOfUser = new File(usersDir.getAbsolutePath() + "/" + user.getUserName());
      if (fileOfUser.isFile())
      {
        try
        {
          user.store(new FileOutputStream(fileOfUser, false), "");
        }
        catch (IOException ex)
        {
          log.error(
            "", ex);
        }
      }

    }
  }
}
