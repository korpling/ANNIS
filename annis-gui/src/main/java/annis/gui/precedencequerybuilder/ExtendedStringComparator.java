/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.precedencequerybuilder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
/**
 *
 * @author klotzmaz
 */
public class ExtendedStringComparator implements Comparator
{
  private static HashMap<Character, Character> ALLOGRAPHS;
  
  public ExtendedStringComparator()
  {
    ALLOGRAPHS = initAlphabet();
  }
  
  private HashMap<Character, Character> initAlphabet()
  {
    HashMap<Character, Character> h = new HashMap<Character, Character>();
    
    //standard-alphabet:
    for(int i=97; i<122; i++)
    {
      char c = (char)i;
      h.put(c, c);
      h.put(Character.toUpperCase(c), c);
    }
    
    //A:
    //h.put('a', 'a');
    //h.put('A', 'a');
    h.put('À', 'a');
    h.put('Á', 'a');
    h.put('Â', 'a');
    h.put('Ã', 'a');
    h.put('Ä', 'a');
    h.put('Å', 'a');
    h.put('Æ', 'a');
    h.put('à', 'a');
    h.put('á', 'a');
    h.put('â', 'a');
    h.put('ã', 'a');
    h.put('ä', 'a');
    h.put('å', 'a');
    h.put('æ', 'a');
    h.put('Ā', 'a');
    h.put('ā', 'a');
    h.put('Ă', 'a');
    h.put('ă', 'a');
    h.put('Ą', 'a');
    h.put('ą', 'a');
    h.put('Ǎ', 'a');
    h.put('ǎ', 'a');
    h.put('Ǟ', 'a');
    h.put('ǟ', 'a');
    h.put('Ǡ', 'a');
    h.put('ǡ', 'a');
    h.put('Ǣ', 'a');
    h.put('ǣ', 'a');
    h.put('Ǻ', 'a');
    h.put('ǻ', 'a');
    h.put('Ǽ', 'a');
    h.put('ǽ', 'a');
    h.put('Ȁ', 'a');
    h.put('ȁ', 'a');
    h.put('Ȃ', 'a');
    h.put('ȃ', 'a');
    h.put('Ȧ', 'a');
    h.put('ȧ', 'a');
    h.put('Ⱥ', 'a');
    h.put('ɐ', 'a');
    h.put('ɑ', 'a');
    h.put('ɒ', 'a');

    //E:
    //h.put('e', 'e');
    //h.put('E', 'e');
    h.put('È', 'e');
    h.put('É', 'e');
    h.put('Ê', 'e');
    h.put('Ë', 'e');
    h.put('è', 'e');
    h.put('é', 'e');
    h.put('ê', 'e');
    h.put('ë', 'e');
    h.put('Ē', 'e');
    h.put('ē', 'e');
    h.put('Ĕ', 'e');
    h.put('ĕ', 'e');
    h.put('Ė', 'e');
    h.put('ė', 'e');
    h.put('Ę', 'e');
    h.put('ę', 'e');
    h.put('Ě', 'e');
    h.put('ě', 'e');
    h.put('Ǝ', 'e');
    h.put('Ə', 'e');
    h.put('Ɛ', 'e');
    h.put('ǝ', 'e');
    h.put('Ȅ', 'e');
    h.put('ȅ', 'e');
    h.put('Ȇ', 'e');
    h.put('ȇ', 'e');
    h.put('Ȩ', 'e');
    h.put('ȩ', 'e');
    h.put('Ɇ', 'e');
    h.put('ɇ', 'e');

    //I:
    //h.put('i', 'i');
    //h.put('I', 'i');
    h.put('Ì', 'i');
    h.put('Í', 'i');
    h.put('Î', 'i');
    h.put('Ï', 'i');
    h.put('ì', 'i');
    h.put('í', 'i');
    h.put('î', 'i');
    h.put('ï', 'i');
    h.put('Ĩ', 'i');
    h.put('ĩ', 'i');
    h.put('İ', 'i');
    h.put('ı', 'i');
    h.put('Ɨ', 'i');
    h.put('Ǐ', 'i');
    h.put('ǐ', 'i');
    h.put('Ȉ', 'i');
    h.put('ȉ', 'i');
    h.put('Ȋ', 'i');
    h.put('ȋ', 'i');
    h.put('ɨ', 'i');
    
    //O:
    
    //h.put('o', 'o');
    //h.put('O', 'o');
    h.put('Ò', 'o');
    h.put('Ó', 'o');
    h.put('Ô', 'o');
    h.put('Õ', 'o');
    h.put('Ö', 'o');
    h.put('Ø', 'o');
    h.put('ò', 'o');
    h.put('ó', 'o');
    h.put('ô', 'o');
    h.put('õ', 'o');
    h.put('ö', 'o');
    h.put('ø', 'o');
    h.put('Ō', 'o');
    h.put('ō', 'o');
    h.put('Ŏ', 'o');
    h.put('ŏ', 'o');
    h.put('Ő', 'o');
    h.put('ő', 'o');
    h.put('Œ', 'o');
    h.put('œ', 'o');
    h.put('Ɔ', 'o');
    h.put('Ɵ', 'o');
    h.put('Ơ', 'o');
    h.put('ơ', 'o');
    h.put('Ƣ', 'o');
    h.put('ƣ', 'o');
    h.put('Ǒ', 'o');
    h.put('ǒ', 'o');
    h.put('Ǫ', 'o');
    h.put('ǫ', 'o');
    h.put('Ǭ', 'o');
    h.put('ǭ', 'o');
    h.put('Ǿ', 'o');
    h.put('ǿ', 'o');
    h.put('Ȍ', 'o');
    h.put('ȍ', 'o');
    h.put('Ȏ', 'o');
    h.put('ȏ', 'o');
    h.put('Ȣ', 'o');
    h.put('ȣ', 'o');
    h.put('Ȫ', 'o');
    h.put('ȫ', 'o');
    h.put('Ȭ', 'o');
    h.put('ȭ', 'o');
    h.put('Ȯ', 'o');
    h.put('ȯ', 'o');
    h.put('Ȱ', 'o');
    h.put('ȱ', 'o');
    h.put('ɔ', 'o');

    //U:
    //h.put('u', 'u');
    //h.put('U', 'u');
    h.put('Ù', 'u');
    h.put('Ú', 'u');
    h.put('Û', 'u');
    h.put('Ü', 'u');
    h.put('ù', 'u');
    h.put('ú', 'u');
    h.put('û', 'u');
    h.put('ü', 'u');
    h.put('Ũ', 'u');
    h.put('ũ', 'u');
    h.put('Ū', 'u');
    h.put('ū', 'u');
    h.put('Ŭ', 'u');
    h.put('ŭ', 'u');
    h.put('Ů', 'u');
    h.put('ů', 'u');
    h.put('Ű', 'u');
    h.put('ű', 'u');
    h.put('Ų', 'u');
    h.put('ų', 'u');
    h.put('Ư', 'u');
    h.put('ư', 'u');
    h.put('Ʊ', 'u');
    h.put('Ǔ', 'u');
    h.put('ǔ', 'u');
    h.put('Ǖ', 'u');
    h.put('ǖ', 'u');
    h.put('Ǘ', 'u');
    h.put('ǘ', 'u');
    h.put('Ǚ', 'u');
    h.put('ǚ', 'u');
    h.put('Ǜ', 'u');
    h.put('ǜ', 'u');
    h.put('Ȕ', 'u');
    h.put('ȕ', 'u');
    h.put('Ȗ', 'u');
    h.put('ȗ', 'u');
    h.put('Ʉ', 'u');
    h.put('ʉ', 'u');
    h.put('ʊ', 'u');

    
    //further special characters:
    h.put('ç', 'c');
    h.put('Ç', 'c'); 
    h.put('ß', 's');
    
    return h;
  }
  
