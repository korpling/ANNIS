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
package annis.sqlgen.dblayout;

import annis.administration.FullFactsCorpusAdministration;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.ql.parser.QueryData;
import annis.sqlgen.TableAccessStrategy;
import annis.sqlgen.dblayout.AbstractDatabaseLayout;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.SqlConstraints.join;

/**
 *
 * @author thomas
 */
public class FullFactsLayout extends AbstractDatabaseLayout<FullFactsCorpusAdministration>
{

  @Override
  public String getDescription()
  {
    return "Full facts";
  }

  @Override
  public String getScriptAppendix()
  {
    return "fullfacts";
  }

  @Override
  public FullFactsCorpusAdministration createCorpusAdministration()
  {
    return new FullFactsCorpusAdministration();
  }

  @Override
  public String getDocumentQueryTemplate(String toplevelCorpusName,
    String documentName)
  {
    return "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name, "
      + "node_anno.namespace AS node_annotation_namespace, "
      + "node_anno.\"name\" AS node_annotation_name, "
      + "node_anno.val AS node_annotation_value,\n"
      + "edge_anno.namespace AS edge_annotation_namespace, "
      + "edge_anno.\"name\" AS edge_annotation_name, "
      + "edge_anno.val AS edge_annotation_value\n"
      + "FROM\n"
      + "\tfacts AS facts, corpus as c, corpus as toplevel, \n"
      + "\tannotation_pool as node_anno, annotation_pool as edge_anno\n"
      + "WHERE\n"
      + "\ttoplevel.name = ':toplevel_name' AND c.name = ':document_name' AND facts.corpus_ref = c.id\n"
      + "\tAND c.pre >= toplevel.pre AND c.post <= toplevel.post\n"
      + "\tAND node_anno.id = facts.node_anno_ref AND node_anno.\"type\" = 'node' AND node_anno.toplevel_corpus = toplevel.id\n"
      + "\tAND edge_anno.id = facts.edge_anno_ref AND edge_anno.\"type\" = 'edge' AND edge_anno.toplevel_corpus = toplevel.id\n"
      + "ORDER BY facts.pre";
  }

  @Override
  public String getTextQueryTemplate(long textID)
  {
    return "SELECT DISTINCT \n"
      + "\tARRAY[-1::bigint] AS key, ARRAY[''::varchar] AS key_names, 0 as matchstart, facts.*, c.path_name as path, c.path_name[1] as document_name,"
      + "node_anno.namespace AS node_annotation_namespace, "
      + "node_anno.\"name\" AS node_annotation_name, "
      + "node_anno.val AS node_annotation_value,\n"
      + "edge_anno.namespace AS edge_annotation_namespace, "
      + "edge_anno.\"name\" AS edge_annotation_name, "
      + "edge_anno.val AS edge_annotation_value\n"
      + "FROM\n"
      + "\tfacts AS facts, corpus as c, annotation_pool as node_anno, annotation_pool as edge_anno\n"
      + "WHERE\n"
      + "\tfacts.text_ref = :text_id AND facts.corpus_ref = c.id\n"
      + "\tAND node_anno.id = facts.node_anno_ref AND node_anno.\"type\" = 'node'\n"
      + "\tAND edge_anno.id = facts.edge_anno_ref AND edge_anno.\"type\" = 'edge'\n"
      + "ORDER BY facts.pre";
  }

  @Override
  public List<String> getAnnotateSelectFields(TableAccessStrategy tas)
  {
    ArrayList<String> fields = new ArrayList<String>();

    fields.add(tas.aliasedColumn(NODE_ANNOTATION_TABLE, "namespace") + " AS \"node_annotation_namespace\"");
    fields.add(tas.aliasedColumn(NODE_ANNOTATION_TABLE, "name") + " AS \"node_annotation_name\"");
    fields.add(tas.aliasedColumn(NODE_ANNOTATION_TABLE, "value") + " AS \"node_annotation_value\"");
    fields.add(tas.aliasedColumn(EDGE_ANNOTATION_TABLE, "namespace") + " AS \"edge_annotation_namespace\"");
    fields.add(tas.aliasedColumn(EDGE_ANNOTATION_TABLE, "name") + " AS \"edge_annotation_name\"");
    fields.add(tas.aliasedColumn(EDGE_ANNOTATION_TABLE, "value") + " AS \"edge_annotation_value\"");
    return fields;
  }

  @Override
  public List<String> getAnnotateWhereConditions(TableAccessStrategy tas,
    List<Long> corpusList)
  {
    return new LinkedList<String>();
  }

  @Override
  public List<String> getAnnotateFromTables()
  {
    return new LinkedList<String>();
  }

  @Override
  public void addAnnotationConditions(List<String> conditions, QueryNode node,
    int index, QueryAnnotation annotation, String table, QueryData queryData,
    TableAccessStrategy tas)
  {
    if (annotation.getNamespace() != null)
    {
      conditions.add(join("=",
        tas.aliasedColumn(table, "namespace", index),
        sqlString(annotation.getNamespace())));
    }
    conditions.add(join("=",
      tas.aliasedColumn(table, "name", index),
      sqlString(annotation.getName())));
    if (annotation.getValue() != null)
    {
      QueryNode.TextMatching textMatching = annotation.getTextMatching();
      conditions.add(join(textMatching.sqlOperator(), tas.aliasedColumn(table,
        "value", index),
        sqlString(annotation.getValue(), textMatching)));
    };
  }
}
