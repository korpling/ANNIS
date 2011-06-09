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

import java.util.List;

import annis.model.Annotation;

public class AnnotatedSpan {

	private long id;
	private String coveredText;
	private List<Annotation> annotations;

	public AnnotatedSpan(long id, String coveredText, List<Annotation> annotations) {
		super();
		this.id = id;
		this.coveredText = coveredText;
		this.annotations = annotations;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getCoveredText() {
		return coveredText;
	}
	
	public void setCoveredText(String coveredText) {
		this.coveredText = coveredText;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = annotations;
	}
	
}
