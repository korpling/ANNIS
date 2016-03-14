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
package annis.dao.objects;

import annis.model.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnnotatedSpan {

	private long id;
	private String coveredText;
	private List<Annotation> annotations;
  private List<Annotation> metadata;
  private List<Long> key;

  public AnnotatedSpan(long id, String coveredText,
    List<Annotation> annotations, List<Long> key)
  {
    this(id, coveredText, annotations, new LinkedList<Annotation>(), key);
  }
  
	public AnnotatedSpan(long id, String coveredText, 
    List<Annotation> annotations, List<Annotation> metadata, List<Long> key) 
  {
		super();
		this.id = id;
		this.coveredText = coveredText;
		this.annotations = annotations;
    this.metadata = metadata;
    this.key = new ArrayList<>(key);
	}
  
  /**
   * Copy constructor
   * @param orig 
   */
  public AnnotatedSpan(AnnotatedSpan orig)
  {
    this.id = orig.id;
    this.annotations = orig.annotations == null ? null 
      : new ArrayList<>(orig.annotations);
    this.metadata = orig.metadata == null ? null 
      : new ArrayList<>(orig.metadata);
    this.coveredText = orig.coveredText;
    this.key = orig.key;
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

  public List<Annotation> getMetadata()
  {
    return metadata;
  }

  public void setMetadata(List<Annotation> metadata)
  {
    this.metadata = metadata;
  }

  public List<Long> getKey()
  {
    return key;
  }

  public void setKey(
    List<Long> key)
  {
    this.key = key;
  }
	
}
