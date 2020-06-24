/*
 * graphANNIS
 * Access the graphANNIS corpora and execute AQL queries with this service. 
 *
 * OpenAPI spec version: 0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.corpus_tools.annis;

import org.corpus_tools.ApiException;
import org.corpus_tools.annis.AnnotationComponentType;
import org.corpus_tools.annis.CountExtra;
import org.corpus_tools.annis.CountQuery;
import org.corpus_tools.annis.FindQuery;
import org.corpus_tools.annis.FrequencyQuery;
import org.corpus_tools.annis.FrequencyTable;
import org.corpus_tools.annis.QueryLanguage;
import org.junit.Test;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for SearchApi
 */
@Ignore
public class SearchApiTest {

    private final SearchApi api = new SearchApi();

    /**
     * Count the number of results for a query.
     *
     * 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void countTest() throws ApiException {
        CountQuery body = null;
        CountExtra response = api.count(body);

        // TODO: test validations
    }
    /**
     * Find results for a query and return the IDs of the matched nodes.
     *
     * 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void findTest() throws ApiException {
        FindQuery body = null;
        String response = api.find(body);

        // TODO: test validations
    }
    /**
     * Find results for a query and return the IDs of the matched nodes.
     *
     * 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void frequencyTest() throws ApiException {
        FrequencyQuery body = null;
        FrequencyTable response = api.frequency(body);

        // TODO: test validations
    }
    /**
     * Get a subgraph of the corpus format given a list of nodes and a context.
     *
     * This only includes the nodes that are the result of the given query and no context is created automatically. The annotation graph also includes all edges between the included nodes. 
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    public void subgraphForQueryTest() throws ApiException {
        String corpus = null;
        String query = null;
        QueryLanguage queryLanguage = null;
        AnnotationComponentType componentTypeFilter = null;
        String response = api.subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter);

        // TODO: test validations
    }
}
