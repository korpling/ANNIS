/*
 * Copyright 2014 SFB 632.
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

package annis.service.internal;

import annis.administration.AdministrationDao;
import annis.administration.CorpusAdministration;
import annis.dao.AnnisDao;
import annis.security.ANNISSecurityManager;
import annis.security.ANNISUserRealm;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Service to create and query unique IDs for URLs used by the frontend.
 * 
 * The frontend is able to decode it's state using URL query parameters and fragments.
 * Unfortunally these can get quite long so this service allows to shorten
 * these URLs to a unique ID which is stored in a special table in the database.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Path("annis/shortener")
public class URLShortenerImpl
{
  
  private final static Logger log = LoggerFactory.getLogger(URLShortenerImpl.class);
  
  private AdministrationDao adminDao;
  private AnnisDao annisDao;
  private CorpusAdministration corpusAdmin;
  
  @Context
  private HttpServletRequest request;
  
  public void init()
  {
    
  }
  
  /**
   * Takes a URI and returns an ID.
   * 
   * In order to access this function the
   * {@code 
   * shortener:create:<ip>
   * }
   * right is needed. "<ip>" is replaced by the IP of the client which makes this request.
   * Either IPv4 or IPv6 can be used. The dots (IPv4) or colons (IPv6) 
   * must be replaced with underscores since they conflict with the Apache
   * Shiro {@link WildcardPermission} format.
   * 
   * @param uri The URI to shorten.
   * @param baseURI The base URI of the application. 
   *                The scheme, host name, port and path from the base URI will 
   *                be removed in the given URI. This parameter is optional,
   *                if not given the URI is saved as it is.
 
   * @return 
   */
  @PUT
  public String addNewID(URI uri, @QueryParam("baseURI") URI baseURI)
  {
    Subject user = SecurityUtils.getSubject();
    
    String remoteIP = request.getRemoteAddr().replaceAll("[.:]", "_");
    user.checkPermission("shortener:create:" + remoteIP);
    
    return "";
  }
  
  
  public AdministrationDao getAdminDao()
  {
    return adminDao;
  }

  public void setAdminDao(AdministrationDao adminDao)
  {
    this.adminDao = adminDao;
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public CorpusAdministration getCorpusAdmin()
  {
    return corpusAdmin;
  }

  public void setCorpusAdmin(CorpusAdministration corpusAdmin)
  {
    this.corpusAdmin = corpusAdmin;
  }
  
}
