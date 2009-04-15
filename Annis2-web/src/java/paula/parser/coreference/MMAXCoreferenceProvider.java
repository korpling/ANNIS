package paula.parser.coreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.XPathAPI;


public class MMAXCoreferenceProvider extends CoreferenceProvider {
	private List<Token> 		tokenList 	 = new ArrayList<Token>();
	private Map<String, String> referenceMap = new HashMap<String, String>();
	private Set<Reference> 		referenceSet = new HashSet<Reference>();
	
	/* (non-Javadoc)
	 * @see paula.parser.InformationStructureProvider#feedNode(org.w3c.dom.Node)
	 */
	public void feedNode(Node node) {
		String nodeName = node.getNodeName();
		if("tok".equals(nodeName)) {
			Token token = new Token();
			try {
				token.id = node.getAttributes().getNamedItem("_id").getNodeValue();
			} catch (NullPointerException e) {
				token.id = node.getAttributes().getNamedItem("id").getNodeValue();
			}
			token.text = node.getTextContent().replace("\n", "").replace("\t", "").trim();
			tokenList.add(token);
		} else if("mmax:primmarkSeg".equals(nodeName) || "mmax:secmarkSeg".equals(nodeName)) {
			//this item is about a discourse referent
			Reference reference = new Reference();
			try {
				reference.id = node.getAttributes().getNamedItem("_org_id").getNodeValue();
			} catch (NullPointerException e) {
				reference.id = node.getAttributes().getNamedItem("_id").getNodeValue();
			}

			try {
				reference.type = node.getAttributes().getNamedItem("mmax:referentiality").getNodeValue();
			} catch (NullPointerException e) {
				//referenciality is not set
			}
			
			try {
				NodeIterator tokenNodeIterator = XPathAPI.selectNodeIterator(node, ".//tok");
				
				Node n;
				while((n = tokenNodeIterator.nextNode()) != null) {
					reference.tokenIdList.add(n.getAttributes().getNamedItem("_id").getNodeValue());
					reference.text += n.getTextContent().replace("\n", "").replace("\t", "").trim() + " ";
				}

				Node relNode = XPathAPI.selectSingleNode(node, ".//_rel");
				try {
					reference.refersTo = relNode.getAttributes().getNamedItem("_target_org").getNodeValue();
				} catch (NullPointerException e) {
					try {
						reference.refersTo = relNode.getAttributes().getNamedItem("_target").getNodeValue();
					} catch (NullPointerException e2) {
						//ignore
					}
				}
				if(!"".equals(reference.refersTo) && !"empty".equals(reference.refersTo))
					referenceMap.put(reference.id, reference.refersTo);
				referenceSet.add(reference);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see paula.parser.InformationStructureProvider#getTokenList()
	 */
	public List<Token> getTokenList() {
		return tokenList;
	}

	/* (non-Javadoc)
	 * @see paula.parser.InformationStructureProvider#getReferenceMap()
	 */
	public Map<String, String> getReferenceMap() {
		return referenceMap;
	}

	/* (non-Javadoc)
	 * @see paula.parser.InformationStructureProvider#getReferenceSet()
	 */
	public Set<Reference> getReferenceSet() {
		return referenceSet;
	}
}
