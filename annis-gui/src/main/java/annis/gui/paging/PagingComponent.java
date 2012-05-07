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

import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */
public class PagingComponent extends CustomComponent implements
  Button.ClickListener
{

  public static final ThemeResource LEFT_ARROW = new ThemeResource(
    "left_arrow.png");
  public static final ThemeResource RIGHT_ARROW = new ThemeResource(
    "right_arrow.png");
  public static final ThemeResource FIRST = new ThemeResource("first.png");
  public static final ThemeResource LAST = new ThemeResource("last.png");
  private HorizontalLayout layout;
  private Button btFirst;
  private Button btLast;
  private Button btNext;
  private Button btPrevious;
  private TextField txtPage;
  private Label lblMaxPages;
  private Label lblStatus;
  private Set<PagingCallback> callbacks;
  private AtomicInteger count;
  private int pageSize;
  private int currentPage;
  private Label lblInfo;

  public PagingComponent(int count, int pageSize)
  {
    if (pageSize <= 0)
    {
      pageSize = 1;
    }
    if (count < 0)
    {
      count = 0;
    }
    currentPage = 1;
    this.count = new AtomicInteger(pageSize);
    this.pageSize = pageSize;

    setWidth("100%");
    setHeight("-1px");

    addStyleName("toolbar");

    callbacks = new HashSet<PagingCallback>();

    layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setMargin(false, true, false, true);

    Panel root = new Panel(layout);
    root.setStyleName(ChameleonTheme.PANEL_BORDERLESS);

    setCompositionRoot(root);


    lblInfo = new Label();
    lblInfo.addStyleName("right-aligned-text");

    layout.setWidth("100%");
    layout.setHeight("-1px");

    btFirst = new Button();
    btFirst.setIcon(FIRST);
    btFirst.setDescription("jump to first page");
    btFirst.addListener((Button.ClickListener) this);
    btFirst.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);

    btLast = new Button();
    btLast.setIcon(LAST);
    btLast.setDescription("jump to last page");
    btLast.addListener((Button.ClickListener) this);
    btLast.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);

    btNext = new Button();
    btNext.setIcon(RIGHT_ARROW);
    btNext.setDescription("jump to next page");
    btNext.addListener((Button.ClickListener) this);
    btNext.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);

    btPrevious = new Button();
    btPrevious.setIcon(LEFT_ARROW);
    btPrevious.setDescription("jump to previous page");
    btPrevious.addListener((Button.ClickListener) this);
    btPrevious.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);

    txtPage = new TextField();
    txtPage.setDescription("current page");
    txtPage.setHeight("-1px");
    txtPage.setWidth(3.f, UNITS_EM);
    Validator pageValidator = new AbstractStringValidator(
      "must be an integer greater than zero")
    {

      @Override
      protected boolean isValidString(String value)
      {
        try
        {
          int v = Integer.parseInt(value);
          if (v > 0)
          {
            return true;
          }
          else
          {
            return false;
          }
        }
        catch (Exception ex)
        {
          return false;
        }
      }
    };
    txtPage.addValidator(pageValidator);
    root.addAction(new EnterListener(txtPage));

    lblMaxPages = new Label();
    lblMaxPages.setDescription("maximal pages");
    lblMaxPages.setSizeUndefined();

    lblStatus = new Label();
    lblStatus.setSizeUndefined();

    layout.addComponent(btFirst);
    layout.addComponent(btPrevious);
    layout.addComponent(txtPage);
    layout.addComponent(lblMaxPages);
    layout.addComponent(btNext);
    layout.addComponent(btLast);
    layout.addComponent(lblStatus);
    layout.addComponent(lblInfo);

    layout.setComponentAlignment(lblStatus, Alignment.MIDDLE_LEFT);
    layout.setComponentAlignment(lblMaxPages, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(txtPage, Alignment.MIDDLE_RIGHT);

    layout.setExpandRatio(lblStatus, 1.0f);
    layout.setComponentAlignment(lblInfo, Alignment.MIDDLE_RIGHT);
    layout.setExpandRatio(lblInfo, 10.0f);



    update(false);
  }

  private void update(boolean informCallbacks)
  {
    int myCount = count.get();
    txtPage.setValue("" + currentPage);
    lblMaxPages.setValue("/ " + getMaxPage());

    lblStatus.setValue("Displaying Results " + (getStartNumber() + 1)
      + " - " + Math.min(getStartNumber() + pageSize, myCount) + " of "
      + myCount);

    btFirst.setEnabled(currentPage > 1);
    btPrevious.setEnabled(currentPage > 1);
    btLast.setEnabled(currentPage < getMaxPage());
    btNext.setEnabled(currentPage < getMaxPage());


    if (informCallbacks)
    {
      for (PagingCallback c : callbacks)
      {
        c.createPage(getStartNumber(), pageSize);
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
    int mycount = Math.max(0, count.get() - 1);
    return (1 + (mycount / pageSize));
  }

  public int getStartNumber()
  {
    return (currentPage - 1) * pageSize;
  }

  public int getCount()
  {
    return count.get();
  }

  public void setCount(int count, boolean update)
  {
    if (count < 0)
    {
      count = 0;
    }
    this.count.set(count);
    update(update);
  }

  public int getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(int pageSize)
  {
    if (pageSize <= 0)
    {
      pageSize = 1;
    }
    this.pageSize = pageSize;
    update(true);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    if (event.getButton() == btFirst)
    {
      currentPage = 1;
    }
    else if (event.getButton() == btLast)
    {
      currentPage = getMaxPage();
    }
    else if (event.getButton() == btNext)
    {
      currentPage++;
    }
    else if (event.getButton() == btPrevious)
    {
      currentPage--;
    }

    // sanitize
    currentPage = sanitizePage(currentPage);

    // clear list with media player
    String clearglobalMediaList = "if (window.document.mediaElement)"
      + "{"
      + "window.document.mediaElement = undefined;"
      + "}";
    getWindow().executeJavaScript(clearglobalMediaList);

    update(true);
  }

  private int sanitizePage(int page)
  {
    int val = Math.max(1, page);
    val = Math.min(1 + (count.get() / pageSize), page);
    return val;
  }

  public class EnterListener extends ShortcutListener
  {

    private Object registeredTarget;

    public EnterListener(Object registeredTarget)
    {
      super("set page", KeyCode.ENTER, null);
      this.registeredTarget = registeredTarget;
    }

    @Override
    public void handleAction(Object sender, Object target)
    {
      if (target != registeredTarget)
      {
        return;
      }
      try
      {
        int newPage = Integer.parseInt((String) txtPage.getValue());
        currentPage = sanitizePage(newPage);
        update(true);
      }
      catch (Exception ex)
      {
        Logger.getLogger(PagingComponent.class.getName()).log(Level.FINE, null,
          ex);
      }
    }
  }

  public void setInfo(String text)
  {
    lblInfo.setValue(text);
  }
}
