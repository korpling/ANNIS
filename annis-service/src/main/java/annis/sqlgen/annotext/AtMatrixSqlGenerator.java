/*
 * Copyright 2012 SFB 632.
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
package annis.sqlgen.annotext;

import static annis.sqlgen.TableAccessStrategy.*;

import annis.ql.parser.QueryData;
import annis.sqlgen.MatrixSqlGenerator;
import annis.sqlgen.TableAccessStrategy;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AtMatrixSqlGenerator extends MatrixSqlGenerator
{

  @Override
  protected void addFromOuterJoins(StringBuilder sb, QueryData queryData,
    TableAccessStrategy tas,
    String indent)
  {
    // get all the original outer joins
    super.addFromOuterJoins(sb, queryData, tas, indent);

    // TODO: implement matrix outer join definition for annotext

  }

  @Override
  protected String selectAnnotationsString(TableAccessStrategy tas)
  {
    // TODO: implement matrix select for annotext
    return "TODO";
  }

  
}
