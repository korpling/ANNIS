package de.deutschdiachrondigital.dddquery.sql;

import static de.deutschdiachrondigital.dddquery.helper.Helper.parse;
import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static de.deutschdiachrondigital.dddquery.helper.IsCollectionSize.size;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.helper.AstBuilder;
import de.deutschdiachrondigital.dddquery.node.AAndExpr;
import de.deutschdiachrondigital.dddquery.node.AOrExpr;
import de.deutschdiachrondigital.dddquery.node.APathExpr;
import de.deutschdiachrondigital.dddquery.node.Node;
import de.deutschdiachrondigital.dddquery.node.PExpr;
import de.deutschdiachrondigital.dddquery.node.Start;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.ArbitraryCondition;

public class TestGraphTranslator {

	private AstBuilder b;
	
	private GraphTranslator translator;

	private static class MockPathTranslator extends PathTranslatorA {
		int i = 0;

		@Override
		public Path translate(Node node) {
			Path path = new Path();
			path.addCondition(new ArbitraryCondition("path" + ++i));
			return path;
		}
	}

	class MockGraphTranslator extends GraphTranslator {
		{
			setPathTranslator(new MockPathTranslator());
			setGraph(new Graph());
		}
		
		@Override
		public void inAPathExpr(APathExpr node) {
			throw new AssertionError("traversal should stop at paths");
		}
	}
	
	@Before
	public void setup() {
		b = new AstBuilder();
		
		translator = new GraphTranslator();
		translator.setPathTranslator(new MockPathTranslator());
		translator.setAliasSetProvider(new AliasSetProvider());
		translator.setGraph(new Graph());
	}
	
	@Test(expected=AssertionError.class)
	public void pathsAreLeafsSetup() {
		GraphTranslator translator = new MockGraphTranslator();
		translator.inAPathExpr(null);
	}
	
	@Test
	public void pathsAreLeafs() {
		Start start = parse("a");
		// exception here if we descent into a path
		MockGraphTranslator mockDddQueryTranslator = new MockGraphTranslator();
		mockDddQueryTranslator.setAliasSetProvider(new AliasSetProvider());
		start.apply(mockDddQueryTranslator);
	}
	
	@Test
	public void onePath() {
		APathExpr node = b.newPathExpr(null, null);
		translator.caseAPathExpr(node);
		
		Graph query = translator.getGraph();
		List<Path> alternatives = query.getAlternatives();
		assertThat(alternatives, size(1));
		
		Path path = alternatives.get(0);
		assertThat(path.getConditions(), isCollection(new ArbitraryCondition("path1")));
		
	}
	
	@Test
	public void oneAlternativeWithAnd() {
		AAndExpr node = b.newAndExpr(new PExpr[] { b.newPathExpr(null, null), b.newPathExpr(null, null) });
		translator.caseAAndExpr(node);
		
		Graph query = translator.getGraph();
		List<Path> alternatives = query.getAlternatives();
		assertThat(alternatives, size(1));

		Path path = alternatives.get(0);
		assertThat(path.getConditions(), isCollection(new ArbitraryCondition("path1"), new ArbitraryCondition("path2")));
	}

	@Test
	public void manyAlternativesMixedPathAnd() {
		AOrExpr node = b.newOrExpr(new PExpr[] { 
				b.newPathExpr(null, null), 
				b.newAndExpr(new PExpr[] {
						b.newPathExpr(null, null),
						b.newPathExpr(null, null)
				})
		});
		translator.caseAOrExpr(node);
		
		Graph query = translator.getGraph();
		List<Path> alternatives = query.getAlternatives();
		
		assertThat(alternatives, size(2));
		
		Path path1 = alternatives.get(0);
		assertThat(path1.getConditions(), isCollection(new ArbitraryCondition("path1")));

		Path path2 = alternatives.get(1);
		assertThat(path2.getConditions(), isCollection(new ArbitraryCondition("path2"), new ArbitraryCondition("path3")));
	}
	
	class MockAliasSetProvider extends AliasSetProvider {
		boolean called = false;
	}
	
	@Test
	public void inAOrExprStartsUnionInAliasSetProvider() {
		MockAliasSetProvider provider = new MockAliasSetProvider() {
			@Override
			public void startUnion() {
				called = true;
			}
		};
		translator.setAliasSetProvider(provider);

		translator.inAOrExpr(null);
		assertThat(provider.called, is(true));
	}
	
	@Test
	public void outAOrExprEndsUnionInAliasSetProvider() {
		MockAliasSetProvider provider = new MockAliasSetProvider() {
			@Override
			public void endUnion() {
				called = true;
			}
		};
		translator.setAliasSetProvider(provider);

		translator.outAOrExpr(null);
		assertThat(provider.called, is(true));
	}
	
}
