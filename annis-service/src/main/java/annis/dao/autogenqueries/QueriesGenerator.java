/*
 * Copyright 2013 SFB 632.
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
package annis.dao.autogenqueries;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annis.dao.DBProvider;
import annis.dao.QueryDao;
import annis.examplequeries.ExampleQuery;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;

/**
 * Controlls the generating of automatic generated queries.
 *
 * For creating automatic generated queries, you have to implement the
 * {@link QueryBuilder} interface and register the new class in the
 * CommonDAO.xml file.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class QueriesGenerator extends DBProvider {

  private final Logger log = LoggerFactory.getLogger(QueriesGenerator.class);

  // for executing AQL queries
  private final QueryDao queryDao;

  // the name of the imported top level corpus
  private String corpusName;

  // a set of query builder, which generate the example queries.
  private Set<QueryBuilder> queryBuilder;
  


  /**
   * All automatic generated queries must implement this interface.
   *
   */
  public interface QueryBuilder {

    /**
     * Getter for a trial query, which is not put into the database. This is
     * used for retrieving a {@link SaltProject}, which could be analyzed with
     * {@link #analyzingQuery(de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject)}.
     *
     * @return The final AQL query.
     */
    public String getAQL();

    /**
     * Specifies the size of the result set.
     *
     * @return Returns a {@link LimitOffsetQueryData}. May not be null. The
     * {@link AbstractAutoQuery} class provides a good default.
     */
    public LimitOffsetQueryData getLimitOffsetQueryData();

    /**
     * Specifies the left and right context of the query from {@link #getAQL()}.
     * Further you could set the segmentation layer.
     *
     * @return Returns a {@link AnnotateQueryData}. May not be null. The
     * {@link AbstractAutoQuery} class provides a good default.
     */
    public AnnotateQueryData getAnnotateQueryData();

    /**
     * Analyzes the resut of the {@link #getAQL()}.
     *
     * @param saltProject Is the result of the query, which is getting from
     * {@link #getAQL()}.
     */
    public void analyzingQuery(SaltProject saltProject);

    /**
     * Provides the final example query. The {@link AbstractAutoQuery} class
     * provides a good default.
     *
     * @return The final {@link ExampleQuery}, which is written to the database.
     */
    public ExampleQuery getExampleQuery();
  }
  
  public QueriesGenerator(QueryDao queryDao) {
      this.queryDao = queryDao;
  }
  
  public static QueriesGenerator create(QueryDao queryDao) {
      QueriesGenerator queriesGenerator = new QueriesGenerator(queryDao);
      Set<QueryBuilder> queryBuilders = new LinkedHashSet<>();
      queryBuilders.add(new AutoTokQuery());
      queryBuilders.add(new AutoSimpleRegexQuery());
      queriesGenerator.setQueryBuilder(queryBuilders);
      
      return queriesGenerator;
  }

  /**
   * Deletes all example queries for a specific corpus.
   *
   * @param corpus The corpus name of the example queries.
   */
  public void delExampleQueriesForCorpus(String corpus)
  {
    log.info("delete example queries of {}", corpus);
    try(Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
      getQueryRunner().update(conn, "DELETE FROM example_queries WHERE corpus=?", corpus);
    } catch(SQLException ex) {
      log.error("Could not delete example queries for corpus {}", corpus, ex);
    }
  }

  /**
   * Deletes all example queries for a given corpus list.
   *
   * @param corpusNames Determines the example queries to delete. If null the
   * example_querie tab is truncated.
   */
  public void delExampleQueries(List<String> corpusNames)
  {

    if (corpusNames == null || corpusNames.isEmpty())
    {
      log.info("delete all example queries");
      try(Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
        getQueryRunner().update(conn, "DELETE FROM example_queries");
      } catch(SQLException ex) {
        log.error("Could not delete example queries", ex);
      }
    }
    else
    {
      for (String name : corpusNames)
      {
        delExampleQueriesForCorpus(name);
      }
    }
  }

  /**
   * Iterates over all registered {@link QueryBuilder} and generate example
   * queries.
   *
   * @param corpus Determines the corpus, for which the example queries are
   * generated for.
   *
   * @param delete Deletes the already existing example queries in the database.
   */
  public void generateQueries(String corpus, boolean delete) {
    if (delete) {
      delExampleQueriesForCorpus(corpus);
    }

    generateQueries(corpus);
  }

  /**
   * Iterates over all registered {@link QueryBuilder} and generate example
   * queries.
   *
   * @param corpus Determines the corpus, for which the example queries are
   * generated for.
   */
  public void generateQueries(String corpus) {
    this.corpusName = corpus;
    if (this.corpusName != null && !this.corpusName.isEmpty()) {
      if (queryBuilder != null) {
        for (QueryBuilder qB : queryBuilder) {
          generateQuery(qB);
        }
      }
    }
  }

  /**
   * Generates example queries for all imported corpora.
   *
   * @param overwrite Deletes already exisiting example queries.
   */
  public void generateQueries(Boolean overwrite) {
    List<AnnisCorpus> corpora = queryDao.listCorpora();
    for (AnnisCorpus annisCorpus : corpora) {
      generateQueries(annisCorpus.getName(), overwrite);
    }
  }

  private void generateQuery(QueryBuilder queryBuilder) {
    try {

      // retrieve the aql query for analyzing purposes
      String aql = queryBuilder.getAQL();
      
      // set some necessary extensions for generating complete sql
      
      // retrieve the salt project to analyze
      List<Match> matches = getQueryDao().find(aql, Arrays.asList(this.corpusName), queryBuilder.getLimitOffsetQueryData());

      if (matches.isEmpty()) {
        return;
      }
      
      SaltProject saltProject = getQueryDao().graph(new MatchGroup(matches), queryBuilder.getAnnotateQueryData());
      queryBuilder.analyzingQuery(saltProject);

      // set the corpus name
      ExampleQuery exampleQuery = queryBuilder.getExampleQuery();
      exampleQuery.setCorpusName(corpusName);

      // copy the example query to the database
      if (exampleQuery.getExampleQuery() != null && !"".equals(exampleQuery.getExampleQuery())) {
        try(Connection conn = createConnection(DB.CORPUS_REGISTRY)) {
          getQueryRunner().update(conn, "INSERT INTO example_queries (example_query, description, corpus) VALUES(?,?,?)",
            exampleQuery.getExampleQuery(), exampleQuery.getDescription(), this.corpusName);

            log.info("generated example query: {}", exampleQuery.getExampleQuery());

        } catch(SQLException ex) {
          log.error("Could not add generated example query", ex);
        }
      
      } else {
        log.warn("could not generating auto query with {}", queryBuilder.getClass().getName());
      }
    } catch (Exception ex) {
      log.warn("Cannot generate example query", ex);
    }
  }
  /**
   * @return the queryDao
   */
  public QueryDao getQueryDao() {
    return queryDao;
  }


  /**
   * @return the queryBuilder
   */
  public Set<QueryBuilder> getQueryBuilder() {
    return queryBuilder;
  }

  /**
   * @param queryBuilder the queryBuilder to set
   */
  public void setQueryBuilder(Set<QueryBuilder> queryBuilder) {
    this.queryBuilder = queryBuilder;
  }
}
