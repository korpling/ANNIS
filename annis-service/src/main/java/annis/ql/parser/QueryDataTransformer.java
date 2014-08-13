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
package annis.ql.parser;

/**
 * Transformer or optimizer for {@link QueryData}.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public interface QueryDataTransformer
{
  /**
   * Transforms or optimizes a specific {@link QueryData}.
   * The resulting opject might be a clone of the original one.
   * @param data The data to transform.
   * @return  The (possably new object) with the transformation applied.
   */
  public QueryData transform(QueryData data);
}
