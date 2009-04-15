package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.AStep;
import de.deutschdiachrondigital.dddquery.node.PMarkerSpec;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestMarkerNormalizer {

	AstBuilder b;
	
	@Before
	public void setup() {
		b = new AstBuilder();
	}
	
	private AStep step(String name, PMarkerSpec marker) {
		return b.newStep(b.newChildAxis(), b.newElementNodeTest(name), marker, null, null);
	}
	
	private Start exampleExpr(PStep[] steps) {
		Start actual = b.newStart(b.newPathExpr(b.newRelativePathType(), steps));
		return actual;
	}
	
	// a#+(v)/b/c#-/d => a#(v)/b#(v)/c#(v)
	@Test
	public void marker() {
		Start actual = exampleExpr(new PStep[] {
			step("a", b.newStartMarkerSpec("v")),
			step("b", null),
			step("c", b.newEndMarkerSpec(null)),
			step("d", null),
		});
		Start expected = exampleExpr(new PStep[] {
			step("a", b.newMarkerSpec("v")),
			step("b", b.newMarkerSpec("v")),
			step("c", b.newMarkerSpec("v")),
			step("d", null),
		});
		actual.apply(new MarkerNormalizer());
		actual.apply(new AstComparator(expected));
	}

	// start and end marker definitions can nest 
	@Test
	public void markerNested() {
		Start actual = exampleExpr(new PStep[] {
			step("a", b.newStartMarkerSpec("v")),
			step("b", null),
			step("c", b.newStartMarkerSpec(null)),
			step("d", null),
			step("e", b.newEndMarkerSpec(null)),
			step("f", null),
			step("g", b.newEndMarkerSpec(null)),
			step("h", null),
		});
		Start expected = exampleExpr(new PStep[] {
			step("a", b.newMarkerSpec("v")),
			step("b", b.newMarkerSpec("v")),
			step("c", b.newMarkerSpec(null)),
			step("d", b.newMarkerSpec(null)),
			step("e", b.newMarkerSpec(null)),
			step("f", b.newMarkerSpec("v")),
			step("g", b.newMarkerSpec("v")),
			step("h", null),
		});
		actual.apply(new MarkerNormalizer());
		actual.apply(new AstComparator(expected));
	}
	
}
