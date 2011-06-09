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
package de.deutschdiachrondigital.dddquery;

import static annis.ql.parser.AstBuilder.newStart;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static test.IsCollectionEmpty.empty;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import annis.exceptions.AnnisMappingException;
import annis.ql.node.Start;
import annis.ql.parser.AnnisParser;
import de.deutschdiachrondigital.dddquery.DddQueryMapper.InternalMapper;

// FIXME: tests für pointing relations und neue dominanz-syntax
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"DddQueryMapper-context.xml"})
public class TestDddQueryMapper {
	
	// object under test
	private DddQueryMapper mapper;
	
	// DddQueryMapper that is managed by Spring
	@Autowired DddQueryMapper springManagedMapper;
	
	@Before
	public void setup() {
		mapper = new DddQueryMapper();
		mapper.setAnnisParser(new AnnisParser());
		mapper.setInternalMapper(new InternalMapper());
	}
	
	///// flow control

	@Test
	public void translate() {
		final String ANNIS_QL = "Annis QL query";
		final Start STATEMENT = newStart(null);
		final String DDD_QUERY = "DDDquery";
		
		AnnisParser annisParser = mock(AnnisParser.class);
		when(annisParser.parse(ANNIS_QL)).thenReturn(STATEMENT);
		
		InternalMapper internalMapper = mock(InternalMapper.class);
		when(internalMapper.getDddQuery()).thenReturn(DDD_QUERY);
		
		DddQueryMapper mapper = new DddQueryMapper();
		mapper.setAnnisParser(annisParser);
		mapper.setInternalMapper(internalMapper);
		
		String actual = mapper.translate(ANNIS_QL);
		assertThat(actual, is(DDD_QUERY));
		
		verify(annisParser).parse(ANNIS_QL);
		verify(internalMapper).caseStart(STATEMENT);
		verify(internalMapper).getDddQuery();
	}
	
	// parentheses of top-level group are stripped
	@Test
	public void stripOuterParentheses() {
		final String ANNIS_QL = "Annis QL query";
		final Start STATEMENT = newStart(null);
		final String DDD_QUERY = "DDDquery";
		final String GROUPED_DDD_QUERY = "( " + DDD_QUERY + " )";
		
		AnnisParser annisParser = mock(AnnisParser.class);
		when(annisParser.parse(ANNIS_QL)).thenReturn(STATEMENT);
		
		InternalMapper internalMapper = mock(InternalMapper.class);
		when(internalMapper.getDddQuery()).thenReturn(GROUPED_DDD_QUERY);
		
		DddQueryMapper mapper = new DddQueryMapper();
		mapper.setAnnisParser(annisParser);
		mapper.setInternalMapper(internalMapper);
		
		String actual = mapper.translate(ANNIS_QL);
		assertThat(actual, is(DDD_QUERY));
	}
	
	///// thread safety
	
	@Test
	public void springManagedIsThreadSafe() {
		InternalMapper internalMapper1 = springManagedMapper.getInternalMapper();
		InternalMapper internalMapper2 = springManagedMapper.getInternalMapper();
		assertThat(internalMapper1, is(not(sameInstance(internalMapper2))));
	}
	
	///// translate corpus list

	// map list of numbers
	@Test
	public void translateCorpusList() {
		final String CORPUS_LIST = "1 2 3";
		final List<Long> expected = Arrays.asList(1L, 2L, 3L);
		assertThat(mapper.translateCorpusList(CORPUS_LIST), is(expected));
	}
	
	// empty list
	@Test
	public void translateCorpusListEmptyList() {
		assertThat(mapper.translateCorpusList(""), is(empty()));
	}
	
	// null list throws IllegalArgumentException
	@Test(expected=IllegalArgumentException.class)
	public void translateCorpusListNull() {
		mapper.translateCorpusList(null);
	}
	
	// bad number value throws IllegalArgumentException
	@Test(expected=IllegalArgumentException.class)
	public void translateCorpusListBadNumber() {
		mapper.translateCorpusList("1 2x 3");
	}
	
	
	///// search expressions
	
	@Test
	public void textSearch() {
		testMapping("\"text\"", "element()#(n1)[. = \"text\"]$n1");
	}
	
	@Test
	public void regexpSearch() {
		testMapping("/regexp/", "element()#(n1)[. = r\"regexp\"]$n1");
	}
	
	@Test
	public void annotationSearchExistance() {
		testMapping("namespace:name", "element()#(n1)[@namespace:name]$n1");
	}
	
	@Test
	public void annotationSearchExistanceNoNamespace() {
		testMapping("name", "element()#(n1)[@name]$n1");
	}
	
	@Test
	public void annotationSearch() {
		testMapping("namespace:name=\"value\"", "element()#(n1)[@namespace:name = \"value\"]$n1");
	}
	
	@Test
	public void annotationSearchRegexpNoNamespace() {
		testMapping("name=/value/", "element()#(n1)[@name = r\"value\"]$n1");
	}
	
	@Test
	public void tokenSearch() {
		testMapping("tok", "element()#(n1)[isToken()]$n1");
	}
	
  @Test
	public void textSearchMapping() {
		testMapping("tok = \"text\"", "element()#(n1)[. = \"text\"]$n1");
	}

  @Test
	public void textSearchNotEqual() {
		testMapping("tok != \"text\"", "element()#(n1)[. != \"text\"]$n1");
	}

