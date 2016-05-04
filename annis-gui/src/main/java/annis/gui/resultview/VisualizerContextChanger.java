/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.objects.PagedResultQuery;
import annis.libgui.ResolverProvider;
import java.io.Serializable;
import org.corpus_tools.salt.common.SaltProject;

/**
 * Defines a facade for changing the context of all visualizer for a single
 * result. Typically this interface should be implement by the controller of the
 * single results or the container class.
 *
 * @see SingleResultPanel
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public interface VisualizerContextChanger extends Serializable
{

  /**
   * Registers the visibility status of single visualizer. The ordinary state is
   * determined by the resolver entry, which is usually provided by the
   * {@link ResolverProvider}. If this status is changed, it must be propagated
   * to an instance of the interface.
   *
   * @param entryId The entry id, which represents the row id in the resolver
   * map table, thus this value should be unique.
   * @param status If true the visualizer is visible.
   */
  public void registerVisibilityStatus(long entryId, boolean status);

  /**
   * Reinitiates all registered visualizer with a new salt project.
   *
   * @param p the project, all visualizer are updated with.
   * @param q originally query, for determine the current context
   */
  public void updateResult(SaltProject p, PagedResultQuery q);

  /**
   * Sends a new query to the ANNIS-Service in order to increase/decrease the
   * context of a single result.
   *
   * @param resultNumber the number of the result.
   * @param context The size of the context.
   * @param left Which context must be change. If true the left context is
   * changed, otherwise the right one.
   */
  public void changeContext(long resultNumber, int context,
    boolean left);
}
