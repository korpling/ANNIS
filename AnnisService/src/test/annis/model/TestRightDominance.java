package annis.model;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.RightDominance;

public class TestRightDominance {

	@Test
	public void instanceOfDominance() {
		assertThat(new RightDominance(new AnnisNode(1)), instanceOf(Dominance.class));
	}
	
}
