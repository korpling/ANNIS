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

public class TestAnnotationRetrieverSqlGenerator2 {

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
			tokenRelation.append("		SELECT '{");
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (Span node : match) {
				min = Math.min(min, node.getTokenLeft());
				max = Math.max(max, node.getTokenRight());
				tokenRelation.append(node.getStructId());
				tokenRelation.append(", ");
			}
			tokenRelation.setLength(tokenRelation.length() - ", ".length());
			tokenRelation.append("}'::numeric[] AS key, ");
			tokenRelation.append(TEXT_REF);
			tokenRelation.append(" AS text_ref, ");
			tokenRelation.append(min - LEFT);
			tokenRelation.append(" AS min, ");
			tokenRelation.append(max + RIGHT);
			tokenRelation.append(" AS max UNION\n");
		}
		tokenRelation.setLength(tokenRelation.length() - " UNION\n".length());
		tokenRelation.append("\n	) AS tokens");
		
		String expected = "" +
				"SELECT DISTINCT\n" +
				"	tokens.key, rank2.pre, rank2.post, annotations.*\n" +
				"FROM\n" +
				tokenRelation + ",\n" +
				"	struct struct,\n" +
				"	rank rank1,\n" +
				"	rank2 rank2,\n" +
				"	annotations annotations\n" +
				"WHERE\n" +
				"	tokens.text_ref = struct.text_ref AND\n" +
				"	tokens.min <= struct.token_count AND\n" +
				"	tokens.max >= struct.token_count AND\n" +
				"	rank1.struct_ref = struct.id AND\n" +
				"	rank1.post > rank2.pre AND\n" +
				"	rank2.post >= rank1.post AND\n" +
				"	rank2.struct_ref = annotations.struct\n" +
				"ORDER BY tokens.key, rank2.pre";
		
		SqlGenerator generator = new AnnotationRetrieverSqlGenerator2();
		
		assertEquals(expected, generator.generateSql(MATCHES, LEFT, RIGHT));
		System.out.println(expected);
	}

}
