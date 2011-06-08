/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
