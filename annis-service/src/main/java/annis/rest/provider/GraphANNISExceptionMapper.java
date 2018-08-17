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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class GraphANNISExceptionMapper implements ExceptionMapper<GraphANNISException> {
    private final Logger log = LoggerFactory.getLogger(GraphANNISExceptionMapper.class);

    /**
     * Maps an exception to a response or returns null if it wasn't handled
     * 
     * @param ex
     * @return
     */
    public static Response map(GraphANNISException ex) {
        // go through each cause
        StringBuilder sb = new StringBuilder();
        
        sb.append(ex.getMessage());
        sb.append(" (");
        sb.append(ex.getClass().getSimpleName());
        sb.append(")");
        
        // build "stack trace" of causes
        Throwable cause = ex.getCause();
        while(cause != null) {
            sb.append("\ncaused by: ");
            sb.append(cause.getMessage());
            sb.append(" (");
            sb.append(ex.getClass().getSimpleName());
            sb.append(")");
            cause = cause.getCause();
        }
        return Response.status(500).entity(sb.toString()).build();
    }

    @Override
    public Response toResponse(GraphANNISException ex) {
        Response r = map(ex);
        if (r != null) {
            return r;
        }

        // default
        log.error("Unhandled GraphANNISException", ex);
        return Response.status(500).entity(ex.getMessage()).build();

    }

}
