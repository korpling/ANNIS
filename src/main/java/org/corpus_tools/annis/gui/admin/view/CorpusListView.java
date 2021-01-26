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

package org.corpus_tools.annis.gui.admin.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public interface CorpusListView extends Serializable {
    public interface Listener {
        public void deleteCorpora(Set<String> corpusName);

    }

    public void addListener(Listener listener);

    public void setAvailableCorpora(Collection<String> corpora);
}
