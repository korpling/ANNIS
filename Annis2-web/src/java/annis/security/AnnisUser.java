package annis.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AnnisUser extends Properties
{

  public final static String SURNAME = "surname";
  public final static String GIVEN_NAME = "given_name";
  public final static String GROUPS = "groups";
  public final static String PASSWORD = "password";
  
  
  private List<Long> corpusIdList;
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
    this.corpusIdList = new ArrayList<Long>();
    this.userName = userName;
  }

  /** legacy construct */
  public AnnisUser(String userName, String surName, String givenName)
  {
    this.corpusIdList = new ArrayList<Long>();
    this.userName = userName;
    setSurName(surName);
    setGivenName(givenName);
  }

  public List<Long> getCorpusIdList()
  {
    return corpusIdList;
  }

  public void setCorpusIdList(List<Long> corpusIdList)
  {
    this.corpusIdList = corpusIdList;
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
}
