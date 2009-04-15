package annisservice.paula.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.w3c.dom.Node;

public class Importer {

	protected Connection dbConnTransact;
	protected PreparedStatement insertNode;
	protected PreparedStatement insertEdge;
	protected PreparedStatement getLastNodeId;
	protected PreparedStatement getLastTextId;
	protected PreparedStatement addCorpus;
	protected PreparedStatement addTextToCorpus;
	protected PreparedStatement getLastCorpusId;
	protected PreparedStatement updatePreorder;
	protected PreparedStatement updatePostorder;
	protected PreparedStatement insertDominance;
	protected PreparedStatement insertTokenDominance;
	protected PreparedStatement updateLevel;
	protected int textId;
	protected Statement stmtSchema;
	protected Statement stmtUpdates;
	protected List<Node> edgeList = new Vector<Node>();
	protected Set<String> nodeNameSet = new HashSet<String>();
	
	public Importer() {
		super();
	}

	public long addCorpus(String name) throws SQLException {
		addCorpus.setString(1, name);
		addCorpus.execute();
		ResultSet rs = getLastCorpusId.executeQuery();
		if(rs.next())
			return rs.getLong(1);
		throw new SQLException();
	}

	public int getNodeCount() {
		return nodeNameSet.size();
	}

	public int getEdgeCount() {
		return edgeList.size();
	}

	protected int getLastNodeId() {
		try {
			ResultSet rs = getLastNodeId.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	protected int getLastTextId() {
		try {
			ResultSet rs = getLastTextId.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}