package de.corpling.peper.modules.relANNIS.tests.complexCases;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import de.corpling.peper.modules.relANNIS.tests.singleCases.MainTestCase;
import de.corpling.salt.saltFW.SaltFWFactory;
import de.corpling.salt.model.salt.SCorpDocRelation;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.salt.SDocumentGraph;
import de.corpling.salt.model.salt.SDominanceRelation;
import de.corpling.salt.model.salt.SPointingRelation;
import de.corpling.salt.model.salt.SSpanRelation;
import de.corpling.salt.model.salt.SStructure;
import de.corpling.salt.model.salt.STextualDataSource;
import de.corpling.salt.model.salt.STextualRelation;
import de.corpling.salt.model.salt.SToken;
import de.corpling.salt.model.saltCore.SAnnotation;

/**
 * Tested features:
 * -	Spans
 * -	Dominance nodes with two types
 * -	Pointing relations
 * Testing following graph:
 * 
 *			struct1				struct3		struct5
 *			/	|				|	 |		/		\
 *		/		struct2			|	 |	struct6	struct7
 *		|		|				|	 |		\	/		
 *		span1	|			struct4	 |		span2
 *		/	\	/	\			/	\|		|
 *	tok1	tok2	tok3	tok4	tok5	tok6	tok7
 * type1:	struct3 -->struct4; struct5-->struct6; struct6-->span2
 * type2:	struct3-->tok5;	struct5-->struct7; struct7-->span2	
 * 
 * pointing relations(anaphor):
 * struct1-->struct3, struct3-->struct6
 * @author Florian Zipser
 *
 */
