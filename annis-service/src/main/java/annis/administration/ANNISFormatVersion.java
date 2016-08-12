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
package annis.administration;

import annis.tabledefs.Column;
import annis.tabledefs.Table;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public enum ANNISFormatVersion
{
  V3_1(".tab", new Table("node")
      .c_int_uniq("id").c_int("text_ref").c_int("corpus_ref")
      .c("layer").c("name").c_int("left").c_int("right")
      .c_int("token_index")
      .c("continuous").c("span")
      ),
  V3_2(".tab", new Table("node")
      .c_int_uniq("id").c_int("text_ref").c_int("corpus_ref")
      .c("layer").c("name").c_int("left").c_int("right")
      .c_int("token_index").c("seg_name").c_int("seg_index").c_int("seg_right")
      .c("continuous").c("span")
  ), 
  V3_3(".annis", new Table("node")
      .c_int_uniq("id").c_int("text_ref").c_int("corpus_ref")
      .c("layer").c("name")
      .c_int("left").c_int("right")
      .c_int("token_index").c_int("left_token").c_int("right_token")
      .c_int("seg_index").c("seg_name")
      .c("span").c("root")
  ), 
  UNKNOWN(".x", new Table("node"));
  
  private final String fileSuffix;
  private final Table nodeTable;

  private ANNISFormatVersion(String fileSuffix, Table nodeTable)
  {
    this.fileSuffix = fileSuffix;
    this.nodeTable = nodeTable;
  }

  public String getFileSuffix()
  {
    return fileSuffix;
  }
  
  public Table getNodeTable()
  {
    return nodeTable;
  }
  
}
