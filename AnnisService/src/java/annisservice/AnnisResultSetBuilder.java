package annisservice;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import annisservice.ifaces.AnnisResultSet;
import annisservice.ifaces.AnnisToken;
import annisservice.objects.AnnisResultImpl;
import annisservice.objects.AnnisResultSetImpl;
import annisservice.objects.AnnisTokenImpl;


public class AnnisResultSetBuilder {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@SuppressWarnings("serial")
	private class AnnisNonTerminal extends HashMap<String, String>{
		String name;
		long id;
		public AnnisNonTerminal(long id, String name) {
			this.id = id;
			this.name = name;
		}
		@Override
		public String toString() {
			return "id: '" + id + "' " + super.toString();
		}
		
		@Override
		public boolean equals(Object o) {
			if ( ! (o instanceof AnnisNonTerminal) )
				return false;
			
			AnnisNonTerminal other = (AnnisNonTerminal) o;
			
			return new EqualsBuilder().append(this.id, other.id).isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(this.id).toHashCode();
		}
	}
	
	@SuppressWarnings("serial")
	private class Edge {
		long src;
		long dst;
		
		public Edge(long src, long dst) {
			this.src = src;
			this.dst = dst;
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( ! (obj instanceof Edge) )
				return false;
			
			Edge e = (Edge) obj;
			return new EqualsBuilder().append(src, e.src).append(dst, e.dst).isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(src).append(dst).toHashCode();
		}
	}
	
	String lastKey;
	long lastPre;
	Stack<Long> postStack;
	long lastTextRef;
	long lastCorpusRef;
	
	AnnisResultSet annisResultSet;
	Map<Long, AnnisToken> tokens;
	Map<Long, AnnisNonTerminal> nonTerminals;
	Map<Long, Stack<AnnisNonTerminal>> nonTerminalStackByToken;
	Stack<AnnisNonTerminal> nonTerminalStack;
	Map<Long, Long> preStructMap;
	Map<Edge, Map<String, String>> edges;
	
	public AnnisResultSetBuilder() {
		postStack = new Stack<Long>();
		tokens = new TreeMap<Long, AnnisToken>(new Comparator<Long>() {

			public int compare(Long o1, Long o2) {
				return Long.signum(o1 - o2);
			}
			
		});
		nonTerminals = new HashMap<Long, AnnisNonTerminal>();
		nonTerminalStackByToken = new HashMap<Long, Stack<AnnisNonTerminal>>();
		nonTerminalStack = new Stack<AnnisNonTerminal>();
		preStructMap = new HashMap<Long, Long>();
		edges = new HashMap<Edge, Map<String, String>>();
	}
	
