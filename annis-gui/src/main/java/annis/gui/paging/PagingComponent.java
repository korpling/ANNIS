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

import annis.gui.AnnisUI;
import annis.gui.ShareQueryReferenceWindow;
import annis.gui.util.ANNISFontIcon;
import annis.libgui.Helper;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ChameleonTheme;
import com.vaadin.ui.themes.ValoTheme;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class PagingComponent extends Panel implements
  Button.ClickListener
{

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
    PagingComponent.class);

  public static final Resource LEFT_ARROW = ANNISFontIcon.LEFT_ARROW;

  public static final Resource RIGHT_ARROW = ANNISFontIcon.RIGHT_ARROW;

  public static final Resource FIRST = ANNISFontIcon.FIRST;

  public static final Resource LAST = ANNISFontIcon.LAST;

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

  private long currentPage;

  private Label lblInfo;
  
  private final Button btShareQuery;

  public PagingComponent()
  {
    this(0, 0);
  }
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

    callbacks = new HashSet<>();

    layout = new HorizontalLayout();
    layout.setSpacing(true);
    layout.setMargin(new MarginInfo(false, true, false, true));

    setContent(layout);
    addStyleName(ChameleonTheme.PANEL_LIGHT);

    lblInfo = new Label();
    lblInfo.setContentMode(ContentMode.HTML);
    lblInfo.addStyleName("right-aligned-text");
    
    btShareQuery = new Button(FontAwesome.SHARE_ALT);
    btShareQuery.setDescription("Share query reference link");
    btShareQuery.addStyleName(ValoTheme.BUTTON_BORDERLESS);
    
    layout.setWidth("100%");
    layout.setHeight("-1px");

    btFirst = new Button();
    btFirst.setIcon(FIRST);
    btFirst.setDescription("jump to first page");
    btFirst.addClickListener((Button.ClickListener) this);
    btFirst.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    btFirst.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btFirst.setDisableOnClick(true);

    btLast = new Button();
    btLast.setIcon(LAST);
    btLast.setDescription("jump to last page");
    btLast.addClickListener((Button.ClickListener) this);
    btLast.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    btLast.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btLast.setDisableOnClick(true);

    btNext = new Button();
    btNext.setIcon(RIGHT_ARROW);
    btNext.setDescription("jump to next page");
    btNext.addClickListener((Button.ClickListener) this);
    btNext.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    btNext.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btNext.setDisableOnClick(true);

    btPrevious = new Button();
    btPrevious.setIcon(LEFT_ARROW);
    btPrevious.setDescription("jump to previous page");
    btPrevious.addClickListener((Button.ClickListener) this);
    btPrevious.addStyleName(ChameleonTheme.BUTTON_ICON_ONLY);
    btPrevious.addStyleName(ChameleonTheme.BUTTON_SMALL);
    btPrevious.setDisableOnClick(true);

    txtPage = new TextField();
    txtPage.setDescription("current page");
    txtPage.setHeight("-1px");
    txtPage.setWidth(5.f, Unit.EM);
    Validator pageValidator = new PageValidator(
      "must be an integer greater than zero");
    txtPage.addValidator(pageValidator);
    addActionHandler(new EnterHandler(txtPage));
    
    
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
    layout.addComponent(btShareQuery);

    layout.setComponentAlignment(btFirst, Alignment.MIDDLE_LEFT);
    layout.setComponentAlignment(btPrevious, Alignment.MIDDLE_LEFT);
    layout.setComponentAlignment(lblStatus, Alignment.MIDDLE_LEFT);
    layout.setComponentAlignment(lblMaxPages, Alignment.MIDDLE_CENTER);
    layout.setComponentAlignment(txtPage, Alignment.MIDDLE_RIGHT);
    layout.setComponentAlignment(btNext, Alignment.MIDDLE_RIGHT);
    layout.setComponentAlignment(btLast, Alignment.MIDDLE_RIGHT);

    layout.setExpandRatio(lblStatus, 1.0f);
    layout.setComponentAlignment(lblInfo, Alignment.MIDDLE_RIGHT);
    layout.setExpandRatio(lblInfo, 10.0f);



    update(false);
  }

  @Override
  public void attach()
  {
    super.attach();
    if(getUI() instanceof AnnisUI)
    {
      btShareQuery.addClickListener(new QueryReferenceLinkHandler((AnnisUI) getUI()));
    }
    else
    {
      btShareQuery.setVisible(false);
    }
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
        c.switchPage(getStartNumber(), pageSize);
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

  public long getStartNumber()
  {
    return (currentPage - 1) * pageSize;
  }

  public void setStartNumber(long startNumber)
  {
    currentPage = (startNumber / pageSize) + 1;
    update(false);
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

  public void setPageSize(int pageSize, boolean informCallback)
  {
    if (pageSize <= 0)
    {
      pageSize = 1;
    }
    this.pageSize = pageSize;
    update(informCallback);
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    btFirst.setEnabled(true);
    btLast.setEnabled(true);
    btNext.setEnabled(true);
    btPrevious.setEnabled(true);
      
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

    update(true);
  }

  private long sanitizePage(long page)
  {
    long val = Math.max(1, page);
    val = Math.min(1 + (count.get() / pageSize), val);
    return val;
  }
  
  private class QueryReferenceLinkHandler implements Button.ClickListener
  {
    private final AnnisUI ui;

    public QueryReferenceLinkHandler(AnnisUI ui)
    {
      this.ui = ui;
    }
    
    
    @Override
    public void buttonClick(ClickEvent event)
    {
      ShareQueryReferenceWindow w = new ShareQueryReferenceWindow(
        ui.getQueryController().getSearchQuery(),
        !Helper.isKickstarter(getSession())
      );
      getUI().addWindow(w);
      w.center();
    }
    
  }

  private class EnterHandler implements Action.Handler
  {
    private final Action enterKeyShortcutAction 
      = new ShortcutAction(null, ShortcutAction.KeyCode.ENTER, null);

    private final Object registeredTarget;

    public EnterHandler(Object registeredTarget)
    {
      this.registeredTarget = registeredTarget;
    }

    @Override
    public Action[] getActions(Object target, Object sender)
    {
      return new Action[]
      {
        enterKeyShortcutAction
      };
    }
    
    @Override
    public void handleAction(Action action, Object sender, Object target)
    {
      if(action == enterKeyShortcutAction && target == registeredTarget)
      {
        try
        {
          int newPage = Integer.parseInt((String) txtPage.getValue());
          currentPage = sanitizePage(newPage);
          update(true);
        }
        catch (NumberFormatException ex)
        {
          log.error(null, ex);
        }
      }
    }
  }

  /**
   * Cuts off long queries. Actually they are restricted to 50 characters. The
   * full query is available with descriptions (tooltip in gui)
   *
   * @param text the query to display in the result view panel
   */
  public void setInfo(String text)
  {
    if (text != null && text.length() > 0)
    {
      String prefix = "Result for: <span class=\"" + Helper.CORPUS_FONT_FORCE + "\">";
      lblInfo.setDescription(prefix + text.replaceAll("\n", " ") + "</span>");
      lblInfo.setValue(text.length() < 50 ? prefix + StringEscapeUtils.
        escapeHtml4(text.substring(0, text.length())) : prefix + StringEscapeUtils.
        escapeHtml4(text.substring(0, 50)) + " ... </span>");
    }
  }

  private static class PageValidator extends AbstractStringValidator
  {

    public PageValidator(String errorMessage)
    {
      super(errorMessage);
    }

    @Override
    protected boolean isValidValue(String value)
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
  }
}
