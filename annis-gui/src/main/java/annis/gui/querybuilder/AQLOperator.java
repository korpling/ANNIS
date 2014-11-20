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

package annis.gui.querybuilder;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public enum AQLOperator
{
  DIRECT_PRECEDENCE(".", "direct precedence"),
  INDIRECT_PRECEDENCE(".*", "indirect precedence"),
  DIRECT_DOMINANCE(">", "direct dominance"),
  INDIRECT_DOMINANCE(">*", "indirect dominance"),
  IDENT_COVERAGE("_=_", "identical coverage"),
  INCLUSION("_i_", "inclusion"),
  OVERLAP("_o_", "overlap"),
  LEFT_ALIGNED("_l_", "left aligned"),
  RIGHT_ALIGNED("_r_", "right aligned"),
  POINTING("->LABEL", "labeled pointing relation"),
  INDIRECT_POINTING("->LABEL *", "indirect pointing relation"),
  LEFT_MOST_CHILD(">@l", "left-most child"),
  RIGHT_MOST_CHILD(">@r", "right-most child"),
  COMMON_PARENT("$", "common parent node"),
  COMMON_ANCESTOR("$*", "common ancestor node"),
  DIRECT_NEAR("^", "directly near"),
  INDIRECT_NEAR("^*", "indirectly near");
  
  
  
  private final String description;
  private final String op;

  private AQLOperator(String op, String description)
  {
    this.description = description;
    this.op = op;
  }

  public String getDescription()
  {
    return description;
  }

  public String getOp()
  {
    return op;
  }
  
  
  
}
