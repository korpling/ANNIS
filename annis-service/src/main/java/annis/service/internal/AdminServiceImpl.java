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

import annis.service.objects.ImportJob;
import annis.administration.AdministrationDao;
import annis.administration.CorpusAdministration;
import annis.dao.AnnisDao;
import annis.security.ANNISSecurityManager;
import annis.security.ANNISUserConfigurationManager;
import annis.security.Group;
import annis.security.User;
import annis.security.UserConfig;
import annis.service.AdminService;
import annis.utils.RelANNISHelper;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Methods for adminstration.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Component
@Path("annis/admin")
public class AdminServiceImpl implements AdminService
{

  private final static Logger log = LoggerFactory.getLogger(
    AdminServiceImpl.class);

  private AdministrationDao adminDao;

  private CorpusAdministration corpusAdmin;

  private AnnisDao annisDao;

  private ImportWorker importWorker;

  @Context
  HttpServletRequest request;

  public void init()
  {
    importWorker.start();
  }

  @GET
  @Path("is-authenticated")
  @Produces("text/plain")
  public String isAuthenticated()
  {
    Subject user = SecurityUtils.getSubject();

    return Boolean.toString(user.isAuthenticated());
  }

  /**
   * Get the user configuration for the currently logged in user.
   *
   * @return
   */
  @GET
  @Path("userconfig")
  @Produces("application/xml")
  public UserConfig getUserConfig()
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:read:userconfig");

