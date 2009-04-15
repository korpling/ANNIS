package de.deutschdiachrondigital.dddquery.sql;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;

public class TestThorstenSqlGenerator {

	private Graph graph;
	private ThorstenSqlGenerator generator;

	class MockFormatter extends PathSqlGenerator {
		
		int i = 0;
		
		@Override
		public String format(Path graph) {
			return "path " + ++i + "\n";
		}
		
	}
	
	@Before
	public void setup() {
		graph = new Graph();
		generator = new ThorstenSqlGenerator();
		generator.setPathSqlGenerator(new MockFormatter());
	}
	
	@Test
	public void formatOneAlternative() {
		graph.addAlternative(new Path());
		
		String expected = "" +
				"path 1\n" +
				"\n" +
				"ORDER BY pre;\n";
		
		String actual = generator.translate(graph);
		
		assertThat(actual, is(expected));
	}
	
	@Test
	public void formatManyAlternatives() {
		graph.addAlternative(new Path());
		graph.addAlternative(new Path());
		graph.addAlternative(new Path());
		
		String expected = "" +
				"path 1\n" +
				"\n" +
				"UNION path 2\n" +
				"\n" +
				"UNION path 3\n" +
				"\n" +
				"ORDER BY pre;\n";
		
		String actual = generator.translate(graph);
		
		assertThat(actual, is(expected));
	}
	
}
