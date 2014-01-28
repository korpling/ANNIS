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
package annis.provider;

import annis.exceptions.AnnisQLSyntaxException;
import annis.model.AqlParseError;
import java.util.List;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author thomas
 */
@Provider
public class AnnisQLSyntaxMapper implements
  ExceptionMapper<AnnisQLSyntaxException>
{

  @Override
  public Response toResponse(AnnisQLSyntaxException exception)
  {
    return Response.status(Response.Status.BAD_REQUEST).entity(
      new GenericEntity<List<AqlParseError>>(exception.getErrors()) {})
      .type("application/xml").build();
  }
}