    return adminDao.retrieveUserConfig((String) user.getPrincipal());
  }

  /**
   * Sets the user configuration for the currently logged in user.
   */
  @POST
  @Path("userconfig")
  @Consumes("application/xml")
  public Response setUserConfig(JAXBElement<UserConfig> config)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:write:userconfig");

    String userName = (String) user.getPrincipal();

    adminDao.storeUserConfig(userName, config.getValue());
    return Response.ok().build();
  }

  @GET
  @Path("users")
  @Produces("application/xml")
  public List<User> listUsers()
  {
    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:read:user");

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        return confManager.listAllUsers();
      }
    }
    return new LinkedList<>();
  }

  @PUT
  @Path("users/{userName}")
  @Consumes("application/xml")
  public Response updateOrCreateUser(
    User user,
    @PathParam("userName") String userName)
  {
    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:write:user");

    if (!userName.equals(user.getName()))
    {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("Username in object is not the same as in path")
        .build();
    }

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        if (confManager.writeUser(user))
        {
          return Response.ok().build();
        }
      }
    }
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity("Could not update/create user")
      .build();
  }

  @DELETE
  @Path("users/{userName}")
  @Consumes("application/xml")
  public Response deleteUser(@PathParam("userName") String userName)
  {
    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:write:user");

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        if (confManager.deleteUser(userName))
        {
          return Response.ok().build();
        }
      }
    }
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity("Could not delete user")
      .build();
  }

  @POST
  @Path("users/{userName}/password")
  @Consumes("text/plain")
  @Produces("application/xml")
  public Response changePassword(
    String newPassword,
    @PathParam("userName") String userName)
  {
    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:write:user");

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        User user = confManager.getUser(userName);
        if (user == null)
        {
          return Response.status(Response.Status.NOT_FOUND).build();
        }

        Shiro1CryptFormat format = new Shiro1CryptFormat();

        Sha256Hash hash = new Sha256Hash(newPassword, null, 1);
        user.setPasswordHash(format.format(hash));

        if (confManager.writeUser(user))
        {
          return Response.ok().entity(user).build();
        }
      }
    }
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity("Could not change password")
      .build();
  }

  @GET
  @Path("groups")
  @Produces("application/xml")
  public List<Group> listGroups()
  {
    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:read:group");

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        return new LinkedList<>(confManager.getGroups().values());
      }
    }
    return new LinkedList<>();
  }

  @PUT
  @Path("groups/{groupName}")
  @Consumes("application/xml")
  public Response updateOrCreateGroup(
    Group group,
    @PathParam("groupName") String groupName)
  {

    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:write:group");

    if (!groupName.equals(group.getName()))
    {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("Group name in object is not the same as in path")
        .build();
    }

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        if (confManager.writeGroup(group))
        {
          return Response.ok().build();
        }
      }
    }
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity("Could not update/create group")
      .build();
  }

  @DELETE
  @Path("groups/{groupName}")
  @Consumes("application/xml")
  public Response deleteGroup(@PathParam("groupName") String groupName)
  {

    Subject requestingUser = SecurityUtils.getSubject();
    requestingUser.checkPermission("admin:write:group");

    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager = getConfManager();
      if (confManager != null)
      {
        
        if (confManager.deleteGroup(groupName))
        {
          return Response.ok().build();
        }

      }
    }
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity("Could not delete group")
      .build();
  }

  @GET
  @Path("import/status")
  @Override
  public List<ImportJob> currentImports()
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:query-import:running");

    List<ImportJob> result = new LinkedList<>();
    ImportJob current = importWorker.getCurrentJob();
    if (current != null
      && current.getStatus() != ImportJob.Status.SUCCESS && current.getStatus() != ImportJob.Status.ERROR)
    {
      result.add(current);
    }
    result.addAll(importWorker.getImportQueue());
    return result;
  }

  @GET
  @Path("import/status/finished/{uuid}")
  @Override
  public ImportJob finishedImport(@PathParam("uuid") String uuid)
  {
    Subject user = SecurityUtils.getSubject();
    user.checkPermission("admin:query-import:finished");

    ImportJob job = importWorker.getFinishedJob(uuid);
    if (job == null)
    {
      throw new WebApplicationException(404);
    }
    return job;
  }

  @POST
  @Path("import")
  @Consumes(
  {
    "application/zip"
  })
  @Override
  public Response importCorpus(
    @QueryParam("overwrite") String overwriteRaw,
    @QueryParam("statusMail") String statusMail,
    @QueryParam("alias") String alias)
  {
    Subject user = SecurityUtils.getSubject();

    boolean overwrite = Boolean.parseBoolean(overwriteRaw);

    // write content to temporary file
    try
    {
      File tmpZip = File.createTempFile("annis-import", ".zip");
      tmpZip.deleteOnExit();

      try (OutputStream tmpOut = new FileOutputStream(tmpZip))
      {
        ByteStreams.copy(request.getInputStream(), tmpOut);
      }
      Set<String> allNames = RelANNISHelper.corporaInZipfile(tmpZip).keySet();

      if (!allNames.isEmpty())
      {
        for (String corpusName : allNames)
        {
          user.checkPermission("admin:import:" + corpusName);
        }
        String caption = Joiner.on(", ").join(allNames);

        List<Long> corpusIDs = annisDao.mapCorpusNamesToIds(new LinkedList<>(
          allNames));
        if (overwrite || corpusIDs == null || corpusIDs.isEmpty())
        {
          ImportJob job = new ImportJob();
          UUID uuid = UUID.randomUUID();
          job.setUuid(uuid.toString());
          job.setCaption(caption);
          job.setImportRootDirectory(tmpZip);
          job.setStatus(ImportJob.Status.WAITING);
          job.setOverwrite(overwrite);
          job.setStatusEmail(statusMail);
          job.setAlias(alias);

          corpusAdmin.sendStatusMail(statusMail, caption,
            ImportJob.Status.WAITING, null);

          try
          {
            importWorker.getImportQueue().put(job);

            return Response.status(Response.Status.ACCEPTED).header("Location",
              request.getContextPath() + "/annis/admin/import/status/finished/" + uuid.
              toString())
              .build();
          }
          catch (InterruptedException ex)
          {
            log.error("Could not add job to import queue", ex);
            return Response.serverError().entity("Could not add job to "
              + "import queue. There might be more information in the server "
              + "log files. Contact the administrator if necessary.").build();
          }
        }
        else
        {
          return Response.status(Response.Status.BAD_REQUEST)
            .entity("The corpus already exists").build();
        }

      }
      else
      {
        return Response.status(Response.Status.BAD_REQUEST)
          .entity("no corpus.tab file found in upload")
          .build();
      }
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    return Response.serverError().build();
  }

  private ANNISUserConfigurationManager getConfManager()
  {
    if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager)
    {
      ANNISUserConfigurationManager confManager
        = ((ANNISSecurityManager) SecurityUtils.getSecurityManager()).
        getConfManager();
      return confManager;
    }
    return null;
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

  public ImportWorker getImportWorker()
  {
    return importWorker;
  }

  public void setImportWorker(ImportWorker importWorker)
  {
    this.importWorker = importWorker;
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
