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
import annis.security.AnnisUserConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;

/**
 * Methods for adminstration.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@Component
@Path("/annis-admin")
public class AdminService
{
  private AdministrationDao adminDao;

  /**
   * Get the user configuration for the currentl logged in user.
   */
  @GET
  @Path("userconfig")
  @Produces("application/xml")
  public AnnisUserConfig getUserConfig()
  {
    Subject user = SecurityUtils.getSubject();
    return adminDao.getUserConfig((String) user.getPrincipal());
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
