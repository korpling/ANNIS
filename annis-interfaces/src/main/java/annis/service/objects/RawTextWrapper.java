/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.objects;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for the rest api call for extracting the raw text.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
@XmlRootElement
public class RawTextWrapper implements Serializable
{

  // holds the texts of a document.
  List<String> texts;

  /**
   * Sets the texts of a document.
   * @param texts Can be null or empty.
   */
  public void setTexts(List<String> texts){
    this.texts = texts;
  }

  /**
   * Returns a list of all texts.
   *
   * @return If the document only uses segmentations and has only an artificial
   * token layer this list of strings is null
   */
  public List<String> getTexts()
  {
    return texts;
  }

  /**
   * Checks, whether at least one text exists
   */
  public boolean hasTexts()
  {
    return texts != null && !texts.isEmpty();
  }

  /**
   * Checks, if multiple texts are stored for one document.
   *
   * @return true if there are more than 1 text.
   */
  public boolean hasMultipleTexts()
  {
    return texts != null && texts.size() > 1;
  }

  /**
   * Extracts first text or an empty string, when texts are empty.
   *
   * @return A string, which represents the first text of a document.
   */
  public String getFirstText()
  {
    return texts != null && !texts.isEmpty() ? texts.get(0) : "";
  }
}
