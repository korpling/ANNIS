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
package annis.service.internal;

import annis.administration.AdministrationDao;
import annis.dao.AnnisDao;
import annis.security.AnnisUserConfig;
import annis.utils.RelANNISHelper;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Methods for adminstration.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@Component
@Path("annis/admin")
public class AdminService
{
  private final static Logger log = LoggerFactory.getLogger(AdminService.class);
  
  private AdministrationDao adminDao;
  private AnnisDao annisDao;
  private ExecutorService importExecutor = Executors.newSingleThreadExecutor();
  private ConcurrentMap<String, Future<String>> importQueue = Maps.newConcurrentMap();

  @GET
  @Path("is-authenticated")
  @Produces("text/plain")
  public String isAuthenticated()
  {
    Subject user = SecurityUtils.getSubject();
    
    return Boolean.toString(user.isAuthenticated());
  }
  
  /**
   * Get the user configuration for the currentl logged in user.
   */
  @GET
  @Path("userconfig")
  @Produces("application/xml")
  public AnnisUserConfig getUserConfig()
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:read:userconfig");
    
    return adminDao.retrieveUserConfig((String) user.getPrincipal());
  }
  
  /**
   * Sets the user configuration for the currentl logged in user.
   */
  @POST
  @Path("userconfig")
  @Consumes("application/xml")
  public Response setUserConfig(JAXBElement<AnnisUserConfig> config)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:write:userconfig");
    
    adminDao.storeUserConfig(config.getValue());      
      return Response.ok().build();
  }
  
  @POST
  @Path("import")
  @Consumes("application/zip")
  public Response importCorpus(@Context HttpServletRequest request)
  {
    // write content to temporary file
    OutputStream tmpOut = null;
    try
    {
      File tmpZip = File.createTempFile("annis-import", ".zip");
      tmpZip.deleteOnExit();
      
      tmpOut = new FileOutputStream(tmpZip);
      ByteStreams.copy(request.getInputStream(), tmpOut);      
      ZipFile zip = new ZipFile(tmpZip);
      
      // find the directory containing the real relannis tab files
      ZipEntry corpusTab = RelANNISHelper.getRelANNISEntry(zip, "corpus", "tab");
      if(corpusTab != null)
      {
        String corpusName = RelANNISHelper.extractToplevelCorpusName(zip.getInputStream(corpusTab));
        if(corpusName != null)
        {
          List<String> asList = new LinkedList<String>();
          asList.add(corpusName);
          List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(asList);
          if(corpusIDs == null || corpusIDs.isEmpty())
          {
            ImportWorker worker = new ImportWorker(adminDao, zip, corpusName);
            UUID uuid = UUID.randomUUID();
            Future<String> future = importExecutor.submit(worker);
            importQueue.put(uuid.toString(), future);
            
            return Response.status(Response.Status.ACCEPTED).header("Location", 
              request.getContextPath() + "/annis/admin/import-status/" + uuid.toString())
              .build();
          }
          else
          {
            return Response.status(Response.Status.BAD_REQUEST)
              .entity("The corpus already exists").build();
          }
        }
        else
        {
          return
            Response.status(Response.Status.BAD_REQUEST)
            .entity("corpus name not found inside corpus.tab")
            .build();
        }
      }
      else
      {
        return
          Response.status(Response.Status.BAD_REQUEST)
          .entity("no corpus.tab file found in upload")
          .build();
      }
    }
    catch(IOException ex)
    {
      log.error(null, ex);
    }
    finally
    {
      if(tmpOut != null)
      {
        try
        {
          tmpOut.close();
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }
    }
    return Response.serverError().build();
  }
 
  
  public AdministrationDao getAdminDao()
  {
    return adminDao;
  }

  public void setAdminDao(AdministrationDao adminDao)
  {
    this.adminDao = adminDao;
  }
  
  
}
