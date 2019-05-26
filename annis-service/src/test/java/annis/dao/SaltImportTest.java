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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.CorpusStorageManager.ResultOrder;
import org.corpus_tools.graphannis.GraphUpdate;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

/**
 *
 * @author thomas
 */
public class SaltImportTest {
    private CorpusStorageManager storage;

    public SaltImportTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws GraphANNISException {
        File tmpDir = Files.createTempDir();

        storage = new CorpusStorageManager(tmpDir.getAbsolutePath());
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of map method, of class SaltImport.
     */
    @Test
    public void testMapComplexExample() throws GraphANNISException {

        SDocument doc = SaltFactory.createSDocument();

        SampleGenerator.createTokens(doc);
        SampleGenerator.createMorphologyAnnotations(doc);
        SampleGenerator.createInformationStructureSpan(doc);
        SampleGenerator.createInformationStructureAnnotations(doc);
        SampleGenerator.createSyntaxStructure(doc);
        SampleGenerator.createSyntaxAnnotations(doc);
        SampleGenerator.createAnaphoricAnnotations(doc);
        SampleGenerator.createDependencies(doc);

        GraphUpdate result = new SaltImport().map(doc.getDocumentGraph()).finish();

        storage.applyUpdate("testCorpus", result);

        List<String> corpus = Arrays.asList("testCorpus");

        assertEquals(26, storage.count(corpus, "node"));

        // test that the token are present and have the correct span values
        assertEquals(11, storage.count(corpus, "tok"));
        assertEquals(1, storage.count(corpus, "tok=\"Is\""));
        assertEquals(1, storage.count(corpus, "tok=\"this\""));
        assertEquals(1, storage.count(corpus, "tok=\"example\""));
        assertEquals(1, storage.count(corpus, "tok=\"more\""));
        assertEquals(1, storage.count(corpus, "tok=\"complicated\""));
        assertEquals(1, storage.count(corpus, "tok=\"than\""));
        assertEquals(1, storage.count(corpus, "tok=\"it\""));
        assertEquals(1, storage.count(corpus, "tok=\"appears\""));
        assertEquals(1, storage.count(corpus, "tok=\"to\""));
        assertEquals(1, storage.count(corpus, "tok=\"be\""));
        assertEquals(1, storage.count(corpus, "tok=\"?\""));

        // test that the token annotations have been added
        assertEquals(1, storage.count(corpus, "pos=\"VBZ\" _=_ \"Is\""));
        assertEquals(1, storage.count(corpus, "pos=\"DT\" _=_ \"this\""));
        assertEquals(1, storage.count(corpus, "pos=\"NN\" _=_ \"example\""));
        assertEquals(1, storage.count(corpus, "pos=\"RBR\" _=_ \"more\""));
        assertEquals(1, storage.count(corpus, "pos=\"JJ\" _=_ \"complicated\""));
        assertEquals(1, storage.count(corpus, "pos=\"IN\" _=_ \"than\""));
        assertEquals(1, storage.count(corpus, "pos=\"PRP\" _=_ \"it\""));
        assertEquals(1, storage.count(corpus, "pos=\"VBZ\" _=_ \"appears\""));
        assertEquals(1, storage.count(corpus, "pos=\"TO\" _=_ \"to\""));
        assertEquals(1, storage.count(corpus, "pos=\"VB\" _=_ \"be\""));
        assertEquals(1, storage.count(corpus, "pos=\".\" _=_ \"?\""));

        // test that the precedence works for the token
        assertEquals(1, storage.count(corpus, 
                "\"Is\" . \"this\" . \"example\" . \"more\" . \"complicated\" . \"than\" . \"it\" . \"appears\" . "
                        + "\"to\" . \"be\" . \"?\""));

        // test that coverage works
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"contrast-focus\" _o_ \"Is\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"this\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"example\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"more\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"complicated\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"than\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"it\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"appears\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"to\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"be\""));
        assertEquals(1, storage.count(corpus, "Inf-Struct=\"topic\" _o_ \"?\""));

        // test some of the dominance edges
        assertEquals(1, storage.count(corpus, "const=\"ROOT\" > const=\"SQ\" > \"Is\""));
        assertEquals(1, storage.count(corpus, "const=\"SQ\" >* \"this\""));

        // test some of the pointing relations
        assertEquals(1, storage.count(corpus, "\"it\" ->anaphoric node _o_ \"example\""));
        assertEquals(9, storage.count(corpus, "tok ->null tok"));
        assertEquals(1, storage.count(corpus, "\"complicated\" ->null[dependency=\"cop\"] \"Is\""));
    }

    @Test
    public void testTwoDocumentsSameNodeName() throws GraphANNISException {

        SaltProject project = SaltFactory.createSaltProject();
        SCorpusGraph corpusGraph = project.createCorpusGraph();

        SCorpus root = corpusGraph.createCorpus(null, "root");

        // add two documents which have a token with the same name
        SDocument doc1 = corpusGraph.createDocument(root, "doc1");
        doc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
        STextualDS text1 = doc1.getDocumentGraph().createTextualDS("abc");
        SToken tok1 = SaltFactory.createSToken();
        tok1.setName("MyToken");
        doc1.getDocumentGraph().addNode(tok1);

        STextualRelation textRel1 = SaltFactory.createSTextualRelation();
        textRel1.setSource(tok1);
        textRel1.setTarget(text1);
        textRel1.setStart(0);
        textRel1.setEnd(2);
        doc1.getDocumentGraph().addRelation(textRel1);

        SDocument doc2 = corpusGraph.createDocument(root, "doc2");
        doc2.setDocumentGraph(SaltFactory.createSDocumentGraph());
        STextualDS text2 = doc2.getDocumentGraph().createTextualDS("abc");
        SToken tok2 = SaltFactory.createSToken();
        tok2.setName("MyToken");
        doc2.getDocumentGraph().addNode(tok2);

        STextualRelation textRel2 = SaltFactory.createSTextualRelation();
        textRel2.setSource(tok2);
        textRel2.setTarget(text2);
        textRel2.setStart(0);
        textRel2.setEnd(2);
        doc2.getDocumentGraph().addRelation(textRel2);

        doc2.getDocumentGraph().addNode(tok2);

        GraphUpdate result1 = new SaltImport().map(doc1.getDocumentGraph()).finish();
        storage.applyUpdate("root", result1);

        GraphUpdate result2 = new SaltImport().map(doc2.getDocumentGraph()).finish();
        storage.applyUpdate("root", result2);

        // test that both token have been added

        Set<String> matches = new HashSet<>();

        String[] result = storage.find(Arrays.asList("root"), "tok", 0, Long.MAX_VALUE,
                ResultOrder.Normal);
        assertEquals(2, result.length);
        for (int i = 0; i < 2; i++) {
            matches.add(result[i]);
        }
        assertEquals(2, matches.size());
        Assert.assertTrue(matches.contains("salt:/root/doc1#MyToken"));
        Assert.assertTrue(matches.contains("salt:/root/doc2#MyToken"));

    }

}
