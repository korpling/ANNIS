package de.deutschdiachrondigital.dddquery.sql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.deutschdiachrondigital.dddquery.sql.model.AliasSet;
import de.deutschdiachrondigital.dddquery.sql.model.Path;
import de.deutschdiachrondigital.dddquery.sql.model.conditions.Join;

// TODO: eine Klasse, die die Generierung des SQL-Statements übernimmt
public class TestPathFormatter {
	
	Path path;
	PathSqlGenerator formatter;
	
	@Before
	public void setup() {
		path = new Path();
		formatter = new PathSqlGenerator();
	}
	
	@Test
	public void selectClause() {
		String expected = "" +
			"SELECT DISTINCT\n" +
			"\toutput.pre AS PRE,\n" +
			"\toutput.post AS POST,\n" +
			"\toutput.struct_id AS STRUCT,\n" +
			"\toutput.name AS NAME,\n" +
			"\toutput.text_ref AS TEXT_ID,\n" +
			"\toutput.left AS LEFT,\n" +
			"\toutput.right AS RIGHT,\n" +
			"\toutput.span AS SPAN,\n" +
			"\toutput.anno_level || ':' || output.anno_name AS ATTRIBUTE,\n" +
			"\toutput.anno_value AS VALUE,\n";
		assertThat(formatter.selectClause(path), is(expected));
	}
	
	@Test(expected=RuntimeException.class)
	public void selectClauseMarkingsNoMark() {
		formatter.selectClauseMarkings(path);
	}
	
	@Test
	public void selectClauseMarkingsOneMark() {
		path.markAliasSet(new AliasSet(1), "foo");
		assertThat(formatter.selectClauseMarkings(path), is("\t'foo' AS MARKER\n"));
	}
	
	@Test
	public void selectClauseMarkingsManyMarks() {
		String[] marks = { "one", "two", "three" };
		AliasSetProvider provider = new AliasSetProvider();
		for (String mark : marks)
			path.markAliasSet(provider.getAliasSet(mark), mark);
		
		String expected = "" +
				"\tCASE\n" +
				"\t\tWHEN output.pre = rank1.pre THEN 'one'\n" +
				"\t\tWHEN output.pre = rank2.pre THEN 'two'\n" +
				"\t\tWHEN output.pre = rank3.pre THEN 'three'\n" +
				"\tEND AS MARKER\n";
		
		assertThat(formatter.selectClauseMarkings(path), is(expected));
	}
	
	@Test
	public void fromClauseEmptyQuery() {
		String expected = "" +
			"FROM\n" +
			"\t( (rank AS rank0 INNER JOIN struct AS struct0 (struct_id, text_ref, doc_id, name, \"left\", \"right\", token_count, cont, span) ON rank0.struct_ref = struct0.struct_id) LEFT OUTER JOIN (anno AS anno0 (anno_id, struct_ref, anno_level) INNER JOIN anno_attribute AS anno_attribute0 (anno_ref, anno_name, anno_value) ON anno_attribute0.anno_ref = anno0.anno_id) ON anno0.struct_ref = struct0.struct_id ) output\n";
		assertThat(formatter.fromClause(path), is(expected));		
	}
	
	@Test
	public void fromClauseSomeAliasSets() {
		String expected = "" +
			"FROM\n" +
			"\t( (rank AS rank0 INNER JOIN struct AS struct0 (struct_id, text_ref, doc_id, name, \"left\", \"right\", token_count, cont, span) ON rank0.struct_ref = struct0.struct_id) LEFT OUTER JOIN (anno AS anno0 (anno_id, struct_ref, anno_level) INNER JOIN anno_attribute AS anno_attribute0 (anno_ref, anno_name, anno_value) ON anno_attribute0.anno_ref = anno0.anno_id) ON anno0.struct_ref = struct0.struct_id ) output,\n" +
			"\telement element1, rank rank1,\n" +
			"\trank rank2\n";
	
		AliasSet aliasSet1 = new AliasSet(1);
		Set<String> tables1 = new HashSet<String>();
		tables1.add("rank");
		tables1.add("element");
		aliasSet1.setUsedTables(tables1);
		path.getAliasSets().add(aliasSet1);
		
		AliasSet aliasSet2 = new AliasSet(2);
		Set<String> tables2 = new HashSet<String>();
		tables2.add("rank");
		aliasSet2.setUsedTables(tables2);
		path.getAliasSets().add(aliasSet2);
		
		String actual = formatter.fromClause(path);
//		System.out.println(actual);
		assertThat(actual, is(expected));
	}
	
	@Test
	public void whereClauseNoConditions() {
		assertThat(formatter.whereClause(path), is(""));
	}
	
	@Test
	public void whereClauseSomeConditions() {
		String expected = "" +
			"WHERE\n" +
			"\trank1.pre = rank2.parent AND\n" +
			"\telement2.name = 'foo' AND\n";
		
		path.addCondition(Join.eq("rank1.pre", "rank2.parent"));
		path.addCondition(Join.eq("element2.name", "'foo'"));
		
		assertThat(formatter.whereClause(path), is(expected));
	}

	@Test(expected=RuntimeException.class)
	public void whereClauseOutputSelectionNoMark() {
		formatter.whereClauseOutputSelection(path);
	}
	
	@Test
	public void whereClauseOutputSelectionOneMark() {
		String expected = "\toutput.pre = rank1.pre\n";
		
		path.markAliasSet(new AliasSet(1), null);
		
		assertThat(formatter.whereClauseOutputSelection(path), is(expected));
	}

	@Test
	public void whereClauseClauseOutputSelectionManyMarks() {
		String expected = "" +
				"\t(\n" +
				"\t\toutput.pre = rank1.pre OR\n" +
				"\t\toutput.pre = rank2.pre OR\n" +
				"\t\toutput.pre = rank3.pre\n" +
				"\t)\n";
		
		String[] marks = { "one", "two", "three" };
		AliasSetProvider provider = new AliasSetProvider();
		for (String mark : marks)
			path.markAliasSet(provider.getAliasSet(mark), mark);
		
		assertThat(formatter.whereClauseOutputSelection(path), is(expected));
	}

//	@Test
	public void whereClauseAliasJoinsRankAnno() {
		AliasSet target = new AliasSet(1);
		path.addAliasSet(target);
		target.useTable("rank");
		target.usesTable("anno");
		assertThat(formatter.whereClauseAliasJoins(path), is("\trank1.struct_ref = struct1.id AND\n\tanno1.struct_ref = anno1.id"));
	}
	
	@Test
	public void whereClauseAliasJoinsRankStruct() {
		AliasSet target = new AliasSet(1);
		path.addAliasSet(target);
		target.useTable("rank");
		target.useTable("struct");
		assertThat(formatter.whereClauseAliasJoins(path), is("\trank1.struct_ref = struct1.id AND\n"));
	}
	
	@Test
	public void whereClauseAliasJoinsStructAnno() {
		AliasSet target = new AliasSet(1);
		path.addAliasSet(target);
		target.useTable("struct");
		target.useTable("anno");
		assertThat(formatter.whereClauseAliasJoins(path), is("\tanno1.struct_ref = struct1.id AND\n"));
	}

	@Test
	public void whereClauseAliasJoinsAnnoAnnoAttribute() {
		AliasSet target = new AliasSet(1);
		path.addAliasSet(target);
		target.useTable("anno");
		target.useTable("anno_attribute");
		assertThat(formatter.whereClauseAliasJoins(path), is("\tanno_attribute1.anno_ref = anno1.id AND\n"));
	}

}
