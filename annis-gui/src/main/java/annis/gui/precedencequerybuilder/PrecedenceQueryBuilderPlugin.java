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
package annis.gui.precedencequerybuilder;

import annis.gui.QueryController;
import annis.gui.querybuilder.QueryBuilderPlugin;
import net.xeoh.plugins.base.annotations.PluginImplementation;



/**
 *
 * @author tom
 */

@PluginImplementation
public class PrecedenceQueryBuilderPlugin implements QueryBuilderPlugin<PrecedenceQueryBuilder>
{

  @Override
  public String getShortName()
  {
    return "precedencequerybuilder";
  }

  @Override
  public String getCaption()
  {
    return "Precedence (Word sequences)";
  }

  @Override
  public PrecedenceQueryBuilder createComponent(QueryController controlPanel)
  {
    return new PrecedenceQueryBuilder(controlPanel);
  }
}
