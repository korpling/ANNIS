package de.corpling.peper.modules.relANNIS.tests.singleCases;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import de.corpling.salt.saltFW.SaltFWFactory;
import de.corpling.salt.model.salt.SCorpDocRelation;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.salt.SDocumentGraph;
import de.corpling.salt.model.salt.SPointingRelation;
import de.corpling.salt.model.salt.STextualDataSource;
import de.corpling.salt.model.salt.STextualRelation;
import de.corpling.salt.model.salt.SToken;
import de.corpling.salt.model.saltCore.SAnnotation;

/**
 * This test tests storing in relANNIS format with salt.
 * tested case is just tokens with two types of pointing relations between them.
 * 
 * PR:
 * type1:
 * tok1-> tok2
 * tok2 -> tok3
 * tok2 -> tok4
 *  
 * type2:
 * tok3 -> tok4
 * tok4 -> tok5
 * 
 * tok1		tok2	tok3	tok4	tok5	
 *	|	 	 |		|		|		|		
 * Hello	this	is		a		sample.
 * 
 * @author Florian Zipser
 *
 */
public class TestToken_Case4 extends MainTestCase
{
	/**
	 * Constructs a new Exporter test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public TestToken_Case4(String name) {
		super(name);
		this.outputDir= new File("./outputDir/relANNISExporter");
		this.referenceDir= new File("./data/relANNISExporter/Token_Case4");
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
		String text= "Hello this is a sample.";
		sTextualDataSource.setSText(text);
		sDocGraph.addSElement(sTextualDataSource);
		Collection<SToken> tokenList= new Vector<SToken>();
		//Vector<String> tokens= new Vector<String>();
		String token= "";
		Long pos= 0l;
		int tokId= 1;
		//create token on text1
		//tok1	tok2	tok3	tok4	tok5	tok6	tok7	tok8	tok9		tok10
		// |	  |		|		|		|		|		|		|		|			|
		//Hello	this	is		a		sample	text	to		test	pointing	relations.
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
		/**
		 * 
		 * PR:
		 * type1:
		 * tok1-> tok2
		 * tok2 -> tok3
		 * tok2 -> tok4
		 *  
		 * type2:
		 * tok3 -> tok4
		 * tok4 -> tok5
		 * 
		 * tok1		tok2	tok3	tok4	tok5	
		 *	|	 	 |		|		|		|		
		 * Hello	this	is		a		sample.
		*/
		
		SPointingRelation pRel= null;
		String type= null;
		
		//type1
		type= "type1";
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType(type);
		pRel.setSSourceElement((SToken)sDocGraph.getSElementById("tok1"));
		pRel.setSDestinationElement((SToken)sDocGraph.getSElementById("tok2"));
		sDocGraph.addSRelation(pRel);
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType(type);
		pRel.setSSourceElement((SToken)sDocGraph.getSElementById("tok2"));
		pRel.setSDestinationElement((SToken)sDocGraph.getSElementById("tok3"));
		sDocGraph.addSRelation(pRel);
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType(type);
		pRel.setSSourceElement((SToken)sDocGraph.getSElementById("tok2"));
		pRel.setSDestinationElement((SToken)sDocGraph.getSElementById("tok4"));
		sDocGraph.addSRelation(pRel);
		
		//type2
		type= "type2";
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType(type);
		pRel.setSSourceElement((SToken)sDocGraph.getSElementById("tok3"));
		pRel.setSDestinationElement((SToken)sDocGraph.getSElementById("tok4"));
		sDocGraph.addSRelation(pRel);
		pRel= SaltFWFactory.eINSTANCE.createSPointingRelation();
		pRel.setSType(type);
		pRel.setSSourceElement((SToken)sDocGraph.getSElementById("tok4"));
		pRel.setSDestinationElement((SToken)sDocGraph.getSElementById("tok5"));
		sDocGraph.addSRelation(pRel);
			}
}
