/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests.singleCases;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>relANNIS</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class AllExampleTests extends TestSuite {

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
		TestSuite suite = new AllExampleTests("relANNIS Tests for all examples");
		suite.addTestSuite(TestText_Case1.class);
		suite.addTestSuite(TestText_Case2.class);
		//just Token
		suite.addTestSuite(TestToken_Case1.class);
		
		//Token and PR
		suite.addTestSuite(TestToken_Case2.class);
		suite.addTestSuite(TestToken_Case3.class);
		suite.addTestSuite(TestToken_Case4.class);
		suite.addTestSuite(TestToken_Case5.class);
		
		//PR
		suite.addTestSuite(TestPointingRel_Case1.class);
		suite.addTestSuite(TestPointingRel_Case2.class);
		
		//tests a relation (PR)
		suite.addTestSuite(TestRelation_Case1.class);
		
		//spanRel
		suite.addTestSuite(TestSpan_Case1.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AllExampleTests(String name) {
		super(name);
	}

} //RelANNISTests
