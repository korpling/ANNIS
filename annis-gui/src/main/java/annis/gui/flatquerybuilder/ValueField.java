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

import annis.libgui.Helper;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Martin
 */
public class ValueField extends Panel implements TextChangeListener, Button.ClickListener
{
  /*BASIC ELEMENTS*/
  private HorizontalLayout frame;
  private SensitiveComboBox scb;
  private Button bt;
  
  /*ADDITIONAL ATTRIBUTES*/
  private FlatQueryBuilder sq;
  private SearchBox sb;
  private ConcurrentSkipListMap<String, String> values;
  private ValueMode vm;
  
  /*LABELS AND STRINGS*/
  private static final String BUTTON_LABEL_REMOVE = "X";
  private static final String SCB_STYLE_NAME = Helper.CORPUS_FONT_FORCE;
  private static final String SCB_WIDTH = "130px";
  
  public ValueField(FlatQueryBuilder sq, SearchBox sb, String level)
  {    
    /*INIT VALUES*/
    this.sq = sq;
    this.sb = sb; 
    vm = (sb.isRegEx()) ? ValueMode.REGEX : ValueMode.NORMAL;
    values = new ConcurrentSkipListMap<>();
    for(String v : sq.getAvailableAnnotationLevels(level))
    {      
      values.put(v, sq.escapeRegexCharacters(v));
    }    
        
    /*SENSITIVE COMBOBOX*/
    scb = new SensitiveComboBox();
    scb.setImmediate(true);
    scb.setNewItemsAllowed(true);
    scb.setTextInputAllowed(true);
    scb.setFilteringMode(FilteringMode.OFF);
    scb.addListener((TextChangeListener)this);
    scb.setItemCaptionMode(ItemCaptionMode.ID);    
    buildValues(vm);   
    scb.addStyleName(SCB_STYLE_NAME);
    scb.setWidth(SCB_WIDTH);
    
    /*BUTTON*/
    bt = new Button(BUTTON_LABEL_REMOVE);
    bt.addClickListener((Button.ClickListener)this);
    bt.setStyleName(ChameleonTheme.BUTTON_SMALL);
    
    /*HORIZONTAL LAYOUT*/
    frame = new HorizontalLayout();
    frame.setSpacing(true);//used to be true    
    frame.setCaption(level);
    
    /*VISUALIZE*/
    frame.addComponent(scb);
    frame.addComponent(bt);
    frame.setComponentAlignment(bt, Alignment.BOTTOM_RIGHT);
    setContent(frame);
  }
  
  @Deprecated /*ever seen this? ^^ just wrote it and it's already deprecated*/
  public ValueField(FlatQueryBuilder sq, SearchBox sb, String level, String value)
  {
    this(sq, sb, level);
    scb.setValue(value);
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {
    if(event.getButton()==bt)
    {
      sb.removeValueField(this);
    }
  }
  
  @Override
  public void textChange(TextChangeEvent event)
  {
    ReducingStringComparator rsc = sq.getRSC();
    String fm = sq.getFilterMechanism();
    if (!"generic".equals(fm))
    {
      ConcurrentSkipListSet<String> notInYet = new ConcurrentSkipListSet<>();       
      String txt = event.getText();
      if (!txt.equals(""))
      {
        scb.removeAllItems();
        for (Iterator<String> it = values.keySet().iterator(); it.hasNext();)
        {
          String s = it.next();
          if(rsc.compare(s, txt, fm)==0)
          {
            scb.addItem(s);            
          }
          else {notInYet.add(s);}        
        }
        //startsWith
        for(String s : notInYet)
        {        
          if(rsc.startsWith(s, txt, fm))
          {
            scb.addItem(s);
            notInYet.remove(s);
          }
        }
        //contains
        for(String s : notInYet)
        {
          if(rsc.contains(s, txt, fm))
          {
            scb.addItem(s);
          }
        }
      }
      else
      {
        buildValues(this.vm);
      }
    }
    else
    {
      String txt = event.getText();
      HashMap<Integer, Collection<String>> levdistvals = new HashMap<>();
      if (txt.length() > 1)
      {
        scb.removeAllItems();
        for(String s : values.keySet())
        {
          Integer d = StringUtils.getLevenshteinDistance(removeAccents(txt).toLowerCase(), removeAccents(s).toLowerCase());
          if (levdistvals.containsKey(d)){
            levdistvals.get(d).add(s);
          }
          if (!levdistvals.containsKey(d)){
            Set<String> newc = new TreeSet<>();
            newc.add(s);
            levdistvals.put(d, newc);
          }
        }
        SortedSet<Integer> keys = new TreeSet<>(levdistvals.keySet());
        for(Integer k : keys.subSet(0, 10)){
          List<String> valueList = new ArrayList<>(levdistvals.get(k));
          Collections.sort(valueList, String.CASE_INSENSITIVE_ORDER);
          for(String v : valueList){
            scb.addItem(v);
          }
        }
      }
    }
  }
  
  public String getValue()
  {    
    return (String)scb.getValue();
    /*ATTENTION: scb.getValue().toString() causes NullPointerException*/
  }
  
  public void setValue(String value)
  {
    if((vm.equals(ValueMode.REGEX))&&(!scb.containsId(value)))
    {
      scb.addItem(value);
      scb.setItemCaption(value, value+" (user defined)");
    }
    scb.setValue(value);
  }
  
  public boolean isRegex()
  {
    return (vm==ValueMode.REGEX);
  }
  
  public void setValueMode(ValueMode vm)
  {
    if(!vm.equals(this.vm))
    {
      buildValues(vm); 
    }
  }
  
  private void buildValues(ValueMode vm)
  {
    this.vm = vm; 
    if(vm.equals(ValueMode.REGEX))
    {
      scb.removeAllItems();
      //scb.setItemCaptionMode(ItemCaptionMode.EXPLICIT);        
      for(String v : values.keySet())
      {
        String item = values.get(v);
        //String itemCaption = v;
        scb.addItem(item);
        //scb.setItemCaption(item, itemCaption);
      }
    }
    else
    {
      scb.removeAllItems();
      scb.setItemCaptionMode(ItemCaptionMode.ID);
      for(String v : values.keySet())
      {
        scb.addItem(v);          
      }
    }
  }
  
  public Button getButton()
  {
    return bt;
  }
  
  public SensitiveComboBox getSCB()
  {
    return scb;
  }
  
  public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
  
  public void setProtected(boolean p)
  {
    bt.setVisible(!p);
  }
  
  public enum ValueMode
  {
    NORMAL, REGEX;
  }
}
