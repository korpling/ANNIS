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
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class SQLExceptionMapper implements ExceptionMapper<DataAccessResourceFailureException>
{

  @Override
  public Response toResponse(DataAccessResourceFailureException ex)
  {
    if(ex.getCause() instanceof PSQLException)
    {
      SQLException sqlEx = (SQLException) ex.getCause();
      if("57014".equals(sqlEx.getSQLState())) //query_canceled
      {
        return Response.status(504).entity(sqlEx.getMessage()).build();
      }
    }
    
    return Response.status(500).entity(ex.getMessage()).build();
    
  }
  
}
