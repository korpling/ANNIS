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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
import java.util.Vector;
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
  private Button or;
  private VerticalNode vn;
  private VerticalLayout vnframe;
  private String ebene;
  private SensitiveComboBox cb;
  private Vector<SensitiveComboBox> cbs;
  private CheckBox reBox;
  private CheckBox negSearchBox;
  private Collection<String> annonames;
  private FlatQueryBuilder sq;
  private VerticalLayout sb;
  public static final String BUTTON_CLOSE_LABEL = "X";
  private static final String SB_CB_WIDTH = "140px";
  private static final String CAPTION_REBOX = "Regex";
  private static reducingStringComparator rsc;
  
  public SearchBox(final String level, final FlatQueryBuilder sq, final VerticalNode vn)
  {
    this(level, sq, vn, false, false);
  }
  
  public SensitiveComboBox newInputField(){
    HorizontalLayout cbframe = new HorizontalLayout();
    cbframe.setSpacing(true);
    // the combobox
    SensitiveComboBox cbi = new SensitiveComboBox();
    rsc = new reducingStringComparator();
    cbi.setNewItemsAllowed(true);
    cbi.setTextInputAllowed(true);
    if (cbs.size() == 0){
      cbi.setCaption(ebene);
    }
    cbi.setWidth(SB_CB_WIDTH);
    // configure & load content
    cbi.setImmediate(true);
    for (String annoname : this.annonames) 
    {
      cbi.addItem(annoname);
    }
    cbi.setFilteringMode(Filtering.FILTERINGMODE_OFF);//necessary?
    cbi.addListener((FieldEvents.TextChangeListener)this);
    // the or operator
    this.or = new Button("+", (Button.ClickListener) this);
    or.setStyleName(ChameleonTheme.BUTTON_SMALL);
    cbframe.addComponent(cbi);
    cbframe.addComponent(or);
    cbframe.setComponentAlignment(or, Alignment.BOTTOM_RIGHT);
    sb.addComponent(cbframe);
    return cbi;
  }
  
  public SearchBox(final String ebene, final FlatQueryBuilder sq, final VerticalNode vn, boolean isRegex, boolean negativeSearch)
  {
    this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    this.cbs = new Vector<SensitiveComboBox>();
    vnframe = new VerticalLayout();
    this.sb = new VerticalLayout();
    sb.setImmediate(true);
    sb.setSpacing(true);
    ConcurrentSkipListSet<String> annos = new ConcurrentSkipListSet<String>();
    for(String a : sq.getAvailableAnnotationLevels(ebene))
    {
      annos.add(a);
    }
    this.annonames = annos;//by Martin
    SensitiveComboBox scb = newInputField();
    scb.addStyleName("corpus-font-force");
    cbs.add(scb);
    VerticalLayout sbtoolbar = new VerticalLayout();
    sbtoolbar.setSpacing(false);
    // searchbox tickbox for regex
    reBox = new CheckBox(CAPTION_REBOX);
    reBox.setImmediate(true);
    sbtoolbar.addComponent(reBox);
    reBox.addValueChangeListener(new ValueChangeListener() {
      // TODO make this into a nice subroutine
      @Override
      public void valueChange(ValueChangeEvent event) {
        for (SensitiveComboBox cbi: cbs){
          boolean r = reBox.getValue();
          if(!r) {
            SpanBox.buildBoxValues(cbi, ebene, sq);
          }
          else if(cbi.getValue()!=null){
            String escapedItem = sq.escapeRegexCharacters(cbi.getValue().toString());
            //String escapedItem = cb.getValue().toString();
            cbi.addItem(escapedItem);
            cbi.setValue(escapedItem);
          }
        }
      }
    });
    reBox.setValue(isRegex);
    // searchbox tickbox for negative search
    negSearchBox = new CheckBox("Neg. search");
    negSearchBox.setImmediate(true);
    negSearchBox.setValue(negativeSearch);
    sbtoolbar.addComponent(negSearchBox);
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);
    vnframe.addComponent(btClose);
    vnframe.setComponentAlignment(btClose, Alignment.TOP_RIGHT);
    vnframe.addComponent(sb);
    vnframe.addComponent(sbtoolbar);
    setContent(vnframe);    
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {    
    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);      
    }
    if(event.getComponent()==reBox)
    {
      boolean r = reBox.booleanValue();
      for (SensitiveComboBox cb: cbs){
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
    if (event.getButton() == or){
      SensitiveComboBox scb = newInputField();
      cbs.add(scb);
    }
  }
  
  @Override
  public void textChange(TextChangeEvent event)
  {
    this.cb = (SensitiveComboBox) event.getSource();
    String fm = sq.getFilterMechanism();
    if (!"generic".equals(fm))
    {
      ConcurrentSkipListSet<String> notInYet = new ConcurrentSkipListSet<String>();       
      String txt = event.getText();
      if (!txt.equals(""))
      {
        cb.removeAllItems();
        for (Iterator<String> it = annonames.iterator(); it.hasNext();)
        {
          String s = it.next();
          if(rsc.compare(s, txt, fm)==0)
          {
            cb.addItem(s);          
          }
          else {notInYet.add(s);}        
        }
        //startsWith
        for(String s : notInYet)
        {        
          if(rsc.startsWith(s, txt, fm))
          {
            cb.addItem(s);
            notInYet.remove(s);
          }
        }
        //contains
        for(String s : notInYet)
        {
          if(rsc.contains(s, txt, fm))
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
    
    if ("generic".equals(sq.getFilterMechanism()))       
    {
      String txt = event.getText();
      HashMap<Integer, Collection> levdistvals = new HashMap<Integer, Collection>();
      if (txt.length() > 1)
      {
        cb.removeAllItems();
        for(String s : annonames)
        {
          Integer d = StringUtils.getLevenshteinDistance(removeAccents(txt).toLowerCase(), removeAccents(s).toLowerCase());
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
    StringBuilder stringbuild = new StringBuilder();
    for (SensitiveComboBox cbi: cbs){
      stringbuild.append("(" + cbi.getValue().toString() + ")");
      stringbuild.append("|");
    }
    if (cbs.size() > 1){
      this.reBox.setValue(Boolean.TRUE);
    }
    return stringbuild.toString().substring(0, stringbuild.toString().length()-1);
  }
  
  public boolean isRegEx()
  {
    return reBox.getValue();
  }
  
  public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
  
  public boolean isNegativeSearch()
  {
    return negSearchBox.getValue();
  }
  
  public void setValue(String value)
  {
    cbs.removeAllElements();
    SensitiveComboBox cbi = new SensitiveComboBox();
    if(reBox.getValue())
    {
      cbi.addItem(value);
    }
    cbi.setValue(value);
    cbs.add(cbi);
  }
}