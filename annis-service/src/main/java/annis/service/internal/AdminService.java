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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import org.aeonbits.owner.ConfigFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.ServiceConfig;
import annis.administration.AdministrationDao;
import annis.security.ANNISSecurityManager;
import annis.security.ANNISUserConfigurationManager;
import annis.security.ANNISUserRealm;
import annis.security.Group;
import annis.security.User;
import annis.security.UserConfig;
import annis.service.objects.ImportJob;

/**
 * Methods for adminstration.
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Path("annis/admin")
public class AdminService {

    private final static Logger log = LoggerFactory.getLogger(AdminService.class);

    @Context
    Configuration config;

    private final ServiceConfig serviceCfg = ConfigFactory.create(ServiceConfig.class);

    @Context
    HttpServletRequest request;

    @GET
    @Path("is-authenticated")
    @Produces("text/plain")
    public Response isAuthenticated() {
        Subject user = SecurityUtils.getSubject();
        Object principal = user.getPrincipal();
        if (principal instanceof String) {
            // if a use has an expired account it won't have it's own name as role
            boolean hasOwnRole = user.hasRole((String) principal);
            if (!hasOwnRole) {
                return Response.status(Response.Status.FORBIDDEN).entity("Account expired").build();
            }
        }

        return Response.ok(Boolean.toString(user.isAuthenticated())).build();
    }

    /**
     * Get the user configuration for the currently logged in user.
     *
     * @return
     */
    @GET
    @Path("userconfig")
    @Produces("application/xml")
    public UserConfig getUserConfig() {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("admin:read:userconfig");

        return getAdminDao().retrieveUserConfig((String) user.getPrincipal());
    }

    /**
     * Sets the user configuration for the currently logged in user.
     */
    @POST
    @Path("userconfig")
    @Consumes("application/xml")
    public Response setUserConfig(JAXBElement<UserConfig> config) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("admin:write:userconfig");

        String userName = (String) user.getPrincipal();

        getAdminDao().storeUserConfig(userName, config.getValue());
        return Response.ok().build();
    }

    @GET
    @Path("users")
    @Produces("application/xml")
    public List<User> listUsers() {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:read:user");

        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = getConfManager();
            if (confManager != null) {
                return confManager.listAllUsers();
            }
        }
        return new LinkedList<>();
    }

    @PUT
    @Path("users/{userName}")
    @Consumes("application/xml")
    public Response updateOrCreateUser(User user, @PathParam("userName") String userName) {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:user");

        if (!userName.equals(user.getName())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Username in object is not the same as in path")
                    .build();
        }

        // if any permission is an adminstrative one the
        // requesting user needs more than just a "admin:write:user" permission"
        for (String permission : user.getPermissions()) {
            if (permission.startsWith("admin:")) {
                requestingUser.checkPermission("admin:write:adminuser");
                break;
            }
        }

        ANNISUserRealm userRealm = getUserRealm();
        if (userRealm != null) {
            if (userRealm.updateUser(user)) {
                return Response.ok().build();
            }
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not update/create user").build();
    }

    @GET
    @Path("users/{userName}")
    @Produces("application/xml")
    public User getUser(@PathParam("userName") String userName) {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:read:user");

        ANNISUserConfigurationManager conf = getConfManager();
        if (conf != null) {
            User u = conf.getUser(userName);
            if (u == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            // remove the password hash from the result, we don't want someone with
            // lower adminstration rights to crack it
            u.setPasswordHash("");

            return u;
        }
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @DELETE
    @Path("users/{userName}")
    public Response deleteUser(@PathParam("userName") String userName) {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:user");

        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = getConfManager();
            if (confManager != null) {
                if (confManager.deleteUser(userName)) {
                    // also delete any possible user configs
                    getAdminDao().deleteUserConfig(userName);
                    // if no error until here everything went well
                    return Response.ok().build();
                }
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not delete user").build();
    }

    @POST
    @Path("users/{userName}/password")
    @Consumes("text/plain")
    @Produces("application/xml")
    public Response changePassword(String newPassword, @PathParam("userName") String userName) {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:user");

        ANNISUserConfigurationManager confManager = getConfManager();
        ANNISUserRealm userRealm = getUserRealm();
        if (confManager != null && userRealm != null) {
            User user = confManager.getUser(userName);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Shiro1CryptFormat format = new Shiro1CryptFormat();

            SecureRandomNumberGenerator generator = new SecureRandomNumberGenerator();
            ByteSource salt = generator.nextBytes(128 / 8); // 128 bit

            Sha256Hash hash = new Sha256Hash(newPassword, salt, 1);
            user.setPasswordHash(format.format(hash));

            if (userRealm.updateUser(user)) {
                return Response.ok().entity(user).build();
            }
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not change password").build();
    }

    @GET
    @Path("groups")
    @Produces("application/xml")
    public List<Group> listGroups() {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:read:group");

        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = getConfManager();
            if (confManager != null) {
                return new LinkedList<>(confManager.getGroups().values());
            }
        }
        return new LinkedList<>();
    }

    @PUT
    @Path("groups/{groupName}")
    @Consumes("application/xml")
    public Response updateOrCreateGroup(Group group, @PathParam("groupName") String groupName) {

        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:group");

        if (!groupName.equals(group.getName())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Group name in object is not the same as in path").build();
        }

        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = getConfManager();
            if (confManager != null) {
                if (confManager.writeGroup(group)) {
                    return Response.ok().build();
                }
            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not update/create group").build();
    }

    @DELETE
    @Path("groups/{groupName}")
    public Response deleteGroup(@PathParam("groupName") String groupName) {

        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:group");

        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = getConfManager();
            if (confManager != null) {

                if (confManager.deleteGroup(groupName)) {
                    return Response.ok().build();
                }

            }
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not delete group").build();
    }

    @DELETE
    @Path("corpora/{corpusName}")
    public Response deleteCorpus(@PathParam("corpusName") String corpusName) {
        Subject requestingUser = SecurityUtils.getSubject();
        requestingUser.checkPermission("admin:write:corpus");

        try {

            getAdminDao().getDeleteCorpusDao().deleteCorpora(Arrays.asList(corpusName));
            return Response.status(Response.Status.OK).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("import/status")
    public List<ImportJob> currentImports() {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("admin:query-import:running");

        List<ImportJob> result = new LinkedList<>();
        ImportJob current = getImportWorker().getCurrentJob();
        if (current != null && current.getStatus() != ImportJob.Status.SUCCESS
                && current.getStatus() != ImportJob.Status.ERROR) {
            result.add(current);
        }
        result.addAll(getImportWorker().getImportQueue());
        return result;
    }

    @GET
    @Path("import/status/finished/{uuid}")
    public ImportJob finishedImport(@PathParam("uuid") String uuid) {
        Subject user = SecurityUtils.getSubject();
        user.checkPermission("admin:query-import:finished");

        ImportJob job = getImportWorker().getFinishedJob(uuid);
        if (job == null) {
            throw new WebApplicationException(404);
        }
        return job;
    }

    @POST
    @Path("import")
    @Consumes({ "application/zip" })
    public Response importCorpus(@QueryParam("overwrite") String overwriteRaw,
            @QueryParam("statusMail") String statusMail, @QueryParam("alias") String alias) {
        Subject user = SecurityUtils.getSubject();

        boolean overwrite = Boolean.parseBoolean(overwriteRaw);

        // write content to temporary file
        try {
            File tmpZip = File.createTempFile("annis-import", ".zip");
            tmpZip.deleteOnExit();

            try (OutputStream tmpOut = new FileOutputStream(tmpZip)) {
                ByteStreams.copy(request.getInputStream(), tmpOut);
            }
            Set<String> allNames = ANNISFormatHelper.corporaInZipfile(tmpZip).keySet();

            if (!allNames.isEmpty()) {
                for (String corpusName : allNames) {
                    user.checkPermission("admin:import:" + corpusName);
                }
                String caption = Joiner.on(", ").join(allNames);

                List<AnnisCorpus> existingCorpora = getAdminDao().getQueryDao().listCorpora(new LinkedList<>(allNames));

                if (overwrite || existingCorpora == null || existingCorpora.isEmpty()) {
                    ImportJob job = new ImportJob();
                    UUID uuid = UUID.randomUUID();
                    job.setUuid(uuid.toString());
                    job.setCaption(caption);
                    job.setImportRootDirectory(tmpZip);
                    job.setStatus(ImportJob.Status.WAITING);
                    job.setOverwrite(overwrite);
                    job.setDiskBased(getAdminDao().);
                    job.setStatusEmail(statusMail);
                    job.setAlias(alias);

                    getAdminDao().sendImportStatusMail(statusMail, caption, ImportJob.Status.WAITING, null);

                    try {
                        getImportWorker().getImportQueue().put(job);

                        return Response.status(Response.Status.ACCEPTED).header("Location",
                                request.getContextPath() + "/annis/admin/import/status/finished/" + uuid.toString())
                                .build();
                    } catch (InterruptedException ex) {
                        log.error("Could not add job to import queue", ex);
                        return Response.serverError()
                                .entity("Could not add job to "
                                        + "import queue. There might be more information in the server "
                                        + "log files. Contact the administrator if necessary.")
                                .build();
                    }
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("The corpus already exists").build();
                }

            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("no corpus.tab file found in upload")
                        .build();
            }
        } catch (IOException ex) {
            log.error(null, ex);
        }
        return Response.serverError().build();
    }

    private ANNISUserConfigurationManager getConfManager() {
        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserConfigurationManager confManager = ((ANNISSecurityManager) SecurityUtils.getSecurityManager())
                    .getConfManager();
            return confManager;
        }
        return null;
    }

    private ANNISUserRealm getUserRealm() {
        if (SecurityUtils.getSecurityManager() instanceof ANNISSecurityManager) {
            ANNISUserRealm userRealm = ((ANNISSecurityManager) SecurityUtils.getSecurityManager()).getANNISUserRealm();
            return userRealm;
        }
        return null;
    }
    
    public ImportWorker getImportWorker() {
        Object prop = config.getProperty("importWorker");
        if (prop instanceof ImportWorker) {
            return (ImportWorker) prop;
        } else {
            return null;
        }
    }

    public AdministrationDao getAdminDao() {
        Object prop = config.getProperty("adminDao");
        if (prop instanceof AdministrationDao) {
            return (AdministrationDao) prop;
        } else {
            return null;
        }
    }

}
