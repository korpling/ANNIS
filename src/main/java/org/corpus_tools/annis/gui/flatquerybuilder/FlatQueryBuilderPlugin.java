/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package org.corpus_tools.annis.gui.flatquerybuilder;

import org.corpus_tools.annis.gui.QueryController;
import org.corpus_tools.annis.gui.querybuilder.QueryBuilderPlugin;
import org.springframework.stereotype.Component;

/**
 * @author martin
 * @author tom
 */
@Component
public class FlatQueryBuilderPlugin implements QueryBuilderPlugin<com.vaadin.ui.Component> { // NO_UCD
                                                                                             // (unused
                                                                                             // code)
    /**
     * 
     */
    private static final long serialVersionUID = -5742603543433685793L;

    @Override
    public com.vaadin.ui.Component createComponent(QueryController controlPanel) {
        FlatQueryBuilder qb = new FlatQueryBuilder(controlPanel);
        return qb;
    }

    @Override
    public String getCaption() {
        return "Word sequences and meta information";
    }

    @Override
    public String getShortName() {
        return "flatquerybuilder";
    }
}