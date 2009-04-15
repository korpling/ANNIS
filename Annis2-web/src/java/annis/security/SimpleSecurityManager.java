/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.security;

import annisservice.AnnisService;
import annisservice.AnnisServiceFactory;
import annisservice.ifaces.AnnisCorpus;
import annisservice.ifaces.AnnisCorpusSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;
import javax.naming.NamingException;

/**
 * A simple security manager using plain text files (Java-properties). <br>
 * 
 * Every user is part of a group. Every group has certain access rights.
 * Ther is a "all-corpora" group.
 * 
 * @author thomas
 */
public class SimpleSecurityManager implements AnnisSecurityManager
{

  public final static String CONFIG_PATH = "config_path";
  private Properties properties;

  public AnnisUser login(String userName, String password) throws NamingException, AuthenticationException
  {
    if(properties == null || !properties.containsKey(CONFIG_PATH))
    {
      throw new NamingException("don't know where to search for the user configuration, " +
        "properties not set");
    }

    if(userName != null && !"".equals(userName) && password != null && !"".equals(password))
    {
      File configDir = new File(properties.getProperty(CONFIG_PATH));
      if(configDir.isDirectory())
      {
        File usersDir = new File(configDir.getAbsolutePath() + "/users/");
        File groupsFile = new File(configDir.getAbsolutePath() + "/groups");

        if(groupsFile.isFile() && usersDir.isDirectory())
        {
          try
          {
            Properties groupProps = new Properties();
            groupProps.load(new FileInputStream(groupsFile));

            File fileOfUser = new File(usersDir.getAbsolutePath() + "/" + userName);
            if(fileOfUser.isFile())
            {
              AnnisUser user = new AnnisUser(userName);

              user.load(new FileInputStream(fileOfUser));

              // check password
              String passwordAsSHA = Crypto.calculateSHAHash(password);

              if(passwordAsSHA.equalsIgnoreCase(user.getPassword()))
              {

                // get corpora by traversing the groups of this user
                String groupNames = user.getProperty(AnnisUser.GROUPS);
                if(groupNames != null)
                {

                  String[] allGroups = groupNames.split("\\s*,\\s*");
                  HashSet<Long> userCorpora = new HashSet<Long>();
                  for(String g : allGroups)
                  {
                    if("*".equals(g))
                    {
                      // superuser, has all available corpora
                      userCorpora.addAll(getAllAvailableCorpora());

                      break;
                    }
                    else
                    {
                      String groupCorporaAsString = groupProps.getProperty(g, "");
                      String[] corporaOfGroup = groupCorporaAsString.split("\\s*,\\s*");

                      for(String c : corporaOfGroup)
                      {
                        try
                        {
                          long cID = Long.parseLong(c);
                          userCorpora.add(cID);
                        }
                        catch(NumberFormatException ex)
                        {
                          // ignore
                        }
                      }

                    }
                  }

                  // create user object
                  user.setCorpusIdList(new LinkedList<Long>(userCorpora));
                 
                  // finally return the user
                  return user;
                }
              }
            }
          }
          catch(NoSuchAlgorithmException ex)
          {
            Logger.getLogger(SimpleSecurityManager.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch(UnsupportedEncodingException ex)
          {
            Logger.getLogger(SimpleSecurityManager.class.getName()).log(Level.SEVERE, null, ex);
          }
          catch(IOException ex)
          {
            Logger.getLogger(SimpleSecurityManager.class.getName()).log(Level.SEVERE, null, ex);
          }
        }

      }

    }
    // not authorized
    throw new AuthenticationException();
  }

  private List<Long> getAllAvailableCorpora()
  {
    LinkedList<Long> result = new LinkedList();
    try
    {
      String url = properties.getProperty("AnnisRemoteService.URL", "rmi://localhost:4711/AnnisService");
      AnnisService service = AnnisServiceFactory.getClient(url);
      AnnisCorpusSet corpusSet = service.getCorpusSet();
      int i = 0;
      for(AnnisCorpus corpus : corpusSet)
      {
        result.add(corpus.getId());
      }
    }
    catch(Exception e)
    {
      // fallback...
      for(long i = 1; i <= 100; i++)
      {
        result.add(i);
      }
    }
    return result;
  }

  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  public void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException
  {
    File configDir = new File(properties.getProperty(CONFIG_PATH));
    if(configDir.isDirectory())
    {
      File usersDir = new File(configDir.getAbsolutePath() + "/users/");
      File fileOfUser = new File(usersDir.getAbsolutePath() + "/" + user.getUserName());
      if(fileOfUser.isFile())
      {        
        try
        {
          user.store(new FileOutputStream(fileOfUser, false), "");
        }
        catch(IOException ex)
        {
          Logger.getLogger(SimpleSecurityManager.class.getName()).log(Level.SEVERE, 
            "", ex);
        }
      }

    }
  }
}