/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.tests;

import de.corpling.peper.modules.relANNIS.DAOOBJECT;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TupleWriterContainer;

import java.util.Map;

import junit.framework.TestCase;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Tuple Writer Entry</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class TupleWriterEntryTest extends TestCase {

	/**
	 * The fixture for this Tuple Writer Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<DAOOBJECT, TupleWriterContainer> fixture = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(TupleWriterEntryTest.class);
	}

	/**
	 * Constructs a new Tuple Writer Entry test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TupleWriterEntryTest(String name) {
		super(name);
	}

	/**
	 * Sets the fixture for this Tuple Writer Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void setFixture(Map.Entry<DAOOBJECT, TupleWriterContainer> fixture) {
		this.fixture = fixture;
	}

	/**
	 * Returns the fixture for this Tuple Writer Entry test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Map.Entry<DAOOBJECT, TupleWriterContainer> getFixture() {
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
		setFixture((Map.Entry<DAOOBJECT, TupleWriterContainer>)RelANNISFactory.eINSTANCE.create(RelANNISPackage.Literals.TUPLE_WRITER_ENTRY));
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

} //TupleWriterEntryTest