  @Override
  //use with Strings only
  public int compare(Object a, Object b)
    /*
     * <0: a<b
     * =0: a=b
     * >0: a>b
     */
  {
    String s1 = (String)a;
    String s2 = (String)b;
    int l = s1.length();
    
    if (l<s2.length())
    {
      return -1;
    }
    else if (l>s2.length())
    {
      return 1;
    }
    
    for(int i=0; i<l; i++)
    {
      char c1 = s1.charAt(i);
      char c2 = s2.charAt(i);      
      
      char rc1 = (ALLOGRAPHS.containsKey(c1)) ? ALLOGRAPHS.get(c1) : c1;
      
      char rc2 = (ALLOGRAPHS.containsKey(c2)) ? ALLOGRAPHS.get(c2) : c2;
      
      if(rc1<rc2)
      {
        return -1;
      }
      else if(rc1>rc2)
      {
        return 1;
      }
    }
    
    return 0;
  }  
  
  public boolean startsWith(String fullSequence, String subSequence)
  {
    int l = subSequence.length();
    if (fullSequence.length()<l) {return false;}
    
    return (compare(fullSequence.substring(0, l), subSequence)==0);
  }
  
  public boolean contains(String fullSequence, String subSequence)
  {
    int l = subSequence.length();
    for (int i=0; i<fullSequence.length()-l; i++)
    {
      if (compare(fullSequence.substring(i, i+l), subSequence)==0)
      {
        return true;
      }
    }
    return false;
  }

  
  //improve algorithm later
  public void sort(Collection<String> c)
  {
    
  }
}
