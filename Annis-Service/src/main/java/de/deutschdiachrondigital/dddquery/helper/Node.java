package de.deutschdiachrondigital.dddquery.helper;

import java.util.List;
import java.util.Map;

@Deprecated
public interface Node {

	public List<Node> getChildNodes();
	public Node getParentNode();
	public String getNodeName();
	public Map<String, String> getAttributes();
	public String getTextContent();
	
}
