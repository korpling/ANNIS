package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestStringLiteralNormalizer {

	AstBuilder b;
	
	@Before
	public void setup() {
		b = new AstBuilder();
	}
	
	private Start exampleExpr(PStep[] steps) {
		Start actual = b.newStart(b.newPathExpr(b.newRelativePathType(), steps));
		return actual;
	}
	
	// "foo" is a string literal
	@Test
	public void stringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			b.newStep(null, b.newExactSearchNodeTest("foo"), null, null, null)	
		});
		Start expected = b.newStart(b.newStringLiteralExpr("foo"));
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
	// "parent"/"child" is not a string literal
	@Test
	public void noStringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			b.newStep(b.newChildAxis(), b.newExactSearchNodeTest("parent"), null, null, null),	
			b.newStep(b.newChildAxis(), b.newExactSearchNodeTest("child"), null, null, null)	
		});
		Start expected = exampleExpr(new PStep[] {
			b.newStep(b.newChildAxis(), b.newExactSearchNodeTest("parent"), null, null, null),	
			b.newStep(b.newChildAxis(), b.newExactSearchNodeTest("child"), null, null, null)	
		});	// no change
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
	// r"foo" is a string literal
	@Test
	public void regexpStringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			b.newStep(null, b.newRegexpSearchNodeTest("foo"), null, null, null)	
		});
		Start expected = b.newStart(b.newRegexpLiteralExpr("foo"));
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
	
}
