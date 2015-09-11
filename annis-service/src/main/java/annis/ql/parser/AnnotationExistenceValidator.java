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

import annis.exceptions.AnnisQLSemanticsException;

import annis.model.QueryNode;

import annis.dao.AnnisDao;
import annis.model.QueryAnnotation;
import java.util.List;
import java.util.Set;
import annis.service.objects.AnnisAttribute;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

  private final static Logger log = LoggerFactory.getLogger(
    AnnotationExistenceValidator.class);

  private AnnisDao annisDao;

  @Override
  public QueryData transform(QueryData data)
  {
    //shuo code
    List<Long> corpusList = data.getCorpusList();

    if ((corpusList != null) && !corpusList.isEmpty())
    {
      // get first corpus name
      //List<AnnisCorpus> mycorpora = annisDao.listCorpora();
      //String firstcorpusname =  mycorpora.get(0).getName();
      Set<String> result = new TreeSet<>();

      /*get a list of all annotations in a similar way that TigerQueryBuilder gets it through 
       QueryServiceImpl in annisDao.listAnnotations()*/
      List<AnnisAttribute> atts = annisDao.listAnnotations(corpusList, false,
        true);

      //among them, get only node annotations
      for (AnnisAttribute a : atts)
      {
        if (a.getType() == AnnisAttribute.Type.node)
        {
          result.add(a.getName().split(":")[1]);
          //result is a set of strings of available annotations
        }
      }

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
              throw new AnnisQLSemanticsException(
                "Node name " + name
                + " is not a valid annotation name in selected corpora ");
            }
          }

          /*TODO : augmentation: in order to be able to display errors for more than one 
           annotations, we can aggregate them in a Set <Boolean> vflag and then 
           throw the error outside of the loop if the set is not empty.
           */
        }
      }

    }
    return data;

  }

  public AnnisDao getAnnisDao()
  {

    return annisDao;

  }

  public void setAnnisDao(AnnisDao annisDao)
  {

    this.annisDao = annisDao;

  }
}
