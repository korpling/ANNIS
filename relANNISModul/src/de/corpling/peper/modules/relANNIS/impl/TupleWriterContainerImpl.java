/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TupleWriterContainer;
import de.dataconnector.tupleconnector.ITupleWriter;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tuple Writer Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.TupleWriterContainerImpl#getTupleWriter <em>Tuple Writer</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TupleWriterContainerImpl extends EObjectImpl implements TupleWriterContainer {
	/**
	 * The default value of the '{@link #getTupleWriter() <em>Tuple Writer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTupleWriter()
	 * @generated
	 * @ordered
	 */
	protected static final ITupleWriter TUPLE_WRITER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTupleWriter() <em>Tuple Writer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTupleWriter()
	 * @generated
	 * @ordered
	 */
	protected ITupleWriter tupleWriter = TUPLE_WRITER_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TupleWriterContainerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.TUPLE_WRITER_CONTAINER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ITupleWriter getTupleWriter() {
		return tupleWriter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTupleWriter(ITupleWriter newTupleWriter) {
		ITupleWriter oldTupleWriter = tupleWriter;
		tupleWriter = newTupleWriter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.TUPLE_WRITER_CONTAINER__TUPLE_WRITER, oldTupleWriter, tupleWriter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RelANNISPackage.TUPLE_WRITER_CONTAINER__TUPLE_WRITER:
				return getTupleWriter();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RelANNISPackage.TUPLE_WRITER_CONTAINER__TUPLE_WRITER:
				setTupleWriter((ITupleWriter)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case RelANNISPackage.TUPLE_WRITER_CONTAINER__TUPLE_WRITER:
				setTupleWriter(TUPLE_WRITER_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case RelANNISPackage.TUPLE_WRITER_CONTAINER__TUPLE_WRITER:
				return TUPLE_WRITER_EDEFAULT == null ? tupleWriter != null : !TUPLE_WRITER_EDEFAULT.equals(tupleWriter);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (tupleWriter: ");
		result.append(tupleWriter);
		result.append(')');
		return result.toString();
	}

} //TupleWriterContainerImpl
