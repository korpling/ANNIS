/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>relANNIS</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class RelANNISTests extends TestSuite {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static Test suite() {
		TestSuite suite = new RelANNISTests("relANNIS Tests");
		suite.addTestSuite(RAExporterTest.class);
		suite.addTestSuite(RAMapperTest.class);
		suite.addTestSuite(RADAOTest.class);
		suite.addTestSuite(RACorpusTest.class);
		suite.addTestSuite(RACorpusAnnotationTest.class);
		suite.addTestSuite(RATextTest.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISTests(String name) {
		super(name);
	}

} //RelANNISTests
