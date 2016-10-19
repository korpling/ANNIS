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
package annis.rest.provider;

import java.sql.SQLException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class DataAccessExceptionMapper implements
  ExceptionMapper<DataAccessException>
{
  
  private final Logger log = LoggerFactory.getLogger(DataAccessExceptionMapper.class);

  @Override
  public Response toResponse(DataAccessException ex)
  {
    if (ex.getCause() instanceof SQLException)
    {
      SQLException sqlEx = (SQLException) ex.getCause();
      Response r = SQLExceptionMapper.map(sqlEx);
      if (r != null)
      {
        return r;
      }
    }

    // default
    log.error("Unhandled DataAccessException", ex);
    return Response.status(500).entity(ex.getMessage()).build();

  }

}
