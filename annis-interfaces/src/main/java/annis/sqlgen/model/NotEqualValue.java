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

package annis.sqlgen.model;

import annis.model.QueryNode;

/**
 * A join that means that two nodes don't have the same annotation/span value.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class NotEqualValue extends NonBindingJoin
{

  public NotEqualValue(QueryNode target)
  {
    super(target);
  }

  @Override
  public String toAqlOperator()
  {
    return "!=";
  }

  @Override
  public String toString()
  {
    return "not equal value " + target.getId();
  }
  
  
  
}
