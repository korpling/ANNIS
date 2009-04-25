package de.corpling.peper.modules.relANNIS.tests.complexCases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import de.corpling.salt.saltFW.SaltFWFactory;
import de.corpling.salt.saltFW.SaltGraph;
import de.corpling.salt.saltFW.SaltProject;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.salt.SSTEREOTYPES;
import de.corpling.salt.model.saltCore.SElement;
import de.corpling.salt.model.saltCore.SStereotype;
import de.corpling.peper.modules.relANNIS.RAExporter;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;

public class MainTestCase extends TestCase
{
	private static final String MSG_ERR=	"Error("+MainTestCase.class+"): ";
	/**
	 * The fixture for this Exporter test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RAExporter fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(MainTestCase.class);
	}

	/**
	 * Constructs a new Exporter test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public MainTestCase(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Exporter test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(RAExporter fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Exporter test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RAExporter getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception 
	{
		setFixture(RelANNISFactory.eINSTANCE.createRAExporter());
		this.saltProject= SaltFWFactory.eINSTANCE.createSaltProject();
		this.exportOrder= new BasicEList<SElement>();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

	protected File referenceDir= null;
	protected File outputDir= null;
	public void testSettingOutputDir()
	{
		
	}
	
	protected SaltGraph saltGraph=null;
	protected SaltProject saltProject= null;
	protected SCorpus rootCorpus= null;
	
	protected boolean compareFiles(File file1, File file2) throws IOException
	{
		boolean retVal= false;
		
		if ((file1== null) || (file2== null))
			throw new NullPointerException("One of the files to compare are null.");
		
		String contentFile1= null;
		String contentFile2= null;
		BufferedReader brFile1= null;
		BufferedReader brFile2= null;
		try 
		{
			brFile1=  new BufferedReader(new FileReader(file1));
			String line= null;
			while (( line = brFile1.readLine()) != null)
			{
		          contentFile1= contentFile1+  line;
		    }
			brFile2=  new BufferedReader(new FileReader(file2));
			line= null;
			while (( line = brFile2.readLine()) != null)
			{
		          contentFile2= contentFile2+  line;
		    }
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally 
		{
			brFile1.close();
			brFile2.close();
		} 
		
		if (contentFile1== null)
		{
			if (contentFile2== null)
				retVal= true;
			else retVal= false;
		}	
		else if (contentFile1.equals(contentFile2))
			retVal= true;
		return(retVal);
	}
	
	/**
	 * stores all elements (corpora and documents) which shall be exported
	 */
	protected EList<SElement> exportOrder= null;
	
	/**
	 * tests exporting corpus structure before exporting document structure.
	 */
	public void testExportCorpusStructureFirst() throws Exception
	{
		this.createCorpusAndDocumentStructure();
		if (realTest)
		{
			//Graph exportieren
			this.getFixture().setOutputDir(outputDir);
	
			this.getFixture().setSaltProject(saltProject);
			for (SElement exportElement: exportOrder)
			{
				//CORPUS
				if (exportElement.getSStereotype().getName().equalsIgnoreCase(SSTEREOTYPES.SCORPUS.toString()))
					this.getFixture().export((SCorpus)exportElement);
				//Document
				else if (exportElement.getSStereotype().getName().equalsIgnoreCase(SSTEREOTYPES.SDOCUMENT.toString()))
					this.getFixture().export((SDocument)exportElement);
				else
					throw new NullPointerException(MSG_ERR + "Cannot export an element which is not of type corpus or document.");
			}
			//checking if output is the same
			//corpus.tab
			File corp_origFile=new File(outputDir.getAbsolutePath()+"/corpus.tab");
			File corp_createdFile= new File(referenceDir.getAbsolutePath() + "/corpus.tab");
			assertTrue(this.compareFiles(corp_origFile, corp_createdFile));
			//corpusAnnotation.tab
			File corpAnno_origFile=new File(outputDir.getAbsolutePath()+"/corpus_annotation.tab");
			File corpAnno_createdFile= new File(referenceDir.getAbsolutePath() + "/corpus_annotation.tab");
			assertTrue(this.compareFiles(corpAnno_origFile, corpAnno_createdFile));
			//text.tab
			File text_origFile=new File(outputDir.getAbsolutePath()+"/text.tab");
			File text_createdFile= new File(referenceDir.getAbsolutePath() + "/text.tab");
			assertTrue(this.compareFiles(text_origFile, text_createdFile));
			//node.tab
			File struct_origFile=new File(outputDir.getAbsolutePath()+"/node.tab");
			File struct_createdFile= new File(referenceDir.getAbsolutePath() + "/node.tab");
			assertTrue(this.compareFiles(struct_origFile, struct_createdFile));
			//node_annotation.tab
			File anno_origFile=new File(outputDir.getAbsolutePath()+"/node_annotation.tab");
			File anno_createdFile= new File(referenceDir.getAbsolutePath() + "/node_annotation.tab");
			assertTrue(this.compareFiles(anno_origFile, anno_createdFile));
			//rank
			File rank_origFile=new File(outputDir.getAbsolutePath()+"/rank.tab");
			File rank_createdFile= new File(referenceDir.getAbsolutePath() + "/rank.tab");
			assertTrue(this.compareFiles(rank_origFile, rank_createdFile));
			//rank_annotation
			File rankAnno_origFile=new File(outputDir.getAbsolutePath()+"/rank_annotation.tab");
			File rankAnno_createdFile= new File(referenceDir.getAbsolutePath() + "/rank_annotation.tab");
			assertTrue(this.compareFiles(rankAnno_origFile, rankAnno_createdFile));
			//component
			File rank_type_origFile=new File(outputDir.getAbsolutePath()+"/component.tab");
			File rank_type_createdFile= new File(referenceDir.getAbsolutePath() + "/component.tab");
			assertTrue(this.compareFiles(rank_type_origFile, rank_type_createdFile));		
		}
	}
	
	private void createCorpusAndDocumentStructure()
	{
		this.createCorpusStructure();
		this.createDocumentStructure();
	}
	
	private boolean realTest= true;
	/**
	 * 		corp1
	 * 		/
	 * doc1
	 */
	protected void createCorpusStructure()
	{
		this.realTest= false;
	}
	
	/**
	 * Content of doc1:
	 * 
	 * text1	text2
	 */
	protected void createDocumentStructure()
	{
		
	}
}
