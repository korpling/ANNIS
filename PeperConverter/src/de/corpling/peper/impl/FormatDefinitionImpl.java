/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.FormatDefinition;
import de.corpling.peper.PeperPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Format Definition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.FormatDefinitionImpl#getFormatName <em>Format Name</em>}</li>
 *   <li>{@link de.corpling.peper.impl.FormatDefinitionImpl#getFormatVersion <em>Format Version</em>}</li>
 *   <li>{@link de.corpling.peper.impl.FormatDefinitionImpl#getFormatReference <em>Format Reference</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FormatDefinitionImpl extends EObjectImpl implements FormatDefinition {
	/**
	 * The default value of the '{@link #getFormatName() <em>Format Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatName()
	 * @generated
	 * @ordered
	 */
	protected static final String FORMAT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFormatName() <em>Format Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatName()
	 * @generated
	 * @ordered
	 */
	protected String formatName = FORMAT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getFormatVersion() <em>Format Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String FORMAT_VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFormatVersion() <em>Format Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatVersion()
	 * @generated
	 * @ordered
	 */
	protected String formatVersion = FORMAT_VERSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getFormatReference() <em>Format Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatReference()
	 * @generated
	 * @ordered
	 */
	protected static final String FORMAT_REFERENCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFormatReference() <em>Format Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormatReference()
	 * @generated
	 * @ordered
	 */
	protected String formatReference = FORMAT_REFERENCE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FormatDefinitionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.FORMAT_DEFINITION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFormatName() {
		return formatName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormatName(String newFormatName) {
		String oldFormatName = formatName;
		formatName = newFormatName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.FORMAT_DEFINITION__FORMAT_NAME, oldFormatName, formatName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFormatVersion() {
		return formatVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormatVersion(String newFormatVersion) {
		String oldFormatVersion = formatVersion;
		formatVersion = newFormatVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.FORMAT_DEFINITION__FORMAT_VERSION, oldFormatVersion, formatVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFormatReference() {
		return formatReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormatReference(String newFormatReference) {
		String oldFormatReference = formatReference;
		formatReference = newFormatReference;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.FORMAT_DEFINITION__FORMAT_REFERENCE, oldFormatReference, formatReference));
	}

	/**
	 * Returns true if and only if formatName and formatVersion are the same. 
	 * False if they aren´t the same or otherObject== null or formateName or formatVersion is false
	 */
	public boolean equals(FormatDefinition otherObject) 
	{
		boolean retVal= false;
		if (otherObject== null)
			retVal= false;
		else if ((this.getFormatName()== null) || this.getFormatVersion()== null)
			retVal= false;
		else
		{
			if ((this.getFormatName().equals(otherObject.getFormatName())) &&
				(this.getFormatVersion().equals(otherObject.getFormatVersion())))
			retVal= true;
		}
		return(retVal);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.FORMAT_DEFINITION__FORMAT_NAME:
				return getFormatName();
			case PeperPackage.FORMAT_DEFINITION__FORMAT_VERSION:
				return getFormatVersion();
			case PeperPackage.FORMAT_DEFINITION__FORMAT_REFERENCE:
				return getFormatReference();
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
			case PeperPackage.FORMAT_DEFINITION__FORMAT_NAME:
				setFormatName((String)newValue);
				return;
			case PeperPackage.FORMAT_DEFINITION__FORMAT_VERSION:
				setFormatVersion((String)newValue);
				return;
			case PeperPackage.FORMAT_DEFINITION__FORMAT_REFERENCE:
				setFormatReference((String)newValue);
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
			case PeperPackage.FORMAT_DEFINITION__FORMAT_NAME:
				setFormatName(FORMAT_NAME_EDEFAULT);
				return;
			case PeperPackage.FORMAT_DEFINITION__FORMAT_VERSION:
				setFormatVersion(FORMAT_VERSION_EDEFAULT);
				return;
			case PeperPackage.FORMAT_DEFINITION__FORMAT_REFERENCE:
				setFormatReference(FORMAT_REFERENCE_EDEFAULT);
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
			case PeperPackage.FORMAT_DEFINITION__FORMAT_NAME:
				return FORMAT_NAME_EDEFAULT == null ? formatName != null : !FORMAT_NAME_EDEFAULT.equals(formatName);
			case PeperPackage.FORMAT_DEFINITION__FORMAT_VERSION:
				return FORMAT_VERSION_EDEFAULT == null ? formatVersion != null : !FORMAT_VERSION_EDEFAULT.equals(formatVersion);
			case PeperPackage.FORMAT_DEFINITION__FORMAT_REFERENCE:
				return FORMAT_REFERENCE_EDEFAULT == null ? formatReference != null : !FORMAT_REFERENCE_EDEFAULT.equals(formatReference);
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
		result.append(" (formatName: ");
		result.append(formatName);
		result.append(", formatVersion: ");
		result.append(formatVersion);
		result.append(", formatReference: ");
		result.append(formatReference);
		result.append(')');
		return result.toString();
	}

} //FormatDefinitionImpl
