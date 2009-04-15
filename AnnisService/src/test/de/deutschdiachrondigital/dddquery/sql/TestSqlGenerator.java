package de.deutschdiachrondigital.dddquery.sql;

import static de.deutschdiachrondigital.dddquery.helper.IsCollection.isCollection;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Condition;
import de.deutschdiachrondigital.dddquery.sql.model.Graph;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.ArbitraryCondition;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;

public class TestSqlGenerator {

	private SqlGenerator generator;

	@Before
	public void setup() {
		generator = new SqlGenerator();
	}
	
	class MockSqlGenerator extends SqlGenerator {
		
		int i = 0;
		
		@Override
		String translatePath(Path path) {
			return "path " + ++i;
		}
		
	}
	
	@Test
	public void translateOnePath() {
		generator = new MockSqlGenerator();
		
		Graph graph = new Graph();
		graph.addAlternative(new Path());
		
		assertThat(generator.translate(graph), is("path 1\n"));
	}
	
	@Test
	public void translateManyPaths() {
		generator = new MockSqlGenerator();
		
		Graph graph = new Graph();
		graph.addAlternative(new Path());
		graph.addAlternative(new Path());
		graph.addAlternative(new Path());
		
		assertThat(generator.translate(graph), is("path 1\nUNION path 2\nUNION path 3\n"));
	}
	
	@Test
	public void translatePath() {
		generator = new SqlGenerator() {
			
			@Override
			String selectClause(Path path) {
				return "SELECT ...\n";
			}
			
			@Override
			String fromClause(Path path) {
				return "FROM ...\n";
			}
			
			@Override
			String whereClause(Path path) {
				return "WHERE ...\n";
			}
		};
		
		assertThat(generator.translatePath(null), is("SELECT ...\nFROM ...\nWHERE ...\n"));
	}
	
	@Test
	public void selectClause() {
		Path path = new Path();
		AliasSet[] aliasSets = {
				new AliasSet(1), new AliasSet(2), new AliasSet(3), new AliasSet(4), new AliasSet(5)
		};
		
		for (AliasSet aliasSet : aliasSets) {
			path.addAliasSet(aliasSet);
		}
		
		path.markAliasSet(aliasSets[0], "a1");
		path.markAliasSet(aliasSets[3], "a2");
		
		String expected = "SELECT DISTINCT\n\tstruct1.id AS a1, struct1.text_ref, struct1.token_left, struct1.token_right, \n\tstruct4.id AS a2, struct4.text_ref, struct4.token_left, struct4.token_right\n";
		assertThat(generator.selectClause(path), is(expected));
	}
	
	@Test
	public void fromClause() {
		Path path = new Path();
		AliasSet[] aliasSets = {
				new AliasSet(1), new AliasSet(2), new AliasSet(3), new AliasSet(4), new AliasSet(5)
		};
		
		for (AliasSet aliasSet : aliasSets) {
			path.addAliasSet(aliasSet);
		}
		
		aliasSets[0].useTable("rank");
		aliasSets[0].useTable("struct");
		aliasSets[1].useTable("rank");
		aliasSets[3].useTable("struct");
		aliasSets[3].useTable("anno");
		
		String expected = "" +
				"FROM\n" +
				"\trank rank1, struct struct1,\n" +
				"\trank rank2,\n" +
				"\tstruct struct4, anno anno4\n";
		assertThat(generator.fromClause(path), is(expected));
	}
	
	@Test
	public void whereClause() {
		generator = new SqlGenerator() {
			@Override
			List<Condition> missingJoins(AliasSet aliasSet) {
				List<Condition> missing = new ArrayList<Condition>();
				missing.add(new ArbitraryCondition("missing 1"));
				missing.add(new ArbitraryCondition("missing 2"));
				return missing;
			}
		};
		
		Path path = new Path();
		path.addAliasSet(new AliasSet(1));
		path.addCondition(new ArbitraryCondition("condition 1"));
		path.addCondition(new ArbitraryCondition("condition 2"));
		
		String expected = "" +
				"WHERE\n" +
				"\tcondition 1 AND\n" +
				"\tcondition 2 AND\n" +
				"\tmissing 1 AND\n" +
				"\tmissing 2\n";
		assertThat(generator.whereClause(path), is(expected));
	}
	
	@Test
	public void missingJoinsRankStruct() {
		AliasSet aliasSet = new AliasSet(1);
		aliasSet.useTable("rank");
		aliasSet.useTable("struct");
		assertThat(generator.missingJoins(aliasSet), isCollection(Join.eq("rank1.struct_ref", "struct1.id")));
	}

	@Test
	public void missingJoinsStructAnno() {
		AliasSet aliasSet = new AliasSet(1);
		aliasSet.useTable("struct");
		aliasSet.useTable("anno");
		assertThat(generator.missingJoins(aliasSet), isCollection(Join.eq("anno1.struct_ref", "struct1.id")));
	}

	@Test
	public void missingJoinsAnnoAnnoAttribute() {
		AliasSet aliasSet = new AliasSet(1);
		aliasSet.useTable("anno");
		aliasSet.useTable("anno_attribute");
		assertThat(generator.missingJoins(aliasSet), isCollection(Join.eq("anno_attribute1.anno_ref", "anno1.id")));
	}

}
