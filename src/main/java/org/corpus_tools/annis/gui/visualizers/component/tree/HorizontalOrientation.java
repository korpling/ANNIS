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

import java.util.Comparator;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.DataSourceSequence;

public enum HorizontalOrientation {
  LEFT_TO_RIGHT(1), RIGHT_TO_LEFT(-1);

  private final int directionModifier;

  HorizontalOrientation(int directionModifier_) {
    directionModifier = directionModifier_;
  }

  Comparator<SNode> getComparator() {
    return (o1, o2) -> {
      SDocumentGraph docGraph = (SDocumentGraph) o1.getGraph();

      DataSourceSequence seq1 =
          docGraph.getOverlappedDataSourceSequence(o1, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);
      DataSourceSequence seq2 =
          docGraph.getOverlappedDataSourceSequence(o2, SALT_TYPE.STEXT_OVERLAPPING_RELATION).get(0);

      return directionModifier * (int) (seq1.getStart().longValue() - seq2.getStart().longValue());
    };
  }
}
