/* Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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
package annis;

import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Ordering which is sensitive to case, but two characters with the same
 * case are next to each other.
 * 
 * E.g. instead of the ordering "A","B","a" it would be "A","a","B".
 * 
 * This is implemented by first comparating both strings according to their
 * case insensitive ordering  ({@link String#CASE_INSENSITIVE_ORDER})
 * and if both are equal an additional comparision using case sensitive
 * ordering is performed.
 *
 */
public class CaseSensitiveOrder implements Comparator<String>, Serializable
{
  
  /**
   * A static variable containg an instance of the comparator.
   */
  public static final Comparator<String> INSTANCE = new CaseSensitiveOrder();

  @Override
  public int compare(String o1, String o2)
  {
    return ComparisonChain.start().compare(o1, o2, String.CASE_INSENSITIVE_ORDER).
      compare(o1, o2).result();
  }
  
}
