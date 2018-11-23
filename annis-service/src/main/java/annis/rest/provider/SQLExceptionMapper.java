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

import annis.model.AqlParseError;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class SQLExceptionMapper implements ExceptionMapper<SQLException>
{
  private final Logger log = LoggerFactory.getLogger(SQLExceptionMapper.class);
  /**
   * Maps an exception to a response or returns null if it wasn't handled
   * @param sqlEx
   * @return 
   */
  public static Response map(SQLException sqlEx)
  {
    if (null != sqlEx.getSQLState())
    {
      switch (sqlEx.getSQLState())
      {
        //query_canceled
        case "57014":
          return Response.status(504).entity(sqlEx.getMessage()).build();
        case "2201B":
          // regular expression did not compile
          AqlParseError error = new AqlParseError(sqlEx.getMessage());
          return Response.status(Response.Status.BAD_REQUEST).entity(
            new GenericEntity<List<AqlParseError>>(Arrays.asList(error))
            {
            })
            .type("application/xml").build();
      }
    }
    return null;
  }

  @Override
  public Response toResponse(SQLException sqlEx)
  {
    Response r = map(sqlEx);
    if(r != null)
    {
      return r;
    }
 
    // default
    log.error("Unhandled SQLException", sqlEx);
    return Response.status(500).entity("error " + sqlEx.getSQLState() + ":" +  sqlEx.getMessage()).build();
    
  }
  
  
}
