package de.deutschdiachrondigital.dddquery.sql;

import java.util.HashMap;
import java.util.Map;

public class Node {

	private long structId;
	private int textRef;
	private int tokenLeft;
	private int tokenRight;
	private Map<String, String> annotations;
	private Long tokenIndex;
	private String span;
	
	public Node(long structId) {
		this.structId = structId;
		this.annotations = new HashMap<String, String>();
	}
	
	public Node(long structId, int textRef, int tokenLeft, int tokenRight) {
		this(structId);
		this.textRef = textRef;
		this.tokenLeft = tokenLeft;
		this.tokenRight = tokenRight;
	}
	
	@Override
	public String toString() {
		return String.valueOf(structId);
	}
	
	public long getStructId() {
		return structId;
	}
	public void setStructId(long structId) {
		this.structId = structId;
	}
	public int getTextRef() {
		return textRef;
	}
	public void setTextRef(int textRef) {
		this.textRef = textRef;
	}
	public int getTokenLeft() {
		return tokenLeft;
	}
	public void setTokenLeft(int tokenLeft) {
		this.tokenLeft = tokenLeft;
	}
	public int getTokenRight() {
		return tokenRight;
	}
	public void setTokenRight(int tokenRight) {
		this.tokenRight = tokenRight;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof Node) )
			return false;
		
		Node n = (Node) obj;
		return structId == n.structId && textRef == n.textRef && tokenLeft == n.tokenLeft && tokenRight == n.tokenRight;
	}
	
	public void addAnnotation(String name, String value) {
		annotations.put(name, value);
	}

	public Map<String, String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Map<String, String> annotations) {
		this.annotations = annotations;
	}

	public Long getTokenIndex() {
		return tokenIndex;
	}

	public void setTokenIndex(Long tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	public String getSpan() {
		return span;
	}

	public void setSpan(String span) {
		this.span = span;
	}

}
