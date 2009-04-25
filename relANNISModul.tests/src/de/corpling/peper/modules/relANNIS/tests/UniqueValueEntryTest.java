/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.UNIQUE_VALUES;
import de.corpling.peper.modules.relANNIS.UniqueValue;

import java.util.Map;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Unique Value Entry</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class UniqueValueEntryTest extends TestCase {

	/**
	 * The fixture for this Unique Value Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<UNIQUE_VALUES, UniqueValue> fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(UniqueValueEntryTest.class);
	}

	/**
	 * Constructs a new Unique Value Entry test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UniqueValueEntryTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Unique Value Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Map.Entry<UNIQUE_VALUES, UniqueValue> fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Unique Value Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<UNIQUE_VALUES, UniqueValue> getFixture() {
		return fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		setFixture((Map.Entry<UNIQUE_VALUES, UniqueValue>)RelANNISFactory.eINSTANCE.create(RelANNISPackage.Literals.UNIQUE_VALUE_ENTRY));
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

} //UniqueValueEntryTest
