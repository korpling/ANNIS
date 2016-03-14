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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Collection;

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
  private Label lbl;
  private boolean reBoxSingleValue; /*Saves the boolean value of reBox when the SearchBox shifts from single-value to multi-value*/
  
  private Button btClose;
  private VerticalNode vn;
  private VerticalLayout vnframe;
  private String ebene;
  private CheckBox reBox;
  private CheckBox negSearchBox;
  private FlatQueryBuilder sq;
  private VerticalLayout sb;
  public static final String BUTTON_CLOSE_LABEL = "X";
  private static final String CAPTION_REBOX = "Regex";
  private static final String NEGATIVE_SEARCH_LABEL = "Neg. search";
  private static final String LABEL_BUTTON_ADD = "+";
  
  public SearchBox(final String level, final FlatQueryBuilder sq, final VerticalNode vn)
  {
    this(level, sq, vn, false, false);
  }
  
  public ValueField addInputField()
  {
    ValueField vf = new ValueField(sq, this, ebene);    
    vfs.add(vf);
    sb.addComponent(vf);
    if(vfs.size()>1)
    {
      vfs.iterator().next().setProtected(false);
    }
    else
    {
      vfs.iterator().next().setProtected(true);
    }
    
    return vf;
  }
  
  public SearchBox(final String ebene, final FlatQueryBuilder sq, final VerticalNode vn, boolean isRegex, boolean negativeSearch)
  {
    this.vn = vn;
    this.ebene = ebene;
    this.sq = sq;
    this.vfs = new ArrayList<>();    
    vnframe = new VerticalLayout();
    vnframe.setSpacing(true);
    vnframe.setImmediate(true);
    this.sb = new VerticalLayout(); //maybe other name? sb is "reserved" by SearchBox
    sb.setImmediate(true);
    sb.setSpacing(false);     //used to be true
    lbl = new Label(ebene);    
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
    reBox.setEnabled(true);
    reBoxSingleValue = isRegex;
    // searchbox tickbox for negative search
    negSearchBox = new CheckBox(NEGATIVE_SEARCH_LABEL);
    negSearchBox.setImmediate(true);
    negSearchBox.setValue(negativeSearch);
        
    sbtoolbar.addComponent(negSearchBox);
    // close the searchbox
    btClose = new Button(BUTTON_CLOSE_LABEL, (Button.ClickListener) this);
    btClose.setStyleName(ValoTheme.BUTTON_SMALL);    
    
    HorizontalLayout titleBar = new HorizontalLayout();
    titleBar.setWidth(vnframe.getWidth(), vnframe.getWidthUnits());
    titleBar.addComponent(lbl);
    titleBar.setComponentAlignment(lbl, Alignment.BOTTOM_LEFT);
    titleBar.addComponent(btClose);
    titleBar.setComponentAlignment(btClose, Alignment.TOP_RIGHT);    
    
    btAdd = new Button(LABEL_BUTTON_ADD);
    btAdd.addClickListener((Button.ClickListener)this);
    btAdd.setStyleName(ValoTheme.BUTTON_SMALL);
    
    vnframe.addComponent(titleBar);
    vnframe.addComponent(sb);    
    vnframe.addComponent(btAdd);
    vnframe.setComponentAlignment(btAdd, Alignment.BOTTOM_RIGHT);    
    vnframe.addComponent(sbtoolbar);    
    
    ValueField vf = new ValueField(sq, this, ebene);
    vf.setProtected(true);
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
      sq.updateQuery();
    }    
    else if(event.getButton()==btAdd)
    {
      addInputField();      
      if(vfs.size()==2)
      {
        reBoxSingleValue = reBox.getValue();
        reBox.setValue(true);
        reBox.setEnabled(false);
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
    for (ValueField vf : vfs){
      if (vfs.size() > 1){
        stringbuild.append("(");
      }
      stringbuild.append(vf.getValue());
      if (vfs.size() > 1){
        stringbuild.append(")");
      }
      stringbuild.append("|");
    }
    return stringbuild.toString().substring(0, stringbuild.toString().length()-1);
  }
  
  public boolean isRegEx()
  {
    return reBox.getValue();
  }
  
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
    reBox.setEnabled(true);
    reBox.setValue(reBoxSingleValue);
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
      addValue(s);
    }    
  }
  
  private void addValue(String value)
  {
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
      reBox.setValue(reBoxSingleValue);
      vfs.iterator().next().setProtected(true);
    }
  }
}