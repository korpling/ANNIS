package de.deutschdiachrondigital.dddquery.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.deutschdiachrondigital.dddquery.analysis.DepthFirstAdapter;
import de.deutschdiachrondigital.dddquery.helper.TreeDumper;
import de.deutschdiachrondigital.dddquery.lexer.Lexer;
import de.deutschdiachrondigital.dddquery.lexer.LexerException;
import de.deutschdiachrondigital.dddquery.node.Start;

/**
 * A class that creates a syntax tree from a DDDquery statement, according to the 
 * grammar defined in <tt>dddquery.grammar</tt>.
 * 
 * <p>
 * The syntax tree can be modified by setting a list of post-processors (instances of
 * {@link DepthFirstAdapter}). The post-processors will be called in the order they appear 
 * in the list.
 * 
 * <p>
 * Note: The SableCC implementation of {@link DepthFirstAdapter} is not thread-safe.  If 
 * {@link DddQueryParser} is used in a multi-threaded environment the method 
 * {@link DddQueryParser.getPostProcessors()} should be overwritten, so that each call to
 * {@link DddQueryParser.parse()} operates on a fresh list.  This is done automatically
 * when the instance is configured using Spring (see <tt>annis-service.xml</tt>).
 * 
 * <p>
 * Exception that are thrown by SableCC ({@link ParserException}, {@link LexerException} and 
 * {@link IOException}) are wrapped in an unchecked {@link ParseException}.
 * 
 * @author Viktor Rosenfeld
 */
public class DddQueryParser {

	private static Logger log = Logger.getLogger(DddQueryParser.class);

	// extra class to allow stubbing in tests
	public static class InternalParser {

		public Start parse(String input)
		throws ParserException, LexerException, IOException {
			return new Parser(new Lexer(new PushbackReader(new StringReader(input), 3000))).parse();
		}

	}
	private InternalParser internalParser;

	// holds a list of post-processors
	private List<DepthFirstAdapter> postProcessors;
	
	/**
	 * Creates a parser for DDDquery statements with an empty post-processor list.
	 */
	public DddQueryParser() {
		postProcessors = new ArrayList<DepthFirstAdapter>();
	}

	public Start parse(String dddQuery) {
		try {
			log.debug("parsing DDDquery: " + dddQuery);

			// build and post-process syntax tree
			Start start = getInternalParser().parse(dddQuery);
			
			for (DepthFirstAdapter postProcessor : getPostProcessors()) {
				log.debug("applying post processor to syntax tree: " + postProcessor.getClass().getSimpleName());
				start.apply(postProcessor);
			}
			
			log.debug("syntax tree is:\n" + dumpTree(start));
			return start;

		} catch (ParserException e) {
			log.warn("an exception occured on the query: " + dddQuery, e);
			throw new ParseException("error parsing: " + dddQuery + "; " + e.getMessage(), e);
		} catch (LexerException e) {
			log.warn("an exception occured on the query: " + dddQuery, e);
			throw new ParseException(e);
		} catch (IOException e) {
			log.warn("an exception occured on the query: " + dddQuery, e);
			throw new ParseException(e);
		}
	}

	public static String dumpTree(Start start) {
		try {
			StringWriter result = new StringWriter();
			start.apply(new TreeDumper(new PrintWriter(result)));
			return result.toString();
		} catch (RuntimeException e) {
			String errorMessage = "could not serialize syntax tree";
			log.warn(errorMessage, e);
			return errorMessage;
		}
	}

	///// Getter / Setter
	
	public List<DepthFirstAdapter> getPostProcessors() {
		return postProcessors;
	}

	public void setPostProcessors(List<DepthFirstAdapter> postProcessors) {
		this.postProcessors = postProcessors;
	}

	protected void setInternalParser(InternalParser parser) {
		this.internalParser = parser;
	}

	protected InternalParser getInternalParser() {
		return internalParser;
	}
	
}
