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

import annis.GraphHelper;
import annis.dao.QueryDao;
import annis.examplequeries.ExampleQuery;
import annis.ql.parser.QueryData;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.sqlgen.extensions.AnnotateQueryData;
import annis.sqlgen.extensions.LimitOffsetQueryData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.corpus_tools.salt.common.SaltProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Controlls the generating of automatic generated queries.
 *
 * For creating automatic generated queries, you have to implement the
 * {@link QueryBuilder} interface and register the new class in the
 * CommonDAO.xml file.
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class QueriesGenerator
{

  private final Logger log = LoggerFactory.getLogger(QueriesGenerator.class);

  // for executing AQL queries
  private QueryDao queryDao;

  // only contains one element: the top level corpus id of the imported corpus
  private List<Long> corpusIds;

  // the name of the imported top level corpus
  private String corpusName;

  // defines which cols of the tmp table are selected
  private Map<String, String> tableInsertSelect;

  // a set of query builder, which generate the example queries.
  private Set<QueryBuilder> queryBuilder;

  // to execute some sql commands directly
  private JdbcTemplate jdbcTemplate;

  /**
   * All automatic generated queries must implement this interface.
   *
   */
  public interface QueryBuilder
  {

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

  /**
   * Deletes all example queries for a specific corpus.
   *
   * @param corpusId References the corpus of the example queries.
   */
  public void delExampleQueries(long corpusId)
  {
    log.info("delete example queries of {}", corpusId);
    jdbcTemplate.execute(
      "DELETE FROM example_queries WHERE corpus_ref = " + corpusId);
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
      jdbcTemplate.execute("TRUNCATE example_queries");
    }
    else
    {
      List<Long> ids = queryDao.mapCorpusNamesToIds(corpusNames);
      for (Long id : ids)
      {
        delExampleQueries(id);
      }
    }
  }

  /**
   * Iterates over all registered {@link QueryBuilder} and generate example
   * queries.
   *
   * @param corpusId Determines the corpus, for which the example queries are
   * generated for. It must be the final ANNIS id of the corpus.
   *
   * @param delete Deletes the already existing example queries in the database.
   */
  public void generateQueries(long corpusId, boolean delete)
  {
    if (delete)
    {
      delExampleQueries(corpusId);
    }

    generateQueries(corpusId);
  }

  /**
   * Iterates over all registered {@link QueryBuilder} and generate example
   * queries.
   *
   * @param corpusId Determines the corpus, for which the example queries are
   * generated for. It must be the final ANNIS id of the corpus.
   */
  public void generateQueries(long corpusId)
  {
    corpusIds = new ArrayList<>();
    corpusIds.add(corpusId);
    List<String> corpusNames = getQueryDao().mapCorpusIdsToNames(corpusIds);
    if(!corpusNames.isEmpty())
    {
      corpusName = corpusNames.get(0);

      if (queryBuilder != null)
      {
        for (QueryBuilder qB : queryBuilder)
        {
          generateQuery(qB);
        }
      }
    }
  }

  /**
   * Iterates over all registered {@link QueryBuilder} and generate example
   * queries.
   *
   * @param name Determines the corpus, for which the example queries are
   * generated for. It must be the final ANNIS id of the corpus.
   */
  public void generateQueries(String name, boolean delete)
  {
    List<String> names = new ArrayList<>();
    names.add(name);
    List<Long> ids = queryDao.mapCorpusNamesToIds(names);
    if (!ids.isEmpty())
    {
      generateQueries(ids.get(0), delete);
    }
    else
    {
      log.error("{} is unknown to the system", name);
    }
  }

  /**
   * Generates example queries for all imported corpora.
   *
   * @param overwrite Deletes already exisiting example queries.
   */
  public void generateQueries(Boolean overwrite)
  {
    List<AnnisCorpus> corpora = queryDao.listCorpora();
    for (AnnisCorpus annisCorpus : corpora)
    {
      generateQueries(annisCorpus.getId(), overwrite);
    }
  }

  private void generateQuery(QueryBuilder queryBuilder)
  {
    try
    {

      // retrieve the aql query for analyzing purposes
      String aql = queryBuilder.getAQL();

      // set some necessary extensions for generating complete sql
      QueryData queryData = getQueryDao().parseAQL(aql, this.corpusIds);
      queryData.addExtension(queryBuilder.getLimitOffsetQueryData());
      
      // retrieve the salt project to analyze
      List<Match> matches = getQueryDao().find(queryData);
      
      if(matches.isEmpty())
      {
        return;
      }
      
      QueryData matchQueryData = GraphHelper.createQueryData(new MatchGroup(matches), queryDao);
      matchQueryData.addExtension(queryBuilder.getAnnotateQueryData());
      
      SaltProject saltProject = getQueryDao().graph(matchQueryData);
      queryBuilder.analyzingQuery(saltProject);

      // set the corpus name
      ExampleQuery exampleQuery = queryBuilder.getExampleQuery();
      exampleQuery.setCorpusName(corpusName);

      // copy the example query to the database
      if (exampleQuery.getExampleQuery() != null
        && !"".equals(exampleQuery.getExampleQuery()))
      {
        if (getTableInsertSelect().containsKey("example_queries"))
        {
          
          Object[] values = new Object[]
          { 
            exampleQuery.getExampleQuery(),
            exampleQuery.getDescription(),
            exampleQuery.getType() == null ? "" : exampleQuery.getType(),
            exampleQuery.getNodes(),
            "{}",
            corpusIds.get(0)
          };
          int[] argTypes = new int[]
          {
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
            Types.VARCHAR, Types.INTEGER
          };

          getJdbcTemplate().update("INSERT INTO example_queries(" 
            + getTableInsertSelect().get("example_queries") 
            + ") VALUES(?, ?, ?, ?, ?::text[], ?)", values, argTypes);
          log.info("generated example query: {}", exampleQuery.getExampleQuery());
        }
      }
      else
      {
        log.warn("could not generating auto query with {}", queryBuilder.
          getClass().getName());
      }
    }
    catch(Exception ex)
    {
      log.warn("Cannot generate example query", ex);
    }
  }

  /**
   * Defines the table columns of the temporary example query table are copied
   * to the final table. The important table name for example queries is
   * "example_queries".
   *
   * @return Returns a map of table names to column names.
   */
  public Map<String, String> getTableInsertSelect()
  {
    return tableInsertSelect;
  }

  /**
   * Defines the table columns of the temporary example query table are copied
   * to the final table. This field is set by using spring beans.
   *
   * @param tableInsertSelect the tableInsertSelect to set
   */
  public void setTableInsertSelect(Map<String, String> tableInsertSelect)
  {
    this.tableInsertSelect = tableInsertSelect;
  }

  /**
   *
   * @return the jdbcTemplate
   */
  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }

  /**
   * @param jdbcTemplate the jdbcTemplate to set
   */
  public void setJdbcTemplate(
    JdbcTemplate jdbcTemplate)
  {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * @return the queryDao
   */
  public QueryDao getQueryDao()
  {
    return queryDao;
  }

  /**
   * @param queryDao the queryDao to set
   */
  public void setQueryDao(QueryDao queryDao)
  {
    this.queryDao = queryDao;
  }

  /**
   * @return the queryBuilder
   */
  public Set<QueryBuilder> getQueryBuilder()
  {
    return queryBuilder;
  }

  /**
   * @param queryBuilder the queryBuilder to set
   */
  public void setQueryBuilder(Set<QueryBuilder> queryBuilder)
  {
    this.queryBuilder = queryBuilder;
  }
}
