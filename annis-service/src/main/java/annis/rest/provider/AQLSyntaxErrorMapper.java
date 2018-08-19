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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.corpus_tools.graphannis.errors.AQLSyntaxError;

import annis.model.AqlParseError;
import annis.model.ParsedEntityLocation;

/**
 *
 * @author thomas
 */
@Provider
public class AQLSyntaxErrorMapper implements ExceptionMapper<AQLSyntaxError> {
    
    private static final  Pattern locationPattern = Pattern.compile(
            "\\[(?<startline>[0-9]+):(?<startcol>[0-9]+)(-(?<endline>[0-9]+):(?<endcol>[0-9]+))?\\]");

    @Override
    public Response toResponse(AQLSyntaxError exception) {
        
        String[] msgLines = exception.getMessage().split("\n");
        List<AqlParseError> errors = new LinkedList<>();
        
        // TODO: split multiple error messages
        
        AqlParseError e = new AqlParseError();
        if(msgLines.length > 0) {
            StringBuilder sb = new StringBuilder(msgLines[0]);
            // append the hints as well
            for(int i=2; i < msgLines.length; i++) {
                sb.append("\n");
                sb.append(msgLines[i]);
            }
            e.setMessage(sb.toString());
        }
        
        if(msgLines.length > 1) {
           // get the position information
           Matcher m = locationPattern.matcher(msgLines[1]);
           if(m.matches()) {
               ParsedEntityLocation loc = new ParsedEntityLocation();
               String startLine = m.group("startline");
               String startCol = m.group("startcol");
               String endLine = m.group("endline");
               String endCol = m.group("endcol");
               
               loc.setStartLine(Integer.parseInt(startLine));
               loc.setStartColumn(Integer.parseInt(startCol));
               
               if(endLine != null &&  endCol != null) {
                   loc.setEndLine(Integer.parseInt(endLine));
                   loc.setEndColumn(Integer.parseInt(endCol));
               } else {
                   loc.setEndLine(loc.getStartLine());
                   loc.setEndColumn(loc.getStartColumn());
               }
               e.setLocation(loc);
           }
        }
        errors.add(e);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new GenericEntity<List<AqlParseError>>(errors) {
                }).type("application/xml").build();
    }
}
