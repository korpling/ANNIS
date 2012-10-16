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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
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

import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.model.QueryNode.TextMatching;
import annis.ql.parser.QueryData;
import annis.sqlgen.annopool.ApAnnotationConditionProvider;
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

  // dummy annotation set
  @Mock private Set<QueryAnnotation> annotations;

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

    final TableAccessStrategy tableAccessStrategy = new TableAccessStrategy();
    tableAccessStrategy.addTableAlias(NODE_TABLE, "_node");
    tableAccessStrategy.addTableAlias(COMPONENT_TABLE, "_component");
    tableAccessStrategy.addTableAlias(RANK_TABLE, "_rank");
    tableAccessStrategy.addTableAlias(NODE_ANNOTATION_TABLE, "_annotation");
    tableAccessStrategy
        .addTableAlias(EDGE_ANNOTATION_TABLE, "_rank_annotation");
    tableAccessStrategy.addColumnAlias(NODE_ANNOTATION_TABLE, "namespace",
        "node_annotation_namespace");
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
    generator = new DefaultWhereClauseGenerator()
    {
      @Override
      protected TableAccessStrategy createTableAccessStrategy()
      {
        return tableAccessStrategy;
      }
    };
    generator.setAnnoCondition(new ApAnnotationConditionProvider());

    // simulate three annotations
    when(annotations.size()).thenReturn(3);
   
  }

  // helper method to check create component predicates (name, edgeType)
  // on the expected side
  private void checkEdgeConditions(String componentPredicate, String edgeType,
      String componentName, String... expected)
  {
    List<String> expectedConditions = new ArrayList<String>();
    expectedConditions.add(join("=", "_component23.id", "_component42.id"));
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
    node23.addJoin(new Dominance(node42, 1));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        join("=", "_rank23.pre", "_rank42.parent"));
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
    node23.addJoin(new Dominance(node42, componentName, 1));
    // then
    checkEdgeConditions(componentPredicate, "d", componentName,
        join("=", "_rank23.pre", "_rank42.parent"));
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
    node23.addJoin(new Dominance(node42, componentName, 1));
    node42.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_EQUAL));
    // then
    checkEdgeConditions(componentPredicate, "d", componentName,
        join("=", "_rank23.pre", "_rank42.parent"));
    checkWhereConditions(
        node42,
        "_rank_annotation42.anno_ref= ANY(getAnno('namespace3', 'name3', NULL, '^(value3)$', ARRAY[], 'edge'))"
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
    node23.addJoin(new Dominance(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
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
    node23.addJoin(new Dominance(node42, distance));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
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
    node23.addJoin(new Dominance(node42, min, max));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
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
    node23.addJoin(new LeftDominance(node42));
    // then
    checkEdgeConditions(
        componentPredicate,
        "d",
        null,
        join("=", "_rank23.pre", "_rank42.parent"),
        "_node42.left_token IN (SELECT min(lrsub.left_token) FROM _node AS lrsub, _rank AS lrsub_rank WHERE parent=_rank23.pre AND "
      + "component_id = _rank23.component_id AND corpus_ref=_node42.corpus_ref AND lrsub.toplevel_corpus IN(NULL)"
      + " AND lrsub_rank.toplevel_corpus IN(NULL) AND lrsub_rank.node_ref = lrsub.id)");
  }

  /**
   * WHERE conditions for right dominance (>@r).
   */
  @Theory
  public void shouldGenerateWhereConditionsForRightDominance(
      String componentPredicate)
  {
    // given
    node23.addJoin(new RightDominance(node42));
    // then
    checkEdgeConditions(
        componentPredicate,
        "d",
        null,
        join("=", "_rank23.pre", "_rank42.parent"),
        "_node42.right_token IN (SELECT max(lrsub.right_token) FROM _node AS lrsub, _rank AS lrsub_rank WHERE parent=_rank23.pre AND "
      + "component_id = _rank23.component_id AND corpus_ref=_node42.corpus_ref AND lrsub.toplevel_corpus IN(NULL)"
      + " AND lrsub_rank.toplevel_corpus IN(NULL) AND lrsub_rank.node_ref = lrsub.id)");
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
    node23.addJoin(new PointingRelation(node42, componentName, 1));
    // then
    checkEdgeConditions(componentPredicate, "p", componentName,
        join("=", "_rank23.pre", "_rank42.parent"));
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
    node23.addJoin(new PointingRelation(node42, componentName));
    // then
    checkEdgeConditions(componentPredicate, "p", componentName,
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
    node23.addJoin(new Sibling(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
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
    node23.addJoin(new Sibling(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        join("=", "_rank23.parent", "_rank42.parent"));
  }

  /**
   * WHERE conditions for common ancestor ($*).
   */
  @Theory
  public void shouldGenerateWhereConditionsForCommonAncestor(String componentPredicate)
  {
    // given
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post)",
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
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post)");
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
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post AND toplevel_corpus IN(" + corpusId + "))",
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
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post)",
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
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post)",
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
    node23.addJoin(new CommonAncestor(node42));
    // then
    checkEdgeConditions(componentPredicate, "d", null,
        join("=", "_rank23.component_ref", "_rank42.component_ref"),
        "EXISTS (SELECT 1 FROM _rank AS ancestor WHERE" + "\n\t" +
            "ancestor.component_ref = _rank23.component_ref AND" + "\n\t" +
            "ancestor.pre < _rank23.pre AND _rank23.pre < ancestor.post AND" + "\n\t" +
            "ancestor.pre < _rank42.pre AND _rank42.pre < ancestor.post)",
            join("<>", "_node23.id", "_node42.id"));
  }  

  /**
   * Indirect precedence on PostgreSQL may be optimized by an index on
   * (leftToken - 1).
   */
  @Test
  public void shouldOptimizeIndirectPrecedenceForIndexOnLeftTokenMinus1()
  {
    // given
    generator.setOptimizeIndirectPrecedence(true);
    node23.addJoin(new Precedence(node42));
    // then
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        numberJoin("<=", "_node23.right_token", "_node42.left_token", -1));
  }

  /**
   * WHERE conditions for inclusion (_i_).
   */
  @Test
  public void shouldGenerateWhereConditionsForInclusion()
  {
    node23.addJoin(new Inclusion(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left", "_node42.left"),
        join(">=", "_node23.right", "_node42.right"));
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
    node23.addJoin(new Inclusion(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left", "_node42.left"),
        join("<=", "_node42.left", "_node23.right"),
        join(">=", "_node23.right", "_node42.right"),
        join(">=", "_node42.right", "_node23.left"));
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

  // WHERE condition for namespace
  @Test
  public void whereClauseForNodeNamespace()
  {
    node23.setNamespace("namespace");
    checkWhereConditions(join("=", "_node23.namespace", "'namespace'"));
  }

  // WHERE condition for name
  @Test
  public void whereClauseForNodeName()
  {
    node23.setName("name");
    checkWhereConditions(join("=", "_node23.name", "'name'"));
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
      "_annotation23_1.anno_ref= ANY(getAnno('namespace1', 'name1', NULL, NULL, ARRAY[], 'node'))",
      "_annotation23_2.anno_ref= ANY(getAnno('namespace2', 'name2', 'value2', NULL, ARRAY[], 'node'))",
      "_annotation23_3.anno_ref= ANY(getAnno('namespace3', 'name3', NULL, '^(value3)$', ARRAY[], 'node'))"
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
      "_annotation23_1.anno_ref= ANY(getAnnoNot('namespace2', 'name2', 'value2', NULL, ARRAY[], 'node'))",
      "_annotation23_2.anno_ref= ANY(getAnnoNot('namespace3', 'name3', NULL, '^(value3)$', ARRAY[], 'node'))"
    );
  }

  // WHERE condition for node annotation
  @Test
  public void whereClauseForNodeEdgeAnnotation()
  {
    node23.addEdgeAnnotation(new QueryAnnotation("namespace1", "name1"));
    node23.addEdgeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_EQUAL));
    node23.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_EQUAL));
    checkWhereConditions(
      "_rank_annotation23_1.anno_ref= ANY(getAnno('namespace1', 'name1', NULL, NULL, ARRAY[], 'edge'))",
      "_rank_annotation23_2.anno_ref= ANY(getAnno('namespace2', 'name2', 'value2', NULL, ARRAY[], 'edge'))",
      "_rank_annotation23_3.anno_ref= ANY(getAnno('namespace3', 'name3', NULL, '^(value3)$', ARRAY[], 'edge'))"
    );
  }
  
  @Test
  public void whereClauseForNodeEdgeAnnotationNot()
  {
    node23.addEdgeAnnotation(new QueryAnnotation("namespace2", "name2",
        "value2", TextMatching.EXACT_NOT_EQUAL));
    node23.addEdgeAnnotation(new QueryAnnotation("namespace3", "name3",
        "value3", TextMatching.REGEXP_NOT_EQUAL));
    checkWhereConditions(
      "_rank_annotation23_1.anno_ref= ANY(getAnnoNot('namespace2', 'name2', 'value2', NULL, ARRAY[], 'edge'))",
      "_rank_annotation23_2.anno_ref= ANY(getAnnoNot('namespace3', 'name3', NULL, '^(value3)$', ARRAY[], 'edge'))"
    );
  }

  // WHERE condition for _=_
  @Test
  public void whereClauseForNodeSameSpan()
  {
    node23.addJoin(new SameSpan(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.left", "_node42.left"),
        join("=", "_node23.right", "_node42.right"));
  }

  // WHERE condition for _l_
  @Test
  public void whereClauseForNodeLeftAlignment()
  {
    node23.addJoin(new LeftAlignment(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.left", "_node42.left"));
  }

  // WHERE condition for _r_
  @Test
  public void whereClauseForNodeRightAlignment()
  {
    node23.addJoin(new RightAlignment(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("=", "_node23.right", "_node42.right"));
  }

  // WHERE condition for _ol_
  @Test
  public void whereClauseForNodeLeftOverlap()
  {
    node23.addJoin(new LeftOverlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left", "_node42.left"),
        join("<=", "_node42.left", "_node23.right"),
        join("<=", "_node23.right", "_node42.right"));
  }

  // WHERE condition for _or_
  // FIXME: unnecessary, is exchanged for #2 _ol_ #2
  @Test
  public void whereClauseForNodeRightOverlap()
  {
    node23.addJoin(new RightOverlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join(">=", "_node23.right", "_node42.right"),
        join(">=", "_node42.right", "_node23.left"),
        join(">=", "_node23.left", "_node42.left"));
  }

  // WHERE condition for _o_
  @Test
  public void whereClauseForNodeOverlap()
  {
    node23.addJoin(new Overlap(node42));
    checkWhereConditions(join("=", "_node23.text_ref", "_node42.text_ref"),
        join("<=", "_node23.left", "_node42.right"),
        join("<=", "_node42.left", "_node23.right"));
  }

  @Test
  public void whereClauseForIdentity()
  {
    node23.addJoin(new Identical(node42));
    checkWhereConditions(join("=", "_node23.id", "_node42.id"));
  }

  // /// Helper

  private void checkWhereConditions(String... expected)
  {
    checkWhereConditions(node23, expected);
  }

  private void checkWhereConditions(QueryNode node, String... expected)
  {
    List<QueryNode> alternative = new ArrayList<QueryNode>();
    alternative.add(node);
    Set<String> actual = generator.whereConditions(queryData, alternative, "");
    for (String item : expected)
      assertThat(actual, hasItem(item));
    assertThat(actual, is(size(expected.length)));
  }

}
