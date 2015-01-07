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

package annis.service;

import annis.security.User;
import annis.service.objects.ImportJob;
import java.util.List;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Interface defining the REST API calls that ANNIS provides for administrative
 * tasks.
 * 
 * Currently it is possible to import corpora and monitor the import status 
 * with this interface.
 * 
 * <div>
 * All paths for this part of the service start with <pre>annis/admin/</pre>
 * </div>
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface AdminService
{
    /**
   * Import one or more corpora from an uploaded ZIP file.
   * 
   * <h3>MIME</h3>
   * consumes:
   * <code>application/zip</code>:<br/>
   * A ZIP file which containes one or more corpora in seperate sub-folders.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>POST annis/admin/import</b></li>
   * </ol>
   * 
   * @param overwrite Set to "true" if the the corpus should be overwritten.
   * @param statusMail An e-mail address to which status reports are sent.
   * @param alias An internal alias name of the corpus.
   * @return 
   */
  public Response importCorpus(
    String overwrite,
    String statusMail,
    String alias);
  
  /**
   * Shows information about a specific job after the import was finished.
   * 
   * When the import is not finished yet, a 404 HTTP status code will be sent.
   * If the import finished a 200 HTTP status code is sent and a proper description
   * of the import job is returned. After this resource has been successfully
   * accessed once, a 404 HTTP status code will be sent on subsequent requests.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/admin/import/status/finished/<b>{uuid}</b></li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * produces:
   * <code>application/xml</code>:
   * {@code
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <importJob>
   *   <!-- visible caption, e.g. corpus name  -->
   *   <caption>MyNewCorpus</caption>
   *   <!-- A list of output messages from the import process-->
   *   <messages>
   *     <m>first message</m>
   *     <m>second message</m>
   *     <m>just another message</m>
   *   </messages>
   *   <!-- true if the corpus will be overwritten -->
   *   <overwrite>true</overwrite>
   *   <!-- current status, can be WAITING, RUNNING, SUCCESS or ERROR -->
   *   <status>RUNNING</status>
   *   <!-- an unique identifier for this import job -->
   *   <uuid>7799322d-83ec-4900-83b0-c542e2ca2137</uuid>
   *   <!-- a mail address to which status reports should be send -->
   *   <statusMail>mail@example.com</statusMail>
   *   <!-- alias name of the corpus as defined by the import request -->
   *   <alias>CorpusAlias</alias>
   * </importJob>
   * }
   * 
   * @param uuid Unique identifier of the import job
   * @return The XML representation of an {@link annis.service.objects.ImportJob} 
   * object inside an "importJob" element.
   */
  public ImportJob finishedImport(
    String uuid);

  /**
   * List all currently running import jobs.
   *
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/admin/import/status/</li>
   * </ol>
   *
   * <h3>MIME</h3>
   * produces:
   * <code>application/xml</code>:
   * {@code
   * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   * <importJobs>
   *   <!-- an importJob tag for each running import -->
   *   <importJob>
   *     <!-- visible caption, e.g. corpus name  -->
   *     <caption>MyNewCorpus</caption>
   *     <!-- A list of output messages from the import process-->
   *     <messages>
   *       <m>first message</m>
   *       <m>second message</m>
   *       <m>just another message</m>
   *     </messages>
   *     <!-- true if the corpus will be overwritten -->
   *     <overwrite>true</overwrite>
   *     <!-- current status, can be WAITING, RUNNING, SUCCESS or ERROR -->
   *     <status>RUNNING</status>
   *     <!-- an unique identifier for this import job -->
   *     <uuid>7799322d-83ec-4900-83b0-c542e2ca2137</uuid>
   *     <!-- a mail address to which status reports should be send -->
   *     <statusMail>mail@example.com</statusMail>
   *     <!-- alias name of the corpus as defined by the import request -->
   *     <alias>CorpusAlias</alias>
   *  </importJob>
   * </importJobs>
   * }
   * 
   * @return The XML representation of a list wich contains {@link annis.service.objects.ImportJob}
   * objects. The root element has the name "importJobs" and there is an 
   * "importJob" element for each element of the list.
   */
  public List<ImportJob> currentImports();
  
  /**
   * 
   * Updates or creates a new user.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>PUT annis/admin/users/<b>{userName}</b></li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * 
   * accepts:<br/>
   * <code>application/xml</code>:<br />
   * This method accepts the user information in XML. The fields correspond to
   * the fields of the  [single user configuration file](@ref admin-configure-userformat). 
   * Please have a look at the [general user configuration information](@ref admin-configure-userformat) for
   * a more detailed explanation.<br />
   * {@code
   * <user>
   *   <!-- User name (must be the same as the "userName" parameter) -->
   *   <name>myusername</name>
   *   <!-- hashed password in the Shiro1CryptFormat -->
   *   <passwordHash>$shiro1$SHA-256$1$tQNwU[...]</passwordHash>
   *   <!-- A list of groups the users should belong to. -->
   *   <group>group1</group>
   *   <group>group2</group>
   *   <group>group3</group>
   *   <!-- A list of explicit permission the users should have. -->
   *   <permission>admin:*</permission>
   *   <permission>query:*</permission>
   *   <!-- Optional expiration date encoded in the ISO-8601 standard</a> -->
   *   <expires>2015-02-12T00:00:00.000+01:00</expires>
   * </user>
   * }
   * 
   * @param requestBody
   * @param userName The name of the user to create or update.
   * 
   * @see http://shiro.apache.org/static/current/apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html
   * @see http://en.wikipedia.org/wiki/ISO_8601
   * @return 
   */
  public Response updateOrCreateUser(
    User requestBody,
    @PathParam("userName") String userName);
  
  /**
   * 
   * Get an existing user.
   * 
   * <h3>Path(s)</h3>
   * <ol>
   * <li>GET annis/admin/users/<b>{userName}</b></li>
   * </ol>
   * 
   * <h3>MIME</h3>
   * 
   * produces:<br/>
   * <code>application/xml</code>:<br />
   * This method return the user information in XML. The fields correspond to
   * the fields of the  [single user configuration file](@ref admin-configure-userformat). 
   * Please have a look at the [general user configuration information](@ref admin-configure-userformat) for
   * a more detailed explanation.<br />
   * {@code
   * <user>
   *   <!-- User name (must be the same as the "userName" parameter) -->
   *   <name>myusername</name>
   *   <!-- hashed password in the Shiro1CryptFormat -->
   *   <passwordHash>$shiro1$SHA-256$1$tQNwU[...]</passwordHash>
   *   <!-- A list of groups the users should belong to. -->
   *   <group>group1</group>
   *   <group>group2</group>
   *   <group>group3</group>
   *   <!-- A list of explicit permission the users should have. -->
   *   <permission>admin:*</permission>
   *   <permission>query:*</permission>
   *   <!-- Optional expiration date encoded in the ISO-8601 standard</a> -->
   *   <expires>2015-02-12T00:00:00.000+01:00</expires>
   * </user>
   * }
   * 
   * @param userName The name of the user
   * 
   * @see http://shiro.apache.org/static/current/apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html
   * @see http://en.wikipedia.org/wiki/ISO_8601
   * @return 
   */
  public User getUser(
    @PathParam("userName") String userName);
  
}