	public AnnisResultSet buildResultSet(ResultSet resultSet) {
		annisResultSet = new AnnisResultSetImpl();
		lastKey = null;
		lastPre = -1;
		int rows = 0;
		try {
			while (resultSet.next()) {
				processRow(resultSet);
				++rows;
			}
			newAnnisResult(null, lastTextRef, lastCorpusRef);
		} catch (SQLException e) {
			log.warn("Got an exception while processing the SQL result set", e);
			throw new RuntimeException("Got an exception while processing the SQL result set: " + e.getMessage());
		} catch (TransformerException e) {
			log.warn("Got an exception while creating the PAULA representation", e);
			throw new RuntimeException("Got an exception while creating the PAULA representation: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			log.warn("Got an exception while creating the PAULA representation", e);
			throw new RuntimeException("Got an exception while creating the PAULA representation: " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.warn("Got an exception while creating the PAULA representation", e);
			throw new RuntimeException("Got an exception while creating the PAULA representation: " + e.getMessage());
		}
		log.info("Processed " + rows + " rows; Created " + annisResultSet.size() + " results");
				
		return annisResultSet;
	}
	
	void processRow(ResultSet resultSet) throws SQLException, TransformerException, ParserConfigurationException, UnsupportedEncodingException {
		String key = resultSet.getString(1);
		long pre = resultSet.getLong(2);
		long post = resultSet.getLong(3);
		long parent = resultSet.getLong(4);
		String edgeAnno = resultSet.getString(5);
		String edgeValue = resultSet.getString(6);
		long structId = resultSet.getLong(7);
		long textRef = resultSet.getLong(8);
		String elementNs = resultSet.getString(11);
		String name = resultSet.getString(13);
		name = elementNs + ":" + name;
		long left = resultSet.getLong(14);
		long right = resultSet.getLong(15);
		long tokenIndex = resultSet.getLong(16);
		String span = resultSet.getString(18);
		long corpusRef = resultSet.getLong(21);
		String attributeNs = resultSet.getString(22);
		String attribute = resultSet.getString(23);
		String value = resultSet.getString(24);
		String edgeType = resultSet.getString(25);
		String edgeName = resultSet.getString(26);
		
//		log.debug("processing row, key = " + key + ", pre = " + pre + ", post = " + post);

		// map rank.pre to struct.id
		if (parent != 0) {
			preStructMap.put(pre, structId);
			Long src = preStructMap.get(parent);
			Long dst = preStructMap.get(pre);
			if (src != null) {
				Edge edge = new Edge(src, dst);
				if ( ! edges.containsKey(edge) )
					edges.put(edge, new HashMap<String, String>());
				Map<String, String> edgeAnnotations = edges.get(edge);

				if (edgeAnno != null)
					edgeAnnotations.put(edgeAnno, edgeValue);
				if ("p".equals(edgeType)) {
					edgeAnnotations.put("annis:type", "p");
					edgeAnnotations.put("annis:subtype", edgeName);
				}
			}
		}

		// check for new result
		if ( ! key.equals(lastKey) ) {
			newAnnisResult(key, lastTextRef, lastCorpusRef);
			lastKey = key;
			lastTextRef = textRef;
			lastCorpusRef = corpusRef;
		}		
		
		if (pre != lastPre) {
			lastPre = pre;
			
			// close nodes until we find the parent
			while ( ! postStack.isEmpty() && pre > postStack.peek()) {
				if (postStack.size() == nonTerminalStack.size())
					nonTerminalStack.pop();
				postStack.pop();
			}
			postStack.push(post);			// remember post value

			if (pre == post - 1) {		// leaf
				// create annis token if necessary
				if ( ! tokens.containsKey(structId) ) {
					AnnisToken annisToken = new AnnisTokenImpl(structId, span, left, right, tokenIndex);
					tokens.put(structId, annisToken);
					nonTerminalStackByToken.put(structId, new Stack<AnnisNonTerminal>());
				}
				
				// save current NTs for this token
				Stack<AnnisNonTerminal> ntStack = nonTerminalStackByToken.get(structId);
				for (AnnisNonTerminal nonTerminal : nonTerminalStack) {
					if ( ! ntStack.contains(nonTerminal) )
						ntStack.push(nonTerminal);
				}
			
			} else {		// not a leaf
				// create and push NT
				AnnisNonTerminal nonTerminal;
				if ( ! nonTerminals.containsKey(structId) ) {
					nonTerminal = new AnnisNonTerminal(structId, name);
					nonTerminals.put(structId, nonTerminal);
				} else {
					nonTerminal = nonTerminals.get(structId);
				}
				nonTerminalStack.push(nonTerminal);
			}
		}
		
		if (attributeNs == null)
			return;
		
		if (pre == post - 1) {
			tokens.get(structId).put(attributeNs + ":" + attribute, value);
		} else {
			AnnisNonTerminal nonTerminal;
			if ( ! nonTerminals.containsKey(structId) ) {
				nonTerminal = new AnnisNonTerminal(structId, name);
				nonTerminals.put(structId, nonTerminal);
			} else {
				nonTerminal = nonTerminals.get(structId);
			}
			nonTerminal.put(attributeNs + ":" + attribute, value);
		}
	}
	
	void newAnnisResult(String key, Long textRef, long corpusRef) throws TransformerException, ParserConfigurationException, UnsupportedEncodingException {
		if (lastKey == null)
			return;	// first call on first row to process, no annis result available so far
		
		AnnisResultImpl annisResult = new AnnisResultImpl();
		annisResult.setKey(lastKey);
		
		
		// build annis result from current state
		
		// annisResult.tokenAnnotationLevelSet
		Set<String> tokenAnnotationSet = new HashSet<String>();
		for (AnnisToken token : tokens.values())
			tokenAnnotationSet.addAll(token.keySet());
		annisResult.setTokenAnnotationLevelSet(tokenAnnotationSet);
		log.debug("token annotations: " + tokenAnnotationSet);
		
		// annisResult.nonTokenAnnotationLevelSet
		Set<String> nonTokenAnnotationSet = new HashSet<String>();
		for (AnnisNonTerminal nonTerminal : nonTerminals.values())
			nonTokenAnnotationSet.add(nonTerminal.name);
		annisResult.setAnnotationLevelSet(nonTokenAnnotationSet);
		log.debug("non-terminal annotations: " + nonTokenAnnotationSet);
		
		// annisResult.text
		annisResult.setTextId(textRef);
		log.debug("text id: " + textRef);
		
		// annisResult.corpus
		annisResult.setCorpusId(corpusRef);
		log.debug("corpus id: " + corpusRef);
		
		// FIX: mark tokens for marked non-tokens (preparataion)
		Map<Long, Set<AnnisToken>> tokensForNonTerminal = new HashMap<Long, Set<AnnisToken>>();
		for (AnnisToken token : tokens.values()) {
			for (AnnisNonTerminal nonTerminal : nonTerminalStackByToken.get(token.getId())) {
				if ( ! tokensForNonTerminal.containsKey(nonTerminal.id) )
					tokensForNonTerminal.put(nonTerminal.id, new HashSet<AnnisToken>());
				tokensForNonTerminal.get(nonTerminal.id).add(token);
			}
		}
			
		// marker
		for (String markedId : lastKey.substring(1, lastKey.length() - 1).split(",")) {
			long marked = Long.parseLong(markedId);
			annisResult.getMarker().put(marked, "n" + markedId);
			// FIX: mark tokens for marked non-tokens
			if (tokensForNonTerminal.containsKey(marked))
				for (AnnisToken token : tokensForNonTerminal.get(marked))
					annisResult.getMarker().put(token.getId(), "n" + String.valueOf(token.getId()));
		}
		
		
		log.debug("marked: " + annisResult.getMarker());
		
		// tokenList
		LinkedList<AnnisToken> tokenList = new LinkedList<AnnisToken>();
		tokenList.addAll(tokens.values());
		Collections.sort(tokenList, new Comparator<AnnisToken>() {

			public int compare(AnnisToken o1, AnnisToken o2) {
				return Long.signum(o1.getTokenIndex() - o2.getTokenIndex());
			}
			
		});
		annisResult.setTokenList(tokenList);
		
		List<String> spans = new ArrayList<String>();
		for (AnnisToken token : tokenList)
			spans.add(token.getText());
		log.debug("token list: " + spans);
		
		// paula
		Document paulaDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		Element root = paulaDom.createElement("RESULT");
		root.setAttribute("xmlns:tiger", "tiger"); // FIXME: name spaces?
		root.setAttribute("xmlns:exmaralda", "exmaralda"); // FIXME: name spaces?
		root.setAttribute("xmlns:urml", "urml");
		root.setAttribute("xmlns:inline", "inline");
		root.setAttribute("xmlns:mmax", "mmax");
		root.setAttribute("xmlns:audio", "audio");
		root.setAttribute("xmlns:annis", "annis");
		root.setAttribute("xmlns:enriched", "enriched");
		root.setAttribute("xmlns:merged", "merged");
		paulaDom.appendChild(root);
		
		Element paula = paulaDom.createElement("paula");
		root.appendChild(paula);
		
		Element inline = paulaDom.createElement("inline");
		paula.appendChild(inline);
		
		for (Long id : tokens.keySet()) {
			Element last = inline;
			for (AnnisNonTerminal nonTerminal : nonTerminalStackByToken.get(id)) {
				Element element = paulaDom.createElement(nonTerminal.name);
				for (Entry<String, String> anno : nonTerminal.entrySet())
					element.setAttribute(anno.getKey(), anno.getValue());
				element.setAttribute("_id", String.valueOf(nonTerminal.id));
				last.appendChild(element);
				last = element;
			}
			Element token = paulaDom.createElement("tok");
			for (Entry<String, String> anno : tokens.get(id).entrySet())
				token.setAttribute(anno.getKey(), anno.getValue());
			token.setAttribute("_id", String.valueOf(id));
			last.appendChild(token);
			String t = tokens.get(id).getText();
			// FIXME: test, transformer can't grok text nodes with null text (and they shouldn't be in the database!)
			token.appendChild(paulaDom.createTextNode(t == null ? "" : t));
			
		}
		
		for (Entry<Edge, Map<String, String>> edgeAnnotations : edges.entrySet()) {
			Edge edge = edgeAnnotations.getKey();
			Map<String, String> annotations = edgeAnnotations.getValue();
			if (annotations.isEmpty())
				continue;
			Element rel = paulaDom.createElement("_rel");
			rel.setAttribute("_src", String.valueOf(edge.src));
			rel.setAttribute("_dst", String.valueOf(edge.dst));
			for (Entry<String, String> annotation : annotations.entrySet()) {
				rel.setAttribute(annotation.getKey(), annotation.getValue());
			}
			inline.appendChild(rel);
		}

		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		
		StringWriter writer = new StringWriter();
		DOMSource domSource = new DOMSource(paulaDom);
		StreamResult streamResult = new StreamResult(writer);
		trans.transform(domSource, streamResult);
		
		String utf8 = new String(writer.toString().getBytes("UTF-8"), "UTF-8");
		annisResult.setPaula(utf8);
		
		log.debug("PAULA:\n" + utf8);
		log.debug("JSON:\n" + annisResult.getJSON());

		annisResultSet.add(annisResult);
		
		
		// reset state
		postStack.clear();
		tokens.clear();
		nonTerminals.clear();
		nonTerminalStack.clear();
		nonTerminalStackByToken.clear();
		preStructMap.clear();
		edges.clear();
	}
	
}