package annisservice.paula.generator;

import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class PaulaExporter {
	private Connection dbConn;
	private Map<Long,Long> parentMap;
	
	private Map<Long,List<Long>> childMap;
	private List<String> nodeAttributeList;
	private Document doc;
	private long textId;
	private Element paula;
	private PreparedStatement getNodeStatement, getNodeIdList;
	private List<Long> nodeIdList;
	Statement stmt;
	
	DocumentBuilderFactory dbfac;
	DocumentBuilder docBuilder;
	Transformer trans;
	
	public PaulaExporter(Properties dbProperties) {
		//setup database connection
		try {
			Class.forName(dbProperties.getProperty("dbDriver")).newInstance(); 
			dbConn = DriverManager.getConnection(dbProperties.getProperty("dbURL"), dbProperties.getProperty("dbUser"), dbProperties.getProperty("dbPassword") );

			stmt = dbConn.createStatement();
			getNodeStatement = dbConn.prepareStatement("SELECT * FROM node n WHERE n._id=?");

			//Setup nodeAttributeList
			nodeAttributeList = new Vector<String>();
			ResultSet rs = stmt.executeQuery("SELECT name FROM node_attribute");
			while(rs.next())
				nodeAttributeList.add(rs.getString(1));
			rs.close();
			
			
			getNodeIdList = dbConn.prepareStatement("SELECT nr._id FROM node nr WHERE nr._tid = ? AND nr._type='tok' AND nr._precedence >= (SELECT n1._precedence FROM node n1 WHERE n1._id=?) AND nr._precedence <= (SELECT n1._precedence FROM node n1 WHERE n1._id=?) ORDER BY nr._precedence");
			
			dbfac = DocumentBuilderFactory.newInstance();
			docBuilder = dbfac.newDocumentBuilder();
			
			TransformerFactory transfac = TransformerFactory.newInstance();
	        trans = transfac.newTransformer();
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<Long> getNodeIdList(long nodeIdStart, long nodeIdEnd) {
		List<Long> idList = new Vector<Long>();
		try {
			getNodeIdList.setLong(1, this.textId);
			getNodeIdList.setLong(2, nodeIdStart);
			getNodeIdList.setLong(3, nodeIdEnd);
			ResultSet rs = getNodeIdList.executeQuery();
			while(rs.next())
				idList.add(rs.getLong(1));
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return idList;
	}
	
	private void setUpRelationsMaps() {
		try {
			parentMap.clear();
			childMap.clear();
		} catch (NullPointerException e) {
			parentMap = new HashMap<Long, Long>();
			childMap = new HashMap<Long, List<Long>>();
		}
		try {
			String query = "SELECT n._id, n._pid FROM node n WHERE n._tid=" + this.textId + " AND n._pid != 0 ORDER BY n._id";
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				//Setup Parent
				parentMap.put(rs.getLong(1), rs.getLong(2));
				
				//Setup Childs
				try { 
					childMap.get(rs.getLong(2)).add(rs.getLong(1));
				} catch (NullPointerException e) {
					List<Long> list = new Vector<Long>();
					list.add(rs.getLong(1));
					childMap.put(rs.getLong(2), list);
				}
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class ParentException extends Exception {}
	private Long getParentId(long nodeId) throws ParentException {
		if(parentMap.containsKey(nodeId))
			return parentMap.get(nodeId);
		throw new ParentException();
	}
	
	private List<Long> getChildIdList(long nodeId) {
		if(childMap.containsKey(nodeId))
			return childMap.get(nodeId);
		return new Vector<Long>();
	}
	
	private void addNamespace(String nodeName) {
		//add namespace to paula element
		if(nodeName.contains(":")) {
			String namespace = (nodeName.split(":"))[0];
			if(null == this.paula.getAttributeNode("xmlns:" + namespace))
				this.paula.setAttribute("xmlns:" + namespace, "paula/" + namespace);
		}
	}
	
	Map<Long, Element> nodeMap = new HashMap<Long, Element>();
	private Element getNode(long id) {
		if(!nodeMap.containsKey(id)) {
			try {
				this.getNodeStatement.setLong(1, id);
				ResultSet rs = this.getNodeStatement.executeQuery();
				if(rs.next()) {
					String type = rs.getString("_type");
					
					addNamespace(type);
					Element elem = doc.createElement(type);
					elem.setAttribute("_id", rs.getString("_id"));
					for(String attribute : this.nodeAttributeList) {
						addNamespace(attribute);
						if(rs.getString(attribute) != null && !attribute.startsWith("_"))
							elem.setAttribute(attribute, rs.getString(attribute));
					}
					
					if("tok".equals(type))
						elem.setTextContent(rs.getString("_text"));
					
					//setting up relations for this node
					try {
						Element relations = getRelations(id);
						elem.appendChild(relations);
					} catch (NoRelationsException e) {
						//ignore
					}
					nodeMap.put(id, elem);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return nodeMap.get(id);
	}
	
	Map<Long, Element> relationsMap = null;
	private class NoRelationsException extends Exception {}
	private Element getRelations(long nodeId) throws NoRelationsException {
		if(relationsMap == null) { 
			relationsMap = new HashMap<Long, Element>();
			try {
				Statement stmt = dbConn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM edge e JOIN node n ON (n._id = e._nid) WHERE e._tid=" + this.textId + " AND e._type='E'");
	
				while(rs.next()) {
					Element relations = relationsMap.get(rs.getLong("_src"));
					if(relations == null) {
						relations = doc.createElement("_relations");
						relationsMap.put(rs.getLong("_src"), relations);
					}
					Element edge = doc.createElement("_rel");
					String dst = rs.getString("_dst");
					edge.setAttribute("_dst", "0".equals(dst) ? "" : dst);
					edge.setAttribute("_src", rs.getString("_src"));
					for(String attribute : this.nodeAttributeList) {
						if(!attribute.startsWith("_")) {
							String value = rs.getString(attribute);
							if(value != null)
								edge.setAttribute(attribute, value);
						}
					}
					relations.appendChild(edge);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Element relations = relationsMap.get(nodeId);
			if(relations != null && relations.hasChildNodes())
				return relations;
		}
		throw new NoRelationsException();
	}
	
	private class NodeInfo {
		public Long leftPrecedence = -1l;
		public Long rightPrecedence = -1l;
		public Long tokenCount = 0l;
		
		public String toString() {
			return "[" + leftPrecedence  + ", " + rightPrecedence + ", " + tokenCount + "]";
		}
	}
	
	private NodeInfo getNodeInfo(Element element) {
		Node n;
		NodeInfo info = new NodeInfo();
		if(!element.hasChildNodes()) {
			if("tok".equals(element.getNodeName())) {
				Long tokenPrecedence = Long.parseLong(element.getAttributes().getNamedItem("_id").getNodeValue());
				info.tokenCount = 1l;
				info.leftPrecedence = tokenPrecedence;
				info.rightPrecedence = tokenPrecedence;
			}
			return info;
		}
		try {
			NodeIterator tokenNodeIterator = XPathAPI.selectNodeIterator(element, ".//tok");
			while((n = tokenNodeIterator.nextNode()) != null) {
				Long tokenPrecedence = Long.parseLong(n.getAttributes().getNamedItem("_id").getNodeValue());
				info.tokenCount++;
				if(tokenPrecedence < info.leftPrecedence || info.leftPrecedence == -1)
					info.leftPrecedence = tokenPrecedence;
				if(tokenPrecedence > info.rightPrecedence || info.rightPrecedence == -1)
					info.rightPrecedence = tokenPrecedence;
			}
		} catch (TransformerException e) {
			//ignore
		}
		return info;
	}
	
	private boolean isAllowedParent(Element element, Element parent) {
		NodeInfo infoParent = getNodeInfo(parent);
		NodeInfo infoElement = getNodeInfo(element);
//		if("45".equals(element.getAttributes().getNamedItem("_id").getNodeValue())) {
//			System.out.println("E " + element.getAttributes().getNamedItem("_id").getNodeValue() + ": " + infoElement);
//			System.out.println("P " + parent.getAttributes().getNamedItem("_id").getNodeValue() + ": " + infoParent);
//			System.out.println(this.nodeIdList.indexOf(infoParent.rightPrecedence) + " . " + this.nodeIdList.indexOf(infoElement.leftPrecedence));
//		}
		
		int elemLeftIndex = this.nodeIdList.indexOf(infoElement.leftPrecedence);
		int parentRightIndex = this.nodeIdList.indexOf(infoParent.rightPrecedence);
		
		if(elemLeftIndex == -1 || parentRightIndex == -1)
			return true;
		if(this.nodeIdList.indexOf(infoParent.rightPrecedence) + 1 != this.nodeIdList.indexOf(infoElement.leftPrecedence))
			System.out.println(this.nodeIdList.indexOf(infoParent.rightPrecedence) + " . " + this.nodeIdList.indexOf(infoElement.leftPrecedence));
		
		return infoParent.rightPrecedence < infoElement.leftPrecedence;
	}
	
	Set<String> seenRelations = new HashSet<String>();
	private Element injectParents(long id, Element element) {
		try {
			Long parentId = getParentId(id);
			Element parent = getNode(parentId);
			if(parent.isSameNode(element.getParentNode()))
				return parent;
			if(isAllowedParent(element, parent)) {
				parent.appendChild(element);
				return injectParents(parentId, parent);
			} else {
				try {
					if(parent.getAttributes().getNamedItem("_id").getNodeValue().equals(element.getParentNode().getAttributes().getNamedItem("_id").getNodeValue()))
						return parent;
				} catch (NullPointerException e) {
					Element discontElem = doc.createElement(parent.getNodeName());
					discontElem.setAttribute("_discont", "yes");
					discontElem.setAttribute("_gid", new String(parent.getAttribute("_id").getBytes()));
					discontElem.setAttribute("_id", new String(parent.getAttribute("_id").getBytes()));
					discontElem.setAttribute("_type", "fragment");
					
					element.getParentNode().appendChild(discontElem);
					discontElem.appendChild(element);
					return discontElem;
				}
			}
		} catch (ParentException e) {
			return element;
		} catch (NullPointerException e) {
			//proceed
		}
		throw new RuntimeException("Moeeeep");
	}
	
	private Element appendChilds(long id, Element element) {
		if(element.hasChildNodes())
			return element;
		List<Long> childIdList = getChildIdList(id);
		if(childIdList.size() == 0)
			return element;
		for(Long childId : childIdList)
    		element.appendChild(appendChilds(childId, getNode(childId)));
		return element;
	}
	
	public void writeXML(Writer writer, long nodeIdStart, long nodeIdEnd) {
		writeXML(writer, nodeIdStart, nodeIdEnd, false);
	}

	public void writeXML(Writer writer, long nodeIdStart, long nodeIdEnd, boolean tokenOnly) {
		//Fetch TextId
		try {
			ResultSet rs = stmt.executeQuery("SELECT n._tid FROM node n WHERE n._id=" + nodeIdStart);
			if(rs.next())
				this.textId = rs.getLong(1);
			rs.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Setup Relation maps... HEURISTIC
		setUpRelationsMaps();
		
		//Fetch NodidList
		nodeIdList = getNodeIdList(nodeIdStart, nodeIdEnd);
		
		try {
			doc = docBuilder.newDocument();
			
			Element elem = doc.createElement("RESULT");
			doc.appendChild(elem);
			this.paula = doc.createElement("paula");
			elem.appendChild(paula);
			Element inline = doc.createElement("inline");
			paula.appendChild(inline);
			
			for(Long id : nodeIdList) {
	        	Element node = getNode(id);

				if(!tokenOnly)
	        		node = injectParents(id, appendChilds(id, node));
				
	        	if(null == node.getParentNode())
					inline.appendChild(node);
			}
			trans.transform(new DOMSource(doc),  new StreamResult(writer));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
