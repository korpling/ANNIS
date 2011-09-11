/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.paging;

import com.vaadin.addon.chameleon.ChameleonTheme;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author thomas
 */
public class PagingComponent extends CustomComponent implements 
  Button.ClickListener, TextChangeListener
{
  public static final ThemeResource LEFT_ARROW = new ThemeResource("left_arrow.png");
  public static final ThemeResource RIGHT_ARROW = new ThemeResource("right_arrow.png");
  public static final ThemeResource FIRST = new ThemeResource("first.png");
  public static final ThemeResource LAST = new ThemeResource("last.png");
  
  private HorizontalLayout layout;
  private Button btFirst;
  private Button btLast;
  private Button btNext;
  private Button btPrevious;
  private TextField txtPage;
  private Label lblMaxPages;
  
  private Set<PagingCallback> callbacks;
  
  private int count;
  private int pageSize;
  private int currentPage;
  
  public PagingComponent(int count, int pageSize)
  {
    if(pageSize <= 0)
    {
      pageSize = 1;
    }
    if(count < 0)
    {
      count = 0;
    }
    currentPage = 1;
    this.count = count;
    this.pageSize = pageSize;    
    
    callbacks = new HashSet<PagingCallback>();
    
    layout = new HorizontalLayout();
    
    layout.setSpacing(true);
    
    setCompositionRoot(layout);
    
  }

  @Override
  public void attach()
  {
    super.attach();
    
    
    btFirst = new Button();
    btFirst.setIcon(FIRST);
    btFirst.setDescription("jump to first page");
    btFirst.addListener(this);
    btFirst.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    btLast = new Button();
    btLast.setIcon(LAST);
    btLast.setDescription("jump to last page");
    btLast.addListener(this);
    btLast.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    btNext = new Button();
    btNext.setIcon(RIGHT_ARROW);
    btNext.setDescription("jump to next page");
    btNext.addListener(this);
    btNext.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    btPrevious = new Button();
    btPrevious.setIcon(LEFT_ARROW);
    btPrevious.setDescription("jump to previous page");
    btPrevious.addListener(this);
    btPrevious.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    
    txtPage = new TextField();
    txtPage.setDescription("current page");
    txtPage.addListener(this);
    txtPage.setWidth(3.f, UNITS_EM);
    
    Validator pageValidator = new AbstractStringValidator("must be an integer greater than zero") {

      @Override
      protected boolean isValidString(String value)
      {
        try
        {
          int v = Integer.parseInt(value);    
          if(v > 0)
          {
            return true;
          }
          else
          {
            return false;
          }
        }
        catch(Exception ex)
        {
          return false;
        }
      }
    };
    txtPage.addValidator(pageValidator);
    txtPage.addListener(this);
    
    lblMaxPages = new Label();
    lblMaxPages.setDescription("maximal pages");
    
    layout.addComponent(btFirst);
    layout.addComponent(btPrevious);
    layout.addComponent(txtPage);
    layout.addComponent(lblMaxPages);
    layout.addComponent(btNext);
    layout.addComponent(btLast);
        
    update(false);
  }
  
  
  
  private void update(boolean informCallbacks)
  {
    txtPage.setValue("" + currentPage);
    lblMaxPages.setValue("/ " + getMaxPage());
    
    if(informCallbacks)
    {
      for(PagingCallback c : callbacks)
      {
        c.createPage((currentPage-1)*pageSize, pageSize);
      }
    }
  }
  
  public void addCallback(PagingCallback callback)
  {
    callbacks.add(callback);
  }
  
  public boolean removeCallback(PagingCallback callback)
  {
    return callbacks.remove(callback);
  }

  
  public int getMaxPage()
  {
    return (1+(count/pageSize));
  }
  public int getCount()
  {
    return count;
  }

  public void setCount(int count)
  {
    if(count < 0)
    {
      count = 0;
    }
    this.count = count;
    update(true);
  }

  public int getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(int pageSize)
  {
    if(pageSize <= 0)
    {
      pageSize = 1;
    }  
    this.pageSize = pageSize;
    update(true);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if(event.getButton() == btFirst)
    {
      currentPage = 1;
    }
    else if(event.getButton() == btLast)
    {
      currentPage = getMaxPage();
    }
    else if(event.getButton() == btNext)
    {
      currentPage++;
    }
    else if(event.getButton() == btPrevious)
    {
      currentPage--;
    }
    
    // sanitize
    currentPage = sanitizePage(currentPage);
    
    update(true);
  }
  
  private int sanitizePage(int page)
  {
    int val =  Math.max(1, page);
    val = Math.min(1+(count/pageSize), page);
    return val;
  }

  @Override
  public void textChange(TextChangeEvent event)
  {
    if(event.getSource() == txtPage)
    {
      try
      {
        int newPage = Integer.parseInt((String) txtPage.getValue());
        currentPage = sanitizePage(newPage);
        update(true);
      }
      catch(Exception ex)
      {
        // ignore
      }
    }
  }
  
  
}
