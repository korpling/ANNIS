/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.resultview;

import annis.gui.ServiceHelper;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;

/**
 *
 * @author thomas
 */
public class ResultViewPanel extends Panel
{

  private Label lblInfo;
  private String aql;
  private Set<Long> corpora;
  private int contextLeft, contextRight, pageSize;
  private Table tblResults;
  private LazyQueryContainer containerResult;
  private Map<String,Object> queryConfiguration;
  private BeanQueryFactory<AnnisResultQuery> queryFactory;
  
  public ResultViewPanel(String aql, Set<Long> corpora, int contextLeft, int contextRight, int pageSize)
  {
    this.aql = aql;
    this.corpora = corpora;
    this.contextLeft = contextLeft;
    this.contextRight = contextRight;
    this.pageSize = pageSize;

    lblInfo = new Label();
    lblInfo.setValue("Result for query \"" + aql.replaceAll("\n", " ") + " is calculated.");

    tblResults = new Table();
    
    
    addComponent(lblInfo);    
    addComponent(tblResults);
  }

  @Override
  public void attach()
  {
    super.attach();

    queryConfiguration=new HashMap<String,Object>();
    queryConfiguration.put("service", ServiceHelper.getService(getApplication(), getWindow()));
    queryConfiguration.put("window", getWindow());
    queryConfiguration.put("aql", aql);
    queryConfiguration.put("corpora", new LinkedList<Long>(corpora));
    queryConfiguration.put("count", -1);
    queryConfiguration.put("pageSize", pageSize);
    queryConfiguration.put("contextLeft", contextLeft);
    queryConfiguration.put("contextRight", contextRight);
    
    queryFactory =
      new BeanQueryFactory<AnnisResultQuery>(AnnisResultQuery.class);
    queryFactory.setQueryConfiguration(queryConfiguration);
    containerResult = new LazyQueryContainer(queryFactory, true, pageSize);
    containerResult.addContainerProperty(LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX, 
      Integer.class, 0, true, false);
    
    tblResults.setContainerDataSource(containerResult);
    
    tblResults.setVisibleColumns(new String[] {LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX});
    tblResults.setPageLength(pageSize);
    
    
  }

  public void setCount(int count)
  {
    queryConfiguration.put("count", count);
    tblResults.setContainerDataSource(containerResult);

  }
}
