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

import static annis.test.TestUtils.size;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import annis.service.objects.AnnisAttribute;

public class TestListAnnotationsSqlHelper {

	// object under test
	private ListAnnotationsSqlHelper listNodeAnnotationsSqlHelper;
	
	// dummy annotation data
	private static final String NULL = null;
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	private static final String NAME3 = "NAME3";
	private static final String VALUE1 = "VALUE1";
	private static final String VALUE2 = "VALUE2";
	private static final String VALUE3 = "VALUE3";

	@Before
	public void setup() {
		listNodeAnnotationsSqlHelper = new ListAnnotationsSqlHelper();
	}

  public void createSqlQueryNoEmptyCorpusList()
  {
    try
    {
      listNodeAnnotationsSqlHelper.createSqlQuery(null, true, true);
    }
    catch(IllegalArgumentException ex)
    {
      return;
    }
    fail("should throw illegal argument exception on empty corpus list");
  }

	@SuppressWarnings("unchecked")
	@Test
	public void extractDataNoValues() throws SQLException {
		// stub a result set with 2 annotations with NULL value set
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("name")).thenReturn(NAME1, NAME2);
		when(resultSet.getString("value")).thenReturn(NULL);
		
		// expected
		AnnisAttribute attribute1 = newNamedAnnisAttribute(NAME1);
		AnnisAttribute attribute2 = newNamedAnnisAttribute(NAME2);
		
		// call and test
		List<AnnisAttribute> annotations = (List<AnnisAttribute>) listNodeAnnotationsSqlHelper.extractData(resultSet);
		assertThat(annotations, size(2));
		assertThat(annotations, hasItems(attribute1, attribute2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void extractDataWithValues() throws SQLException {
		// stub a result set with 5 rows
		// row 1 - 3: annotation with 3 values
		// row 4: annotation with 1 value
		// row 5: annotation with no values
		ResultSet resultSet = mock(ResultSet.class);
		when(resultSet.next()).thenReturn(true, true, true, true, true, false);
		// row 1 - 3: annotation with 3 values
		when(resultSet.getString("name")).thenReturn(NAME1, NAME1, NAME1, NAME2, NAME3);
		when(resultSet.getString("value")).thenReturn(VALUE1, VALUE2, VALUE3, VALUE1, NULL);
		
		// expected
		AnnisAttribute attribute1 = newNamedAnnisAttribute(NAME1, VALUE1, VALUE2, VALUE3);
		AnnisAttribute attribute2 = newNamedAnnisAttribute(NAME2, VALUE1);
		AnnisAttribute attribute3 = newNamedAnnisAttribute(NAME3);
		
		// call and test
		List<AnnisAttribute> annotations = (List<AnnisAttribute>) listNodeAnnotationsSqlHelper.extractData(resultSet);
		assertThat(annotations, size(3));
		assertThat(annotations, hasItems(attribute1, attribute2, attribute3));
	}

	///// private helper
	
	private AnnisAttribute newNamedAnnisAttribute(String name, String... values) {
		AnnisAttribute attribute = new AnnisAttribute();
		
		attribute.setName(name);
		for (String value : values)
			attribute.addValue(value);
		
		return attribute;
	}
	

}
