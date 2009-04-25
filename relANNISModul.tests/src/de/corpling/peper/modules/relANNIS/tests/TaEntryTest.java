/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TAObject;

import java.util.Map;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Ta Entry</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TaEntryTest extends TestCase {

	/**
	 * The fixture for this Ta Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<Long, EList<TAObject>> fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TaEntryTest.class);
	}

	/**
	 * Constructs a new Ta Entry test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TaEntryTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Ta Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Map.Entry<Long, EList<TAObject>> fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Ta Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<Long, EList<TAObject>> getFixture() {
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
		setFixture((Map.Entry<Long, EList<TAObject>>)RelANNISFactory.eINSTANCE.create(RelANNISPackage.Literals.TA_ENTRY));
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

} //TaEntryTest
