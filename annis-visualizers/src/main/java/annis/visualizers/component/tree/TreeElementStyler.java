/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.visualizers.component.tree;

import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.Edge;
import annis.visualizers.component.tree.GraphicsBackend.Font;
import java.awt.Color;
import java.awt.Stroke;

  public interface TreeElementStyler {
	
	Font getFont(AnnisNode n, VisualizerInput input);
	Font getFont(Edge e);
	
	Color getTextBrush(AnnisNode n, VisualizerInput input);
	Color getTextBrush(Edge n);
	
	Color getEdgeColor(Edge n, VisualizerInput input);
	Stroke getStroke(Edge n, VisualizerInput input);
	
	Shape getShape(AnnisNode n, VisualizerInput input);
	Shape getShape(Edge e, VisualizerInput input);
	
	
	int getLabelPadding();
	int getHeightStep();
	int getTokenSpacing();
	int getVEdgeOverlapThreshold();
}
