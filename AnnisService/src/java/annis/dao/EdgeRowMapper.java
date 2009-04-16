package annis.dao;


import static annis.sqlgen.TableAccessStrategy.EDGE_TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;

import annis.model.AnnisNode;
import annis.model.Edge;
import annis.model.Edge.EdgeType;

public class EdgeRowMapper extends AbstractModelRowMapper<Edge> {
	
	public Edge mapRow(ResultSet resultSet, int rowNum)
			throws SQLException {
		Edge edge = new Edge();
		
		edge.setPre(longValue(resultSet, "pre"));
		edge.setPost(longValue(resultSet, "post"));
		edge.setComponent(longValue(resultSet, "component"));
		edge.setEdgeType(EdgeType.parseEdgeType(stringValue(resultSet, "edge_type")));
		edge.setNamespace(stringValue(resultSet, "namespace"));
		edge.setName(stringValue(resultSet, "name"));
		edge.setLevel(longValue(resultSet, "level"));
		edge.setDestination(new AnnisNode(longValue(resultSet, "node_ref")));
		
		// create nodes for src with rank value (parent) as id.
		// this must later be fixed by AnnotationGraphDaoHelper.fixSourceNodeIds().
		// this is simpler than chaining the edgeByPre map in AnnisResultSetBuilder
		// and making the EdgeRowMapper thread-safe.
		// FIXME: use custum mapRow(resultSet, edgeByPre) function, throw Exception here
		// also, no need to implement Spring RowMapper
		long parent = longValue(resultSet, "parent");
		if ( ! resultSet.wasNull() )
			edge.setSource(new AnnisNode(parent));
		
		return edge;
	}
	
	private String stringValue(ResultSet resultSet, String column) throws SQLException {
		return stringValue(resultSet, EDGE_TABLE, column);
	}
	
	private long longValue(ResultSet resultSet, String column) throws SQLException {
		return longValue(resultSet, EDGE_TABLE, column);
	}

}