/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.FormatDefinition;
import de.corpling.peper.ImExportSet;
import de.corpling.peper.PeperPackage;

import java.io.File;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Im Export Set</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ImExportSetImpl#getDataSourcePath <em>Data Source Path</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImExportSetImpl#getFormatDefinition <em>Format Definition</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImExportSetImpl extends EObjectImpl implements ImExportSet {
	/**
	 * The default value of the '{@link #getDataSourcePath() <em>Data Source Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataSourcePath()
	 * @generated
	 * @ordered
	 */
	protected static final File DATA_SOURCE_PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDataSourcePath() <em>Data Source Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDataSourcePath()
	 * @generated
	 * @ordered
	 */
	protected File dataSourcePath = DATA_SOURCE_PATH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getFormatDefinition() <em>Format Definition</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatDefinition()
	 * @generated
	 * @ordered
	 */
	protected FormatDefinition formatDefinition;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImExportSetImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.IM_EXPORT_SET;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File getDataSourcePath() {
		return dataSourcePath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDataSourcePath(File newDataSourcePath) {
		File oldDataSourcePath = dataSourcePath;
		dataSourcePath = newDataSourcePath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IM_EXPORT_SET__DATA_SOURCE_PATH, oldDataSourcePath, dataSourcePath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FormatDefinition getFormatDefinition() {
		return formatDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFormatDefinition(FormatDefinition newFormatDefinition, NotificationChain msgs) {
		FormatDefinition oldFormatDefinition = formatDefinition;
		formatDefinition = newFormatDefinition;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION, oldFormatDefinition, newFormatDefinition);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormatDefinition(FormatDefinition newFormatDefinition) {
		if (newFormatDefinition != formatDefinition) {
			NotificationChain msgs = null;
			if (formatDefinition != null)
				msgs = ((InternalEObject)formatDefinition).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION, null, msgs);
			if (newFormatDefinition != null)
				msgs = ((InternalEObject)newFormatDefinition).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION, null, msgs);
			msgs = basicSetFormatDefinition(newFormatDefinition, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION, newFormatDefinition, newFormatDefinition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION:
				return basicSetFormatDefinition(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.IM_EXPORT_SET__DATA_SOURCE_PATH:
				return getDataSourcePath();
			case PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION:
				return getFormatDefinition();
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
			case PeperPackage.IM_EXPORT_SET__DATA_SOURCE_PATH:
				setDataSourcePath((File)newValue);
				return;
			case PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION:
				setFormatDefinition((FormatDefinition)newValue);
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
			case PeperPackage.IM_EXPORT_SET__DATA_SOURCE_PATH:
				setDataSourcePath(DATA_SOURCE_PATH_EDEFAULT);
				return;
			case PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION:
				setFormatDefinition((FormatDefinition)null);
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
			case PeperPackage.IM_EXPORT_SET__DATA_SOURCE_PATH:
				return DATA_SOURCE_PATH_EDEFAULT == null ? dataSourcePath != null : !DATA_SOURCE_PATH_EDEFAULT.equals(dataSourcePath);
			case PeperPackage.IM_EXPORT_SET__FORMAT_DEFINITION:
				return formatDefinition != null;
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
		result.append(" (dataSourcePath: ");
		result.append(dataSourcePath);
		result.append(')');
		return result.toString();
	}

} //ImExportSetImpl
