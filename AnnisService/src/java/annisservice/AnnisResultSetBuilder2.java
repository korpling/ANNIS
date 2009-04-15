package annisservice;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annisservice.ifaces.AnnisResult;
import annisservice.ifaces.AnnisResultSet;
import annisservice.objects.AnnisResultImpl;
import annisservice.objects.AnnisResultSetImpl;
import annisservice.objects.AnnisTokenImpl;

@Deprecated
public class AnnisResultSetBuilder2 {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private String lastKey;
	private long lastPre = -1;
	private Stack<Long> postStack;
	
	private AnnisResultSet annisResultSet;
	private AnnisResultImpl annisResult;
	
	private Document paulaDom;
	private Stack<Element> elementStack;
	
	private int rows;
	
	private Set<Long> seenTokens;
	
	private Set<String> tokenAnnotationLevels;
	private Set<String> nonTokenAnnotationLevels;
	
	public AnnisResultSetBuilder2() {
		elementStack = new Stack<Element>();
		postStack = new Stack<Long>();
	}

	private void reset() {
		annisResultSet = new AnnisResultSetImpl();
		annisResult = null;
		lastKey = null;
		updatePaula();
	}
	
	public AnnisResultSet buildResultSet(ResultSet resultSet) {
		reset();	// FIXME: besser eine Methode endNode(), und updatePaula() refactoren
		try {
			rows = 0;
			while (resultSet.next()) {
				processRow(resultSet);	// FIXME: test
				++rows;
			}
			updatePaula(); 	// FIXME: test
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		log.info("# annis results: " + annisResultSet.size() + " (" + rows + " rows)");
		return annisResultSet;
	}
	
	void processRow(ResultSet resultSet) throws SQLException {
		String key = resultSet.getString(1);
		long pre = resultSet.getLong(2);
		long post = resultSet.getLong(3);
		
		log.debug("processing row, key = " + key + ", pre = " + pre + ", post = " + post);

		// check for new result
		if ( ! key.equals(lastKey) ) {
			lastKey = key;
			getPostStack().clear();			// new result -> entire new PAULA tree
			newAnnisResult();
		}
		
		// check for new node
		if (pre != lastPre) {
			lastPre = pre;
			
			// close nodes until we find the parent
			while ( ! getPostStack().isEmpty() && pre > getPostStack().peek()) {
				getPostStack().pop();
				closeNode();
			}
			getPostStack().push(post);			// remember post value

			// FIXME: Kantenannotation
			
			// new node
			boolean leaf = (pre == post - 1) ? true : false;
			long structId = resultSet.getLong(4);
			Long textRef = resultSet.getLong(5);
			String name = resultSet.getString(9);
			long left = resultSet.getLong(10);
			long right = resultSet.getLong(11);
			long tokenIndex = resultSet.getLong(12);
			String span = resultSet.getString(14);
			startNode(leaf, structId, name, textRef, left, right, span, tokenIndex);
		}

		// add annotation, FIXME: no annotation?
		String ns = resultSet.getString(17);
		String attribute = resultSet.getString(18);
		String value = resultSet.getString(19);
		if (ns != null)
			addAnnotation(ns + ":" + attribute, value);
	}

	void newAnnisResult() {
		updatePaula();		// FIXME: test
		
		seenTokens = new HashSet<Long>();
		tokenAnnotationLevels = new HashSet<String>();
		nonTokenAnnotationLevels = new HashSet<String>();

		annisResult = new AnnisResultImpl();
		annisResultSet.add(annisResult);
		annisResult.setAnnotationLevelSet(nonTokenAnnotationLevels);
		annisResult.setTokenAnnotationLevelSet(tokenAnnotationLevels);
	}

	private void updatePaula() throws TransformerFactoryConfigurationError {
		try {
			if (annisResult != null) {
				elementStack.clear();
				TransformerFactory transfac = TransformerFactory.newInstance();
				Transformer trans = transfac.newTransformer();
//				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				
				StringWriter writer = new StringWriter();
				trans.transform(new DOMSource(paulaDom), new StreamResult(writer));
				
				String utf8 = new String(writer.toString().getBytes("UTF-8"), "UTF-8");
				annisResult.setPaula(utf8);
				
				// debugging: write string to temp file
//				try {
//					File tmpFile = File.createTempFile("paula", ".xml");
//					tmpFile.deleteOnExit();
//					FileWriter fileWriter = new FileWriter(tmpFile);
//					fileWriter.write(utf8);
//					fileWriter.close();
//					log.debug("wrote xml to " + tmpFile.getAbsolutePath());
//				} catch (IOException e) {
//					// don't bother
//				}
				
				log.debug("Found an AnnisResult; PAULA is\n" + writer.toString());
				log.debug("annotation levels: " + annisResult.getAnnotationLevelSet());
				log.debug("token annotations levels: " + annisResult.getTokenAnnotationLevelSet());
			}
			paulaDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = paulaDom.createElement("RESULT");
			root.setAttribute("xmlns:tiger", "tiger"); // FIXME: name spaces?
			root.setAttribute("xmlns:exmaralda", "exmaralda"); // FIXME: name spaces?
			root.setAttribute("xmlns:urml", "urml");
			root.setAttribute("xmlns:inline", "inline");
			root.setAttribute("xmlns:mmax", "mmax");
			paulaDom.appendChild(root);
			elementStack.push(root);
			
			Element paula = paulaDom.createElement("paula");
			root.appendChild(paula);
			elementStack.push(paula);
			
			Element inline = paulaDom.createElement("inline");
			paula.appendChild(inline);
			elementStack.push(inline);
			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}
	
	void addAnnotation(String attribute, String value) {
		Element currentNode = elementStack.peek();
		String nodeName = currentNode.getNodeName();

		log.debug("annotation for " + nodeName + ": " + attribute + " = " + value);		
		currentNode.setAttribute(attribute, value);
		
		if ("tok".equals(nodeName)) {
			log.debug("adding " + attribute + " to token annotations");
			tokenAnnotationLevels.add(attribute);
		}
	}
	
	void startNode(boolean leaf, long id, String name, Long textRef, long left, long right, String text, long tokenIndex) {
		log.debug("creating node: id = " + id + ", name = " + name + ", text_ref = " + textRef);
		
		// set text_ref (assumed the same over the entire result)
		// FIXME: test
		annisResult.setTextId(textRef);
		
		if (leaf) {
			if (! seenTokens.contains(id)) {
				seenTokens.add(id);
				annisResult.addToken(new AnnisTokenImpl(id, text, left, right, tokenIndex));
			}
			// FIXME: test
			Element node = paulaDom.createElement("tok");
			node.appendChild(paulaDom.createTextNode(text));
			if (elementStack.isEmpty())
				throw new RuntimeException("BUG: was expecting a parent");
			elementStack.peek().appendChild(node);
			elementStack.push(node);
	
		} else {

			// FIXME: test
			Element node = paulaDom.createElement(name);
			if (elementStack.isEmpty())
				throw new RuntimeException("BUG: was expecting a parent");
			elementStack.peek().appendChild(node);
			elementStack.push(node);
		}
		
		if ( ! leaf ) 
			nonTokenAnnotationLevels.add(name);
		
		// FIXME: test, refactor
		if (Arrays.asList(lastKey.substring(1, lastKey.length() - 1).split(",")).contains(String.valueOf(id)))
			annisResult.getMarker().put(id, "n" + id);
		
		// Knoten-ID
		elementStack.peek().setAttribute("_id", String.valueOf(id));
	}
	
	void closeNode() {
		log.debug("closing node");
		
		elementStack.pop();
	}

	
	void setPostStack(Stack<Long> postStack) {
		this.postStack = postStack;
	}
	
	Stack<Long> getPostStack() {
		return postStack;
	}

	void setAnnisResultSet(AnnisResultSet annisResultSet) {
		this.annisResultSet = annisResultSet;
	}

	AnnisResultSet getAnnisResultSet() {
		return annisResultSet;
	}

	AnnisResult getAnnisResult() {
		return annisResult;
	}

	void setAnnisResult(AnnisResultImpl annisResult) {
		this.annisResult = annisResult;
	}

	void setLastKey(String lastKey) {
		this.lastKey = lastKey;
	}

	Document getPaulaDom() {
		return paulaDom;
	}

	void setPaulaDom(Document paulaDom) {
		this.paulaDom = paulaDom;
	}

	Stack<Element> getElementStack() {
		return elementStack;
	}

	void setElementStack(Stack<Element> elementStack) {
		this.elementStack = elementStack;
	}
	
}