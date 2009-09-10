package annis.dao;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestListCorpusByNameDaoHelper {

	@Test
	public void createSql() {
		final ListCorpusByNameDaoHelper listCorpusByNameDaoHelper = new ListCorpusByNameDaoHelper();
		
		// a few dummy corpus names
		List<String> corpusNames = Arrays.asList("a", "b", "c");
		
		String expected = "SELECT id FROM corpus WHERE name IN ( 'a', 'b', 'c' ) AND top_level = 't'";
		
		assertEquals(expected, listCorpusByNameDaoHelper.createSql(corpusNames));
	}
}
