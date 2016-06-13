/*
 * Copyright 2015 SFB 632.
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
package annis.ql.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

import annis.dao.QueryDao;
import annis.exceptions.AnnisQLSemanticsException;
import annis.model.AqlParseError;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.service.objects.AnnisAttribute;

/**
 *
 *
 *
 * @author Shuo Zhang ssz6@georgetown.edu
 * @author Amir Zeldes amir.zeldes@georgetown.edu
 *
 */
@Component
public class AnnotationExistenceValidator implements QueryDataTransformer
{

  private QueryDao queryDao;

  @Override
  public QueryData transform(QueryData data)
  {
    List<Long> corpusList = data.getCorpusList();

    if (queryDao != null && (corpusList != null) && !corpusList.isEmpty())
    {
      // get first corpus name
      //List<AnnisCorpus> mycorpora = queryDao.listCorpora();
      //String firstcorpusname =  mycorpora.get(0).getName();
      Set<String> result = new TreeSet<>();

      /*get a list of all annotations in a similar way that TigerQueryBuilder gets it through 
       QueryServiceImpl in queryDao.listAnnotations()*/
      List<AnnisAttribute> atts = queryDao.listAnnotations(corpusList, false,
        true);

      //among them, get only node annotations
      for (AnnisAttribute a : atts)
      {
        if (a.getType() == AnnisAttribute.Type.node)
        {
          List<String> splitted = Splitter.on(":").limit(2).omitEmptyStrings().trimResults().splitToList(a.getName());
          result.add(splitted.get(splitted.size()-1));
          //result is a set of strings of available annotations
        }
      }
      
      List<AqlParseError> errors = new LinkedList<>();

      for (List<QueryNode> alternative : data.getAlternatives())
      {
        for (QueryNode n : alternative)
        {
          Set<QueryAnnotation> m = n.getNodeAnnotations();
          // always get the first one
          if(!m.isEmpty())
          {
            //name is the node name string, ready to check if name is in the list of 
            //available names
            String name = m.iterator().next().getName();
            if (!result.contains(name))
            {
              errors.add(new AqlParseError(n,
                "\"" + name + "\""
                + " is not a valid annotation name in selected corpora "));
            }
          }
        }
      }
      if(!errors.isEmpty())
      {
        throw new AnnisQLSemanticsException("Invalid annotation names detected.", errors);
      }
    }
    return data;

  }

  public QueryDao getQueryDao()
  {

    return queryDao;

  }

  public void setQueryDao(QueryDao queryDao)
  {

    this.queryDao = queryDao;

  }
}
