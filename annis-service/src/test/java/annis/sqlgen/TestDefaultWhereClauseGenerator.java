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
package annis.sqlgen;

import static annis.sqlgen.SqlConstraints.between;
import static annis.sqlgen.SqlConstraints.isNotNull;
import static annis.sqlgen.SqlConstraints.isNull;
import static annis.sqlgen.SqlConstraints.join;
import static annis.sqlgen.SqlConstraints.numberJoin;
import static annis.sqlgen.SqlConstraints.sqlString;
import static annis.sqlgen.TableAccessStrategy.COMPONENT_TABLE;
import static annis.sqlgen.TableAccessStrategy.EDGE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_ANNOTATION_TABLE;
import static annis.sqlgen.TableAccessStrategy.NODE_TABLE;
import static annis.sqlgen.TableAccessStrategy.RANK_TABLE;
import static annis.test.TestUtils.size;
import static annis.test.TestUtils.uniqueInt;
import static annis.test.TestUtils.uniqueLong;
import static annis.test.TestUtils.uniqueString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import annis.model.Join;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import annis.sqlgen.model.CommonAncestor;
import annis.sqlgen.model.Dominance;
import annis.sqlgen.model.Identical;
import annis.sqlgen.model.Inclusion;
import annis.sqlgen.model.LeftAlignment;
import annis.sqlgen.model.LeftDominance;
import annis.sqlgen.model.LeftOverlap;
import annis.sqlgen.model.Overlap;
import annis.sqlgen.model.PointingRelation;
import annis.sqlgen.model.Precedence;
import annis.sqlgen.model.RightAlignment;
import annis.sqlgen.model.RightDominance;
import annis.sqlgen.model.RightOverlap;
import annis.sqlgen.model.SameSpan;
import annis.sqlgen.model.Sibling;

/*
 * FIXME: refactor tests, so they use the same condition constants everywhere
 * also, get rid of stupid helper functions like join (dup code)
 */
/**
 * Test generation of WHERE clause conditions for each operator and node search.
 */
@RunWith(Theories.class)
public class TestDefaultWhereClauseGenerator
{

  // an example node
  private QueryNode node23;
  private QueryNode node42;

  // object under test: the adapter to that node
  private DefaultWhereClauseGenerator generator;

  // which side to attach component predicates (name and edgeType)
  // in an edge operation
  @DataPoints
  public final static String[] componentPredicates =
  { "lhs", "rhs", "both" };
  
  // test data
  @Mock private QueryData queryData;

  @Before
  public void setup()
  {
    initMocks(this);
    node23 = new QueryNode(23);
    node42 = new QueryNode(42);

    
    generator = new TestWhereClauseGenerator();
    generator.setAnnoCondition(new AnnotationConditionProvider());
   
  }
  
