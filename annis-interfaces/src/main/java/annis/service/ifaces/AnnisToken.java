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
package annis.service.ifaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a corpus token.
 * 
 * @author k.huetter
 *
 */
public interface AnnisToken extends Map<String, String>, Serializable {

	/**
	 * 
	 * @return Node id of this token.
	 */
	public abstract long getId();

	/**
	 * 
	 * @param id Node id of this token.
	 */
	public abstract void setId(long id);
	
	/**
	 * 
	 * @return Source text (word, etc.) of this token.
	 */
	public abstract String getText();
	
	/**
	 * 
	 * @param Source text (word, etc.) of this token.
	 */
	public abstract void setText(String text);

  /**
	 *
	 * @return Corpus id  of this token.
	 */
	public abstract long getCorpusId();

	/**
	 *
	 * @param corpusId Corpus id of this token.
	 */
	public abstract void setCorpusId(long corpusId);

	
	long getLeft();
	long getRight();
	long getTokenIndex();

}