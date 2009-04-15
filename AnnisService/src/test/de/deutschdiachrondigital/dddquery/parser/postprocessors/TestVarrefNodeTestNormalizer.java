package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.AStep;

public class TestVarrefNodeTestNormalizer {

	AstBuilder b;
	
	@Before
	public void setup() {
		b = new AstBuilder();
	}
	
	// child::$v results in an element node test and a variable binding of the step
	@Test
	public void normalizeVarrefNodeTests() {
		AStep step1 = b.newStep(null, b.newVarrefNodeTest("p"));
		AStep step2 = b.newStep(null, b.newVarrefNodeTest("q"));
		APathExpr path = b.newPathExpr(b.newRelativePathType(), new AStep[] { 
			step1, step2
		});
		path.apply(new VarrefNodeTestNormalizer());
		assertThat(step1.getNodeTest(), instanceOf(AElementNodeTest.class));
		assertThat(step1.getVariable().getText(), is("p"));
		assertThat(step2.getNodeTest(), instanceOf(AElementNodeTest.class));
		assertThat(step2.getVariable().getText(), is("q"));
	}
	
}
