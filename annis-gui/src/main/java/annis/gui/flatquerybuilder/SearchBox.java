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
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ChameleonTheme;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author tom
 */
public class SearchBox extends Panel implements Button.ClickListener, 
  FieldEvents.TextChangeListener
{
  private Button btClose;
  private VerticalNode vn;
  private String ebene;
  private SensitiveComboBox cb;  
  private CheckBox reBox;
  private CheckBox negSearchBox;
  private Collection<String> annonames;
  private FlatQueryBuilder sq;
  public static final String BUTTON_CLOSE_LABEL = "X";
  private static final String SB_CB_WIDTH = "145px";
  private static reducingStringComparator rsc;
  
  public SearchBox(final String ebene, final FlatQueryBuilder sq, final VerticalNode vn)
  {
    this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    rsc = new reducingStringComparator();
    VerticalLayout sb = new VerticalLayout();
    sb.setImmediate(true);
    sb.setSpacing(true);
    ConcurrentSkipListSet<String> annos = new ConcurrentSkipListSet<String>();
    for(String a : sq.getAvailableAnnotationLevels(ebene))
    {
      annos.add(a);
    }
    this.annonames = annos;//by Martin    
    this.cb = new SensitiveComboBox();
    cb.setCaption(ebene);
    cb.setInputPrompt(ebene);
    cb.setWidth(SB_CB_WIDTH);
    // configure & load content
    cb.setImmediate(true);
    for (String annoname : this.annonames) 
    {
      cb.addItem(annoname);
    }
    cb.setFilteringMode(Filtering.FILTERINGMODE_OFF);//necessary?
    cb.addListener((FieldEvents.TextChangeListener)this);    
    sb.addComponent(cb);
    VerticalLayout sbtoolbar = new VerticalLayout();
    sbtoolbar.setSpacing(false);
    // searchbox tickbox for regex
    reBox = new CheckBox("Regex");
    reBox.setImmediate(true);
    sbtoolbar.addComponent(reBox);
    reBox.addListener(new ValueChangeListener() {
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
    // searchbox tickbox for negative search
    negSearchBox = new CheckBox("Neg. search");
    negSearchBox.setImmediate(true);
    sbtoolbar.addComponent(negSearchBox);
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    sbtoolbar.addComponent(btClose);
    sb.addComponent(sbtoolbar);
    sb.setSpacing(true);
    setContent(sb);    
  }
 
  
  
 
  @Override
  public void buttonClick(Button.ClickEvent event)
  {    
    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);      
    }
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
  public void textChange(TextChangeEvent event)
  {
    if ("specific".equals(sq.getFilterMechanism()))
    {
      ConcurrentSkipListSet<String> notInYet = new ConcurrentSkipListSet<String>();       
      String txt = event.getText();
      if (!txt.equals(""))
      {
        cb.removeAllItems();
        for (Iterator<String> it = annonames.iterator(); it.hasNext();)
        {
          String s = it.next();
          if(rsc.compare(s, txt)==0)
          {
            cb.addItem(s);          
          }
          else {notInYet.add(s);}        
        }
        //startsWith
        for(String s : notInYet)
        {        
          if(rsc.startsWith(s, txt))
          {
            cb.addItem(s);
            notInYet.remove(s);
          }
        }
        //contains
        for(String s : notInYet)
        {
          if(rsc.contains(s, txt))
          {
            cb.addItem(s);
          }
        }
      }
      else
      {
        //have a look and speed it up
        SpanBox.buildBoxValues(cb, ebene, sq);
      }
    }
    
    if ("levenshtein".equals(sq.getFilterMechanism()))       
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
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
  
  public boolean isNegativeSearch()
  {
    return negSearchBox.booleanValue();
  }
  
  public void setValue(String value)
  {
    cb.setValue(value);
  }
}