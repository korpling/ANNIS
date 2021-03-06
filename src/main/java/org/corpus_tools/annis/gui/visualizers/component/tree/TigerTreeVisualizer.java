/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corpus_tools.annis.gui.visualizers.component.tree;

import com.vaadin.ui.Notification;
import edu.uci.ics.jung.graph.DirectedGraph;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.corpus_tools.annis.gui.MatchedNodeColors;
import org.corpus_tools.annis.gui.objects.AnnisConstants;
import org.corpus_tools.annis.gui.visualizers.VisualizerInput;
import org.corpus_tools.annis.gui.visualizers.component.AbstractImageVisualizer;
import org.corpus_tools.annis.gui.visualizers.component.tree.backends.staticimg.AbstractImageGraphicsItem;
import org.corpus_tools.annis.gui.visualizers.component.tree.backends.staticimg.Java2dBackend;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Visualizes a constituent syntax tree.
 *
 * <p>
 * Mappings:<br />
 * The annotation names to be displayed in non terminal nodes can be set e.g. using
 * <b>node_key:cat</b> for an annotation called cat (the default), and similarly the edge labels
 * using <b>edge_key:func</b> for an edge label called <b>func</b> (the default). Instructions are
 * separated using semicolons. <br />
 * <br />
 *
 * With the mapping <b>terminal_name</b> and <b>terminal_ns</b> you can select span nodes with the
 * corresponding annotations as terminal elements instead of the default tokens.
 * </p>
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@Component
public class TigerTreeVisualizer extends AbstractImageVisualizer {

  private static class DefaultLabeler implements TreeElementLabeler, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1302584248978638957L;

    private String extractAnnotation(Set<SAnnotation> annotations, String namespace,
        String featureName) {
      String result = AnnisGraphTools.extractAnnotation(annotations, namespace, featureName);
      if (result == null) {
        result = "--";
      }
      return result;
    }

    @Override
    public String getLabel(SNode n, VisualizerInput input) {

      if (AnnisGraphTools.isTerminal(n, input)) {
        String terminalName = input.getMappings().get(TERMINAL_NAME_KEY);
        if (terminalName == null) {

          String spannedText = ((SDocumentGraph) n.getGraph()).getText(n);
          if (spannedText == null || "".equals(spannedText)) {
            spannedText = " ";
          }
          return spannedText;
        } else {
          String terminalNamespace = input.getMappings().get(TERMINAL_NS_KEY);
          return extractAnnotation(n.getAnnotations(), terminalNamespace, terminalName);
        }
      } else {
        return extractAnnotation(n.getAnnotations(),
            input.getMappings().getOrDefault("node_anno_ns", input.getNamespace()),
            input.getMappings().getOrDefault("node_key", "cat"));
      }
    }

