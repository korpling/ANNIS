/*
 * Copyright 2014 SFB 632.
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

package annis.visualizers.component.kwic;

import com.vaadin.ui.Component;
import java.util.Map;
import java.util.Set;

/**
 * A KWIC (Keyword in context) visualization shows the token of the match and
 * their context in a table like view. This is the basic interface for
 * different variants of the KWIC panel implementation.
 */
public interface KWICInterface extends Component
{

  public void setVisibleTokenAnnos(Set<String> annos);

  public void setSegmentationLayer(String segmentationName,
    Map<String, Long> markedAndCovered);
  
}
