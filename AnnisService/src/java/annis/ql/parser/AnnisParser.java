package annis.ql.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import annis.exceptions.ParseException;
import annis.ql.analysis.DepthFirstAdapter;
import annis.ql.lexer.Lexer;
import annis.ql.lexer.LexerException;
import annis.ql.node.Start;

public class AnnisParser {

	private static Logger log = Logger.getLogger(AnnisParser.class);
	
	// extra class to allow stubbing in tests
	public static class InternalParser {

		public Start parse(String input) throws ParserException, LexerException, IOException {
			return new Parser(new Lexer(new PushbackReader(new StringReader(input), 3000))).parse();
		}

	}
	private InternalParser internalParser;
	
	// holds a list of post-processors
	private List<DepthFirstAdapter> postProcessors;
	
	/**
	 * Creates a parser for AnnisQL statements.
	 */
	public AnnisParser() {
		postProcessors = new ArrayList<DepthFirstAdapter>();
		postProcessors.add(new NodeSearchNormalizer());
		postProcessors.add(new TokenSearchNormalizer());
		postProcessors.add(new QueryValidator());
		internalParser = new InternalParser();
	}

	public Start parse(String annisQuery) {
		try {
			log.info("parsing Annis query: " + annisQuery);

			// build and post-process syntax tree
			Start start = getInternalParser().parse(annisQuery);
			
			for (DepthFirstAdapter postProcessor : getPostProcessors()) {
				log.debug("applying post processor to syntax tree: " + postProcessor.getClass().getSimpleName());
				start.apply(postProcessor);
			}
			
			log.debug("syntax tree is:\n" + dumpTree(start));
			return start;

		} catch (ParserException e) {
			log.warn("an exception occured on the query: " + annisQuery, e);
			throw new ParseException("error parsing: " + annisQuery + "; " + e.getMessage(), e);
		} catch (LexerException e) {
			log.warn("an exception occured on the query: " + annisQuery, e);
			throw new ParseException(e);
		} catch (IOException e) {
			log.warn("an exception occured on the query: " + annisQuery, e);
			throw new ParseException(e);
		}
	}
	
	public String dumpTree(String annisQuery) {
		return dumpTree(parse(annisQuery));
	}
	
	public String dumpTree(Start start) {
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
