package annis.gui.widgets.gwt.client.ui;

import annis.gui.widgets.gwt.client.ui.autoheightiframe.AutoHeightIFrameConnector;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import java.util.logging.Logger;

public class VAutoHeightIFrame extends Widget
{

  private final static Logger logger = Logger.getLogger("VAutoHeightIFrame");

  /**
   * Set the CSS class name to allow styling.
   */
  public static final String CLASSNAME = "v-autoheightiframe";

  /**
   * The client side widget identifier
   */
  private IFrameElement iframe;

  private int additionalHeight;

  private AutoHeightIFrameConnector.LoadCallback callback;

  /**
   * The constructor should first call super() to initialize the component and
   * then handle any initialization relevant to Vaadin.
   */
  public VAutoHeightIFrame()
  {
    super();

    iframe = Document.get().createIFrameElement();
    iframe.setFrameBorder(0);
    iframe.setScrolling("auto");
    
    setElement(iframe);


    // This method call of the Paintable interface sets the component
    // style name in DOM tree
    setStyleName(CLASSNAME);

    addDomHandler(new LoadHandler()
    {
      @Override
      public void onLoad(LoadEvent event)
      {
        if (!iframe.getSrc().endsWith("empty.html"))
        {
          try
          {
            final Document doc = iframe.getContentDocument();
            if (doc != null)
            {
              Timer t = new Timer()
              {
                @Override
                public void run()
                {
                  checkIFrameLoaded(doc);
                }
              };
              t.schedule(100);
            }
          }
          catch (JavaScriptException ex)
          {
            logger.severe(
              "trying to access iframe source from different domain which is forbidden");
          }
        }
      }
    }, LoadEvent.getType());

  }

  private void checkIFrameLoaded(Document doc)
  {
    int newHeight = -1;

    doc.getScrollLeft();
    String contentType = getContentType(doc); //doc.getDocumentElement().getPropertyString("contentType");

    if (contentType != null && contentType.startsWith("image/"))
    {
      // image
      NodeList<Element> imgList = doc.getElementsByTagName("img");
      if (imgList.getLength() > 0)
      {
        ImageElement img = (ImageElement) imgList.getItem(0);
        newHeight = img.getPropertyInt("naturalHeight");
      }
    }
    else
    {
      logger.fine("body height defined?: " + doc.getBody().hasAttribute(
        "scrollHeight"));
      logger.fine("document height defined?: " + doc.getDocumentElement().
        hasAttribute("scrollHeight"));
      int bodyHeight = doc.getBody().getScrollHeight();
      int documentHeight = doc.getDocumentElement().getScrollHeight();


      logger.fine("body scrollHeight: " + bodyHeight
        + "document scrollHeight: " + documentHeight);

      int maxHeight = Math.max(bodyHeight, documentHeight);

      if (maxHeight > 20)
      {
        // real html page or fallback if content type is unknown (e.g. in chrome)
        newHeight = maxHeight + additionalHeight;
      }
    }

    logger.fine("newheight: " + newHeight);

    if (newHeight > -1 && callback != null)
    {
      callback.onIFrameLoaded(newHeight);
    }
  }

  public void setLoadCallback(AutoHeightIFrameConnector.LoadCallback callback)
  {
    this.callback = callback;
  }

  /**
   * Called whenever an update is received from the server
   */
  public void update(String url, int additionalHeight)
  {

    if (iframe.getSrc() != null && url != null && iframe.getSrc().equals(url))
    {
      return;
    }

    if (additionalHeight > -1)
    {
      this.additionalHeight = additionalHeight;
    }

    final Style style = iframe.getStyle();

    style.setWidth(
      100, Style.Unit.PCT);

    if (url != null)
    {
      //VConsole.log("iframe is updated with url " + url );
      iframe.setSrc(url);

    }
  }

  public final native String getContentType(Document doc) /*-{
   return doc.contentType;
   }-*/;
}
