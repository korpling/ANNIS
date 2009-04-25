/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import java.io.File;

import de.dataconnector.tupleconnector.ITupleWriter;
import de.dataconnector.tupleconnector.TupleWriter;

import de.corpling.peper.modules.relANNIS.DAOOBJECT;
import de.corpling.peper.modules.relANNIS.RACorpus;
import de.corpling.peper.modules.relANNIS.RADAO;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.UNIQUE_VALUES;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>RADAO</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 *   <li>{@link exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpus) <em>Write</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#beginTA() <em>Begin TA</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#commitTA(long) <em>Commit TA</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#addTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter) <em>Add Tuple Writer</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#reSetTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter) <em>Re Set Tuple Writer</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#getTupleWriter(exporter.relANNIS.DAOOBJECT) <em>Get Tuple Writer</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpusMetaAttribute) <em>Write</em>}</li>
 *   <li>{@link exporter.relANNIS.RADAO#getUniqueValue(exporter.relANNIS.UNIQUE_VALUES) <em>Get Unique Value</em>}</li>
 * </ul>
 * </p>
 * @generated
 */
public class RADAOTest extends TestCase {

	/**
	 * The fixture for this RADAO test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RADAO fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(RADAOTest.class);
	}

	/**
	 * Constructs a new RADAO test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RADAOTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this RADAO test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(RADAO fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this RADAO test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RADAO getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(RelANNISFactory.eINSTANCE.createRADAO());
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

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpus) <em>Write</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpus)
	 */
	public void testWrite__Long_RACorpus() 
	{
		try {
			this.getFixture().write(0l, RelANNISFactory.eINSTANCE.createRACorpus());
			fail("shall not write an object without ta init");
		} catch (Exception e) {}
		
		this.getFixture().setOutputDir(new File("outputDir/"));
		long taId= this.getFixture().beginTA();
		this.getFixture().write(taId, RelANNISFactory.eINSTANCE.createRACorpus());
		try {
			RACorpus raCorpus= null;
			this.getFixture().write(taId, raCorpus);
			fail("shall not write an empty object");
		} catch (Exception e) {}
		this.getFixture().commitTA(taId);
		
		
		try {
			this.getFixture().write(taId, RelANNISFactory.eINSTANCE.createRACorpus());
			fail("shall not write object, because taId is out of date");
		} catch (Exception e) {}
		
	}

	public void testBeginCommitWriteTA() 
	{
		try {
			this.getFixture().commitTA(0l);
			fail("shall not commit non existing ta");
		} catch (Exception e) {}
		
		Long taId= this.getFixture().beginTA();
		this.getFixture().commitTA(taId);
	}
	
	public void testBeginCommitTA() 
	{
		try {
			this.getFixture().commitTA(0l);
			fail("shall not commit non existing ta");
		} catch (Exception e) {}
		
		Long taId= this.getFixture().beginTA();
		this.getFixture().commitTA(taId);
	}
	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#beginTA() <em>Begin TA</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#beginTA()
	 */
	public void testBeginTA() 
	{
		this.testBeginCommitTA();
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#commitTA(long) <em>Commit TA</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#commitTA(long)
	 */
	public void testCommitTA__long() 
	{
		this.testBeginCommitTA();
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#addTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter) <em>Add Tuple Writer</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#addTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter)
	 */
	public void testAddTupleWriter__DAOOBJECT_ITupleWriter() 
	{
		ITupleWriter tw= null;
		DAOOBJECT daoObject= null;
		
		try {
			this.getFixture().addTupleWriter(daoObject, tw);
			fail("shall not add empty objects");
		} catch (Exception e) {}
		
		daoObject= DAOOBJECT.CORPUS;
		try {
			this.getFixture().addTupleWriter(daoObject, tw);
			fail("shall not add an empty tupleWriter");
		} catch (Exception e) {}
		
		daoObject= null;
		tw= new TupleWriter();
		try {
			this.getFixture().addTupleWriter(daoObject, tw);
			fail("shall not add an empty daoObject");
		} catch (Exception e) {}
		
		daoObject= DAOOBJECT.CORPUS;
		this.getFixture().addTupleWriter(daoObject, tw);
		assertEquals(tw, this.getFixture().getTupleWriter(daoObject));
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#reSetTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter) <em>Re Set Tuple Writer</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#reSetTupleWriter(exporter.relANNIS.DAOOBJECT, de.dataconnector.tupleconnector.ITupleWriter)
	 */
	public void testReSetTupleWriter__DAOOBJECT_ITupleWriter() 
	{
		ITupleWriter tw= null;
		DAOOBJECT daoObject= null;
		
		try {
			this.getFixture().reSetTupleWriter(daoObject, tw);
			fail("shall not add empty objects");
		} catch (Exception e) {}
		
		daoObject= DAOOBJECT.CORPUS;
		try {
			this.getFixture().reSetTupleWriter(daoObject, tw);
			fail("shall not add an empty tupleWriter");
		} catch (Exception e) {}
		
		daoObject= null;
		tw= new TupleWriter();
		try {
			this.getFixture().reSetTupleWriter(daoObject, tw);
			fail("shall not add an empty daoObject");
		} catch (Exception e) {}
		
		daoObject= DAOOBJECT.CORPUS;
		this.getFixture().reSetTupleWriter(daoObject, tw);
		assertEquals(tw, this.getFixture().getTupleWriter(daoObject));
		tw= new TupleWriter();
		this.getFixture().reSetTupleWriter(daoObject, tw);
		assertEquals(tw, this.getFixture().getTupleWriter(daoObject));
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#getTupleWriter(exporter.relANNIS.DAOOBJECT) <em>Get Tuple Writer</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#getTupleWriter(exporter.relANNIS.DAOOBJECT)
	 */
	public void testGetTupleWriter__DAOOBJECT() 
	{
		for (DAOOBJECT daoObject: DAOOBJECT.VALUES)
		{	
			assertNull("shall not get a tuple writer before setOutputDir was called", this.getFixture().getTupleWriter(daoObject));
		}
		
		
		this.getFixture().setOutputDir(new File("outputDir/"));
		//checking if all daoObjects are initialized
		for (DAOOBJECT daoObject: DAOOBJECT.VALUES)
		{	
			assertNotNull(this.getFixture().getTupleWriter(daoObject));
		}
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpusMetaAttribute) <em>Write</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#write(java.lang.Long, exporter.relANNIS.RACorpusMetaAttribute)
	 * @generated
	 */
	public void testWrite__Long_RACorpusMetaAttribute() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

	/**
	 * Tests the '{@link exporter.relANNIS.RADAO#getUniqueValue(exporter.relANNIS.UNIQUE_VALUES) <em>Get Unique Value</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see exporter.relANNIS.RADAO#getUniqueValue(exporter.relANNIS.UNIQUE_VALUES)
	 */
	public void testGetUniqueValue__UNIQUE_VALUES() 
	{
		for (UNIQUE_VALUES uniqueValue: UNIQUE_VALUES.values())
		{
			for (Long i= 0l; i < 10; i++)
			{
				assertEquals(i, this.getFixture().getUniqueValue(uniqueValue));
			}
		}
	}

} //RADAOTest
