/*
 * Copyright 2016 Thomas Krause.
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
package annis.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.CorpusStorageManager.QueryLanguage;
import org.corpus_tools.graphannis.GraphUpdate;
import org.corpus_tools.graphannis.LogLevel;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.corpus_tools.salt.util.SaltUtil;
import org.corpus_tools.salt.util.internal.ValidationResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author thomas
 */
public class SaltExportTest {
    @BeforeClass
    public static void setUpClass() {}

    @AfterClass
    public static void tearDownClass() {}

    private CorpusStorageManager storage;

    public SaltExportTest() {}

    @Before
    public void setUp() throws GraphANNISException {
        File tmpDir = Files.createTempDir();

        File logfile = new File(tmpDir, "graphannis.log");
        System.out.println("logging to " + logfile.getAbsolutePath());
        storage = new CorpusStorageManager(tmpDir.getAbsolutePath(), logfile.getAbsolutePath(), LogLevel.Trace, false);
    }

    @After
    public void tearDown() {}

    @Test
    public void testMapComplexExample() throws IOException, XMLStreamException, GraphANNISException {

        SDocument doc = SaltFactory.createSDocument();

        SampleGenerator.createTokens(doc);
        SampleGenerator.createMorphologyAnnotations(doc);
        SampleGenerator.createInformationStructureSpan(doc);
        SampleGenerator.createInformationStructureAnnotations(doc);
        SampleGenerator.createSyntaxStructure(doc);
        SampleGenerator.createSyntaxAnnotations(doc);
        SampleGenerator.createAnaphoricAnnotations(doc);
        SampleGenerator.createDependencies(doc);

        assertEquals(27, doc.getDocumentGraph().getNodes().size());

        GraphUpdate result = new SaltImport().map(doc.getDocumentGraph()).finish();

        storage.applyUpdate("testCorpus", result);

        assertEquals(26, storage.count(Arrays.asList("testCorpus"), "node", QueryLanguage.AQL));

        SToken sampleTok = doc.getDocumentGraph().getTokens().get(2);

        // get a subgraph for the complete document
        SDocumentGraph exportedGraph = SaltExport
                .map(storage.subgraph("testCorpus", Arrays.asList(sampleTok.getId()), 100, 100, Optional.empty()));

        ValidationResult validResult = SaltUtil.validate(exportedGraph).andFindInvalidities();
        assertTrue("Invalid graph detected:\n" + validResult.toString(), validResult.isValid());

        assertEquals(doc.getDocumentGraph().getNodes().size(), exportedGraph.getNodes().size());
        assertEquals(doc.getDocumentGraph().getTokens().size(), exportedGraph.getTokens().size());

        List<SToken> sortedTokenOrig = doc.getDocumentGraph().getSortedTokenByText();
        List<SToken> sortedTokenSubgraph = exportedGraph.getSortedTokenByText();

        for (int i = 0; i < sortedTokenOrig.size(); i++) {
            assertEquals(doc.getDocumentGraph().getText(sortedTokenOrig.get(i)),
                    exportedGraph.getText(sortedTokenSubgraph.get(i)));
        }

        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SSPANNING_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SSPANNING_RELATION).size());
        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SPOINTING_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SPOINTING_RELATION).size());
        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SDOMINANCE_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SDOMINANCE_RELATION).size());

        int numOfOrderRels = exportedGraph.getRelations(SALT_TYPE.SORDER_RELATION).size();

        assertEquals(doc.getDocumentGraph().getRelations().size(),
                exportedGraph.getRelations().size() - numOfOrderRels);

        // TODO: actual diff
    }

    @Test
    public void testMapComplexExampleDoc() throws IOException, XMLStreamException, GraphANNISException {

        SCorpusGraph corpusGraph = SaltFactory.createSCorpusGraph();

        SCorpus topCorpus = corpusGraph.createCorpus(null, "testMapComplexExampleDoc");
        SCorpus subCorpus = corpusGraph.createCorpus(topCorpus, "subcorpus");
        SDocument doc = corpusGraph.createDocument(subCorpus, "TestDoc");

        SampleGenerator.createTokens(doc);
        SampleGenerator.createMorphologyAnnotations(doc);
        SampleGenerator.createInformationStructureSpan(doc);
        SampleGenerator.createInformationStructureAnnotations(doc);
        SampleGenerator.createSyntaxStructure(doc);
        SampleGenerator.createSyntaxAnnotations(doc);
        SampleGenerator.createAnaphoricAnnotations(doc);
        SampleGenerator.createDependencies(doc);

        assertEquals(27, doc.getDocumentGraph().getNodes().size());

        GraphUpdate result = new SaltImport().map(doc.getDocumentGraph()).finish();

        storage.applyUpdate("testCorpus", result);

        assertEquals(26, storage.count(Arrays.asList("testCorpus"), "node", QueryLanguage.AQL));

        // get a subgraph for the complete document
        SDocumentGraph exportedGraph = SaltExport.map(storage.subcorpusGraph("testCorpus", Arrays.asList(doc.getId())));

        assertNotNull(exportedGraph);

        ValidationResult validResult = SaltUtil.validate(exportedGraph).andFindInvalidities();
        assertTrue("Invalid graph detected:\n" + validResult.toString(), validResult.isValid());

        assertEquals(doc.getDocumentGraph().getNodes().size(), exportedGraph.getNodes().size());
        assertEquals(doc.getDocumentGraph().getTokens().size(), exportedGraph.getTokens().size());

        List<SToken> sortedTokenOrig = doc.getDocumentGraph().getSortedTokenByText();
        List<SToken> sortedTokenSubgraph = exportedGraph.getSortedTokenByText();

        for (int i = 0; i < sortedTokenOrig.size(); i++) {
            assertEquals(doc.getDocumentGraph().getText(sortedTokenOrig.get(i)),
                    exportedGraph.getText(sortedTokenSubgraph.get(i)));
        }

        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SSPANNING_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SSPANNING_RELATION).size());
        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SPOINTING_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SPOINTING_RELATION).size());
        assertEquals(doc.getDocumentGraph().getRelations(SALT_TYPE.SDOMINANCE_RELATION).size(),
                exportedGraph.getRelations(SALT_TYPE.SDOMINANCE_RELATION).size());

        int numOfOrderRels = exportedGraph.getRelations(SALT_TYPE.SORDER_RELATION).size();

        assertEquals(doc.getDocumentGraph().getRelations().size(),
                exportedGraph.getRelations().size() - numOfOrderRels);

        // TODO: actual diff
    }

}