	@Test
	public void nodeSearch() {
		testMapping("node", "element()#(n1)$n1");
	}
	
	///// binary linguistic contraints
	
	@Test
	public void exactCover() {
		testBinaryLingOp("#1 _=_ #2", "$n1/matching-element::$n2");
	}
	
	@Test
	public void leftAligned() {
		testBinaryLingOp("#1 _l_ #2", "$n1/left-align::$n2");
	}
	
	@Test
	public void rightAligned() {
		testBinaryLingOp("#1 _r_ #2", "$n1/right-align::$n2");
	}
	
	@Test
	public void inclusion() {
		testBinaryLingOp("#1 _i_ #2", "$n1/containing::$n2");
	}
	
	@Test
	public void leftOverlap() {
		testBinaryLingOp("#1 _ol_ #2", "$n1/overlapping-following::$n2");
	}
	
	// FIXME: add _or_ to Annis grammar
	@Ignore
	public void rightOverlap() {
		testBinaryLingOp("#1 _or_ #2", "$n1/overlapping-preceding::$n2");
	}
	
	// FIXME: add _o_ to Annis grammar
	@Ignore
	public void overlap() {
		testBinaryLingOp("#1 _o_ #2", "$n1/overlapping::$n2");
	}
	
	@Test
	public void directPrecedence() {
		testBinaryLingOp("#1 . #2", "$n1/immediately-following::$n2");
	}
	
	@Test
	public void indirectPrecedence() {
		testBinaryLingOp("#1 .* #2", "$n1/following::$n2");
	}
	
	@Test
	public void exactPrecedence() {
		testBinaryLingOp("#1 .10 #2", "$n1/following(10)::$n2");
	}
	
	@Test
	public void rangedPrecedence() {
		testBinaryLingOp("#1 .10,20 #2", "$n1/following(10, 20)::$n2");
	}
	
	@Test
	public void directDominance() {
		testBinaryLingOp("#1 > #2", "$n1/child[d]::$n2");
	}
	
	@Test
	public void directDominanceNamed() {
		testBinaryLingOp("#1 > name #2", "$n1/child[d, name]::$n2");
	}
	
	@Test
	public void directDominanceNamedAndAnnotated() {
		testBinaryLingOp("#1 > name [annotation] #2", "$n1/child[d, name](annotation)::$n2");
	}
	
	@Test
	public void indirectDominance() {
		testBinaryLingOp("#1 >* #2", "$n1/descendant[d]::$n2");
	}
	
	@Test
	public void indirectDominanceNamed() {
		testBinaryLingOp("#1 > name * #2", "$n1/descendant[d, name]::$n2");
	}
	
	@Test
	public void exactDominance() {
		testBinaryLingOp("#1 >10 #2", "$n1/descendant[d](10)::$n2");
	}
	
	@Test
	public void exactDominanceNamed() {
		testBinaryLingOp("#1 > name 10 #2", "$n1/descendant[d, name](10)::$n2");
	}
	
	@Test
	public void rangedDominance() {
		testBinaryLingOp("#1 >10,20 #2", "$n1/descendant[d](10, 20)::$n2");
	}
	
	@Test
	public void rangedDominanceNamed() {
		testBinaryLingOp("#1 > name 10,20 #2", "$n1/descendant[d, name](10, 20)::$n2");
	}
	
	@Ignore // We're getting rid of that mapping anyway
	public void leftDominance() {
		testBinaryLingOp("#1 >@l #2", "$n1/left-child::$n2");
	}
	
	@Ignore // We're getting rid of that mapping anyway
	public void rightDominance() {
		testBinaryLingOp("#1 >@r #2", "$n1/right-child::$n2");
	}
	
	@Test
	public void sibling() {
		testBinaryLingOp("#1 $ #2", "$n1/sibling::$n2");
	}
	
	///// boolean combinations and grouping
	
	@Test
	public void booleanAndGrouping() {
		testMapping("node & ( node & #1 > #2 | node & #1 > #3 )", 
				"element()#(n1)$n1 & ( ( element()#(n2)$n2 & $n1/child[d]::$n2 ) | ( element()#(n3)$n3 & $n1/child[d]::$n3 ) )");
	}
	
	@Ignore // We're getting rid of that mapping anyway
	public void root() {
		mapper.translate("tok & #1:root");
	}
	
	@Ignore // We're getting rid of that mapping anyway
	public void tokenArity() {
		mapper.translate("tok & #1:tokenarity=1");
	}
	
	@Ignore // We're getting rid of that mapping anyway
	public void arity() {
		mapper.translate("tok & #1:arity=1");
	}
	
	@Test(expected=AnnisMappingException.class)
	public void sameAnnotationGroup() {
		mapper.translate("tok & tok & #1 @ #2");
	}
	
	///// meta data
	
	@Test
	public void metadataEqual() {
		testMapping("meta::namespace:name=/value/", "meta(namespace:name = r\"value\")");
	}

  @Test
	public void metadataUnequal() {
    testMapping("meta::namespace:name!=/value/", "meta(namespace:name != r\"value\")");
	}

	
	///// Helper
	
	private void testMapping(String annisQuery, String expected) {
		String actual = mapper.translate(annisQuery);
		assertEquals(expected, actual);
	}
	
	private void testBinaryLingOp(String lingopExample, String expected) {
		testMapping("node & node & " + lingopExample, "element()#(n1)$n1 & element()#(n2)$n2 & " + expected);
	}
	
}
