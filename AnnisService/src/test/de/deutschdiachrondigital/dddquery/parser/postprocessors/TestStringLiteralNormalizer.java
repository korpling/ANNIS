package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newChildAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newExactSearchNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRegexpLiteralExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRegexpSearchNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRelativePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStringLiteralExpr;

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
		Start actual = newStart(newPathExpr(newRelativePathType(), steps));
		return actual;
	}
	
	// "foo" is a string literal
	@Test
	public void stringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			newStep(null, newExactSearchNodeTest("foo"), null, null, null)	
		});
		Start expected = newStart(newStringLiteralExpr("foo"));
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
	// "parent"/"child" is not a string literal
	@Test
	public void noStringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			newStep(newChildAxis(), newExactSearchNodeTest("parent"), null, null, null),	
			newStep(newChildAxis(), newExactSearchNodeTest("child"), null, null, null)	
		});
		Start expected = exampleExpr(new PStep[] {
			newStep(newChildAxis(), newExactSearchNodeTest("parent"), null, null, null),	
			newStep(newChildAxis(), newExactSearchNodeTest("child"), null, null, null)	
		});	// no change
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
	// r"foo" is a string literal
	@Test
	public void regexpStringLiteral() {
		Start actual = exampleExpr(new PStep[] {
			newStep(null, newRegexpSearchNodeTest("foo"), null, null, null)	
		});
		Start expected = newStart(newRegexpLiteralExpr("foo"));
		actual.apply(new StringLiteralNormalizer());
		actual.apply(new AstComparator(expected));
	}
		
}
