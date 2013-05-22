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
package annis.gui.flatquerybuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author klotzmaz
 * @author tom
 */
public class reducingStringComparator implements Comparator
{
  private static HashMap<Character, Character> ALLOGRAPHS;
  
  public reducingStringComparator()
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
       
    //read from file:
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
    h.put('ī', 'i');
    
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
    h.put('ʒ', 'z');
    
    return h;
  }
    
  private String removeCombiningCharacters(String s)
  {
    String t="";    
    
    for (int i=0; i<s.length(); i++)
    {
      char c = s.charAt(i);
      int cp = (int)c;
      //improve later with IntRanges
      if(!(
        ((cp>767) & (cp<880)) |
        ((cp>1154) & (cp<1162)) |
        (cp==1619) |
        ((cp>2026) & (cp<2036)) |
        (cp==4352) |
        ((cp>4956) & (cp<4960)) |
        (cp==6783) |
        ((cp>7018) & (cp<7028)) |
        ((cp>7615) & (cp<7655)) |
        ((cp>7675) & (cp<7680)) |
        ((cp>8399) & (cp<8433)) |
        ((cp>11502) & (cp<11506)) |
        ((cp>11743) & (cp<11776)) |
        ((cp>12440) & (cp<12443)) |
        ((cp>42606) & (cp<42611)) |
        ((cp>42611) & (cp<42622)) |
        ((cp>42654) & (cp<42738)) |
        ((cp>43231) & (cp<43250)) |
        ((cp>65055) & (cp<65063)) |
        (cp==66045) |
        ((cp>119140) & (cp<119146)) |
        ((cp>119148) & (cp<119155)) |
        ((cp>119162) & (cp<119171)) |
        ((cp>119172) & (cp<119180)) |
        ((cp>119209) & (cp<119214)) |
        ((cp>119361) & (cp<119365))
        ))
      {
        t = t + c;
      }     
    }
    
    return t;
  }
  
  @Override
  public int compare(Object a, Object b)
    /*
     * use with Strings only
     * 
     * <0: a<b
     * =0: a=b
     * >0: a>b
     * 
     * compare() is split in 2 methods to make contains()
     * more comfortable (contains() could use compare2(),
     * so that a multiple application of removeCombiningCharacters() 
     * on the same string is avoided)
     * 
     */
  { 
    String s1 = removeCombiningCharacters((String)a);
    String s2 = removeCombiningCharacters((String)b);
    //compare without spaces
    return compare2(s1.replace(" ", ""), s2.replace(" ", ""));    
  }
  
  private int compare2(String s1, String s2)
  {
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
    //kill diacritics:
    String subS = removeCombiningCharacters(subSequence);
    String fullS = removeCombiningCharacters(fullSequence);
    //remove spaces:
    subS = subS.replace(" ", "");
    fullS = fullS.replace(" ", "");
    int l = subS.length();
    if (fullS.length()<l) {return false;}
    return (compare2(fullS.substring(0, l), subS)==0);
  }
  
  public boolean contains(String fullSequence, String subSequence)
  {
    //kill diacritics:    
    String subS = removeCombiningCharacters(subSequence);
    String fullS = removeCombiningCharacters(fullSequence);
    //remove spaces:
    subS = subS.replace(" ", "");
    fullS = fullS.replace(" ", "");
    int l = subS.length();
    for (int i=0; i<fullS.length()-l+1; i++)
    {
      if (compare2(fullS.substring(i, i+l), subS)==0)
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
