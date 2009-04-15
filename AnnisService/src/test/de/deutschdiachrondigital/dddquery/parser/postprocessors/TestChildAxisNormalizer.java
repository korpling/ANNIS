package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestChildAxisNormalizer {

	AstBuilder b;
	
	public TestChildAxisNormalizer() {
		b = new AstBuilder();
	}
	
	private Start exampleExpr(AStep step) {
		Start actual = b.newStart(b.newPathExpr(b.newRelativePathType(), new PStep[] { step } ));
		return actual;
	}

	// if axis is null, set axis to AChildAxis
	@Test
	public void childAxis() {
		Start actual = exampleExpr(b.newStep(null, b.newElementNodeTest("a")));
		Start expected = exampleExpr(b.newStep(b.newChildAxis(), b.newElementNodeTest("a")));
		actual.apply(new ChildAxisNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
}
