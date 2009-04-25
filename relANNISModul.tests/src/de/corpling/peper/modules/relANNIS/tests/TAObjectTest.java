/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.TAObject;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>TA Object</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TAObjectTest extends TestCase {

	/**
	 * The fixture for this TA Object test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TAObject fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TAObjectTest.class);
	}

	/**
	 * Constructs a new TA Object test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TAObjectTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this TA Object test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(TAObject fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this TA Object test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TAObject getFixture() {
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
		setFixture(RelANNISFactory.eINSTANCE.createTAObject());
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

} //TAObjectTest
