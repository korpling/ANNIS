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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.vaadin.server.ClassResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;
import java.io.BufferedReader;
import java.util.Comparator;
import java.util.HashMap;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;


/**
 * @author klotzmaz
 * @author tom
 */
public class reducingStringComparator implements Comparator
{
  private static HashMap<Character, Character> ALLOGRAPHS;
  private static final String READING_ERROR_MESSAGE = "ERROR: Unable to load mapping file(s)!";
  private static String MAPPING_FILE = "/home/klotzmaz/Documents/ANNIS/annis-gui/src/main/resources/annis/gui/components/mapfile.fqb";
  
  public reducingStringComparator()
  {
    initAlphabet();
    readMappings();
  }
  
  private void initAlphabet()
  {
    HashMap<Character, Character> h = new HashMap<Character, Character>();
    
    //standard-alphabet:
    for(int i=97; i<122; i++)
    {
      char c = (char)i;
      h.put(c, c);
      h.put(Character.toUpperCase(c), c);
    }
    
    ALLOGRAPHS = h;
  }
  
  private void readMappings()
  {	  
    try
    {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();      
      URL u = cl.getResource(MAPPING_FILE);
      String whereAmI = Window.Location.getPath();
      
      File mf = new File(MAPPING_FILE);
      
      HashMap<Character, Character> h = new HashMap<Character, Character>();
      
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      ClassResource cr = new ClassResource(reducingStringComparator.class, MAPPING_FILE);
      
      Document mappingD = db.parse(mf);      
      
      NodeList variants = mappingD.getElementsByTagName("variant");
      for(int i=0; i<variants.getLength(); i++)
      {
        Element var = (Element)variants.item(i);        
        h.put(var.getAttribute("value").charAt(0), ((Element)var.getParentNode()).getAttribute("value").charAt(0));        
      }
      
      ALLOGRAPHS.putAll(h);
      
    } catch(Exception e)
    {
      e = null;
      Notification.show(READING_ERROR_MESSAGE);
    }    
  }
    
  private String removeCombiningCharacters(String s)
  {
    String t="";    
    
    for (int i=0; i<s.length(); i++)
    {
      char c = s.charAt(i);
      int cp = (int)c;
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
}
