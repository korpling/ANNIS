/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.gui.flatquerybuilder;

import annis.gui.components.ExceptionDialog;
import com.vaadin.server.ClassResource;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author klotzmaz
 * @author tom
 */
public class ReducingStringComparator {
    private static final String READING_ERROR_MESSAGE = "ERROR: Unable to load mapping file(s)!";
    private static String MAPPING_FILE = "mapfile.fqb";
    private HashMap<String, HashMap<Character, Character>> ALLOGRAPHS;

    public static final Pattern DIACRITICS =
        Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

    public ReducingStringComparator() {
        readMappings();
    }

    public int compare(Object a, Object b, String mapname)
    /*
     * use with Strings only
     * 
     * <0: a<b =0: a=b >0: a>b
     * 
     * compare() is split in 2 methods to make contains() more comfortable (contains() could use
     * compare2(), so that a multiple application of removeCombiningCharacters() on the same string
     * is avoided)
     * 
     */
    {
        String s1 = removeCombiningCharacters((String) a);
        String s2 = removeCombiningCharacters((String) b);
        // compare without spaces
        return compare2(s1.replace(" ", ""), s2.replace(" ", ""), mapname);
    }

    private int compare2(String s1, String s2, String mapname) {
        int l = s1.length();

        if (l < s2.length()) {
            return -1;
        } else if (l > s2.length()) {
            return 1;
        }

        for (int i = 0; i < l; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            HashMap<Character, Character> curMap = ALLOGRAPHS.get(mapname);
            char rc1 = curMap.containsKey(c1) ? curMap.get(c1) : c1;

            char rc2 = (curMap.containsKey(c2)) ? curMap.get(c2) : c2;

            if (rc1 < rc2) {
                return -1;
            } else if (rc1 > rc2) {
                return 1;
            }
        }
        return 0;
    }

    public boolean contains(String fullSequence, String subSequence, String mapname) {
        // kill diacritics:
        String subS = removeCombiningCharacters(subSequence);
        String fullS = removeCombiningCharacters(fullSequence);
        // remove spaces:
        subS = subS.replace(" ", "");
        fullS = fullS.replace(" ", "");
        int l = subS.length();
        for (int i = 0; i < fullS.length() - l + 1; i++) {
            if (compare2(fullS.substring(i, i + l), subS, mapname) == 0) {
                return true;
            }
        }
        return false;
    }

    public HashMap<String, HashMap<Character, Character>> getMappings() {
        return ALLOGRAPHS;
    }

    private HashMap<Character, Character> initAlphabet() {
        HashMap<Character, Character> h = new HashMap<>();

        // standard-alphabet:
        for (int i = 97; i < 122; i++) {
            char c = (char) i;
            h.put(c, c);
            h.put(Character.toUpperCase(c), c);
        }

        return h;
    }

    private void readMappings() {
        ALLOGRAPHS = new HashMap<>();
        ClassResource cr = new ClassResource(ReducingStringComparator.class, MAPPING_FILE);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document mappingD = db.parse(cr.getStream().getStream());

            NodeList mappings = mappingD.getElementsByTagName("mapping");
            for (int i = 0; i < mappings.getLength(); i++) {
                Element mapping = (Element) mappings.item(i);
                String mappingName = mapping.getAttribute("name");
                HashMap<Character, Character> mappingMap = initAlphabet();
                NodeList variants = mapping.getElementsByTagName("variant");
                for (int j = 0; j < variants.getLength(); j++) {
                    Element var = (Element) variants.item(j);
                    char varvalue = var.getAttribute("value").charAt(0);
                    Element character = (Element) var.getParentNode();
                    char charactervalue = character.getAttribute("value").charAt(0);
                    mappingMap.put(varvalue, charactervalue);
                }
                ALLOGRAPHS.put(mappingName, mappingMap);
            }

          } catch (SAXException | IOException | ParserConfigurationException ex) {
            ExceptionDialog.show(ex, READING_ERROR_MESSAGE, UI.getCurrent());
        }

    }

    private String removeCombiningCharacters(String s) {
      s = Normalizer.normalize(s, Normalizer.Form.NFD);
      s = DIACRITICS.matcher(s).replaceAll("");
      return s;
    }

    public boolean startsWith(String fullSequence, String subSequence, String mapname) {
        // kill diacritics:
        String subS = removeCombiningCharacters(subSequence);
        String fullS = removeCombiningCharacters(fullSequence);
        // remove spaces:
        subS = subS.replace(" ", "");
        fullS = fullS.replace(" ", "");
        int l = subS.length();
        if (fullS.length() < l) {
            return false;
        }
        return (compare2(fullS.substring(0, l), subS, mapname) == 0);
    }
}
