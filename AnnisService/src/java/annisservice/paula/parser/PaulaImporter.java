package annisservice.paula.parser;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class PaulaImporter extends Importer {
	private Node rootNode;
	
	private PreparedStatement addText;
	private Map<Node, Long> nodeIdMap = new HashMap<Node, Long>();
	private Map<String, Node> nameNodeMap = new HashMap<String, Node>();
	private Map<String, Long> orgId2NewId = new HashMap<String, Long>();
	
	private Map<String, Node> lastNodeList = new HashMap<String, Node>();
	int preorder, postorder;
	private Map<String, Integer> namepaceLevel;
	
	private int precedence = 1;
	public PaulaImporter(Properties dbProperties) {		
			//setup database connection
			try {
				Class.forName(dbProperties.getProperty("dbDriver")).newInstance(); 
				dbConnTransact = DriverManager.getConnection(dbProperties.getProperty("dbURL"), dbProperties.getProperty("dbUser"), dbProperties.getProperty("dbPassword") );
				//dbConnTransact.setAutoCommit(false);
				
				getLastTextId = dbConnTransact.prepareStatement("SELECT currval('text_id')");
				addText = dbConnTransact.prepareStatement("INSERT INTO text (_name) VALUES (?)");
				addCorpus = dbConnTransact.prepareStatement("INSERT INTO corpus (_name) VALUES (?)");
				addTextToCorpus = dbConnTransact.prepareStatement("INSERT INTO corpus_text (_cid, _tid) VALUES (?, ?)");

				insertNode = dbConnTransact.prepareStatement("INSERT INTO node (_tid, _type, _precedence, _pid, _preorder, _postorder, _level, _typelevel) VALUES(?, ?, ?, ?, 0, 0, 0, 0)");
				getLastNodeId = dbConnTransact.prepareStatement("SELECT currval('node_id')");
				getLastCorpusId = dbConnTransact.prepareStatement("SELECT currval('corpus_id')");
				
				updatePreorder = dbConnTransact.prepareStatement("UPDATE node SET _preorder=? WHERE _id=?");
				updatePostorder = dbConnTransact.prepareStatement("UPDATE node SET _postorder=? WHERE _id=?");
				updateLevel = dbConnTransact.prepareStatement("UPDATE node SET _level=?, _typelevel=? WHERE _id=?");
				
				insertEdge = dbConnTransact.prepareStatement("INSERT INTO edge (_tid, _src, _dst, _nid, _type) VALUES(?, ?, ?, ?, 'E')");
				insertDominance = dbConnTransact.prepareStatement("INSERT INTO edge (_tid, _src, _dst, _nid, _type) VALUES(?, ?, ?, null, 'D')");
				insertTokenDominance = dbConnTransact.prepareStatement("INSERT INTO edge (_tid, _src, _dst, _nid, _type) VALUES(?, ?, ?, null, 'T')");
				
				stmtSchema = dbConnTransact.createStatement();
				stmtUpdates = dbConnTransact.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	}
	
	private void buildDefinition(Node node) throws IOException {
		//determining level of root node
		int level=0;
		Node parentNode = node;
		while(parentNode != null) {
			level++;
			parentNode = parentNode.getParentNode();
		}
		buildDefinition(node, level, null);
	}
	
	private void buildDefinition(Node node, int level, Node parentNode) throws IOException {
		if("#text".equals(node.getNodeName()))
			return;
		//System.out.print("NODE: " + node.getNodeName() + " : " + text);
		if("_rel".equals(node.getNodeName())) {
			edgeList.add(node);
		} else {
			Node passedParentNode = node;
			//Writing Current Node
			if(!"_relations".equals(node.getNodeName()) && !"inline".equals(node.getNodeName()) && !"RESULT".equals(node.getNodeName())) {
				writeNode(node, level);
			} else {
				passedParentNode = parentNode;
				//System.out.println();
			}
				
			//Traversing Child Nodes
			NodeList childNodeList = node.getChildNodes();
			for(int i=0; i<childNodeList.getLength(); i++) {
				Node n = childNodeList.item(i);
				buildDefinition(n, level+1, passedParentNode);
			}
		}
	}
	
	private boolean writeNode(Node node, int level) {
		if("#text".equals(node.getNodeName()))
			return false;
		String nodeName = getNodeName(node);
		String levelString = getLevelString(node, level);
		
		insertNode(node, level, null);
		lastNodeList.put(levelString, node);
		nodeNameSet.add(nodeName);
		return true;
	}
	
	private String getLevelString(Node node, int level) {	
		//Using Level "token" for all Tokens
		if("tok".equals(node.getNodeName()))
				return "token";
		return Integer.toString(level);
	}
	
	
	private String getNodeName(Node node) {
		if("_rel".equals(node.getNodeName()))
			return "";
		StringBuffer name = new StringBuffer();		
		try {
			String id = node.getAttributes().getNamedItem("_id").getNodeValue();
			if(id.indexOf("_", 3) != -1) {
				id = id.replaceFirst("_\\d+$", "");
			}
			name.append(id);
		} catch (NullPointerException e) {
			System.out.println("Moep: " + node.getNodeName());
		}
		//name.insert(0, node.getNodeName() + ".");
		////System.out.println("Node: " + name);
		return name.toString();
	}
	
	
	public void run(Document document, String textName, long corpusId, String xPath) throws IOException, TransformerException, SQLException {
		this.nodeIdMap.clear();
		this.nameNodeMap.clear();
		this.edgeList.clear();
		this.nodeNameSet.clear();
		
		this.precedence = 1;
		
		this.rootNode = XPathAPI.selectSingleNode(document, xPath);
		
		addText.setString(1, textName);
		addText.execute();
		this.textId = this.getLastTextId();
		
		addTextToCorpus.setLong(1, corpusId);
		addTextToCorpus.setLong(2, this.textId);
		addTextToCorpus.execute();
		
		insertEdge.setLong(1, this.textId);
		
		buildDefinition(rootNode);
		
		//inserting edges	
		for(Node node : edgeList) {
			try {
				NamedNodeMap attributes = node.getAttributes();
				long srcId, dstId;
				try {
					srcId = orgId2NewId.get(attributes.getNamedItem("_src").getNodeValue());
				} catch (NullPointerException e) {
					srcId = orgId2NewId.get(node.getParentNode().getParentNode().getAttributes().getNamedItem("_id").getNodeValue());
				}
				try {
					dstId = orgId2NewId.get(attributes.getNamedItem("_dst").getNodeValue());
				} catch (NullPointerException e) {
					try {
						dstId = orgId2NewId.get(attributes.getNamedItem("_target").getNodeValue());
					} catch (NullPointerException e2) {
						dstId = 0; //The target definition of this edge is empty
					}
				}
				
				insertEdge(srcId, dstId, node, null);
			} catch (NullPointerException e) {		
				//TODO implement
				System.out.println("Something is wrong... ");
				e.printStackTrace();
			}
		}
		
		//inserting traversations
		this.preorder = 1;
		this.postorder = 1;
		this.namepaceLevel = new HashMap<String, Integer>();
		this.updatePreOrder(this.rootNode);
		this.updatePostOrder(this.rootNode);
		
		stmtSchema.executeBatch();
		insertEdge.executeBatch();
		updatePreorder.executeBatch();
		updatePostorder.executeBatch();
		insertDominance.executeBatch();
		insertTokenDominance.executeBatch();
		updateLevel.executeBatch();
		stmtUpdates.executeBatch();
		dbConnTransact.commit();
	}	
	
	private boolean insertNode(Node node, int level, String type) {
		String nodeName = getNodeName(node);
		boolean isRel = "_rel".equals(node.getNodeName());
		
		if(!isRel) {
			if("".equals(nodeName))
				return false;
			if(nameNodeMap.containsKey(nodeName)) {
				//this is the rest of a fragment
				nodeIdMap.put(node, nodeIdMap.get(nameNodeMap.get(nodeName)));
				return true;
			}
		}
	
		try {
			insertNode.setLong(1, this.textId);
			insertNode.setString(2, (type == null ) ? node.getNodeName() : type);
			insertNode.setInt(3, "tok".equals(node.getNodeName()) ? precedence++ : 0);
			insertNode.setLong(4, 0);
			if(!isRel) {
				try {
					Node parent = node.getParentNode();
					while(parent != null && parent.getNodeName().startsWith("_"))
						parent = parent.getParentNode();
					insertNode.setLong(4, orgId2NewId.get(getNodeName(parent)));
					if(nodeName.equals("id_217"))
						System.out.println("217 -> " + getNodeName(parent) + " ----- " + orgId2NewId.get(getNodeName(parent)));
				} catch (NullPointerException e) {
					//ignore
					//e.printStackTrace();
				}
			}
			insertNode.execute();
			long nodeId = getLastNodeId();
			if(!isRel) {
				nodeIdMap.put(node, nodeId);
				nameNodeMap.put(nodeName, node);
			}
			
			NamedNodeMap attributes = node.getAttributes();
			for(int i=0; i<attributes.getLength(); i++) {
				Node attribute = attributes.item(i);

				if(!attribute.getNodeName().startsWith("_")) {
					String updateQuery = "UPDATE node SET \"" + attribute.getNodeName() + "\"='" + attribute.getNodeValue().replace("'", "\\'") + "' WHERE _id=" + nodeId + "";
					try {
						stmtUpdates.execute(updateQuery);
					} catch (SQLException e) {
						//Attribute does not exist
						insertNodeAttribute(attribute.getNodeName());
						stmtUpdates.execute(updateQuery);
					}
				}
			}
			
			if("tok".equals(node.getNodeName())) {
				String updateQuery = "UPDATE node SET \"_text\"='" + node.getTextContent().replace("\t", "").replace("\n", "").trim().replace("'", "\\'") + "' WHERE _id=" + nodeId + "";
				try {
					stmtUpdates.execute(updateQuery);
				} catch (SQLException e) {
					//Attribute does not exist
					insertNodeAttribute("TEXT");
					stmtUpdates.execute(updateQuery);
				}
			}
			try {
				orgId2NewId.put(node.getAttributes().getNamedItem("_id").getNodeValue(), nodeId);
			} catch (NullPointerException e) {
				//this was a _rel
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			//ignore
			e.printStackTrace();
		}
		return false;
	}
	
	private void insertNodeAttribute(String name) throws SQLException {
		stmtSchema.execute("ALTER TABLE node ADD COLUMN \"" + name + "\" text NULL");
		stmtSchema.execute("INSERT INTO node_attribute VALUES ('" + name + "')");
		//stmtSchema.execute("CREATE INDEX \"index_node_" + name + "\" ON node USING btree (\"" + name + "\")");
	}
	
	private void insertEdge(Long idFrom, Long idTo, Node edgeNode, String type) {
		try {
			if(idFrom.equals(idTo))
				return;
		} catch (NullPointerException e) {
			return;
		}
		if("TOKENDOMINANCE".equals(type)) {
				try {
					insertTokenDominance.setLong(1, this.textId);
					insertTokenDominance.setLong(2, idFrom);
					insertTokenDominance.setLong(3, idTo);
					
					//TODO: make this namespace safe!!!
					
					insertTokenDominance.addBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		} else {
			//this is an explicit edge
			//model this edge as node	
			try {
				insertNode(edgeNode, 0, "_rel");
				
				//_tid, _src, _dst, _nid
				insertEdge.setLong(2, idFrom);
				insertEdge.setLong(3, idTo);
				insertEdge.setLong(4, this.getLastNodeId());
				insertEdge.addBatch();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				//ignore
				e.printStackTrace();
			}
		}
	}
	
	private void updatePreOrder(Node n) {
		updatePreOrder(n, 0);
	}
	
	private void updatePreOrder(Node node, int level) {
		try {
			long id = nodeIdMap.get(node);
			
			if("tok".equals(node.getNodeName())) {
				//update all nodes set tokendomination
				Set<String> typeSet = new HashSet<String>();
				Node parent = node.getParentNode();
				while(parent != null) {
					String pName = parent.getNodeName();
					Long parentId = this.nodeIdMap.get(parent);
					if(parentId != null && !typeSet.contains(pName)) {
						Long idFrom = nodeIdMap.get(parent);
						Long idTo = nodeIdMap.get(node);
						
						insertEdge(idFrom, idTo, null, "TOKENDOMINANCE");
						typeSet.add(pName);
					}
					parent = parent.getParentNode();
				}
			}
			
			//update values in db
			updatePreorder.setInt(1, preorder++);
			updatePreorder.setLong(2, id);
			updatePreorder.addBatch();
			int nsLevel = 0;
			
			if(namepaceLevel.containsKey(node.getNodeName()))
				nsLevel = namepaceLevel.get(node.getNodeName()) + 1;
			
			namepaceLevel.put(node.getNodeName(), nsLevel);
			
			updateLevel.setInt(1, level);
			updateLevel.setInt(2, nsLevel);
			updateLevel.setLong(3, id);
			updateLevel.addBatch();
		} catch (NullPointerException e) {
			//ignore
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList childNodeList = node.getChildNodes();
		for(int i=0;i<childNodeList.getLength();i++)
			updatePreOrder(childNodeList.item(i), level+1);
	}
	
	private void updatePostOrder(Node node) {
		NodeList childNodeList = node.getChildNodes();
		for(int i=0;i<childNodeList.getLength();i++)
			updatePostOrder(childNodeList.item(i));
		try {
			long id = nodeIdMap.get(node);
			
			//update values in db
			updatePostorder.setInt(1, postorder++);
			updatePostorder.setLong(2, id);
			updatePostorder.addBatch();
		} catch (NullPointerException e) {
			//ignore
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
