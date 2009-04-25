/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import de.corpling.salt.model.salt.SCorpus;
import de.corpling.peper.modules.relANNIS.RAExporter;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>RA Exporter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following operations are tested:
 * <ul>
 * </ul>
 * </p>
 * @generated
 */
public class RAExporterTest extends TestCase {

	/**
	 * The fixture for this RA Exporter test case.
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
		TestRunner.run(RAExporterTest.class);
	}

	/**
	 * Constructs a new RA Exporter test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAExporterTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this RA Exporter test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(RAExporter fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this RA Exporter test case.
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
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(RelANNISFactory.eINSTANCE.createRAExporter());
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void testExport__SCorpus() 
	{
		SCorpus sCorpus= null;
		try {
			this.getFixture().export(sCorpus);
			fail("Shall not export, because project, output directory and corpus aren´t set.");
		} catch (Exception e) {
		}		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void testExport__SDocument() {
		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
	}

} //RAExporterTest