public class TestComplex_Case1 extends MainTestCase
{
	/**
	 * Constructs a new Exporter test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public TestComplex_Case1(String name) {
		super(name);
		this.outputDir= new File("./outputDir/relANNISExporter/");
		this.referenceDir= new File("./data/relANNISExporter/Complex_Case1/");
	}
	
	protected void createCorpusStructure()
	{
		
		//creating corpora
		String[] corpNames= {"corp1"};
		SCorpus[] corpora= new SCorpus[corpNames.length];
		this.rootCorpus= null;
		int i= 0;
		for (String corpName: corpNames)
		{
			SCorpus sCorpus= null;
			sCorpus= SaltFWFactory.eINSTANCE.createSCorpus();
			sCorpus.setId(corpName);
			corpora[i]= sCorpus;
			if (i== 0)
				rootCorpus= sCorpus;
			i++;
		}
		
		//creating documents
		String[] docNames= {"doc1"};
		SDocument[] documents= new SDocument[docNames.length];
		i=0;
		for (String docName: docNames)
		{
			SDocument sDocument= null;
			sDocument= SaltFWFactory.eINSTANCE.createSDocument();
			sDocument.setId(docName);
			documents[i]= sDocument;
			i++;
		}
		
		//creating graph
		saltGraph= SaltFWFactory.eINSTANCE.createSaltGraph();
		saltGraph.setId("graphCase1");
		saltProject.getSaltGraphs().add(saltGraph);
		
		//=========================== filling the graph
		//---------corpora
		for (SCorpus sCorpus : corpora)
		{
			//System.out.println(sCorpus);
			saltGraph.addSElement(sCorpus);
			
		}
		
		//---------documents
			
		for (SDocument sDocument: documents)
		{
			saltGraph.addSElement(sDocument);
		}
		//creating relations
		SCorpDocRelation sCorpDocRelation= null;
		//corp1-> doc1
		sCorpDocRelation= SaltFWFactory.eINSTANCE.createSCorpDocRelation();
		sCorpDocRelation.setId("corp1->doc1");
		sCorpDocRelation.setSSourceElement(saltGraph.getSElementById(corpNames[0]));
		sCorpDocRelation.setSDestinationElement(saltGraph.getSElementById(docNames[0]));
		saltGraph.addSRelation(sCorpDocRelation);
		this.exportOrder.add(saltGraph.getSElementById("corp1"));
		this.exportOrder.add(saltGraph.getSElementById("doc1"));
	}
	
	/**
	 * Content of doc1:
	 * 
	 * text1	text2
	 */
	protected void createDocumentStructure()
	{
		SDocument sDocument= (SDocument)this.saltGraph.getSElementById("doc1");
		SDocumentGraph sDocGraph= sDocument.getSDocumentGraph();
		sDocGraph.setId("docGraph1");
		//sDocGraph.setSCoreProject(saltProject);
		//saltProject.getSGraphs().add(sDocGraph);
		//sDocGraph.setSCoreProject(this.saltProject);
		//sDocument.setSDocumentGraph(sDocGraph);
		
		STextualDataSource sTextualDataSource= SaltFWFactory.eINSTANCE.createSTextualDataSource();
		sTextualDataSource.setId("text1");
		String text= "Hello this is the first sample text.";
		sTextualDataSource.setSText(text);
		sDocGraph.addSElement(sTextualDataSource);
		Collection<SToken> tokenList= new Vector<SToken>();
		//Vector<String> tokens= new Vector<String>();
		String token= "";
		Long pos= 0l;
		int tokId= 1;
		//create token on text1
		//tok1	tok2	tok3	tok4	tok5	tok6	tok7
		// |	  |		|		|		|		|		|
		//Hello	this 	is 		the 	first 	sample 	text.
		for (Character ch: text.toCharArray())
		{
			if (ch.equals(' '))
			{
				//set salt graph
				SToken sToken= SaltFWFactory.eINSTANCE.createSToken();
				sToken.setId("tok"+tokId);
				sDocGraph.addSElement(sToken);
				STextualRelation sTextRel= SaltFWFactory.eINSTANCE.createSTextualRelation();
				sTextRel.setSTextualDataSource(sTextualDataSource);
				sTextRel.setSToken(sToken);
				sTextRel.setSLeftPos(pos - token.length());
				sTextRel.setSRightPos(pos);
				sDocGraph.addSRelation(sTextRel);
				tokenList.add(sToken);
				//set token array
				//tokens.add(token);
				token= "";
				tokId++;
			}
			else
			{
				token= token + ch;
			}
			pos++;
		}
		if ((token!= null) && (token!= " ")) 
		{	
			//set salt graph
			SToken sToken= SaltFWFactory.eINSTANCE.createSToken();
			sToken.setId("tok"+tokId);
			sDocGraph.addSElement(sToken);
			STextualRelation sTextRel= SaltFWFactory.eINSTANCE.createSTextualRelation();
			sTextRel.setSTextualDataSource(sTextualDataSource);
			sTextRel.setSToken(sToken);
			sTextRel.setSLeftPos(pos - token.length());
			sTextRel.setSRightPos(pos);
			sDocGraph.addSRelation(sTextRel);
			tokenList.add(sToken);
			//tokens.add(token);
		}
		
		//creating annotations on tokens
		int i= 0;
		SAnnotation sAnno= null;
		for (SToken sToken: tokenList)
		{
			i++;
			sAnno= SaltFWFactory.eINSTANCE.createSAnnotation();
			sAnno.setFullName("namespace1::annoName"+ i);
			sAnno.setValue("annoValue"+i);
			sToken.addSAnnotation(sAnno);
			i++;
			sAnno= SaltFWFactory.eINSTANCE.createSAnnotation();
			sAnno.setNamespace("namespace2");
			sAnno.setName("annoName"+ i);
			sAnno.setValue("annoValue"+i);
			sToken.addSAnnotation(sAnno);
			i++;
			sAnno= SaltFWFactory.eINSTANCE.createSAnnotation();
			sAnno.setFullName("annoName"+ i);
			sAnno.setValue("annoValue"+i);
			sToken.addSAnnotation(sAnno);
		}
		
		//creating spans on tokens
		// span1 		span2	
		// /	\  		|
		//tok1	tok2	tok6	
		SStructure span= null;
		SSpanRelation cRel= null;
		
		//span1 --> tok1; tok2
		span= SaltFWFactory.eINSTANCE.createSStructure();
		span.setId("span1");
		sDocGraph.addSElement(span);
		cRel= SaltFWFactory.eINSTANCE.createSSpanRelation();
		cRel.setSToken((SToken)sDocGraph.getSElementById("tok1"));
		cRel.setSStructure(span);
		sDocGraph.addSRelation(cRel);
		cRel= SaltFWFactory.eINSTANCE.createSSpanRelation();
		cRel.setSToken((SToken)sDocGraph.getSElementById("tok2"));
		cRel.setSStructure(span);
		sDocGraph.addSRelation(cRel);
		
		//span2 --> tok2; tok3;tok4
		span= SaltFWFactory.eINSTANCE.createSStructure();
		span.setId("span2");
		sDocGraph.addSElement(span);
		cRel= SaltFWFactory.eINSTANCE.createSSpanRelation();
		cRel.setSToken((SToken)sDocGraph.getSElementById("tok6"));
		cRel.setSStructure(span);
		sDocGraph.addSRelation(cRel);
		
		
		//creating tree (dominance) on spans and tokens
		//
		//			struct1					struct3	struct5
		//			/	|					|		/		\
		//		/		struct2				|	struct6	struct7
		//		|		|					|		\	/		
		//		span1	|				struct4		span2
		//		/	\	/	\			/	\		|
		//	tok1	tok2	tok3	tok4	tok5	tok6	tok7
		//type1:	struct3 -->struct4; struct5-->struct6; struct6-->span2
		//type2:	struct3-->tok5;	struct5-->struct7; struct7-->span2		
		SDominanceRelation dRel= null;
		SStructure struct= null;
		SAnnotation anno= null;
		String id= null;
		
		//struct2 --> tok2, tok3
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct2";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("tok2"));
		sDocGraph.addSRelation(dRel);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("tok3"));
		sDocGraph.addSRelation(dRel);
		
