package ex.annis.ql.lexer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import org.junit.Test;

import ex.annis.ql.node.EOF;
import ex.annis.ql.node.TAnd;
import ex.annis.ql.node.TBlanks;
import ex.annis.ql.node.TDigits;
import ex.annis.ql.node.TEqual;
import ex.annis.ql.node.THash;
import ex.annis.ql.node.TId;
import ex.annis.ql.node.TLPar;
import ex.annis.ql.node.TLeftDominance;
import ex.annis.ql.node.TOpArity;
import ex.annis.ql.node.TOpDominance;
import ex.annis.ql.node.TOpExactOverlap;
import ex.annis.ql.node.TOpInclusion;
import ex.annis.ql.node.TOpLeftAlign;
import ex.annis.ql.node.TOpLeftOverlap;
import ex.annis.ql.node.TOpPrecedence;
import ex.annis.ql.node.TOpRightAlign;
import ex.annis.ql.node.TOpRoot;
import ex.annis.ql.node.TOpSameAnnotationGroup;
import ex.annis.ql.node.TOpSibling;
import ex.annis.ql.node.TOpTokenArity;
import ex.annis.ql.node.TOr;
import ex.annis.ql.node.TRPar;
import ex.annis.ql.node.TRegexp;
import ex.annis.ql.node.TRegexpLQuote;
import ex.annis.ql.node.TRegexpRQuote;
import ex.annis.ql.node.TRegularLQuote;
import ex.annis.ql.node.TRegularRQuote;
import ex.annis.ql.node.TRightDominance;
import ex.annis.ql.node.TText;
import ex.annis.ql.node.TXor;
import ex.annis.ql.node.Token;

public class TestLexer {
	
	/**
	 * Takes an input string and checks that the Lexer produces the right tokens.
	 * 
	 * @param input		the string that should be tokenized.
	 * @param tokens	the expected tokens.
	 * @param tokenStrings	the expected string representations of the tokens.	
	 */
	void checkTokenization(String input, Class[] tokens, String[] tokenStrings) throws LexerException, IOException {
		
		if (tokens.length != tokenStrings.length) 
			fail("usage error: different count for tokens and string represenation");
		
		Lexer lexer = new Lexer(new PushbackReader(new StringReader(input)));
		
		int i = 0;
		while (true) {
			Token token = lexer.next();
			
			if (token instanceof EOF) {
				assertTrue("input string not completely tokenized", i == tokens.length);
				return;
			}
			
			assertEquals("wrong kind of token given by lexer: " + i, tokens[i], token.getClass());
			assertEquals("token is not right: " + i, tokenStrings[i], token.toString().trim());
			
			++i;
		}
	}
	
	/**
	 * Convenience method for input strings that produce exactly one token.
	 * 
	 * @param input		the string that should be tokenized.
	 * @param token		the expected token.
	 */
	void checkTokenization(String input, Class token) throws LexerException, IOException {
		checkTokenization(input, new Class[] { token }, new String[] { input } );
	}
		
	@Test
	public void normalPattern() throws LexerException, IOException {
		checkTokenization(
				"typ=\"wert\"", 
				new Class[] { TId.class, TEqual.class, TRegularLQuote.class, TText.class, TRegularRQuote.class	}, 
				new String[]{ "typ", "=", "\"", "wert", "\""});
	}
	
	@Test
	public void regexpPattern() throws IOException, LexerException {
		checkTokenization(
				"typ=/regexp/",
				new Class[] { TId.class, TEqual.class, TRegexpLQuote.class, TRegexp.class, TRegexpRQuote.class },
				new String[] { "typ", "=", "/", "regexp", "/"
		});
	}
	
	@Test
	public void grouping() throws LexerException, IOException {
		for (String paren : new String[] { "(", "[", "{" } )
			checkTokenization(paren, TLPar.class);
		
		for (String paren : new String[] { ")", "]", "}" } ) {
			checkTokenization(paren, TRPar.class);
		}
	}
	
	@Test
	public void booleanOperators() throws LexerException, IOException {
		checkTokenization("&", TAnd.class);
		checkTokenization("|", TOr.class);
		checkTokenization("^", TXor.class);
	}
	
	@Test
	public void patternReference() throws LexerException, IOException {
		checkTokenization(
				"#123", 
				new Class[] { THash.class, TDigits.class },
				new String[] { "#", "123" });
	}
	
	@Test
	public void linguisticOperators() throws LexerException, IOException {
		checkTokenization("_=_", TOpExactOverlap.class);
		checkTokenization("_l_", TOpLeftAlign.class);
		checkTokenization("_r_", TOpRightAlign.class);
		checkTokenization("_i_", TOpInclusion.class);
		checkTokenization("_ol_", TOpLeftOverlap.class);
		
		checkTokenization(".", TOpPrecedence.class);
		
		checkTokenization(">", TOpDominance.class);
		
		checkTokenization("@l", TLeftDominance.class);
		checkTokenization("@r", TRightDominance.class);
		
		checkTokenization("$", TOpSibling.class);
		
		checkTokenization("@", TOpSameAnnotationGroup.class);
		
		checkTokenization(":arity", TOpArity.class);
		
		checkTokenization(":root", TOpRoot.class);
		checkTokenization(":tokenarity", TOpTokenArity.class);
	}
	
	@Test
	public void underscoreInAllowedHelperBreakage() throws LexerException, IOException {
		checkTokenization(
				"#1_=_#2", 
				new Class[] { THash.class, TDigits.class, TOpExactOverlap.class, THash.class, TDigits.class}, 
				new String[] { "#", "1", "_=_", "#", "2" } );
	}
	
	@Test
	public void complexExample() throws LexerException, IOException {
		String input = "typ1 & typ2=\"text\"&typ3=/regexp/ & { (#1 _=_ #2) | [#1_=_#3] )";
		
		Class[] tokens = {
				TId.class,
				TBlanks.class, TAnd.class, TBlanks.class,
				TId.class, TEqual.class, TRegularLQuote.class, TText.class, TRegularRQuote.class,
				TAnd.class,
				TId.class, TEqual.class, TRegexpLQuote.class, TRegexp.class, TRegexpRQuote.class,
				TBlanks.class, TAnd.class, TBlanks.class,
				TLPar.class, TBlanks.class,
					TLPar.class, THash.class, TDigits.class, TBlanks.class, TOpExactOverlap.class, TBlanks.class, THash.class, TDigits.class, TRPar.class,
					TBlanks.class, TOr.class, TBlanks.class,
					TLPar.class, THash.class, TDigits.class, TOpExactOverlap.class, THash.class, TDigits.class, TRPar.class,
				TBlanks.class, TRPar.class
		};
		
		String[] tokenStrings = {
				"typ1",
				"", "&", "",
				"typ2", "=", "\"", "text", "\"",
				"&",
				"typ3", "=", "/", "regexp", "/",
				"", "&", "",
				"{", "",
					"(", "#", "1", "", "_=_", "", "#", "2", ")",
					"", "|", "",
					"[", "#", "1", "_=_", "#", "3", "]",
				"", ")"
		};
		checkTokenization(input, tokens, tokenStrings);
	}
}
