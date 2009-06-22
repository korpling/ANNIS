package annis.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Sibling;

public class TestSibling {

	@Test
	public void instanceOfDominance() {
		assertThat(new Sibling(new AnnisNode(1)), instanceOf(Dominance.class));
	}
	
}
