package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newChildAxis;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newElementNodeTest;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newPathExpr;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newRelativePathType;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStart;
import static de.deutschdiachrondigital.dddquery.helper.AstBuilder.newStep;

import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestChildAxisNormalizer {

	private Start exampleExpr(AStep step) {
		Start actual = newStart(newPathExpr(newRelativePathType()));
		return actual;
	}

	// if axis is null, set axis to AChildAxis
	@Test
	public void childAxis() {
		Start actual = exampleExpr(newStep(null, newElementNodeTest("a")));
		Start expected = exampleExpr(newStep(newChildAxis(), newElementNodeTest("a")));
		actual.apply(new ChildAxisNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
}
