/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Tom
 * @author Martin
 */
 
public class SpanBox extends Panel implements Button.ClickListener, FieldEvents.TextChangeListener
{  
  private Button btClose;
  private String ebene;
  private HorizontalLayout sb;
  private SensitiveComboBox cb;  
  private CheckBox reBox;//by Martin, tick for regular expression
  private Collection<String> annonames;//added by Martin, necessary for rebuilding the list of cb-Items
  private FlatQueryBuilder sq;
  
  public static final String BUTTON_CLOSE_LABEL = "Close";
  private static final String SB_CB_WIDTH = "140px";
  
  public SpanBox(final String ebene, final FlatQueryBuilder sq)
  {
    this.ebene = ebene;
    this.sq = sq;
    sb = new HorizontalLayout();
    sb.setImmediate(true);
    sb.setSpacing(true);
    sb.setMargin(true);
    ConcurrentSkipListSet<String> annos = new ConcurrentSkipListSet<String>();
    for(String a : sq.getAvailableAnnotationLevels(ebene))
    {
      annos.add(a);
    }
    this.annonames = annos; 
    Label tf = new Label(ebene);
    sb.addComponent(tf);
    this.cb = new SensitiveComboBox();
    cb.setInputPrompt(ebene);
    cb.setWidth(SB_CB_WIDTH);
    // configure & load content
    cb.setImmediate(true);
    for (String annoname : this.annonames) 
    {
      cb.addItem(annoname);
    }
    cb.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_OFF);
    cb.addListener((FieldEvents.TextChangeListener)this);    
    sb.addComponent(cb);
    HorizontalLayout sbtoolbar = new HorizontalLayout();
    sbtoolbar.setSpacing(true);
    // searchbox tickbox for regex
    CheckBox tb = new CheckBox("Regex");
    tb.setImmediate(true);
    sbtoolbar.addComponent(tb);
    tb.addListener(new ValueChangeListener() {
      // TODO make this into a nice subroutine
      @Override
      public void valueChange(ValueChangeEvent event) {
        boolean r = reBox.booleanValue();
        cb.setNewItemsAllowed(r);
        if(!r)
        {         
          SpanBox.buildBoxValues(cb, ebene, sq);
        }
        else if(cb.getValue()!=null)
        {
          String escapedItem = sq.escapeRegexCharacters(cb.getValue().toString());
          cb.addItem(escapedItem);
          cb.setValue(escapedItem);         
        }
      }
    });
    reBox = tb;
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    sbtoolbar.addComponent(btClose);
    // make visable
    sb.addComponent(sbtoolbar);
    setContent(sb);    
  } 
 
  @Override
  public void buttonClick(Button.ClickEvent event)
  {  
    // close functionality
    if(event.getButton() == btClose)
    {
      sb.removeComponent(cb); 
      cb.setValue("");
      sq.removeSpanBox(this);      
    }
    // regex box functionality
    else if(event.getComponent()==reBox)
    {
      boolean r = reBox.booleanValue();
      cb.setNewItemsAllowed(r);
      if(!r)
      {         
        SpanBox.buildBoxValues(cb, ebene, sq);
      }
      else if(cb.getValue()!=null)
      {
        String escapedItem = sq.escapeRegexCharacters(cb.getValue().toString());
        cb.addItem(escapedItem);
        cb.setValue(escapedItem);         
      }
    }
  }
  
  @Override
  public void textChange(FieldEvents.TextChangeEvent event)
  {        
    String txt = event.getText();
    HashMap<Integer, Collection> levdistvals = new HashMap<Integer, Collection>();
    if (txt.length() > 1)
    {
      cb.removeAllItems();
      for(String s : annonames)
      {
        Integer d = StringUtils.getLevenshteinDistance(removeAccents(txt), removeAccents(s));
        if (levdistvals.containsKey(d)){
          levdistvals.get(d).add(s);
        }
        if (!levdistvals.containsKey(d)){
          Set<String> newc = new TreeSet<String>();
          newc.add(s);
          levdistvals.put(d, newc);
        }
      }
      SortedSet<Integer> keys = new TreeSet<Integer>(levdistvals.keySet());
      for(Integer k : keys.subSet(0, 5)){
        List<String> values = new ArrayList(levdistvals.get(k));
        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
        for(String v : values){
          cb.addItem(v);
        }
      }
    }
  }
  
  public String getAttribute()
  {
    return ebene;
  }
  
  public String getValue()
  {
    return cb.toString();
  }
  
  public boolean isRegEx()
  {
    return reBox.booleanValue();
  }
  
  public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
  
  public static void buildBoxValues(ComboBox cb, String level, FlatQueryBuilder sq)
  {  
    String value = (cb.getValue()!=null) ? cb.getValue().toString() : "";
    Collection<String> annovals = sq.getAnnotationValues(level);    
    cb.removeAllItems();
    for (String s : annovals)
    {
      cb.addItem(s);
    }
    if (annovals.contains(value))
    {
      cb.setValue(value);
    }
    else 
    {
      cb.setValue(null);
    }
  }
}