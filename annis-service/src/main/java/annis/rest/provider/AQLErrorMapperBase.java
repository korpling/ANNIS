package annis.rest.provider;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.corpus_tools.graphannis.errors.AQLSyntaxError;
import org.corpus_tools.graphannis.errors.GraphANNISException;

import annis.model.AqlParseError;
import annis.model.ParsedEntityLocation;

public abstract class AQLErrorMapperBase<T extends GraphANNISException> implements ExceptionMapper<T> {

    private static final Pattern locationPattern = Pattern.compile(
                "\\[(?<startline>[0-9]+):(?<startcol>[0-9]+)(-(?<endline>[0-9]+):(?<endcol>[0-9]+))?\\]");

    public AQLErrorMapperBase() {
        super();
    }

    @Override
    public Response toResponse(T exception) {
     
        List<AqlParseError> errors = new LinkedList<>();
        String[] errorMessageBlocks = exception.getMessage().split("---+\n");
        
        for(String block : errorMessageBlocks) {
            
            String[] msgLines = block.split("\n");
            
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
                   loc.setStartColumn(Integer.parseInt(startCol)-1);
                   
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
        }
    
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new GenericEntity<List<AqlParseError>>(errors) {
                }).type("application/xml").build();
    }

}