		//struct 1 --> span1 struct2
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct1";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("span1"));
		sDocGraph.addSRelation(dRel);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("struct2"));
		sDocGraph.addSRelation(dRel);
		
		//struct4 --> span4; tok5
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct4";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("tok4"));
		sDocGraph.addSRelation(dRel);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("tok5"));
		sDocGraph.addSRelation(dRel);
		
		//struct3 --> struct4; tok5
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct3";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type1");
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("struct4"));
		sDocGraph.addSRelation(dRel);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type2");
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("tok5"));
		sDocGraph.addSRelation(dRel);
		
		//struct6 --> span2
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct6";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type1");
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("span2"));
		sDocGraph.addSRelation(dRel);
		
		//struct7 --> span2
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct7";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type2");
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("span2"));
		sDocGraph.addSRelation(dRel);
		
		//struct5 --> struct6,struct7
		struct= SaltFWFactory.eINSTANCE.createSStructure();
		id= "struct5";
		struct.setId(id);
		anno= SaltFWFactory.eINSTANCE.createSAnnotation();
		anno.setName("name");
		anno.setValue(id);
		struct.addSAnnotation(anno);
		sDocGraph.addSElement(struct);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type1");
		dRel.setSSourceElement(struct);
		dRel.setSDestinationElement(sDocGraph.getSElementById("struct6"));
		sDocGraph.addSRelation(dRel);
		dRel= SaltFWFactory.eINSTANCE.createSDominanceRelation();
		dRel.setSType("type2");
		dRel.setSSourceElement(sDocGraph.getSElementById("struct5"));
		dRel.setSDestinationElement(sDocGraph.getSElementById("struct7"));
		sDocGraph.addSRelation(dRel);
		
		//adding pointing relations
		//struct1-->struct3
		//struct3-->struct6
		SPointingRelation pRel= null;
		
		//struct1-->struct3
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType("anaphor");
		pRel.setSSourceElement(sDocGraph.getSElementById("struct1"));
		pRel.setSDestinationElement(sDocGraph.getSElementById("struct3"));
		sDocGraph.addSRelation(pRel);
		
		//struct6-->struct6
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType("anaphor");
		pRel.setSSourceElement(sDocGraph.getSElementById("struct3"));
		pRel.setSDestinationElement(sDocGraph.getSElementById("struct6"));
		sDocGraph.addSRelation(pRel);
		
	}
}
