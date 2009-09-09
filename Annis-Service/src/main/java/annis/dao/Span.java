package annis.dao;

@Deprecated
public class Span {

	private long structId;
	private int textRef;
	private int tokenLeft;
	private int tokenRight;
	
	public Span(long structId, int textRef, int tokenLeft, int tokenRight) {
		this.structId = structId;
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
		if ( ! (obj instanceof Span) )
			return false;
		
		Span n = (Span) obj;
		return structId == n.structId && textRef == n.textRef && tokenLeft == n.tokenLeft && tokenRight == n.tokenRight;
	}
	
}
