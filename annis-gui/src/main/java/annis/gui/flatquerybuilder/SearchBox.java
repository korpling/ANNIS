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
import com.vaadin.ui.ComboBox;
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
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

/**
 *
 * @author tom
 */
public class SearchBox extends Panel implements Button.ClickListener/*, 
  FieldEvents.TextChangeListener*/
{
  /*NEW ADDITIONAL ATTRIBUTES*/
  private Collection<ValueField> vfs;
  private Button btAdd;
  
  /*OLD ATTRIBUTE LIST, SOME STUFF CAN BE DELETED*/
  private Button btClose;
  /*DELETE*/private Collection<Button> ors;
  /*DELETE*/private Collection<Button> nors;
  private VerticalNode vn;
  private VerticalLayout vnframe;
  private String ebene;
  //private SensitiveComboBox cb;
  /*DELETE*/ /*private Vector<SensitiveComboBox> cbs;*/
  private DualHashBidiMap<SensitiveComboBox, Button> cbframes;
  private CheckBox reBox;
  private CheckBox negSearchBox;
  /*DELETE*/private Collection<String> annonames;
  private FlatQueryBuilder sq;
  private VerticalLayout sb;
  public static final String BUTTON_CLOSE_LABEL = "X";
  /*DELETE*/private static final String SB_CB_WIDTH = "130px";
  private static final String CAPTION_REBOX = "Regex";
  private static final String NEGATIVE_SEARCH_LABEL = "Neg. search";
  private static final String LABEL_BUTTON_ADD = "+";
  
  public SearchBox(final String level, final FlatQueryBuilder sq, final VerticalNode vn)
  {
    this(level, sq, vn, false, false);
  }
  
  public ValueField addInputField()
  {
    /*OLD CODE*/
    /*HorizontalLayout cbframe = new HorizontalLayout();
    cbframe.setSpacing(true);
    // the combobox
    SensitiveComboBox cbi = new SensitiveComboBox();
    rsc = new reducingStringComparator();/*NOT HERE*//*
    cbi.setNewItemsAllowed(true);
    cbi.setTextInputAllowed(true);*/
    /*if (cbs.isEmpty()){
      cbi.setCaption(ebene);
    } OLD CODE*//*
    if(cbframes.isEmpty())
    {
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
    Button or = new Button("+", (Button.ClickListener) this);
    or.setStyleName(ChameleonTheme.BUTTON_SMALL);
    
    ors.add(or);*/ /*DELETE*/ /*   
    cbframes.put(cbi, or);
    
    cbframe.addComponent(cbi);
    cbframe.addComponent(or);
    cbframe.setComponentAlignment(or, Alignment.BOTTOM_RIGHT);
    sb.addComponent(cbframe);
    return cbi;*/
    
    /*NEW CODE*/
      
    ValueField vf = new ValueField(sq, this, ebene);    
    vfs.add(vf);
    sb.addComponent(vf);    
    
    return vf;
  }
  
  public SearchBox(final String ebene, final FlatQueryBuilder sq, final VerticalNode vn, boolean isRegex, boolean negativeSearch)
  {
    /*OLD CODE*/
    /*this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    this.cbs = new Vector<SensitiveComboBox>(); //DELETE
    cbframes = new DualHashBidiMap<SensitiveComboBox, Button>();
    this.ors = new ArrayList<Button>(); //DELETE
    this.nors = new ArrayList<Button>(); //DELETE
    vnframe = new VerticalLayout();
    this.sb = new VerticalLayout(); //maybe other name? sb is "reserved" by SearchBox
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
    cbs.add(scb); //DELETE
    HorizontalLayout sbtoolbar = new HorizontalLayout();
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
    negSearchBox = new CheckBox(NEGATIVE_SEARCH_LABEL);
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
    setContent(vnframe);*/
    
    /*NEW CODE*/
    this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    this.vfs = new ArrayList<ValueField>();    
    vnframe = new VerticalLayout();
    vnframe.setSpacing(true);
    vnframe.setImmediate(true);
    this.sb = new VerticalLayout(); //maybe other name? sb is "reserved" by SearchBox
    sb.setImmediate(true);
    sb.setSpacing(false);     //used to be true
    HorizontalLayout sbtoolbar = new HorizontalLayout();
    sbtoolbar.setSpacing(false);
    // searchbox tickbox for regex
    reBox = new CheckBox(CAPTION_REBOX);
    reBox.setImmediate(true);
    sbtoolbar.addComponent(reBox);
    /*CHECK THIS LATER!*/
    reBox.addValueChangeListener(new ValueChangeListener() {
      // TODO make this into a nice subroutine
      @Override
      public void valueChange(ValueChangeEvent event) {
        /*for (SensitiveComboBox cbi: cbs){
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
        }*/
        if(reBox.getValue())
        {
          for(ValueField vf : vfs)
          {
            String value = vf.getValue();
            vf.setValueMode(ValueField.ValueMode.REGEX);
            if(value!=null)
            {
              vf.setValue(sq.escapeRegexCharacters(value));
            }
          }
        }
        else
        {
          for(ValueField vf : vfs)
          {
            String value = vf.getValue();
            vf.setValueMode(ValueField.ValueMode.NORMAL);
            if(value!=null)
            {
              vf.setValue(sq.unescape(value));
            }
          }
        }
      }
    });
    reBox.setValue(isRegex);
    // searchbox tickbox for negative search
    negSearchBox = new CheckBox(NEGATIVE_SEARCH_LABEL);
    negSearchBox.setImmediate(true);
    negSearchBox.setValue(negativeSearch);
    
    sbtoolbar.addComponent(negSearchBox);
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ChameleonTheme.BUTTON_SMALL);    
    vnframe.addComponent(btClose);
    vnframe.setComponentAlignment(btClose, Alignment.TOP_RIGHT);
    
    btAdd = new Button(LABEL_BUTTON_ADD);
    btAdd.addClickListener((Button.ClickListener)this);
    btAdd.setStyleName(ChameleonTheme.BUTTON_SMALL);
        
    vnframe.addComponent(sb);
    vnframe.addComponent(btAdd);
    vnframe.setComponentAlignment(btAdd, Alignment.BOTTOM_RIGHT);    
    vnframe.addComponent(sbtoolbar);    
    
    ValueField vf = new ValueField(sq, this, ebene);    
    vfs.add(vf);
    sb.addComponent(vf); 
    
    setContent(vnframe); 
  }
  
  @Override
  public void buttonClick(Button.ClickEvent event)
  {    
    if(event.getButton() == btClose)
    {
      vn.removeSearchBox(this);      
    }
    /*else if(event.getComponent()==reBox)
    {
      boolean r = reBox.getValue();
      for (SensitiveComboBox cb: cbframes.keySet()){
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
    }*//*OLD CODE
    if (ors.contains(event.getButton())){
      reBox.setValue(true);
      reBox.setEnabled(false);
      Button or = event.getButton();
      HorizontalLayout cbframe = (HorizontalLayout) or.getParent();
      cbframe.removeComponent(or);
      Button nor = new Button("-", (Button.ClickListener) this);
      nor.setStyleName(ChameleonTheme.BUTTON_SMALL);
      nors.add(nor);
      cbframe.addComponent(nor);
      cbframe.setComponentAlignment(nor, Alignment.BOTTOM_RIGHT);
      SensitiveComboBox scb = newInputField();
      cbs.add(scb);
    }
    
    if (nors.contains(event.getButton())){
      Button nor = event.getButton();
      HorizontalLayout cbframe = (HorizontalLayout) nor.getParent();
      sb.removeComponent(cbframe);
      cbs.remove((SensitiveComboBox) cbframe.getComponent(0));
      if (cbs.size() == 1){
        cbs.get(0).setCaption(ebene);
        reBox.setEnabled(true);
      }      
    }*/
    
    else if(event.getButton()==btAdd)
    {
      addInputField();
      if(vfs.size()>1)
      {
        reBox.setValue(true);
        reBox.setEnabled(false);
      }
    }
  }
  /*
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
        for(Integer k : keys.subSet(0, 10)){
          List<String> values = new ArrayList(levdistvals.get(k));
          Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
          for(String v : values){
            cb.addItem(v);
          }
        }
      }
    }
  }
  */
  public String getAttribute()
  {
    return ebene;
  }
  
  public String getValue()
  {
    StringBuilder stringbuild = new StringBuilder();
    for (ValueField vf: vfs){
      if (vfs.size() > 1){
        stringbuild.append("(");
      }
      stringbuild.append(vf.getValue());
      if (vfs.size() > 1){
        stringbuild.append(")");
      }
      stringbuild.append("|");
    }
    if (vfs.size() > 1){
      reBox.setValue(true);
      reBox.setEnabled(false);
    }
    return stringbuild.toString().substring(0, stringbuild.toString().length()-1);
  }
  
  public boolean isRegEx()
  {
    return reBox.getValue();
  }
  
  /*DELETE (OLD SOURCE)
  public static String removeAccents(String text) {
    return text == null ? null
        : Normalizer.normalize(text, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }*/
  
  public boolean isNegativeSearch()
  {
    return negSearchBox.getValue();
  }
  
  public void setValue(String value)
  {
    /*
     * actually there is a problem within this method and
     * the constructor in consequence. This method should
     * be delivered a parameter isRegex which gives information
     * about the characteristics of the value to be set.
     * The constructor should actually not be given this
     * information. negativeSearch might be the same...
     */
    for(ValueField vf : vfs)
    {
      sb.removeComponent(vf);
    }
    vfs.clear();    
    ValueField vf = addInputField();
    vf.setValue(value);
  }
  
  public void setValue(Collection<String> values)
  {
    /*CLEAR SEARCHBOX*/
    for(ValueField vf : vfs)
    {
      sb.removeComponent(vf);
    }    
    vfs.clear();       
    /* if this method is called, there are always at least
     * two values. Regex has to be ticked and deactivated.
     */ 
    reBox.setValue(true);
    reBox.setEnabled(false);
    for(String s : values)
    {
      s = sq.escapeRegexCharacters(s); //try to improve, critical characters were unescaped nanoseconds ago
      addValue(s);
    }    
  }
  
  private void addValue(String value)
  {
    /*OLD CODE*/
    /*SensitiveComboBox cbi = new SensitiveComboBox();
    if(cbi.containsId(value))
    {
      cbi.addItem(value);
    }
    cbi.setValue(value);
    cbs.add(cbi);*/
    
    /*NEW CODE*/
    ValueField vf = addInputField();
    /*
     * vf.setValueMode(ValueField.ValueMode.REGEX);
     *
     * this line is not needed (at this state), because
     * a change in the reBox-Value in setValue(...)
     * causes the REGEX-Mode to be set
     */
    vf.setValue(value);
  }
  
  public void removeValueField(ValueField vf)
  {
    sb.removeComponent(vf);
    vfs.remove(vf);
    if(vfs.size()<2)
    {
      reBox.setEnabled(true);
      /*STAY IN REGEX-MODE +++ IN A LATER VERSION THE FORMER STATE CAN BE STORED*/
      /*TO DO*/
    }
  }
}