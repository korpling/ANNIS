package annis.dao;

import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;

import annis.model.AnnisNode;
import annis.model.Edge;
import annis.model.Edge.EdgeType;


public class TestEdgeRowMapper extends ModelRowMapperTestCase<Edge> {

	// some row data
	private static final long PRE = 1;
	private static final long POST = 2;
	private static final long COMPONENT = 3;
	private static final EdgeType EDGETYPE = EdgeType.UNKNOWN;
	private static final String NAMESPACE = "NAMESPACE";
	private static final String NAME = "NAME";
	private static final long LEVEL = 4;
	private static final long PARENT = 5;
	private static final long NODE_REF = 6;

	// object under test
	@Override
	protected AbstractModelRowMapper<Edge> createModelRowMapper() {
		return new EdgeRowMapper();
	}

	@Test
	public void edgeRowMapper() throws SQLException {
		stubLongColumn("pre", PRE);
		stubLongColumn("post", POST);
		stubLongColumn("node_ref", NODE_REF);
		stubLongColumn("parent", PARENT);
		stubLongColumn("component_id", COMPONENT);
		stubStringColumn("edge_type", EDGETYPE.getTypeChar());
		stubStringColumn("namespace", NAMESPACE);
		stubStringColumn("name", NAME);
		stubLongColumn("level", LEVEL);
		
		// expected edge
		Edge expected = new Edge();
		expected.setPre(PRE);
		expected.setPost(POST);
		expected.setComponent(COMPONENT);
		expected.setEdgeType(EDGETYPE);
		expected.setNamespace(NAMESPACE);
		expected.setName(NAME);
		expected.setLevel(LEVEL);
		expected.setSource(new AnnisNode(PARENT));
		expected.setDestination(new AnnisNode(NODE_REF));
		
		// call and test		
		Edge actual = rowMapper.mapRow(resultSet, 0);
		assertThat(actual, is(expected));
	}
	
	@Test
	public void mapRowRootNode() throws SQLException {
		stubLongColumn("pre", PRE);
		stubLongColumn("post", POST);
		stubLongColumn("node_ref", NODE_REF);
		when(resultSet.wasNull()).thenReturn(true);
		stubLongColumn("component", COMPONENT);
		stubStringColumn("edge_type", EDGETYPE.getTypeChar());
		stubStringColumn("namespace", NAMESPACE);
		stubStringColumn("name", NAME);
		stubLongColumn("level", LEVEL);
		
		// expected edge
		Edge expected = new Edge();
		expected.setPre(PRE);
		expected.setPost(POST);
		expected.setComponent(COMPONENT);
		expected.setEdgeType(EDGETYPE);
		expected.setNamespace(NAMESPACE);
		expected.setName(NAME);
		expected.setLevel(LEVEL);
		expected.setSource(null);
		expected.setDestination(new AnnisNode(NODE_REF));
		
		// call and test		
		assertThat(rowMapper.mapRow(resultSet, 0), is(expected));
	}
	
	private void stubStringColumn(final String column, final String value) throws SQLException {
		stubStringColumn(RANK_TABLE, column, value);
	}

	private void stubLongColumn(String column, Long value) throws SQLException {
		stubLongColumn(RANK_TABLE, column, value);
	}
	
}
