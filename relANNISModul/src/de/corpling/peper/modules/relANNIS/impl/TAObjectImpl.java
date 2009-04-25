/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.DAOOBJECT;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TAObject;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>TA Object</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.TAObjectImpl#getTaId <em>Ta Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.TAObjectImpl#getTwWriterKey <em>Tw Writer Key</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TAObjectImpl extends EObjectImpl implements TAObject {
	/**
	 * The default value of the '{@link #getTaId() <em>Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaId()
	 * @generated
	 * @ordered
	 */
	protected static final Long TA_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTaId() <em>Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaId()
	 * @generated
	 * @ordered
	 */
	protected Long taId = TA_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getTwWriterKey() <em>Tw Writer Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTwWriterKey()
	 * @generated
	 * @ordered
	 */
	protected static final DAOOBJECT TW_WRITER_KEY_EDEFAULT = DAOOBJECT.CORPUS;

	/**
	 * The cached value of the '{@link #getTwWriterKey() <em>Tw Writer Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTwWriterKey()
	 * @generated
	 * @ordered
	 */
	protected DAOOBJECT twWriterKey = TW_WRITER_KEY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TAObjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.TA_OBJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getTaId() {
		return taId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTaId(Long newTaId) {
		Long oldTaId = taId;
		taId = newTaId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.TA_OBJECT__TA_ID, oldTaId, taId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DAOOBJECT getTwWriterKey() {
		return twWriterKey;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTwWriterKey(DAOOBJECT newTwWriterKey) {
		DAOOBJECT oldTwWriterKey = twWriterKey;
		twWriterKey = newTwWriterKey == null ? TW_WRITER_KEY_EDEFAULT : newTwWriterKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.TA_OBJECT__TW_WRITER_KEY, oldTwWriterKey, twWriterKey));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RelANNISPackage.TA_OBJECT__TA_ID:
				return getTaId();
			case RelANNISPackage.TA_OBJECT__TW_WRITER_KEY:
				return getTwWriterKey();
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
			case RelANNISPackage.TA_OBJECT__TA_ID:
				setTaId((Long)newValue);
				return;
			case RelANNISPackage.TA_OBJECT__TW_WRITER_KEY:
				setTwWriterKey((DAOOBJECT)newValue);
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
			case RelANNISPackage.TA_OBJECT__TA_ID:
				setTaId(TA_ID_EDEFAULT);
				return;
			case RelANNISPackage.TA_OBJECT__TW_WRITER_KEY:
				setTwWriterKey(TW_WRITER_KEY_EDEFAULT);
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
			case RelANNISPackage.TA_OBJECT__TA_ID:
				return TA_ID_EDEFAULT == null ? taId != null : !TA_ID_EDEFAULT.equals(taId);
			case RelANNISPackage.TA_OBJECT__TW_WRITER_KEY:
				return twWriterKey != TW_WRITER_KEY_EDEFAULT;
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
		result.append(" (taId: ");
		result.append(taId);
		result.append(", twWriterKey: ");
		result.append(twWriterKey);
		result.append(')');
		return result.toString();
	}

} //TAObjectImpl
