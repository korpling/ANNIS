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

import java.nio.BufferUnderflowException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.Subject;

import annis.dao.ShortenerDao;

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
  private ShortenerDao shortenerDao;
  
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
   * right is needed. "&lt;ip&gt;" is replaced by the IP of the client which makes this request.
   * Either IPv4 or IPv6 can be used. The dots (IPv4) or colons (IPv6) 
   * must be replaced with underscores since they conflict with the Apache
   * Shiro {@link WildcardPermission} format.
   * 
   * @param str The string to shorten.
   * @return 
   */
  @POST
  @Produces(value = "text/plain")
  public String addNewID(String str)
  {
    Subject user = SecurityUtils.getSubject();
    
    String remoteIP = request.getRemoteAddr().replaceAll("[.:]", "_");
    user.checkPermission("shortener:create:" + remoteIP);
    
    return shortenerDao.shorten(str, "" + user.getPrincipal()).toString();
  }
  
  @GET
  @Path("{id}")
  @Produces(value = "text/plain")
  public String getLong(@PathParam("id") String idRaw)
  {
    if(idRaw == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    try
    {
      UUID id = UUID.fromString(idRaw);
      String result = shortenerDao.unshorten(id);
      if(result == null)
      {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      return result;
    }
    catch(IllegalArgumentException | BufferUnderflowException ex)
    {
      
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  public ShortenerDao getShortenerDao()
  {
    return shortenerDao;
  }

  public void setShortenerDao(ShortenerDao shortenerDao)
  {
    this.shortenerDao = shortenerDao;
  }
  
  
}
