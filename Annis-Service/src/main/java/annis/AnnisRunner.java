package annis;

import annis.model.AnnisNode;
import annis.ql.node.Start;
import annis.ql.parser.AQLAnalysis;
import annis.ql.parser.AnnisParser;
import annis.ql.parser.QueryData;
import de.deutschdiachrondigital.dddquery.DddQueryMapper;
import de.deutschdiachrondigital.dddquery.DddQueryRunner;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

// TODO: test AnnisRunner
public class AnnisRunner extends AnnisBaseRunner
{

//	private static Logger log = Logger.getLogger(AnnisRunner.class);
  // delegate most commands to DddQueryRunner
  private DddQueryRunner dddQueryRunner;
  // parser for Annis queries
  private AnnisParser annisParser;
  // map Annis queries to DDDquery
  private DddQueryMapper dddQueryMapper;

  private AQLAnalysis aqlAnalysis;

  public static void main(String[] args)
  {
    // get runner from Spring
    AnnisBaseRunner.getInstance("annisRunner", "annis/AnnisRunner-context.xml").run(args);
  }

  ///// Commands
  
  public void doDebug(String ignore)
  {
    doAqlParser("namespace:word=\"abc\" & tok & #1 >[secedge!=\"sdf\"] #2");
  }

  public void doProposedIndex(String ignore)
  {
    File fInput = new File("queries.txt");

    Map<String,List<String>> output = new HashMap<String, List<String>>();

    if(fInput.exists())
    {
      try
      {
        String[] content = FileUtils.readFileToString(fInput).split("\n");
        
        for(String query : content)
        {
          if(query.trim().length() > 0)
          {
            Map<String,Set<String>> map = dddQueryRunner.proposedIndexHelper(translate(query.trim()));
            for(String table : map.keySet())
            {
              if(!output.containsKey(table))
              {
                output.put(table, new LinkedList<String>());
              }
              Set<String> l = map.get(table);
              if(l.size() > 0)
              {
                output.get(table).add(StringUtils.join(l, ","));
              }
              out.println(query + "/" + table + ": " + map.get(table));
            }
          }
        }

        for(String table : output.keySet())
        {
          File fOutput = new File(table + "_attributes.csv");
          FileUtils.writeLines(fOutput, output.get(table));
        }

      }
      catch (IOException ex)
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

  public void doParseInternal(String annisQuery)
  {
    dddQueryRunner.doParse(translate(annisQuery));
  }

  public void doParse(String annisQuery)
  {
    out.println(annisParser.dumpTree(annisQuery));
  }

  public void doSql(String annisQuery)
  {
    dddQueryRunner.doSql(translate(annisQuery));
  }

  public void doSqlGraph(String annisQuery)
  {
    dddQueryRunner.doSqlGraph(translate(annisQuery));
  }

  public void doMatrix(String annisQuery)
  {
    dddQueryRunner.doMatrix(translate(annisQuery));
  }

  public void doCount(String annisQuery)
  {
    dddQueryRunner.doCount(translate(annisQuery));
  }

  public void doPlanCount(String annisQuery)
  {
    dddQueryRunner.doPlanCount(translate(annisQuery));
  }

  public void doAnalyzeCount(String annisQuery)
  {
    dddQueryRunner.doAnalyzeCount(translate(annisQuery));
  }

  public void doPlanGraph(String annisQuery)
  {
    dddQueryRunner.doPlanGraph(translate(annisQuery));
  }

  public void doAnalyzeGraph(String annisQuery)
  {
    dddQueryRunner.doAnalyzeGraph(translate(annisQuery));
  }

  public void doAnnotate(String annisQuery)
  {
    dddQueryRunner.doAnnotate(translate(annisQuery));
  }

  public void doCorpus(String corpusList)
  {
    dddQueryRunner.doCorpus(dddQueryMapper.translateCorpusList(corpusList));
    setPrompt(dddQueryRunner.getPrompt());
  }

  public void doList(String unused)
  {
    dddQueryRunner.doList(unused);
  }

  public void doNodeAnnotations(String doListValues)
  {
    dddQueryRunner.doNodeAnnotations(doListValues);
  }

  public void doMeta(String corpusId)
  {
    dddQueryRunner.doMeta(corpusId);
  }

  public void doQuit(String dummy)
  {
    System.out.println("bye bye!");
    System.exit(0);
  }

  public void doAqlParser(String query)
  {
    Start start = annisParser.parse(query);

    System.out.println("AQL graph (seen from parser):");
    System.out.println(annisParser.dumpTree(start));

    QueryData qd =  aqlAnalysis.analyzeQuery(start, dddQueryRunner.getCorpusList());

    System.out.println("AQL graph (seen from analyzer):");
    Iterator<List<AnnisNode>> itOr = qd.getAlternatives().iterator();
    while(itOr.hasNext())
    {
      List<AnnisNode> nextNodes = itOr.next();
      Iterator<AnnisNode> itAnd = nextNodes.iterator();
      while(itAnd.hasNext())
      {
        System.out.println("\t" + itAnd.next());
        if(itAnd.hasNext())
        {
          System.out.println("\tAND");
        }
      }

      if(itOr.hasNext())
      {
        System.out.println("OR");
      }
    }

  }

  ///// Delegates for convenience
  private String translate(String annisQuery)
  {
    return dddQueryMapper.translate(annisQuery);
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

  public DddQueryRunner getDddQueryRunner()
  {
    return dddQueryRunner;
  }

  public void setDddQueryRunner(DddQueryRunner dddQueryRunner)
  {
    this.dddQueryRunner = dddQueryRunner;
  }

  public AQLAnalysis getAqlAnalysis()
  {
    return aqlAnalysis;
  }

  public void setAqlAnalysis(AQLAnalysis aqlAnalysis)
  {
    this.aqlAnalysis = aqlAnalysis;
  }

  
}
