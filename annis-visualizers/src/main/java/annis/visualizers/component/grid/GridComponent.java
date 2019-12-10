/*
 * Copyright 2019 Humboldt-Universität zu Berlin.
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
package annis.visualizers.component.grid;

import com.vaadin.ui.Component;

public interface GridComponent extends Component {

    public static final String MAPPING_ANNOS_KEY = "annos";
    public static final String MAPPING_ANNO_REGEX_KEY = "anno_regex";
    public static final String MAPPING_HIDE_TOK_KEY = "hide_tok";
    public static final String MAPPING_TOK_ANNOS_KEY = "tok_anno";
    public static final String MAPPING_ESCAPE_HTML = "escape_html";
    public static final String MAPPING_SHOW_NAMESPACE = "show_ns";
    public static final String MAPPING_GRID_TEMPLATES = "templates";

}