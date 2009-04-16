package de.deutschdiachrondigital.dddquery.sql.old2;

import static de.deutschdiachrondigital.dddquery.helper.TestHelpers.uniqueInt;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.dao.Match;
import annis.dao.Span;
import de.deutschdiachrondigital.dddquery.sql.old2.AnnotationRetriever.SqlGenerator;

public class TestAnnotationRetrieverSqlGenerator3 {

	private final int NUM_MATCHES = 3;
	private final List<Match> MATCHES = new ArrayList<Match>();
	private final int TEXT_REF = uniqueInt();
	private final int LEFT = uniqueInt();
	private final int RIGHT = uniqueInt();
	
	@Before
	public void setupMatches() {
		for (int i = 0; i < NUM_MATCHES; ++i)
			MATCHES.add(new Match(Arrays.asList(
					new Span(uniqueInt(), TEXT_REF, uniqueInt(), uniqueInt()),
					new Span(uniqueInt(), TEXT_REF, uniqueInt(), uniqueInt()))));
	}
	
	@Test
	public void generateSql() {
		StringBuffer tokenRelation = new StringBuffer();
		tokenRelation.append("	(\n");

		for (Match match : MATCHES) {
			tokenRelation.append("		SELECT * FROM ( SELECT '{");
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
				tokenRelation.append(node.getStructId());
				tokenRelation.append(", ");
			}
			tokenRelation.setLength(tokenRelation.length() - ", ".length());
			tokenRelation.append("}'::numeric[] AS key ) AS key, graph_over_tokens(");
			tokenRelation.append(TEXT_REF);
			tokenRelation.append(", ");
			tokenRelation.append(min - LEFT);
			tokenRelation.append(", ");
			tokenRelation.append(max + RIGHT);
			tokenRelation.append(") AS graph UNION\n");
		}
		tokenRelation.setLength(tokenRelation.length() - " UNION\n".length());
		tokenRelation.append("\n	) AS graph");
		
		String expected = "" +
			"SELECT DISTINCT\n" +
			"	graph.key, graph.pre, graph.post, annotations.*\n" +
			"FROM\n" +
			tokenRelation + "\n" +
			"	JOIN annotations ON (graph.struct_ref = annotations.struct)\n" +
			"ORDER BY graph.key, graph.pre";
		
		SqlGenerator generator = new AnnotationRetrieverSqlGenerator3();
		
		assertEquals(expected, generator.generateSql(MATCHES, LEFT, RIGHT));
		System.out.println(expected);
	}

}