  private static class TestWhereClauseGenerator extends DefaultWhereClauseGenerator
  {
    @Override
    protected TableAccessStrategy createTableAccessStrategy()
    {
      TableAccessStrategy tableAccessStrategy = new TableAccessStrategy();
      tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
      tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_component");
      tableAccessStrategy.addTableAlias(RANK_TABLE, "_rank");
      tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
      tableAccessStrategy
        .addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
      tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "namespace",
        "node_annotation_namespace");
      tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "anno_ref",
        "anno_ref");
      tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "name",
        "node_annotation_name");
      tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "value",
        "node_annotation_value");
      tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "namespace",
        "edge_annotation_namespace");
      tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "name",
        "edge_annotation_name");
      tableAccessStrategy.addColumnAlias(EDGE_ANNOTATION_TABLE, "value",
        "edge_annotation_value");
      return tableAccessStrategy;
    }
    
  }

  // helper method to check create component predicates (name, edgeType)
  // on the expected side
  private void checkEdgeConditions(String componentPredicate, String edgeType,
      String componentName, boolean addComponentIDJoin, String... expected)
  {
    List<String> expectedConditions = new ArrayList<>();
    if(addComponentIDJoin)
    {
      expectedConditions.add(join("=", "_component23.id", "_component42.id"));
    }
    if ("lhs".equals(componentPredicate) || "both".equals(componentPredicate))
    {
      expectedConditions
          .add(join("=", "_component23.type", sqlString(edgeType)));
      if (componentName == null)
      {
        expectedConditions.add(isNull("_component23.name"));
      } else
      {
        expectedConditions.add(join("=", "_component23.name",
            sqlString(componentName)));
      }
    }
    if ("rhs".equals(componentPredicate) || "both".equals(componentPredicate))
    {
      expectedConditions
          .add(join("=", "_component42.type", sqlString(edgeType)));
      if (componentName == null)
      {
        expectedConditions.add(isNull("_component42.name"));
      } else
      {
        expectedConditions.add(join("=", "_component42.name",
            sqlString(componentName)));
      }
    }
    expectedConditions.addAll(asList(expected));
    generator.setComponentPredicates(componentPredicate);
    checkWhereConditions(node23, expectedConditions.toArray(new String[]
    {}));
  }

  /**
   * WHERE conditions for direct dominance operator (>).
   */
  @Theory
  public void shouldGenerateWhereConditionsForNodeDirectDominance(
      String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new Dominance(node42, 1));
    // then
    checkEdgeConditions(componentPredicate, "d", null, false,
        join("=", "_rank23.id", "_rank42.parent"));
  }

  /**
   * WHERE conditions for named direct dominance operator (> name).
   */
  @Theory
  public void shouldGenerateWhereConditionsForNodeNamedDirectDominance(
      String componentPredicate)
  {
    // given
    String componentName = uniqueString();
    node23.addOutgoingJoin(new Dominance(node42, componentName, 1));
    // then
    checkEdgeConditions(componentPredicate, "d", componentName, false,
        join("=", "_rank23.id", "_rank42.parent"));
  }

  /**
   * WHERE conditions for annotated named direct dominance (> name
   * [annotation]).
   */
  @Theory
  public void shouldGenerateWhereConditionsForNamedAndAnnotatedDirectDominance(
      String componentPredicate)
  {
    // given
    String componentName = uniqueString();
    Join j = new Dominance(node42, componentName, 1);
    node23.addOutgoingJoin(j);
    j.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_EQUAL));
    // then
    checkEdgeConditions(componentPredicate, "d", componentName, false,
        join("=", "_rank23.id", "_rank42.parent"));
    checkWhereConditions(
        node42,
        "_rank_annotation42.qannotext ~ '^(namespace3:name3:(value3))$'"
    );
  }

  /**
   * WHERE conditions for indirect dominance (>*).
   */
  @Theory
  public void shouldGenerateWhereConditionsForIndirectDominance(
      String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new Dominance(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        join("<", "_rank23.pre", "_rank42.pre"),
        join("<", "_rank42.pre", "_rank23.post"));
  }

  /**
   * WHERE conditions for exact dominance (>n).
   */
  @Theory
  public void shouldGenerateWhereConditionsForExactDominance(
      String componentPredicate)
  {
    // given
    int distance = uniqueInt();
    node23.addOutgoingJoin(new Dominance(node42, distance));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        join("<", "_rank23.pre", "_rank42.pre"),
        join("<", "_rank42.pre", "_rank23.post"),
        numberJoin("=", "_rank23.level", "_rank42.level", -distance));
  }

  /**
   * WHERE conditions for ranged dominance (>n,m).
   */
  @Theory
  public void shouldGenerateWhereConditionsForRangedDominance(
      String componentPredicate)
  {
    // given
    int min = uniqueInt(1, 10);
    int max = min + uniqueInt(1, 10);
    node23.addOutgoingJoin(new Dominance(node42, min, max));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        join("<", "_rank23.pre", "_rank42.pre"),
        join("<", "_rank42.pre", "_rank23.post"),
        between("_rank23.level", "_rank42.level", -min, -max));
  }

  /**
   * WHERE conditions for left dominance (>@l).
   */
  //
  @Theory
  public void shouldGenerateWhereConditionsForLeftDominance(
      String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new LeftDominance(node42));
    
    long corpusId = uniqueLong();
    given(queryData.getCorpusList()).willReturn(asList(corpusId));
    
    // then
    checkEdgeConditions(
      componentPredicate,
      "d",
      null,
      false,
      join("=", "_rank23.id", "_rank42.parent"),
      "_node42.left_token IN (SELECT min(lrsub.left_token) FROM facts_"
      + corpusId
      + " AS lrsub WHERE parent=_rank23.id AND "
      + "component_id = _rank23.component_id AND corpus_ref=_node42.corpus_ref AND lrsub.toplevel_corpus IN(" + corpusId + ")"
      + ")");
  }

  /**
   * WHERE conditions for right dominance (>@r).
   */
  @Theory
  public void shouldGenerateWhereConditionsForRightDominance(
      String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new RightDominance(node42));
    
    long corpusId = uniqueLong();
    given(queryData.getCorpusList()).willReturn(asList(corpusId));
    
    // then
    checkEdgeConditions(
        componentPredicate,
        "d",
        null,
        false,
        join("=", "_rank23.id", "_rank42.parent"),
        "_node42.right_token IN (SELECT max(lrsub.right_token) FROM facts_" + corpusId + " AS lrsub WHERE parent=_rank23.id AND "
      + "component_id = _rank23.component_id AND corpus_ref=_node42.corpus_ref AND lrsub.toplevel_corpus IN("  + corpusId + ")"
      + ")");
  }

  /**
   * WHERE conditions for direct pointing relation (->).
   */
  @Theory
  public void shouldGenerateWhereConditionsForDirectPointingRelation(
      String componentPredicate)
  {
    // given
    String componentName = uniqueString();
    node23.addOutgoingJoin(new PointingRelation(node42, componentName, 1));
    // then
    checkEdgeConditions(componentPredicate, "p", componentName, false,
        join("=", "_rank23.id", "_rank42.parent"));
  }

  /**
   * WHERE conditions for indirect pointing relation (->*).
   */
  @Theory
  public void shouldGenerateWhereConditionsForIndirectPointingRelation(
      String componentPredicate)
  {
    // given
    String componentName = uniqueString();
    node23.addOutgoingJoin(new PointingRelation(node42, componentName));
    // then
    checkEdgeConditions(componentPredicate, "p", componentName, true,
        join("<", "_rank23.pre", "_rank42.pre"),
        join("<", "_rank42.pre", "_rank23.post"));
  }

  /**
   * WHERE conditions for sibling ($).
   */
  @Theory
  public void shouldGenerateWhereConditionsForSibling(String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new Sibling(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, false,
        join("=", "_rank23.parent", "_rank42.parent"),
        join("<>", "_node23.id", "_node42.id"));
  }
  
  /**
   * The sibling operator may optionally bind the same node to both operands.
   */
  @Theory
  public void shouldAllowIdenticalNodeForSiblingTarget(
      String componentPredicate)
  {
    // given
    generator.setAllowIdenticalSibling(true);
    node23.addOutgoingJoin(new Sibling(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, false,
        join("=", "_rank23.parent", "_rank42.parent"));
  }

  /**
   * WHERE conditions for common ancestor ($*).
   */
  @Theory
  public void shouldGenerateWhereConditionsForCommonAncestor(String componentPredicate)
  {
    // given
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post" +
            "\n\tLIMIT 1)",
        join("<>", "_node23.id", "_node42.id"));
  }
  
  /**
   * The common ancestor operator may optionally bind the same node to both operands.
   */
  @Theory
  public void shouldAllowIdenticalNodeForCommonAncestorTarget(
      String componentPredicate)
  {
    // given
    generator.setAllowIdenticalSibling(true);
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post" +
            "\n\tLIMIT 1)");
  }
  
  /**
   * The common ancestor operator may optionally use a predicate on toplevel_corpus
   * in the EXISTS subquery.
   */
  @Theory
  public void shouldUseToplevelCorpusPredicateForCommonAncestor(
      String componentPredicate)
  {
    // given
    generator.setUseToplevelCorpusPredicateInCommonAncestorSubquery(true);
    long corpusId = uniqueLong();
    given(queryData.getCorpusList()).willReturn(asList(corpusId));
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post AND toplevel_corpus IN(" + corpusId + ")" + "\n\t" +
            "LIMIT 1)",
            join("<>", "_node23.id", "_node42.id"));
  }  

  /**
   * The common ancestor operator should skip the predicate on toplevel_corpus
   * in the EXISTS subquery if the corpus list is empty.
   */
  @Theory
  public void shouldSkipToplevelCorpusPredicateForCommonAncestorIfCorpusListIsEmpty(
      String componentPredicate)
  {
    // given
    generator.setUseToplevelCorpusPredicateInCommonAncestorSubquery(true);
    given(queryData.getCorpusList()).willReturn(new ArrayList<Long>());
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post" +
            "\n\tLIMIT 1)",
            join("<>", "_node23.id", "_node42.id"));
  }  

  /**
   * The common ancestor operator should skip the predicate on toplevel_corpus
   * in the EXISTS subquery if the corpus list is NULL.
   */
  @Theory
  public void shouldSkipToplevelCorpusPredicateForCommonAncestorIfCorpusListIsNull(
      String componentPredicate)
  {
    // given
    generator.setUseToplevelCorpusPredicateInCommonAncestorSubquery(true);
    given(queryData.getCorpusList()).willReturn(null);
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        "EXISTS (SELECT 1 FROM (SELECT * FROM facts LIMIT 0) AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post" + "\n\t" + 
            "LIMIT 1)",
            join("<>", "_node23.id", "_node42.id"));
  }  

  /**
   * The common ancestor operator may optionally use a predicate on toplevel_corpus
   * in the EXISTS subquery.
   */
  @Theory
  public void shouldUseComponentRefPredicateForCommonAncestor(
      String componentPredicate)
  {
    // given
    generator.setUseComponentRefPredicateInCommonAncestorSubquery(true);
    long corpusId = uniqueLong();
    given(queryData.getCorpusList()).willReturn(asList(corpusId));
    node23.addOutgoingJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null, true,
        join("=", "_rank23.component_ref", "_rank42.component_ref"),
        "EXISTS (SELECT 1 FROM facts_" + corpusId + " AS ancestor WHERE" + "\n\t" +
            "ancestor.component_ref = _rank23.component_ref AND" + "\n\t" +
            "ancestor.component_ref = _rank42.component_ref AND" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post\n\t" +
            "LIMIT 1)",
            join("<>", "_node23.id", "_node42.id"));
  }  


  /**
   * WHERE conditions for inclusion (_i_).
   */
  @Test
  public void shouldGenerateWhereConditionsForInclusion()
  {
    node23.addOutgoingJoin(new Inclusion(node42));
    checkWhereConditions(
      join("=", "_node23.text_ref", "_node42.text_ref"),
      join("<=", "_node23.left_token", "_node42.left_token"),
      join(">=", "_node23.right_token", "_node42.right_token"),
      join("<>", "_node23.id", "_node42.id")
    );
  }

  /**
   * Inclusion benefits from two-sided boundaries for both left and right.
   */
  @Test
  public void shouldOptimizeInclusion()
  {
    // given
    generator.setOptimizeInclusion(true);
    // when
    node23.addOutgoingJoin(new Inclusion(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left_token", "_node42.left_token"),
        join("<=", "_node42.left_token", "_node23.right_token"),
        join(">=", "_node23.right_token", "_node42.right_token"),
        join(">=", "_node42.right_token", "_node23.left_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  /**
   * WHERE conditions for isToken (tok).
   */
  @Test
  public void shouldGenerateWhereConditionsForIsToken()
  {
    // given
    generator.setUseIsTokenColumn(true);
    node23.setToken(true);
    // then
    checkWhereConditions("_node23.is_token IS TRUE");
  }

  @Test
  public void shouldGenerateWhereConditionsForIsTokenSpanAlternative()
  {
    // given
    generator.setUseIsTokenColumn(false);
    node23.setToken(true);
    // then
    checkWhereConditions(isNotNull("_node23.span"), isNull("_node23.seg_name"));
  }

  // WHERE condition for root node
  @Test
  public void whereClauseForNodeRoot()
  {
    node23.setRoot(true);
    checkWhereConditions("_rank23.root IS TRUE");
  }

  // WHERE condition for spanned text (string)
  @Test
  public void whereClauseForNodeSpanString()
  {
    node23.setSpannedText("string", TextMatching.EXACT_EQUAL);
    checkWhereConditions(join("=", "_node23.span", "'string'"));
  }

  // WHERE condition for spanned text (regexp)
  @Test
  public void whereClauseForNodeSpanRegexp()
  {
    node23.setSpannedText("regexp", TextMatching.REGEXP_EQUAL);
    checkWhereConditions(join("~", "_node23.span", "'^(regexp)$'"));
  }

  // WHERE condition for node annotation
  @Test
  public void whereClauseForNodeAnnotation()
  {
    node23.addNodeAnnotation(new QueryAnnotation("namespace1", "name1"));
    node23.addNodeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_EQUAL));
    node23.addNodeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_EQUAL));
    
    checkWhereConditions(
      "_annotation23_1.qannotext LIKE 'namespace1:name1:%'",
      "_annotation23_2.qannotext LIKE 'namespace2:name2:value2'",
      "_annotation23_3.qannotext ~ '^(namespace3:name3:(value3))$'"
    );
  }
  
  @Test
  public void whereClauseForNodeAnnotation2Nodes()
  {
    node23.addNodeAnnotation(new QueryAnnotation("namespace1", "name1"));
    node23.addNodeAnnotation(new QueryAnnotation("namespace2", "name2"));
    
    node42.addNodeAnnotation(new QueryAnnotation("namespace3", "name3"));
    node42.addNodeAnnotation(new QueryAnnotation("namespace4", "name4"));
    
    node23.addOutgoingJoin(new Precedence(node42, 1));
    
    
    checkWhereConditions(node23,
      "_annotation23_1.qannotext LIKE 'namespace1:name1:%'",
      "_annotation23_2.qannotext LIKE 'namespace2:name2:%'",
      "_node23.right_token = _node42.left_token - 1",
      "_node23.text_ref = _node42.text_ref"
    );
    checkWhereConditions(node42,
      "_annotation42_1.qannotext LIKE 'namespace3:name3:%'",
      "_annotation42_2.qannotext LIKE 'namespace4:name4:%'"
    );
  }
  
  @Test
  public void whereClauseForNodeAnnotationNot()
  {
    node23.addNodeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_NOT_EQUAL));
    node23.addNodeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_NOT_EQUAL));
    
    checkWhereConditions(
      "_annotation23_1.qannotext NOT LIKE 'namespace2:name2:value2'",
      "_annotation23_1.qannotext LIKE 'namespace2:name2:%'",
      "_annotation23_2.qannotext !~ '^(namespace3:name3:(value3))$'",
      "_annotation23_2.qannotext LIKE 'namespace3:name3:%'"
    );
  }

  // WHERE condition for node annotation
  @Test
  public void whereClauseForNodeEdgeAnnotation()
  {
    Dominance j = new Dominance(node23);
    
    j.addEdgeAnnotation(new QueryAnnotation("namespace1", "name1"));
    j.addEdgeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_EQUAL));
    j.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_EQUAL));
    node42.addOutgoingJoin(j);
    checkWhereConditions(
      "_rank_annotation23_1.qannotext LIKE 'namespace1:name1:%'",
      "_rank_annotation23_2.qannotext LIKE 'namespace2:name2:value2'",
      "_rank_annotation23_3.qannotext ~ '^(namespace3:name3:(value3))$'"
    );
  }
  
  @Test
  public void whereClauseForNodeEdgeAnnotationNot()
  {
    Dominance j = new Dominance(node23);
    
    j.addEdgeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_NOT_EQUAL));
    j.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_NOT_EQUAL));
    
    node42.addOutgoingJoin(j);
    
    checkWhereConditions(
      "_rank_annotation23_1.qannotext NOT LIKE 'namespace2:name2:value2'",
      "_rank_annotation23_1.qannotext LIKE 'namespace2:name2:%'",
      "_rank_annotation23_2.qannotext !~ '^(namespace3:name3:(value3))$'",
      "_rank_annotation23_2.qannotext LIKE 'namespace3:name3:%'"
    );
  }

  // WHERE condition for _=_
  @Test
  public void whereClauseForNodeSameSpan()
  {
    node23.addOutgoingJoin(new SameSpan(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.left_token", "_node42.left_token"),
        join("=", "_node23.right_token", "_node42.right_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }
  
  @Test
  public void whereClauseForNodeSameSpanOperatorHack()
  {
    generator.setHackOperatorSameSpan(true);
    node23.addOutgoingJoin(new SameSpan(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.left_token", "_node42.left_token"),
        join("^=^", "_node23.right_token", "_node42.right_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  // WHERE condition for _l_
  @Test
  public void whereClauseForNodeLeftAlignment()
  {
    node23.addOutgoingJoin(new LeftAlignment(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.left_token", "_node42.left_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  // WHERE condition for _r_
  @Test
  public void whereClauseForNodeRightAlignment()
  {
    node23.addOutgoingJoin(new RightAlignment(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.right_token", "_node42.right_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  // WHERE condition for _ol_
  @Test
  public void whereClauseForNodeLeftOverlap()
  {
    node23.addOutgoingJoin(new LeftOverlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left_token", "_node42.left_token"),
        join("<=", "_node42.left_token", "_node23.right_token"),
        join("<=", "_node23.right_token", "_node42.right_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  // WHERE condition for _or_
  // FIXME: unnecessary, is exchanged for #2 _ol_ #2
  @Test
  public void whereClauseForNodeRightOverlap()
  {
    node23.addOutgoingJoin(new RightOverlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join(">=", "_node23.right_token", "_node42.right_token"),
        join(">=", "_node42.right_token", "_node23.left_token"),
        join(">=", "_node23.left_token", "_node42.left_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  // WHERE condition for _o_
  @Test
  public void whereClauseForNodeOverlap()
  {
    node23.addOutgoingJoin(new Overlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left_token", "_node42.right_token"),
        join("<=", "_node42.left_token", "_node23.right_token"),
        join("<>", "_node23.id", "_node42.id")
    );
  }

  @Test
  public void whereClauseForIdentity()
  {
    node23.addOutgoingJoin(new Identical(node42));
    checkWhereConditions(join("=", "_node23.id", "_node42.id"));
  }

  // /// Helper

  private void checkWhereConditions(String... expected)
  {
    checkWhereConditions(node23, expected);
  }

  private void checkWhereConditions(QueryNode node, String... expected)
  {
    List<QueryNode> alternative = new ArrayList<>();
    alternative.add(node);
    Set<String> actual = generator.whereConditions(queryData, alternative, "");
    for (String item : expected)
    {
      assertThat(actual, hasItem(item));
    }
    assertThat(actual, is(size(expected.length)));
  }

}
