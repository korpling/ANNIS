package annis.sqlgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.IsCollectionSize.size;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.service.ifaces.AnnisAttribute;
import annis.service.objects.AnnisAttributeImpl;
import java.util.LinkedList;
import org.junit.Ignore;

public class TestListAnnotationsSqlHelper {

	// object under test
	private ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	
	// dummy annotation data
	private static final String NULL = null;
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	private static final String NAME3 = "NAME3";
	private static final String VALUE1 = "VALUE1";
	private static final String VALUE2 = "VALUE2";
	private static final String VALUE3 = "VALUE3";

	@Before
	public void setup() {
		listNodeAnnotationsSqlHelper = new ListAnnotationsSqlHelper();
	}

  public void createSqlQueryNoEmptyCorpusList()
  {
    try
    {
      listNodeAnnotationsSqlHelper.createSqlQuery(null, true, true);
    }
    catch(IllegalArgumentException ex)
    {
      return;
    }
    fail("should throw illegal argument exception on empty corpus list");
  }

  @Test
  public void createSqlQueryOnlyNames()
  {
    String expected = 
      "select 'node' as \"type\", namespace, name, value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY namespace, name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    node_annotation_namespace as namespace, node_annotation_name as name, NULL::varchar AS value, \n"
     + "    count(node_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    node_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (1977, 1988)\n"
     + "    GROUP BY node_annotation_namespace, node_annotation_name, node_annotation_value\n"
     + "    ORDER by node_annotation_namespace, node_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n"
     + "where row_num = 1\n"
     + "UNION ALL\n"
     + "select 'edge' as \"type\", namespace, name, value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY namespace, name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    edge_annotation_namespace as namespace, edge_annotation_name as name, NULL::varchar AS value, \n"
     + "    count(edge_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    edge_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (1977, 1988)\n"
     + "    GROUP BY edge_annotation_namespace, edge_annotation_name, edge_annotation_value\n"
     + "    ORDER by edge_annotation_namespace, edge_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n"
     + "where row_num = 1\n";
     
    List<Long> corpora = new LinkedList<Long>();
    corpora.add(1977l);
    corpora.add(1988l);
    assertEquals(expected, listNodeAnnotationsSqlHelper.createSqlQuery(corpora, false, false));
    assertEquals(expected, listNodeAnnotationsSqlHelper.createSqlQuery(corpora, false, true));
  }

  @Test
  @Ignore
  public void createSqlQueryListAllValues()
  {
    String expected = 
      "select 'node' as \"type\", namespace, name, value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY namespace, name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    node_annotation_namespace as namespace, node_annotation_name as name, node_annotation_value AS value, \n"
     + "    count(node_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    node_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (1977, 1988)\n"
     + "    GROUP BY node_annotation_namespace, node_annotation_name, node_annotation_value\n"
     + "    ORDER by node_annotation_namespace, node_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n"
     + "UNION ALL\n"
     + "select 'edge' as \"type\", namespace, name, value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY namespace, name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    edge_annotation_namespace as namespace, edge_annotation_name as name, edge_annotation_value AS value, \n"
     + "    count(edge_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    edge_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (1977, 1988)\n"
     + "    GROUP BY edge_annotation_namespace, edge_annotation_name, edge_annotation_value\n"
     + "    ORDER by edge_annotation_namespace, edge_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n";
    List<Long> corpora = new LinkedList<Long>();
    corpora.add(1977l);
    corpora.add(1988l);
    assertEquals(expected, listNodeAnnotationsSqlHelper.createSqlQuery(corpora, true, false));
  }

  @Test
  @Ignore
  public void createSqlQueryListMostFrequentValues()
  {
    String expected = "select node_annotation_namespace, node_annotation_name, node_annotation_value from\n"
     + "(\n"
     + "  select *, row_number() OVER (PARTITION BY node_annotation_namespace, node_annotation_name) as row_num\n"
     + "  FROM\n"
     + "  (\n"
     + "    select \n"
     + "    node_annotation_namespace, node_annotation_name, node_annotation_value AS node_annotation_value, \n"
     + "    count(node_annotation_value) as frequency\n"
     + "    FROM facts\n"
     + "    WHERE\n"
     + "    sample_node_annotation = true AND\n"
     + "    node_annotation_value <> '--' AND\n"
     + "    toplevel_corpus IN (1977, 1988)\n"
     + "    GROUP BY node_annotation_namespace, node_annotation_name, node_annotation_value\n"
     + "    ORDER by node_annotation_namespace, node_annotation_name, frequency desc\n"
     + "  ) as tableAll\n"
     + ") as tableFreq\n"
     + "where row_num = 1";
    List<Long> corpora = new LinkedList<Long>();
    corpora.add(1977l);
    corpora.add(1988l);
    assertEquals(expected, listNodeAnnotationsSqlHelper.createSqlQuery(corpora, true, true));
  }
		
	@SuppressWarnings("unchecked")
	@Test
  @Ignore
	public void extractDataNoValues() throws SQLException {
		// stub a result set with 2 annotations with NULL value set
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("node_annotation_name")).thenReturn(NAME1, NAME2);
		when(resultSet.getString("node_annotation_value")).thenReturn(NULL);
		
		// expected
		AnnisAttribute attribute1 = newNamedAnnisAttribute(NAME1);
		AnnisAttribute attribute2 = newNamedAnnisAttribute(NAME2);
		
		// call and test
		List<AnnisAttribute> annotations = (List<AnnisAttribute>) listNodeAnnotationsSqlHelper.extractData(resultSet);
		assertThat(annotations, size(2));
		assertThat(annotations, hasItems(attribute1, attribute2));
	}

	@SuppressWarnings("unchecked")
	@Test
  @Ignore
	public void extractDataWithValues() throws SQLException {
		// stub a result set with 5 rows
		// row 1 - 3: annotation with 3 values
		// row 4: annotation with 1 value
		// row 5: annotation with no values
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, true, true, true, true, false);
		// row 1 - 3: annotation with 3 values
		when(resultSet.getString("node_annotation_name")).thenReturn(NAME1, NAME1, NAME1, NAME2, NAME3);
		when(resultSet.getString("node_annotation_value")).thenReturn(VALUE1, VALUE2, VALUE3, VALUE1, NULL);
		
		// expected
		AnnisAttribute attribute1 = newNamedAnnisAttribute(NAME1, VALUE1, VALUE2, VALUE3);
		AnnisAttribute attribute2 = newNamedAnnisAttribute(NAME2, VALUE1);
		AnnisAttribute attribute3 = newNamedAnnisAttribute(NAME3);
		
		// call and test
		List<AnnisAttribute> annotations = (List<AnnisAttribute>) listNodeAnnotationsSqlHelper.extractData(resultSet);
		assertThat(annotations, size(3));
		assertThat(annotations, hasItems(attribute1, attribute2, attribute3));
	}

	///// private helper
	
	private AnnisAttribute newNamedAnnisAttribute(String name, String... values) {
		AnnisAttribute attribute = new AnnisAttributeImpl();
		
		attribute.setName(name);
		for (String value : values)
			attribute.addValue(value);
		
		return attribute;
	}
	

}
