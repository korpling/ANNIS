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
package annis;

import annis.dao.AnnisDao;
import annis.dao.AnnotatedMatch;
import annis.dao.Match;
import annis.dao.MatrixExtractor;
import annis.dao.MetaDataFilter;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.ql.parser.QueryAnalysis;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryData;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisCorpus;
import annis.service.objects.AnnisAttributeSetImpl;
import annis.sqlgen.AnnotateSqlGenerator;
import annis.sqlgen.SqlGenerator;
import annis.sqlgen.AnnotateSqlGenerator.AnnotateQueryData;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

	// dependencies
  private AnnisDao annisDao;
  private AnnisParser annisParser;
  // map Annis queries to DDDquery
  private DddQueryMapper dddQueryMapper;
  private QueryAnalysis aqlAnalysis;
  private SqlGenerator findSqlGenerator;
  private MetaDataFilter metaDataFilter;
  private int context;
  private int matchLimit;
  private boolean isDDDQueryMode;
  private QueryAnalysis queryAnalysis;

	// settings
	private int limit;
	private int offset;
	private int left;
	private int right;
	private List<Long> corpusList;
  
	// benchmarking
	private static class Benchmark {
		private String functionCall;
		private QueryData queryData;
		private float avgTimeInMilliseconds;
		private long bestTimeInMilliseconds;
		private String sql;
		private String plan;
		private int runs;
		private int errors;
		
		public Benchmark(String functionCall, QueryData queryData) {
			super();
			this.functionCall = functionCall;
			this.queryData = queryData;
		}
		
	}
	private List<Benchmark> benchmarks;
  
  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("annisRunner", "annis/AnnisRunner-context.xml").run(args);
  }

  public AnnisRunner()
  {
    corpusList = new LinkedList<Long>();
	benchmarks = new ArrayList<Benchmark>();
    isDDDQueryMode = false;
  }

  // switch between AQL as input mode and DDDQuery
  public void doLanguage(String newLanguage)
  {
    if("ddd".equalsIgnoreCase(newLanguage) || "dddquery".equalsIgnoreCase(newLanguage))
    {
      isDDDQueryMode = true;
      System.out.println("new input language is DDDQuery");
    }
    else
    {
      isDDDQueryMode = false;
      System.out.println("new input language is AQL");
    }
  }

  ///// Commands
  public void doDebug(String ignore)
  {
    QueryData qdAQL = annisDao.parseAQL("node & node & node & #1 > #2 & #1 > #3", null);
    System.out.println(qdAQL);
  }

  public void doProposedIndex(String ignore)
  {
    File fInput = new File("queries.txt");

    Map<String, List<String>> output = new HashMap<String, List<String>>();

    if(fInput.exists())
    {
      try
      {
        String[] content = FileUtils.readFileToString(fInput).split("\n");

        for(String query : content)
        {
          if(query.trim().length() > 0)
          {
            Map<String, Set<String>> map = proposedIndexHelper(query.trim());
            for (Map.Entry<String, Set<String>> t : map.entrySet())
            {
              String table = t.getKey();
              Set<String> l = t.getValue();

              if (!output.containsKey(table))
              {
                output.put(table, new LinkedList<String>());
              }

              if (l.size() > 0)
              {
                output.get(table).add(StringUtils.join(l, ","));
              }
              out.println(query + "/" + table + ": " + l);
            }
          }
        }
        
        for (Entry<String,List<String>> entry: output.entrySet())
        {
          File fOutput = new File(entry.getKey() + "_attributes.csv");
          FileUtils.writeLines(fOutput, entry.getValue());
        }

      }
      catch(IOException ex)
      {
        Logger.getLogger(AnnisRunner.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
    else
    {
      out.println("Could not find queries.txt");
    }
  }

  public void doDddquery(String annisQuery)
  {
    out.println(translate(annisQuery));
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
  }

  public void doSql(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    out.println(sql);
  }

  public void doSqlGraph(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    out.println("CREATE OR REPLACE TEMPORARY VIEW matched_nodes AS " + sql + ";");

    AnnotateSqlGenerator ge = new AnnotateSqlGenerator();
    ge.setMatchedNodesViewName("matched_nodes");
    out.println(ge.getContextQuery(corpusList, context, context, matchLimit, 0, queryData.getMaxWidth(),
      new HashMap<Long, Properties>())
      + ";");
  }
  
  public void doSqlMatrix(String annisQuery)
  {
    // sql query
    QueryData queryData = parse(annisQuery);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    out.println("CREATE OR REPLACE TEMPORARY VIEW matched_nodes AS " + sql + ";");

    MatrixExtractor me = new MatrixExtractor();
    me.setMatchedNodesViewName("matched_nodes");
    out.println(me.getMatrixQuery(corpusList, queryData.getMaxWidth())
      + ";");
  }

  public void doMatrix(String annisQuery)
  {
    List<AnnotatedMatch> matches = annisDao.matrix(getCorpusList(), parse(annisQuery));
    if(matches.isEmpty())
    {
      out.println("(empty");
    }
    else
    {
      WekaHelper helper = new WekaHelper();
      out.println(helper.exportAsArff(matches));
    }
  }

	private QueryData analyzeQuery(String annisQuery, String queryFunction) {
		QueryData queryData = annisDao.parseAQL(annisQuery, corpusList);
		queryData.addExtension(new AnnotateQueryData(offset, limit, left, right));
		if (annisQuery != null)
			benchmarks.add(new Benchmark(queryFunction + " " + annisQuery, queryData));
		out.println("NOTICE: corpus = " + queryData.getCorpusList());
		return queryData;
	}
	
  public void doCount(String annisQuery)
  {
	  out.println(annisDao.count(analyzeQuery(annisQuery, "count")));
  }
  
	public void doFind(String annisQuery) {
		List<Match> matches = annisDao.find(analyzeQuery(annisQuery, "find"));
		printAsTable(matches);
	}
  

  public void doPlanCount(String annisQuery)
  {
    out.println(annisDao.planCount(parse(annisQuery), getCorpusList(), false));
  }

  public void doAnalyzeCount(String annisQuery)
  {
    out.println(annisDao.planCount(parse(annisQuery), getCorpusList(), true));
  }

  public void doPlanGraph(String annisQuery)
  {
    out.println(annisDao.planGraph(parse(annisQuery), getCorpusList(),
      0, matchLimit, context, context, false));
  }

  public void doAnalyzeGraph(String annisQuery)
  {
    out.println(annisDao.planGraph(parse(annisQuery), getCorpusList(),
      0, matchLimit, context, context, true));
  }

  public void doAnnotate2(String annisQuery) {
	    List<AnnotationGraph> graphs = annisDao.retrieveAnnotationGraph(getCorpusList(),
	    	      parse(annisQuery), 0, matchLimit, context, context);
	    printAsTable(graphs, "nodes", "edges");
  }
  
  public void doAnnotate(String annisQuery)
  {
		QueryData queryData = analyzeQuery(annisQuery, "annotate");
		out.println("NOTICE: left = " + left + "; right = " + right + "; limit = " + limit + "; offset = " + offset);
		List<AnnotationGraph> graphs = annisDao.annotate(queryData);
		// FIXME: annotations graphen visualisieren
    printAsTable(graphs, "nodes", "edges");
  }

  public void doCorpus(String list)
  {
    corpusList = new LinkedList<Long>();
    String[] splits = StringUtils.split(list, " ");
    for(String split : splits)
    {
      try
      {
        corpusList.add(Long.parseLong(split));
      }
      catch(NumberFormatException e)
      {
        // check if there is a corpus with this name
        LinkedList<String> splitList = new LinkedList<String>();
        splitList.add(split);
        corpusList.addAll(annisDao.listCorpusByName(splitList));
      }
    }

    if(corpusList.isEmpty())
    {
      setPrompt("no corpus>");
    }
    else
    {
      setPrompt(StringUtils.join(corpusList, ",") + ">");
    }
  }

  public void doList(String unused)
  {
    List<AnnisCorpus> corpora = annisDao.listCorpora();
    printAsTable(corpora, "id", "name", "textCount", "tokenCount");
  }

  public void doAnnotations(String doListValues)
  {
    boolean listValues = "values".equals(doListValues);
    List<AnnisAttribute> annotations =
      annisDao.listAnnotations(getCorpusList(), listValues, true);
    AnnisAttributeSetImpl set = new AnnisAttributeSetImpl(annotations);
    System.out.println(set.getJSON());
  }

  public void doMeta(String corpusId)
  {
    List<Annotation> corpusAnnotations = annisDao.listCorpusAnnotations(Long.parseLong(corpusId));
    printAsTable(corpusAnnotations, "namespace", "name", "value");
  }

  public void doText(String textID)
  {
    List<AnnotationGraph> result = new LinkedList<AnnotationGraph>();
    AnnotationGraph graph = annisDao.retrieveAnnotationGraph(Long.parseLong(textID));
    result.add(graph);
    printAsTable(result, "nodes", "edges");
  }

  public void doQuit(String dummy)
  {
    System.out.println("bye bye!");
    System.exit(0);
  }

  public void doCompareParser(String query)
  {
    QueryData qdAQL = annisDao.parseAQL(query, null);
    QueryData qdDDD = annisDao.parseDDDQuery(translate(query), null);

    String strAQL = qdAQL.toString();
    String strDDD = qdDDD.toString();

    if(strAQL.equals(strDDD))
    {
      System.out.println(strAQL);
      System.out.println("both are equal");
    }
    else
    {
      System.out.println("AQL:");
      System.out.println(strAQL);
      System.out.println("DDD:");
      System.out.println(strDDD);
      System.out.println("NOT EQUAL");
    }
  }

  ///// Delegates for convenience
  private String translate(String annisQuery)
  {
    return dddQueryMapper.translate(annisQuery);
  }

  public Map<String, Set<String>> proposedIndexHelper(String aql)
  {
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    result.put("facts", new TreeSet<String>());
    result.put("node", new TreeSet<String>());
    result.put("node_annotation", new TreeSet<String>());

    // sql query
    QueryData queryData = parse(aql);
    queryData.setCorpusList(corpusList);
    queryData.setDocuments(metaDataFilter.getDocumentsForMetadata(queryData));

    String sql = findSqlGenerator.toSql(queryData);

    // extract WHERE clause

    Matcher mWhere = Pattern.compile("WHERE\n").matcher(sql);
    if(mWhere.find())
    {
      String whereClause = sql.substring(mWhere.end());
      //out.println("WHERE clause:\n" + whereClause);

      for(String table : result.keySet())
      {
        Set<String> attr = result.get(table);
        Matcher mFacts = Pattern.compile(table + "[0-9]+\\.([a-zA-Z0-9_]+)").matcher(whereClause);
        while(mFacts.find())
        {
          attr.add(mFacts.group(1).trim());
        }
      }

      // print result
      //out.println("facts: " + StringUtils.join(factsAttributes, ", "));
      //out.println("node: " + StringUtils.join(nodeAttributes, ", "));
      //out.println("suggested index: ");
      //out.println("CREATE INDEX idx__facts__noname ON facts (" + StringUtils.join(factsAttributes, ", ") + ");");
      //out.println("CREATE INDEX idx__node__noname ON node (" + StringUtils.join(nodeAttributes, ", ") + ");");
    }
    else
    {
      out.println("Could not find the WHERE clause");
    }
    return result;
  }

  private void printAsTable(List<? extends Object> list, String... fields)
  {
    out.println(new TableFormatter().formatAsTable(list, fields));
  }

  private QueryData parse(String input)
  {
    if(isDDDQueryMode)
    {
      return annisDao.parseDDDQuery(input, getCorpusList());
    }
    else
    {
      return annisDao.parseAQL(input, getCorpusList());
    }
  }

  ///// Getter / Setter
  public DddQueryMapper getDddQueryMapper()
  {
    return dddQueryMapper;
  }

  public void setDddQueryMapper(DddQueryMapper dddQueryMapper)
  {
    this.dddQueryMapper = dddQueryMapper;
  }

  public AnnisParser getAnnisParser()
  {
    return annisParser;
  }

  public void setAnnisParser(AnnisParser annisParser)
  {
    this.annisParser = annisParser;
  }

  public AnnisDao getAnnisDao()
  {
    return annisDao;
  }

  public void setAnnisDao(AnnisDao annisDao)
  {
    this.annisDao = annisDao;
  }

  public QueryAnalysis getAqlAnalysis()
  {
    return aqlAnalysis;
  }

  public void setAqlAnalysis(QueryAnalysis aqlAnalysis)
  {
    this.aqlAnalysis = aqlAnalysis;
  }

  public SqlGenerator getFindSqlGenerator()
  {
    return findSqlGenerator;
  }

  public void setFindSqlGenerator(SqlGenerator findSqlGenerator)
  {
    this.findSqlGenerator = findSqlGenerator;
  }

  public List<Long> getCorpusList()
  {
    return corpusList;
  }

  public void setCorpusList(List<Long> corpusList)
  {
    this.corpusList = corpusList;
  }

  public MetaDataFilter getMetaDataFilter()
  {
    return metaDataFilter;
  }

  public void setMetaDataFilter(MetaDataFilter metaDataFilter)
  {
    this.metaDataFilter = metaDataFilter;
  }

  public int getContext()
  {
    return context;
  }

  public void setContext(int context)
  {
    this.context = context;
  }

  public int getMatchLimit()
  {
    return matchLimit;
  }

  public void setMatchLimit(int matchLimit)
  {
    this.matchLimit = matchLimit;
  }

public QueryAnalysis getQueryAnalysis() {
	return queryAnalysis;
}

public void setQueryAnalysis(QueryAnalysis queryAnalysis) {
	this.queryAnalysis = queryAnalysis;
}
}
