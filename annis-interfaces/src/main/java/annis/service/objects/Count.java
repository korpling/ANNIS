/*
 * Copyright 2012 SFB 632.
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the result of a COUNT query in ANNIS.
 * 
 * It provides the number of matched node-tuples and the number of distinct
 * documents where there matches occured.
 * 
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@XmlRootElement
public class Count implements Serializable
{
  private int tupelMatched;
  private int documentsMatched;

  public int getTupelMatched()
  {
    return tupelMatched;
  }

  public void setTupelMatched(int tupelMatched)
  {
    this.tupelMatched = tupelMatched;
  }

  public int getDocumentsMatched()
  {
    return documentsMatched;
  }

  public void setDocumentsMatched(int documentsMatched)
  {
    this.documentsMatched = documentsMatched;
  }
  
  
}
