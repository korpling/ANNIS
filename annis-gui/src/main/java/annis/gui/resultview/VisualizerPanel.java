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
package annis.gui.resultview;

import annis.gui.Helper;
import annis.gui.PluginSystem;
import annis.gui.VisualizationToggle;
import annis.gui.media.MediaPlayer;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.VisualizerPlugin;
import annis.resolver.ResolverEntry;
import annis.visualizers.LoadableVisualizer;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the visibility of visualizer plugins and provides some control
 * methods for the media visualizers.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 *
 */
public class VisualizerPanel extends CustomLayout 
  implements Button.ClickListener, VisualizationToggle
{

  private final Logger log = LoggerFactory.getLogger(VisualizerPanel.class);
  public static final ThemeResource ICON_COLLAPSE = new ThemeResource(
    "icon-collapse.gif");
  public static final ThemeResource ICON_EXPAND = new ThemeResource(
    "icon-expand.gif");
  private ApplicationResource resource = null;
  private Component vis;
  private transient SDocument result;
  private transient PluginSystem ps;
  private ResolverEntry entry;
  private Random rand = new Random();
  private transient Map<SNode, Long> markedAndCovered;
  private transient List<SToken> token;
  private Map<String, String> markersExact;
  private Map<String, String> markersCovered;
  private Button btEntry;
  private String htmlID;
  private String resultID;;
  private transient VisualizerPlugin visPlugin;
  private Set<String> visibleTokenAnnos;
  private String segmentationName;
  private final String PERMANENT = "permanent";
  private final String ISVISIBLE = "visible";
  private final String HIDDEN = "hidden";
  private final String PRELOADED = "preloaded";

  private final static String htmlTemplate = 
    "<div id=\":id\"><div location=\"btEntry\"></div>"
    + "<div location=\"iframe\"></div></div>";
    
  /**
   * This Constructor should be used for {@link ComponentVisualizerPlugin}
   * Visualizer.
   *
   */
  public VisualizerPanel(
    final ResolverEntry entry,
    SDocument result,
    List<SToken> token,
    Set<String> visibleTokenAnnos,
    Map<SNode, Long> markedAndCovered,
    @Deprecated Map<String, String> markedAndCoveredMap,
    @Deprecated Map<String, String> markedExactMap,
    String htmlID,
    String resultID,
    SingleResultPanel parent,
    String segmentationName,
    PluginSystem ps) throws IOException
  {
    super(new ByteArrayInputStream(htmlTemplate.replace(":id", htmlID).getBytes("UTF-8")));

    visPlugin = ps.getVisualizer(entry.getVisType());
    
    this.ps = ps;
    this.entry = entry;
    this.markersExact = markedExactMap;
    this.markersCovered = markedAndCoveredMap;


    this.result = result;
    this.token = token;
    this.visibleTokenAnnos = visibleTokenAnnos;
    this.markedAndCovered = markedAndCovered;
    this.segmentationName = segmentationName;
    this.htmlID = htmlID;
    this.resultID = resultID;

    this.addStyleName(ChameleonTheme.PANEL_BORDERLESS);
    this.setWidth("100%");
  }

  @Override
  public void attach()
  {

    if (visPlugin == null && ps != null)
    {
      entry.setVisType(PluginSystem.DEFAULT_VISUALIZER);
      visPlugin = ps.getVisualizer(entry.getVisType());
    }

    if(entry != null && visPlugin != null)
    {
      
      if(HIDDEN.equalsIgnoreCase(entry.getVisibility()))
      {
        // build button for visualizer
        btEntry = new Button(entry.getDisplayName());
        btEntry.setIcon(ICON_EXPAND);
        btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
          + ChameleonTheme.BUTTON_SMALL);
        btEntry.addListener((Button.ClickListener) this);
        addComponent(btEntry, "btEntry");
      }
      else
      {
        
        if ( ISVISIBLE.equalsIgnoreCase(entry.getVisibility())
          || PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          // build button for visualizer
          btEntry = new Button(entry.getDisplayName());
          btEntry.setIcon(ICON_COLLAPSE);
          btEntry.setStyleName(ChameleonTheme.BUTTON_BORDERLESS + " "
            + ChameleonTheme.BUTTON_SMALL);
          btEntry.addListener((Button.ClickListener) this);
          addComponent(btEntry, "btEntry");
        }
        
        
        // create the visualizer and calc input
        try
        {
          vis = createComponent();
          if(vis != null)
          {
            vis.setVisible(true);
            addComponent(vis, "iframe");
          }
        }
        catch(Exception ex)
        {
          getWindow().showNotification(
            "Could not create visualizer " + visPlugin.getShortName(), 
            ex.toString(),
            Window.Notification.TYPE_TRAY_NOTIFICATION
          );
          log.error("Could not create visualizer " + visPlugin.getShortName(), ex);
        }
        
        
        if (PRELOADED.equalsIgnoreCase(entry.getVisibility()))
        {
          btEntry.setIcon(ICON_EXPAND);
          if(vis != null)
          {
            vis.setVisible(false);
          }
        }
        
      }
    } // end if entry not null

  }
  
  private Component createComponent()
  {
    if(visPlugin == null)
    {
      return null;
    }
    
    final Application application = getApplication();
    final VisualizerInput input = createInput();
    
    FutureTask<Component> task = new FutureTask<Component>(new Callable<Component>() 
    {

      @Override
      public Component call() throws Exception
      {
        return visPlugin.createComponent(input, application);
      }
    });
    
    Component c = null;
    try
    {
      Executor exec = Executors.newSingleThreadExecutor();
      exec.execute(task);
      c = task.get(60, TimeUnit.SECONDS);
      c.setVisible(false);
    }
    catch (InterruptedException ex)
    {
      getWindow().showNotification("Could not create visualizer", ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
      log.error(null, ex);
    }
    catch (ExecutionException ex)
    {
      log.error(null, ex);
      getWindow().showNotification("Could not create visualizer", ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
    }
    catch (TimeoutException ex)
    {
      getWindow().showNotification("Timeout when creating visualizer", ex.getMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
      log.error(null, ex);
    }
    finally
    {
      task.cancel(true);
    }
    
    
    return c;
  }

  private VisualizerInput createInput()
  {
    VisualizerInput input = new VisualizerInput();
    input.setAnnisWebServiceURL(getApplication().getProperty(
      "AnnisWebService.URL"));
    input.setContextPath(Helper.getContext(getApplication()));
    input.setDotPath(getApplication().getProperty("DotPath"));
    
    input.setId(resultID);

    input.setMarkableExactMap(markersExact);
    input.setMarkableMap(markersCovered);
    input.setMarkedAndCovered(markedAndCovered);
    input.setVisPanel(this);

    input.setResult(result);
    input.setToken(token);
    input.setVisibleTokenAnnos(visibleTokenAnnos);
    input.setSegmentationName(segmentationName);

    if (entry != null)
    {
      input.setMappings(entry.getMappings());
      input.setNamespace(entry.getNamespace());
      String template = Helper.getContext(getApplication())
        + "/Resource/" + entry.getVisType() + "/%s";
      input.setResourcePathTemplate(template);
    }

    if (visPlugin != null && visPlugin.isUsingText()
      && result != null && result.getSDocumentGraph().getSNodes().size() > 0)
    {
      SaltProject p = getDocument(result.getSCorpusGraph().getSRootCorpus().
        get(0).getSName(), result.getSName());

      input.setDocument(p.getSCorpusGraphs().get(0).getSDocuments().get(0));

    }
    else
    {
      input.setDocument(result);
    }

    return input;
  }
  
  public void setVisibleTokenAnnosVisible(Set<String> annos)
  {
    this.visibleTokenAnnos = annos;
    if(visPlugin != null && vis != null)
    {
      visPlugin.setVisibleTokenAnnosVisible(vis, annos);
    }
  }
  
  public void setSegmentationLayer(String segmentationName, Map<SNode, Long> markedAndCovered)
  {
    this.segmentationName = segmentationName;
    this.markedAndCovered = markedAndCovered;
    
    if(visPlugin != null && vis != null)
    {
      visPlugin.setSegmentationLayer(vis, segmentationName,markedAndCovered);
    }
  }

  public ApplicationResource createResource(
    ByteArrayOutputStream byteStream,
    String mimeType)
  {

    StreamResource r;

    r = new StreamResource(new ByteArrayOutputStreamSource(byteStream), 
      entry.getVisType() + "_" + rand.nextInt(Integer.MAX_VALUE), getApplication());
    r.setMIMEType(mimeType);

    return r;
  }

  private SaltProject getDocument(String toplevelCorpusName, String documentName)
  {
    SaltProject txt = null;
    try
    {
      toplevelCorpusName = URLEncoder.encode(toplevelCorpusName, "UTF-8");
      documentName = URLEncoder.encode(documentName, "UTF-8");
      WebResource annisResource = Helper.getAnnisWebResource(getApplication());
      txt = annisResource.path("query").path("graphs").path(toplevelCorpusName).path(
        documentName).get(SaltProject.class);
    }
    catch (Exception e)
    {
      log.error("General remote service exception", e);
    }
    return txt;
  }

  @Override
  public void detach()
  {
    super.detach();

    if (resource != null)
    {
      getApplication().removeResource(resource);
    }
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    toggleVisualizer(!visualizerIsVisible(), null);
  }

  @Override
  public boolean visualizerIsVisible()
  {
    if(vis == null || !vis.isVisible())
    {
      return false;
    }
    return true;
  }

  
  @Override
  public void toggleVisualizer(boolean visible, LoadableVisualizer.Callback callback)
  {
    
    if (visible)
    {
      // check if it's necessary to create input
      if (visPlugin != null && vis == null)
      {
        try
        {
          
          vis = createComponent();
        }
        catch(Exception ex)
        {
          getWindow().showNotification(
            "Could not create visualizer " + visPlugin.getShortName(), 
            ex.toString(),
            Window.Notification.TYPE_WARNING_MESSAGE
          );
          log.error("Could not create visualizer " + visPlugin.getShortName(), ex);
        }
      }
      // end if create input was needed
      
      if(callback != null && vis instanceof LoadableVisualizer)
      {
        LoadableVisualizer loadableVis = (LoadableVisualizer) vis;
        if(loadableVis.isLoaded())
        {
          // direct call callback since the visualizer is already ready
          callback.visualizerLoaded((LoadableVisualizer) vis);
        }
        else
        {
          loadableVis.clearCallbacks();
          // add listener when player was fully loaded
          loadableVis.addOnLoadCallBack(callback);
        }
      }
          
      if(vis != null)
      {
        btEntry.setIcon(ICON_COLLAPSE);
        vis.setVisible(true);
        if(getComponent("iframe") == null)
        {
          addComponent(vis, "iframe");
        }
      }
    }
    else
    {
      // hide
      
      if (vis != null)
      {
        vis.setVisible(false);
        if(vis instanceof MediaPlayer)
        {
          removeComponent(vis);
        }
      }

      btEntry.setIcon(ICON_EXPAND);
    }

  }

  public String getHtmlID()
  {
    return htmlID;
  }
  
  public static class ByteArrayOutputStreamSource implements StreamResource.StreamSource
  {
    private static final Logger log = LoggerFactory.getLogger(ByteArrayOutputStreamSource.class);
    private transient ByteArrayOutputStream byteStream;

    public ByteArrayOutputStreamSource(ByteArrayOutputStream byteStream)
    {
      this.byteStream = byteStream;
    }
    
    @Override
    public InputStream getStream()
    {
      if(byteStream == null)
      {
        log.error("byte stream was null");
        return null;
      }
      return new ByteArrayInputStream(byteStream.toByteArray());
    }
    
  }

}
