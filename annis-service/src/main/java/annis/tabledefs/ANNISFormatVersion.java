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
package annis.tabledefs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import jline.internal.InputStreamReader;

/**
 * Definies the schemata of different ANNIS import format versions.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public enum ANNISFormatVersion
{
  
  V3_1(
      ".tab",
      new Table("node").c_int_uniq("id").c_int("text_ref").c_int("corpus_ref").c("layer").c("name")
          .c_int("left").c_int("right").c_int("token_index").c("continuous").c("span"),
      new Table("component").c_int_uniq("id").c("type").c("layer").c("name"),
      new Table("rank").c_int("pre").c_int("post").c_int("node_ref").c_int("component_ref")
          .c_int("parent"),
      new Table("node_annotation").c_int("node_ref").c("namespace").c("name").c("value"),
      new Table("edge_annotation").c_int("rank_ref").c("namespace").c("name").c("value"),
      new Table("corpus").c_int_uniq("id").c("name").c("type").c("version").c_int("pre")
          .c_int("post")
  ), V3_2(
      ".tab",
      new Table("node").c_int_uniq("id").c_int("text_ref").c_int("corpus_ref").c("layer").c("name")
          .c_int("left").c_int("right").c_int("token_index").c("seg_name").c_int("seg_index")
          .c_int("seg_right").c("continuous").c("span"),
      new Table("component").c_int_uniq("id").c("type").c("layer").c("name"),
      new Table("rank").c_int("pre").c_int("post").c_int("node_ref").c_int("component_ref")
          .c_int("parent"),
      new Table("node_annotation").c_int("node_ref").c("namespace").c("name").c("value"),
      new Table("edge_annotation").c_int("rank_ref").c("namespace").c("name").c("value"),
      new Table("corpus").c_int_uniq("id").c("name").c("type").c("version").c_int("pre")
          .c_int("post")
  ), V3_3(
      ".annis",
      new Table("node").c_int_uniq("id").c_int("text_ref").c_int("corpus_ref").c("layer").c("name")
          .c_int("left").c_int("right").c_int("token_index").c_int("left_token")
          .c_int("right_token").c_int("seg_index").c("seg_name").c("span").c("root"),
      new Table("component").c_int_uniq("id").c("type").c("layer").c("name"),
      new Table("rank").c_int_uniq("id").c_int("pre").c_int("post").c_int("node_ref")
          .c_int("component_ref").c_int("parent").c_int("level"),
      new Table("node_annotation").c_int("node_ref").c("namespace").c("name").c("value"),
      new Table("edge_annotation").c_int("rank_ref").c("namespace").c("name").c("value"),
      new Table("corpus").c_int_uniq("id").c("name").c("type").c("version").c_int("pre")
          .c_int("post").c_int("top_level")
  ), UNKNOWN(
      ".x", new Table("node"), new Table("component"), new Table("rank"),
      new Table("node_annotation"), new Table("edge_annotation"), new Table("corpus")
  );
  
  private static final Logger log = LoggerFactory.getLogger(ANNISFormatVersion.class);


  private final String fileSuffix;
  private final Table nodeTable;
  private final Table componentTable;
  private final Table rankTable;
  private final Table nodeAnnotationTable;
  private final Table edgeAnnotationTable;
  private final Table corpusTable;
  

  private ANNISFormatVersion(String fileSuffix, Table nodeTable, Table componentTable,
      Table rankTable, Table nodeAnnotationTable, Table edgeAnnotationTable, Table corpusTable)
  {
    this.fileSuffix = fileSuffix;
    this.nodeTable = nodeTable;
    this.componentTable = componentTable;
    this.rankTable = rankTable;
    this.nodeAnnotationTable = nodeAnnotationTable;
    this.edgeAnnotationTable = edgeAnnotationTable;
    this.corpusTable = corpusTable;
  }

  /**
   * The used file suffix for each single table file.
   * 
   * @return
   */
  public String getFileSuffix()
  {
    return fileSuffix;
  }

  public Table getNodeTable()
  {
    return nodeTable;
  }

  public Table getComponentTable()
  {
    return componentTable;
  }

  public Table getRankTable()
  {
    return rankTable;
  }

  public Table getNodeAnnotationTable()
  {
    return nodeAnnotationTable;
  }

  public Table getEdgeAnnotationTable()
  {
    return edgeAnnotationTable;
  }

  public Table getCorpusTable()
  {
    return corpusTable;
  }
  
  public String getFactsSQL()
  {
    try(InputStream res = this.getClass().getResourceAsStream("facts_" + name() + ".sql"))
    {
      if(res != null)
      { 
        return CharStreams.toString(new InputStreamReader(res, StandardCharsets.UTF_8));
      }
    }
    catch(IOException ex)
    {
      log.error("Can't read SQL for facts definition", ex);
    }
    return null;
  }
}