    @Override
    public String getLabel(SRelation e, VisualizerInput input) {
      return extractAnnotation(e.getAnnotations(),
          input.getMappings().getOrDefault("edge_anno_ns", input.getNamespace()),
          input.getMappings().getOrDefault("edge_key", "func"));
    }
  }

  public class DefaultStyler implements TreeElementStyler {

    public static final int LABEL_PADDING = 2;

    public static final int HEIGHT_STEP = 40;

    public static final int TOKEN_SPACING = 15;

    public static final int VEDGE_OVERLAP_THRESHOLD = 20;

    private final BasicStroke DEFAULT_PEN_STYLE = new BasicStroke(1);

    private final Java2dBackend backend;

    private final Font notoSansFontRegular;
    private final Font notoSansFontBold;

    public DefaultStyler(Java2dBackend backend) throws FontFormatException, IOException {
      this.backend = backend;

      // Loading fonts directly from the classpath fails, copy to temporary file first
      File regularFont = File.createTempFile("NotoSans-Regular", ".ttf");
      File boldFont = File.createTempFile("NotoSans-Bold", ".ttf");

      regularFont.deleteOnExit();
      boldFont.deleteOnExit();

      FileUtils.copyToFile(
          new ClassPathResource("NotoSans-Regular.ttf", TigerTreeVisualizer.class).getInputStream(),
          regularFont);
      FileUtils.copyToFile(
          new ClassPathResource("NotoSans-Bold.ttf", TigerTreeVisualizer.class).getInputStream(),
          boldFont);

      this.notoSansFontRegular = Font.createFont(Font.TRUETYPE_FONT, regularFont.getAbsoluteFile());
      this.notoSansFontBold = Font.createFont(Font.TRUETYPE_FONT, boldFont.getAbsoluteFile());
    }

    @Override
    public Color getEdgeColor(SRelation e, VisualizerInput input) {
      if (graphtools.hasEdgeSubtype(e, graphtools.getSecEdgeSubType())) {
        return new Color(0.5f, 0.5f, 0.8f, 0.7f);
      } else {
        return new Color(0.3f, 0.3f, 0.3f);
      }
    }

    @Override
    public GraphicsBackend.Font getFont(SNode n, VisualizerInput input) {
      if (AnnisGraphTools.isTerminal(n, input)) {
        return backend.getFont(this.notoSansFontRegular, 12, java.awt.Font.PLAIN);
      } else {
        return backend.getFont(this.notoSansFontBold, 15, java.awt.Font.BOLD);
      }

    }

    @Override
    public GraphicsBackend.Font getFont(SRelation e) {
      return backend.getFont(this.notoSansFontRegular, 10, java.awt.Font.PLAIN);
    }

    @Override
    public int getHeightStep() {
      return HEIGHT_STEP;
    }


    @Override
    public int getLabelPadding() {
      return LABEL_PADDING;
    }

    @Override
    public Shape getShape(SNode n, VisualizerInput input) {
      SFeature featMatch = n.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
      if (featMatch != null) {
        // get CSS color name
        int matchIdx = featMatch.getValue_SNUMERIC().intValue() - 1;
        MatchedNodeColors[] allColors = MatchedNodeColors.values();
        Color backColor =
            matchIdx >= 0 && matchIdx < allColors.length ? allColors[matchIdx].getColor()
                : Color.RED;


        if (AnnisGraphTools.isTerminal(n, input)) {
          return new Shape.Rectangle(Color.WHITE, backColor, DEFAULT_PEN_STYLE, getLabelPadding());
        } else {
          return new Shape.Ellipse(Color.WHITE, backColor, DEFAULT_PEN_STYLE, getLabelPadding());
        }
      } else {
        if (AnnisGraphTools.isTerminal(n, input)) {
          return new Shape.Invisible(getLabelPadding());
        } else {
          return new Shape.Ellipse(Color.BLACK, Color.WHITE, DEFAULT_PEN_STYLE, getLabelPadding());
        }
      }
    }

    @Override
    public Shape getShape(SRelation e, VisualizerInput input) {
      if (graphtools.hasEdgeSubtype(e, graphtools.getSecEdgeSubType())) {
        return new Shape.Rectangle(getEdgeColor(e, input), Color.WHITE, DEFAULT_PEN_STYLE,
            getLabelPadding());
      } else {
        return new Shape.Rectangle(new Color(0.4f, 0.4f, 0.4f), Color.WHITE, DEFAULT_PEN_STYLE,
            getLabelPadding());
      }
    }

    @Override
    public Stroke getStroke(SRelation e, VisualizerInput input) {
      if (graphtools.hasEdgeSubtype(e, graphtools.getSecEdgeSubType())) {
        return new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,
            new float[] {2, 2}, 0);
      } else {
        return new BasicStroke(2);
      }
    }

    @Override
    public Color getTextBrush(SNode n, VisualizerInput input) {
      SFeature featMatch = n.getFeature(AnnisConstants.ANNIS_NS, AnnisConstants.FEAT_MATCHEDNODE);
      if (featMatch != null) {
        return Color.WHITE;
      } else {
        return Color.BLACK;
      }
    }

    @Override
    public Color getTextBrush(SRelation n) {
      return Color.BLACK;
    }

    @Override
    public int getTokenSpacing() {
      return TOKEN_SPACING;
    }

    @Override
    public int getVEdgeOverlapThreshold() {
      return VEDGE_OVERLAP_THRESHOLD;
    }
  }

  /**
   * 
   */
  private static final long serialVersionUID = -6254684499926926147L;

  private static final int SIDE_MARGIN = 20;

  private static final int TOP_MARGIN = 40;

  private static final int TREE_DISTANCE = 40;

  public static final String TERMINAL_NAME_KEY = "terminal_name";

  public static final String TERMINAL_NS_KEY = "terminal_ns";

  private transient Java2dBackend backend;

  private final DefaultLabeler labeler;

  private transient DefaultStyler styler;

  private AnnisGraphTools graphtools;

  public TigerTreeVisualizer() throws FontFormatException, IOException {
    labeler = new DefaultLabeler();
    initTransients();
  }

  private Graphics2D createCanvas(BufferedImage image) {
    Graphics2D canvas = (Graphics2D) image.getGraphics();
    canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    return canvas;
  }

  private Java2dBackend getBackend() {
    if (backend == null) {
      backend = new Java2dBackend();
    }
    return backend;
  }

  @Override
  public String getContentType() {
    return "image/png";
  }

  @Override
  public String getShortName() {
    return "tree";
  }

  private void initTransients() throws FontFormatException, IOException {
    styler = new DefaultStyler(getBackend());
  }

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException, FontFormatException {
    in.defaultReadObject();
    initTransients();
  }

  private void renderTree(AbstractImageGraphicsItem item, Graphics2D canvas) {
    List<AbstractImageGraphicsItem> allItems = new ArrayList<AbstractImageGraphicsItem>();
    item.getAllChildren(allItems);

    Collections.sort(allItems, (o1, o2) -> o1.getZValue() - o2.getZValue());

    for (AbstractImageGraphicsItem c : allItems) {
      c.draw(canvas);
    }
  }

  @Override
  public void writeOutput(VisualizerInput input, OutputStream outstream) {
    graphtools = new AnnisGraphTools(input);
    List<AbstractImageGraphicsItem> layouts = new LinkedList<AbstractImageGraphicsItem>();

    double width = 0;
    double maxheight = 0;

    for (DirectedGraph<SNode, SRelation> g : graphtools.getSyntaxGraphs()) {
      if (g.getEdgeCount() > 0 && g.getVertexCount() > 0) {

        ConstituentLayouter<AbstractImageGraphicsItem> cl =
            new ConstituentLayouter<AbstractImageGraphicsItem>(g, getBackend(), labeler, styler,
                input, graphtools);

        AbstractImageGraphicsItem item =
            cl.createLayout(new LayoutOptions(VerticalOrientation.TOP_ROOT, AnnisGraphTools
                .detectLayoutDirection(input.getDocument().getDocumentGraph(), input.getUI())));

        Rectangle2D treeSize = item.getBounds();

        maxheight = Math.max(maxheight, treeSize.getHeight());
        width += treeSize.getWidth();
        layouts.add(item);
      }
    }

    BufferedImage image;
    if (width == 0 || maxheight == 0) {
      input.getUI().access(() -> Notification.show("Can't generate tree visualization.",
          Notification.Type.WARNING_MESSAGE));
      image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    } else {
      image =
          new BufferedImage((int) (width + (layouts.size() - 1) * TREE_DISTANCE + 2 * SIDE_MARGIN),
              (int) (maxheight + 2 * TOP_MARGIN), BufferedImage.TYPE_INT_ARGB);
      Graphics2D canvas = createCanvas(image);
      double xOffset = SIDE_MARGIN;
      for (AbstractImageGraphicsItem item : layouts) {
        AffineTransform t = canvas.getTransform();
        Rectangle2D bounds = item.getBounds();
        canvas.translate(xOffset, TOP_MARGIN + maxheight - bounds.getHeight());
        renderTree(item, canvas);
        xOffset += bounds.getWidth() + TREE_DISTANCE;
        canvas.setTransform(t);
      }
    }
    try {
      ImageIO.write(image, "png", outstream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
