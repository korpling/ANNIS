package de.deutschdiachrondigital.dddquery.parser.postprocessors;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.node.AAttributeNodeTest;
import de.deutschdiachrondigital.dddquery.node.AElementNodeTest;
import de.deutschdiachrondigital.dddquery.node.ASpanNodeTest;
import de.deutschdiachrondigital.dddquery.node.AStep;

public class TestNodeTestNormalizer {

	AstBuilder b;
	
	@Before
	public void setup() {
		b = new AstBuilder();
	}
	
	// attribute::name => attribute::attribute(name)
	@Test
	public void normalizeAttributeAxis() {
		AStep in = b.newStep(b.newAttributeAxis(), b.newUnknownNodeTest(null, "foo"));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(AAttributeNodeTest.class)));
		AAttributeNodeTest out = (AAttributeNodeTest) in.getNodeTest();
		assertThat(out.getName().getText(), is("foo"));
	}

	// element-span::* => element-span::span()
	@Test
	public void normalizeSpanAxis() {
		AStep in = b.newStep(b.newElementSpanAxis(), b.newUnknownNodeTest(null, null));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(ASpanNodeTest.class)));
	}
	
	// child::name => child::element(name)
	@Test
	public void normalizeChildAxis() {
		AStep in = b.newStep(b.newChildAxis(), b.newUnknownNodeTest(null, "foo"));
		in.apply(new NodeTestNormalizer());
		
		assertThat(in.getNodeTest(), is(instanceOf(AElementNodeTest.class)));
		AElementNodeTest out = (AElementNodeTest) in.getNodeTest();
		assertThat(out.getName().getText(), is("foo"));
	}
	
}
