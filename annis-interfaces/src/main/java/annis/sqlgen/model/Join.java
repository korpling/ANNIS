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
 * This class is only here in order to be backwards compatible to external code
 * and might be removed in later releases.
 * 
 * @deprecated Use {@link annis.model.Join} directly instead.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Deprecated
public class Join extends annis.model.Join
{

  public Join(QueryNode target)
  {
    super(target);
  }
  
}
