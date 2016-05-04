/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.exceptions;

import annis.model.AqlParseError;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnnisQLSyntaxException extends AnnisException
{

  private List<AqlParseError> errors = new LinkedList<>();
  
  public AnnisQLSyntaxException()
  {
    super();
  }

  public AnnisQLSyntaxException(String message)
  {
    super(message);
    errors.add(new AqlParseError(message));
  }
  
  public AnnisQLSyntaxException(String message, List<AqlParseError> errors)
  {
    super(message);
    this.errors = new ArrayList<>(errors);
  }

  public List<AqlParseError> getErrors()
  {
    return errors;
  }

  
}
