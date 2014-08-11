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

package annis.gui.components.codemirror;

import annis.model.AqlParseError;
import com.vaadin.shared.ui.JavaScriptComponentState;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AqlCodeEditorState extends JavaScriptComponentState
{
  /** The current text of the editor **/
  public String text = "";
  /** The last string as sent by the client **/
  public String clientText = "";
  public String inputPrompt = "";
  public List<AqlParseError> errors = new LinkedList<AqlParseError>();

}
