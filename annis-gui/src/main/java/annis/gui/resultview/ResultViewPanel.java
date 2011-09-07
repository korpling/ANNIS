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

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.gui.ServiceHelper;
import annis.service.ifaces.AnnisResult;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.CompositeItem;
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

    setSizeFull();
    
    VerticalLayout layout = (VerticalLayout) getContent();
    layout.setMargin(false);
    layout.setSizeFull();
    
    
    lblInfo = new Label();
    lblInfo.setValue("Result for query \"" + aql.replaceAll("\n", " ") + "\"");

    tblResults = new Table();
    tblResults.setPageLength(pageSize);
    tblResults.setSizeFull();
    
    addComponent(lblInfo);    
    addComponent(tblResults);
    
    layout.setExpandRatio(tblResults, 1.0f);
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
    containerResult = new LazyQueryContainer(queryFactory, false, pageSize);
    
    tblResults.setContainerDataSource(containerResult);
    
    tblResults.addGeneratedColumn("panel", new Table.ColumnGenerator() {

      @Override
      public Component generateCell(Table source, Object itemId, Object columnId)
      {
        try
        {
          BeanItem<AnnisResult> item = (BeanItem<AnnisResult>) containerResult.getItem(itemId);
          return new SingleResultPanel(item.getBean());
        }    
        catch(AnnisQLSemanticsException ex)
        {
          lblInfo.setValue("Semantic error: " + ex.getLocalizedMessage());
          tblResults.setVisible(false);
          return new Label("");
        }
        catch(AnnisQLSyntaxException ex)
        {
          lblInfo.setValue("Syntax error: " + ex.getLocalizedMessage());
          tblResults.setVisible(false);
          return new Label("");
        }
        catch(AnnisCorpusAccessException ex)
        {
          lblInfo.setValue("Corpus access error: " + ex.getLocalizedMessage());
          tblResults.setVisible(false);
          return new Label("");
        }
        catch(Exception ex)
        {
          lblInfo.setValue("unknown exception: " + ex.getLocalizedMessage());
          tblResults.setVisible(false);
          return new Label("");
        }
      }
    });
    
    tblResults.setVisibleColumns(new String[] {"panel"});
    tblResults.setColumnHeader("panel", "");
    tblResults.setColumnExpandRatio("panel", 1.0f);
  }

  public void setCount(int count)
  {
    queryConfiguration.put("count", count);
    tblResults.setContainerDataSource(containerResult);

  }
}
