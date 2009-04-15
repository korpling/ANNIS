package de.deutschdiachrondigital.dddquery.sql.preprocessors;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.helper.AstComparator;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.PStep;
import de.deutschdiachrondigital.dddquery.node.Start;

public class TestLeadingChildAxisRemover {

	private AstBuilder b;
	
	@Before
	public void setup() {
		b = new AstBuilder();
	}
	
	private void testForNoChange(Start node) {
		Start expected = (Start) node.clone();
		testForChange(node, expected);
	}
	
	private void testForChange(Start node, Start expected) {
		node.apply(new LeadingChildAxisRemover());
		// exception here if trees differ
		node.apply(new AstComparator(expected));
	}
	
	// remove FIRST child axis in a RELATIVE path
	@Test
	public void firstAxisIsChildRelativePath() {
		Start node = b.newStart(b.newPathExpr(b.newRelativePathType(), new PStep[] {
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
		}));
		
		Start expected = b.newStart(b.newPathExpr(b.newRelativePathType(), new PStep[] {
			b.newStep(null, b.newElementNodeTest(null)),
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
		}));
	
		testForChange(node, expected);
	}

	// DON'T remove first child axis in an ABSOLUTE path
	@Test
	public void firstAxisIsChildAbsolutePath() {
		Start node = b.newStart(b.newPathExpr(b.newAbsolutePathType(), new PStep[] {
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
		}));
		testForNoChange(node);
	}
	
	// only check child axis
	@Test
	public void firstAxisNotChild() {
		Start node = b.newStart(b.newPathExpr(b.newRelativePathType(), new PStep[] {
			b.newStep(b.newParentAxis(), b.newElementNodeTest(null)),
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
		}));
		testForNoChange(node);
	}
	
	// DON'T look into predicates
	@Test
	public void firstAxisInPredicate() {
		PExpr predicate = b.newPathExpr(b.newRelativePathType(), new PStep[] {
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null)),
		});
		Start node = b.newStart(b.newPathExpr(b.newAbsolutePathType(), new PStep[] {
			b.newStep(b.newChildAxis(), b.newElementNodeTest(null), null, new PExpr[] { predicate }, null)
		}));
		testForNoChange(node);
	}
	
}
