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
package annis.dao;

import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.service.objects.DocumentBrowserConfig;
import annis.sqlgen.ListCorpusSqlHelper;

public class TestQueryDaoImpl {


    // simple SpringDao instance with mocked dependencies
    private QueryDaoImpl queryDao;
    @Mock
    private ListCorpusSqlHelper listCorpusHelper;

    // constants for flow control verification
    private static final String SQL = "SQL";
    private static final List<Long> CORPUS_LIST = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws GraphANNISException {
        initMocks(this);
    }
    @Test
    public void getDefaultDocBrowserConfiguration() {
        DocumentBrowserConfig docBrowseConfig = queryDao.getDefaultDocBrowserConfiguration();

        Assert.assertNotNull("default document browser config may not be null", docBrowseConfig);
        Assert.assertNotNull(docBrowseConfig.getVisualizers());
        Assert.assertTrue(docBrowseConfig.getVisualizers().length > 0);
        Assert.assertTrue(docBrowseConfig.getVisualizers()[0].getType() != null);
        Assert.assertTrue(docBrowseConfig.getVisualizers()[0].getDisplayName() != null);
    }
}
