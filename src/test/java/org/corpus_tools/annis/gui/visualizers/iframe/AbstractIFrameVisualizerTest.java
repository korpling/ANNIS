package org.corpus_tools.annis.gui.visualizers.iframe;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.ServletContext;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.resultview.VisualizerPanel;
import org.corpus_tools.annis.gui.visualizers.IFrameResourceMap;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.widgets.AutoHeightIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractIFrameVisualizerTest {

  AbstractIFrameVisualizer vis;

  Optional<IOException> writeException;

  private VisualizerInput visInput;

  private VisualizerPanel visPanel;

  private AnnisUI ui;

  private VaadinSession session;

  private ServletContext servletContext;

  @BeforeEach
  void setUp() throws Exception {

    this.writeException = Optional.empty();
    this.vis = new AbstractIFrameVisualizer() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getShortName() {
        return "test";
      }

      @Override
      public void writeOutput(VisualizerInput input, OutputStream outstream) {
        try {
          outstream.write("test".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
          AbstractIFrameVisualizerTest.this.writeException = Optional.of(e);
        }
      }
    };

    // Init mocks
    visInput = mock(VisualizerInput.class);
    visPanel = mock(VisualizerPanel.class);
    ui = mock(AnnisUI.class);
    session = mock(VaadinSession.class);
    servletContext = mock(ServletContext.class);

    when(visInput.getUI()).thenReturn(ui);
    when(ui.getSession()).thenReturn(session);
    when(ui.getServletContext()).thenReturn(servletContext);
  }

  @Test
  void contextPathSet() {
    IFrameResourceMap attributes = new IFrameResourceMap();
    when(session.getAttribute(any(Class.class))).thenReturn(attributes);

    // Test with trailing slash
    when(servletContext.getContextPath()).thenReturn("/somestring/");

    Component component = vis.createComponent(visInput, visPanel);
    assertNotNull(component);
    assertTrue(component instanceof AutoHeightIFrame);
    if (component instanceof AutoHeightIFrame) {
      AutoHeightIFrame iframeVis = (AutoHeightIFrame) component;
      assertTrue(iframeVis.getState().getUrl().matches("/somestring/vis-iframe-res/.*"),
          () -> "getState().getUrl() of AutoHeightIFrame state should match \"/somerandom/vis-iframe-res/.*\" but was \""
              + iframeVis.getState().getUrl() + "\"");
    }

    // Test without trailing slash
    when(servletContext.getContextPath()).thenReturn("/somestring");

    component = vis.createComponent(visInput, visPanel);
    assertNotNull(component);
    assertTrue(component instanceof AutoHeightIFrame);
    if (component instanceof AutoHeightIFrame) {
      AutoHeightIFrame iframeVis = (AutoHeightIFrame) component;
      assertTrue(iframeVis.getState().getUrl().matches("/somestring/vis-iframe-res/.*"),
          () -> "getState().getUrl() of AutoHeightIFrame state should match \"/somerandom/vis-iframe-res/.*\" but was \""
              + iframeVis.getState().getUrl() + "\"");

    }
  }
}
